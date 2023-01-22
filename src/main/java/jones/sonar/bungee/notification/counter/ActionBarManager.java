package jones.sonar.bungee.notification.counter;

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

@UtilityClass
public class ActionBarManager {
    private final Set<String> VERBOSE_ENABLED = new CopyOnWriteArraySet<>();

    public Stream<ProxiedPlayer> getPlayers() {
        return VERBOSE_ENABLED.stream()
                .map(SonarBungee.INSTANCE.proxy::getPlayer)
                .filter(Objects::nonNull)
                .filter(player -> player.hasPermission("sonar.verbose"));
    }

    public Collection<String> getVerboseEnabled() {
        return VERBOSE_ENABLED;
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
            VERBOSE_ENABLED.add(playerName);
        }
    }

    public void remove(final String playerName) {
        if (contains(playerName)) {
            VERBOSE_ENABLED.remove(playerName);
        }
    }

    public boolean contains(final String playerName) {
        return VERBOSE_ENABLED.contains(playerName);
    }
}
