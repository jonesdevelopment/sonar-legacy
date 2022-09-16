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

package jones.sonar.command;

import jones.sonar.command.manager.CommandManager;
import jones.sonar.config.Config;
import jones.sonar.config.Messages;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public final class SonarCommand extends Command {

    public SonarCommand() {
        super("sonar", null, "antibot", "ab", "anti-bot");
    }

    @Override
    public void execute(final CommandSender sender, final String[] args) {
        if (sender instanceof ProxiedPlayer && Config.Values.ENABLE_MAIN_PERMISSION) {
            if (!sender.hasPermission(Config.Values.MAIN_PERMISSION)) {
                sender.sendMessage(Messages.Values.PERMISSION_MESSAGE);
                return;
            }
        }

        if (args.length == 0) {
            printHelp(sender);
            return;
        }

        for (final SubCommand command : CommandManager.SUB_COMMANDS) {
            if (!command.isEnablePermission()
                    || sender.hasPermission(command.getPermission())) {
                if (args[0].equalsIgnoreCase(command.getName())) {
                    command.execute(new CommandExecution(sender, args, command));
                    return;
                }

                if (command.getAliases().length > 0) {
                    for (final String alias : command.getAliases()) {
                        if (args[0].equalsIgnoreCase(alias)) {
                            command.execute(new CommandExecution(sender, args, command));
                            return;
                        }
                    }
                }
            }

            else if (args[0].equalsIgnoreCase(command.getName())) {
                sender.sendMessage(Messages.Values.PERMISSION_MESSAGE);
                return;
            }
        }

        printHelp(sender);
    }

    private void printHelp(final CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            sender.sendMessage(Messages.Values.HELP_COMMAND_BAR);

            CommandManager.getCommands().forEach(command -> {
                if (!command.isEnablePermission()
                        || sender.hasPermission(command.getPermission())) {
                    sender.sendMessage(Messages.Values.HELP_COMMAND_LAYOUT
                            .replaceAll("%command%", command.getName())
                            .replaceAll("%description%", command.getDescription()));
                }
            });
        } else {
            sender.sendMessage(Messages.Values.HELP_COMMAND_BAR);

            CommandManager.getCommands().stream().filter(command -> !command.getName().contains("notify")).forEach(command ->
                    sender.sendMessage(Messages.Values.HELP_COMMAND_LAYOUT
                            .replaceAll("%command%", command.getName())
                            .replaceAll("%description%", command.getDescription())));
        }

        sender.sendMessage(Messages.Values.PREFIX + "§f§oSonar §f§oversion §f§o" + Sonar.INSTANCE.getVersion() + " §f§oby §f§ojonesdev.xyz§r");
        sender.sendMessage(Messages.Values.HELP_COMMAND_BAR);
    }
}
