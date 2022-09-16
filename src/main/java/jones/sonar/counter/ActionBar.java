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
import jones.sonar.util.ColorUtil;
import jones.sonar.util.Sensibility;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.stream.Collectors;

public final class ActionBar {
    public ActionBar(final SonarBungee sonar) {
        new Thread(() -> {
            while (SonarBungee.INSTANCE.running) {
                try {
                    try {
                        // caching all values as local variables to save some performance
                        final long cps = Counter.CONNECTIONS_PER_SECOND.get(),
                                pps = Counter.PINGS_PER_SECOND.get(),
                                eps = Counter.ENCRYPTIONS_PER_SECOND.get(),
                                ips = Counter.IPS_PER_SECOND.get(),
                                jps = Counter.JOINS_PER_SECOND.get();

                        // this is needed to make the action bar align in the middle
                        String GENERAL_FORMAT = (!Sensibility.isUnderAttack() && Messages.Values.ENABLE_COUNTER_WAITING_FORMAT
                                ? Messages.Values.COUNTER_WAITING_FORMAT
                                : Messages.Values.COUNTER_FORMAT);

                        int colorCodeCount = 0;

                        // counting every color code within the message
                        for (final char c : GENERAL_FORMAT.toCharArray()) {
                            if (c == '§') colorCodeCount++;
                        }

                        // adding empty lines in front of the message to align the message in
                        // the center of the players' screen
                        GENERAL_FORMAT = repeat(" ", Math.min(colorCodeCount, 16)) + GENERAL_FORMAT;

                        final TextComponent counter = new TextComponent(GENERAL_FORMAT
                                .replaceAll("%cps%", ColorUtil.getColorForCounter(cps) + sonar.FORMAT.format(cps))
                                .replaceAll("%pings%", ColorUtil.getColorForCounter(pps) + sonar.FORMAT.format(pps))
                                .replaceAll("%verify%", sonar.FORMAT.format(ConnectionDataManager.DATA.values().stream()
                                        .filter(connectionData -> connectionData.checked <= 1)
                                        .count()))
                                .replaceAll("%blocked%", sonar.FORMAT.format(ServerStatistics.BLOCKED_CONNECTIONS))
                                .replaceAll("%ips%", ColorUtil.getColorForCounter(ips) + sonar.FORMAT.format(ips))
                                .replaceAll("%total%", sonar.FORMAT.format(ServerStatistics.TOTAL_CONNECTIONS))
                                .replaceAll("%encryptions%", ColorUtil.getColorForCounter(eps) + sonar.FORMAT.format(eps))
                                .replaceAll("%filter-symbol%", Sensibility.isUnderAttack() ? Messages.Values.FILTER_SYMBOL_ON : Messages.Values.FILTER_SYMBOL_OFF)
                                .replaceAll("%joins%", ColorUtil.getColorForCounter(jps) + sonar.FORMAT.format(jps)));

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

    private String repeat(final String string, final int count) {
        final StringBuilder buffer = new StringBuilder();

        for(int i = 0; i < count; ++i) {
            buffer.append(string);
        }

        return buffer.toString();
    }
}
