package io.s4.manager.core;

import io.s4.manager.persist.MachineInfo;
import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser.Cluster.ClusterType;
import io.s4.manager.util.ConfigParser.ClusterNode;
import io.s4.manager.util.ConfigParser.Config;
import io.s4.manager.util.JSONUtil;
import io.s4.manager.util.SSHWrapper;
import io.s4.manager.zk.DefaultWatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

public class S4ClusterManager extends DefaultWatcher {
	private String zkAddress;
	private Map<String, Map<String, String>> ClusterMessage = new HashMap<String, Map<String, String>>();
	private Map<String, MachineInfo> MachineMap = new HashMap<String, MachineInfo>();
	private FileConfiguration fconfig;
	private ZKConfiguration zkconfig;
	private int nodecount = 0;

	public S4ClusterManager(String zkAddress, String ConfigFileUrl) throws Exception{
		super(zkAddress);
		this.zkAddress = zkAddress;
		this.fconfig = new FileConfiguration(ConfigFileUrl);
		this.zkconfig = new ZKConfiguration(zkAddress, fconfig.GetConfig());
	}

	public boolean shutdown(){		
		boolean success = this.StopS4Cluster();
		try {
			fconfig.close();
			zkconfig.CleanUp(fconfig.GetConfig());
			zk.close();
		} catch (Exception e) {
			return false;
		}
		return success;
	}
	/*
	 * param ClusterConfig:集群的配置信息
	 * param clean:是否清空原有配置
	 */
	public void S4ClusterReset(String ClusterConfig) throws Exception {
		this.nodecount = 0;
		Config config = this.fconfig.ResetConfig(ClusterConfig);
		zkconfig.ResetConfig(config);
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
	private Cluster AddTasks(String NodeConfig) throws Exception{
		Config nodesconfig = this.fconfig.addnode(NodeConfig);
		if(nodesconfig != null){
			Cluster nodes = nodesconfig.getClusters().get(0);
			zkconfig.addnode(nodes, nodesconfig.getVersion());
			return nodesconfig.getClusters().get(0);
		}
		return null;
	}
	
	private boolean DelTask(String clustername, ClusterType clustertype, int partition){
		try {
			fconfig.delnode(clustername, clustertype, partition);
			zkconfig.delnode(clustername, clustertype, partition);
		} catch (Exception e) {
			return false;
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
			if(fconfig.GetConfig() != null){
				for(Cluster cluster : fconfig.GetConfig().getClusters()){
					readProcessConfig(cluster);
				}
			} else {
				return null;
			}
			
			ClusterMessage.clear();
			for (Cluster cluster : fconfig.GetConfig().getClusters()) {
				Map<String, String> map = new HashMap<String, String>();
				for(ClusterNode node : cluster.getNodes()){
					String hostport = node.getMachineName() + ":" + node.getPort();
					String znode = MachineMap.get(hostport).znode;
					map.put(hostport, (znode == null || znode.equals(""))? "null" : znode);
				}

				this.ClusterMessage.put(cluster.getName(), map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return this.ClusterMessage;
	}

	//param: cluster 非空的参数
	private void readProcessConfig(Cluster cluster) throws KeeperException, InterruptedException {
		String pZnode = "/" + cluster.getName() + "/" + cluster.getType().toString() + "/process";
		Stat pExists = zk.exists(pZnode, false);
		if (pExists != null) {
			List<String> pZnodeChildren = zk.getChildren(pZnode, true);
			//reset the znode of macinemap
			Set<String> hostports = MachineMap.keySet();
			for(String hostport : hostports){
				MachineMap.get(hostport).znode = null;
			}
			
			for (String node : pZnodeChildren) {
				String nodeFullPath = pZnode + "/" + node;
				Stat pNodeStat = zk.exists(nodeFullPath, false);
				if (pNodeStat != null) {
					byte[] bytes = zk.getData(nodeFullPath, false, pNodeStat);
					Map<String, Object> map = (Map<String, Object>) JSONUtil.getMapFromJson(new String(bytes));
					String address = (String) map.get("address");
					String port = (String) map.get("port");
					MachineInfo minfo = MachineMap.get(address + ":" + port);
					if (minfo != null) {
						minfo.znode = nodeFullPath;
					}
				}
			}
		}
	}

	private Cluster getCluster(String clustername){
		if(fconfig.GetConfig() != null){
			for(Cluster cluster : fconfig.GetConfig().getClusters()){
				if(cluster.getName().equals(clustername)){
					return cluster;
				}
			}
		}
		return null;
	}
	/*
	 * 通过提供一个hostport值，可以达到恢复或启动一个S4Server节点
	 * param ClusterName：表明是哪个s4cluster
	 * param AdapterClusterName:表明该S4Server以后会把一些请求结果返回到那个adapter中（这个参数并不重要）
	 * param hostport：指明将要恢复和启动的节点地址
	 */
	public boolean StartS4Server(String clustername, String adapterclustername, String hostport) {
		try {
			String portStr = (hostport.trim().split(":"))[1];
			int port = Integer.valueOf(portStr);
			SSHWrapper sw = new SSHWrapper();
			MachineInfo minfo = MachineMap.get(hostport);
			System.out.println("MachineMap=" + MachineMap);
			System.out.println("addport=" + hostport);
			if (minfo != null && minfo.znode == null) {
				String command = this.makeStartS4Command(clustername, adapterclustername, minfo.s4image, port);
				System.out.println("command=" + command);
				System.out.println("minfo=" + minfo);
				sw.Authenticated(minfo.host, minfo.user, minfo.pass);
				sw.RunRemoteCommand(command);
				sw.close();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/*
	 * 恢复或启动一个clientadapter
	 * param ClusterName：表明是哪个s4clustername
	 * param ListenAppName:表明将要发送消息到那个s4cluster
	 * param hostport：表明将要恢复和启动哪一个节点
	 */
	public boolean StartClientAdapter(String clustername, String listenappname, String hostport) {
		try{
			String portStr = (hostport.trim().split(":"))[1];
			int port = Integer.valueOf(portStr);
			
			SSHWrapper sw = new SSHWrapper();
			MachineInfo minfo = MachineMap.get(hostport);
			if (minfo != null && minfo.znode == null) {
				String command = this.makeRunClientAdapterCommand(clustername, listenappname, minfo.s4image, port);
				sw.Authenticated(minfo.host, minfo.user, minfo.pass);
				sw.RunRemoteCommand(command);
				sw.close();
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/*
	 * 启动整一个clientadapter的集群
	 */
	public boolean StartClientAdapterCluster(String clustername, String listenappname) {
		if (isNullString(clustername) || isNullString(listenappname))
			return false;
		Cluster cluster = this.getCluster(clustername);
		if(cluster != null){
			for(ClusterNode node : cluster.getNodes()){
				String hostport = node.getMachineName() + ":" + node.getPort();
				boolean isSucc = this.StartClientAdapter(clustername, listenappname, hostport);
				if(!isSucc){
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private String makeRunClientAdapterCommand(String clustername,
			String listenappname, String s4image, int port) {
		String command = "/run-client-adapter.sh -s " + clustername 
				+ " -g " + listenappname
				+ " -d " + s4image + "/s4-core/conf/dynamic/client-stub-conf.xml "
				+ " -z " + this.zkAddress 
				+ " -p " + port + " dynamic &";
		return (s4image + command);
	}

	/*
	 * 启动整一个S4Server的集群
	 */
	public boolean StartS4ServerCluster(String clustername, String adapterclustername) {
		if (isNullString(clustername) || isNullString(adapterclustername))
			return false;
		Cluster cluster = this.getCluster(clustername);
		if(cluster != null){
			for(ClusterNode node : cluster.getNodes()){
				String hostport = node.getMachineName() + ":" + node.getPort();
				boolean isSucc = this.StartS4Server(clustername, adapterclustername, hostport);
				if(!isSucc){
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}
	
	/*
	 * 增加一个S4Server的节点
	 */
	public boolean AddS4Server(String ClusterName, String AdapterClusterName, String NodeConfig){
		try {
			Cluster nodecluster = this.AddTasks(NodeConfig);
			if(nodecluster != null){
				for(ClusterNode node : nodecluster.getNodes()){
					String hostport = node.getMachineName() + ":" + node.getPort();
					this.StartS4Server(ClusterName, AdapterClusterName, hostport);
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
			Cluster nodecluster = this.AddTasks(NodeConfig);
			if(nodecluster != null){
				for(ClusterNode node : nodecluster.getNodes()){
					String hostport = node.getMachineName() + ":" + node.getPort();
					this.StartClientAdapter(ClusterName, ListenAppName, hostport);
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
	public boolean RomoveS4Node(String clustername, String hostport){
		Cluster cluster = this.getCluster(clustername);
		
		//读取集群信息，******临时策略
		try {
			readProcessConfig(cluster);
		} catch (KeeperException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		MachineInfo minfo = MachineMap.get(hostport);
		boolean isnull = isNullString(minfo.znode);
		if(!isnull && cluster != null){		
			SSHWrapper sw = new SSHWrapper();
			try {
				System.out.println("minfo=" + minfo);
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
						DelTask(cluster.getName(), cluster.getType(), Integer.valueOf(taskid));
					}
				}
			} catch (Exception e) {
				return false;
			} finally {
				sw.close();
			}
		}
		return true;
	}

	private String makeStartS4Command(String clustername, String adapterclustername, String s4image,int port) {
		String command = "/start-s4.sh -r " + adapterclustername + " -g "
				+ clustername + " -z " + this.zkAddress + " -p " + port + " dynamic &";
		return (s4image + command);
	}

	private boolean isNullString(String target) {
		if (target == null || target.equals(""))
			return true;
		return false;
	}

	/*
	 * 关闭整一个集群
	 */
	public boolean StopS4Cluster() {
		if(fconfig.GetConfig() != null){
			for (Cluster cluster : fconfig.GetConfig().getClusters()) {
				boolean isSucc = StopCluster(cluster);
				if (!isSucc)
					return false;
			}
			return true;
		}
		return false;
	}

	/*
	 * 移除某一个S4cluster
	 */
	public boolean StopCluster(String clustername) {
		Cluster cluster = this.getCluster(clustername);
		if (cluster != null) {
			return StopCluster(cluster);
		}
		return false;
	}

	private boolean StopCluster(Cluster cluster) {
		String processNode = "/" + cluster.getName() + "/" + cluster.getType().toString() + "/process";
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
						MachineInfo minfo = MachineMap.get(address + ":" + port);
						sw.Authenticated(address, minfo.user, minfo.pass);
						sw.RunRemoteCommand("kill " + pid);
						sw.close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
