package io.s4.test;
import io.s4.processor.PEContainer;
import io.s4.util.clock.WallClock;
import io.s4.listener.CommLayerListener;
import io.s4.collector.EventListener;
import io.s4.serialize.KryoSerDeser;
import io.s4.logger.Log4jMonitor;

public class TestS4 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log4jMonitor monitor = new Log4jMonitor();
		monitor.setFlushInterval(30);
		monitor.setLoggerName("monitor");
		WallClock wallclock = new WallClock();
		
		//set up peContainer
		PEContainer peContainer = new PEContainer();
		peContainer.setMaxQueueSize(8000);
		peContainer.setClock(wallclock);
		peContainer.setTrackByKey(false);
		peContainer.setMonitor(monitor);
		
		//set up rawListener
		CommLayerListener rawListener = new CommLayerListener();
		KryoSerDeser serDeser = new KryoSerDeser();
		serDeser.setInitialBufferSize(2048);
		serDeser.setMaxBufferSize(262144);
		rawListener.setSerDeser(serDeser);
		rawListener.setClusterManagerAddress("localhost:2181");
		rawListener.setAppName("s4");
		rawListener.setMaxQueueSize(8000);
		rawListener.setMonitor(monitor);
		rawListener.init();
		
		//set up eventListener
		EventListener eventListener = new EventListener();
		eventListener.setRawListener(rawListener);
		eventListener.setPeContainer(peContainer);
		eventListener.setMonitor(monitor);
		eventListener.init();
		
		//init
		peContainer.init();
		
		TestPE testpe = new TestPE();
		testpe.setClock(wallclock);
		String[] keys = {"test *"};
		testpe.setKeys(keys);
		peContainer.addProcessor(testpe);
	}

}
