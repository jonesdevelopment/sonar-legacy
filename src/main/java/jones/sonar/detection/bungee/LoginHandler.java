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

package jones.sonar.detection.bungee;

import jones.sonar.config.Config;
import jones.sonar.data.connection.ConnectionData;
import jones.sonar.data.connection.manager.ConnectionDataManager;
import jones.sonar.detection.Detection;
import jones.sonar.detection.Detections;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.protocol.packet.LoginRequest;

import java.util.Objects;

@UtilityClass
public final class LoginHandler {
    public Detection check(final LoginRequest loginRequest, final ConnectionData connectionData) {
        connectionData.CONNECTIONS_PER_SECOND.increment();

        if (connectionData.CONNECTIONS_PER_SECOND.get() > Config.Values.MAX_REJOINS_PER_SECOND) {
            ConnectionDataManager.remove(connectionData);
            return Detections.BLACKLIST;
        }

        if (!connectionData.username.matches(Config.Values.NAME_VALIDATION_REGEX)) {
            return Detections.INVALID_NAME;
        }

        if (connectionData.checked == 0) {
            connectionData.checked = 1;
            connectionData.verifiedName = connectionData.username;
            return Detections.FIRST_JOIN_KICK;
        }

        if (connectionData.checked == 1) {
            connectionData.checked = 2;

            if (!Objects.equals(connectionData.verifiedName, connectionData.username)) {
                connectionData.checked = 0;
                ConnectionDataManager.remove(connectionData);
                return Detections.BLACKLIST;
            }
        }

        final long timeStamp = System.currentTimeMillis();

        if (timeStamp - connectionData.lastJoin <= Config.Values.REJOIN_DELAY) {
            return Detections.TOO_FAST_RECONNECT;
        }

        connectionData.lastJoin = timeStamp;

        return Detections.ALLOW;
    }
}
