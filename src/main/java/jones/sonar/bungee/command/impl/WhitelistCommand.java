/*
 *  Copyright (c) 2022, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.bungee.command.impl;

import jones.sonar.SonarBungee;
import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.whitelist.Whitelist;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public final class WhitelistCommand extends SubCommand {

    public WhitelistCommand() {
        super("whitelist", "Whitelist management", "sonar.whitelist");
    }

    @Override
    public void execute(final CommandExecution execution) {
        if (execution.arguments.length > 1) {
            switch (execution.arguments[1].toLowerCase()) {
                case "size": {
                    final long whitelisted = Whitelist.size();

                    if (whitelisted > 0) {
                        execution.send(Messages.Values.WHITELIST_SIZE
                                .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(whitelisted))
                                .replaceAll("%es%", whitelisted == 1 ? "" : "es"));
                    } else {
                        execution.send(Messages.Values.WHITELIST_EMPTY);
                    }
                    return;
                }

                case "forcereset":
                case "reset":
                case "forceclear":
                case "clear": {
                    final long whitelisted = Whitelist.size();

                    if (whitelisted > 0) {

                        // clear all whitelisted ip addresses
                        Whitelist.WHITELISTED.clear();

                        // reset checked stage to 2 to prevent exploits
                        ConnectionDataManager.resetCheckStage(2);

                        execution.send(Messages.Values.WHITELIST_CLEAR
                                .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(whitelisted))
                                .replaceAll("%es%", whitelisted == 1 ? "" : "es"));
                    } else {
                        execution.send(Messages.Values.WHITELIST_EMPTY);
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

                            if (Whitelist.isWhitelisted(inetAddress)) {
                                execution.send(Messages.Values.WHITELIST_ALREADY);
                            } else {
                                Whitelist.addToWhitelist(inetAddress);

                                execution.send(Messages.Values.WHITELIST_ADD_PLAYER
                                        .replaceAll("%player%", target.getName())
                                        .replaceAll("%ip%", inetAddress.toString().replaceAll("/", "")));
                            }
                        } catch (Exception exception) {
                            execution.send(Messages.Values.WHITELIST_INVALID_IP);
                        }
                        return;
                    }

                    final InetAddress inetAddress = InetAddress.getByName(execution.arguments[2]);

                    if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
                        execution.send(Messages.Values.WHITELIST_INVALID_IP);
                        return;
                    }

                    if (Whitelist.isWhitelisted(inetAddress)) {
                        execution.send(Messages.Values.WHITELIST_ALREADY);
                    } else {
                        Whitelist.addToWhitelist(inetAddress);

                        execution.send(Messages.Values.WHITELIST_ADD_IP
                                .replaceAll("%ip%", inetAddress.toString().replaceAll("/", "")));
                    }
                } catch (Exception exception) {
                    execution.sendUsage("/ab whitelist add <ip|username>");
                }
                return;
            }

            else if (execution.arguments[1].equalsIgnoreCase("remove")) {
                try {
                    if (!execution.arguments[2].matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])([.,])){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")) {
                        execution.send(Messages.Values.WHITELIST_INVALID_IP);
                        return;
                    }

                    final InetAddress inetAddress = InetAddress.getByName(execution.arguments[2]);

                    if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
                        execution.send(Messages.Values.WHITELIST_INVALID_IP);
                        return;
                    }

                    if (Whitelist.isWhitelisted(inetAddress)) {
                        Whitelist.removeFromWhitelist(inetAddress);

                        execution.send(Messages.Values.WHITELIST_REMOVE
                                .replaceAll("%ip%", inetAddress.toString().replaceAll("/", "")));
                    } else {
                        execution.send(Messages.Values.WHITELIST_NOT);
                    }
                } catch (Exception exception) {
                    execution.sendUsage("/ab whitelist remove <ip>");
                }
                return;
            }
        }

        execution.sendUsage("/ab whitelist <size|clear|remove|add> [ip|username]");
    }
}
