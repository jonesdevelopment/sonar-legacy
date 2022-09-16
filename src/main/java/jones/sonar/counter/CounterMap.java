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

package jones.sonar.counter;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class CounterMap {

    public final long decay;

    private long decayInNanos = 1000000000L;

    private HashSet<Long> map = null;

    public CounterMap build() {
        map = new HashSet<>();
        decayInNanos = decay * 1000000L;
        return this;
    }

    public CounterMap build(final int maxSize) {
        map = new HashSet<>(maxSize);
        decayInNanos = decay * 1000000L;
        return this;
    }

    public long get() {
        cleanUp();
        return map.size();
    }

    public void increment() {
        map.add(System.nanoTime());
    }

    private void cleanUp() {
        final long timeStamp = System.nanoTime();

        map.stream()
                .filter(time -> (timeStamp - time) >= decayInNanos)
                .collect(Collectors.toSet())
                .forEach(time -> map.remove(time));
    }
}
