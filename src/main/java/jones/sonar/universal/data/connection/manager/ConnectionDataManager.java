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

package jones.sonar.universal.data.connection.manager;

import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.connection.ConnectionData;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class ConnectionDataManager {
    public final Map<InetAddress, ConnectionData> DATA = new ConcurrentHashMap<>(500000);

    public long getVerifying() {
        return getVerifyingData().count();
    }

    public Stream<ConnectionData> getVerifyingData() {
        return DATA.values().stream()
                .filter(connectionData -> connectionData.checked <= 1);
    }

    public ConnectionData get(final InetAddress inetAddress) {
        if (contains(inetAddress)) {
            return DATA.get(inetAddress);
        }

        return null;
    }

    public boolean contains(final InetAddress inetAddress) {
        return DATA.containsKey(inetAddress);
    }

    public void resetCheckStage(final int newStage) {
        DATA.values().forEach(data -> data.checked = newStage);
    }

    public void removeAllUnused() {
        DATA.keySet().stream()
                .filter(Blacklist::isBlacklisted)
                .collect(Collectors.toSet())
                .forEach(DATA::remove);
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

    public ConnectionData create(final InetAddress inetAddress) {
        if (contains(inetAddress)) {
            return get(inetAddress);
        }

        DATA.put(inetAddress, new ConnectionData(inetAddress));

        return DATA.get(inetAddress);
    }
}
