package jones.sonar.universal.webhook;

import jones.sonar.bungee.config.Config;
import jones.sonar.universal.webhook.embed.EmbedObject;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class WebhookSender {
    private final WebhookIntegration webhook = new WebhookIntegration();

    public void sendWebhook(final String content, final String title, final Color color, final boolean embed) {
        if (!Config.Values.WEBHOOK_PING.isEmpty()) {
            webhook.setContent("<@" + Config.Values.WEBHOOK_PING + ">");

            run();
        }

        if (embed) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            final LocalDateTime localDateTime = LocalDateTime.now();

            webhook.embeds.add(new EmbedObject()
                    .setDescription(content)
                    .setTitle(title)
                    .setColor(color)
                    .setFooter(formatter.format(localDateTime), ""));
        } else {
            webhook.setContent(content);
        }

        run();
    }

    private void run() {
        try {
            webhook.webhookUrl = Config.Values.WEBHOOK_URL;

            webhook.setUsername(Config.Values.WEBHOOK_USERNAME);

            webhook.execute();

            webhook.embeds.clear();

            webhook.setContent(null);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
