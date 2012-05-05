package io.s4.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.server.TThreadPoolServer;

import io.s4.logger.*;

public class ThriftMonitor implements Monitor{
	private Map<String, Integer> metricMap = new ConcurrentHashMap<String, Integer>();
	private TServerSocket serverTransport;
	private TServer server;
	
	public void init(){
		Factory proFactory = new TBinaryProtocol.Factory();
		TProcessor processor = new S4Monitor.Processor<S4MonitorImpl>(new S4MonitorImpl(metricMap));
		server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor).protocolFactory(proFactory));
		System.out.println("monitor server started...");
		//server.serve();
		Thread t = new Thread(MonitorServer);
		t.start();
	}
	
	@Override
	public void increment(String metricName, int increment,
			String metricEventName, String... furtherDistinctions) {
		increment(buildMetricName(metricName, metricEventName,
				furtherDistinctions), increment);
	}

	@Override
	public void increment(String metricName, int increment) {
		Integer currValue = metricMap.get(metricName);
		if (currValue == null) {
			currValue = 0;
		}
		currValue += increment;
		metricMap.put(metricName, currValue);
	}

	@Override
	public void set(String metricName, int value, String metricEventName,
			String... furtherDistinctions) {
		metricMap.put(buildMetricName(metricName, metricEventName,
				furtherDistinctions), value);
	}

	@Override
	public void set(String metricName, int value) {
		metricMap.put(metricName, value);
	}

	@Override
	public void flushStats() {
		
	}

	@Override
	public void setDefaultValue(String key, int val) {
		
	}

	private String buildMetricName(String metricName, String metricEventName,
			String[] furtherDistinctions) {
		StringBuffer sb = new StringBuffer(metricEventName);
		sb.append(":");
		sb.append(metricName);
		if (furtherDistinctions != null) {
			for (String furtherDistinction : furtherDistinctions) {
				sb.append(":");
				sb.append(furtherDistinction);
			}
		}
		return sb.toString().intern();

	}

	/*
	@Override
	public Map<String, Integer> monitor() throws TException {
		return metricMap;
	}*/
	
	public void setConnectionPort(int port) throws TTransportException{
		serverTransport = new TServerSocket(port);
	}
	
	private Runnable MonitorServer = new Runnable(){

		@Override
		public void run() {
			server.serve();
		}
	
	};
	
	public static void main(String[] args) throws TException{
		ThriftMonitor monitor = new ThriftMonitor();
		monitor.setConnectionPort(7791);
		monitor.init();
		monitor.set("abc", 1);
		monitor.set("efg", 2);
		
	}
}
