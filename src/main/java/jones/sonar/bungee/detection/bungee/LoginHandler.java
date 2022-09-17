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

package jones.sonar.bungee.detection.bungee;

import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.detection.Detection;
import jones.sonar.bungee.detection.Detections;
import jones.sonar.universal.config.options.CustomRegexOptions;
import jones.sonar.universal.data.connection.ConnectionData;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.queue.PlayerQueue;
import jones.sonar.universal.util.Sensibility;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public final class LoginHandler implements Detections {
    public Detection check(final ConnectionData connectionData) {
        connectionData.CONNECTIONS_PER_SECOND.increment();

        if (connectionData.CONNECTIONS_PER_SECOND.get() > Config.Values.MAX_REJOINS_PER_SECOND) {
            ConnectionDataManager.remove(connectionData);
            return BLACKLIST;
        }

        if (!connectionData.username.matches(Config.Values.NAME_VALIDATION_REGEX)) {
            return INVALID_NAME;
        }

        if (connectionData.checked == 0) {
            connectionData.checked = 1;
            connectionData.verifiedName = connectionData.username;
            return FIRST_JOIN_KICK;
        }

        if ((Config.Values.REGEX_CHECK_MODE == CustomRegexOptions.DURING_ATTACK && Sensibility.isUnderAttack())
                || Config.Values.REGEX_CHECK_MODE == CustomRegexOptions.ALWAYS) {
            if (Config.Values.CUSTOM_REGEXES.stream().anyMatch(connectionData.username::matches)) {
                connectionData.checked = 0;

                if ((Config.Values.REGEX_BLACKLIST_MODE == CustomRegexOptions.DURING_ATTACK && Sensibility.isUnderAttack())
                        || Config.Values.REGEX_BLACKLIST_MODE == CustomRegexOptions.ALWAYS) {
                    ConnectionDataManager.remove(connectionData);
                    return BLACKLIST;
                }

                return INVALID_NAME;
            }
        }

        if (connectionData.checked == 1) {
            connectionData.checked = 2;

            if (!Objects.equals(connectionData.verifiedName, connectionData.username)) {
                ConnectionDataManager.remove(connectionData);
                return BLACKLIST;
            }
        }

        final long timeStamp = System.currentTimeMillis();

        if (timeStamp - connectionData.lastJoin <= Config.Values.REJOIN_DELAY) {
            connectionData.checked = 2;
            return TOO_FAST_RECONNECT;
        }

        connectionData.lastJoin = timeStamp;

        if (Sensibility.isUnderAttack()) {
            if (connectionData.checked == 2) {
                connectionData.checked = 3;
                return DURING_ATTACK;
            }

            PlayerQueue.addToQueue(connectionData.username);

            if (PlayerQueue.getPosition(connectionData.username) > 1) {
                return PLAYER_IN_QUEUE;
            }
        }

        final long online = connectionData.getAccountsOnlineWithSameIP();

        if (online > Config.Values.MAXIMUM_ONLINE_PER_IP) {
            if (online > Config.Values.MAXIMUM_ONLINE_PER_IP_BLACKLIST) {
                ConnectionDataManager.remove(connectionData);
                return BLACKLIST;
            }

            return TOO_MANY_ONLINE;
        }

        return ALLOW;
    }
}
