package io.s4.manager.persist;

import io.s4.manager.cluster.Cluster;
import io.s4.manager.cluster.Machine;
import io.s4.manager.core.ServerManager;
import io.s4.manager.util.ConfigParser;
import io.s4.manager.util.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
			parseDocument(this.XMLFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		VerifyMachineMap();
	}
	
	private void VerifyMachineMap(){
		
	}
	
	private void parseDocument(String configFilename) throws Exception{
		Document document = ConfigParser.createDocument(configFilename);
		NodeList topLevelNodeList = document.getChildNodes();
		for (int i = 0; i < topLevelNodeList.getLength(); ++i) {
			Node node = topLevelNodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("machines")) {
				NodeList machinelist = node.getChildNodes();
				for(int j = 0; j < machinelist.getLength(); ++j){
					Node machine = machinelist.item(j);
					if(machine.getNodeType() == Node.ELEMENT_NODE && machine.getNodeName().equals("machine")){
						MachineInfo minfo = processMachineElement(machine);
						MachineMap.put(minfo.host + ":" + minfo.port, minfo);
					}
				}
			}
		}
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
		FileOutputStream clustersfile;
		try {
			clustersfile = new FileOutputStream(dirpath + "/" + Constant.CLUSTER_FILE);
			clustersfile.write(xmlconfig.getBytes(Charset.defaultCharset()));
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
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
			sm = new ServerManager(ZkAddress);
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
}
