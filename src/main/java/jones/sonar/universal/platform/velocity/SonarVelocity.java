package jones.sonar.universal.platform.velocity;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import jones.sonar.universal.util.AssertionHelper;
import jones.sonar.velocity.SonarVelocityPlugin;

public enum SonarVelocity {

    INSTANCE;

    private SonarVelocityPlugin plugin;

    public void onInitialize(final SonarVelocityPlugin plugin, final ProxyInitializeEvent event) {
        AssertionHelper.check(plugin != null, "Error initializing Sonar!");

        this.plugin = plugin;
    }

    public void onShutDown(final SonarVelocityPlugin plugin, final ProxyShutdownEvent event) {
        AssertionHelper.check(plugin != null, "Error stopping Sonar!");

        this.plugin = plugin;
    }
}
