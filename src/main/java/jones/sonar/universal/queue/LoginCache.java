package jones.sonar.universal.queue;

import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.platform.SonarPlatform;
import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class LoginCache {
    public final Set<String> HAVE_LOGGED_IN = new HashSet<>();

    public void removeAllUnused(final SonarPlatform platform) {
        switch (platform) {
            case BUNGEE: {
                HAVE_LOGGED_IN.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()).forEach(name -> {
                            final ProxiedPlayer proxiedPlayer = SonarBungee.INSTANCE.proxy.getPlayer(name);

                            if (proxiedPlayer == null) {
                                return;
                            }

                            final InetAddress inetAddress = ((InetSocketAddress) proxiedPlayer.getPendingConnection().getSocketAddress()).getAddress();

                            if (Blacklist.isBlacklisted(inetAddress)) {
                                HAVE_LOGGED_IN.remove(name);
                            }
                        });
                break;
            }

            default:
            case VELOCITY: {
                break;
            }
        }
    }
}
