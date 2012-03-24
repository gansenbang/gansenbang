package io.s4.manager.core;

import io.s4.manager.cluster.ManagerServer;
import io.s4.manager.cluster.ManagerServerImpl;
import io.s4.manager.persist.DataManager;
import io.s4.manager.persist.FileDataManager;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;



public class ClusterManager {
	private TServer server;
	
	public final DataManager dm;
	private final String XMLConfig = "clusters.xml";
	private final int port;
	
	public ClusterManager(){
		String target = getProperty("PORT");
		port = target != null ? Integer.valueOf(target) : 10030;
		dm = new FileDataManager(this.XMLConfig);
	}
	
	private static String getProperty(String property){
		return System.getProperty(property);
	}
	
	public void init() throws TTransportException{
		// set the server port
		TServerSocket serverTransport = new TServerSocket(port);

		// set the protocol factory
		Factory proFactory = new TBinaryProtocol.Factory();
					
		TProcessor processor = new ManagerServer.Processor<ManagerServerImpl>(new ManagerServerImpl(dm));
					
		server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport)
																															.processor(processor)
																															.protocolFactory(proFactory));
		System.out.println("Start server on port " + port +"...");
	}
	
	public void run(){
		server.serve();
	}
	
	
	
	public static void main(String[] args){
		ClusterManager cm = new ClusterManager();
		try {
			cm.init();
			
			cm.run();
			/*
			
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
