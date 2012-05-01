package io.s4.logger;

import java.util.Map;

import org.apache.thrift.TException;

public class S4MonitorImpl implements S4Monitor.Iface{
	Map<String, Integer> metricMap;
	
	public S4MonitorImpl(Map<String, Integer> metricmap){
		this.metricMap = metricmap;
	}
	@Override
	public Map<String, Integer> monitor() throws TException {
		return metricMap;
	}

}
