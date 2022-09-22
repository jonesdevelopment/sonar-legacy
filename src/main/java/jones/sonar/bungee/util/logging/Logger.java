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

package jones.sonar.bungee.util.logging;

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.RequiredArgsConstructor;

import java.util.logging.Level;

@RequiredArgsConstructor
public enum Logger implements ILogger {

    WARNING(Level.WARNING),
    ERROR(Level.SEVERE),
    INFO(Level.INFO);

    private final Level logLevel;

    @Override
    public void log(final String data) {
        SonarBungee.INSTANCE.proxy.getLogger().log(logLevel, "[Sonar] " + data);
    }

    @Override
    public void log(final String data, final String prefix) {
        SonarBungee.INSTANCE.proxy.getLogger().log(logLevel, prefix + " " + data);
    }

    @Override
    public void logNoPrefix(final String data) {
        SonarBungee.INSTANCE.proxy.getLogger().log(logLevel, data);
    }
}
