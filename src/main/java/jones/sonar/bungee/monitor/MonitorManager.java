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

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@UtilityClass
public class MonitorManager {
    public final Set<String> MONITOR_ENABLED = new HashSet<>();

    public Stream<ProxiedPlayer> getPlayers() {
        return MONITOR_ENABLED.stream()
                .map(SonarBungee.INSTANCE.proxy::getPlayer)
                .filter(Objects::nonNull)
                .filter(player -> player.hasPermission("sonar.monitor"));
    }

    public boolean toggle(final ProxiedPlayer player) {
        if (contains(player.getName())) {
            remove(player.getName());
        } else {
            add(player.getName());
        }

        return contains(player.getName());
    }

    public void add(final String playerName) {
        if (!contains(playerName)) {
            MONITOR_ENABLED.add(playerName);
        }
    }

    public void remove(final String playerName) {
        if (contains(playerName)) {
            MONITOR_ENABLED.remove(playerName);
        }
    }

    public boolean contains(final String playerName) {
        return MONITOR_ENABLED.contains(playerName);
    }
}
