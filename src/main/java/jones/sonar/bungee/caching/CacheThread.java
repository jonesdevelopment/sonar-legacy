package jones.sonar.bungee.caching;

import jones.sonar.api.event.bungee.SonarBlacklistClearEvent;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.notification.NotificationManager;
import jones.sonar.bungee.notification.actionbar.ActionBarManager;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.connection.ConnectionData;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.firewall.FirewallManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.Sensibility;

import java.util.Optional;

public final class CacheThread extends Thread implements Runnable {

    public CacheThread() {
        super("sonar#cache");
    }

    private long lastBlacklistClear = 0L;

    @Override
    public void run() {
        while (SonarBungee.INSTANCE.running) {
            try {
                try {

                    // reset server ping cache
                    ServerPingCache.needsUpdate = true;

                    // check for any attacks
                    NotificationManager.checkForAttack();

                    if (Config.Values.AUTOMATICALLY_REMOVE_BOTS_FROM_VERIFICATION) {
                        final long verifying = ConnectionDataManager.getVerifying();

                        if (verifying > Config.Values.MINIMUM_JOINS_PER_SECOND) {
                            final Optional<ConnectionData> first = ConnectionDataManager.getVerifyingData().findFirst();

                            first.ifPresent(connectionData -> ConnectionDataManager.remove(connectionData.inetAddress));
                        }
                    }

                    ServerPingCache.removeAllUnused();

                    // automatically clear the blacklist
                    final long timeStamp = System.currentTimeMillis();

                    if (lastBlacklistClear == 0L) {
                        lastBlacklistClear = timeStamp;
                    }

                    final long blacklisted = Blacklist.size();

                    if (timeStamp - lastBlacklistClear > Messages.Values.BLACKLIST_CLEAR_TIME && !Sensibility.isUnderAttack() && blacklisted > 0) {
                        Blacklist.BLACKLISTED.invalidateAll();
                        Blacklist.TEMP_BLACKLISTED.invalidateAll();

                        SonarBungee.INSTANCE.callEvent(new SonarBlacklistClearEvent(blacklisted));

                        if (Config.Values.AUTOMATICALLY_REMOVE_BOTS_FROM_VERIFICATION) {
                            if (ConnectionDataManager.getVerifying() > Config.Values.MINIMUM_JOINS_PER_SECOND) {
                                ConnectionDataManager.getVerifyingData().forEach(ConnectionDataManager::remove);
                            }
                        }

                        final String alert = Messages.Values.BLACKLIST_AUTO_CLEAR
                                .replaceAll("%es%", blacklisted == 1 ? "" : "es")
                                .replaceAll("%have/has%", blacklisted == 1 ? "has" : "have")
                                .replaceAll("%has/have%", blacklisted == 1 ? "has" : "have")
                                .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(blacklisted))
                                .replaceAll("%firewalled%", SonarBungee.INSTANCE.FORMAT.format(Blacklist.FIREWALLED.size()));

                        try {
                            FirewallManager.clear();

                            Blacklist.FIREWALLED.clear();
                        } catch (Exception exception) {
                           // exception.printStackTrace();
                        }

                        ActionBarManager.getPlayers().forEach(player -> player.sendMessage(alert));

                        lastBlacklistClear = timeStamp;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                Thread.sleep(500L);
            } catch (InterruptedException exception) {
                break;
            }
        }
    }
}
