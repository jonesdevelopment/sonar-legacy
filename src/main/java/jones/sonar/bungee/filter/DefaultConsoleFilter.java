package jones.sonar.bungee.filter;

import jones.sonar.bungee.config.Config;
import jones.sonar.universal.platform.bungee.SonarBungee;

public final class DefaultConsoleFilter {
    DefaultConsoleFilter() {
        SonarBungee.INSTANCE.proxy.getLogger().setFilter(record -> {
            final String message = SonarBungee.INSTANCE.proxy.getName().equals("BungeeCord") ? (new ConciseFormatter(true))
                    .formatMessage(record)
                    .trim() : record.getMessage();

            return ((!(record.getSourceClassName().equals("net.md_5.bungee.connection.InitialHandler")
                    || (record.getSourceClassName().equals("net.md_5.bungee.log.BungeeLogger") && message.contains(" InitialHandler ")))
                    && !(record.getSourceClassName().equals("net.md_5.bungee.netty.HandlerBoss")
                    && message.contains(" - encountered exception: ")))
                    || Config.Values.LOG_CONNECTIONS)
                    && !message.equals("No client connected for pending server!");
        });
    }
}
