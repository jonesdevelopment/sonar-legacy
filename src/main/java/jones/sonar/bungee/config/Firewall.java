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

package jones.sonar.bungee.config;

import jones.sonar.bungee.util.ColorUtil;
import jones.sonar.universal.config.yaml.Configuration;
import jones.sonar.universal.config.yaml.ConfigurationProvider;
import jones.sonar.universal.config.yaml.YamlConfiguration;
import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@UtilityClass
public class Firewall {

    public Configuration config;

    public final String fileName = "firewall.yml";

    public void initialize() {
        try {
            final File file = new File(SonarBungee.INSTANCE.getPlugin().getDataFolder(), fileName);

            if (!file.exists()) {
                try (final InputStream in = SonarBungee.INSTANCE.getPlugin().getResourceAsStream("bungee/" + fileName)) {
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
        public boolean ENABLE_FIREWALL, BROADCAST;
        public String BLACKLIST_SET_NAME, BROADCAST_MESSAGE;
        public int BLACKLIST_TIMEOUT, BLACKLIST_DELAY, MAX_CPS_PER_IP;

        public boolean load() {
            try {
                // general
                ENABLE_FIREWALL = config.getBoolean("firewall.enabled", false);
                BLACKLIST_SET_NAME = config.getString("firewall.blacklist-name", "blacklist");
                BLACKLIST_TIMEOUT = Math.max(config.getInt("firewall.blacklist-timeout", 120000), 3000);
                BLACKLIST_DELAY = Math.max(config.getInt("firewall.blacklist-delay", 10000), 1000);
                MAX_CPS_PER_IP = Math.max(Math.min(config.getInt("firewall.max-cps-per-ip", 8), 999), 5);
                BROADCAST = config.getBoolean("firewall.broadcast-blacklisting", false);
                BROADCAST_MESSAGE = ColorUtil.format(config.getString("firewall.broadcast-message"))
                        .replaceAll("%prefix%", Messages.Values.PREFIX)
                        .replaceAll("%seconds%", SonarBungee.INSTANCE.FORMAT.format(BLACKLIST_DELAY / 1000D))
                        .replaceAll("%milliseconds%", SonarBungee.INSTANCE.FORMAT.format(BLACKLIST_DELAY));
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }
    }
}
