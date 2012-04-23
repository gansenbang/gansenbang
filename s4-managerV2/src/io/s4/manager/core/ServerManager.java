package io.s4.manager.core;

import io.s4.manager.persist.MachineInfo;
import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser.Cluster.ClusterType;
import io.s4.manager.util.ConfigParser.ClusterNode;
import io.s4.manager.util.ConfigParser.Config;
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
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

public class ServerManager extends DefaultWatcher {
	private Object lock = new Object();
	private String zkAddress;
	private Map<String, ClusterStatus> S4Cluster = new HashMap<String, ClusterStatus>();
	private Map<String, Map<String, String>> ClusterMessage = new HashMap<String, Map<String, String>>();
	private Map<String, MachineInfo> MachineMap = new HashMap<String, MachineInfo>();
	private Configuration config;
	private int nodecount = 0;

	public ServerManager(String zkAddress, String ConfigFileUrl) throws Exception{
		super(zkAddress);
		this.zkAddress = zkAddress;
		this.config = new Configuration(zkAddress, ConfigFileUrl);
		if(this.config.GetConfig() != null){
			for(Cluster cluster : this.config.GetConfig().getClusters()){
				SetupS4Cluster(cluster, this.config.GetConfig().getVersion(), true);
			}
		}
	}

	public boolean shutdown(){
		config.close();
		return this.RemoveAllS4Cluster();
	}
	/*
	 * param ClusterConfig:集群的配置信息
	 * param clean:是否清空原有配置
	 */
	public void TaskSetup(String ClusterConfig) throws Exception {
		synchronized (lock) {
			this.nodecount = 0;
			S4Cluster.clear();
			Config config = this.config.ResetConfig(ClusterConfig);
			if(config != null){
				for(Cluster cluster : config.getClusters()){
					SetupS4Cluster(cluster, config.getVersion(), true);
				}
			}
		}
	}

	private void SetupS4Cluster(Cluster cluster, String version, boolean clean)
			throws Exception {
		String Name = cluster.getName();
		ClusterType Type = cluster.getType();
		String Mode = cluster.getMode();
		ClusterStatus cs = S4Cluster.get(Name);
		if(cs == null){
			cs = new ClusterStatus(Name, Type, Mode);
		}
	
		List<ClusterNode> nodelist = cluster.getNodes();
		for (ClusterNode node : nodelist) {
			String hostport = node.getMachineName() + ":" + node.getPort();
			if (this.MachineMap.get(hostport) != null)
				cs.getMahcineMap().put(hostport, this.MachineMap.get(hostport));
		}

		S4Cluster.put(Name, cs);
		TaskSetupApp.processCluster(clean, zkAddress, cluster, version);
	}

	/*
	 * param NodeConfig
	 * 格式为：
	* <config version="-1">
  *<cluster name="s4" type="s4" mode="unicast">
  * <node>
  *   <partition>0</partition>
  *   <machine>192.168.1.15</machine>
  *   <port>5077</port>
  *   <taskId>s4node-0</taskId>
  * </node>  
  *</cluster>
  *</config>
	 */
	private Cluster AddZkTask(String NodeConfig) throws Exception{
		synchronized(lock){
			Config nodeconfig = this.config.addnodeinfo(NodeConfig);
			if(nodeconfig != null){
				Cluster cluster = nodeconfig.getClusters().get(0);
				for(ClusterNode node : cluster.getNodes()){
					String Name = cluster.getName();
					ClusterType Type = cluster.getType();
					int partitionid = node.getPartition();
					String znodepath = "/" + Name + "/" + Type.toString() + "/task/task-" + partitionid;
					Stat tExist = zk.exists(znodepath, false);
					if(tExist != null){
						return null;
					}
				}
				//set up cluster for zk
				SetupS4Cluster(cluster, nodeconfig.getVersion(), false);
				return nodeconfig.getClusters().get(0);
			}
		}
		return null;
	}
	
	private boolean DelZkTask(String taskpath){
		synchronized(lock){
			try {
				Stat tExist = zk.exists(taskpath, false);
				if(tExist != null){
					zk.delete(taskpath, 0);
				}
			} catch (Exception e) {
				return false;
			} 
		}
		return false;
	}
	
	public Map<String, MachineInfo> getMachineMap() {
		return MachineMap;
	}

	public int GetNodeCount() {
		return nodecount;
	}

