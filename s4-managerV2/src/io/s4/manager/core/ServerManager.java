package io.s4.manager.core;

import io.s4.manager.persist.MachineInfo;
import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser.ClusterNode;
import io.s4.manager.util.ConfigParser.Config;
import io.s4.manager.util.ConfigParser.Cluster.ClusterType;
import io.s4.manager.util.ConfigParser;
import io.s4.manager.util.JSONUtil;
import io.s4.manager.util.SSHWrapper;
import io.s4.manager.util.TaskSetupApp;
import io.s4.manager.zk.DefaultWatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

public class ServerManager extends DefaultWatcher {
	private Object lock = new Object();
	private String zkAddress;
	private Map<String, ClusterStatus> S4Cluster = new HashMap<String, ClusterStatus>();
	private Map<String, Map<String, String>> ClusterMessage = new HashMap<String, Map<String, String>>();
	private Map<String, MachineInfo> MachineMap = new HashMap<String, MachineInfo>();
	private int nodecount = 0;

	public ServerManager(String zkAddress) throws Exception {
		super(zkAddress);
		this.zkAddress = zkAddress;
	}

	public void TaskSetup(String ClusterConfig, boolean clean,
			List<String> s4clustersname) throws Exception {
		if (clean)
			TaskSetupAll(ClusterConfig);
		else
			TaskSetupWithName(ClusterConfig, s4clustersname);
	}

	private void TaskSetupWithName(String ClusterConfig,
			List<String> s4clustersname) throws Exception {
		synchronized (lock) {
			ConfigParser parser = new ConfigParser();
			Config config = parser.parse(ClusterConfig);
			this.nodecount = 0;
			for (Cluster cluster : config.getClusters()) {
				if (s4clustersname != null) {
					for (String s4cluster : s4clustersname) {
						if (cluster.getName().equals(s4cluster)) {
							SetupS4Cluster(cluster, config.getVersion());
						}
					}
				}
			}
		}
	}

	private void TaskSetupAll(String ClusterConfig) throws Exception {
		synchronized (lock) {
			ConfigParser parser = new ConfigParser();
			Config config = parser.parse(ClusterConfig);
			this.nodecount = 0;
			S4Cluster.clear();
			for (Cluster cluster : config.getClusters()) {
				SetupS4Cluster(cluster, config.getVersion());
			}
		}
	}

	private void SetupS4Cluster(Cluster cluster, String version)
			throws Exception {
		String Name = cluster.getName();
		ClusterType Type = cluster.getType();
		String Mode = cluster.getMode();
		ClusterStatus cs = new ClusterStatus(Name, Type, Mode);
		List<ClusterNode> nodelist = cluster.getNodes();

		for (ClusterNode node : nodelist) {
			String hostport = node.getMachineName() + ":" + node.getPort();
			if (this.MachineMap.get(hostport) != null)
				cs.getMahcineMap().put(hostport, this.MachineMap.get(hostport));
		}

		S4Cluster.put(Name, cs);
		TaskSetupApp.processCluster(true, zkAddress, cluster, version);
	}

	public Map<String, MachineInfo> getMachineMap() {
		return MachineMap;
	}

	public int GetNodeCount() {
		return nodecount;
	}

