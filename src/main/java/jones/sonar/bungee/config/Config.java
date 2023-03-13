package jones.sonar.bungee.config;

import jones.sonar.bungee.util.ColorUtil;
import jones.sonar.universal.config.options.CustomRegexOptions;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.Sensibility;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@UtilityClass
public class Config {

    public Configuration config;

    public final String fileName = "config.yml";

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
                REJOIN_DELAY, MINIMUM_KEEP_ALIVE_TICK, MINIMUM_JOINS_PER_SECOND,
                ACTION_BAR_COUNTER_DELAY, MAXIMUM_ONLINE_PER_IP,
                MAXIMUM_QUEUE_POLL_RATE, MAX_NAME_LENGTH, WEBHOOK_DELAY,
                WEBHOOK_COLOR_R, WEBHOOK_COLOR_G, WEBHOOK_COLOR_B,
                QUEUE_POLL_RATE, ANTI_PROXY_TIMEOUT, MAX_STATUS_DURING_ATTACK,
                MAXIMUM_HANDSHAKES_PER_IP_SEC, MAXIMUM_HANDSHAKES_PER_IP_SEC_BLACKLIST,
                TCP_FAST_OPEN_MODE;

        public boolean CLIENT_CONNECT_EVENT, ENABLE_RECONNECT_CHECK,
                ENABLE_INVALID_NAME_CHECK, ENABLE_FIRST_JOIN, CACHE_MOTDS,
                ALLOW_PROXY_PROTOCOL, ALLOW_PING_PASS_THROUGH, WEBHOOK_ENABLED,
                LOG_CONNECTIONS, AUTOMATICALLY_REMOVE_BOTS_FROM_VERIFICATION,
                PING_BEFORE_JOIN, ENABLE_PROXY_CHECK, FORCE_PUBLIC_KEY,
                PING_NEEDED_DURING_ATTACK, ENABLE_TCP_FAST_OPEN, USE_EMBED_WEBHOOKS;

        public CustomRegexOptions REGEX_BLACKLIST_MODE = CustomRegexOptions.UNKNOWN,
                REGEX_CHECK_MODE = CustomRegexOptions.UNKNOWN;

        public String NAME_VALIDATION_REGEX, SERVER_BRAND, WEBHOOK_URL,
                WEBHOOK_FORMAT, WEBHOOK_TITLE, FAKE_SERVER_CLIENT_BRAND,
                WEBHOOK_USERNAME, WEBHOOK_PING;

        public List<String> CUSTOM_REGEXES = new CopyOnWriteArrayList<>();

        public boolean load() {
            try {
                // reset the list in order to prevent duplicates
                CUSTOM_REGEXES.clear();

                // anti proxy
                ENABLE_PROXY_CHECK = config.getBoolean("anti-proxy.enabled", false);
                ANTI_PROXY_TIMEOUT = config.getInt("anti-proxy.timeout", 4000);

                // general
                CLIENT_CONNECT_EVENT = config.getBoolean("general.use-client-connect-event", false);
                ALLOW_PROXY_PROTOCOL = config.getBoolean("general.use-proxy-protocol", false);
                MAX_PACKET_INDEX = config.getInt("general.maximum-packet-index", 1024);
                MAX_PACKET_BYTES = config.getInt("general.maximum-packet-bytes", 2048);
                MAX_PACKET_CAPACITY = config.getInt("general.maximum-packet-capacity", 4096);
                MINIMUM_JOINS_PER_SECOND = config.getInt("general.minimum-joins-per-second", 6);
                FORCE_PUBLIC_KEY = config.getBoolean("general.force-valid-public-key", true);
                MAX_STATUS_DURING_ATTACK = config.getInt("general.maximum-status-requests-for-attack", 256);
                MAXIMUM_HANDSHAKES_PER_IP_SEC = config.getInt("general.maximum-handshakes-per-ip", 3);
                MAXIMUM_HANDSHAKES_PER_IP_SEC_BLACKLIST = config.getInt("general.maximum-handshakes-per-ip-blacklist", 9);
                PING_NEEDED_DURING_ATTACK = config.getBoolean("general.ping-needed-during-attack", true);
                ENABLE_TCP_FAST_OPEN = config.getBoolean("general.enable-tcp-fast-open", true);
                TCP_FAST_OPEN_MODE = config.getInt("general.tcp-fast-open-mode", Math.max(Math.min(TCP_FAST_OPEN_MODE, 4), 1));

                Sensibility.minJoinsPerSecond = MINIMUM_JOINS_PER_SECOND;

                SERVER_BRAND = ColorUtil.format(config.getString("general.fake-server-brand", "Protected"));
                CACHE_MOTDS = config.getBoolean("general.cache-incoming-motd-requests", true);
                ACTION_BAR_COUNTER_DELAY = Math.max(Math.min(config.getInt("general.action-bar-counter-delay", 80), 1000), 10);
                MAXIMUM_ONLINE_PER_IP = Math.max(config.getInt("general.maximum-online-per-ip", 2), 1);
                MAXIMUM_QUEUE_POLL_RATE = Math.max(config.getInt("general.maximum-queue-poll-rate", 2000), 1);
                QUEUE_POLL_RATE = Math.max(Math.min(config.getInt("general.queue-poll-rate", 1000), MAXIMUM_QUEUE_POLL_RATE), 1);
                ALLOW_PING_PASS_THROUGH = config.getBoolean("general.allow-forced-host-ping", false);
                LOG_CONNECTIONS = config.getBoolean("general.log-connections", false);
                MINIMUM_KEEP_ALIVE_TICK = Math.max(config.getInt("general.minimum-tick-to-auto-whitelist", 3), 0);
                FAKE_SERVER_CLIENT_BRAND = config.getString("general.fake-server-client-brand", "%proxy% -> %backend%");
                AUTOMATICALLY_REMOVE_BOTS_FROM_VERIFICATION = config.getBoolean("general.automatically-remove-bots-from-verifying", true);

                // webhook
                WEBHOOK_ENABLED = config.getBoolean("notifications.webhook.enabled", false);
                WEBHOOK_DELAY = config.getInt("notifications.webhook.delay", 240000);
                WEBHOOK_FORMAT = Messages.format(Messages.fromList(config.getStringList("notifications.webhook.format"), "\\n"));
                WEBHOOK_TITLE = Messages.format(config.getString("notifications.webhook.title"));
                WEBHOOK_URL = Messages.format(config.getString("notifications.webhook.url", ""));
                WEBHOOK_COLOR_R = Math.min(Math.max(config.getInt("notifications.webhook.embed.color.r", 255), 0), 255);
                WEBHOOK_COLOR_G = Math.min(Math.max(config.getInt("notifications.webhook.embed.color.g", 50), 0), 255);
                WEBHOOK_COLOR_B = Math.min(Math.max(config.getInt("notifications.webhook.embed.color.b", 0), 0), 255);
                USE_EMBED_WEBHOOKS = config.getBoolean("notifications.webhook.embed.enabled", true);
                WEBHOOK_USERNAME = config.getString("notifications.webhook.username", "Sonar");
                WEBHOOK_PING = config.getString("notifications.webhook.ping");

                // checks
                ENABLE_RECONNECT_CHECK = config.getBoolean("checks.reconnect.enabled", true);
                ENABLE_FIRST_JOIN = config.getBoolean("checks.reconnect.first-join", true);
                REJOIN_DELAY = config.getInt("checks.reconnect.rejoin-delay", 1000);
                PING_BEFORE_JOIN = config.getBoolean("checks.reconnect.ping-before-join");

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
