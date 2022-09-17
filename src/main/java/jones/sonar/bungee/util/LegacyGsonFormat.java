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

package jones.sonar.bungee.util;

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
