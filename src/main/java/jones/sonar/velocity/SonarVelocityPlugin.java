package jones.sonar.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import jones.sonar.universal.platform.SonarBridge;
import jones.sonar.universal.platform.SonarPlatform;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "sonar",
        name = "Sonar",
        version = "1.4.6-FIX",
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
    public void handle(final ProxyInitializeEvent event) {
        SonarBridge.onEnable(SonarPlatform.VELOCITY, this, event);
    }

    @Subscribe
    public void handle(final ProxyShutdownEvent event) {
        SonarBridge.onDisable(SonarPlatform.VELOCITY, this, event);
    }
}
