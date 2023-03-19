package jones.sonar.bungee.command.impl;

import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.ServerStatistics;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.whitelist.Whitelist;

public final class StatsCommand extends SubCommand {

    public StatsCommand() {
        super("stats",
                "Show server statistics",
                "sonar.stats",
                null);
    }

    @Override
    public void execute(final CommandExecution execution) {
        execution.send(Messages.Values.HEADER_BAR);

        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fConnections per second: " + SonarBungee.INSTANCE.FORMAT.format(Counter.CONNECTIONS_PER_SECOND.get()));
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fIp addresses per second: " + SonarBungee.INSTANCE.FORMAT.format(Counter.IPS_PER_SECOND.get()));
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fMoTDs/Pings per second: " + SonarBungee.INSTANCE.FORMAT.format(Counter.PINGS_PER_SECOND.get()));
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fJoins/Logins per second: " + SonarBungee.INSTANCE.FORMAT.format(Counter.JOINS_PER_SECOND.get()));
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fVerifying players (right now): " + SonarBungee.INSTANCE.FORMAT.format(ConnectionDataManager.DATA.values().stream()
                .filter(connectionData -> connectionData.checkState <= 1)
                .count()));
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fTotal connections (session): " + SonarBungee.INSTANCE.FORMAT.format(ServerStatistics.TOTAL_CONNECTIONS));
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fTotal aborted connections (session): " + SonarBungee.INSTANCE.FORMAT.format(ServerStatistics.BLOCKED_CONNECTIONS));
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fBlacklisted ip addresses (session): " + SonarBungee.INSTANCE.FORMAT.format(Blacklist.size()));
        execution.send("§7 " + Messages.Values.LIST_SYMBOL + " §fWhitelisted ip addresses (session): " + SonarBungee.INSTANCE.FORMAT.format(Whitelist.size()));

        execution.send(Messages.Values.FOOTER_BAR);
    }
}
