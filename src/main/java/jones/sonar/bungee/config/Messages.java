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

    private String fromList(final List<String> list) {
        return fromList(list, "\n");
    }

    public String fromList(final List<String> list, final String lineSeparator) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            stringBuilder.append(list.get(i)).append(i < list.size() - 1 ? lineSeparator : "");
        }

        return stringBuilder.toString();
    }

    public String format(final String message) {
        return ColorUtil.format(message)
                .replaceAll("%prefix%", Values.PREFIX)
                .replaceAll("%list%", Values.LIST_SYMBOL)
                .replaceAll("%footer-bar%", Values.FOOTER_BAR)
                .replaceAll("%header-bar%", Values.HEADER_BAR);
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
                WHITELIST_NOT, WHITELIST_ADD_PLAYER, WHITELIST_ADD_IP, WHITELIST_REMOVE,
                BLACKLIST_SIZE, WHITELIST_SIZE, COMMAND_USAGE, BLACKLIST_CLEAR_ATTACK,
                WHITELIST_CLEAR, BLACKLIST_CLEAR, RELOAD_WAIT, COUNTER_ENABLED_OTHER,
                COUNTER_DISABLED_OTHER, NOTIFY_ENABLED, NOTIFY_DISABLED, NOTIFY_ENABLED_OTHER,
                NOTIFY_DISABLED_OTHER, NOTIFY_FORMAT, PEAK_FORMAT_CPS, PEAK_FORMAT_IPS,
                RELOAD_CONFIRMATION_ATTACK;

        public int NOTIFY_DELAY, PEAK_DELAY, PEAK_RESET_DELAY;

        public boolean ENABLE_COUNTER_WAITING_FORMAT, ENABLE_PEAK;

        public boolean load() {
            try {
                // general
                PREFIX = ColorUtil.format(config.getString("prefix", "&e&lSonar &7» &f"));
                LIST_SYMBOL = config.getString("commands.listing", "▪");
                FOOTER_BAR = ColorUtil.format(config.getString("footer-bar", "&7---&r"));
                HEADER_BAR = ColorUtil.format(config.getString("header-bar", "&7---&r"));

                if (PREFIX.length() > 32) PREFIX = PREFIX.substring(0, 32);

                NO_PERMISSION = format(config.getString("no-permission"));
                NO_PERMISSION_SUB_COMMAND = format(config.getString("no-permission-sub"));
                UNKNOWN_SUB_COMMAND = format(config.getString("unknown-sub-command"));
                ONLY_PLAYERS = format(config.getString("only-players"));

                // commands
                HELP_COMMAND_LAYOUT = format(config.getString("commands.help.layout"));
                COMMAND_USAGE = format(config.getString("commands.help.usage"));

                RELOAD_WAIT = format(config.getString("commands.reload.wait"));
                RELOADING = format(config.getString("commands.reload.reloading"));
                RELOADED = format(config.getString("commands.reload.reloaded"));
                RELOAD_CONFIRMATION_ATTACK = format(config.getString("commands.reload.confirmation"));

                PING = format(config.getString("commands.ping.you"));
                PING_SPECIFY = format(config.getString("commands.ping.specify"));
                PING_OTHER = format(config.getString("commands.ping.other"));

                WHITELIST_CLEAR = format(config.getString("commands.whitelist.clear"));
                WHITELIST_SIZE = format(config.getString("commands.whitelist.size"));
                WHITELIST_EMPTY = format(config.getString("commands.whitelist.empty"));
                WHITELIST_INVALID_IP = format(config.getString("commands.whitelist.invalid"));
                WHITELIST_NOT = format(config.getString("commands.whitelist.not"));
                WHITELIST_ALREADY = format(config.getString("commands.whitelist.already"));
                WHITELIST_ADD_PLAYER = format(config.getString("commands.whitelist.add-player"));
                WHITELIST_ADD_IP = format(config.getString("commands.whitelist.add-ip"));
                WHITELIST_REMOVE = format(config.getString("commands.whitelist.remove"));

                BLACKLIST_CLEAR = format(config.getString("commands.blacklist.clear"));
                BLACKLIST_CLEAR_ATTACK = format(config.getString("commands.blacklist.attack-clear"));
                BLACKLIST_SIZE = format(config.getString("commands.blacklist.size"));
                BLACKLIST_EMPTY = format(config.getString("commands.blacklist.empty"));
                BLACKLIST_INVALID_IP = format(config.getString("commands.blacklist.invalid"));
                BLACKLIST_NOT = format(config.getString("commands.blacklist.not"));
                BLACKLIST_ALREADY = format(config.getString("commands.blacklist.already"));
                BLACKLIST_ADD_PLAYER = format(config.getString("commands.blacklist.add-player"));
                BLACKLIST_ADD_IP = format(config.getString("commands.blacklist.add-ip"));
                BLACKLIST_REMOVE = format(config.getString("commands.blacklist.remove"));

                if (LIST_SYMBOL.length() > 2) LIST_SYMBOL = LIST_SYMBOL.substring(0, 2);
                else if (LIST_SYMBOL.isEmpty()) LIST_SYMBOL = "▪";

                // notifications
                NOTIFY_DELAY = Math.max(config.getInt("notifications.chat.delay", 25000), 100);
                NOTIFY_FORMAT = format(fromList(config.getStringList("notifications.chat.format")));
                NOTIFY_ENABLED = format(config.getString("notifications.chat.enabled"));
                NOTIFY_DISABLED = format(config.getString("notifications.chat.disabled"));
                NOTIFY_ENABLED_OTHER = format(config.getString("notifications.chat.enabled-other"));
                NOTIFY_DISABLED_OTHER = format(config.getString("notifications.chat.disabled-other"));

                // counter
                COUNTER_ENABLED = format(config.getString("notifications.action-bar.enabled"));
                COUNTER_DISABLED = format(config.getString("notifications.action-bar.disabled"));
                COUNTER_ENABLED_OTHER = format(config.getString("notifications.action-bar.enabled-other"));
                COUNTER_DISABLED_OTHER = format(config.getString("notifications.action-bar.disabled-other"));
                COUNTER_FORMAT = format(config.getString("notifications.action-bar.format"));
                COUNTER_WAITING_FORMAT = format(config.getString("notifications.action-bar.waiting"));
                ENABLE_COUNTER_WAITING_FORMAT = config.getBoolean("notifications.action-bar.enable-waiting-message");
                FILTER_SYMBOL_ON = format(config.getString("notifications.action-bar.filter-enabled-symbol"));
                FILTER_SYMBOL_OFF = format(config.getString("notifications.action-bar.filter-disabled-symbol"));

                // peak
                ENABLE_PEAK = config.getBoolean("notifications.peak.enabled", true);
                PEAK_DELAY = Math.max(config.getInt("notifications.peak.delay", 1500), 100);
                PEAK_RESET_DELAY = Math.max(config.getInt("notifications.peak.reset-delay", 8000), 1000);
                PEAK_FORMAT_CPS = format(config.getString("notifications.peak.format-new-cps"));
                PEAK_FORMAT_IPS = format(config.getString("notifications.peak.format-new-ips"));

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
