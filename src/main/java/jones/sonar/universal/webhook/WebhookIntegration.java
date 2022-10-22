package jones.sonar.universal.webhook;

import jones.sonar.universal.webhook.embed.EmbedObject;
import jones.sonar.universal.webhook.embed.object.Image;
import jones.sonar.universal.webhook.embed.object.*;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.List;
import java.util.*;

public final class WebhookIntegration {

    protected final List<EmbedObject> embeds = new ArrayList<>();

    protected String webhookUrl;

    @Getter
    @Setter
    protected String content, username, avatarUrl;

    public void execute() throws IOException {
        if (content == null && embeds.isEmpty()) return;

        final JSONObject json = new JSONObject();

        json.put("content", content);
        json.put("username", username);
        json.put("avatar_url", avatarUrl);
        json.put("tts", false);

        if (!embeds.isEmpty()) {
            final List<JSONObject> embedObjects = new ArrayList<>();

            for (final EmbedObject embed : embeds) {
                final JSONObject jsonEmbed = new JSONObject();

                jsonEmbed.put("title", embed.getTitle());
                jsonEmbed.put("description", embed.getDescription());
                jsonEmbed.put("url", embed.getUrl());

                if (embed.getColor() != null) {
                    Color color = embed.getColor();
                    int rgb = color.getRed();
                    rgb = (rgb << 8) + color.getGreen();
                    rgb = (rgb << 8) + color.getBlue();

                    jsonEmbed.put("color", rgb);
                }

                final Footer footer = embed.getFooter();
                final Image image = embed.getImage();
                final Thumbnail thumbnail = embed.getThumbnail();
                final Author author = embed.getAuthor();
                final List<Field> fields = embed.getFields();

                if (footer != null) {
                    final JSONObject jsonFooter = new JSONObject();

                    jsonFooter.put("text", footer.text);
                    jsonFooter.put("icon_url", footer.iconUrl);
                    jsonEmbed.put("footer", jsonFooter);
                }

                if (image != null) {
                    final JSONObject jsonImage = new JSONObject();

                    jsonImage.put("url", image.url);
                    jsonEmbed.put("image", jsonImage);
                }

                if (thumbnail != null) {
                    final JSONObject jsonThumbnail = new JSONObject();

                    jsonThumbnail.put("url", thumbnail.url);
                    jsonEmbed.put("thumbnail", jsonThumbnail);
                }

                if (author != null) {
                    final JSONObject jsonAuthor = new JSONObject();

                    jsonAuthor.put("name", author.name);
                    jsonAuthor.put("url", author.url);
                    jsonAuthor.put("icon_url", author.iconUrl);
                    jsonEmbed.put("author", jsonAuthor);
                }

                final List<JSONObject> jsonFields = new ArrayList<>();

                for (final Field field : fields) {
                    final JSONObject jsonField = new JSONObject();

                    jsonField.put("name", field.name);
                    jsonField.put("value", field.value);
                    jsonField.put("inline", field.inline);

                    jsonFields.add(jsonField);
                }

                jsonEmbed.put("fields", jsonFields.toArray());
                embedObjects.add(jsonEmbed);
            }

            json.put("embeds", embedObjects.toArray());
        }

        final URL url = new URL(webhookUrl);

        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.addRequestProperty("Content-Type", "application/json");
        connection.addRequestProperty("User-Agent", "Java_Sonar_jonesdev_xyz");
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        final OutputStream stream = connection.getOutputStream();

        stream.write(json.toString().getBytes());
        stream.flush();
        stream.close();

        connection.getInputStream().close();
        connection.disconnect();
    }


    class JSONObject {

        private final HashMap<String, Object> map = new HashMap<>();

        private void put(String key, Object value) {
            if (value != null) {
                map.put(key, value);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            Set<Map.Entry<String, Object>> entrySet = map.entrySet();
            builder.append("{");

            int i = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                Object val = entry.getValue();
                builder.append(quote(entry.getKey())).append(":");

                if (val instanceof String) {
                    builder.append(quote(String.valueOf(val)));
                } else if (val instanceof Integer) {
                    builder.append(Integer.valueOf(String.valueOf(val)));
                } else if (val instanceof Boolean) {
                    builder.append(val);
                } else if (val instanceof JSONObject) {
                    builder.append(val.toString());
                } else if (val.getClass().isArray()) {
                    builder.append("[");
                    int len = Array.getLength(val);
                    for (int j = 0; j < len; j++) {
                        builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }

                builder.append(++i == entrySet.size() ? "}" : ",");
            }

            return builder.toString();
        }

        String quote(String string) {
            return "\"" + string + "\"";
        }
    }
}
