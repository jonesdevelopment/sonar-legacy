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

import java.util.Objects;

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
                return cachedDetections[0];
            }

            connectionData.checked = 0;
            return cachedDetections[1];
        }

        if (connectionData.checked == 0) {
            connectionData.checked = 1;
            connectionData.verifiedName = connectionData.username;

            /*connectionData.lastJoin = timeStamp;
            return FIRST_JOIN_KICK;*/
        }

        if (connectionData.checked == 1) {
            connectionData.checked = 2;

            if (!Objects.equals(connectionData.verifiedName, connectionData.username)
                    && !connectionData.allowedNames.contains(connectionData.username)
                    && Config.Values.ENABLE_RECONNECT_CHECK) {
                return cachedDetections[0];
            }
        }

        final long timeStamp = System.currentTimeMillis();

        if (timeStamp - connectionData.lastJoin <= Config.Values.REJOIN_DELAY) {
            connectionData.checked = 2;
            connectionData.failedReconnect++;

            connectionData.lastJoin = (timeStamp - (Config.Values.REJOIN_DELAY / 2L));
            return cachedDetections[2];
        } else if (connectionData.botLevel > 0) {
            connectionData.botLevel--;
        }

        connectionData.lastJoin = timeStamp;

        if (!connectionData.verifiedNames.contains(connectionData.username)
                && !Objects.equals(connectionData.verifiedName, connectionData.username)) {
            connectionData.verifiedNames.add(connectionData.username);

            connectionData.botLevel++;
            //return FIRST_JOIN_KICK;
        }

        if (Config.Values.CUSTOM_REGEXES.stream().anyMatch(connectionData.username::matches)) {
            if ((Config.Values.REGEX_BLACKLIST_MODE == CustomRegexOptions.DURING_ATTACK && underAttack)
                    || Config.Values.REGEX_BLACKLIST_MODE == CustomRegexOptions.ALWAYS) {
                return cachedDetections[0];
            }

            connectionData.botLevel++;

            if ((Config.Values.REGEX_CHECK_MODE == CustomRegexOptions.DURING_ATTACK && underAttack)
                    || Config.Values.REGEX_CHECK_MODE == CustomRegexOptions.ALWAYS) {
                connectionData.checked = 0;
                return cachedDetections[1];
            }
        }

        if (underAttack && !Whitelist.isWhitelisted(connectionData.inetAddress)) {
            if (connectionData.checked == 2) {
                connectionData.checked = 3;
                connectionData.underAttackChecks++;

                if (connectionData.failedReconnect > 2
                        && connectionData.underAttackChecks < connectionData.failedReconnect) {
                    connectionData.botLevel++;
                }
                return cachedDetections[4];
            }

            PlayerQueue.addToQueue(connectionData.username);

            if (PlayerQueue.getPosition(connectionData.username) > 1) {
                return new Detection(DetectionResult.DENIED,
                        Messages.Values.DISCONNECT_QUEUED
                        .replaceAll("%position%", SonarBungee.INSTANCE.FORMAT.format(PlayerQueue.getPosition(connectionData.username)))
                        .replaceAll("%size%", SonarBungee.INSTANCE.FORMAT.format(PlayerQueue.QUEUE.size())),
                        false);
            }
        } else {
            connectionData.underAttackChecks = 0;
            connectionData.failedReconnect = 0;
        }

        final long online = connectionData.getAccountsOnlineWithSameIP();

        if (online > Config.Values.MAXIMUM_ONLINE_PER_IP) {
            connectionData.checked = 2;
            return cachedDetections[3];
        }

        // strong intelligent bot detection
        // TODO: update this?
        if (connectionData.botLevel > 0) {
            if (connectionData.botLevel > 4) {
                connectionData.botLevel = 3;
                return cachedDetections[5];
            }

            if (connectionData.failedReconnect < 3) {
                connectionData.botLevel--;
            }
        }

        connectionData.allowedNames.add(connectionData.username);

        final PlayerData playerData = PlayerDataManager.create(connectionData.username);

        // don't let bots reconnect too quickly
        if (timeStamp - playerData.lastDetection < Config.Values.REJOIN_DELAY * 2L) {
            connectionData.botLevel++;
            return cachedDetections[5];
        }

        if (Config.Values.ENABLE_PROXY_CHECK
                && SonarBungee.INSTANCE.selectedAntiProxyProvider != null
                && !connectionData.inetAddress.isLoopbackAddress()) {

            // Run this synchronously but the actual vpn check asynchronously to prevent lag
            if (SonarBungee.INSTANCE.selectedAntiProxyProvider.isInProxyCache(connectionData.inetAddress)) {
                return cachedDetections[6];
            }

            // Async vpn check
            // TODO: is this really async? lol
            handler.ctx.channel().eventLoop().execute(() -> {
                if (SonarBungee.INSTANCE.selectedAntiProxyProvider.isUsingProxy(connectionData.inetAddress)) {
                    handler.disconnect_(Messages.Values.DISCONNECT_VPN_OR_PROXY);
                }
            });
        }
        return cachedDetections[9];
    }
}
