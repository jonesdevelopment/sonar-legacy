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
import java.util.List;

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

    private String fromList(final List<String> list) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(list.get(i)).append(i < list.size() - 1 ? "\n" : "");
        }

        return stringBuilder.toString();
    }

    private String format(final String message) {
        return ColorUtil.format(message)
                .replaceAll("%prefix%", Values.PREFIX);
    }

    @UtilityClass
    public class Values {
        public String PREFIX, COUNTER_FORMAT, COUNTER_WAITING_FORMAT,
                COUNTER_ENABLED, COUNTER_DISABLED, DISCONNECT_TOO_FAST_RECONNECT,
                DISCONNECT_FIRST_JOIN, DISCONNECT_INVALID_NAME, NO_PERMISSION,
                ONLY_PLAYERS, FOOTER_BAR, HEADER_BAR, HELP_COMMAND_LAYOUT,
                NO_PERMISSION_SUB_COMMAND, UNKNOWN_SUB_COMMAND;

        public boolean ENABLE_COUNTER_WAITING_FORMAT;

        public boolean load() {
            try {
                // general
                PREFIX = ColorUtil.format(config.getString("prefix", "&e&lSonar &7» &f"));
                NO_PERMISSION = format(config.getString("no-permission", "&cNo permission!"));
                NO_PERMISSION_SUB_COMMAND = format(config.getString("no-permission-sub", "&cNo permission!"));
                UNKNOWN_SUB_COMMAND = format(config.getString("unknown-sub-command", "&cUnknown sub-command."));
                ONLY_PLAYERS = format(config.getString("only-players", "&cOnly players!"));
                FOOTER_BAR = format(config.getString("footer-bar", "&7---&r"));
                HEADER_BAR = format(config.getString("header-bar", "&7---&r"));

                // commands
                HELP_COMMAND_LAYOUT = ColorUtil.format(config.getString("commands.help.layout", "» /ab %command% - %description%"));

                // counter
                COUNTER_ENABLED = format(config.getString("counter.action-bar.enabled"));
                COUNTER_DISABLED = format(config.getString("counter.action-bar.disabled"));
                COUNTER_FORMAT = format(config.getString("counter.action-bar.format"));
                COUNTER_WAITING_FORMAT = format(config.getString("counter.action-bar.waiting"));
                ENABLE_COUNTER_WAITING_FORMAT = config.getBoolean("counter.action-bar.enable-waiting-message");

                // disconnect messages
                DISCONNECT_TOO_FAST_RECONNECT = format(fromList(config.getStringList("disconnect.reconnect-check.too-fast-reconnect")));
                DISCONNECT_FIRST_JOIN = format(fromList(config.getStringList("disconnect.reconnect-check.first-join")));
                DISCONNECT_INVALID_NAME = format(fromList(config.getStringList("disconnect.invalid-name")));
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }
    }
}
