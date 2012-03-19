package io.s4.manager.test;

import java.util.Map;
import java.util.Set;

import io.s4.manager.core.ServerManager;

public class testservermanager {
	private ServerManager smanager = new ServerManager("localhost:2181");
	
	public void testGetSomeMessage(){
		try {
			Set<String> nodepaths;
			
			Map<String, String> maps4 = smanager.GetRunningS4Server();
			nodepaths = maps4.keySet();
			for(String np : nodepaths){
				System.out.println(np + ":" + maps4.get(np));
			}
			
			
			Map<String, String> mapadapter = smanager.GetRunningAdapter();
			nodepaths = mapadapter.keySet();
			for(String np : nodepaths){
				System.out.println(np + ":" + mapadapter.get(np));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addS4Server(){
		smanager.StartS4Server("/s4/s4/task/task-0");
	}
	
	public void deleteS4Server(){
		smanager.RemoveS4Server("/s4/s4/process/task-0");
	}
	
	public static void main(String[] args){
		testservermanager tsm = new testservermanager();
		//tsm.testGetSomeMessage();
		//tsm.addS4Server();
		tsm.deleteS4Server();
	}
}
