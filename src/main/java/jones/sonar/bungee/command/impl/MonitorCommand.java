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
                    target.sendMessage(Messages.Values.MONITOR_UNSUPPORTED_OTHER
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
