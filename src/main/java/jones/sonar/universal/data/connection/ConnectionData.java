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

package jones.sonar.universal.data.connection;

import jones.sonar.SonarBungee;
import jones.sonar.universal.counter.CounterMap;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@RequiredArgsConstructor
public final class ConnectionData {
    public final InetAddress inetAddress;

    public final CounterMap CONNECTIONS_PER_SECOND = new CounterMap(1000).build();

    public String username = "", verifiedName = "", lastUsername = "";

    public long lastJoin = 0L, sinceLoggedIn = 0L;

    public int checked = 0, clientSettingsPackets = 0,
            customPayloadPackets = 0, failedReconnect = 0,
            underAttackChecks = 0, botLevel = 0;

    public ProxiedPlayer tryToGetPlayer() {
        return SonarBungee.INSTANCE.proxy.getPlayer(username);
    }

    // current player + number of all online players with that ip
    public long getAccountsOnlineWithSameIP() {
        return 1 + SonarBungee.INSTANCE.proxy.getPlayers().stream()
                .filter(player -> inetAddress == ((InetSocketAddress) player.getSocketAddress()).getAddress())
                .count();
    }
}
