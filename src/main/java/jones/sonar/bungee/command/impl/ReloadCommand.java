package jones.sonar.bungee.command.impl;

import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.Sensibility;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        super("reload",
                "Reload all configurations",
                "sonar.reload",
                null);
    }

    private long lastReload = 0L;

    @Override
    public void execute(final CommandExecution execution) {

        // only if the executor is a player
        if (execution.commandSender instanceof ProxiedPlayer) {
            if (execution.arguments.length > 1) {
                if (!execution.arguments[1].equalsIgnoreCase("confirm")) {
                    execution.sendUsage("/ab reload [confirm]");
                    return;
                }
            }

            // the server is under attack, we need to confirm reload!
            else if (Sensibility.isUnderAttack()) {
                execution.send(Messages.Values.RELOAD_CONFIRMATION_ATTACK);
                return;
            }
        }

        final long timeStamp = System.currentTimeMillis();

        // we don't want people to spam reload
        if (timeStamp - lastReload <= 1500L) {
            execution.send(Messages.Values.RELOAD_WAIT);
            return;
        }

        lastReload = timeStamp;

        execution.send(Messages.Values.RELOADING
                .replaceAll("%version%", SonarBungee.INSTANCE.getVersion()));

        final long timeTaken = SonarBungee.INSTANCE.reload();

        execution.send(Messages.Values.RELOADED
                .replaceAll("%seconds%", String.format("%.3f", timeTaken / 1000D))
                .replaceAll("%version%", SonarBungee.INSTANCE.getVersion()));
    }
}
