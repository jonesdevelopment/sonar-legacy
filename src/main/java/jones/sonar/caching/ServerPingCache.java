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

package jones.sonar.caching;

import jones.sonar.SonarBungee;
import jones.sonar.config.Config;
import jones.sonar.network.bungee.handler.PlayerHandler;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ServerPing;

@UtilityClass
public class ServerPingCache {

    public ServerPing cachedServerPing = null;

    public boolean needsUpdate = true;

    public ServerPing getCached(final PlayerHandler adapter, final String motd, final int protocol) {
        if (!Config.Values.CACHE_MOTDS) {
            return getServerPing(adapter, motd, protocol);
        }

        if (needsUpdate || cachedServerPing == null) {
            needsUpdate = false;

            System.out.println("[!] updating server motd & icon");
            return update(adapter, motd, protocol);
        }

        System.out.println("[?] using cached server motd & icon");
        return cachedServerPing;
    }

    private ServerPing update(final PlayerHandler adapter, final String motd, final int protocol) {
        cachedServerPing = getServerPing(adapter, motd, protocol);

        return cachedServerPing;
    }

    private ServerPing getServerPing(final PlayerHandler adapter, final String motd, final int protocol) {
        return new ServerPing(
                new ServerPing.Protocol(Config.Values.SERVER_BRAND, protocol),
                new ServerPing.Players(adapter.getListener().getMaxPlayers(), SonarBungee.INSTANCE.proxy.getOnlineCount(), null),
                motd, SonarBungee.INSTANCE.proxy.getConfig().getFaviconObject());
    }
}
