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

public final class BlacklistCommand extends SubCommand {

    public BlacklistCommand() {
        super("blacklist", "Blacklist management", "sonar.blacklist");
    }

    @Override
    public void execute(final CommandExecution execution) {
        if (execution.arguments.length <= 1) {
            execution.send("&cUsage: &7/ab blacklist <size|clear|remove|add> [ip|username]");
        }
    }
}
