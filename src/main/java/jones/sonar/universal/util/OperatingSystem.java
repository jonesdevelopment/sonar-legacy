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

package jones.sonar.universal.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OperatingSystem {
    public final String OS_NAME = System.getProperty("os.name");

    public final String OS_VERSION = System.getProperty("os.version");

    public final String OS_ARCH = System.getProperty("os.arch");

    public String getOSName() {
        if (OS_NAME.toLowerCase().contains("unix")) {
            return "Unix / Linux";
        }
        if (OS_NAME.toLowerCase().contains("os")) {
            return "macOS";
        }
        if (OS_NAME.toLowerCase().contains("wind")) {
            return "Windows";
        }
        if (OS_NAME.toLowerCase().contains("linu")) {
            return "Linux";
        }
        return "unknown";
    }
}
