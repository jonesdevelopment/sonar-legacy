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

import jones.sonar.bungee.config.Config;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class PlayerQueue {

    public final Map<String, Long> QUEUE = new ConcurrentHashMap<>(50000);

    public final ExecutorService SERVICE = Executors.newSingleThreadExecutor();

    public void clear() {
        if (QUEUE.isEmpty()) return;

        SERVICE.execute(() -> {
            final Map<String, Long> cleaned = new HashMap<>(QUEUE);

            final AtomicInteger currentIndex = new AtomicInteger(0);

            cleaned.forEach((name, position) -> {
                if (currentIndex.get() > Config.Values.MAXIMUM_QUEUE_POLL_RATE) return;

                if (position < 200) {
                    currentIndex.incrementAndGet();

                    remove(name);
                }
            });

            QUEUE.forEach((name, position) -> QUEUE.replace(name, position - currentIndex.get()));
        });
    }

    public void remove(final String playerName) {
        if (contains(playerName)) {
            QUEUE.remove(playerName);
        }
    }

    public void addToQueue(final String playerName) {
        if (!contains(playerName)) {
            QUEUE.put(playerName, QUEUE.size() + 1L);
        }
    }

    public boolean contains(final String playerName) {
        return QUEUE.containsKey(playerName);
    }

    public long getPosition(final String playerName) {
        if (contains(playerName)) {
            return QUEUE.get(playerName);
        } else {
            return -1;
        }
    }

    public long getPositionOrCreate(final String playerName) {
        if (!contains(playerName)) {
            addToQueue(playerName);
        }
        return QUEUE.get(playerName);
    }
}
