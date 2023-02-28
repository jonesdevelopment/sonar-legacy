package jones.sonar.universal.platform;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import jones.sonar.bungee.SonarBungeePlugin;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.platform.velocity.SonarVelocity;
import jones.sonar.universal.util.GeneralException;
import jones.sonar.velocity.SonarVelocityPlugin;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SonarBridge {
    public void onEnable(final SonarPlatform platform, final Object... objects) {
        switch (platform) {
            default:
            case UNKNOWN: {
                throw new GeneralException("Invalid platform");
            }

            case VELOCITY: {

                // objects[0] = SonarVelocityPlugin
                // objects[1] = ProxyInitializeEvent
                SonarVelocity.INSTANCE.onInitialize((SonarVelocityPlugin) objects[0], (ProxyInitializeEvent) objects[1]);
                break;
            }

            case BUNGEE: {

                // objects[0] = SonarBungeePlugin
                SonarBungee.INSTANCE.onEnable();
                break;
            }
        }
    }

    public void onDisable(final SonarPlatform platform, final Object... objects) {
        switch (platform) {
            default:
            case UNKNOWN: {
                throw new GeneralException("Invalid platform");
            }

            case VELOCITY: {

                // objects[0] = SonarVelocityPlugin
                // objects[1] = ProxyShutdownEvent
                SonarVelocity.INSTANCE.onShutDown((SonarVelocityPlugin) objects[0], (ProxyShutdownEvent) objects[1]);
                break;
            }

            case BUNGEE: {

                // objects[0] = SonarBungeePlugin
                SonarBungee.INSTANCE.onDisable();
                break;
            }
        }
    }

    public void onLoad(final SonarPlatform platform, final SonarBungeePlugin bungeePlugin) {
        if (platform == SonarPlatform.BUNGEE) {
            SonarBungee.INSTANCE.onLoad(bungeePlugin);
        } else {
            throw new GeneralException("Velocity and other platforms do not support the load() event");
        }
    }
}
