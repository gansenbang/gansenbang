package io.s4.manager.test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import io.s4.manager.cluster.ManagerServer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class testclient {
	
	public static final String ClusterName = "denghankun";
	public static final String ZkAddress = "192.168.1.100:2181";

	public static void main(String[] args) {
		try {
			TTransport transport = new TSocket("localhost", 10030);
			transport.open();
			// set the protocol
			TProtocol protocol = new TBinaryProtocol(transport);
			
			ManagerServer.Client client = new ManagerServer.Client(protocol);
			boolean isSucc;
			
			List<String> machinelist = new ArrayList<String>();
			machinelist.add("192.168.1.15:5077");
			isSucc = client.CreateCluster(ZkAddress, ClusterName, machinelist);
			System.out.println("CreateCluster:" + isSucc);
			System.out.println(readClusterConfig());
			String ClusterFullName = ClusterName + "@" + ZkAddress;
			isSucc = client.CommitS4ClusterXMLConfig(readClusterConfig(), ClusterFullName, true, null);
			System.out.println("Commit:" +isSucc);
			System.out.println(client.GetAllClustersList());
			
			isSucc = client.StartS4ServerCluster(ClusterFullName, "s4", "client-adapter");
			System.out.println("StartS4ServerCluster:" + isSucc);
			
			System.out.println(client.GetS4ClusterMessage(ClusterFullName));
			
			isSucc = client.RemoveS4Cluster(ClusterFullName, "s4");
			System.out.println("RemoveS4Cluster:" + isSucc);
			
			System.out.println(client.GetS4ClusterMessage(ClusterFullName));
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}
	
	public static String readClusterConfig(){
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("clusters.xml")));
			String clusters = "";
			String str = null;
			while((str = br.readLine()) != null){
				clusters += str;
			}
			return clusters;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
