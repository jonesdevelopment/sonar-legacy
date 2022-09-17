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

package jones.sonar.bungee.caching;

import jones.sonar.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;

@UtilityClass
public class ServerDataProvider {
    public ServerInfo getForcedHost(final ListenerInfo listener, final InetSocketAddress virtualHost) {
        String forced = (virtualHost == null) ? null : listener.getForcedHosts().get(virtualHost.getHostString());

        if (forced == null && listener.isForceDefault()) {
            forced = listener.getDefaultServer();
        }

        return (forced == null) ? null : SonarBungee.INSTANCE.proxy.getServerInfo(forced);
    }
}
