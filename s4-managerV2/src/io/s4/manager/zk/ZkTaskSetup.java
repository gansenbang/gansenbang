/*
 * Copyright (c) 2010 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 	        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License. See accompanying LICENSE file. 
 */
package io.s4.manager.zk;

import io.s4.manager.core.CommEventCallback;
import io.s4.manager.zk.DefaultWatcher;
import io.s4.manager.util.CommUtil;
import io.s4.manager.util.JSONUtil;
import io.s4.manager.util.ConfigParser.Cluster.ClusterType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class ZkTaskSetup extends DefaultWatcher {
	String tasksListRoot;
	String processListRoot;

	public ZkTaskSetup(String address, String clusterName,
			ClusterType clusterType) {
		this(address, clusterName, clusterType, null);
	}

	/**
	 * Constructor of ZkTaskSetup
	 * 
	 * @param address
	 * @param clusterName
	 */
	public ZkTaskSetup(String address, String clusterName,
			ClusterType clusterType, CommEventCallback callbackHandler) {
		super(address, callbackHandler);

		this.root = "/" + clusterName + "/" + clusterType.toString();
		this.tasksListRoot = root + "/task";
		this.processListRoot = root + "/process";
	}

	public void setUpTasks(Object[] data) {
		setUpTasks("-1", data);
	}

	/**
	 * Creates task nodes.
	 * 
	 * @param version
	 * @param data
	 */
	public void setUpTasks(String version, Object[] data) {
		try {
			if (!version.equals("-1")) {
				if (!isConfigVersionNewer(version)) {
					return;
				} else {
					cleanUp();
				}
			} 

			// check if config data newer
			if (!isConfigDataNewer(data)) {
				return;
			} else {
				cleanUp();
			}

			// Create ZK node name
			if (zk != null) {
				Stat s;
				s = zk.exists(root, false);
				if (s == null) {
					String parent = new File(root).getParent().replace(File.separatorChar, '/');
					Stat exists = zk.exists(parent, false);
					if (exists == null) {
						zk.create(parent, new byte[0], Ids.OPEN_ACL_UNSAFE,
								CreateMode.PERSISTENT);
					}
					zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
				}
			}
			Stat s;
			s = zk.exists(tasksListRoot, false);
			if (s == null) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("config.version", version);
				String jsonString = JSONUtil.toJsonString(map);
				zk.create(tasksListRoot, jsonString.getBytes(),
						Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			s = zk.exists(processListRoot, false);
			if (s == null) {
				zk.create(processListRoot, new byte[0], Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);

			}

			for (int i = 0; i < data.length; i++) {
				String nodeName = tasksListRoot + "/" + "task" + "-" + i;
				Stat sTask = zk.exists(nodeName, false);
				if (sTask == null) {
					byte[] byteBuffer = JSONUtil.toJsonString(data[i]).getBytes();
					zk.create(nodeName, byteBuffer, Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isConfigDataNewer(Object[] data) {
		try {
			Stat s;
			s = zk.exists(tasksListRoot, false);
			if (s != null) {
				List<String> children = zk.getChildren(tasksListRoot, false);
				if (children.size() != data.length) {
					return true;
				}
				boolean[] matched = new boolean[data.length];
				for (String child : children) {
					String childPath = tasksListRoot + "/" + child;
					Stat sTemp = zk.exists(childPath, false);
					byte[] tempData = zk.getData(tasksListRoot + "/" + child,
							false, sTemp);
					Map<String, Object> map = (Map<String, Object>) JSONUtil.getMapFromJson(new String(tempData));

					// check if it matches any of the data
					for (int i = 0; i < data.length; i++) {
						Map<String, Object> newData = (Map<String, Object>) data[i];
						if (!matched[i] && CommUtil.compareMaps(newData, map)) {
							matched[i] = true;
							break;
						}
					}
				}
				for (int i = 0; i < matched.length; i++) {
					if (!matched[i]) {
						return true;
					}
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			throw new RuntimeException(
					" Exception in isConfigDataNewer method ", e);
		}
		return false;
	}

	private boolean isConfigVersionNewer(String version) throws Exception {
		Stat s;
		s = zk.exists(tasksListRoot, false);
		if (s != null) {
			byte[] data = zk.getData(tasksListRoot, false, s);
			if (data != null && data.length > 0) {
				String jsonString = new String(data);
				if (jsonString != null) {
					Map<String, Object> map = JSONUtil.getMapFromJson(jsonString);
					if (map.containsKey("config.version")) {
						boolean update = false;
						String currentVersion = map.get("config.version").toString();
						String[] curV = currentVersion.split("\\.");
						String[] newV = version.split("\\.");
						for (int i = 0; i < Math.max(curV.length, newV.length); i++) {
							if (Integer.parseInt(newV[i]) > Integer.parseInt(curV[i])) {
								update = true;
								break;
							}
						}
						return update;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Will clean up taskList Node and process List Node
	 */
	public boolean cleanUp() {
		try {
			Stat exists = zk.exists(tasksListRoot, false);
			if (exists != null) {
				List<String> children = zk.getChildren(tasksListRoot, false);
				if (children.size() > 0) {
					for (String child : children) {
						zk.delete(tasksListRoot + "/" + child, 0);
					}
				}
				zk.delete(tasksListRoot, 0);
			}

			exists = zk.exists(processListRoot, false);
			if (exists != null) {
				List<String> children = zk.getChildren(processListRoot, false);
				if (children.size() > 0) {
					for (String child : children) {
						zk.delete(processListRoot + "/" + child, 0);
					}
				}
				zk.delete(processListRoot, 0);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
