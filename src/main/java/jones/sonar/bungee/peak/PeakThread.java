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

package jones.sonar.bungee.peak;

import jones.sonar.SonarBungee;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.counter.ActionBarManager;
import jones.sonar.bungee.util.ColorUtil;

public final class PeakThread extends Thread implements Runnable {

    public PeakThread() {
        super("sonar#peak");
    }

    private final SonarBungee sonar = SonarBungee.INSTANCE;

    @Override
    public void run() {
        while (sonar.running) {
            try {
                try {

                    // if the peak is greater than the last peak
                    if (sonar.ipSecPeakCalculator.newPeak > sonar.ipSecPeakCalculator.lastPeak

                            // we don't want to send messages twice
                            && !sonar.ipSecPeakCalculator.broadcasted

                            // we want the peak to only show when there's an actual attack
                            && sonar.ipSecPeakCalculator.newPeak > Config.Values.MINIMUM_JOINS_PER_SECOND) {

                        sonar.ipSecPeakCalculator.broadcasted = true;

                        // generate and format the message that needs to be sent to all players
                        final String ipsPeakMessage = Messages.Values.PEAK_FORMAT_IPS
                                .replaceAll("%old%", ColorUtil.getColorForCounter(sonar.ipSecPeakCalculator.realLastPeak)
                                        + sonar.FORMAT.format(sonar.ipSecPeakCalculator.realLastPeak))
                                .replaceAll("%new%", ColorUtil.getColorForCounter(sonar.ipSecPeakCalculator.newPeak)
                                        + sonar.FORMAT.format(sonar.ipSecPeakCalculator.newPeak));

                        sonar.ipSecPeakCalculator.realLastPeak = sonar.ipSecPeakCalculator.newPeak;

                        // broadcast the new peak to every player
                        ActionBarManager.getPlayers().forEach(player -> player.sendMessage(ipsPeakMessage));
                    }

                    // if the peak is greater than the last peak
                    if (sonar.cpsPeakCalculator.newPeak > sonar.cpsPeakCalculator.lastPeak

                            // we don't want to send messages twice
                            && !sonar.cpsPeakCalculator.broadcasted

                            // we want the peak to only show when there's an actual attack
                            && sonar.cpsPeakCalculator.newPeak > (Config.Values.MINIMUM_JOINS_PER_SECOND * 2L)) {

                        sonar.cpsPeakCalculator.broadcasted = true;

                        // generate and format the message that needs to be sent to all players
                        final String cpsPeakMessage = Messages.Values.PEAK_FORMAT_CPS
                                .replaceAll("%old%", ColorUtil.getColorForCounter(sonar.cpsPeakCalculator.realLastPeak)
                                        + sonar.FORMAT.format(sonar.cpsPeakCalculator.realLastPeak))
                                .replaceAll("%new%", ColorUtil.getColorForCounter(sonar.cpsPeakCalculator.newPeak)
                                        + sonar.FORMAT.format(sonar.cpsPeakCalculator.newPeak));

                        sonar.cpsPeakCalculator.realLastPeak = sonar.cpsPeakCalculator.newPeak;

                        // broadcast the new peak to every player
                        ActionBarManager.getPlayers().forEach(player -> player.sendMessage(cpsPeakMessage));
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                Thread.sleep(Messages.Values.PEAK_DELAY);
            } catch (InterruptedException exception) {
                break;
            }
        }
    }
}
