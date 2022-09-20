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
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        super("reload",
                "Reload all configurations",
                "sonar.reload",
                null);
    }

    private long lastReload = 0L;

    @Override
    public void execute(final CommandExecution execution) {

        // only if the executor is a player
        if (execution.commandSender instanceof ProxiedPlayer) {
            if (execution.arguments.length > 1) {
                if (!execution.arguments[1].equalsIgnoreCase("confirm")) {
                    execution.sendUsage("/ab reload [confirm]");
                    return;
                }
            }

            // the server is under attack, we need to confirm reload!
            else if (Sensibility.isUnderAttack()) {
                execution.send(Messages.Values.RELOAD_CONFIRMATION_ATTACK);
                return;
            }
        }

        final long timeStamp = System.currentTimeMillis();

        // we don't want people to spam reload
        if (timeStamp - lastReload <= 1500L) {
            execution.send(Messages.Values.RELOAD_WAIT);
            return;
        }

        lastReload = timeStamp;

        execution.send(Messages.Values.RELOADING
                .replaceAll("%version%", SonarBungee.INSTANCE.VERSION));

        final long timeTaken = SonarBungee.INSTANCE.reload();

        execution.send(Messages.Values.RELOADED
                .replaceAll("%seconds%", String.format("%.3f", timeTaken / 1000D))
                .replaceAll("%version%", SonarBungee.INSTANCE.VERSION));
    }
}
