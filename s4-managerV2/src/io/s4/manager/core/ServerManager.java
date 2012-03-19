package io.s4.manager.core;

import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser.Cluster.ClusterType;
import io.s4.manager.util.JSONUtil;
import io.s4.manager.util.SSHWrapper;
import io.s4.manager.util.TaskSetupApp;
import io.s4.manager.zk.DefaultWatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

public class ServerManager extends DefaultWatcher{
	private Map<String, Map<String, Object>> S4Serverprocessmap;
	private Map<String, Map<String, Object>> S4Servertaskmap;
	private Map<String, Map<String, Object>> Adapterprocessmap;
	private Map<String, Map<String, Object>> Adaptertaskmap;
	//通过求processmap与taskmap的交集求出正在运行的机器
	private Map<String, String> RunningS4Server;
	private Map<String, String> RunningAdapter;
	
	private Boolean isS4ClusterChange = true;
	private Boolean isAdapterClusterChange = true;
	private String zkAddress;
	
	
	
	public ServerManager(String zkAddress) {
		super(zkAddress);
		this.zkAddress = zkAddress;
		S4Serverprocessmap = new HashMap<String, Map<String, Object>>();
		S4Servertaskmap = new HashMap<String, Map<String, Object>>();
		Adapterprocessmap = new HashMap<String, Map<String, Object>>();
		Adaptertaskmap = new HashMap<String, Map<String, Object>>();
		RunningS4Server = new HashMap<String, String>();
		RunningAdapter = new HashMap<String, String>();

	}
	
	public void TaskSetup(boolean clean, Cluster cluster, String version){
		TaskSetupApp.processCluster(clean, this.zkAddress, cluster, version);
	}
	
	public Map<String, String> GetRunningS4Server() throws Exception{
		if(RunningS4Server.isEmpty() || isS4ClusterChange){
			synchronized(isS4ClusterChange){
				String taskroot = "/" + ClusterType.S4.toString() + "/s4/task";
				String processroot = "/" + ClusterType.S4.toString() + "/s4/process";
				GetRunningMachine(RunningS4Server, 
																taskroot, 
																processroot,
																S4Servertaskmap,
																S4Serverprocessmap);
				isS4ClusterChange = false;
			}
		}
		return RunningS4Server;
	}
	
	public Map<String, String> GetRunningAdapter() throws Exception{
		if(RunningAdapter.isEmpty() || isAdapterClusterChange){
			synchronized(isAdapterClusterChange){
				String taskroot = "/" + "client-" + ClusterType.ADAPTER.toString() + "/s4/task";
				String processroot = "/" + "client-" + ClusterType.ADAPTER.toString() + "/s4/process";
				GetRunningMachine(RunningAdapter, 
																taskroot, 
																processroot,
																Adaptertaskmap,
																Adapterprocessmap);
				isAdapterClusterChange = false;
			}
		}
		return RunningAdapter;
	}
	
	private Map<String, String> GetRunningMachine(Map<String, String> runningmap, 
																															 String taskroot, 
																															 String processroot, 
																															 Map<String, Map<String, Object>> taskmap, 
																															 Map<String, Map<String, Object>> promap) throws Exception{
		GetProcessesMap(processroot, promap);
		GetNewTasksMap(taskroot, taskmap, false);
		
		
		Set<String> keyset = taskmap.keySet();
		for(String tasknodepath : keyset){
			Map<String, Object> map = taskmap.get(tasknodepath);
			String IP = (String)map.get("machine");
			String port = (String)map.get("port");
			String IPP = IP + ":" + port;
			if(IP == null || port == null) continue;
			
			runningmap.put(IPP, null);	
		}
			
		keyset = promap.keySet();
		for(String pronodepath : keyset){
			Map<String, Object> map = promap.get(pronodepath);
			String IP = (String)map.get("address");
			String port = (String)map.get("port");
			String IPP = IP + ":" + port;
			if(IP == null || port == null) continue;
			
			runningmap.put(IPP, pronodepath);
		}
		
		return runningmap;
	}
	
	
	public Map<String, Map<String, Object>> GetProcessesMap(String processroot, Map<String, Map<String, Object>> processmap) throws Exception{
		
		GetNodeMap(processroot, processmap);
		
		return processmap;
	}
	
	private Map<String, Map<String, Object>> GetNewTasksMap(String taskroot, 
																		Map<String, Map<String, Object>> taskmap,
																		boolean refind) throws Exception{
		if(refind || taskmap.isEmpty())
			GetNodeMap(taskroot, taskmap);
		
		return taskmap;
	}
	
	private void GetNodeMap(String root, Map<String, Map<String, Object>> nodemap) throws Exception{
		Stat pExists = zk.exists(root, false);
		if(pExists != null){
			List<String> tasks = zk.getChildren(root, false);
			for(String taskNode : tasks){
				String taskfullpath = root + "/" + taskNode;
				Stat tNodeStat = zk.exists(taskfullpath, true);
				if(tNodeStat != null){
					byte[] bytes = zk.getData(taskfullpath, false, tNodeStat);
					Map<String, Object> map = (Map<String, Object>) JSONUtil.getMapFromJson(new String(bytes));
					nodemap.put(taskfullpath, map);
				}
			}
		}
	}
	
