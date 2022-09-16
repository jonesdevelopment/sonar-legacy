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

package jones.sonar.command.impl;

import jones.sonar.SonarBungee;
import jones.sonar.blacklist.Blacklist;
import jones.sonar.command.CommandExecution;
import jones.sonar.command.SubCommand;
import jones.sonar.config.Messages;
import jones.sonar.counter.Counter;
import jones.sonar.data.ServerStatistics;
import jones.sonar.data.connection.manager.ConnectionDataManager;
import jones.sonar.whitelist.Whitelist;

public final class StatsCommand extends SubCommand {

    public StatsCommand() {
        super("stats", "Show server statistics", "sonar.stats");
    }

    @Override
    public void execute(final CommandExecution execution) {
        execution.sender.sendMessage(Messages.Values.HEADER_BAR);

        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fConnections per second: " + SonarBungee.INSTANCE.FORMAT.format(Counter.CONNECTIONS_PER_SECOND.get()));
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fIp addresses per second: " + SonarBungee.INSTANCE.FORMAT.format(Counter.IPS_PER_SECOND.get()));
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fMoTDs/Pings per second: " + SonarBungee.INSTANCE.FORMAT.format(Counter.PINGS_PER_SECOND.get()));
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fJoins/Logins per second: " + SonarBungee.INSTANCE.FORMAT.format(Counter.JOINS_PER_SECOND.get()));
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fVerifying players (right now): " + SonarBungee.INSTANCE.FORMAT.format(ConnectionDataManager.DATA.values().stream()
                .filter(connectionData -> connectionData.checked <= 1)
                .count()));
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fTotal connections (session): " + SonarBungee.INSTANCE.FORMAT.format(ServerStatistics.TOTAL_CONNECTIONS));
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fTotal aborted connections (session): " + SonarBungee.INSTANCE.FORMAT.format(ServerStatistics.BLOCKED_CONNECTIONS));
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fBlacklisted ip addresses (session): " + SonarBungee.INSTANCE.FORMAT.format(Blacklist.size()));
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fWhitelisted ip addresses (session): " + SonarBungee.INSTANCE.FORMAT.format(Whitelist.size()));

        execution.sender.sendMessage(Messages.Values.FOOTER_BAR);
    }
}
