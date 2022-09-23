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
