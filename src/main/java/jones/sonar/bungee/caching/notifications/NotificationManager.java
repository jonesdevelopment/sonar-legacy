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

package jones.sonar.bungee.caching.notifications;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class NotificationManager {
    public final Set<String> SUBSCRIBED = new HashSet<>();

    public boolean toggle(final ProxiedPlayer player) {
        if (!contains(player.getName())) {
            subscribe(player.getName());
        } else {
            unsubscribe(player.getName());
        }
        return contains(player.getName());
    }

    public void subscribe(final String playerName) {
        if (!contains(playerName)) {
            SUBSCRIBED.add(playerName);
        }
    }

    public void unsubscribe(final String playerName) {
        if (contains(playerName)) {
            SUBSCRIBED.remove(playerName);
        }
    }

    public boolean contains(final String playerName) {
        return SUBSCRIBED.contains(playerName);
    }
}