	/*
	 * 返回当前集群信息，主要的格式为一个s4cluster里面包含的节点信息，节点信息以key=hostport，value=znode为对形成
	 * 的map
	 */
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
			ClusterType clustertype) throws KeeperException, InterruptedException {
		String pZnode = "/" + clustername + "/" + clustertype.toString() + "/process";
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
						MachineInfo minfo = cs.getMahcineMap().get(address + ":" + port);
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

	/*
	 * 通过提供一个hostport值，可以达到恢复或启动一个S4Server节点
	 * param ClusterName：表明是哪个s4cluster
	 * param AdapterClusterName:表明该S4Server以后会把一些请求结果返回到那个adapter中（这个参数并不重要）
	 * param hostport：指明将要恢复和启动的节点地址
	 */
	public boolean RecoveryS4Server(String ClusterName, String AdapterClusterName, String hostport) {
		String command = this.makeStartS4Command(ClusterName, AdapterClusterName);
		ClusterStatus cs = this.S4Cluster.get(ClusterName);
		if (cs != null) {
			SSHWrapper sw = new SSHWrapper();
			try {
				MachineInfo minfo = cs.getMahcineMap().get(hostport);
				if (minfo != null) {
					sw.Authenticated(minfo.host, minfo.user, minfo.pass);
					sw.RunRemoteCommand(command);
					sw.close();
					return true;
				}
			} catch (IOException e) {
			}
		}
		return false;
	}

	/*
	 * 恢复或启动一个clientadapter
	 * param ClusterName：表明是哪个s4clustername
	 * param ListenAppName:表明将要发送消息到那个s4cluster
	 * param hostport：表明将要恢复和启动哪一个节点
	 */
	public boolean RecoveryClientAdapter(String ClusterName, String ListenAppName, String hostport) {
		String command = this.makeRunClientAdapterCommand(ClusterName, ListenAppName);
		ClusterStatus cs = this.S4Cluster.get(ClusterName);
		if (cs != null) {
			SSHWrapper sw = new SSHWrapper();
			try {
				MachineInfo minfo = cs.getMahcineMap().get(hostport);
				if (minfo != null) {
					sw.Authenticated(minfo.host, minfo.user, minfo.pass);
					sw.RunRemoteCommand(command);
					sw.close();
					return true;
				}
			} catch (IOException e) {
			}
		}
		return false;
	}

	/*
	 * 启动整一个clientadapter的集群
	 */
	public boolean StartClientAdapterCluster(String ClusterName, String ListenAppName) {
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

	/*
	 * 启动整一个S4Server的集群
	 */
	public boolean StartS4ServerCluster(String ClusterName,
			String AdapterClusterName) {
		if (isNullString(ClusterName) || isNullString(AdapterClusterName))
			return false;
		String command = makeStartS4Command(ClusterName, AdapterClusterName);
		return StartS4Cluster(ClusterName, command);
	}
	
	/*
	 * 增加一个S4Server的节点
	 */
	public boolean AddS4Server(String ClusterName, String AdapterClusterName, String NodeConfig){
		try {
			Cluster nodecluster = this.AddZkTask(NodeConfig);
			if(nodecluster != null){
				for(ClusterNode node : nodecluster.getNodes()){
					String hostport = node.getMachineName() + ":" + node.getPort();
					this.RecoveryS4Server(ClusterName, AdapterClusterName, hostport);
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/*
	 * 增加一个clientadapter节点
	 */
	public boolean AddClientAdapter(String ClusterName, String ListenAppName, String NodeConfig){
		try {
			Cluster nodecluster = this.AddZkTask(NodeConfig);
			if(nodecluster != null){
				for(ClusterNode node : nodecluster.getNodes()){
					String hostport = node.getMachineName() + ":" + node.getPort();
					this.RecoveryClientAdapter(ClusterName, ListenAppName, hostport);
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/*
	 * 删除一个S4节点，包括S4Server和ClientAdapter
	 */
	public boolean RomoveS4Node(String s4clustername, String hostport){
		synchronized(lock){
			ClusterStatus cs = this.S4Cluster.get(s4clustername);
			MachineInfo minfo = cs.getMahcineMap().get(hostport);
			boolean isnull = isNullString(minfo.znode);
			if(!isnull){
				SSHWrapper sw = new SSHWrapper();
				try {
					Stat pExist = zk.exists(minfo.znode, false);
					if(pExist != null){
						byte[] bytes = zk.getData(minfo.znode, false, pExist);
						Map<String, Object> map = (Map<String, Object>) JSONUtil.getMapFromJson(new String(bytes));
						String address = (String)map.get("address");
						String PID = (String)map.get("PID");
						sw.Authenticated(address, minfo.user, minfo.pass);
						sw.RunRemoteCommand("kill " + PID);
						
						//delete zknode and confignode
						String pNodePath = minfo.znode.trim();
						if(pNodePath.startsWith("/")){
							String pProcess = (pNodePath.split("/"))[4];
							String taskid = (pProcess.split("-"))[1];
							String taskpath = "/" + cs.getName() + "/" + cs.getType() + "/task/task-" +
										taskid;
							this.DelZkTask(taskpath);
							config.delnodeinfo(cs.getName(), cs.getType(), Integer.parseInt(taskid));
						}
					}
				} catch (KeeperException e) {
					
				} catch (InterruptedException e) {
					
				} catch (IOException e) {
					
				} finally {
					sw.close();
				}
			}
		}
		return false;
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

	private synchronized boolean StartS4Cluster(String ClusterName, String Command) {
		SSHWrapper sw = new SSHWrapper();
		try {
			ClusterStatus cs = this.S4Cluster.get(ClusterName);
			if (cs != null) {
				Set<String> s4cluset = cs.getMahcineMap().keySet();
				if (s4cluset != null) {
					for (String s4clustername : s4cluset) {
						MachineInfo minfo = cs.getMahcineMap().get(s4clustername);
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

	/*
	 * 移除整一个集群
	 */
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

	/*
	 * 移除某一个S4cluster
	 */
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
						byte[] bytes = zk.getData(nodefullpath, false, pTaskStat);
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

		public Map<String, MachineInfo> getMahcineMap() {
			return this.machinemap;
		}
	}

}
