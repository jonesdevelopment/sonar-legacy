package jones.sonar.bungee.command.impl;

import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.monitor.MonitorManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class MonitorCommand extends SubCommand {

    public MonitorCommand() {
        super("monitor",
                "Toggle boss-bar verbose",
                "sonar.monitor",
                null);
    }

    @Override
    public void execute(final CommandExecution execution) {
        if (execution.arguments.length > 1) {
            try {
                final ProxiedPlayer target = SonarBungee.INSTANCE.proxy.getPlayer(execution.arguments[1]);

                if (target.getPendingConnection().getVersion() <= 47) {
                    execution.send(Messages.Values.MONITOR_UNSUPPORTED_OTHER
                            .replaceAll("%player%", target.getName()));
                    return;
                }

                if (MonitorManager.toggle(target)) {
                    execution.send(Messages.Values.MONITOR_ENABLED_OTHER.replaceAll("%player%", target.getName()));

                    target.sendMessage(Messages.Values.MONITOR_ENABLED);
                } else {
                    execution.send(Messages.Values.MONITOR_DISABLED_OTHER.replaceAll("%player%", target.getName()));

                    target.sendMessage(Messages.Values.MONITOR_DISABLED);
                }
            } catch (Exception exception) {
                execution.sendUsage("/ab monitor [player]");
            }
            return;
        }

        if (execution.commandSender instanceof ProxiedPlayer) {
            final ProxiedPlayer player = (ProxiedPlayer) execution.commandSender;

            if (player.getPendingConnection().getVersion() <= 47) {
                execution.send(Messages.Values.MONITOR_UNSUPPORTED);
                return;
            }

            if (MonitorManager.toggle(player)) {
                execution.send(Messages.Values.MONITOR_ENABLED);
            } else {
                execution.send(Messages.Values.MONITOR_DISABLED);
            }
        } else {
            execution.send(Messages.Values.ONLY_PLAYERS);
        }
    }
}
