package jones.sonar.universal.whitelist;

import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class Whitelist {
    public final Set<InetAddress> WHITELISTED = new HashSet<>();

    public long size() {
        return WHITELISTED.size();
    }

    public void removeFromWhitelist(final InetAddress inetAddress) {
        if (!isWhitelisted(inetAddress)) return;

        WHITELISTED.remove(inetAddress);
    }

    public void addToWhitelist(final InetAddress inetAddress) {
        if (isWhitelisted(inetAddress)) return;

        WHITELISTED.add(inetAddress);
    }

    public boolean isWhitelisted(final InetAddress inetAddress) {
        return WHITELISTED.contains(inetAddress);
    }
}
