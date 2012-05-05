package io.s4.manager.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser;
import io.s4.manager.util.ConfigParser.Cluster.ClusterType;
import io.s4.manager.util.ConfigParser.ClusterNode;
import io.s4.manager.util.ConfigParser.Config;
import io.s4.manager.util.Constants;

public class FileConfiguration {
	private Config config;
	private String ConfigFileUrl;
	private Integer UpdateMutex = new Integer(-1);
	private Thread updateThread = null;
	
	public FileConfiguration(String ConfigFileUrl){
		ConfigParser parser = new ConfigParser();
		Config config;
		this.ConfigFileUrl = ConfigFileUrl;

		try {
			config = parser.parse(ConfigFileUrl, false , ConfigParser.StringType.CONFIGURL);
			parser.verifyConfig(config);
			this.config = config;
		} catch (Exception e) {
			//not handle
		}
		
		//start the update configuration thread
		(updateThread = new Thread(UpdateConfig)).start();
		updateThread.setPriority(Thread.MAX_PRIORITY);
	}
	
	public void close(){
		synchronized(UpdateMutex){
			writeconfig(config);
			updateThread.interrupt();
		}
	}
	
	public void showconfig(){
		System.out.println(config);
	}
	
	public Config GetConfig(){
		return this.config;
	}
	
	public Config ResetConfig(String config) throws Exception{
		ConfigParser parser = new ConfigParser();
		Config newconfig = parser.parse(config, false, ConfigParser.StringType.CONFIGCONTENT);
		synchronized(UpdateMutex){
			this.config = newconfig;
			UpdateMutex.notifyAll();
		}
		return this.config;
	}
	
	//如果成功解释返回Config对象
	//否则返回null
	public Config addcluster(String ClusterInfo) throws Exception{
		ConfigParser parser = new ConfigParser();
		Config clusterconfig = parser.parse(ClusterInfo, false, ConfigParser.StringType.CONFIGCONTENT);
		Cluster targetcluster = null;
		if(clusterconfig.getClusters().size() == 1){
			targetcluster = clusterconfig.getClusters().get(0);
		} else {
			return null;
		}
		
		synchronized(UpdateMutex){
			if(config == null){
				config = clusterconfig;
				UpdateMutex.notifyAll();
				return clusterconfig;
			}
			for(Cluster cluster : config.getClusters()){
				if(cluster.getName().equals(targetcluster.getName()) &&
						cluster.getType().equals(targetcluster.getType())){
					//the new cluster is existed
					return null;
				}
			}
			config.addCluster(targetcluster);
			parser.verifyConfig(config);
			UpdateMutex.notifyAll();
		}
		return clusterconfig;
	}
	
	public Config addnode(String NodeInfo) throws Exception{
		ConfigParser parser = new ConfigParser();
		Config nodeconfig = parser.parse(NodeInfo, false, ConfigParser.StringType.CONFIGCONTENT);
		//check the target cluster
		Cluster targetcluster = null;
		if(nodeconfig.getClusters().size() == 1){
			targetcluster = nodeconfig.getClusters().get(0);
		} else {
			return null;
		}
		synchronized(UpdateMutex){
			if(config == null){
				config = nodeconfig;
				UpdateMutex.notifyAll();
				return nodeconfig;
			}
			for(Cluster cluster : config.getClusters()){
				if(cluster.getName().equals(targetcluster.getName()) &&
						cluster.getType().equals(targetcluster.getType())){
					for(ClusterNode node : targetcluster.getNodes()){
						cluster.addNode(node);
					}
					parser.verifyConfig(config);
					UpdateMutex.notifyAll();
					break;
				}
			}
		}
		
		return nodeconfig;
	}
	
	public void delcluster(String clustername, ClusterType clustertype){
		synchronized(UpdateMutex){
			if(config != null){
				for(Cluster cluster : config.getClusters()){
					if(cluster.getName().equals(clustername) && 
							cluster.getType().equals(clustertype)){
						config.deleteCluster(cluster);
						UpdateMutex.notifyAll();
						break;
					}
				}
			}
		}
	}
	
