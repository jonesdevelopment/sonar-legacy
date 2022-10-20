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

package jones.sonar.bungee.filter;

import jones.sonar.bungee.config.Config;
import jones.sonar.universal.platform.bungee.SonarBungee;

public final class DefaultConsoleFilter {
    DefaultConsoleFilter() {
        SonarBungee.INSTANCE.proxy.getLogger().setFilter(record -> {
            final String message = SonarBungee.INSTANCE.proxy.getName().equals("BungeeCord") ? (new ConciseFormatter(true))
                    .formatMessage(record)
                    .trim() : record.getMessage();

            return ((!(record.getSourceClassName().equals("net.md_5.bungee.connection.InitialHandler")
                    || (record.getSourceClassName().equals("net.md_5.bungee.log.BungeeLogger") && message.contains(" InitialHandler ")))
                    && !(record.getSourceClassName().equals("net.md_5.bungee.netty.HandlerBoss")
                    && message.contains(" - encountered exception: ")))
                    || Config.Values.LOG_CONNECTIONS)
                    && !message.equals("No client connected for pending server!");
        });
    }
}
