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

package jones.sonar.universal.data.player.manager;

import jones.sonar.universal.data.player.PlayerData;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class PlayerDataManager {
    public final Map<String, PlayerData> DATA = new ConcurrentHashMap<>(300000);

    public PlayerData get(final String playerName) {
        if (contains(playerName)) {
            return DATA.get(playerName);
        }

        return null;
    }

    public boolean contains(final String playerName) {
        return DATA.containsKey(playerName);
    }

    public boolean remove(final PlayerData playerData) {
        return remove(playerData.username);
    }

    public boolean remove(final String playerName) {
        if (contains(playerName)) {
            DATA.remove(playerName);
            return true;
        }
        return false;
    }

    public PlayerData create(final String username) {
        if (contains(username)) {
            return get(username);
        }

        DATA.put(username, new PlayerData(username));

        return DATA.get(username);
    }
}
