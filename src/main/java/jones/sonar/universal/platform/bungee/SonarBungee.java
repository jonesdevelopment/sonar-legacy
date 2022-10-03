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

package jones.sonar.universal.platform.bungee;

import io.netty.util.ResourceLeakDetector;
import jones.sonar.api.event.bungee.SonarReloadEvent;
import jones.sonar.bungee.SonarBungeePlugin;
import jones.sonar.bungee.caching.CacheThread;
import jones.sonar.bungee.command.SonarCommand;
import jones.sonar.bungee.command.manager.CommandManager;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Firewall;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.filter.ConsoleFilter;
import jones.sonar.bungee.monitor.MonitorThread;
import jones.sonar.bungee.network.BungeeInterceptor;
import jones.sonar.bungee.notification.counter.ActionBar;
import jones.sonar.bungee.peak.PeakThread;
import jones.sonar.bungee.util.Reflection;
import jones.sonar.bungee.util.logging.Logger;
import jones.sonar.universal.firewall.FirewallManager;
import jones.sonar.universal.firewall.FirewallThread;
import jones.sonar.universal.license.response.LicenseResponse;
import jones.sonar.universal.license.response.WebResponse;
import jones.sonar.universal.peak.PeakCalculator;
import jones.sonar.universal.platform.SonarPlatform;
import jones.sonar.universal.queue.QueueThread;
import jones.sonar.universal.util.AssertionHelper;
import jones.sonar.universal.util.FastException;
import jones.sonar.universal.util.PerformanceMonitor;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Event;

import java.io.File;
import java.text.DecimalFormat;

public enum SonarBungee implements SonarBungeePlatform {

    INSTANCE;

    @Getter
    private SonarBungeePlugin plugin;

    public final ProxyServer proxy = ProxyServer.getInstance();

    public final DecimalFormat FORMAT = new DecimalFormat("#,###");

    public final FastException EXCEPTION = new FastException();

    public final PeakCalculator cpsPeakCalculator = new PeakCalculator(),
            ipSecPeakCalculator = new PeakCalculator();

    public final SonarPlatform platform = SonarPlatform.BUNGEE;

    public boolean isReverseProxy = false;

    public boolean running = false;

    public int JAVA_VERSION = 0;

    public void onLoad(final SonarBungeePlugin plugin) {
        AssertionHelper.check(plugin != null, "Error loading Sonar!");

        this.plugin = plugin;

        createDataFolder();

        // cached license response
        LicenseResponse licenseResponse;

        // cached exception, if something goes wrong
        Exception unhandledException = null;

        try {

            // try to load the license
            licenseResponse = checkLicense();

            running = licenseResponse.response == WebResponse.SUCCESS;
        } catch (Exception exception) {

            // set to null if something goes wrong
            licenseResponse = null;

            // cache exception
            unhandledException = exception;
        }

        // something did go wrong or the license is invalid
        if (!running) {
            Logger.INFO.log("§7§m«-----------------------------------------»§r");
            Logger.INFO.log(" ");
            Logger.INFO.log(" §cSonar couldn't start because of following error:");
            Logger.INFO.log(" ");

            // invalid license
            if (licenseResponse != null) {
                Logger.INFO.log(" §e" + licenseResponse.errorMessage);
                Logger.INFO.log(" §7Your hardware id: §f" + licenseResponse.license.hardwareID.encryptedInformation);
            }

            // general exception
            else {

                // print the stack trace
                Logger.INFO.log(" §cException: §e" + unhandledException.getMessage());
            }

            Logger.INFO.log(" ");
            Logger.INFO.log(" §7Support Discord:§f https://discord.jonesdev.xyz/");
            Logger.INFO.log(" ");
            Logger.INFO.log("§7§m«-----------------------------------------»§r");
            return;
        }

        // another check if the license got spoofed
        running = licenseResponse != null;
    }

