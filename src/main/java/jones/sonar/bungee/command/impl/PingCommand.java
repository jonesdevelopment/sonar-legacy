package jones.sonar.bungee.command.impl;

import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import jones.sonar.universal.platform.bungee.SonarBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class PingCommand extends SubCommand {

    public PingCommand() {
        super("ping",
                "Show a players latency",
                "sonar.use",
                null);
    }

    @Override
    public void execute(final CommandExecution execution) {
        try {
            if (execution.arguments.length == 1) {
                if (!(execution.commandSender instanceof ProxiedPlayer)) {
                    execution.commandSender.sendMessage(Messages.Values.PING_SPECIFY);
                    return;
                }

                final ProxiedPlayer player = (ProxiedPlayer) execution.commandSender;

                final String ping = SonarBungee.INSTANCE.FORMAT.format(player.getPing());

                execution.commandSender.sendMessage(Messages.Values.PING.replaceAll("%ping%", ping));
            } else {
                try {
                    final ProxiedPlayer target = SonarBungee.INSTANCE.proxy.getPlayer(execution.arguments[1]);

                    final String ping = SonarBungee.INSTANCE.FORMAT.format(target.getPing());

                    execution.commandSender.sendMessage(Messages.Values.PING_OTHER.replaceAll("%ping%", ping).replaceAll("%player%", target.getName()));
                } catch (Exception exception) {
                    execution.commandSender.sendMessage(Messages.Values.PREFIX + "§cUsage: §c/ab §cping §c[player]");
                }
            }
        } catch (Exception ignored) {
            execution.commandSender.sendMessage(Messages.Values.PREFIX + "§cUsage: §c/ab §cping §c[player]");
        }
    }
}
