/*
 *  Copyright (c) 2022, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.universal.queue;

import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class IPSQueue {

    public final Map<InetAddress, Long> QUEUE = new ConcurrentHashMap<>(350000);

    public void remove(final InetAddress inetAddress) {
        if (contains(inetAddress)) {
            QUEUE.remove(inetAddress);
        }
    }

    public void addToQueue(final InetAddress inetAddress) {
        if (!contains(inetAddress)) {
            QUEUE.put(inetAddress, QUEUE.size() + 1L);
        }
    }

    public boolean contains(final InetAddress inetAddress) {
        return QUEUE.containsKey(inetAddress);
    }

    public long getPosition(final InetAddress inetAddress) {
        if (contains(inetAddress)) {
            return QUEUE.get(inetAddress);
        } else {
            return -1;
        }
    }

    public long getPositionOrCreate(final InetAddress inetAddress) {
        if (!contains(inetAddress)) {
            addToQueue(inetAddress);
        }
        return QUEUE.get(inetAddress);
    }
}
