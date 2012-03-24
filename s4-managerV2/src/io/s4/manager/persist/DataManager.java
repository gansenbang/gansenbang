package io.s4.manager.persist;

import java.util.List;
import java.util.Map;

import io.s4.manager.cluster.Cluster;
import io.s4.manager.cluster.Machine;
import io.s4.manager.core.ServerManager;

public interface DataManager {
	public Map<String, Map<String, String>> GetClusterMessage(String ClusterName);
	
	public List<Cluster> GetAllClusters();
	
	public ServerManager GetCluster(String ClusterName);
	
	public boolean AddCluster(String ClusterName, String ZkAddress, List<String> MachineList);
	
	public boolean RemoveCluster(String ClusterName);
	
	public List<Machine> GetAllMachine();
	
	public boolean ReceiveXMLConfig(String xmlconfig, String ClusterName);
}