    public void onEnable(final SonarBungeePlugin plugin) {
        AssertionHelper.check(plugin != null, "Error starting Sonar!");

        // we don't want to continue loading Sonar if the license is invalid
        // or the plugin hasn't started correctly
        if (!running) {
            disable();
            return;
        }

        final long start = System.currentTimeMillis();

        /*
         * Start-up message
         */

        Logger.INFO.log("§7§m«-----------------------------------------»§r");
        Logger.INFO.log(" ");

        Logger.INFO.log(" §7Starting §eSonar §7version §f" + getVersion() + "§7...");

        /*
         * Load all configurations
         */

        Config.initialize();

        if (!Config.Values.load()) {
            Logger.INFO.log(" §cError loading configuration! §7(" + Config.fileName + ")");
            Logger.INFO.log(" ");
        }

        Messages.initialize();

        if (!Messages.Values.load()) {
            Logger.INFO.log(" §cError loading message configuration! §7(" + Messages.fileName + ")");
            Logger.INFO.log(" ");
        }

        Firewall.initialize();

        if (!Firewall.Values.load()) {
            Logger.INFO.log(" §cError loading firewall configuration! §7(" + Firewall.fileName + ".yml)");
            Logger.INFO.log(" ");
        }

        Logger.INFO.log(" §7Setting up commands and features...");

        if (proxy.getPluginManager().getPlugin("TCPShield") != null) {
            isReverseProxy = true;

            Logger.INFO.log(" §cTCPShield detected! §7Switching into compatibility mode.");
            Logger.INFO.log(" §cBad packet checks have been forcefully disabled!️");
        }

        if (proxy.getPluginManager().getPlugin("floodgate") != null) {
            Logger.INFO.log(" §cGeyser detected! §7Geyser players will be fully exempted.");
        }

        proxy.getPluginManager().registerCommand(plugin, new SonarCommand());

        CommandManager.initialize();

        /*
         * Inject in the netty to intercept packets
         * and use Waterfall functions
         */

        JAVA_VERSION = Reflection.getVersion();

        if (!Reflection.inject(new BungeeInterceptor(proxy.getProtocolVersion()), JAVA_VERSION)) {
            Logger.INFO.log(" ");
            Logger.INFO.log(" §cError setting up the connection interceptor! [v " + JAVA_VERSION + "]");
            Logger.INFO.log(" §cMake sure you are using the correct version of the proxy and Java.");
            Logger.INFO.log(" ");
            Logger.INFO.log("§7§m«-----------------------------------------»§r");
            return;
        }

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        ResourceLeakDetector.setEnabled(false);

        /*
         * Starting threads
         */

        Logger.INFO.log(" §7Starting worker threads... (6)");
        Logger.INFO.log(" ");

        new QueueThread().start();

        new ActionBar(this).start();

        new CacheThread().start();

        new PeakThread().start();

        new FirewallThread().start();

        new MonitorThread().start();

        ConsoleFilter.apply();

        /*
         * Process finished
         */

        Logger.INFO.log(" §aSuccessfully started Sonar! §7(" + String.format("%.2f", (System.currentTimeMillis() - start) / 1000D) + " s)");
        Logger.INFO.log(" ");
        Logger.INFO.log("§7§m«-----------------------------------------»§r");

        if (Firewall.Values.ENABLE_FIREWALL) {
            FirewallManager.install(platform);
        } else {
            FirewallManager.uninstall(platform);
        }

        // remove all firewall stuff if the jar gets shut down
        PerformanceMonitor.RUNTIME.addShutdownHook(new Thread(() -> FirewallManager.uninstall(platform)));
    }

    public void onDisable(final SonarBungeePlugin plugin) {
        AssertionHelper.check(plugin != null, "Error stopping Sonar!");

        // remove all firewall stuff
        FirewallManager.uninstall(platform);

        // we need to set this to false in order for all threads
        // to stop correctly since we're doing while(running) and
        // not just while(true)
        running = false;

        // cancel all tasks to prevent any issues
        proxy.getScheduler().cancel(plugin);
    }

    public long reload() {
        AssertionHelper.check(plugin != null, "Error reloading Sonar!");

        final long startTimeStamp = System.currentTimeMillis();

        // re-create the data folder if it got deleted somehow
        SonarBungee.INSTANCE.createDataFolder();

        // initialize and load all config values
        Config.initialize();
        Config.Values.load();

        // initialize and load all messages
        Messages.initialize();
        Messages.Values.load();

        // initialize and load all firewall settings
        Firewall.initialize();
        Firewall.Values.load();

        // Firewall
        if (Firewall.Values.ENABLE_FIREWALL) {
            FirewallManager.install(platform);
        } else {
            FirewallManager.uninstall(platform);
        }

        // call the SonarReloadEvent (API)
        final long endTimeStamp = System.currentTimeMillis();

        final long timeTaken = endTimeStamp - startTimeStamp;

        callEvent(new SonarReloadEvent(startTimeStamp, endTimeStamp, timeTaken));

        return timeTaken;
    }

    private void checkTCPShield() {
    }

    public void createDataFolder() {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdir()) {
                Logger.INFO.log("§cCould not create data folder §7(No permission?)");
            }
        }

        final File logsFolder = new File(plugin.getDataFolder(), "logs");

        if (!logsFolder.exists()) {
            if (!logsFolder.mkdir()) {
                Logger.INFO.log("§cCould not create logs folder §7(No permission?)");
            }
        }
    }

    public <T extends Event> T callEvent(final T event) {
        return proxy.getPluginManager().callEvent(event);
    }
}
