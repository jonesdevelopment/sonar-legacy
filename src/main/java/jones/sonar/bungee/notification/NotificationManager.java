package jones.sonar.bungee.notification;

import jones.sonar.api.event.bungee.SonarAttackDetectedEvent;
import jones.sonar.api.event.bungee.SonarWebhookSentEvent;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.util.logging.Logger;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.platform.SonarPlatform;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.PerformanceMonitor;
import jones.sonar.universal.util.Sensibility;
import jones.sonar.universal.util.logging.AttackLogger;
import jones.sonar.universal.webhook.WebhookSender;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@UtilityClass
public class NotificationManager {

    private final Set<String> SUBSCRIBED = new HashSet<>();

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

                final long ips = Counter.IPS_PER_SECOND.get();

                // check if the last notification was sent more than 15 seconds ago
                // to prevent chat spam
                if (timeStamp - lastNotification > Messages.Values.NOTIFY_DELAY

                        // we want the peak to only show when there's an actual attack
                        && ips > Config.Values.MINIMUM_JOINS_PER_SECOND) {

                    // cache all variables
                    final long cps = Counter.CONNECTIONS_PER_SECOND.get(),
                            joins = Counter.JOINS_PER_SECOND.get(),
                            pings = Counter.PINGS_PER_SECOND.get(),
                            encryptions = Counter.ENCRYPTIONS_PER_SECOND.get();

                    // save the alert message dynamically to save performance
                    final String alert = Messages.Values.NOTIFY_FORMAT
                            .replaceAll("%cps%", SonarBungee.INSTANCE.FORMAT.format(cps))
                            .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(ips))
                            .replaceAll("%logins%", SonarBungee.INSTANCE.FORMAT.format(joins))
                            .replaceAll("%pings%", SonarBungee.INSTANCE.FORMAT.format(pings))
                            .replaceAll("%encryptions%", SonarBungee.INSTANCE.FORMAT.format(encryptions))
                            .replaceAll("%cpu%", PerformanceMonitor.formatCPULoad())
                            .replaceAll("%cpu-avg%", PerformanceMonitor.formatAverageCPULoad());

                    if (timeStamp - Sensibility.sinceLastAttack > 5000L) {
                        try {
                            AttackLogger.logIncomingAttack(SonarPlatform.BUNGEE);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }

                    SonarBungee.INSTANCE.callEvent(new SonarAttackDetectedEvent(cps, ips, joins, pings, encryptions));

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
                                            .replaceAll("%cps%", SonarBungee.INSTANCE.FORMAT.format(cps))
                                            .replaceAll("%ips%", SonarBungee.INSTANCE.FORMAT.format(ips))
                                            .replaceAll("%logins%", SonarBungee.INSTANCE.FORMAT.format(joins))
                                            .replaceAll("%pings%", SonarBungee.INSTANCE.FORMAT.format(pings))
                                            .replaceAll("%encryptions%", SonarBungee.INSTANCE.FORMAT.format(Counter.ENCRYPTIONS_PER_SECOND.get()))
                                            .replaceAll("%online%", SonarBungee.INSTANCE.FORMAT.format(SonarBungee.INSTANCE.proxy.getPlayers().size()))
                                            .replaceAll("%cpu%", PerformanceMonitor.formatCPULoad())
                                            .replaceAll("%cpu-avg%", PerformanceMonitor.formatAverageCPULoad()),
                                    Config.Values.WEBHOOK_TITLE,
                                    new Color(Config.Values.WEBHOOK_COLOR_R,
                                            Config.Values.WEBHOOK_COLOR_G,
                                            Config.Values.WEBHOOK_COLOR_B));

                            SonarBungee.INSTANCE.callEvent(new SonarWebhookSentEvent(Config.Values.WEBHOOK_URL));

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
