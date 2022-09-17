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

package jones.sonar.universal.license;

import jones.sonar.universal.bridge.SonarBridgeType;
import jones.sonar.universal.license.hwid.HardwareID;
import jones.sonar.universal.license.response.LicenseResponse;
import jones.sonar.universal.license.response.WebResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LicenseLoader {
    public LicenseResponse loadFromFile(final SonarBridgeType type) {
        final String key = "loadthisfrompropertiesfile";
        final HardwareID hardwareID = new HardwareID();

        // try to get the hardware id
        // this is not done automatically to ensure more protection against
        // deobfuscation, reverse-engineering, ...
        hardwareID.tryAndGet();

        switch (type) {
            case BUNGEE: {

                break;
            }

            case VELOCITY: {
                break;
            }

            case UNKNOWN: {
                return null;
            }
        }

        final License license = new License(hardwareID, key);

        final WebResponse response = license.validate();

        switch (response) {
            default:
            case INVALID:
            case NOT_EXISTING: {
                return new LicenseResponse(license,
                        response,
                        response.getExpectedResponseCode(),
                        "The given license key or hardware id is invalid.");
            }

            case PERM_REDIRECT:
            case TEMP_REDIRECT: {
                return new LicenseResponse(license,
                        response,
                        response.getExpectedResponseCode(),
                        "The api server seems to be offline. If this keep occurring, please contact support via Discord (https://discord.jonesdev.xyz).");
            }

            case SUCCESS: {
                return new LicenseResponse(license,
                        response,
                        response.getExpectedResponseCode(),
                        "The license was successfully validated.");
            }
        }
    }
}
