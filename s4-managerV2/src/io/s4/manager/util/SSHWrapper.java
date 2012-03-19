package io.s4.manager.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHWrapper {
	private static final int TIME_OUT = 1000*5*60;
	private String charset = Charset.defaultCharset().toString();
	private Connection conn;
	
	protected void processOutStream(InputStream in, Map<String, String> result, String charset) throws IOException{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while((line = br.readLine()) != null){
			//System.out.println(line);
			if(line.equals("start finished")) break;
		}
	}
	
	protected void processErrStream(InputStream err, Map<String, String> result, String charset) throws IOException{
	}
	
	public boolean Authenticated(String hostname, String username, String password) throws IOException{
		conn = new Connection(hostname);
		conn.connect();
		boolean isAuthenticated = conn.authenticateWithPassword(username, password);
		if(isAuthenticated == false){
			throw new IOException("Authentication failed");
		}
		return isAuthenticated;
	}
	
	public void close(){
		conn.close();
	}
	
	public Map<String, String> RunRemoteCommand(String command) throws IOException{
		Session sess = conn.openSession();
		sess.execCommand(command);
		
		Map<String, String> execret = new HashMap<String, String>();
		InputStream stdout = new StreamGobbler(sess.getStdout());
		//处理输出的结果
		processOutStream(stdout, execret, charset);
		InputStream stderr = new StreamGobbler(sess.getStderr());
		//处理出错
		processErrStream(stderr, execret, charset);
		
		sess.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
		Integer ret = sess.getExitStatus();
		sess.close();
		execret.put("result", ret.toString());
		return execret;
	}
}	
