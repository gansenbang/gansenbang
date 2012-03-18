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
package io.s4.dispatcher;

import io.s4.collector.EventWrapper;
import io.s4.dispatcher.partitioner.CompoundKeyInfo;
import io.s4.dispatcher.partitioner.Partitioner;
import io.s4.dispatcher.partitioner.VariableKeyPartitioner;
import io.s4.dispatcher.transformer.Transformer;
import io.s4.emitter.EventEmitter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Dispatcher implements EventDispatcher {
    private EventEmitter eventEmitter;
    private Transformer[] transformers = new Transformer[0];
    private Partitioner[] partitioners = new Partitioner[0];
    private String configFilename;
    private boolean debug = false;
    private String loggerName = "s4";

    public final static String PARTITION_INFO_KEY = "S4__PartitionInfo";

    public void setTransformers(Transformer[] transformers) {
        this.transformers = transformers;
    }

    public void setPartitioners(Partitioner[] partitioners) {
        this.partitioners = partitioners;
    }

    public void setEventEmitter(EventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }

    public void setConfigFilename(String configFilename) {
        this.configFilename = configFilename;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    private volatile int eventCount = 0;
    private volatile int rawEventCount = 0;

    public Dispatcher() {

    }

    int counts[];

    public void init() {

        Runnable r = new Runnable() {
            private long configFileTime = -1;

            public void run() {
                long lastCheckTime = System.currentTimeMillis();
                int lastEventCount = eventCount;
                int lastRawEventCount = rawEventCount;
                while (!Thread.currentThread().isInterrupted()) {
                    int eventCount = Dispatcher.this.eventCount;
                    long currentTime = System.currentTimeMillis();
                    double rate = (eventCount - lastEventCount)
                            / ((currentTime - lastCheckTime) / 1000.0);
                    double rawRate = (rawEventCount - lastRawEventCount)
                            / ((currentTime - lastCheckTime) / 1000.0);
                    lastCheckTime = currentTime;
                    lastEventCount = eventCount;
                    lastRawEventCount = rawEventCount;
                    Logger.getLogger(loggerName).info("Event count is "
                            + eventCount + "; rate " + rate);
                    Logger.getLogger(loggerName).info("Raw event count is "
                            + rawEventCount + "; rate " + rawRate);
                    if (counts != null) {
                        for (int i = 0; i < counts.length; i++) {
                            Logger.getLogger(loggerName).info(i + ": "
                                    + counts[i]);
                        }
                    }

                    configCheck();

                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

            }

            private void configCheck() {
                if (configFilename == null) {
                    return;
                }

                File file = new File(configFilename);
                if (!file.exists()) {
                    return;
                }
                long lastModified = file.lastModified();
                if (configFileTime == -1) {
                    configFileTime = lastModified;
                    return;
                }

                if (lastModified > configFileTime) {
                    Logger.getLogger(loggerName)
                          .info("Config file has changed. Exiting!!");
                    System.exit(4);
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    @Override
    public void dispatchEvent(String streamName,
                              List<List<String>> compoundKeyNames, Object event) {
        dispatchEvent(streamName, event, true, compoundKeyNames);
    }

    @Override
    public void dispatchEvent(String streamName, Object event) {
        dispatchEvent(streamName, event, false, null);
    }

    private void dispatchEvent(String streamName, Object event,
                               boolean variableKey,
                               List<List<String>> compoundKeyNames) {
        synchronized (this) {
            rawEventCount++;
        }
        if (eventEmitter.getNodeCount() <= 0) {
            return;
        } else {
            if (counts == null) {
                counts = new int[eventEmitter.getNodeCount()];
            }
        }

        try {
            synchronized (this) {
                eventCount++;
            }

            List<CompoundKeyInfo> partionInfoList = new ArrayList<CompoundKeyInfo>();
            for (Partitioner partitioner : partitioners) {
                List<CompoundKeyInfo> pInfoList = null;

                if (!variableKey) {
                    pInfoList = partitioner.partition(streamName,
                                                      event,
                                                      eventEmitter.getNodeCount());
                } else {
                    if (partitioner instanceof VariableKeyPartitioner) {
                    	//注意修改这里
                        VariableKeyPartitioner vp = (VariableKeyPartitioner) partitioner;
                        pInfoList = vp.partition(streamName,
                                                 compoundKeyNames,
                                                 event,
                                                 eventEmitter.getNodeCount());
                    }
                }

                if (pInfoList != null) {
                    partionInfoList.addAll(pInfoList);
                }
            }

            Map<Integer, List<CompoundKeyInfo>> pInfoMap = new HashMap<Integer, List<CompoundKeyInfo>>();
            for (CompoundKeyInfo partitionInfo : partionInfoList) {
                int partitionId = partitionInfo.getPartitionId();
                List<CompoundKeyInfo> listByPartitionNumber = pInfoMap.get(partitionId);
                if (listByPartitionNumber == null) {
                    listByPartitionNumber = new ArrayList<CompoundKeyInfo>();
                    pInfoMap.put(partitionId, listByPartitionNumber);
                }
                listByPartitionNumber.add(partitionInfo);
            }

            for (int partitionId : pInfoMap.keySet()) {
                EventWrapper eventWrapper = new EventWrapper(streamName,
                                                             event,
                                                             pInfoMap.get(partitionId));
                counts[partitionId]++;
                eventEmitter.emit(partitionId, eventWrapper);
            }
        } catch (Exception e) {
            Logger.getLogger(loggerName)
                  .error("Exception in processEvent on thread "
                                 + Thread.currentThread().getId() + " at time "
                                 + System.currentTimeMillis(),
                         e);
        }
    }

}
