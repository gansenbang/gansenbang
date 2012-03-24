package io.s4.manager.persist;

public class MachineInfo {
	public enum MachineStatus{
		FREE("free"),ALLOC("alloc");
		
		private final String serverStatusString;
		
		private MachineStatus(String serverStatusString){
			this.serverStatusString = serverStatusString;
		}
		
		public String toString(){
			return serverStatusString;
		}
	}
	
	public MachineInfo(String host, String user, int port, String pass, String s4image){
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.port = port;
		this.s4image = s4image;
		this.status = MachineStatus.FREE;
		this.znode = null;
	}
	
	public MachineStatus status;
	public String host;
	public String user;
	public int port;
	public String pass;
	public String s4image;
	public String znode;
	
	@Override
	public String toString() {
		return "MachineInfo {status=" + status + ", host=" + host
				+ ", port=" + port + ", pass=" + pass + ", s4image="
				+ s4image + ", znode=" + znode + "}";
	}
	
}
