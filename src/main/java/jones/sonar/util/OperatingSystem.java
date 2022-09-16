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

package jones.sonar.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OperatingSystem {
    public final String OS = System.getProperty("os.name");

    public String getOSName() {
        if (OS.toLowerCase().contains("unix")) {
            return "Unix / Linux";
        }
        if (OS.toLowerCase().contains("os")) {
            return "macOS";
        }
        if (OS.toLowerCase().contains("wind")) {
            return "Windows";
        }
        if (OS.toLowerCase().contains("linu")) {
            return "Linux";
        }
        return "unknown";
    }
}