	public Map<String, Map<String, String>> GetClusterMessage() {
		try {
			Set<String> clusterset = this.S4Cluster.keySet();
			for (String s4cluster : clusterset) {
				ClusterStatus cs = this.S4Cluster.get(s4cluster);
				readProcessConfig(cs, cs.getName(), cs.getType());
			}
			ClusterMessage.clear();

			for (String s4cluster : clusterset) {
				Map<String, String> map = new HashMap<String, String>();
				ClusterStatus cs = this.S4Cluster.get(s4cluster);
				Map<String, MachineInfo> minfomap = cs.getMahcineMap();
				Set<String> hostportset = minfomap.keySet();

				for (String hostport : hostportset) {
					String znode = minfomap.get(hostport).znode;
					map.put(hostport, znode == null ? "null" : znode);
				}

				this.ClusterMessage.put(s4cluster, map);
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return this.ClusterMessage;
	}

	private void readProcessConfig(ClusterStatus cs, String clustername,
			ClusterType clustertype) throws KeeperException,
			InterruptedException {
		String pZnode = "/" + clustername + "/" + clustertype.toString()
				+ "/process";
		Stat pExists = zk.exists(pZnode, false);
		if (pExists != null) {
			List<String> pZnodeChildren = zk.getChildren(pZnode, true);
			for (String node : pZnodeChildren) {
				String nodeFullPath = pZnode + "/" + node;
				Stat pNodeStat = zk.exists(nodeFullPath, false);
				if (pNodeStat != null) {
					byte[] bytes = zk.getData(nodeFullPath, false, pNodeStat);
					Map<String, Object> map = (Map<String, Object>) JSONUtil
							.getMapFromJson(new String(bytes));
					String address = (String) map.get("address");
					String port = (String) map.get("port");
					if (cs != null) {
						MachineInfo minfo = cs.getMahcineMap().get(
								address + ":" + port);
						if (minfo != null) {
							minfo.znode = nodeFullPath;
							cs.getMahcineMap().put(address + ":" + port, minfo);
						}
					}
				}
			}
		}
	}

	@Override
	public synchronized void process(WatchedEvent event) {
		super.process(event);
		if (event.getType() == EventType.NodeChildrenChanged) {
			Set<String> s4clusterset = this.S4Cluster.keySet();
			for (String s4cluster : s4clusterset) {
				ClusterStatus cs = this.S4Cluster.get(s4cluster);
				String pZnode = "/" + cs.getName() + "/"
						+ cs.getType().toString() + "/process";
				try {
					zk.exists(pZnode, true);
				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean RecoveryS4Server(String ClusterName,
			String AdapterClusterName, String hostport) {
		String command = this.makeStartS4Command(ClusterName,
				AdapterClusterName);
		ClusterStatus cs = this.S4Cluster.get(ClusterName);
		if (cs != null) {
			SSHWrapper sw = new SSHWrapper();
			try {
				MachineInfo minfo = cs.getMahcineMap().get(hostport);
				if (minfo != null) {
					sw.Authenticated(minfo.host, minfo.user, minfo.pass);
					sw.RunRemoteCommand(command);
					sw.close();
				}
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	public boolean RecoveryClientAdapter(String ClusterName,
			String ListenAppName, String hostport) {
		String command = this.makeRunClientAdapterCommand(ClusterName,
				ListenAppName);
		ClusterStatus cs = this.S4Cluster.get(ClusterName);
		if (cs != null) {
			SSHWrapper sw = new SSHWrapper();
			try {
				MachineInfo minfo = cs.getMahcineMap().get(hostport);
				if (minfo != null) {
					sw.Authenticated(minfo.host, minfo.user, minfo.pass);
					sw.RunRemoteCommand(command);
					sw.close();
				}
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

	public boolean StartClientAdapterCluster(String ClusterName,
			String ListenAppName) {
		if (isNullString(ClusterName) || isNullString(ListenAppName))
			return false;
		String command = makeRunClientAdapterCommand(ClusterName, ListenAppName);
		return StartS4Cluster(ClusterName, command);
	}

	private String makeRunClientAdapterCommand(String ClusterName,
			String ListenAppName) {
		String command = "run-client-adapter.sh -s " + ClusterName + " -g s4 "
				+ ListenAppName
				+ " -d $S4_IMAGE/s4-core/conf/dynamic/client-stub-conf.xml "
				+ " -z " + this.zkAddress + " dynamic &";
		return command;
	}

	public boolean StartS4ServerCluster(String ClusterName,
			String AdapterClusterName) {
		if (isNullString(ClusterName) || isNullString(AdapterClusterName))
			return false;
		String command = makeStartS4Command(ClusterName, AdapterClusterName);
		return StartS4Cluster(ClusterName, command);
	}

	private String makeStartS4Command(String ClusterName,
			String AdapterClusterName) {
		String command = "/start-s4.sh -r " + AdapterClusterName + " -g "
				+ ClusterName + " -z " + this.zkAddress + " dynamic &";
		return command;
	}

	private boolean isNullString(String target) {
		if (target == null || target.equals(""))
			return true;
		return false;
	}

	private synchronized boolean StartS4Cluster(String ClusterName,
			String Command) {
		SSHWrapper sw = new SSHWrapper();
		try {
			ClusterStatus cs = this.S4Cluster.get(ClusterName);
			if (cs != null) {
				Set<String> s4cluset = cs.getMahcineMap().keySet();
				if (s4cluset != null) {
					for (String s4clustername : s4cluset) {
						MachineInfo minfo = cs.getMahcineMap().get(
								s4clustername);
						sw.Authenticated(minfo.host, minfo.user, minfo.pass);
						sw.RunRemoteCommand(minfo.s4image + Command);
						sw.close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean RemoveAllS4Cluster() {
		Set<String> s4cluset = this.S4Cluster.keySet();
		for (String s4clustername : s4cluset) {
			ClusterStatus cs = this.S4Cluster.get(s4clustername);
			boolean isSucc = RemoveS4Cluster(cs);
			if (!isSucc)
				return false;
		}
		return true;
	}

	public boolean RemoveS4Cluster(String s4clustername) {
		ClusterStatus cs = this.S4Cluster.get(s4clustername);
		if (cs != null) {
			return RemoveS4Cluster(cs);
		}
		return false;
	}

	public boolean RemoveS4Cluster(ClusterStatus cs) {
		String processNode = "/" + cs.getName() + "/" + cs.getType().toString()
				+ "/process";
		SSHWrapper sw = new SSHWrapper();
		try {
			Stat pNodeStat = zk.exists(processNode, false);
			if (pNodeStat != null) {
				List<String> prochildren = zk.getChildren(processNode, false);
				for (String nodepath : prochildren) {
					String nodefullpath = processNode + "/" + nodepath;
					Stat pTaskStat = zk.exists(nodefullpath, false);
					if (pTaskStat != null) {
						byte[] bytes = zk.getData(nodefullpath, false,
								pTaskStat);
						Map<String, Object> map = (Map<String, Object>) JSONUtil.getMapFromJson(new String(bytes));
						String address = (String) map.get("address");
						String port = (String) map.get("port");
						String pid = (String) map.get("PID");
						MachineInfo minfo = cs.getMahcineMap().get(address + ":" + port);
						String user = minfo.user;
						String pass = minfo.pass;
						sw.Authenticated(address, user, pass);
						sw.RunRemoteCommand("kill " + pid);
						sw.close();
						System.out.println(minfo);
						System.out.println("PID=" + pid);
					}
				}
			}
		} catch (KeeperException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private class ClusterStatus {
		private String name;
		private ClusterType type;
		private String mode;
		private Map<String, MachineInfo> machinemap = new HashMap<String, MachineInfo>();
		private volatile boolean updateMode = false;
		private volatile boolean changeMode = false;

		public ClusterStatus(String name, ClusterType type, String mode) {
			this.name = name;
			this.type = type;
			this.mode = mode;
		}

		public String getName() {
			return name;
		}

		public ClusterType getType() {
			return type;
		}

		public String getMode() {
			return mode;
		}

		public boolean isUpdateMode() {
			return updateMode;
		}

		public void setUpdateMode(boolean updateMode) {
			this.updateMode = updateMode;
		}

		public boolean isChangeMode() {
			return changeMode;
		}

		public void setChangeMode(boolean changeMode) {
			this.changeMode = changeMode;
		}

		public Map<String, MachineInfo> getMahcineMap() {
			return this.machinemap;
		}
	}

	public static void main(String[] args) {

	}

}
