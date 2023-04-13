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
package jones.sonar.bungee.bossbar;

import jones.sonar.bungee.notification.monitor.MonitorManager;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.BossBar;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DynamicBossBar {

    @Getter
    private final UUID uuid;

    private final BossBar barBuilder, removeBar;

    private final Set<String> peopleWhoSeeTheBossBar = new HashSet<>();

    public DynamicBossBar() {
        uuid = UUID.randomUUID();

        barBuilder = new BossBar(uuid, BossBarActions.TITLE);

        removeBar = new BossBar(uuid, BossBarActions.REMOVE);
    }

    public void update(final TextComponent title) {
        barBuilder.setTitle(ComponentSerializer.toString(title));
        barBuilder.setAction(2);
        barBuilder.setHealth(0f);

        barBuilder.setAction(4);
        barBuilder.setColor(3);
        barBuilder.setDivision(0);

        barBuilder.setAction(5);
        barBuilder.setFlags((byte) 0);
        barBuilder.setAction(0);

        MonitorManager.getPlayers()
                .filter(player -> player.getPendingConnection().getVersion() > 47)
                .filter(player -> !player.getName().startsWith(".")) // Geyser?
                .filter(player -> !player.getName().startsWith("*")) // Geyser?
                .forEach(player -> {
                    peopleWhoSeeTheBossBar.add(player.getName());

                    player.unsafe().sendPacket(barBuilder);
                });

        peopleWhoSeeTheBossBar.stream()
                .filter(name -> !MonitorManager.contains(name))
                .filter(name -> !name.startsWith(".")) // Geyser?
                .filter(name -> !name.startsWith("*")) // Geyser?
                .forEach(name -> {
                    final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);

                    if (player != null && player.getPendingConnection().getVersion() > 47) {
                        player.unsafe().sendPacket(removeBar);
                    }
                });
    }
}
