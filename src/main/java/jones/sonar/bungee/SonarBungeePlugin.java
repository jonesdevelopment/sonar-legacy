package jones.sonar.bungee;

import jones.sonar.universal.platform.SonarBridge;
import jones.sonar.universal.platform.SonarPlatform;
import net.md_5.bungee.api.plugin.Plugin;

public final class SonarBungeePlugin extends Plugin {

    @Override
    public void onLoad() {
        SonarBridge.onLoad(SonarPlatform.BUNGEE, this);
    }

    @Override
    public void onEnable() {
        SonarBridge.onEnable(SonarPlatform.BUNGEE, this);
    }

    @Override
    public void onDisable() {
        SonarBridge.onDisable(SonarPlatform.BUNGEE, this);
    }

}
