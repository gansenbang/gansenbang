package io.s4.manager.zk;

import io.s4.manager.core.CommEventCallback;
import io.s4.manager.core.CommState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;

public class DefaultWatcher implements Watcher{
	public static List<KeeperState> interestingStates = new ArrayList<KeeperState>();
	static {
		interestingStates.add(KeeperState.Expired);
		interestingStates.add(KeeperState.SyncConnected);
	}
	protected ZooKeeper zk = null;
	protected Integer mutex;
	protected String root;
	protected WatchedEvent currentEvent;
	protected CommEventCallback callbackHandler;
	private String zkAddress;
	volatile boolean connected = false;
	
	protected DefaultWatcher(String address){
		this(address, null);
	}
	
	protected DefaultWatcher(String address, CommEventCallback callbackHandler){
		this.zkAddress = address;
		this.callbackHandler = callbackHandler;
		if(zk == null){
			try{
				String sTimeout = System.getProperty("zk.session.timeout");
				int timeout = 30000;
				if(sTimeout != null){
					try{
						timeout = Integer.parseInt(sTimeout);
					} catch (Exception e){
						//ignore will use default
					}
				}
				mutex = new Integer(-1);
				synchronized(mutex){
					zk = new ZooKeeper(address, timeout, this);
					while(!connected){
						mutex.wait();
					}
				}
			} catch (Exception e) {
				zk = null;
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	synchronized public void process(WatchedEvent event) {
		synchronized(mutex){
			currentEvent = event;
			if(event.getState() == KeeperState.SyncConnected){
				connected = true;
			}
			if(callbackHandler != null &&
					interestingStates.contains(event.getState())){
				Map<String, Object> eventData = new HashMap<String, Object>();
				if(event.getState() == KeeperState.SyncConnected){
					eventData.put("state", CommState.INITIALIZED);
				} else if(event.getState() == KeeperState.Expired){
					eventData.put("state", CommState.BROKEN);
				}
				eventData.put("source", event);
				callbackHandler.handleCallback(eventData);
			}
			mutex.notify();
		}
	}
	
	public String getZkAddress(){
		return this.zkAddress;
	}
}
