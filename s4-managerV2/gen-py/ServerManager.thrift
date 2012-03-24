namespace java io.s4.manager.cluster
namespace py io.s4.manager.cluster

struct Machine{
	1:string hostport,
	2:string status,
}

struct Cluster{
	1:string clustername,
	2:string zkAddress,
	3:i32 	 number,
}

service ManagerServer{
	bool CreateCluster(1:string zkAddress,2:string clustername, 3:list<string> machinelist),
	bool RemoveCluster(1:string clustername),
	list<Machine> GetAllMachinesList(),
	list<Cluster> GetAllClustersList(),
	/*xmlfile 配置文件，String*/
	bool CommitS4ClusterXMLConfig(1:string xmlfile,2:string clustername,3:bool clean,4:list<string> s4clustersname),

	map<string,map<string,string>> GetS4ClusterMessage(1:string clustername),
	bool StartS4ServerCluster(1:string clustername,2:string s4clustername,3:string adapterclustername),
	bool StartClientAdapterCluster(1:string clustername,2:string s4clustername,3:string listenappname),
	bool RemoveS4Cluster(1:string clustername,2:string s4clustername),
	bool RemoveAllS4Cluster(1:string clustername),
	bool RecoveryS4Server(1:string clustername,2:string s4clustername,3:string s4adaptername,4:string hostport),
	bool RecoveryClientServer(1:string clustername,2:string s4clustername,3:string listenappname,4:string hostport),
	
}