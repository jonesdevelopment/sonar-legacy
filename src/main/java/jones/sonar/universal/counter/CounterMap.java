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
