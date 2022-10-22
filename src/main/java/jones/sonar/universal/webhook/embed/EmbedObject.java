package jones.sonar.universal.webhook.embed;

import jones.sonar.universal.webhook.embed.object.Image;
import jones.sonar.universal.webhook.embed.object.*;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class EmbedObject {

    @Getter
    private final List<Field> fields = new ArrayList<>();

    @Getter
    private String title, description, url;

    @Getter
    private Color color;

    @Getter
    private Footer footer;

    @Getter
    private Thumbnail thumbnail;

    @Getter
    private jones.sonar.universal.webhook.embed.object.Image image;

    @Getter
    private Author author;

    public EmbedObject setTitle(String title) {
        this.title = title;
        return this;
    }

    public EmbedObject setDescription(String description) {
        this.description = description;
        return this;
    }

    public EmbedObject setUrl(String url) {
        this.url = url;
        return this;
    }

    public EmbedObject setColor(Color color) {
        this.color = color;
        return this;
    }

    public EmbedObject setFooter(String text, String icon) {
        this.footer = new Footer(text, icon);
        return this;
    }

    public EmbedObject setThumbnail(String url) {
        this.thumbnail = new Thumbnail(url);
        return this;
    }

    public EmbedObject setImage(String url) {
        this.image = new Image(url);
        return this;
    }

    public EmbedObject setAuthor(String name, String url, String icon) {
        this.author = new Author(name, url, icon);
        return this;
    }
}
