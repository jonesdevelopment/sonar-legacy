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

package jones.sonar.counter;

import jones.sonar.SonarBungee;
import jones.sonar.config.Messages;
import jones.sonar.data.ServerStatistics;
import jones.sonar.data.connection.manager.ConnectionDataManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.stream.Collectors;

public final class ActionBar {
    public ActionBar(final SonarBungee sonar) {
        new Thread(() -> {
            while (SonarBungee.INSTANCE.running) {
                try {
                    try {
                        final TextComponent counter = new TextComponent(Messages.Values.COUNTER_FORMAT
                                .replaceAll("%cps%", sonar.FORMAT.format(Math.max(Counter.CONNECTIONS_PER_SECOND.get(), 0)))
                                .replaceAll("%pings%", sonar.FORMAT.format(Math.max(Counter.PINGS_PER_SECOND.get(), 0)))
                                .replaceAll("%verify%", sonar.FORMAT.format(ConnectionDataManager.DATA.values().stream()
                                        .filter(connectionData -> connectionData.checked <= 1)
                                        .count()))
                                .replaceAll("%blocked%", sonar.FORMAT.format(ServerStatistics.BLOCKED_CONNECTIONS))
                                .replaceAll("%ips%", sonar.FORMAT.format(Math.max(Counter.IPS_PER_SECOND.get(), 0)))
                                .replaceAll("%total%", sonar.FORMAT.format(ServerStatistics.TOTAL_CONNECTIONS))
                                .replaceAll("%encryptions%", sonar.FORMAT.format(Math.max(Counter.ENCRYPTIONS_PER_SECOND.get(), 0)))
                                .replaceAll("%joins%", sonar.FORMAT.format(Math.max(Counter.JOINS_PER_SECOND.get(), 0))));

                        sonar.proxy.getPlayers().stream()
                                .filter(player -> player.hasPermission("sonar.verbose"))
                                .collect(Collectors.toSet())
                                .forEach(player -> player.sendMessage(ChatMessageType.ACTION_BAR, counter));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        // don't throw any exceptions
                    }

                    Thread.sleep(80);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        }, "sonar#counter").start();
    }
}
