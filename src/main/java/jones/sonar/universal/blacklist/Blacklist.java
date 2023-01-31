package jones.sonar.universal.blacklist;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Blacklist {
    public final Cache<InetAddress, Byte> TEMP_BLACKLISTED = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.SECONDS)
            .initialCapacity(1)
            .build();

    public final Cache<InetAddress, Byte> BLACKLISTED = CacheBuilder.newBuilder()
            .expireAfterWrite(30L, TimeUnit.MINUTES)
            .initialCapacity(1)
            .build();

    public final Set<InetAddress> FIREWALLED = new HashSet<>();

    public long size() {
        return BLACKLISTED.size();
    }

    public long fullSize() {
        return BLACKLISTED.size() + TEMP_BLACKLISTED.size();
    }

    public void removeFromBlacklist(final InetAddress inetAddress) {
        BLACKLISTED.asMap().remove(inetAddress);
    }

    public void addToBlacklist(final InetAddress inetAddress) {
        BLACKLISTED.put(inetAddress, (byte) 0);
    }

    public void addToTempBlacklist(final InetAddress inetAddress) {
        TEMP_BLACKLISTED.put(inetAddress, (byte) 0);
    }

    public boolean isBlacklisted(final InetAddress inetAddress) {
        return BLACKLISTED.asMap().containsKey(inetAddress);
    }

    public boolean isTempBlacklisted(final InetAddress inetAddress) {
        return TEMP_BLACKLISTED.asMap().containsKey(inetAddress);
    }
}
