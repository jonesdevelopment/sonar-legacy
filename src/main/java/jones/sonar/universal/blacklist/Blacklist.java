package jones.sonar.universal.blacklist;

import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class Blacklist {
    public final Set<InetAddress> BLACKLISTED = new HashSet<>();
    public final Set<InetAddress> FIREWALLED = new HashSet<>();

    public long size() {
        return BLACKLISTED.size();
    }

    public void removeFromBlacklist(final InetAddress inetAddress) {
        if (!isBlacklisted(inetAddress)) return;

        BLACKLISTED.remove(inetAddress);
    }

    public void addToBlacklist(final InetAddress inetAddress) {
        if (isBlacklisted(inetAddress)) return;

        BLACKLISTED.add(inetAddress);
    }

    public boolean isBlacklisted(final InetAddress inetAddress) {
        return BLACKLISTED.contains(inetAddress);
    }
}
