package jones.sonar.bungee.config;

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
        public int BLACKLIST_TIMEOUT, BLACKLIST_DELAY, MAX_CPS_PER_IP, BLACKLIST_CACHE_LIMIT;

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
                BLACKLIST_CACHE_LIMIT = Math.max(Math.min(config.getInt("firewall.blacklist-cache-limit", 5000), 100000), 100);
                return true;
            } catch (final Exception exception) {
                return false;
            }
        }
    }
}