	@Override
	public synchronized void process(WatchedEvent event) {
		super.process(event);
		if(event.getType() == EventType.NodeChildrenChanged){
			if(event.getPath().equals("/" + ClusterType.S4.toString() + "/s4/process"))
				synchronized(isS4ClusterChange){
					isS4ClusterChange = true;
				}
			
			if(event.getPath().equals("/" + ClusterType.ADAPTER.toString() + "/s4/process"))
				synchronized(isAdapterClusterChange){
					isAdapterClusterChange = true;
				}
		}
	}
	
	public boolean StartClientAdapter(String tasknodepath){
		String command = "run-client-adapter.sh -s client-adapter -g s4 -d $S4_IMAGE/s4-core/conf/dynamic/client-stub-conf.xml -z "
				+ this.zkAddress + " &";
		boolean isSucc = StartMachine(tasknodepath, command);
		return isSucc;
	}
	
	public boolean StartS4Server(String tasknodepath){
		String command = "/start-s4.sh -r client-adapter -z " + this.zkAddress + " dynamic &";
		boolean isSucc = StartMachine(tasknodepath, command);
		return isSucc;
	}
	
	public boolean StartMachine(String tasknodepath ,String command){
		synchronized(this){
			try {
				Stat pNodeStat = zk.exists(tasknodepath, false);
				if (pNodeStat != null) {
					byte[] bytes = zk.getData(tasknodepath, false, pNodeStat);
					Map<String, Object> map = (Map<String, Object>) JSONUtil.getMapFromJson(new String(bytes));
					String address = (String) map.get("machine");
					String user = (String) map.get("user");
					String pass = (String) map.get("pass");
					String s4_image = (String) map.get("s4image");
					SSHWrapper sw = new SSHWrapper();
					sw.Authenticated(address, user, pass);
					sw.RunRemoteCommand(s4_image + command);
					sw.close();
					
				}
			} catch (Exception e) {
				return false;
			} 
		}
		return true;
	}
	
	public boolean StartAllS4Server(){
		String taskroot = "/" + ClusterType.S4.toString() + "/s4/task";
		String processroot = "/" + ClusterType.S4.toString() + "/s4/process";
		String command = "run-client-adapter.sh -s client-adapter -g s4 -d $S4_IMAGE/s4-core/conf/dynamic/client-stub-conf.xml -z "
				+ this.zkAddress + " &";
		boolean isSucc = StartAllMachine(taskroot, processroot, command);
		return isSucc;
	}
	
	public boolean StartAllClientAdapter(){
		String taskroot = "/" + ClusterType.ADAPTER.toString() + "/s4/task";
		String processroot = "/" + ClusterType.ADAPTER.toString() + "/s4/process";
		String command =  "/start-s4.sh -r client-adapter -z " + this.zkAddress + " dynamic &";
		boolean isSucc = StartAllMachine(taskroot, processroot, command);
		return isSucc;
	}
	
	public boolean StartAllMachine(String taskroot, String processroot, String command){
		synchronized(this){
			try {
				Stat tExists = zk.exists(taskroot, false);
				if (tExists == null)
					return false;

				Stat pExists = zk.exists(processroot, false);
				if (pExists == null)
					return false;
				
				List<String> tasks = zk.getChildren(taskroot, false);
				List<String> processes = zk.getChildren(processroot, false);
				if (processes.size() < tasks.size()) {
					ArrayList<String> tasksAvailable = new ArrayList<String>();
					for (int i = 0; i < tasks.size(); i++) {
						tasksAvailable.add("" + i);
					}
					if (processes != null) {
						for (String s : processes) {
							String taskId = s.split("-")[1];
							tasksAvailable.remove(taskId);
						}
					}
					SSHWrapper sw = new SSHWrapper();
					for(String taskid : tasksAvailable){
						String tNode = taskroot + "/" + "task-" + taskid;
						Stat tNodeStat = zk.exists(tNode, false);
						if(tNodeStat != null){
							byte[] bytes = zk.getData(tNode, false, tNodeStat);
							Map<String, Object> map = (Map<String, Object>) JSONUtil.getMapFromJson(new String(bytes));
							String address = (String) map.get("address");
							String user = (String) map.get("user");
							String pass = (String) map.get("pass");
							String s4_image = (String) map.get("s4imgae");
							sw.Authenticated(address, user, pass);
							sw.RunRemoteCommand(s4_image + command);
						}
					}
					sw.close();
				}
			} catch(Exception e){
				return false;
			}
		}
		return true;
	}
	
	public boolean RemoveS4Server(String pronodepath){
		boolean isSucc = RemoveMachine(pronodepath);
		return isSucc;
	}
	
	public boolean RemoveClientAdapter(String pronodepath){
		boolean isSucc = RemoveClientAdapter(pronodepath);
		return isSucc;
	}
	
	public boolean RemoveMachine(String pronodepath){
		SSHWrapper sw = new SSHWrapper();
		synchronized(this){
			Stat pNodeStat;
			try {
				pNodeStat = zk.exists(pronodepath, false);
				if (pNodeStat != null){
					
					byte[] bytes = zk.getData(pronodepath, false, pNodeStat);
					Map<String, Object> map = (Map<String, Object>) JSONUtil.getMapFromJson(new String(bytes));
					String address = (String) map.get("address");
					String user = (String) map.get("user");
					String pass = (String) map.get("pass");
					String pid = (String) map.get("PID");
					sw.Authenticated(address, user, pass);
					sw.RunRemoteCommand("kill " + pid);
				}
				else 
					return false;
			} catch (Exception e) {
				return false;
			} finally {
				sw.close();
			}
		}
		return true;
	}
	
	
}
