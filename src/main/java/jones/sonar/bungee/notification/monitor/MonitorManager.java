package jones.sonar.bungee.notification.monitor;

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@UtilityClass
public class MonitorManager {
    public final Set<String> MONITOR_ENABLED = new HashSet<>();

    public Stream<ProxiedPlayer> getPlayers() {
        return MONITOR_ENABLED.stream()
                .map(SonarBungee.INSTANCE.proxy::getPlayer)
                .filter(Objects::nonNull)
                .filter(player -> player.hasPermission("sonar.monitor"));
    }

    public boolean toggle(final ProxiedPlayer player) {
        if (contains(player.getName())) {
            remove(player.getName());
        } else {
            add(player.getName());
        }

        return contains(player.getName());
    }

    public void add(final String playerName) {
        if (!contains(playerName)) {
            MONITOR_ENABLED.add(playerName);
        }
    }

    public void remove(final String playerName) {
        if (contains(playerName)) {
            MONITOR_ENABLED.remove(playerName);
        }
    }

    public boolean contains(final String playerName) {
        return MONITOR_ENABLED.contains(playerName);
    }
}
