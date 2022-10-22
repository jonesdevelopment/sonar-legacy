package jones.sonar.bungee.command.impl;

import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.notification.NotificationManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class NotifyCommand extends SubCommand {

    public NotifyCommand() {
        super("notify",
                "Toggle chat notifications",
                "sonar.notify",
                null);
    }

    @Override
    public void execute(final CommandExecution execution) {
        if (execution.arguments.length > 1) {
            try {
                final ProxiedPlayer target = SonarBungee.INSTANCE.proxy.getPlayer(execution.arguments[1]);

                if (NotificationManager.toggle(target)) {
                    execution.send(Messages.Values.NOTIFY_ENABLED_OTHER.replaceAll("%player%", target.getName()));

                    target.sendMessage(Messages.Values.NOTIFY_ENABLED);
                } else {
                    execution.send(Messages.Values.NOTIFY_DISABLED_OTHER.replaceAll("%player%", target.getName()));

                    target.sendMessage(Messages.Values.NOTIFY_DISABLED);
                }
            } catch (Exception exception) {
                execution.sendUsage("/ab notify [player]");
            }
            return;
        }

        if (execution.commandSender instanceof ProxiedPlayer) {
            if (NotificationManager.toggle((ProxiedPlayer) execution.commandSender)) {
                execution.send(Messages.Values.NOTIFY_ENABLED);
            } else {
                execution.send(Messages.Values.NOTIFY_DISABLED);
            }
        } else {
            execution.send(Messages.Values.ONLY_PLAYERS);
        }
    }
}
