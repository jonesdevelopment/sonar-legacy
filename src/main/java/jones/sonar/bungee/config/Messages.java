package jones.sonar.bungee.config;

import jones.sonar.bungee.detection.LoginHandler;
import jones.sonar.bungee.util.ColorUtil;
import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@UtilityClass
public class Messages {

    public Configuration config;

    public final String fileName = "messages.yml";

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
        return ColorUtil.format(message
                .replaceAll("%prefix%", Values.PREFIX)
                .replaceAll("%list%", Values.LIST_SYMBOL)
                .replaceAll("%footer-bar%", Values.FOOTER_BAR)
                .replaceAll("%header-bar%", Values.HEADER_BAR));
    }

    @UtilityClass
    public class Values {
        public String PREFIX, COUNTER_FORMAT, COUNTER_WAITING_FORMAT,
                COUNTER_ENABLED, COUNTER_DISABLED, DISCONNECT_TOO_FAST_RECONNECT,
                DISCONNECT_FIRST_JOIN, DISCONNECT_INVALID_NAME, NO_PERMISSION,
                ONLY_PLAYERS, FOOTER_BAR, HEADER_BAR, HELP_COMMAND_LAYOUT,
                NO_PERMISSION_SUB_COMMAND, UNKNOWN_SUB_COMMAND, RELOADING,
                RELOADED, LIST_SYMBOL, FILTER_SYMBOL_ON, FILTER_SYMBOL_OFF,
                DISCONNECT_PING_BEFORE_JOIN, MONITOR_GRAPH_FILLED_BAD,
                DISCONNECT_TOO_MANY_ONLINE, DISCONNECT_QUEUED, DISCONNECT_ATTACK,
                PING_SPECIFY, PING_OTHER, PING, BLACKLIST_EMPTY, BLACKLIST_INVALID_IP,
                BLACKLIST_NOT, BLACKLIST_ALREADY, BLACKLIST_ADD_PLAYER, BLACKLIST_ADD_IP,
                BLACKLIST_REMOVE, WHITELIST_EMPTY, WHITELIST_INVALID_IP, WHITELIST_ALREADY,
                WHITELIST_NOT, WHITELIST_ADD_PLAYER, WHITELIST_ADD_IP, WHITELIST_REMOVE,
                BLACKLIST_SIZE, WHITELIST_SIZE, COMMAND_USAGE, BLACKLIST_CLEAR_ATTACK,
                WHITELIST_CLEAR, BLACKLIST_CLEAR, RELOAD_WAIT, COUNTER_ENABLED_OTHER,
                COUNTER_DISABLED_OTHER, NOTIFY_ENABLED, NOTIFY_DISABLED, NOTIFY_ENABLED_OTHER,
                NOTIFY_DISABLED_OTHER, NOTIFY_FORMAT, PEAK_FORMAT_CPS, PEAK_FORMAT_IPS,
                RELOAD_CONFIRMATION_ATTACK, DISCONNECT_BOT_BEHAVIOUR, DISCONNECT_BOT_DETECTION,
                NO_PERMISSION_SUB_COMMAND_ANY, VERIFICATION_PURGING, VERIFICATION_PURGE_COMPLETE,
                VERIFICATION_SIZE, VERIFICATION_CLEAR, VERIFICATION_PURGE_NONE, VERIFICATION_EMPTY,
                MONITOR_ENABLED, MONITOR_DISABLED, MONITOR_ENABLED_OTHER, MONITOR_DISABLED_OTHER,
                MONITOR_FORMAT, MONITOR_GRAPH_FILLED_SAFE, MONITOR_GRAPH_FILLED_UNSAFE,
                MONITOR_GRAPH_UNFILLED, MONITOR_GRAPH_FILL_SYMBOL, MONITOR_GRAPH_UP, MONITOR_GRAPH_DOWN,
                MONITOR_GRAPH_STATIC, MONITOR_UNSUPPORTED, MONITOR_UNSUPPORTED_OTHER, BLACKLIST_AUTO_CLEAR,
                DISCONNECT_VPN_OR_PROXY, TEMP_BLACKLISTED;

        public int NOTIFY_DELAY, PEAK_DELAY, PEAK_RESET_DELAY, GRAPH_SYMBOL_COUNT, MONITOR_REFRESH_DELAY,
                BLACKLIST_CLEAR_TIME;

        public boolean ENABLE_COUNTER_WAITING_FORMAT, ENABLE_PEAK, COLOR_ACTION_BAR_COUNTER;

        public boolean load() {
            try {
                // general
                PREFIX = ColorUtil.format(config.getString("prefix", "&e&lSonar &7» &f"));
                LIST_SYMBOL = config.getString("commands.listing", "▪");
                FOOTER_BAR = ColorUtil.format(config.getString("footer-bar"));
                HEADER_BAR = ColorUtil.format(config.getString("header-bar"));

                if (PREFIX.length() > 32) PREFIX = PREFIX.substring(0, 32);

                NO_PERMISSION = format(config.getString("no-permission"));
                NO_PERMISSION_SUB_COMMAND = format(config.getString("no-permission-sub"));
                NO_PERMISSION_SUB_COMMAND_ANY = format(config.getString("no-permission-sub-any"));
                UNKNOWN_SUB_COMMAND = format(config.getString("unknown-sub-command"));
                ONLY_PLAYERS = format(config.getString("only-players"));

                // automatic
                BLACKLIST_CLEAR_TIME = Math.max(config.getInt("notifications.automatic.blacklist-clear-delay"), 10000);
                BLACKLIST_AUTO_CLEAR = format(config.getString("notifications.automatic.blacklist-clear"));

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

                VERIFICATION_PURGING = format(config.getString("commands.verification.purging"));
                VERIFICATION_PURGE_COMPLETE = format(config.getString("commands.verification.purge"));
                VERIFICATION_PURGE_NONE = format(config.getString("commands.verification.purge-none"));
                VERIFICATION_CLEAR = format(config.getString("commands.verification.clear"));
                VERIFICATION_SIZE = format(config.getString("commands.verification.size"));
                VERIFICATION_EMPTY = format(config.getString("commands.verification.empty"));

                MONITOR_ENABLED = format(config.getString("commands.monitor.enabled"));
                MONITOR_DISABLED = format(config.getString("commands.monitor.disabled"));
                MONITOR_ENABLED_OTHER = format(config.getString("commands.monitor.enabled-other"));
                MONITOR_DISABLED_OTHER = format(config.getString("commands.monitor.disabled-other"));
                MONITOR_UNSUPPORTED = format(config.getString("commands.monitor.unsupported"));
                MONITOR_UNSUPPORTED_OTHER = format(config.getString("commands.monitor.unsupported-other"));

                NOTIFY_ENABLED = format(config.getString("commands.notify.enabled"));
                NOTIFY_DISABLED = format(config.getString("commands.notify.disabled"));
                NOTIFY_ENABLED_OTHER = format(config.getString("commands.notify.enabled-other"));
                NOTIFY_DISABLED_OTHER = format(config.getString("commands.notify.disabled-other"));

                COUNTER_ENABLED = format(config.getString("commands.verbose.enabled"));
                COUNTER_DISABLED = format(config.getString("commands.verbose.disabled"));
                COUNTER_ENABLED_OTHER = format(config.getString("commands.verbose.enabled-other"));
                COUNTER_DISABLED_OTHER = format(config.getString("commands.verbose.disabled-other"));

                if (LIST_SYMBOL.length() > 2) LIST_SYMBOL = LIST_SYMBOL.substring(0, 2);
                else if (LIST_SYMBOL.isEmpty()) LIST_SYMBOL = "▪";

                // notifications
                NOTIFY_DELAY = Math.max(config.getInt("notifications.chat.delay", 25000), 100);
                NOTIFY_FORMAT = format(fromList(config.getStringList("notifications.chat.format")));

                // counter
                COUNTER_FORMAT = format(config.getString("notifications.action-bar.format"));
                COUNTER_WAITING_FORMAT = format(config.getString("notifications.action-bar.waiting"));
                ENABLE_COUNTER_WAITING_FORMAT = config.getBoolean("notifications.action-bar.enable-waiting-message");
                COLOR_ACTION_BAR_COUNTER = config.getBoolean("notifications.action-bar.colorize-number-counts");
                FILTER_SYMBOL_ON = format(config.getString("notifications.action-bar.filter-enabled-symbol"));
                FILTER_SYMBOL_OFF = format(config.getString("notifications.action-bar.filter-disabled-symbol"));

                // monitor
                MONITOR_FORMAT = format(config.getString("notifications.boss-bar.format", ""));
                MONITOR_GRAPH_FILL_SYMBOL = config.getString("notifications.boss-bar.fill-symbol", "|");
                GRAPH_SYMBOL_COUNT = config.getInt("notifications.boss-bar.fill-symbol-count", 25);
                MONITOR_GRAPH_STATIC = ColorUtil.format(config.getString("notifications.boss-bar.static-symbol"));
                MONITOR_GRAPH_UP = ColorUtil.format(config.getString("notifications.boss-bar.going-up-symbol"));
                MONITOR_GRAPH_DOWN = ColorUtil.format(config.getString("notifications.boss-bar.going-down-symbol"));
                MONITOR_REFRESH_DELAY = Math.max(config.getInt("notifications.boss-bar.delay", 1000), 800);
                MONITOR_GRAPH_UNFILLED = ColorUtil.format(config.getString("notifications.boss-bar.unfilled-color", "&7"));
                MONITOR_GRAPH_FILLED_BAD = ColorUtil.format(config.getString("notifications.boss-bar.filled-color-bad", "&c"));
                MONITOR_GRAPH_FILLED_SAFE = ColorUtil.format(config.getString("notifications.boss-bar.filled-color-safe", "&a"));
                MONITOR_GRAPH_FILLED_UNSAFE = ColorUtil.format(config.getString("notifications.boss-bar.filled-color-unsafe", "&e"));

                if (MONITOR_GRAPH_FILL_SYMBOL.length() > 1) {
                    MONITOR_GRAPH_FILL_SYMBOL = MONITOR_GRAPH_FILL_SYMBOL.substring(0, 1);
                } else if (MONITOR_GRAPH_FILL_SYMBOL.isEmpty()) {
                    MONITOR_GRAPH_FILL_SYMBOL = "|";
                }

                // peak
                ENABLE_PEAK = config.getBoolean("notifications.peak.enabled", true);
                PEAK_DELAY = Math.max(config.getInt("notifications.peak.delay", 1500), 100);
                PEAK_RESET_DELAY = Math.max(config.getInt("notifications.peak.reset-delay", 8000), 1000);
                PEAK_FORMAT_CPS = format(config.getString("notifications.peak.format-new-cps", "not found"));
                PEAK_FORMAT_IPS = format(config.getString("notifications.peak.format-new-ips", "not found"));

                // disconnect messages
                DISCONNECT_TOO_FAST_RECONNECT = format(fromList(config.getStringList("disconnect.reconnect-check.too-fast-reconnect"))
                        .replaceAll("%milliseconds%", SonarBungee.INSTANCE.FORMAT.format(Config.Values.REJOIN_DELAY))
                        .replaceAll("%seconds%", String.format("%.2f", Config.Values.REJOIN_DELAY / 1000D)
                                .replaceAll("\\.00", "")));
                DISCONNECT_FIRST_JOIN = format(fromList(config.getStringList("disconnect.reconnect-check.first-join")));
                DISCONNECT_INVALID_NAME = format(fromList(config.getStringList("disconnect.invalid-name")));
                DISCONNECT_TOO_MANY_ONLINE = format(fromList(config.getStringList("disconnect.too-many-accounts-per-ip"))
                        .replaceAll("%max%", "" + Config.Values.MAXIMUM_ONLINE_PER_IP));
                DISCONNECT_QUEUED = format(fromList(config.getStringList("disconnect.currently-in-queue")));
                DISCONNECT_ATTACK = format(fromList(config.getStringList("disconnect.verification-during-attack")));
                DISCONNECT_BOT_BEHAVIOUR = format(fromList(config.getStringList("disconnect.suspicious-behaviour")));
                DISCONNECT_BOT_DETECTION = format(fromList(config.getStringList("disconnect.bot-detection")));
                DISCONNECT_PING_BEFORE_JOIN = format(fromList(config.getStringList("disconnect.ping-before-join")));
                DISCONNECT_VPN_OR_PROXY = format(fromList(config.getStringList("disconnect.vpn-or-proxy")));
                TEMP_BLACKLISTED = format(fromList(config.getStringList("disconnect.temporarily-blacklisted")));
                LoginHandler.updateDetectionCache();
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }
    }
}
