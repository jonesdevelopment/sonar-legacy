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
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.platform.bungee.SonarBungee;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class FirewallThread extends Thread implements Runnable {

    @Override
    public void run() {
        while (SonarBungee.INSTANCE.running) {
            try {
                try {
                    if (Firewall.Values.ENABLE_FIREWALL) {
                        final Set<InetAddress> toRemove = new HashSet<>();

                        Blacklist.BLACKLISTED.stream()
                                .limit(10000)
                                .collect(Collectors.toSet())
                                .forEach(inetAddress -> {
                                    FirewallManager.execute("ipset -A "
                                            + Firewall.Values.BLACKLIST_SET_NAME + " "
                                            + String.valueOf(inetAddress).replace("/", ""));
                                    toRemove.add(inetAddress);
                                });

                        Blacklist.BLACKLISTED.removeAll(toRemove);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                Thread.sleep(Firewall.Values.BLACKLIST_DELAY);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }
}
