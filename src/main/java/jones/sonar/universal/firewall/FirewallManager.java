package jones.sonar.universal.firewall;

import jones.sonar.bungee.config.Firewall;
import jones.sonar.bungee.util.Logger;
import jones.sonar.universal.platform.SonarPlatform;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.GeneralException;
import jones.sonar.universal.util.OperatingSystem;
import jones.sonar.universal.util.PerformanceMonitor;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.config.ListenerInfo;

@UtilityClass
public class FirewallManager {
    public void uninstall(final SonarPlatform platform) throws GeneralException {
        if (execute("iptables") != -1) {
            final int port = getPort(platform);

            execute("iptables -D INPUT -p tcp --syn --dport " + port + " -m set --match-set " + Firewall.Values.BLACKLIST_SET_NAME + " src -j DROP");
            execute("iptables -D INPUT -p tcp --syn --dport " + port + " -m connlimit --connlimit-above " + Firewall.Values.MAX_CPS_PER_IP + " -j DROP");
        }

        clear();

        if (execute("ipset") != -1) {
            execute("ipset destroy " + Firewall.Values.BLACKLIST_SET_NAME);
        }
    }

    public void clear() throws GeneralException {
        if (execute("ipset") != -1) {
            execute("ipset flush " + Firewall.Values.BLACKLIST_SET_NAME);
        }
    }

    public void install(final SonarPlatform platform) throws GeneralException {
        if (OperatingSystem.OS_NAME.toLowerCase().contains("wind")) {
            Logger.ERROR.log("The firewall can't run on Windows systems!", "[Sonar-Firewall]");
            return;
        }

        if (execute("iptables") == -1) {
            Logger.INFO.log("IPTables not found; trying to install...", "[Sonar-Firewall]");

            if (execute("sudo apt install iptables -y") == -1) {
                Logger.ERROR.log("Error while setting up iptables (No permission?)", "[Sonar-Firewall]");
                return;
            }
        }

        if (execute("ipset") == -1) {
            Logger.INFO.log("IPSet not found; trying to install...", "[Sonar-Firewall]");

            if (execute("sudo apt install ipset -y") == -1) {
                Logger.ERROR.log("Error while setting up ipset (No permission?)", "[Sonar-Firewall]");
                return;
            }
        }

        uninstall(platform);

        final int port = getPort(platform);

        new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }

            execute("ipset create " + Firewall.Values.BLACKLIST_SET_NAME + " hash:ip timeout " + (Firewall.Values.BLACKLIST_TIMEOUT / 1000L));

            try {
                Thread.sleep(10);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }

            // drop all connections which exceed the connections-per-ip limit
            execute("iptables -A INPUT -p tcp --syn --dport " + port + " -m connlimit --connlimit-above " + Firewall.Values.MAX_CPS_PER_IP + " -j DROP");

            try {
                Thread.sleep(10);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }

            // drop all blacklisted ips from ipset
            execute("iptables -I INPUT -p tcp --syn --dport " + port + " -m set --match-set " + Firewall.Values.BLACKLIST_SET_NAME + " src -j DROP");
        }, "sonar#install_thread").start();

        Logger.INFO.log("The firewall is listening on port " + port + ".", "[Sonar-Firewall]");
    }

    private int getPort(final SonarPlatform platform) {
        switch (platform) {
            default: {
                return 25565;
            }

            case BUNGEE: {
                int port = 25565;

                for (final ListenerInfo listener : SonarBungee.INSTANCE.proxy.getConfig().getListeners()) {
                    port = listener.getHost().getPort();
                }

                return port;
            }
        }
    }

    @SuppressWarnings("all")
    protected int execute(final String command) {
        try {
            final Process process = PerformanceMonitor.RUNTIME.exec(command);

            return process.exitValue();
        } catch (Exception exception) {
            return -2;
        }
    }
}
