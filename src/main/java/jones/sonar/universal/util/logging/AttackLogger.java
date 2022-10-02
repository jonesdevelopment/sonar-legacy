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

package jones.sonar.universal.util.logging;

import jones.sonar.bungee.util.logging.Logger;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.platform.SonarPlatform;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.PerformanceMonitor;
import jones.sonar.universal.whitelist.Whitelist;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class AttackLogger {
    public void logIncomingAttack(final SonarPlatform platform) throws Exception {
        switch (platform) {
            case BUNGEE: {
                final String dateTime = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());

                Logger.INFO.log("Logging incoming attack " + dateTime + "...");

                // don't cause errors if the folder got deleted
                SonarBungee.INSTANCE.createDataFolder();

                final File logsFolder = new File(SonarBungee.INSTANCE.getPlugin().getDataFolder(), "logs");

                final File logsFile = new File(logsFolder, "attack." + dateTime
                        .replaceAll("/", "-")
                        .replaceAll(" ", "_")
                        .replaceAll(":", ".") + ".log");

                if (logsFile.exists()) {
                    if (!logsFile.delete()) {
                        Logger.ERROR.log("Failed to delete old logs file (No permission?)");
                        return;
                    }
                }

                if (!logsFile.createNewFile()) {
                    Logger.ERROR.log("Unable to log incoming attack #2 (No permission?)");
                    return;
                }

                final FileWriter fileWriter = new FileWriter(logsFile, true);

                logIncomingAttack_(fileWriter, dateTime);
                break;
            }

            default: {
                break;
            }
        }
    }

    private void logIncomingAttack_(final FileWriter fileWriter, final String dateTime) throws Exception {
        fileWriter.write("---- Incoming attack (" + dateTime + ") ----\n");
        fileWriter.write(" \n");
        fileWriter.write("Current connections per second: " + Counter.CONNECTIONS_PER_SECOND.get() + "\n");
        fileWriter.write("Current ip addresses per second: " + Counter.IPS_PER_SECOND.get() + "\n");
        fileWriter.write("Current logins per second: " + Counter.JOINS_PER_SECOND.get() + "\n");
        fileWriter.write("Current encryptions per second: " + Counter.ENCRYPTIONS_PER_SECOND.get() + "\n");
        fileWriter.write("Current handshakes per second: " + Counter.HANDSHAKES_PER_SECOND.get() + "\n");
        fileWriter.write(" \n");
        fileWriter.write("Blacklisted ip addresses: " + Blacklist.size() + "\n");
        fileWriter.write("Whitelisted ip addresses: " + Whitelist.size() + "\n");
        fileWriter.write(" \n");
        fileWriter.write("Current CPU usage (average): " + PerformanceMonitor.formatAverageCPULoad() + "%" + "\n");
        fileWriter.write("Current CPU usage (global): " + PerformanceMonitor.formatCPULoad() + "%" + "\n");
        fileWriter.write("Available processors (jvm): " + PerformanceMonitor.OPERATING_SYSTEM.getAvailableProcessors() + "\n");
        fileWriter.write(" \n");
        fileWriter.write("Total available memory: " + PerformanceMonitor.getTotalMemory() + "MB" + "\n");
        fileWriter.write("Total used/free memory: " + PerformanceMonitor.getUsedMemory() + "MB / " + PerformanceMonitor.getFreeMemory() + "MB" + "\n");
        fileWriter.close();
    }
}
