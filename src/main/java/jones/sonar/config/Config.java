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
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Config {

    public Configuration config;

    private final String fileName = "config.yml";

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

    @UtilityClass
    public class Values {
        public int MAX_PACKET_INDEX, MAX_PACKET_BYTES, MAX_PACKET_CAPACITY;

        public boolean CLIENT_CONNECT_EVENT, ENABLE_RECONNECT_CHECK,
                ENABLE_INVALID_NAME_CHECK;

        public String NAME_VALIDATION_REGEX = "^[a-zA-Z0-9_.]*$";

        public List<String> CUSTOM_REGEXES = new ArrayList<>();

        public boolean load() {
            try {
                // reset the list in order to prevent duplicates
                CUSTOM_REGEXES.clear();

                // general

                CLIENT_CONNECT_EVENT = config.getBoolean("general.use-client-connect-event", false);
                MAX_PACKET_INDEX = config.getInt("general.max-packet-index", 1024);
                MAX_PACKET_BYTES = config.getInt("general.max-packet-bytes", 2048);
                MAX_PACKET_CAPACITY = config.getInt("general.max-packet-capacity", 4096);

                // checks

                ENABLE_INVALID_NAME_CHECK = config.getBoolean("checks.invalid-name.enabled", true);
                NAME_VALIDATION_REGEX = config.getString("checks.invalid-name.regex", "^[a-zA-Z0-9_.]*$");
                CUSTOM_REGEXES.addAll(config.getStringList("checks.invalid-name.custom-regexes"));
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }
    }
}
