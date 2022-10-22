package jones.sonar.bungee.command.impl;

import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.notification.counter.ActionBarManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class VerboseCommand extends SubCommand {

    public VerboseCommand() {
        super("verbose",
                "Toggle action-bar verbose",
                "sonar.verbose",
                null);
    }

    @Override
    public void execute(final CommandExecution execution) {
        if (execution.arguments.length > 1) {
            try {
                final ProxiedPlayer target = SonarBungee.INSTANCE.proxy.getPlayer(execution.arguments[1]);

                if (ActionBarManager.toggle(target)) {
                    execution.send(Messages.Values.COUNTER_ENABLED_OTHER.replaceAll("%player%", target.getName()));

                    target.sendMessage(Messages.Values.COUNTER_ENABLED);
                } else {
                    execution.send(Messages.Values.COUNTER_DISABLED_OTHER.replaceAll("%player%", target.getName()));

                    target.sendMessage(Messages.Values.COUNTER_DISABLED);
                    target.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(" "));
                }
            } catch (Exception exception) {
                execution.sendUsage("/ab verbose [player]");
            }
            return;
        }

        if (execution.commandSender instanceof ProxiedPlayer) {
            if (ActionBarManager.toggle((ProxiedPlayer) execution.commandSender)) {
                execution.send(Messages.Values.COUNTER_ENABLED);
            } else {
                execution.send(Messages.Values.COUNTER_DISABLED);
                ((ProxiedPlayer) execution.commandSender).sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(" "));
            }
        } else {
            execution.send(Messages.Values.ONLY_PLAYERS);
        }
    }
}
