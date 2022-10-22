package jones.sonar.bungee.caching;

import jones.sonar.bungee.config.Config;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class ServerPingCache {

    public final Set<InetAddress> HAS_PINGED = new HashSet<>();

    public void removeAllUnused() {
        HAS_PINGED.stream()
                .filter(Blacklist::isBlacklisted)
                .collect(Collectors.toSet())
                .forEach(HAS_PINGED::remove);
    }

    public ServerPing cachedServerPing = null;

    public boolean needsUpdate = true;

    public ServerPing getCached(final ListenerInfo server, final String motd) {
        if (!Config.Values.CACHE_MOTDS) {
            return getServerPing(server, motd);
        }

        if (needsUpdate || cachedServerPing == null) {
            needsUpdate = false;

            return update(server, motd);
        }

        return cachedServerPing;
    }

    private ServerPing update(final ListenerInfo server, final String motd) {
        cachedServerPing = getServerPing(server, motd);

        return cachedServerPing;
    }

    private ServerPing getServerPing(final ListenerInfo server, final String motd) {
        return new ServerPing(
                new ServerPing.Protocol(Config.Values.SERVER_BRAND, 120),
                new ServerPing.Players(server.getMaxPlayers(), SonarBungee.INSTANCE.proxy.getOnlineCount(), null),
                motd, SonarBungee.INSTANCE.proxy.getConfig().getFaviconObject());
    }
}
