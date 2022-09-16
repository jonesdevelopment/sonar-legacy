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
import jones.sonar.command.CommandExecution;
import jones.sonar.command.SubCommand;
import jones.sonar.config.Messages;
import jones.sonar.util.OperatingSystem;
import jones.sonar.util.PerformanceMonitor;
import jones.sonar.util.Sensibility;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public final class InfoCommand extends SubCommand {

    public InfoCommand() {
        super("info", "Show server information", "sonar.info");
    }

    @Override
    public void execute(final CommandExecution execution) {
        execution.sender.sendMessage(Messages.Values.HEADER_BAR);

        final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();

        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fOperating system: " + OperatingSystem.getOSName());

        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fAvailable processors: " + operatingSystem.getAvailableProcessors() + " cores");

        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fGlobal CPU load (all cores): " + PerformanceMonitor.formatCPULoad() + "%");
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fAverage CPU load (all cores): " + PerformanceMonitor.formatAverageCPULoad() + "%");

        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fTotal available memory: " + PerformanceMonitor.getTotalMemory() + " MB");
        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fMemory (used/free): " + PerformanceMonitor.getUsedMemory() + " MB / " + PerformanceMonitor.getFreeMemory() + " MB");

        if (SonarBungee.INSTANCE.proxy.getPlayers().size() > 0) {
            execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fAverage §flatency §fof §fall §fplayers: "
                    + SonarBungee.INSTANCE.FORMAT.format(SonarBungee.INSTANCE.proxy.getPlayers().stream()
                    .mapToLong(ProxiedPlayer::getPing).sum() / SonarBungee.INSTANCE.proxy.getPlayers().size()) + " ms");
        }

        execution.sender.sendMessage("§7 " + Messages.Values.LIST_SYMBOL + " §fIs §fyour §fserver §funder §fattack? " + (Sensibility.isUnderAttack() ? "§cYes" : "§aNo"));
        execution.sender.sendMessage(Messages.Values.FOOTER_BAR);
    }
}
