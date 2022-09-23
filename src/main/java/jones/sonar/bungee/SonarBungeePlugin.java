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
