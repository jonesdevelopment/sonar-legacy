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

package jones.sonar.bungee.data.connection.manager;

import jones.sonar.bungee.data.connection.ConnectionData;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ConnectionDataManager {
    public final Map<InetAddress, ConnectionData> DATA = new ConcurrentHashMap<>();

    public ConnectionData get(final InetAddress inetAddress) {
        return DATA.get(inetAddress);
    }

    public boolean contains(final InetAddress inetAddress) {
        return DATA.containsKey(inetAddress);
    }

    public boolean remove(final ConnectionData connectionData) {
        return remove(connectionData.inetAddress);
    }

    public boolean remove(final InetAddress inetAddress) {
        if (contains(inetAddress)) {
            DATA.remove(inetAddress);
            return true;
        }
        return false;
    }

    public ConnectionData createOrReturn(final InetAddress inetAddress) {
        if (contains(inetAddress)) {
            return get(inetAddress);
        }

        DATA.put(inetAddress, new ConnectionData(inetAddress));

        return DATA.get(inetAddress);
    }

    public void create(final InetAddress inetAddress) {
        if (contains(inetAddress)) return;

        DATA.put(inetAddress, new ConnectionData(inetAddress));
    }
}
