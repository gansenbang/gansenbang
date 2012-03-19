package io.s4.manager.test;

import java.io.IOException;

import io.s4.manager.util.SSHWrapper;

public class testssh {
	public static void main(String[] args) {
		SSHWrapper sw = new SSHWrapper();
		try {
			sw.Authenticated("localhost", "denghankun", "dhk19890630");
			String s4_image = "/home/denghankun/s4project/s4/build/s4-image/scripts/";
			sw.RunRemoteCommand(s4_image + "start-s4.sh -r client-adapter &");
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
