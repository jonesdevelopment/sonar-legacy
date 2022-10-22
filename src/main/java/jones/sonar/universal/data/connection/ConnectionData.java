package jones.sonar.universal.data.connection;

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public final class ConnectionData {
    public final InetAddress inetAddress;

    public String username = "", verifiedName = "";

    public final Set<String> verifiedNames = new HashSet<>(50),
            allowedNames = new HashSet<>(50);

    public long lastJoin = 0L;

    public int checked = 0, failedReconnect = 0,
            underAttackChecks = 0, botLevel = 0;

    // current player + number of all online players with that ip
    public long getAccountsOnlineWithSameIP() {
        return 1 + SonarBungee.INSTANCE.proxy.getPlayers().stream()
                .filter(player -> inetAddress.toString().equals((((InetSocketAddress) player.getSocketAddress()).getAddress().toString())))
                .count();
    }
}
