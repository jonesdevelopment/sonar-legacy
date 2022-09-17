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
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;

public final class ReloadCommand extends SubCommand {

    public ReloadCommand() {
        super("reload", "Reload all configurations", "sonar.reload");
    }

    private long lastReload = 0L;

    @Override
    public void execute(final CommandExecution execution) {
        final long timeStamp = System.currentTimeMillis();

        if (timeStamp - lastReload < 1500L) {
            execution.send(Messages.Values.PREFIX + "§cPlease wait a bit before reloading Sonar again.");
            return;
        }

        lastReload = timeStamp;

        execution.send(Messages.Values.RELOADING);

        SonarBungee.INSTANCE.createDataFolder();

        Config.initialize();
        Config.Values.load();

        Messages.initialize();
        Messages.Values.load();

        execution.send(Messages.Values.RELOADED
                .replaceAll("%seconds%", String.format("%.2f", (System.currentTimeMillis() - timeStamp) / 1000D)));
    }
}
