package jones.sonar.bungee;

import com.google.gson.annotations.SerializedName;
import jones.sonar.universal.platform.SonarBridge;
import jones.sonar.universal.platform.SonarPlatform;
import net.md_5.bungee.api.plugin.Plugin;

public final class SonarBungeePlugin extends Plugin {

    @Override
    @SerializedName("load")
    public void onLoad() {
        SonarBridge.onLoad(SonarPlatform.BUNGEE, this);
    }

    @Override
    @SerializedName("enable")
    public void onEnable() {
        SonarBridge.onEnable(SonarPlatform.BUNGEE, this);
    }

    @Override
    @SerializedName("disable")
    public void onDisable() {
        SonarBridge.onDisable(SonarPlatform.BUNGEE, this);
    }

}
