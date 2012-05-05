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
package io.s4.dispatcher.partitioner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoundRobinPartitioner implements Partitioner {
    private int counter = 0;
    private Set<String> streamNameSet;

    public void setStreamNames(String[] streamNames) {
        streamNameSet = new HashSet<String>(streamNames.length);
        for (String eventType : streamNames) {
            streamNameSet.add(eventType);
        }
    }

    @Override
    public List<CompoundKeyInfo> partition(String streamName, Object event,
            int partitionCount) {

        if (streamName != null && streamNameSet != null
                && !streamNameSet.contains(streamName)) {
            return null;
        }

        CompoundKeyInfo partitionInfo = new CompoundKeyInfo();
        int partitionId = 0;

        synchronized (this) {
            counter++;
            if (counter < 0) {
                counter = 0;
            }
            partitionId = counter % partitionCount;
        }

        partitionInfo.setPartitionId(partitionId);
        List<CompoundKeyInfo> partitionInfoList = new ArrayList<CompoundKeyInfo>();
        partitionInfoList.add(partitionInfo);

        return partitionInfoList;
    }
}
