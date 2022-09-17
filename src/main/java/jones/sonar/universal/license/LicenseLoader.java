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

import jones.sonar.SonarBungee;
import jones.sonar.universal.SonarPlatform;
import jones.sonar.universal.license.hwid.HardwareID;
import jones.sonar.universal.license.response.LicenseResponse;
import jones.sonar.universal.license.response.WebResponse;
import jones.sonar.universal.util.GeneralException;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@UtilityClass
public class LicenseLoader {
    public LicenseResponse loadFromFile(final SonarPlatform type) throws Exception {
        String key = "unknown";

        final String fileName = "license.properties";

        final File fileToCopy;

        switch (type) {
            case BUNGEE: {
                fileToCopy = new File(SonarBungee.INSTANCE.getPlugin().getDataFolder(), fileName);

                if (!fileToCopy.exists()) {
                    try (final InputStream in = SonarBungee.INSTANCE.getPlugin().getResourceAsStream(fileName)) {
                        Files.copy(in, fileToCopy.toPath());
                    } catch (IOException exception) {
                        throw new GeneralException("Could not find license file: " + fileName);
                    }
                }
                break;
            }

            // TODO: Velocity support
            case VELOCITY: {
                throw new GeneralException("Velocity isn't supported yet");
            }

            default:
            case UNKNOWN: {
                throw new GeneralException("Unknown server type");
            }
        }

        final List<String> linesFromProperties = Files.readAllLines(fileToCopy.toPath());

        // check if the license file is empty
        if (linesFromProperties.isEmpty()) {
            throw new GeneralException("Empty license file");
        }

        // check if the line size doesn't match
        if (linesFromProperties.size() != 6) {
            throw new GeneralException("Modified license file");
        }

        // read the key from the file
        key = linesFromProperties.get(5);

        // check if the key is valid
        if (!key.startsWith("sonar-license=")) {
            throw new GeneralException("Invalid license format (sonar-license=XXX)");
        }

        // replace the key option prefix
        key = key.replaceFirst("sonar-license=", "");

        // check if the length of the key is valid
        if (key.isEmpty()) {
            throw new GeneralException("Invalid license key length");
        }

        final HardwareID hardwareID = new HardwareID();

        // try to get the hardware id
        // this is not done automatically to ensure more protection against
        // deobfuscation, reverse-engineering, ...
        hardwareID.tryAndGet();

        final License license = new License(hardwareID, key);

        // validating license
        final WebResponse response = license.validate();

        switch (response) {

            // license is invalid
            default:
            case INVALID:
            case NOT_EXISTING: {
                return new LicenseResponse(license,
                        response,
                        response.getExpectedResponseCode(),
                        "The given license key or hardware id is invalid.");
            }

            // api server down
            case PERM_REDIRECT:
            case TEMP_REDIRECT: {
                return new LicenseResponse(license,
                        response,
                        response.getExpectedResponseCode(),
                        "The api server seems to be offline. If this keep occurring, please contact support via Discord (https://discord.jonesdev.xyz).");
            }

            // license is valid
            case SUCCESS: {
                return new LicenseResponse(license,
                        response,
                        response.getExpectedResponseCode(),
                        "The license was successfully validated.");
            }
        }
    }
}
