package io.s4.manager.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser;
import io.s4.manager.util.ConfigParser.Cluster.ClusterType;
import io.s4.manager.util.ConfigParser.ClusterNode;
import io.s4.manager.util.ConfigParser.Config;
import io.s4.manager.util.TaskSetupApp;

public class Configuration {
	private Config config;
	private String ConfigFileUrl;
	private Integer UpdateMutex = new Integer(-1);
	private String zkAddress;
	
	public Configuration(String zkAddress, String ConfigFileUrl){
		ConfigParser parser = new ConfigParser();
		Config config;
		this.ConfigFileUrl = ConfigFileUrl;
		this.zkAddress = zkAddress;

		try {
			config = parser.parse(ConfigFileUrl, false , ConfigParser.StringType.CONFIGURL);
			this.config = config;
			//readconfig();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		//start the update configuration thread
		new Thread(UpdateConfig).start();
	}
	
	public Config GetConfig(){
		return this.config;
	}
	
	public Config ResetConfig(String config) throws Exception{
		ConfigParser parser = new ConfigParser();
		Config newconfig = parser.parse(config, false, ConfigParser.StringType.CONFIGCONTENT);
		synchronized(UpdateMutex){
			this.config = newconfig;
			//readconfig();
			UpdateMutex.notifyAll();
		}
		return this.config;
	}
	
	public Config addnodeinfo(String NodeInfo) throws Exception{
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
					Set<Integer> targetnodeset = new HashSet<Integer>();
					for(ClusterNode node : targetcluster.getNodes()){
						targetnodeset.add(node.getPartition());
					}
					//match the node
					for(ClusterNode node : cluster.getNodes()){
						if(targetnodeset.contains(new Integer(node.getPartition()))){
							return null;
						}
					}
					//not exist the same node
					cluster.getNodes().addAll(targetcluster.getNodes());
					UpdateMutex.notifyAll();
					break;
				}
			}
		}
		
		return nodeconfig;
	}
	
	public Config delnodeinfo(String clustername, ClusterType clustertype, int partitionid){
		synchronized(UpdateMutex){
			if(config != null){
				for(Cluster cluster : config.getClusters()){
					if(cluster.getName().equals(clustername) && 
							cluster.getType().equals(clustertype)){
						for(ClusterNode node : cluster.getNodes()){
							if(node.getPartition() == partitionid){
								cluster.getNodes().remove(node);
								UpdateMutex.notifyAll();
							}
						}
						break;
					}
				}
			}
		}
		return config;
	}
	
	public class CompareNodeByPartitionid implements Comparator<ClusterNode>{

		public int compare(ClusterNode o1, ClusterNode o2) {
			int partitionid1 = o1.getPartition();
			int partitionid2 = o2.getPartition();
			return (partitionid1 - partitionid2);
		}
		
	}
	
	private final Runnable UpdateConfig = new Runnable(){

		@Override
		public void run() {
			try {
				while(true){
					synchronized(UpdateMutex){
						UpdateMutex.wait();
						for(Cluster cluster : config.getClusters()){
							Collections.sort(cluster.getNodes(), new CompareNodeByPartitionid());
						}
						writeconfig(config);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
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
		
	};
	
}
