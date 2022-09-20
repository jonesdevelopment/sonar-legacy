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

package jones.sonar.universal.util;

import io.netty.channel.Channel;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.ServerStatistics;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.InetSocketAddress;

@UtilityClass
public class ExceptionHandler {
    public void handle(final Channel channel, final Throwable cause) {

        // forcibly close connections without using a future (delayed)
        channel.unsafe().closeForcibly();

        ServerStatistics.BLOCKED_CONNECTIONS++;

        // IOException can be thrown by disconnecting from the server
        // We need to exempt clients for that, so they won't get false blacklisted
        if (cause instanceof IOException) return;

        // blacklist the ip address
        Blacklist.addToBlacklist(((InetSocketAddress) channel.remoteAddress()).getAddress());
    }
}