	public void delnode(String clustername, ClusterType clustertype, int partitionid){
		synchronized(UpdateMutex){
			if(config != null){
				for(Cluster cluster : config.getClusters()){
					if(cluster.getName().equals(clustername) && 
							cluster.getType().equals(clustertype)){
						for(ClusterNode node : cluster.getNodes()){
							if(node.getPartition() == partitionid){
								cluster.deleteNode(node);
								UpdateMutex.notifyAll();
								break;
							}
						}
						break;
					}
				}
			}
		}
	}
	
	private final Runnable UpdateConfig = new Runnable(){

		@Override
		public void run() {
			try {
				while(!Thread.currentThread().isInterrupted()){
					synchronized(UpdateMutex){
						UpdateMutex.wait();
						for(Cluster cluster : config.getClusters()){
							cluster.sortByPartitionid();
						}
						System.out.println("I'm writing now");
						writeconfig(config);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		
	};
	
	private void writeconfig(Config config){
		StringBuffer sb = new StringBuffer();
		sb.append(writeheader(config.getVersion()));
		for(Cluster cluster : config.getClusters()){
			sb.append(writeclusterheader(cluster));
			for(ClusterNode node : cluster.getNodes()){
				sb.append(writenode(node));
			}
			sb.append(writeclusterfooter());
		}
		sb.append(writefooter());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(ConfigFileUrl);
			fos.write(sb.toString().getBytes(Charset.defaultCharset()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String writeheader(String version){
		return String.format("<config version= \"%s\">\n", version);
	}
	
	private String writeclusterheader(Cluster cluster){
		return String.format("<cluster name=\"%s\" type=\"%s\" mode=\"%s\">\n", 
							cluster.getName(),
							cluster.getType().toString(),
							cluster.getMode());
	}
	
	private String writenode(ClusterNode node){
		StringBuffer sb = new StringBuffer();
		sb.append("<node>\n")
		  .append("<partition>").append(node.getPartition()).append("</partition>\n")
		  .append("<machine>").append(node.getMachineName()).append("</machine>\n")
		  .append("<port>").append(node.getPort()).append("</port>\n")
		  .append("<taskId>").append(node.getTaskId()).append("</taskId>\n")
		  .append("</node>\n");
		return sb.toString();
	}
	
	private String writeclusterfooter(){
		return "</cluster>";
	}
	
	private String writefooter(){
		return "</config>";
	}
	public static void main(String[] args){
		String ConFileUrl = Constants.CONF_PATH + "/" + "denghankun@localhost:2181" + "/" + Constants.CLUSTER_FILE;
		FileConfiguration fc = new FileConfiguration(ConFileUrl);
		fc.showconfig();
		
		try {
			Thread.sleep(1000);
			
			//addnode
			String NodeInfo = "<config version=\"-1\"><cluster name=\"s4\" type=\"s4\" mode=\"unicast\">" +
					   " <node><partition>1</partition><machine>192.168.1.15</machine><port>5078</port><taskId>s4node-1</taskId>" +
					   "</node></cluster></config>";
			fc.addnode(NodeInfo);
			fc.showconfig();
			
			//deletenode
			fc.delnode("s4", ClusterType.S4, 1);
			fc.showconfig();
			
			//addcluster
			String ClusterInfo = "<config version=\"-1\"><cluster name=\"client-adapter\" type=\"s4\" mode=\"unicast\">" +
					   " <node><partition>0</partition><machine>192.168.1.15</machine><port>5079</port><taskId>adapter-0</taskId>" +
					   "</node></cluster></config>";
			fc.addcluster(ClusterInfo);
			fc.showconfig();
			
			//deletecluster
			fc.delcluster("client-adapter", ClusterType.S4);
			fc.showconfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
