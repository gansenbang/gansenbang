package io.s4.manager.persist;

import java.util.List;
import java.util.Map;

import io.s4.manager.core.S4ClusterManager;
import io.s4.manager.thrift.Cluster;
import io.s4.manager.thrift.Machine;

public interface DataManager {
	//定义一个数据管理器
	//通过一个clustername对应一个机器集群，建议使用map作为数据结构
	public Map<String, Map<String, String>> GetClusterMessage(String ClusterName);
	
	public List<Cluster> GetAllClusters();
	
	public S4ClusterManager GetCluster(String ClusterName);
	
	public boolean AddCluster(String ClusterName, String ZkAddress, List<String> MachineList);
	
	public boolean RemoveCluster(String ClusterName);
	
	public List<Machine> GetAllMachine();
	
	public boolean ReceiveXMLConfig(String xmlconfig, String ClusterName);
}
