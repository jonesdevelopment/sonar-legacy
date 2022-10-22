package jones.sonar.bungee.command;

import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.util.ColorUtil;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.CommandSender;

@RequiredArgsConstructor
public final class CommandExecution {
    public final CommandSender commandSender;
    public final String[] arguments;
    public final SubCommand subCommand;

    public void sendUsage(final String syntax) {
        commandSender.sendMessage(Messages.Values.COMMAND_USAGE.replaceAll("%command%", syntax));
    }

    public void send(final String message) {
        commandSender.sendMessage(ColorUtil.format(message));
    }

    public void sendPrefixed(final String message) {
        commandSender.sendMessage(Messages.Values.PREFIX + ColorUtil.format(message));
    }
}
