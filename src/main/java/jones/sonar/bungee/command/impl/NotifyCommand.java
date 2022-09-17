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
import jones.sonar.bungee.caching.notifications.NotificationManager;
import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class NotifyCommand extends SubCommand {

    public NotifyCommand() {
        super("notify", "Toggle chat notifications", "sonar.notify");
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
