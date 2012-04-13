package io.s4.manager.persist;

import java.util.List;
import java.util.Map;

import io.s4.manager.core.ServerManager;
import io.s4.manager.thrift.Cluster;
import io.s4.manager.thrift.Machine;

public interface DataManager {
	public Map<String, Map<String, String>> GetClusterMessage(String ClusterName);
	
	public List<Cluster> GetAllClusters();
	
	public ServerManager GetCluster(String ClusterName);
	
	public boolean AddCluster(String ClusterName, String ZkAddress, List<String> MachineList);
	
	public boolean RemoveCluster(String ClusterName);
	
	public List<Machine> GetAllMachine();
	
	public boolean ReceiveXMLConfig(String xmlconfig, String ClusterName);
}
