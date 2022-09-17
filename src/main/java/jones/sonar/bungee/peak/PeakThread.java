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
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.peak.PeakCalculator;
import jones.sonar.universal.peak.result.PeakCalculation;
import jones.sonar.universal.peak.result.PeakSubmitResult;

public final class PeakThread extends Thread implements Runnable {

    public PeakThread() {
        super("sonar#peak");
    }

    @Override
    public void run() {
        while (SonarBungee.INSTANCE.running) {
            try {
                try {

                    // We only want to calculate the peak if the module is enabled
                    if (Messages.Values.ENABLE_PEAK) {

                        // calculate the peak
                        final PeakCalculation cpsPeak = PeakCalculator.submit(Counter.CONNECTIONS_PER_SECOND);
                        final PeakCalculation ipsPeak = PeakCalculator.submit(Counter.IPS_PER_SECOND);

                        // check if there's a new peak for CPS
                        if (cpsPeak.submitResult == PeakSubmitResult.NEW_PEAK

                                // only broadcast a new CPS peak if the cps are high enough
                                && cpsPeak.newPeak > (Config.Values.MINIMUM_JOINS_PER_SECOND * 2L)) {

                            // generate and format the message that needs to be sent to all players
                            final String cpsPeakMessage = Messages.Values.PEAK_FORMAT_CPS
                                    .replaceAll("%old%", ColorUtil.getColorForCounter(cpsPeak.lastPeak) + SonarBungee.INSTANCE.FORMAT.format(cpsPeak.lastPeak))
                                    .replaceAll("%new%", ColorUtil.getColorForCounter(cpsPeak.newPeak) + SonarBungee.INSTANCE.FORMAT.format(cpsPeak.newPeak));

                            // broadcast the new peak to every player
                            ActionBarManager.getPlayers().forEach(player -> player.sendMessage(cpsPeakMessage));
                        }

                        // check if there's a new peak for IPs/sec
                        if (ipsPeak.submitResult == PeakSubmitResult.NEW_PEAK

                                // only broadcast a new IPs/sec peak if the ips are high enough
                                && ipsPeak.newPeak > (Config.Values.MINIMUM_JOINS_PER_SECOND)) {

                            // generate and format the message that needs to be sent to all players
                            final String ipsPeakMessage = Messages.Values.PEAK_FORMAT_IPS
                                    .replaceAll("%old%", ColorUtil.getColorForCounter(ipsPeak.lastPeak) + SonarBungee.INSTANCE.FORMAT.format(ipsPeak.lastPeak))
                                    .replaceAll("%new%", ColorUtil.getColorForCounter(ipsPeak.newPeak) + SonarBungee.INSTANCE.FORMAT.format(ipsPeak.newPeak));

                            // broadcast the new peak to every player
                            ActionBarManager.getPlayers().forEach(player -> player.sendMessage(ipsPeakMessage));
                        }
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
