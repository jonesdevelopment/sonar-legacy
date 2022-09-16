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
package jones.sonar.config;

import jones.sonar.SonarBungee;
import jones.sonar.config.yaml.Configuration;
import jones.sonar.config.yaml.ConfigurationProvider;
import jones.sonar.config.yaml.YamlConfiguration;
import jones.sonar.util.ColorUtil;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@UtilityClass
public class Messages {

    public Configuration config;

    private final String fileName = "messages.yml";

    public void initialize() {
        try {
            final File file = new File(SonarBungee.INSTANCE.getPlugin().getDataFolder().getPath(), fileName);

            if (!file.exists()) {
                try (final InputStream in = SonarBungee.INSTANCE.getPlugin().getResourceAsStream(fileName)) {
                    Files.copy(in, file.toPath());
                } catch (IOException exception) {
                    exception.printStackTrace();
                    return;
                }
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String format(final String message) {
        return ColorUtil.format(message)
                .replaceAll("%prefix%", Values.PREFIX);
    }

    @UtilityClass
    public class Values {
        public String PREFIX, COUNTER_FORMAT, COUNTER_WAITING_FORMAT,
                COUNTER_ENABLED, COUNTER_DISABLED;

        public boolean ENABLE_COUNTER_WAITING_FORMAT;

        public boolean load() {
            try {
                // general
                PREFIX = ColorUtil.format(config.getString("prefix"));

                // counter
                COUNTER_ENABLED = format(config.getString("counter.action-bar.enabled"));
                COUNTER_DISABLED = format(config.getString("counter.action-bar.disabled"));
                COUNTER_FORMAT = format(config.getString("counter.action-bar.format"));
                COUNTER_WAITING_FORMAT = format(config.getString("counter.action-bar.waiting"));
                ENABLE_COUNTER_WAITING_FORMAT = config.getBoolean("counter.action-bar.enable-waiting-message");
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }
    }
}
