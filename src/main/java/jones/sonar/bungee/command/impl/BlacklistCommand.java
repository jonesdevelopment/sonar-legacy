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
import jones.sonar.bungee.util.Sensibility;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;

public final class BlacklistCommand extends SubCommand {

    public BlacklistCommand() {
        super("blacklist", "Blacklist management", "sonar.blacklist");
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

                    // clear all blacklisted ip addresses
                    Blacklist.BLACKLISTED.clear();

                    // reset checked stage to 2 to prevent exploits
                    ConnectionDataManager.DATA.values()
                            .forEach(data -> data.checked = 2);

                    execution.send(Messages.Values.BLACKLIST_CLEAR
                            .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(blacklisted))
                            .replaceAll("%es%", blacklisted == 1 ? "" : "es"));
                    return;
                }
            }

            if (execution.arguments[1].equalsIgnoreCase("add")) {

            }

            if (execution.arguments[1].equalsIgnoreCase("remove")) {

            }
        } else {
            execution.sendUsage("/ab blacklist <size|clear|remove|add> [ip|username]");
        }
    }
}
