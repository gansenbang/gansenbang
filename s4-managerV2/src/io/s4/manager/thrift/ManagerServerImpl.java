package io.s4.manager.thrift;

import io.s4.manager.core.S4ClusterManager;
import io.s4.manager.persist.DataManager;
import io.s4.manager.util.Constants;

import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

public class ManagerServerImpl implements S4Manager.Iface{
	
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
	public boolean CommitS4ClusterXMLConfig(String xmlfile,	String clustername) throws TException {
		return dm.ReceiveXMLConfig(xmlfile, clustername);
	}

	@Override
	public Map<String, Map<String, String>> GetS4ClusterMessage(
			String clustername) throws TException {
		return dm.GetClusterMessage(clustername);
	}

	@Override
	public boolean StartS4ServerCluster(String clustername,
			String s4clustername, String adapterclustername) throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.StartS4ServerCluster(s4clustername, adapterclustername);
	}

	@Override
	public boolean StartClientAdapterCluster(String clustername,
			String s4clustername, String listenappname) throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.StartClientAdapterCluster(s4clustername, listenappname);
	}

	@Override
	public boolean RemoveS4Cluster(String clustername, String s4clustername)
			throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.StopCluster(s4clustername);
	}

	@Override
	public boolean RemoveAllS4Cluster(String clustername) throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.StopS4Cluster();
	}

	@Override
	public boolean RecoveryS4Server(String clustername, String s4clustername,
			String s4adaptername, String hostport) throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.StartS4Server(s4clustername, s4adaptername, hostport);
	}

	@Override
	public boolean RecoveryClientServer(String clustername,
			String s4clustername, String listenappname, String hostport)
			throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm == null) return false;
		return sm.StartClientAdapter(s4clustername, listenappname, hostport);
	}
	
	@Override
	public boolean AddS4Server(String nodeconfig, String clustername,
			String s4clustername, String adapterclustername) throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm != null){
			return sm.AddS4Server(s4clustername, adapterclustername, nodeconfig);
		}
		return false;
	}

	@Override
	public boolean AddClientAdapter(String nodeconfig, String clustername,
			String s4clustername, String listenappname) throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm != null){
			return sm.AddClientAdapter(s4clustername, listenappname, nodeconfig);
			
		}
		return false;
	}

	@Override
	public boolean RemoveS4Node(String clustername, String s4clustername,
			String hostport) throws TException {
		S4ClusterManager sm = dm.GetCluster(clustername);
		if(sm != null){
			return sm.RomoveS4Node(s4clustername, hostport);
		}
		return false;
	}
	
}
