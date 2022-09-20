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
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;

import java.util.Arrays;

public final class VerifyCommand extends SubCommand {

    public VerifyCommand() {
        super("verify",
                "Verification management",
                "sonar.verify",
                Arrays.asList("size", "clear", "reset", "purge"));
    }

    @Override
    public void execute(final CommandExecution execution) {
        if (execution.arguments.length > 1) {
            switch (execution.arguments[1]) {
                case "reset":
                case "clear": {
                    if (ConnectionDataManager.getVerifying() == 0) {
                        execution.send(Messages.Values.VERIFICATION_EMPTY);
                        return;
                    }

                    final long verifying = ConnectionDataManager.getVerifying();

                    // reset all stages of all verifying players
                    ConnectionDataManager.resetCheckStage(0);

                    final long difference = Math.max(verifying - ConnectionDataManager.getVerifying(), 0);

                    execution.send(Messages.Values.VERIFICATION_CLEAR
                            .replaceAll("%verifying%", SonarBungee.INSTANCE.FORMAT.format(difference)));
                    return;
                }

                case "size": {
                    if (ConnectionDataManager.getVerifying() == 0) {
                        execution.send(Messages.Values.VERIFICATION_EMPTY);
                        return;
                    }

                    execution.send(Messages.Values.VERIFICATION_SIZE
                            .replaceAll("%verifying%", SonarBungee.INSTANCE.FORMAT.format(ConnectionDataManager.getVerifying())));
                    return;
                }

                case "purge": {
                    execution.send(Messages.Values.VERIFICATION_PURGING);

                    if (ConnectionDataManager.getVerifying() > 0) {

                        // remove all blacklisted but still existing players
                        ConnectionDataManager.removeAllUnused();

                        // reset all stages of all verifying players to 1 to
                        // force another reconnect when verifying during an attack
                        ConnectionDataManager.resetCheckStage(1);

                        execution.send(Messages.Values.VERIFICATION_PURGE_COMPLETE);
                    } else {
                        execution.send(Messages.Values.VERIFICATION_PURGE_NONE);
                    }
                    return;
                }
            }
        }

        execution.sendUsage("/ab verify <clear|size|purge>");
    }
}
