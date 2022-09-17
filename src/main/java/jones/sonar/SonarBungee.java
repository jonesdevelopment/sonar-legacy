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

import jones.sonar.bungee.SonarBungeePlugin;
import jones.sonar.bungee.command.SonarCommand;
import jones.sonar.bungee.command.manager.CommandManager;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.counter.ActionBar;
import jones.sonar.bungee.network.BungeeInterceptor;
import jones.sonar.bungee.util.Reflection;
import jones.sonar.bungee.util.logging.Logger;
import jones.sonar.universal.queue.QueueThread;
import jones.sonar.universal.util.FastException;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;

import java.text.DecimalFormat;

public enum SonarBungee {

    INSTANCE;

    @Getter
    private SonarBungeePlugin plugin;

    public final ProxyServer proxy = ProxyServer.getInstance();

    public final DecimalFormat FORMAT = new DecimalFormat("#,###");

    public final FastException EXCEPTION = new FastException();

    public String VERSION = "unknown";

    public boolean running = false;

    public int JAVA_VERSION = 0;

    public void onLoad(final SonarBungeePlugin plugin) {
        assert plugin != null : "Error loading Sonar!";

        this.plugin = plugin;

        running = true;
    }

    public void onEnable(final SonarBungeePlugin plugin) {
        assert plugin != null : "Error starting Sonar!";

        final long start = System.currentTimeMillis();

        /*
         * Start-up message
         */

        final String LINE = "§7§m«-----------------------------------------»§r";

        Logger.INFO.log(LINE);
        Logger.INFO.log(" ");

        VERSION = plugin.getDescription().getVersion();

        Logger.INFO.log(" §7Starting §eSonar §7version §f" + VERSION + "§7...");

        /*
         * Load all configurations
         */

        createDataFolder();

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

        Logger.INFO.log(" §7Getting everything ready...");
        Logger.INFO.log(" ");

        proxy.getPluginManager().registerCommand(plugin, new SonarCommand());

        CommandManager.initialize();

        /*
         * Inject in the netty to intercept packets
         * and use Waterfall functions
         */

        JAVA_VERSION = Reflection.getVersion();

        if (!Reflection.inject(new BungeeInterceptor(proxy.getProtocolVersion()), JAVA_VERSION)) {
            Logger.INFO.log(" §cError setting up the connection interceptor! [JVM " + JAVA_VERSION + "]");
            Logger.INFO.log(" §cMake sure you are using the correct version of the proxy and Java.");
            Logger.INFO.log(" ");
            Logger.INFO.log(LINE);
            return;
        }

        /*
         * Process finished
         */

        new ActionBar(this);

        new QueueThread().start();

        Logger.INFO.log(" §aSuccessfully started Sonar! §7(" + String.format("%.2f", (System.currentTimeMillis() - start) / 1000D) + " s)");
        Logger.INFO.log(" ");
        Logger.INFO.log(LINE);
    }

    public void onDisable(final SonarBungeePlugin plugin) {
        assert plugin != null : "Error stopping Sonar!";

        running = false;

        proxy.getScheduler().cancel(plugin);
    }

    public void createDataFolder() {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdir()) {
                Logger.INFO.log(" §cCould not create data folder §7(No permission?)");
                Logger.INFO.log(" ");
            }
        }
    }
}
