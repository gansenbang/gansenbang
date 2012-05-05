package io.s4.manager.core;

import java.util.List;
import java.util.Map;

import io.s4.manager.util.ConfigParser.Cluster.ClusterType;
import io.s4.manager.util.ConfigUtils;
import io.s4.manager.util.Constants;
import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser.Config;
import io.s4.manager.zk.ZkTaskSetup;

public class ZKConfiguration {
	private String zkAddress;
	private boolean DEBUG = false;
	
	public ZKConfiguration(String zkAddress, Config config){
		this.zkAddress = zkAddress;
		if(config != null){
			for(Cluster cluster : config.getClusters()){
				try {
					processCluster(true, true, cluster, config.getVersion());
				} catch (Exception e) {
					//not handle
				}
			}
		}
	}
	
	public void ResetConfig(Config config) throws Exception{
		if(config != null){
			for(Cluster cluster : config.getClusters()){
				processCluster(true, true, cluster, config.getVersion());
			}
		}
	}
	
	public void SetDEBUG(boolean debug){
		this.DEBUG = debug;
	}
	
	public void CleanUp(Config config) throws Exception{
		if(config != null){
			for(Cluster cluster : config.getClusters()){
				processCluster(true, false, cluster, config.getVersion());
			}
		}
	}
	
	private void processCluster(boolean clean, boolean setup,
			Cluster cluster, String version) throws Exception {
 		ZkTaskSetup zkSetup = new ZkTaskSetup(zkAddress, cluster.getName(), cluster.getType());
		if (clean) {
			zkSetup.cleanUp();
		}
		
		if(setup){
			List<Map<String, String>> clusterInfo = ConfigUtils.readConfig(cluster,
																	   cluster.getName(), 
																	   cluster.getType(), 
																	   false);
			zkSetup.setUpTasks(version, clusterInfo.toArray());
		}
		if(DEBUG){
			zkSetup.showTasks();
		}
		zkSetup.close();
	}
	
	public void addcluster(Cluster cluster, String version) throws Exception{
		processCluster(false, true, cluster, version);
	}
	
	public void addnode(Cluster nodes, String version) throws Exception{
		ZkTaskSetup zkSetup = new ZkTaskSetup(zkAddress, nodes.getName(), nodes.getType());
		List<Map<String, String>> clusterInfo = ConfigUtils.readConfig(nodes,
																	   nodes.getName(), 
																	   nodes.getType(), 
																	   false);
		zkSetup.AddTasks(clusterInfo.toArray());
	}
	
	public void delcluster(String clustername, ClusterType clustertype) throws Exception{
		Cluster cluster = new Cluster();
		cluster.setName(clustername);
		cluster.setType(clustertype);
		processCluster(true, false, cluster, "-1");
	}
	
	public void delnode(String clustername, ClusterType clustertype, int partition) throws Exception{
		Cluster cluster = new Cluster();
		cluster.setName(clustername);
		cluster.setType(clustertype);
		ZkTaskSetup zkSetup = new ZkTaskSetup(zkAddress, cluster.getName(), cluster.getType());
		zkSetup.DeleteTask(partition);
		if(DEBUG){
			zkSetup.showTasks();
		}
		zkSetup.close();
	}
	
	public static void main(String[] args){
		String ConFileUrl = Constants.CONF_PATH + "/" + "denghankun@localhost:2181" + "/" + Constants.CLUSTER_FILE;
		FileConfiguration fc = new FileConfiguration(ConFileUrl);
		ZKConfiguration zkc = new ZKConfiguration("localhost:2181", fc.GetConfig());
		zkc.SetDEBUG(true);
		fc.showconfig();
		try {
			//add node
			String NodeInfo = "<config version=\"-1\"><cluster name=\"s4\" type=\"s4\" mode=\"unicast\">" +
				   " <node><partition>1</partition><machine>192.168.1.15</machine><port>5078</port><taskId>s4node-1</taskId>" +
				   "</node></cluster></config>";
			Config nodesconfig = fc.addnode(NodeInfo);
			fc.showconfig();
			zkc.addnode(nodesconfig.getClusters().get(0), nodesconfig.getVersion());
			//delete node
			fc.delnode("s4", ClusterType.S4, 1);
			fc.showconfig();
			zkc.delnode("s4", ClusterType.S4, 1);
			
			//add cluster
			String ClusterInfo = "<config version=\"-1\"><cluster name=\"client-adapter\" type=\"s4\" mode=\"unicast\">" +
					   " <node><partition>0</partition><machine>192.168.1.15</machine><port>5079</port><taskId>adapter-0</taskId>" +
					   "</node></cluster></config>";
			Config clusterconfig = fc.addcluster(ClusterInfo);
			fc.showconfig();
			zkc.addcluster(clusterconfig.getClusters().get(0), clusterconfig.getVersion());
			
			//delete cluster
			fc.delcluster("client-adapter", ClusterType.S4);
			fc.showconfig();
			zkc.delcluster("client-adapter", ClusterType.S4);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//zkc.CleanUp(fc.GetConfig());
				//fc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
}
