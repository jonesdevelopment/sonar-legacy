package jones.sonar.api;

import com.google.gson.annotations.SerializedName;
import jones.sonar.api.data.BotLevel;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.connection.ConnectionData;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.util.Sensibility;
import jones.sonar.universal.whitelist.Whitelist;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;

@APIClass(since = "1.3.1", lastUpdate = "1.3.2")
interface SonarAPIImpl {

    default boolean isUnderAttack() {
        return Sensibility.isUnderAttack();
    }

    @SerializedName("BotLevel")
    default BotLevel getPlayerBotLevel(final InetAddress inetAddress) {
        final ConnectionData data = ConnectionDataManager.get(inetAddress);

        if (data != null) {
            return new BotLevel(data.getAccountsOnlineWithSameIP(), data.threatScore, data.inetAddress);
        }

        return null;
    }

    @SerializedName("BotLevel")
    default BotLevel getPlayerBotLevel(final SocketAddress inetAddress) {
        return getPlayerBotLevel(((InetSocketAddress) inetAddress).getAddress());
    }

    @SerializedName("BotLevel")
    default BotLevel getPlayerBotLevel(final ProxiedPlayer proxiedPlayer) {
        return getPlayerBotLevel(((InetSocketAddress) proxiedPlayer.getPendingConnection().getSocketAddress()).getAddress());
    }

    default boolean isBlacklisted(final InetAddress inetAddress) {
        return Blacklist.isBlacklisted(inetAddress);
    }

    default boolean isBlacklisted(final SocketAddress inetAddress) {
        return isBlacklisted(((InetSocketAddress) inetAddress).getAddress());
    }

    default boolean isWhitelisted(final InetAddress inetAddress) {
        return Whitelist.isWhitelisted(inetAddress);
    }

    default boolean isWhitelisted(final SocketAddress inetAddress) {
        return isWhitelisted(((InetSocketAddress) inetAddress).getAddress());
    }

    default boolean isWhitelisted(final ProxiedPlayer proxiedPlayer) {
        return isWhitelisted(((InetSocketAddress) proxiedPlayer.getPendingConnection().getSocketAddress()).getAddress());
    }

    default void addToBlacklist(final InetAddress inetAddress) {
        Blacklist.addToBlacklist(inetAddress);
    }

    default void addToBlacklist(final SocketAddress inetAddress) {
        addToBlacklist(((InetSocketAddress) inetAddress).getAddress());
    }

    default void addToWhitelist(final InetAddress inetAddress) {
        Whitelist.addToWhitelist(inetAddress);
    }

    default void addToWhitelist(final SocketAddress inetAddress) {
        addToWhitelist(((InetSocketAddress) inetAddress).getAddress());
    }

    default Collection<InetAddress> getBlacklistedIPAddresses() {
        return Blacklist.BLACKLISTED.asMap().keySet();
    }

    default Collection<InetAddress> getWhitelistedIPAddresses() {
        return Whitelist.WHITELISTED;
    }
}
