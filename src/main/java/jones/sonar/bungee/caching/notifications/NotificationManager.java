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

package jones.sonar.bungee.caching.notifications;

import jones.sonar.SonarBungee;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.util.Sensibility;
import jones.sonar.bungee.util.logging.Logger;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.util.PerformanceMonitor;
import jones.sonar.universal.webhook.WebhookSender;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@UtilityClass
public class NotificationManager {
    public final Set<String> SUBSCRIBED = new HashSet<>();

    private long lastNotification = 0L, lastWebhook = 0L;

    public void checkForAttack() {
        if (Sensibility.isUnderAttack()) {
            final long timeStamp = System.currentTimeMillis();

            // if the currentlyUnderAttack is already set, we want to reset
            // the timer to sure the timer is working perfectly and doesn't cause issues
            if (Sensibility.currentlyUnderAttack) {
                Sensibility.sinceLastAttack = timeStamp;
            }

            // we want accurate counting, that's why we need to wait one second
            // after an attack was detected to ensure the results (cps, ips, ...)
            // are valid and accurate.
            else if (timeStamp - Sensibility.sinceLastAttack > 1000L) {
                Sensibility.currentlyUnderAttack = true;

                // check if the last notification was sent more than 15 seconds ago
                // to prevent chat spam
                if (timeStamp - lastNotification > Messages.Values.NOTIFY_DELAY) {
                    final String cps = SonarBungee.INSTANCE.FORMAT.format(Counter.CONNECTIONS_PER_SECOND.get()),
                            ips = SonarBungee.INSTANCE.FORMAT.format(Counter.IPS_PER_SECOND.get()),
                            joins = SonarBungee.INSTANCE.FORMAT.format(Counter.JOINS_PER_SECOND.get()),
                            pings = SonarBungee.INSTANCE.FORMAT.format(Counter.PINGS_PER_SECOND.get()),
                            encryptions = SonarBungee.INSTANCE.FORMAT.format(Counter.ENCRYPTIONS_PER_SECOND.get());

                    final String alert = Messages.Values.NOTIFY_FORMAT
                            .replaceAll("%cps%", cps)
                            .replaceAll("%ips%", ips)
                            .replaceAll("%joins%", joins)
                            .replaceAll("%pings%", pings)
                            .replaceAll("%encryptions%", encryptions)
                            .replaceAll("%cpu%", PerformanceMonitor.formatCPULoad())
                            .replaceAll("%cpu-avg%", PerformanceMonitor.formatAverageCPULoad());

                    // broadcast the notification to each player who has notifications enabled
                    SUBSCRIBED.stream()
                            .map(SonarBungee.INSTANCE.proxy::getPlayer)
                            .filter(Objects::nonNull)
                            .forEach(player -> player.sendMessage(alert));

                    // check if the discord webhook is enabled
                    if (Config.Values.WEBHOOK_ENABLED && timeStamp - lastWebhook >= Config.Values.WEBHOOK_DELAY) {

                        // check if the webhook url is valid
                        if (Config.Values.WEBHOOK_URL.toLowerCase().startsWith("https://discord.com/api/webhooks/")) {

                            // set the webhook url
                            WebhookSender.URL = Config.Values.WEBHOOK_URL;

                            // send the webhook
                            WebhookSender.sendWebhook(Config.Values.WEBHOOK_FORMAT
                                            .replaceAll("%cps%", cps)
                                            .replaceAll("%ips%", ips)
                                            .replaceAll("%joins%", joins)
                                            .replaceAll("%pings%", pings)
                                            .replaceAll("%online%", SonarBungee.INSTANCE.FORMAT.format(SonarBungee.INSTANCE.proxy.getPlayers().size()))
                                            .replaceAll("%cpu%", PerformanceMonitor.formatCPULoad())
                                            .replaceAll("%cpu-avg%", PerformanceMonitor.formatAverageCPULoad())
                                            .replaceAll("%encryptions%", encryptions),
                                    Config.Values.WEBHOOK_TITLE,
                                    new Color(Config.Values.WEBHOOK_COLOR_R,
                                            Config.Values.WEBHOOK_COLOR_G,
                                            Config.Values.WEBHOOK_COLOR_B));

                            lastWebhook = timeStamp;
                        } else {
                            Logger.INFO.log("§cYou provided an invalid Webhook URL. §7(messages.yml)");
                        }
                    }

                    // reset the lastNotification timer
                    lastNotification = timeStamp;
                }
            }
        } else {

            // The server is not under attack anymore
            Sensibility.currentlyUnderAttack = false;
        }
    }

    public boolean toggle(final ProxiedPlayer player) {
        if (!contains(player.getName())) {
            subscribe(player.getName());
        } else {
            unsubscribe(player.getName());
        }
        return contains(player.getName());
    }

    public void subscribe(final String playerName) {
        if (!contains(playerName)) {
            SUBSCRIBED.add(playerName);
        }
    }

    public void unsubscribe(final String playerName) {
        if (contains(playerName)) {
            SUBSCRIBED.remove(playerName);
        }
    }

    public boolean contains(final String playerName) {
        return SUBSCRIBED.contains(playerName);
    }
}
