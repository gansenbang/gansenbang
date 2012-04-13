package io.s4.manager.test;

import java.util.List;

import io.s4.manager.util.ConfigParser;
import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser.Config;
import io.s4.manager.util.TaskSetupApp;

public class testtasksetup {
	public static String setupxml = "/home/denghankun/s4project/s4/build/s4-image/s4-core" +
			"/conf/dynamic/clusters.xml";
	
	public static void main(String[] args) throws Exception{
		
		Config config = parseXML();
		/*
		List<Cluster> clusters = config.getClusters();
		for(Cluster cluster : clusters){
			System.out.println(cluster);
		}
		*/
		
		TaskSetupApp.doMain("localhost:2181", true, false, setupxml);
	}
	
	public static Config parseXML(){
		ConfigParser parser = new ConfigParser();
		Config config = null;
		try {
			config = parser.parse(setupxml, true, ConfigParser.StringType.CONFIGURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return config;
	}
}
