package io.s4.manager.persist;

import io.s4.manager.core.ServerManager;
import io.s4.manager.thrift.Cluster;
import io.s4.manager.thrift.Machine;
import io.s4.manager.util.ConfigParser;
import io.s4.manager.util.Constant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FileDataManager implements DataManager{

	private final Map<String, ServerManager> ClusterMap = new ConcurrentHashMap<String, ServerManager>();
	
	public final Map<String, MachineInfo> MachineMap = new ConcurrentHashMap<String, MachineInfo>();
	
	private final String XMLFile;
	
	public FileDataManager(String XMLFile){
		if(XMLFile == null || XMLFile.equals("")){
			this.XMLFile = Constant.CONF_PATH + "/" + Constant.MACHINE_FILE;
		} else {
			this.XMLFile = XMLFile;
		}
		
		try {
			this.MachineMap.putAll(parseDocument(this.XMLFile));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		VerifyMachineMap();
		Setup();
	}
	
	private void VerifyMachineMap(){
		
	}
	
	private Map<String, MachineInfo> parseDocument(String configFilename) throws Exception{
		Map<String, MachineInfo> machinfomap = new HashMap<String, MachineInfo>();
		Document document = ConfigParser.createDocument(configFilename, true, ConfigParser.StringType.CONFIGURL);
		NodeList topLevelNodeList = document.getChildNodes();
		for (int i = 0; i < topLevelNodeList.getLength(); ++i) {
			Node node = topLevelNodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("machines")) {
				NodeList machinelist = node.getChildNodes();
				for(int j = 0; j < machinelist.getLength(); ++j){
					Node machine = machinelist.item(j);
					if(machine.getNodeType() == Node.ELEMENT_NODE && machine.getNodeName().equals("machine")){
						MachineInfo minfo = processMachineElement(machine);
						// add minfo to the machinemap
						machinfomap.put(minfo.host + ":" + minfo.port, minfo);
					}
				}
			}
		}
		return machinfomap;
	}
	
	private MachineInfo processMachineElement(Node machine){
		String host = null;
		int port = 0;
		String pass = null;
		String s4image = null;
		String user = null;
		NodeList nodeList = machine.getChildNodes();
		for(int i = 0; i < nodeList.getLength(); ++i){
			Node node = nodeList.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			if(node.getNodeName().equals("host")){
				host = ConfigParser.getElementContentText(node);
			} else if(node.getNodeName().equals("port")) {
				try {
					port = Integer.parseInt(ConfigParser.getElementContentText(node));

				} catch (NumberFormatException nfe) {
					throw new RuntimeException("Bad port specified " + ConfigParser.getElementContentText(node));
				}
			} else if(node.getNodeName().equals("pass")){
				pass = ConfigParser.getElementContentText(node);
			} else if(node.getNodeName().equals("s4image")){
				s4image = ConfigParser.getElementContentText(node);
			} else if(node.getNodeName().equals("user")){
				user = ConfigParser.getElementContentText(node);
			}
		}
		return new MachineInfo(host, user, port, pass, s4image);
	}
	
	private void Setup(){
		File rootconf = new File(Constant.CONF_PATH);
		if(rootconf.isDirectory()){
			for(File smdir : rootconf.listFiles()){
				if(smdir.isDirectory()){
					String clustername = smdir.getName();
					String[] zkwithhp = clustername.split("@");
					if(zkwithhp.length == 2){
						String ClusterName = zkwithhp[0];
						String ZkAddress = zkwithhp[1];
						List<String> MachineList = SetupServerManager(smdir.getPath());
						if(MachineList != null && MachineList.size() != 0){
							this.AddCluster(ClusterName, ZkAddress, MachineList);
						}
					}
				}
			}
		}
	}
	
	private List<String> SetupServerManager(String SMConfigUrl){
		File smconfigdir = new File(SMConfigUrl);
		List<String> MachineList = null;
		if(smconfigdir.isDirectory()){
			String SubMachListUrl = SMConfigUrl + "/" + Constant.SUB_MACHINE_FILE;
			File submachlist = new File(SubMachListUrl);
			if(submachlist.exists() && submachlist.isFile()){
				try {
					MachineList = new ConfigServerManager(SubMachListUrl).readSubMachList();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return MachineList;
	}
	
	
	@Override
	public List<Cluster> GetAllClusters() {
		Set<String> clusternameset = ClusterMap.keySet();
		List<Cluster> ClusterList = new ArrayList<Cluster>();
		for(String clustername : clusternameset){
			Cluster cluster  = new Cluster();
			cluster.setClustername(clustername);
			ServerManager sm = ClusterMap.get(clustername);
			cluster.setZkAddress(sm.getZkAddress());
			int number = sm.GetNodeCount();
			cluster.setNumber(number);
			ClusterList.add(cluster);
		}
		return ClusterList;
	}

	@Override
	public boolean ReceiveXMLConfig(String xmlconfig, String ClusterName) {
		String dirpath = Constant.CONF_PATH + "/" + ClusterName;
		File filedir = new File(dirpath);
		if(!filedir.exists()){
			filedir.mkdir();
		}
		return true;
	}

	@Override
	public boolean AddCluster(String ClusterName, String ZkAddress, List<String> MachineList) {
		String target = ClusterName + "@" + ZkAddress;
		Set<String> cnamewithzkaddr = this.ClusterMap.keySet();
		for(String s : cnamewithzkaddr){
			String[] ca = s.split("@");
			if(ca[0].equals(ClusterName)) return false;
			if(ca[1].equals(ZkAddress)) return false;
		}
		ServerManager sm;
		try {
			String ConfigFileUrl = Constant.CONF_PATH + "/" + target + "/" + Constant.CLUSTER_FILE;
			new ConfigServerManager(Constant.CONF_PATH + "/" + target + "/" + Constant.SUB_MACHINE_FILE)
				.writeSubMachList(MachineList);
			sm = new ServerManager(ZkAddress, ConfigFileUrl);
		} catch (Exception e) {
			return false;
		}
		
		Map<String, MachineInfo> smMachMap = sm.getMachineMap();
		if(smMachMap != null){
			for(String hostport : MachineList){
				MachineInfo minfo = this.MachineMap.get(hostport);
				if(minfo != null){
					minfo.status = MachineInfo.MachineStatus.ALLOC;
					this.MachineMap.put(hostport, minfo);
				}
				smMachMap.put(hostport, minfo);
			}
		}
		ClusterMap.put(target, sm);
		return true;
 	}
	
	@Override
	public synchronized boolean RemoveCluster(String ClusterName) {
		ServerManager sm = ClusterMap.get(ClusterName);
		return sm.RemoveAllS4Cluster();
	}

	@Override
	public ServerManager GetCluster(String ClusterName) {
		return ClusterMap.get(ClusterName);
	}


	@Override
	public Map<String, Map<String, String>> GetClusterMessage(String ClusterName) {
		ServerManager sm = ClusterMap.get(ClusterName);
		return sm.GetClusterMessage();
	}


	@Override
	public List<Machine> GetAllMachine() {
		List<Machine> MachineList = new ArrayList<Machine>();
		Set<String> hostportset = this.MachineMap.keySet();
		for(String hostport : hostportset){
			Machine machine = new Machine();
			machine.hostport = hostport;
			machine.status = this.MachineMap.get(hostport).status.toString();
			MachineList.add(machine);
		}
		return MachineList;
	}
	
	private class ConfigServerManager{
		private String ConfigFileUrl;
		
		public ConfigServerManager(String ConfigFileUrl){
			this.ConfigFileUrl = ConfigFileUrl;
		}
		
		public void writeSubMachList(List<String> MachineList){
			BufferedWriter fw = null;
			try {
				fw = new BufferedWriter(new FileWriter(this.ConfigFileUrl));
				for(String str : MachineList){
					fw.write(str);
					fw.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(fw != null) {
					try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public List<String> readSubMachList(){
			List<String> MachineList = new ArrayList<String>();
			BufferedReader fr = null;
			try {
				fr = new BufferedReader(new FileReader(this.ConfigFileUrl));
				String str;
				while((str = fr.readLine()) != null){
					System.out.println(str);
					MachineList.add(str.trim());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(fr != null){
					try {
						fr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return MachineList;
		}
	}
	
	public static void main(String[] args){
		FileDataManager fdm = new FileDataManager(null);

		System.out.println(fdm.GetAllMachine());
		
		ServerManager sm = fdm.GetCluster("denghankun@localhost:2181");
		if(sm != null){
			String NodeConfig = "<config version=\"-1\"><cluster name=\"s4\" type=\"s4\" mode=\"unicast\">" +
   " <node><partition>0</partition><machine>192.168.1.15</machine><port>5077</port><taskId>s4node-0</taskId>" +
   "</node></cluster></config>";
			//sm.AddS4Server("s4", "client-adapter", NodeConfig);
			try {
				sm.TaskSetup(NodeConfig);
				sm.StartS4ServerCluster("s4", "client-adapter");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
