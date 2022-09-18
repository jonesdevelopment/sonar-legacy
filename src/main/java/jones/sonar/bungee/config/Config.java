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

import jones.sonar.SonarBungee;
import jones.sonar.bungee.util.ColorUtil;
import jones.sonar.universal.config.options.CustomRegexOptions;
import jones.sonar.universal.config.yaml.Configuration;
import jones.sonar.universal.config.yaml.ConfigurationProvider;
import jones.sonar.universal.config.yaml.YamlConfiguration;
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
        public int MAX_PACKET_INDEX, MAX_PACKET_BYTES, MAX_PACKET_CAPACITY,
                REJOIN_DELAY, MAX_REJOINS_PER_SECOND, MINIMUM_JOINS_PER_SECOND,
                ACTION_BAR_COUNTER_DELAY, MAXIMUM_ONLINE_PER_IP,
                MAXIMUM_QUEUE_POLL_RATE, MAX_NAME_LENGTH, WEBHOOK_DELAY,
                WEBHOOK_COLOR_R, WEBHOOK_COLOR_G, WEBHOOK_COLOR_B,
                QUEUE_POLL_RATE;

        public boolean CLIENT_CONNECT_EVENT, ENABLE_RECONNECT_CHECK,
                ENABLE_INVALID_NAME_CHECK, ENABLE_FIRST_JOIN, CACHE_MOTDS,
                ALLOW_PROXY_PROTOCOL, ALLOW_PING_PASS_THROUGH, WEBHOOK_ENABLED;

        public CustomRegexOptions REGEX_BLACKLIST_MODE = CustomRegexOptions.UNKNOWN,
                REGEX_CHECK_MODE = CustomRegexOptions.UNKNOWN;

        public String NAME_VALIDATION_REGEX, SERVER_BRAND, WEBHOOK_URL,
                WEBHOOK_FORMAT, WEBHOOK_TITLE;

        public List<String> CUSTOM_REGEXES = new ArrayList<>();

        public boolean load() {
            try {
                // reset the list in order to prevent duplicates
                CUSTOM_REGEXES.clear();

                // general
                CLIENT_CONNECT_EVENT = config.getBoolean("general.use-client-connect-event", false);
                ALLOW_PROXY_PROTOCOL = config.getBoolean("general.use-proxy-protocol", false);
                MAX_PACKET_INDEX = config.getInt("general.maximum-packet-index", 1024);
                MAX_PACKET_BYTES = config.getInt("general.maximum-packet-bytes", 2048);
                MAX_PACKET_CAPACITY = config.getInt("general.maximum-packet-capacity", 4096);
                MINIMUM_JOINS_PER_SECOND = config.getInt("general.minimum-joins-per-second", 6);
                SERVER_BRAND = ColorUtil.format(config.getString("general.fake-server-brand", "Protected"));
                CACHE_MOTDS = config.getBoolean("general.cache-incoming-motd-requests", true);
                ACTION_BAR_COUNTER_DELAY = Math.max(Math.min(config.getInt("general.action-bar-counter-delay", 80), 1000), 10);
                MAXIMUM_ONLINE_PER_IP = Math.max(config.getInt("general.maximum-online-per-ip", 2), 1);
                MAXIMUM_QUEUE_POLL_RATE = Math.max(config.getInt("general.maximum-queue-poll-rate", 2000), 1);
                QUEUE_POLL_RATE = Math.max(Math.min(config.getInt("general.queue-poll-rate", 1000), MAXIMUM_QUEUE_POLL_RATE), 1);
                ALLOW_PING_PASS_THROUGH = config.getBoolean("general.allow-forced-host-ping", false);

                // webhook
                WEBHOOK_ENABLED = config.getBoolean("notifications.webhook.enabled", false);
                WEBHOOK_DELAY = config.getInt("notifications.webhook.delay", 240000);
                WEBHOOK_FORMAT = Messages.format(Messages.fromList(config.getStringList("notifications.webhook.format"), "\\n"));
                WEBHOOK_TITLE = Messages.format(config.getString("notifications.webhook.title"));
                WEBHOOK_URL = Messages.format(config.getString("notifications.webhook.url", ""));
                WEBHOOK_COLOR_R = Math.min(Math.max(config.getInt("notifications.webhook.color.r", 255), 0), 255);
                WEBHOOK_COLOR_G = Math.min(Math.max(config.getInt("notifications.webhook.color.g", 50), 0), 255);
                WEBHOOK_COLOR_B = Math.min(Math.max(config.getInt("notifications.webhook.color.b", 0), 0), 255);

                // checks
                ENABLE_RECONNECT_CHECK = config.getBoolean("checks.reconnect-check.enabled", true);
                ENABLE_FIRST_JOIN = config.getBoolean("checks.reconnect-check.first-join", true);
                REJOIN_DELAY = config.getInt("checks.reconnect-check.rejoin-delay", 1000);
                MAX_REJOINS_PER_SECOND = config.getInt("checks.reconnect-check.maximum-rejoins-per-second", 8);

                ENABLE_INVALID_NAME_CHECK = config.getBoolean("checks.invalid-name.enabled", true);
                MAX_NAME_LENGTH = Math.max(config.getInt("checks.invalid-name.max-length", 16), 1);
                NAME_VALIDATION_REGEX = config.getString("checks.invalid-name.regex", "^[a-zA-Z0-9_.]*$");

                switch (config.getString("checks.invalid-name.blacklist-custom-regexes", "during_attack").toLowerCase()) {
                    case "during_attack": {
                        REGEX_BLACKLIST_MODE = CustomRegexOptions.DURING_ATTACK;
                        break;
                    }

                    case "always": {
                        REGEX_BLACKLIST_MODE = CustomRegexOptions.ALWAYS;
                        break;
                    }

                    case "never": {
                        REGEX_BLACKLIST_MODE = CustomRegexOptions.NEVER;
                        break;
                    }

                    default: {
                        REGEX_BLACKLIST_MODE = CustomRegexOptions.UNKNOWN;
                        break;
                    }
                }

                switch (config.getString("checks.invalid-name.check-custom-regexes", "during_attack").toLowerCase()) {
                    case "during_attack": {
                        REGEX_BLACKLIST_MODE = CustomRegexOptions.DURING_ATTACK;
                        break;
                    }

                    case "always": {
                        REGEX_BLACKLIST_MODE = CustomRegexOptions.ALWAYS;
                        break;
                    }

                    case "never": {
                        REGEX_BLACKLIST_MODE = CustomRegexOptions.NEVER;
                        break;
                    }

                    default: {
                        REGEX_BLACKLIST_MODE = CustomRegexOptions.UNKNOWN;
                        break;
                    }
                }

                CUSTOM_REGEXES.addAll(config.getStringList("checks.invalid-name.custom-regexes"));
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }
    }
}
