package io.s4.test;
import org.json.JSONObject;
import io.s4.emitter.CommLayerEmitter;
import io.s4.listener.CommLayerListener;
import io.s4.serialize.KryoSerDeser;
import io.s4.logger.Log4jMonitor;
import io.s4.collector.EventWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;


public class TestEmit {
	public static void main(String[] args) throws JSONException{
		KryoSerDeser serDeser = new KryoSerDeser();
		Log4jMonitor monitor = new Log4jMonitor();
		monitor.setFlushInterval(30);
		monitor.setLoggerName("monitor");
		Message m = new Message("test", "java.lang.Object", "abc");
		JSONObject json = new JSONObject();
		m.toJson(json);
		EventWrapper eventWrapper = new EventWrapper("test","this is a test!",null);
		/*
		CommLayerListener rawListener = new CommLayerListener();
		rawListener.setSerDeser(serDeser);
		rawListener.setClusterManagerAddress("localhost:2181");
		rawListener.setAppName("client-adapter");
		rawListener.setMaxQueueSize(8000);
		rawListener.setMonitor(monitor);
		rawListener.init();*/
		
		CommLayerEmitter commLayerEmitter = new CommLayerEmitter();
		commLayerEmitter.setSerDeser(serDeser);
		commLayerEmitter.setAppName("client-adapter");
		commLayerEmitter.setListenerAppName("s4");
		//commLayerEmitter.setListener(rawListener);
		commLayerEmitter.setMonitor(monitor);
		commLayerEmitter.init();
		while(true){/*
			String buffer;
			Scanner scanner = new Scanner(System.in);
			buffer = scanner.next();
			if(buffer.trim().equals("emit"))*/
			commLayerEmitter.emit(0, eventWrapper);
		}
	}
}
