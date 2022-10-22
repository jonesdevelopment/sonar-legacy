package jones.sonar.bungee.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.chat.*;

@UtilityClass
public class LegacyGsonFormat {
    public final Gson LEGACY = new GsonBuilder()
            .registerTypeAdapter(BaseComponent.class, new ComponentSerializer())
            .registerTypeAdapter(TextComponent.class, new TextComponentSerializer())
            .registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer())
            .registerTypeAdapter(KeybindComponent.class, new KeybindComponentSerializer())
            .registerTypeAdapter(ScoreComponent.class, new ScoreComponentSerializer())
            .registerTypeAdapter(SelectorComponent.class, new SelectorComponentSerializer())
            .registerTypeAdapter(ServerPing.PlayerInfo.class, new PlayerInfoToJson(4))
            .registerTypeAdapter(Favicon.class, Favicon.getFaviconTypeAdapter()).create();
}
