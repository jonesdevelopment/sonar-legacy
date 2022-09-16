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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.stream.Collectors;

public final class ActionBar {
    public ActionBar(final SonarBungee sonar) {
        new Thread(() -> {
            while (SonarBungee.INSTANCE.running) {
                try {
                    final TextComponent counter = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                            "&e&lSonar &7» &7CPS: &r%cps% &a∙ &7IPs: &r%ips% &a∙ &7Joins: &r%joins% &a∙ &7Pings: &r%pings% &a∙ &7Verification: &r%verify% &a∙ &7Blocked: &r%blocked%"
                                    .replaceAll("%cps%", sonar.FORMAT.format(Counter.CONNECTIONS_PER_SECOND.get()))
                                    .replaceAll("%pings%", sonar.FORMAT.format(Counter.PINGS_PER_SECOND.get()))
                                    .replaceAll("%joins%", sonar.FORMAT.format(Counter.JOINS_PER_SECOND.get()))));

                    sonar.proxy.getPlayers().stream()
                            .filter(player -> player.hasPermission("sonar.verbose"))
                            .collect(Collectors.toSet())
                            .forEach(player -> player.sendMessage(ChatMessageType.ACTION_BAR, counter));

                    Thread.sleep(80);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }
}
