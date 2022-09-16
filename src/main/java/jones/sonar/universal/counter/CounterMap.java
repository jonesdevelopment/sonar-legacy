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

package jones.sonar.universal.counter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class CounterMap {

    public final long decay;

    private Cache<Long, Byte> cache = null;

    public CounterMap build() {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(2)
                .expireAfterWrite(decay, TimeUnit.MILLISECONDS)
                .build();
        return this;
    }

    public CounterMap build(final int maxSize) {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(2)
                .maximumSize(maxSize)
                .expireAfterWrite(decay, TimeUnit.MILLISECONDS)
                .build();
        return this;
    }

    public long get() {
        cache.cleanUp();
        return cache.size();
    }

    public void increment() {
        cache.put(System.nanoTime(), (byte) 0);
    }
}
