package io.s4.manager.test;

import java.util.List;

import io.s4.manager.core.ServerManager;
import io.s4.manager.util.ConfigParser;
import io.s4.manager.util.ConfigParser.Cluster;
import io.s4.manager.util.ConfigParser.Config;

public class testtasksetup {
	public static String setupxml = "/home/denghankun/s4project/s4/build/s4-image/s4-core" +
			"/conf/dynamic/clusters.xml";
	
	public static void main(String[] args) throws Exception{
		ServerManager sm = new ServerManager("localhost:2181");
		Config config = parseXML();
		List<Cluster> clusters = config.getClusters();
		for(Cluster cluster : clusters){
			System.out.println(cluster);
		}
	}
	
	public static Config parseXML(){
		Config config = new Config();
		return config;
	}
}
