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

import jones.sonar.network.bungee.BungeeInterceptor;
import jones.sonar.util.Reflection;
import jones.sonar.util.logging.Logger;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;

public enum Sonar {

    INSTANCE;

    @Getter
    private SonarBungee plugin;

    public final ProxyServer proxy = ProxyServer.getInstance();

    public int JAVA_VERSION = 0;

    public void onLoad(final SonarBungee plugin) {
        assert plugin != null : "Error loading Sonar!";

        this.plugin = plugin;
    }

    public void onEnable(final SonarBungee plugin) {
        assert plugin != null : "Error starting Sonar!";

        /*
         * Start-up message
         */

        final String LINE = "§7§m«-------------------------------------------»§r";

        Logger.INFO.log(LINE);
        Logger.INFO.log(" ");
        Logger.INFO.log(" §7Starting §eSonar §7version §f" + plugin.getDescription().getVersion() + "§7...");

        /*
         * Load all configurations
         */

        Logger.INFO.log(" §7Getting everything ready...");
        Logger.INFO.log(" ");

        /*
         * Inject in the netty to intercept packets
         * and use Waterfall functions
         */

        JAVA_VERSION = Reflection.getVersion();

        if (!Reflection.inject(new BungeeInterceptor(proxy.getProtocolVersion()), JAVA_VERSION)) {
            Logger.INFO.log(" §cError injecting into the proxy!");
            Logger.INFO.log(" §cMake sure you are using the correct version of the proxy and Java.");
            Logger.INFO.log(" ");
            Logger.INFO.log(LINE);
            return;
        }

        /*
         * Process finished
         */

        Logger.INFO.log(" §aSuccessfully started Sonar!");
        Logger.INFO.log(" ");
        Logger.INFO.log(LINE);
    }

    public void onDisable(final SonarBungee plugin) {
        assert plugin != null : "Error stopping Sonar!";
    }
}
