package io.s4.manager.test;

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

	public static void main(String[] args) {
		try {
			TTransport transport = new TSocket("localhost", 10030);
			transport.open();
			// set the protocol
			TProtocol protocol = new TBinaryProtocol(transport);
			
			ManagerServer.Client client = new ManagerServer.Client(protocol);
			
			List<String> s4serverlist = new ArrayList<String>();
			s4serverlist.add("192.168.1.25:5077");
			List<String> adapterlist = new ArrayList<String>();
			adapterlist.add("192.168.1.25:6077");
			
			
			System.out.println(client.GetAllMachinesList());
			System.out.println(client.GetAllClustersList());
			
			
			System.out.println(client.GetAllMachinesList());
			System.out.println(client.GetAllClustersList());
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}
}
