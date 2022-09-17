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

import jones.sonar.bungee.SonarBungee;
import jones.sonar.bungee.command.CommandExecution;
import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.config.Messages;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class PingCommand extends SubCommand {

    public PingCommand() {
        super("ping", "Show a players latency", "sonar.use");
    }

    @Override
    public void execute(final CommandExecution execution) {
        try {
            if (execution.arguments.length == 1) {
                if (!(execution.commandSender instanceof ProxiedPlayer)) {
                    execution.commandSender.sendMessage(Messages.Values.PING_SPECIFY);
                    return;
                }

                final ProxiedPlayer player = (ProxiedPlayer) execution.commandSender;

                final String ping = SonarBungee.INSTANCE.FORMAT.format(player.getPing());

                execution.commandSender.sendMessage(Messages.Values.PING.replaceAll("%ping%", ping));
            } else {
                try {
                    final ProxiedPlayer target = SonarBungee.INSTANCE.proxy.getPlayer(execution.arguments[1]);

                    final String ping = SonarBungee.INSTANCE.FORMAT.format(target.getPing());

                    execution.commandSender.sendMessage(Messages.Values.PING_OTHER.replaceAll("%ping%", ping).replaceAll("%player%", target.getName()));
                } catch (Exception exception) {
                    execution.commandSender.sendMessage(Messages.Values.PREFIX + "§cUsage: §c/ab §cping §c[player]");
                }
            }
        } catch (Exception ignored) {
            execution.commandSender.sendMessage(Messages.Values.PREFIX + "§cUsage: §c/ab §cping §c[player]");
        }
    }
}
