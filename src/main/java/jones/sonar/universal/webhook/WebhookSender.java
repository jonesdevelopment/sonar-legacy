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
