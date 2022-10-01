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

package jones.sonar.bungee.monitor;

import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.monitor.bossbar.BossBarManager;
import jones.sonar.bungee.monitor.bossbar.DynamicBossBar;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.PerformanceMonitor;
import net.md_5.bungee.api.chat.TextComponent;

public final class MonitorThread extends Thread implements Runnable {

    private double lastUsage = 0D;

    @Override
    public void run() {
        while (SonarBungee.INSTANCE.running) {
            try {
                try {
                    if (BossBarManager.CURRENT == null) {
                        BossBarManager.CURRENT = new DynamicBossBar();
                    }
                    final DynamicBossBar bossBar = BossBarManager.CURRENT;

                    final double totalMemory = (double) PerformanceMonitor.getTotalMemory();
                    final double usedMemory = (double) PerformanceMonitor.getUsedMemory();

                    final double memoryPercent = usedMemory / totalMemory;

                    final double memoryUsage = memoryPercent * Messages.Values.GRAPH_SYMBOL_COUNT;

                    final double cpuUsage = (PerformanceMonitor.getCPULoad() / 100D) * Messages.Values.GRAPH_SYMBOL_COUNT;

                    String symbol = Messages.Values.MONITOR_GRAPH_STATIC;

                    final double usage = cpuUsage + memoryUsage;

                    if (usage < lastUsage) {
                        symbol = Messages.Values.MONITOR_GRAPH_DOWN;
                    } else if (usage > lastUsage && lastUsage > 0) {
                        symbol = Messages.Values.MONITOR_GRAPH_UP;
                    }

                    lastUsage = usage;

                    final StringBuilder memoryGraph = new StringBuilder(), cpuGraph = new StringBuilder();

                    if (Messages.Values.MONITOR_FORMAT.contains("-graph%")) {
                        for (int i = 0; i < Messages.Values.GRAPH_SYMBOL_COUNT; i++) {
                            if (i <= cpuUsage) {
                                if (i >= Messages.Values.GRAPH_SYMBOL_COUNT / 1.5D) {
                                    if (i > Messages.Values.GRAPH_SYMBOL_COUNT / 1.15D) {
                                        cpuGraph.append(Messages.Values.MONITOR_GRAPH_FILLED_BAD).append(Messages.Values.MONITOR_GRAPH_FILL_SYMBOL);
                                    } else {
                                        cpuGraph.append(Messages.Values.MONITOR_GRAPH_FILLED_UNSAFE).append(Messages.Values.MONITOR_GRAPH_FILL_SYMBOL);
                                    }
                                } else {
                                    cpuGraph.append(Messages.Values.MONITOR_GRAPH_FILLED_SAFE).append(Messages.Values.MONITOR_GRAPH_FILL_SYMBOL);
                                }
                            } else {
                                cpuGraph.append(Messages.Values.MONITOR_GRAPH_UNFILLED).append(Messages.Values.MONITOR_GRAPH_FILL_SYMBOL);
                            }

                            if (i <= memoryUsage) {
                                if (i >= Messages.Values.GRAPH_SYMBOL_COUNT / 1.5D) {
                                    if (i > Messages.Values.GRAPH_SYMBOL_COUNT / 1.15D) {
                                        memoryGraph.append(Messages.Values.MONITOR_GRAPH_FILLED_BAD).append(Messages.Values.MONITOR_GRAPH_FILL_SYMBOL);
                                    } else {
                                        memoryGraph.append(Messages.Values.MONITOR_GRAPH_FILLED_UNSAFE).append(Messages.Values.MONITOR_GRAPH_FILL_SYMBOL);
                                    }
                                } else {
                                    memoryGraph.append(Messages.Values.MONITOR_GRAPH_FILLED_SAFE).append(Messages.Values.MONITOR_GRAPH_FILL_SYMBOL);
                                }
                            } else {
                                memoryGraph.append(Messages.Values.MONITOR_GRAPH_UNFILLED).append(Messages.Values.MONITOR_GRAPH_FILL_SYMBOL);
                            }
                        }
                    }

                    final String formatted = Messages.Values.MONITOR_FORMAT
                            .replaceAll("%cpu-graph%", cpuGraph.toString())
                            .replaceAll("%memory-graph%", memoryGraph.toString())
                            .replaceAll("%up-down%", symbol);

                    bossBar.update(new TextComponent(formatted));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                Thread.sleep(Messages.Values.MONITOR_REFRESH_DELAY);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}
