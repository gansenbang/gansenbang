package io.s4.manager.core;

import java.util.Map;

public interface CommEventCallback {
	
	public void handleCallback(Map<String, Object> eventData);

}
