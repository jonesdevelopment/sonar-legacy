package jones.sonar.universal.webhook;

import jones.sonar.universal.webhook.embed.EmbedObject;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class WebhookSender {
    private final WebhookIntegration webhook = new WebhookIntegration();

    public String URL = "";

    public void sendWebhook(final String content, final String title, final Color color) {
        webhook.webhookUrl = URL;

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        final LocalDateTime localDateTime = LocalDateTime.now();

        webhook.embeds.add(new EmbedObject()
                .setDescription(content)
                .setTitle(title)
                .setColor(color)
                .setFooter(formatter.format(localDateTime), ""));

        try {
            webhook.execute();

            webhook.embeds.clear();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
