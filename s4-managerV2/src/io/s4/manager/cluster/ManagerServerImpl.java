package io.s4.manager.cluster;

import io.s4.manager.core.ServerManager;
import io.s4.manager.persist.DataManager;
import io.s4.manager.util.Constant;

import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

public class ManagerServerImpl implements ManagerServer.Iface{
	
	private final DataManager dm;
	
	public ManagerServerImpl(DataManager dm){
		this.dm = dm;
	}

	@Override
	public boolean CreateCluster(String zkAddress, String clustername,
			List<String> machinelist) throws TException {
		return dm.AddCluster(clustername, zkAddress, machinelist);
	}

	@Override
	public boolean RemoveCluster(String clustername) throws TException {
		return dm.RemoveCluster(clustername);
	}

	@Override
	public List<Machine> GetAllMachinesList() throws TException {
		return dm.GetAllMachine();
	}

	@Override
	public List<Cluster> GetAllClustersList() throws TException {
		return dm.GetAllClusters();
	}

	@Override
	public boolean CommitS4ClusterXMLConfig(String xmlfile,
			String clustername, boolean clean, List<String> s4clustersname) throws TException {
		if(dm.GetCluster(clustername) == null) return false;
		
		boolean isSucc = dm.ReceiveXMLConfig(xmlfile, clustername);
		if(isSucc){
			ServerManager sm = dm.GetCluster(clustername);
			String ClusterConfig = Constant.CONF_PATH + "/" + clustername + "/" + Constant.CLUSTER_FILE; 
			try {
				if(sm != null)
					sm.TaskSetup(ClusterConfig, clean, s4clustersname);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		} else 
			return false;
		
		return true;
	}

	@Override
	public Map<String, Map<String, String>> GetS4ClusterMessage(
			String clustername) throws TException {
		return dm.GetClusterMessage(clustername);
	}

	@Override
	public boolean StartS4ServerCluster(String clustername,
			String s4clustername, String adapterclustername) throws TException {
		ServerManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.StartS4ServerCluster(s4clustername, adapterclustername);
	}

	@Override
	public boolean StartClientAdapterCluster(String clustername,
			String s4clustername, String listenappname) throws TException {
		ServerManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.StartClientAdapterCluster(s4clustername, listenappname);
	}

	@Override
	public boolean RemoveS4Cluster(String clustername, String s4clustername)
			throws TException {
		ServerManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.RemoveS4Cluster(s4clustername);
	}

	@Override
	public boolean RemoveAllS4Cluster(String clustername) throws TException {
		ServerManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.RemoveAllS4Cluster();
	}

	@Override
	public boolean RecoveryS4Server(String clustername, String s4clustername,
			String s4adaptername, String hostport) throws TException {
		ServerManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.RecoveryS4Server(s4clustername, s4adaptername, hostport);
	}

	@Override
	public boolean RecoveryClientServer(String clustername,
			String s4clustername, String listenappname, String hostport)
			throws TException {
		ServerManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.RecoveryClientAdapter(s4clustername, listenappname, hostport);
	}
	
	public static void main(String[] args){
		
	}
	
}
