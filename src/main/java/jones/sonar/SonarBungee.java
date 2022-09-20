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

package jones.sonar;

import jones.sonar.api.event.bungee.SonarReloadEvent;
import jones.sonar.bungee.SonarBungeePlugin;
import jones.sonar.bungee.caching.CacheThread;
import jones.sonar.bungee.command.SonarCommand;
import jones.sonar.bungee.command.manager.CommandManager;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.counter.ActionBar;
import jones.sonar.bungee.filter.ConsoleFilter;
import jones.sonar.bungee.network.BungeeInterceptor;
import jones.sonar.bungee.peak.PeakThread;
import jones.sonar.bungee.util.Reflection;
import jones.sonar.bungee.util.logging.Logger;
import jones.sonar.universal.SonarPlatform;
import jones.sonar.universal.license.LicenseLoader;
import jones.sonar.universal.license.response.LicenseResponse;
import jones.sonar.universal.license.response.WebResponse;
import jones.sonar.universal.peak.PeakCalculator;
import jones.sonar.universal.queue.QueueThread;
import jones.sonar.universal.util.FastException;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Event;

import java.text.DecimalFormat;

public enum SonarBungee {

    INSTANCE;

    @Getter
    private SonarBungeePlugin plugin;

    public final ProxyServer proxy = ProxyServer.getInstance();

    public final DecimalFormat FORMAT = new DecimalFormat("#,###");

    public final FastException EXCEPTION = new FastException();

    public final PeakCalculator cpsPeakCalculator = new PeakCalculator(),
            ipSecPeakCalculator = new PeakCalculator();

    public final SonarPlatform platform = SonarPlatform.BUNGEE;

    public String VERSION = "unknown";

    public boolean running = false;

    public int JAVA_VERSION = 0;

    public void onLoad(final SonarBungeePlugin plugin) {
        assert plugin != null : "Error loading Sonar!";

        this.plugin = plugin;

        createDataFolder();

        // cached license response
        LicenseResponse licenseResponse;

        // cached exception, if something goes wrong
        Exception unhandledException = null;

        try {

            // try to load the license
            licenseResponse = LicenseLoader.loadFromFile(platform);

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
        }
    }

    public void onEnable(final SonarBungeePlugin plugin) {
        assert plugin != null : "Error starting Sonar!";

        // we don't want to continue loading Sonar if the license is invalid
        // or the plugin hasn't started correctly
        if (!running) {
            plugin.onDisable();
            return;
        }

        final long start = System.currentTimeMillis();

        /*
         * Start-up message
         */

        Logger.INFO.log("§7§m«-----------------------------------------»§r");
        Logger.INFO.log(" ");

        VERSION = plugin.getDescription().getVersion();

        Logger.INFO.log(" §7Starting §eSonar §7version §f" + VERSION + "§7...");

        /*
         * Load all configurations
         */

        Config.initialize();

        if (!Config.Values.load()) {
            Logger.INFO.log(" §cError loading configuration! §7(config.yml)");
            Logger.INFO.log(" ");
        }

        Messages.initialize();

        if (!Messages.Values.load()) {
            Logger.INFO.log(" §cError loading message configuration! §7(messages.yml)");
            Logger.INFO.log(" ");
        }

        Logger.INFO.log(" §7Setting up commands and features...");

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

        /*
         * Starting threads
         */

        Logger.INFO.log(" §7Starting worker threads... (4)");
        Logger.INFO.log(" ");

        new QueueThread().start();

        new ActionBar(this).start();

        new CacheThread().start();

        new PeakThread().start();

        ConsoleFilter.apply(this);

        /*
         * Process finished
         */

        Logger.INFO.log(" §aSuccessfully started Sonar! §7(" + String.format("%.2f", (System.currentTimeMillis() - start) / 1000D) + " s)");
        Logger.INFO.log(" ");
        Logger.INFO.log("§7§m«-----------------------------------------»§r");
    }

    public void onDisable(final SonarBungeePlugin plugin) {
        assert plugin != null : "Error stopping Sonar!";

        // we need to set this to false in order for all threads
        // to stop correctly since we're doing while(running) and
        // not just while(true)
        running = false;

        // cancel all tasks to prevent any issues
        proxy.getScheduler().cancel(plugin);
    }

    public long reload() {
        final long startTimeStamp = System.currentTimeMillis();

        // re-create the data folder if it got deleted somehow
        SonarBungee.INSTANCE.createDataFolder();

        // initialize and load all config values
        Config.initialize();
        Config.Values.load();

        // initialize and load all messages
        Messages.initialize();
        Messages.Values.load();

        // call the SonarReloadEvent (API)
        final long endTimeStamp = System.currentTimeMillis();

        final long timeTaken = endTimeStamp - startTimeStamp;

        callEvent(new SonarReloadEvent(startTimeStamp, endTimeStamp, timeTaken));

        return timeTaken;
    }

    public void createDataFolder() {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdir()) {
                Logger.INFO.log("§cCould not create data folder §7(No permission?)");
            }
        }
    }

    public <T extends Event> T callEvent(final T event) {
        return proxy.getPluginManager().callEvent(event);
    }
}
