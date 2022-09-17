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

package jones.sonar.api;

import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.connection.ConnectionData;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.whitelist.Whitelist;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;

public interface SonarAPI {
    default long getPlayerBotLevel(final InetAddress inetAddress) {
        final ConnectionData data = ConnectionDataManager.get(inetAddress);

        if (data != null) {
            return data.botLevel;
        }

        return 0L;
    }

    default long getPlayerBotLevel(final SocketAddress inetAddress) {
        return getPlayerBotLevel(((InetSocketAddress) inetAddress).getAddress());
    }

    default boolean isBlacklisted(final InetAddress inetAddress) {
        return Blacklist.isBlacklisted(inetAddress);
    }

    default boolean isBlacklisted(final SocketAddress inetAddress) {
        return isBlacklisted(((InetSocketAddress) inetAddress).getAddress());
    }

    default boolean isWhitelisted(final InetAddress inetAddress) {
        return Whitelist.isWhitelisted(inetAddress);
    }

    default boolean isWhitelisted(final SocketAddress inetAddress) {
        return isWhitelisted(((InetSocketAddress) inetAddress).getAddress());
    }

    default void addToBlacklist(final InetAddress inetAddress) {
        Blacklist.addToBlacklist(inetAddress);
    }

    default void addToBlacklist(final SocketAddress inetAddress) {
        addToBlacklist(((InetSocketAddress) inetAddress).getAddress());
    }

    default void addToWhitelist(final InetAddress inetAddress) {
        Whitelist.addToWhitelist(inetAddress);
    }

    default void addToWhitelist(final SocketAddress inetAddress) {
        addToWhitelist(((InetSocketAddress) inetAddress).getAddress());
    }

    default Collection<InetAddress> getBlacklistedIPAddresses() {
        return Blacklist.BLACKLISTED;
    }

    default Collection<InetAddress> getWhitelistedIPAddresses() {
        return Whitelist.WHITELISTED;
    }
}