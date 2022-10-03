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
package jones.sonar.bungee.monitor.bossbar;

import jones.sonar.bungee.monitor.MonitorManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.BossBar;

import java.util.UUID;

public final class DynamicBossBar {

    @Getter
    private final UUID uuid;

    private final BossBar barBuilder, removeBar;

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
                .filter(player -> !player.getName().startsWith("."))
                .forEach(player -> player.unsafe().sendPacket(barBuilder));

        SonarBungee.INSTANCE.proxy.getPlayers().stream()
                .filter(player -> !MonitorManager.contains(player.getName()))
                .filter(player -> player.getPendingConnection().getVersion() > 47)
                .filter(player -> !player.getName().startsWith("."))
                .forEach(player -> player.unsafe().sendPacket(removeBar));
    }
}
