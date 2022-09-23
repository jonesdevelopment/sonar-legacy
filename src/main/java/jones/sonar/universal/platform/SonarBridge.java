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

package jones.sonar.universal.platform;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
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
                SonarBungee.INSTANCE.onEnable((SonarBungeePlugin) objects[0]);
                break;
            }
        }
    }

    public void onDisable(final SonarPlatform platform, final SonarBungeePlugin bungeePlugin) {
        if (platform == SonarPlatform.BUNGEE) {
            SonarBungee.INSTANCE.onDisable(bungeePlugin);
        } else {
            throw new GeneralException("Velocity and other platforms do not have a onDisable() event");
        }
    }

    public void onLoad(final SonarPlatform platform, final SonarBungeePlugin bungeePlugin) {
        if (platform == SonarPlatform.BUNGEE) {
            SonarBungee.INSTANCE.onLoad(bungeePlugin);
        } else {
            throw new GeneralException("Velocity and other platforms do not have a onLoad() event");
        }
    }
}
