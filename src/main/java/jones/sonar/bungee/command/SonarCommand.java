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

package jones.sonar.bungee.command;

import jones.sonar.SonarBungee;
import jones.sonar.bungee.command.manager.CommandManager;
import jones.sonar.bungee.config.Messages;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class SonarCommand extends Command {

    public SonarCommand() {
        super("sonar", null, "antibot", "ab", "anti-bot");
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("sonar.use")) {
            sender.sendMessage(Messages.Values.NO_PERMISSION);
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Messages.Values.HEADER_BAR);

            CommandManager.SUB_COMMANDS.stream()
                    .filter(command -> !command.name.equals("notify") || sender instanceof ProxiedPlayer)
                    .forEach(command ->
                            sender.sendMessage(Messages.Values.HELP_COMMAND_LAYOUT
                                    .replaceAll("%command%", command.name)
                                    .replaceAll("%description%", command.description)));

            sender.sendMessage(Messages.Values.PREFIX + "§f§oSonar §f§oversion §f§o" + SonarBungee.INSTANCE.VERSION + " §f§oby §f§ojonesdev.xyz§r");
            sender.sendMessage(Messages.Values.FOOTER_BAR);
            return;
        }

        for (final SubCommand command : CommandManager.SUB_COMMANDS) {
            if (sender.hasPermission(command.permission)) {
                final CommandExecution possibleExecution = new CommandExecution(sender, args, command);

                if (args[0].equalsIgnoreCase(command.name)) {
                    command.execute(possibleExecution);
                    return;
                }

                if (command.aliases.length > 0) {
                    for (final String alias : command.aliases) {
                        if (args[0].equalsIgnoreCase(alias)) {
                            command.execute(possibleExecution);
                            return;
                        }
                    }
                }
            }

            else if (args[0].equalsIgnoreCase(command.name)) {
                sender.sendMessage(Messages.Values.NO_PERMISSION_SUB_COMMAND.replaceAll("%permission%", command.permission));
                return;
            }
        }

        sender.sendMessage(Messages.Values.UNKNOWN_SUB_COMMAND);
    }
}
