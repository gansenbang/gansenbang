package io.s4.manager.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser.Cluster.ClusterType;
import io.s4.manager.util.ConfigParser.ClusterNode;

public class ConfigUtils {
	public static List<Map<String, String>> readConfig(String configFilename,
			String clusterName, ClusterType clusterType, boolean isStatic) {
		ConfigParser parser = new ConfigParser();
		ConfigParser.Config config = parser.parse(configFilename);

		// find the requested cluster
		Cluster cluster = null;
		for (Cluster checkCluster : config.getClusters()) {
			if (checkCluster.getName().equals(clusterName)
					&& checkCluster.getType().equals(clusterType)) {
				cluster = checkCluster;
				break;
			}
		}
		if (cluster == null) {
			throw new RuntimeException("Cluster " + clusterName + " of type "
					+ clusterType + " not configured");
		}
		return readConfig(cluster, clusterName, clusterType, isStatic);
	}

	public static List<Map<String, String>> readConfig(Cluster cluster,
			String clusterName, ClusterType clusterType, boolean isStatic) {

		List<Map<String, String>> processSet = new ArrayList<Map<String, String>>();
		for (ClusterNode node : cluster.getNodes()) {
			Map<String, String> nodeInfo = new HashMap<String, String>();
			if (node.getPartition() != -1) {
				nodeInfo.put("partition", String.valueOf(node.getPartition()));
			}
			if (node.getPort() != -1) {
				nodeInfo.put("port", String.valueOf(node.getPort()));
			}
			nodeInfo.put("cluster.type", String.valueOf(clusterType));
			nodeInfo.put("cluster.name", clusterName);
			if (isStatic) {
				nodeInfo.put("address", node.getMachineName());
				nodeInfo.put("process.host", node.getMachineName());
			}
			nodeInfo.put("mode", cluster.getMode());
			nodeInfo.put("ID", node.getTaskId());
			nodeInfo.put("user", node.getUser());
			nodeInfo.put("pass", node.getPass());
			nodeInfo.put("s4image", node.getS4image());
			if(node.getMachineName().equals("localhost")){
				try {
					nodeInfo.put("machine", GetHostAddress());
				} catch (Exception e) {
					throw new RuntimeException();
				} 
			} else {
				nodeInfo.put("machine", node.getMachineName());
			}
			processSet.add(nodeInfo);
		}
		return processSet;
	}
	
	public static String GetHostAddress() throws UnknownHostException, SocketException {
		Enumeration allNetInterfaces;
		InetAddress ip = null;
		allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
			String niName = netInterface.getName();
			if(!niName.equals("eth0") && !niName.equals("wlan0"))
				continue;
			else {
				Enumeration addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					ip = (InetAddress) addresses.nextElement();
					if (ip != null && ip instanceof Inet4Address) {
						break;
					}
				}
			}
		}
		return (ip != null ? ip.getHostAddress() : InetAddress.getLocalHost().getHostAddress());
	}
}
