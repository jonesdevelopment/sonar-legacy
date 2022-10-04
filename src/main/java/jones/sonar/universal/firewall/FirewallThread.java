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

package jones.sonar.universal.firewall;

import jones.sonar.bungee.config.Firewall;
import jones.sonar.bungee.notification.counter.ActionBarManager;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.platform.bungee.SonarBungee;

import java.util.stream.Collectors;

public final class FirewallThread extends Thread implements Runnable {

    @Override
    public void run() {
        while (SonarBungee.INSTANCE.running) {
            try {
                try {
                    if (Firewall.Values.ENABLE_FIREWALL) {
                        final long firewalledBefore = Blacklist.FIREWALLED.size();

                        Blacklist.BLACKLISTED.stream()
                                .limit(Firewall.Values.BLACKLIST_CACHE_LIMIT)
                                .collect(Collectors.toSet())
                                .forEach(inetAddress -> {
                                    FirewallManager.execute("ipset add "
                                            + Firewall.Values.BLACKLIST_SET_NAME + " "
                                            + String.valueOf(inetAddress).replace("/", ""));

                                    Blacklist.FIREWALLED.add(inetAddress);
                                });

                        final long firewalled = Math.max(Blacklist.FIREWALLED.size() - firewalledBefore, 0);

                        if (Firewall.Values.BROADCAST && firewalled > 0) {
                            final String alert = Firewall.Values.BROADCAST_MESSAGE
                                    .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(firewalled))
                                    .replaceAll("%es%", firewalled == 1 ? "" : "es")
                                    .replaceAll("%have/has%", firewalled == 1 ? "has" : "have")
                                    .replaceAll("%has/have%", firewalled == 1 ? "has" : "have");

                            ActionBarManager.getPlayers()
                                    .collect(Collectors.toSet())
                                    .forEach(player -> player.sendMessage(alert));
                        }
                    }
                } catch (Exception exception) {
                   // exception.printStackTrace();
                }

                Thread.sleep(Firewall.Values.BLACKLIST_DELAY);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}
