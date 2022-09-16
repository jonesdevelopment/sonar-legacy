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

package jones.sonar.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import jones.sonar.SonarVelocity;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "sonar",
        name = "Sonar",
        version = "1.3.1",
        url = "https://jonesdev.xyz/",
        description = "Anti bot plugin for BungeeCord with 1.7-1.19 support",
        authors = {"jonesdev.xyz"})
public final class SonarVelocityPlugin {

    public final ProxyServer server;

    public final Logger logger;

    public final Path dataDirectory;

    @Inject
    public SonarVelocityPlugin(final ProxyServer server, final Logger logger, final @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        SonarVelocity.INSTANCE.onInitialize(this, event);
    }
}
