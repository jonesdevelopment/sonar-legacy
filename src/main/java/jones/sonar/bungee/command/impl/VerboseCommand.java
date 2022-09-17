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
import jones.sonar.bungee.counter.ActionBarManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class VerboseCommand extends SubCommand {

    public VerboseCommand() {
        super("verbose", "Toggle action-bar verbose", "sonar.verbose");
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
