package cn.edu.scnu.s4;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.apache.s4.client.Driver;
import org.apache.s4.client.Message;

/**
 * @author ChunweiXu
 *
 */
public class S4SDSMSApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("/home/phoenixcw/cloud/s4/build/s4-image/s4-example-testinput/rate.jin");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Reader inputReader = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(inputReader);
        int cc = 0;
        try {
			for (String line = null; (line = br.readLine()) != null;) {
				System.out.println(line);
				++cc;
				if (cc == 10) break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		String hostName = "localhost";
        
        int port = 2334;
        String streamName = "TrafficFlow";
        String clazz = "cn.edu.scnu.s4.test.TrafficFlow";

        Driver d = new Driver(hostName, port);
        Reader inputReader = null;
        BufferedReader br = null;
        try {
            if (!d.init()) {
                System.err.println("Driver initialization failed");
                System.exit(1);
            }

            if (!d.connect()) {
                System.err.println("Driver initialization failed");
                System.exit(1);
            }
            
            FileInputStream fis = null;
            
    		try {
    			//fis = new FileInputStream("/home/user/s4/build/s4-image/s4-example-testinput/rate.jin");
    			fis = new FileInputStream("/home/phoenixcw/cloud/s4/build/s4-image/s4-example-testinput/rate.jin");
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
            inputReader = new InputStreamReader(fis);
    		//inputReader = new InputStreamReader(System.in);
            br = new BufferedReader(inputReader);

            for (String inputLine = null; (inputLine = br.readLine()) != null;) {
            	System.out.println(inputLine);
                Message m = new Message(streamName, clazz, inputLine);
                d.send(m);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                d.disconnect();
            } catch (Exception e) {
            }
            try {
                br.close();
            } catch (Exception e) {
            }
            try {
                inputReader.close();
            } catch (Exception e) {
            }
        }
	}

}
