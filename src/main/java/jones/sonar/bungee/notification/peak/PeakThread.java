package jones.sonar.bungee.notification.peak;

import jones.sonar.api.enums.PeakType;
import jones.sonar.api.event.bungee.SonarPeakChangedEvent;
import jones.sonar.api.event.bungee.SonarPeakResetEvent;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.notification.actionbar.ActionBarManager;
import jones.sonar.bungee.util.ColorUtil;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.Sensibility;

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

                    final long timeStamp = System.currentTimeMillis();

                    // the server shouldn't be under attack when resetting the peak
                    // in order to prevent a peak reset during an attack
                    if (!Sensibility.isUnderAttack()) {

                        // reset CPS peak if it didn't change in a few milliseconds
                        if (timeStamp - sonar.cpsPeakCalculator.lastPeakChange > Messages.Values.PEAK_RESET_DELAY) {
                            sonar.cpsPeakCalculator.reset();

                            sonar.callEvent(new SonarPeakResetEvent(PeakType.CONNECTIONS_PER_SECOND));
                        }

                        // reset IPS peak if it didn't change in a few milliseconds
                        if (timeStamp - sonar.ipSecPeakCalculator.lastPeakChange > Messages.Values.PEAK_RESET_DELAY) {
                            sonar.ipSecPeakCalculator.reset();

                            sonar.callEvent(new SonarPeakResetEvent(PeakType.IP_ADDRESSES_PER_SECOND));
                        }
                    } else {
                        sonar.cpsPeakCalculator.lastPeakChange = timeStamp;
                        sonar.ipSecPeakCalculator.lastPeakChange = timeStamp;
                    }

                    // if the peak is greater than the last peak
                    if (sonar.ipSecPeakCalculator.newPeak > sonar.ipSecPeakCalculator.lastPeak

                            // we don't want to send messages twice
                            && !sonar.ipSecPeakCalculator.didBroadcast

                            // we want the peak to only show when there's an actual attack
                            && sonar.ipSecPeakCalculator.newPeak > Config.Values.MINIMUM_JOINS_PER_SECOND) {

                        sonar.ipSecPeakCalculator.didBroadcast = true;

                        // generate and format the message that needs to be sent to all players
                        final String ipsPeakMessage = Messages.Values.PEAK_FORMAT_IPS
                                .replaceAll("%old%", ColorUtil.getColorForCounter(sonar.ipSecPeakCalculator.realLastPeak)
                                        + sonar.FORMAT.format(sonar.ipSecPeakCalculator.realLastPeak))
                                .replaceAll("%new%", ColorUtil.getColorForCounter(sonar.ipSecPeakCalculator.newPeak)
                                        + sonar.FORMAT.format(sonar.ipSecPeakCalculator.newPeak));

                        sonar.ipSecPeakCalculator.realLastPeak = sonar.ipSecPeakCalculator.newPeak;

                        sonar.callEvent(new SonarPeakChangedEvent(PeakType.IP_ADDRESSES_PER_SECOND, sonar.ipSecPeakCalculator.newPeak));

                        // broadcast the new peak to every player
                        ActionBarManager.getPlayers().forEach(player -> player.sendMessage(ipsPeakMessage));
                    }

                    // if the peak is greater than the last peak
                    if (sonar.cpsPeakCalculator.newPeak > sonar.cpsPeakCalculator.lastPeak

                            // we don't want to send messages twice
                            && !sonar.cpsPeakCalculator.didBroadcast

                            // we want the peak to only show when there's an actual attack
                            && sonar.cpsPeakCalculator.newPeak > (Config.Values.MINIMUM_JOINS_PER_SECOND * 2L)) {

                        sonar.cpsPeakCalculator.didBroadcast = true;

                        // generate and format the message that needs to be sent to all players
                        final String cpsPeakMessage = Messages.Values.PEAK_FORMAT_CPS
                                .replaceAll("%old%", ColorUtil.getColorForCounter(sonar.cpsPeakCalculator.realLastPeak)
                                        + sonar.FORMAT.format(sonar.cpsPeakCalculator.realLastPeak))
                                .replaceAll("%new%", ColorUtil.getColorForCounter(sonar.cpsPeakCalculator.newPeak)
                                        + sonar.FORMAT.format(sonar.cpsPeakCalculator.newPeak));

                        sonar.cpsPeakCalculator.realLastPeak = sonar.cpsPeakCalculator.newPeak;

                        sonar.callEvent(new SonarPeakChangedEvent(PeakType.CONNECTIONS_PER_SECOND, sonar.cpsPeakCalculator.newPeak));

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
