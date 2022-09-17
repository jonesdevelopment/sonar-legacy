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
import jones.sonar.universal.config.yaml.Configuration;
import jones.sonar.universal.config.yaml.ConfigurationProvider;
import jones.sonar.universal.config.yaml.YamlConfiguration;
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
                .replaceAll("%prefix%", Values.PREFIX)
                .replaceAll("%list%", Values.LIST_SYMBOL);
    }

    @UtilityClass
    public class Values {
        public String PREFIX, COUNTER_FORMAT, COUNTER_WAITING_FORMAT,
                COUNTER_ENABLED, COUNTER_DISABLED, DISCONNECT_TOO_FAST_RECONNECT,
                DISCONNECT_FIRST_JOIN, DISCONNECT_INVALID_NAME, NO_PERMISSION,
                ONLY_PLAYERS, FOOTER_BAR, HEADER_BAR, HELP_COMMAND_LAYOUT,
                NO_PERMISSION_SUB_COMMAND, UNKNOWN_SUB_COMMAND, RELOADING,
                RELOADED, LIST_SYMBOL, FILTER_SYMBOL_ON, FILTER_SYMBOL_OFF,
                DISCONNECT_UNSUPPORTED_VERSION, DISCONNECT_ALREADY_CONNECTED,
                DISCONNECT_TOO_MANY_ONLINE, DISCONNECT_QUEUED, DISCONNECT_ATTACK,
                PING_SPECIFY, PING_OTHER, PING, BLACKLIST_EMPTY, BLACKLIST_INVALID_IP,
                BLACKLIST_NOT, BLACKLIST_ALREADY, BLACKLIST_ADD_PLAYER, BLACKLIST_ADD_IP,
                BLACKLIST_REMOVE, WHITELIST_EMPTY, WHITELIST_INVALID_IP, WHITELIST_ALREADY,
                WHITELIST_NOT, WHITELIST_ADD_PLAYER, WHITELIST_ADD_IP, WHITELIST_REMOVE;

        public boolean ENABLE_COUNTER_WAITING_FORMAT;

        public boolean load() {
            try {
                // general
                PREFIX = ColorUtil.format(config.getString("prefix", "&e&lSonar &7» &f"));
                LIST_SYMBOL = config.getString("commands.listing", "▪");

                if (PREFIX.length() > 32) PREFIX = PREFIX.substring(0, 32);

                NO_PERMISSION = format(config.getString("no-permission", "&cNo permission!"));
                NO_PERMISSION_SUB_COMMAND = format(config.getString("no-permission-sub", "&cNo permission!"));
                UNKNOWN_SUB_COMMAND = format(config.getString("unknown-sub-command", "&cUnknown sub-command."));
                ONLY_PLAYERS = format(config.getString("only-players", "&cOnly players!"));
                FOOTER_BAR = format(config.getString("footer-bar", "&7---&r"));
                HEADER_BAR = format(config.getString("header-bar", "&7---&r"));

                // commands
                HELP_COMMAND_LAYOUT = format(config.getString("commands.help.layout", "» /ab %command% - %description%"));
                RELOADING = format(config.getString("commands.reload.reloading", "Reloading..."));
                RELOADED = format(config.getString("commands.reload.reloaded", "Reloaded in %time% ms"));
                PING = format(config.getString("commands.ping.you"));
                PING_SPECIFY = format(config.getString("commands.ping.specify"));
                PING_OTHER = format(config.getString("commands.ping.other"));

                WHITELIST_EMPTY = format(config.getString("commands.whitelist.empty"));
                WHITELIST_INVALID_IP = format(config.getString("commands.whitelist.invalid"));
                WHITELIST_NOT = format(config.getString("commands.whitelist.not"));
                WHITELIST_ALREADY = format(config.getString("commands.whitelist.already"));
                WHITELIST_ADD_PLAYER = format(config.getString("commands.whitelist.add-player"));
                WHITELIST_ADD_IP = format(config.getString("commands.whitelist.add-ip"));
                WHITELIST_REMOVE = format(config.getString("commands.whitelist.remove"));

                BLACKLIST_EMPTY = format(config.getString("commands.blacklist.empty"));
                BLACKLIST_INVALID_IP = format(config.getString("commands.blacklist.invalid"));
                BLACKLIST_NOT = format(config.getString("commands.blacklist.not"));
                BLACKLIST_ALREADY = format(config.getString("commands.blacklist.already"));
                BLACKLIST_ADD_PLAYER = format(config.getString("commands.blacklist.add-player"));
                BLACKLIST_ADD_IP = format(config.getString("commands.blacklist.add-ip"));
                BLACKLIST_REMOVE = format(config.getString("commands.blacklist.remove"));

                if (LIST_SYMBOL.length() > 2) LIST_SYMBOL = LIST_SYMBOL.substring(0, 2);
                else if (LIST_SYMBOL.isEmpty()) LIST_SYMBOL = "▪";

                // counter
                COUNTER_ENABLED = format(config.getString("counter.action-bar.enabled"));
                COUNTER_DISABLED = format(config.getString("counter.action-bar.disabled"));
                COUNTER_FORMAT = format(config.getString("counter.action-bar.format"));
                COUNTER_WAITING_FORMAT = format(config.getString("counter.action-bar.waiting"));
                ENABLE_COUNTER_WAITING_FORMAT = config.getBoolean("counter.action-bar.enable-waiting-message");
                FILTER_SYMBOL_ON = format(config.getString("counter.action-bar.filter-enabled-symbol"));
                FILTER_SYMBOL_OFF = format(config.getString("counter.action-bar.filter-disabled-symbol"));

                // disconnect messages
                DISCONNECT_TOO_FAST_RECONNECT = format(fromList(config.getStringList("disconnect.reconnect-check.too-fast-reconnect"))
                        .replaceAll("%seconds%", String.format("%.2f", Config.Values.REJOIN_DELAY / 1000D)
                                .replaceAll("\\.00", "")));
                DISCONNECT_FIRST_JOIN = format(fromList(config.getStringList("disconnect.reconnect-check.first-join")));
                DISCONNECT_INVALID_NAME = format(fromList(config.getStringList("disconnect.invalid-name")));
                DISCONNECT_UNSUPPORTED_VERSION = format(fromList(config.getStringList("disconnect.unsupported-version")));
                DISCONNECT_ALREADY_CONNECTED = format(fromList(config.getStringList("disconnect.already-connected")));
                DISCONNECT_TOO_MANY_ONLINE = format(fromList(config.getStringList("disconnect.too-many-accounts-per-ip"))
                        .replaceAll("%max%", "" + Config.Values.MAXIMUM_ONLINE_PER_IP));
                DISCONNECT_QUEUED = format(fromList(config.getStringList("disconnect.currently-in-queue")));
                DISCONNECT_ATTACK = format(fromList(config.getStringList("disconnect.verification-during-attack")));
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }
    }
}
