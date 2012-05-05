package io.s4.manager.test;

import java.io.IOException;

import io.s4.manager.util.SSHWrapper;

public class testssh {
	public static void main(String[] args) {
		SSHWrapper sw = new SSHWrapper();
		try {
			sw.Authenticated("192.168.1.15", "lenovo", "cloud");
			String s4_image = "/home/lenovo/s4project/build/s4-image/scripts";
			sw.RunRemoteCommand(s4_image + "/start-s4.sh -r client-adapter -g s4 -z 192.168.1.15:2181 -p 5077 dynamic &");
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
