package jones.sonar.bungee.detection;

import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.network.handler.PlayerHandler;
import jones.sonar.universal.config.options.CustomRegexOptions;
import jones.sonar.universal.data.connection.ConnectionData;
import jones.sonar.universal.data.player.PlayerData;
import jones.sonar.universal.data.player.manager.PlayerDataManager;
import jones.sonar.universal.detection.Detection;
import jones.sonar.universal.detection.DetectionResult;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.queue.PlayerQueue;
import jones.sonar.universal.util.Sensibility;
import jones.sonar.universal.whitelist.Whitelist;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class LoginHandler {
    private final Detection[] cachedDetections = new Detection[10];

    public void updateDetectionCache() {
        cachedDetections[0] = new Detection(DetectionResult.DENIED, null, true);
        cachedDetections[1] = new Detection(DetectionResult.DENIED, Messages.Values.DISCONNECT_INVALID_NAME, false);
        cachedDetections[2] = new Detection(DetectionResult.DENIED, Messages.Values.DISCONNECT_TOO_FAST_RECONNECT, false);
        cachedDetections[3] = new Detection(DetectionResult.DENIED, Messages.Values.DISCONNECT_TOO_MANY_ONLINE, false);
        cachedDetections[4] = new Detection(DetectionResult.DENIED, Messages.Values.DISCONNECT_ATTACK, false);
        cachedDetections[5] = new Detection(DetectionResult.DENIED, Messages.Values.DISCONNECT_BOT_BEHAVIOUR, false);
        cachedDetections[6] = new Detection(DetectionResult.DENIED, Messages.Values.DISCONNECT_VPN_OR_PROXY, false);
        cachedDetections[9] = new Detection(DetectionResult.ALLOWED, null, false);
    }

    public Detection check(final ConnectionData connectionData, final PlayerHandler handler) throws Exception {
        final boolean underAttack = Sensibility.isUnderAttackJoins();

        if (connectionData.username.length() > Config.Values.MAX_NAME_LENGTH
                || !connectionData.username.matches(Config.Values.NAME_VALIDATION_REGEX)) {
            if ((Config.Values.REGEX_BLACKLIST_MODE == CustomRegexOptions.DURING_ATTACK && underAttack)
                    || Config.Values.REGEX_BLACKLIST_MODE == CustomRegexOptions.ALWAYS) {
                //System.out.println(connectionData.username + " - regex [1]");
                return cachedDetections[0];
            }

            connectionData.checkState = 0;
            //System.out.println(connectionData.username + " - invalid name, cs reset");
            return cachedDetections[1];
        }

        reconnect_check: {
            if (connectionData.checkState == 0) {
                connectionData.checkState = 1;
                connectionData.verifiedName = connectionData.username;
                //System.out.println(connectionData.username + " - cache username for verification cs=1");
                break reconnect_check;
            }

            if (connectionData.checkState == 1) {
                connectionData.checkState = 2;

                if (Config.Values.ENABLE_RECONNECT_CHECK
                        && !connectionData.verifiedName.equals(connectionData.username)) {
                    connectionData.threatScore++;
                    //System.out.println(connectionData.username + " - TS=" + connectionData.threatScore + " VN=" + connectionData.verifiedName + " [ne] cs=" + connectionData.checkState);
                    return cachedDetections[0];
                }
            }
        }

        final long timeStamp = System.currentTimeMillis();

        if (timeStamp - connectionData.lastJoinTimestamp <= Config.Values.REJOIN_DELAY) {
            connectionData.checkState = 2;
            connectionData.failedReconnectAttempts++;
            connectionData.threatScore++;

            connectionData.lastJoinTimestamp = (timeStamp - (Config.Values.REJOIN_DELAY / 2L));
            //System.out.println(connectionData.username + " - too fast reconnect");
            return cachedDetections[2];
        } else if (connectionData.threatScore > 0) {
            connectionData.threatScore--;
        }

        connectionData.lastJoinTimestamp = timeStamp;

        if (Config.Values.CUSTOM_REGEXES.stream().anyMatch(connectionData.username::matches)) {
            if ((Config.Values.REGEX_BLACKLIST_MODE == CustomRegexOptions.DURING_ATTACK && underAttack)
                    || Config.Values.REGEX_BLACKLIST_MODE == CustomRegexOptions.ALWAYS) {
                //System.out.println(connectionData.username + " - regex always");
                return cachedDetections[0];
            }

            connectionData.threatScore++;

            if ((Config.Values.REGEX_CHECK_MODE == CustomRegexOptions.DURING_ATTACK && underAttack)
                    || Config.Values.REGEX_CHECK_MODE == CustomRegexOptions.ALWAYS) {
                connectionData.checkState = 0;
                //System.out.println(connectionData.username + " - regex, reset threat score");
                return cachedDetections[1];
            }
        }

        if (underAttack && !Whitelist.isWhitelisted(connectionData.inetAddress)) {
            if (connectionData.checkState == 2) {
                connectionData.checkState = 3;
                connectionData.underAttackChecks++;

                if (connectionData.failedReconnectAttempts > 2
                        && connectionData.underAttackChecks < connectionData.failedReconnectAttempts) {
                    connectionData.threatScore++;
                    //System.out.println(connectionData.username + " - threat score increment; wl check under attack");
                }
                //System.out.println(connectionData.username + " - threat score check reset");
                return cachedDetections[4];
            }

            PlayerQueue.addToQueue(connectionData.username);

            if (PlayerQueue.getPosition(connectionData.username) > 1) {
                //System.out.println(connectionData.username + " - added to queue");
                return new Detection(DetectionResult.DENIED,
                        Messages.Values.DISCONNECT_QUEUED
                        .replaceAll("%position%", SonarBungee.INSTANCE.FORMAT.format(PlayerQueue.getPosition(connectionData.username)))
                        .replaceAll("%size%", SonarBungee.INSTANCE.FORMAT.format(PlayerQueue.QUEUE.size())),
                        false);
            }
        } else {
            //System.out.println(connectionData.username + " - fine - reset");
            connectionData.underAttackChecks = 0;
            connectionData.failedReconnectAttempts = 0;
        }

        if (Config.Values.MAXIMUM_ONLINE_PER_IP > 1 && connectionData.getAccountsOnlineWithSameIP() > Config.Values.MAXIMUM_ONLINE_PER_IP) {
            connectionData.checkState = 2;
            //System.out.println(connectionData.username + " - check 2");
            return cachedDetections[3];
        }

        // strong intelligent bot detection
        // TODO: update this?
        if (connectionData.threatScore > 0) {
            if (connectionData.threatScore > 4) {
                connectionData.threatScore = 3;
                //System.out.println(connectionData.username + " - threat score check 1");
                return cachedDetections[5];
            }

            if (connectionData.failedReconnectAttempts < 3) {
                //System.out.println(connectionData.username + " - threat score decrement");
                connectionData.threatScore--;
            }
        }

        final PlayerData playerData = PlayerDataManager.create(connectionData.username);

        // don't let bots reconnect too quickly
        if (timeStamp - playerData.lastDetection < Config.Values.REJOIN_DELAY * 2L) {
            connectionData.threatScore++;
            //System.out.println(playerData.username + " - threat score " + connectionData.threatScore);
            return cachedDetections[5];
        }

        if (Config.Values.ENABLE_PROXY_CHECK
                && SonarBungee.INSTANCE.selectedAntiProxyProvider != null
                && !connectionData.inetAddress.isLoopbackAddress()) {

            // Run this synchronously but the actual vpn check asynchronously to prevent lag
            if (SonarBungee.INSTANCE.selectedAntiProxyProvider.isInProxyCache(connectionData.inetAddress)) {
                //System.out.println(playerData.username + " - proxy cache");
                return cachedDetections[6];
            }

            // Async vpn check
            // TODO: is this really async? lol
            handler.ctx.channel().eventLoop().execute(() -> {
                if (SonarBungee.INSTANCE.selectedAntiProxyProvider.isUsingProxy(connectionData.inetAddress)) {
                    //System.out.println(playerData.username + " - proxy new");
                    handler.disconnect_(Messages.Values.DISCONNECT_VPN_OR_PROXY);
                }
            });
        }
        //System.out.println("ALLOWING " + playerData.username);
        return cachedDetections[9];
    }
}
