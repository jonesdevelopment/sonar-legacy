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
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public final class BlacklistCommand extends SubCommand {

    public BlacklistCommand() {
        super("blacklist",
                "Blacklist management",
                "sonar.blacklist",
                Arrays.asList("size", "clear", "reset", "forceclear", "forcereset", "add", "remove", "save"));
    }

    private static volatile long lastBlacklistSave = -1L;

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

                case "save": {
                    if (Blacklist.size() == 0) {
                        execution.send(Messages.Values.BLACKLIST_EMPTY);
                        return;
                    }

                    final String fileName = "blacklist_save_"
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            + "_"
                            + System.currentTimeMillis()
                            + ".txt";
                    final File file = new File(SonarBungee.INSTANCE.getPlugin().getDataFolder(), fileName);

                    if (file.exists() && !file.delete()) {
                        execution.send(Messages.Values.PREFIX + "§cAn error occurred, please check console.");
                        ProxyServer.getInstance().getLogger().warning("Could not delete old log file: " + fileName);
                        return;
                    }

                    try {
                        if (!file.createNewFile()) {
                            execution.send(Messages.Values.PREFIX + "§cAn error occurred, please check console.");
                            ProxyServer.getInstance().getLogger().warning("Could not create new log file [no permission?]: " + fileName);
                            return;
                        }

                        // ===========================================================
                        final long timeStamp = System.currentTimeMillis();

                        if (timeStamp - lastBlacklistSave < 1000L) {
                            execution.send(Messages.Values.PREFIX + "§cPlease wait a bit before saving the blacklist again.");
                            return;
                        }

                        lastBlacklistSave = timeStamp;
                        // ===========================================================

                        try (final FileWriter fileWriter = new FileWriter(file)) {
                            Blacklist.BLACKLISTED.asMap().keySet().stream()
                                    .map(inetAddress -> inetAddress.toString().replace("/", ""))
                                    .forEach(string -> {
                                        try {
                                            fileWriter.append(string).append("\n");
                                        } catch (Exception exception) {
                                            exception.printStackTrace();
                                            execution.send(Messages.Values.PREFIX + "§cAn error occurred while writing §f" + string);
                                        }
                                    });
                        }

                        execution.send(Messages.Values.PREFIX + "§aSuccessfully saved blacklisted ips to §f" + fileName + "§a.");
                    } catch (IOException exception) {
                        execution.send(Messages.Values.PREFIX + "§cAn error occurred, please check console.");
                        exception.printStackTrace();
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
                        Blacklist.BLACKLISTED.invalidateAll();
                        Blacklist.TEMP_BLACKLISTED.invalidateAll();

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

        execution.sendUsage("/ab blacklist <size|clear|remove|add|save> [ip|username]");
    }
}
