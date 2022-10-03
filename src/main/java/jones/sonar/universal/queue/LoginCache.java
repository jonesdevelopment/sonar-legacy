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

package jones.sonar.universal.queue;

import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.platform.SonarPlatform;
import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class LoginCache {
    public final Set<String> HAVE_LOGGED_IN = new HashSet<>();

    public void removeAllUnused(final SonarPlatform platform) {
        switch (platform) {
            case BUNGEE: {
                HAVE_LOGGED_IN.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()).forEach(name -> {
                            final ProxiedPlayer proxiedPlayer = SonarBungee.INSTANCE.proxy.getPlayer(name);

                            if (proxiedPlayer == null) {
                                return;
                            }

                            final InetAddress inetAddress = ((InetSocketAddress) proxiedPlayer.getPendingConnection().getSocketAddress()).getAddress();

                            if (Blacklist.isBlacklisted(inetAddress)) {
                                HAVE_LOGGED_IN.remove(name);
                            }
                        });
                break;
            }

            default:
            case VELOCITY: {
                break;
            }
        }
    }
}
