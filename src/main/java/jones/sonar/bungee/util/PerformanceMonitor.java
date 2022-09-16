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

package jones.sonar.bungee.util;

import lombok.experimental.UtilityClass;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@UtilityClass
public class PerformanceMonitor {
    public final Runtime RUNTIME = Runtime.getRuntime();

    public OperatingSystemMXBean OPERATING_SYSTEM = ManagementFactory.getOperatingSystemMXBean();

    public long getTotalMemory() {
        return RUNTIME.totalMemory() / 1000000L;
    }

    public long getFreeMemory() {
        return RUNTIME.freeMemory() / 1000000L;
    }

    public long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    public double getCPULoad() {
        return OPERATING_SYSTEM.getSystemLoadAverage() * 10;
    }

    public double getAverageCPULoad() {
        return getCPULoad() / OPERATING_SYSTEM.getAvailableProcessors();
    }

    public String formatCPULoad() {
        return String.format("%.2f", getCPULoad());
    }

    public String formatAverageCPULoad() {
        return String.format("%.2f", getAverageCPULoad());
    }
}
