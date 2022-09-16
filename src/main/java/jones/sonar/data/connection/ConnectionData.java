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

package jones.sonar.data.connection;

import jones.sonar.SonarBungee;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;

@RequiredArgsConstructor
public final class ConnectionData {
    public final InetAddress inetAddress;
    public final String name;

    public boolean proxy = false, checked = false;

    public ProxiedPlayer tryToGetPlayer() {
        return SonarBungee.INSTANCE.proxy.getPlayer(name);
    }
}
