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

package jones.sonar.bungee.counter;

import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.util.ColorUtil;
import jones.sonar.bungee.util.Sensibility;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.ServerStatistics;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.queue.IPSQueue;
import jones.sonar.universal.queue.PlayerQueue;
import jones.sonar.universal.whitelist.Whitelist;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class ActionBar extends Thread implements Runnable {

    private final SonarBungee sonar;

    private long lastSwitch = 0L;

    @Override
    public void run() {
        int index = 0;

        while (sonar.running) {
            try {
                try {
                    if (index > 8) index = 0;

                    // caching all values as local variables to save some performance
                    final long cps = Counter.CONNECTIONS_PER_SECOND.get(),
                            pps = Counter.PINGS_PER_SECOND.get(),
                            eps = Counter.ENCRYPTIONS_PER_SECOND.get(),
                            ips = Counter.IPS_PER_SECOND.get(),
                            jps = Counter.JOINS_PER_SECOND.get(),
                            handshakes = Counter.HANDSHAKES_PER_SECOND.get();

                    // submit our counter results to the peak calculation
                    sonar.ipSecPeakCalculator.submit(ips);
                    sonar.cpsPeakCalculator.submit(cps);

                    final long timeStamp = System.currentTimeMillis();

                    if (Sensibility.isUnderAttack() || !Messages.Values.ENABLE_COUNTER_WAITING_FORMAT) {
                        lastSwitch = timeStamp;
                    }

                    // this is needed to make the action bar align in the middle
                    final String GENERAL_FORMAT = timeStamp - lastSwitch > 2500L
                            ? Messages.Values.COUNTER_WAITING_FORMAT
                            : Messages.Values.COUNTER_FORMAT;

                    int colorCodeCount = 0;

                    // counting every color code within the message
                    for (final char c : GENERAL_FORMAT.toCharArray()) {
                        if (c == '§') colorCodeCount++;
                    }

                    // adding empty lines in front of the message to align the message in
                    // the center of the players' screen
                    final String SPACES = repeat(" ", Math.min(colorCodeCount, 24));

                    final TextComponent counter = new TextComponent(GENERAL_FORMAT
                            .replaceAll("%verify%", sonar.FORMAT.format(ConnectionDataManager.getVerifying()))
                            .replaceAll("%blocked%", sonar.FORMAT.format(ServerStatistics.BLOCKED_CONNECTIONS))
                            .replaceAll("%whitelisted%", sonar.FORMAT.format(Whitelist.size()))
                            .replaceAll("%blacklisted%", sonar.FORMAT.format(Blacklist.size()))
                            .replaceAll("%total%", sonar.FORMAT.format(ServerStatistics.TOTAL_CONNECTIONS))
                            .replaceAll("%arrow%", getSpinningSymbol(index++))
                            .replaceAll("%queue%", sonar.FORMAT.format(PlayerQueue.QUEUE.size() + IPSQueue.QUEUE.size()))
                            .replaceAll("%filter-symbol%", Sensibility.isUnderAttack() ? Messages.Values.FILTER_SYMBOL_ON : Messages.Values.FILTER_SYMBOL_OFF)
                            .replaceAll("%cps%", ColorUtil.getColorForCounter(cps) + sonar.FORMAT.format(cps))
                            .replaceAll("%pings%", ColorUtil.getColorForCounter(pps) + sonar.FORMAT.format(pps))
                            .replaceAll("%ips%", ColorUtil.getColorForCounter(ips) + sonar.FORMAT.format(ips))
                            .replaceAll("%handshakes%", ColorUtil.getColorForCounter(handshakes) + sonar.FORMAT.format(handshakes))
                            .replaceAll("%encryptions%", ColorUtil.getColorForCounter(eps) + sonar.FORMAT.format(eps))
                            .replaceAll("%logins%", ColorUtil.getColorForCounter(jps) + sonar.FORMAT.format(jps)));

                    final TextComponent legacyCounter = new TextComponent(counter);

                    legacyCounter.setText(SPACES + counter.getText());

                    ActionBarManager.getPlayers()
                            .filter(player -> player.hasPermission("sonar.verbose"))
                            .collect(Collectors.toSet())
                            .forEach(player -> {
                                if (player.getPendingConnection().getVersion() < ProtocolConstants.MINECRAFT_1_13) {
                                    player.sendMessage(ChatMessageType.ACTION_BAR, legacyCounter);
                                } else {
                                    player.sendMessage(ChatMessageType.ACTION_BAR, counter);
                                }
                            });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                Thread.sleep(Config.Values.ACTION_BAR_COUNTER_DELAY);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    // ↺ ↻
    private String getSpinningSymbol(final int index) {
        switch (index) {
            default:
            case 1:
                return "←";
            case 2:
                return "↖";
            case 3:
                return "↑";
            case 4:
                return "↗";
            case 5:
                return "→";
            case 6:
                return "↘";
            case 7:
                return "↓";
            case 8:
                return "↙";
        }
    }

    private String repeat(final String string, final int count) {
        final StringBuilder buffer = new StringBuilder();

        for(int i = 0; i < count; ++i) {
            buffer.append(string);
        }

        return buffer.toString();
    }
}
