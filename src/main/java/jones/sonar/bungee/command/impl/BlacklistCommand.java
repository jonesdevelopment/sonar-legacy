package jones.sonar.bungee.command.impl;

import jones.sonar.api.event.bungee.SonarBlacklistClearEvent;
import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Firewall;
import jones.sonar.bungee.config.Messages;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.firewall.FirewallManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.Sensibility;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

public final class BlacklistCommand extends SubCommand {

    public BlacklistCommand() {
        super("blacklist",
                "Blacklist management",
                "sonar.blacklist",
                Arrays.asList("size", "clear", "reset", "forceclear", "forcereset", "add", "remove"));
    }

    @Override
    public void execute(final CommandExecution execution) {
        if (execution.arguments.length > 1) {
            switch (execution.arguments[1].toLowerCase()) {
                case "size": {
                    final long blacklisted = Blacklist.size();

                    if (blacklisted > 0) {
                        execution.send(Messages.Values.BLACKLIST_SIZE
                                .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(blacklisted))
                                .replaceAll("%es%", blacklisted == 1 ? "" : "es"));
                    } else {
                        execution.send(Messages.Values.BLACKLIST_EMPTY);
                    }
                    return;
                }

                case "forcereset":
                case "reset":
                case "forceclear":
                case "clear": {
                    if (Sensibility.isUnderAttack() && !execution.arguments[1].toLowerCase().contains("force")) {
                        execution.send(Messages.Values.BLACKLIST_CLEAR_ATTACK);
                        return;
                    }

                    final long blacklisted = Blacklist.size();

                    if (Firewall.Values.ENABLE_FIREWALL) {
                        FirewallManager.clear();
                    }

                    if (blacklisted > 0) {

                        // clear all blacklisted ip addresses
                        Blacklist.BLACKLISTED.clear();

                        SonarBungee.INSTANCE.callEvent(new SonarBlacklistClearEvent(blacklisted));

                        // reset checked stage to 2 to prevent exploits
                        ConnectionDataManager.resetCheckStage(2);

                        execution.send(Messages.Values.BLACKLIST_CLEAR
                                .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(blacklisted))
                                .replaceAll("%es%", blacklisted == 1 ? "" : "es"));
                    } else {
                        execution.send(Messages.Values.BLACKLIST_EMPTY);
                    }
                    return;
                }
            }

            if (execution.arguments[1].equalsIgnoreCase("add")) {
                try {
                    if (!execution.arguments[2].matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([.,])){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")) {
                        try {
                            final ProxiedPlayer target = SonarBungee.INSTANCE.proxy.getPlayer(execution.arguments[2]);

                            final InetAddress inetAddress = ((InetSocketAddress) target.getSocketAddress()).getAddress();

                            if (Blacklist.isBlacklisted(inetAddress)) {
                                execution.send(Messages.Values.BLACKLIST_ALREADY);
                            } else {
                                Blacklist.addToBlacklist(inetAddress);

                                execution.send(Messages.Values.BLACKLIST_ADD_PLAYER
                                        .replaceAll("%player%", target.getName())
                                        .replaceAll("%ip%", inetAddress.toString().replaceAll("/", "")));

                                target.disconnect("Disconnected");
                            }
                        } catch (Exception exception) {
                            execution.send(Messages.Values.BLACKLIST_INVALID_IP);
                        }
                        return;
                    }

                    final InetAddress inetAddress = InetAddress.getByName(execution.arguments[2]);

                    if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
                        execution.send(Messages.Values.BLACKLIST_INVALID_IP);
                        return;
                    }

                    if (Blacklist.isBlacklisted(inetAddress)) {
                        execution.send(Messages.Values.BLACKLIST_ALREADY);
                    } else {
                        Blacklist.addToBlacklist(inetAddress);

                        execution.send(Messages.Values.BLACKLIST_ADD_IP
                                .replaceAll("%ip%", inetAddress.toString().replaceAll("/", "")));
                    }
                } catch (Exception exception) {
                    execution.sendUsage("/ab blacklist add <ip|username>");
                }
                return;
            }

            else if (execution.arguments[1].equalsIgnoreCase("remove")) {
                try {
                    if (!execution.arguments[2].matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([.,])){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")) {
                        execution.send(Messages.Values.BLACKLIST_INVALID_IP);
                        return;
                    }

                    final InetAddress inetAddress = InetAddress.getByName(execution.arguments[2]);

                    if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
                        execution.send(Messages.Values.BLACKLIST_INVALID_IP);
                        return;
                    }

                    if (Blacklist.isBlacklisted(inetAddress)) {
                        Blacklist.removeFromBlacklist(inetAddress);

                        execution.send(Messages.Values.BLACKLIST_REMOVE
                                .replaceAll("%ip%", inetAddress.toString().replaceAll("/", "")));
                    } else {
                        execution.send(Messages.Values.BLACKLIST_NOT);
                    }
                } catch (Exception exception) {
                    execution.sendUsage("/ab blacklist remove <ip>");
                }
                return;
            }
        }

        execution.sendUsage("/ab blacklist <size|clear|remove|add> [ip|username]");
    }
}
