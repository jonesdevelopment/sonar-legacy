package jones.sonar.bungee.caching;

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;

@UtilityClass
public class ServerDataProvider {
    public ServerInfo getForcedHost(final ListenerInfo listener, final InetSocketAddress virtualHost) {
        String forced = (virtualHost == null) ? null : listener.getForcedHosts().get(virtualHost.getHostString());

        if (forced == null && listener.isForceDefault()) {
            forced = listener.getDefaultServer();
        }

        return (forced == null) ? null : SonarBungee.INSTANCE.proxy.getServerInfo(forced);
    }
}
