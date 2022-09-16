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

package jones.sonar.bungee.command.manager;

import jones.sonar.bungee.command.SubCommand;
import jones.sonar.bungee.command.impl.*;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class CommandManager {
    public final List<SubCommand> SUB_COMMANDS = new ArrayList<>();

    public void initialize() {
        addCommands(new InfoCommand(), new StatsCommand(), new ReloadCommand(), new BlacklistCommand(), new WhitelistCommand());
    }

    private void addCommands(final SubCommand... command) {
        SUB_COMMANDS.addAll(Arrays.asList(command));
    }
}
