package jones.sonar.universal.data.connection;

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@RequiredArgsConstructor
public final class ConnectionData {
    public final InetAddress inetAddress;
    public String username, verifiedName;

    public long lastJoinTimestamp;
    public int checkState, failedReconnectAttempts, underAttackChecks, threatScore;

    // current player + number of all online players with that ip
    public long getAccountsOnlineWithSameIP() {
        return 1 + SonarBungee.INSTANCE.proxy.getPlayers().stream()
                .map(player -> ((InetSocketAddress) player.getSocketAddress()).getAddress().toString())
                .filter(playerInetAddress -> inetAddress.toString().equals(playerInetAddress))
                .count();
    }
}
