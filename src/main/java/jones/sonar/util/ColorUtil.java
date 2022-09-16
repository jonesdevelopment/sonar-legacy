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

package jones.sonar.util;

import jones.sonar.config.Config;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ColorUtil {
    private final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");

    public String format(final String data) {
        return formatColorCodeOnly(formatHex(data));
    }

    public String formatColorCodeOnly(final String data) {
        return ChatColor.translateAlternateColorCodes('&', data);
    }

    public String formatHex(String data) {
        Matcher matcher = HEX_PATTERN.matcher(data);

        while (matcher.find()) {
            final String color = data.substring(matcher.start(), matcher.end());

            data = data.replace(color, ChatColor.of(color.replace("&", "")) + "");

            matcher = HEX_PATTERN.matcher(data);
        }

        return formatColorCodeOnly(data);
    }

    public String format(final String data, final char color) {
        return ChatColor.translateAlternateColorCodes(color, data);
    }

    public String getColorForCounter(final long counterResult) {
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND) return "§a";
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 5L) return "§e";
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 20L) return "§6";
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 40L) return "§c";
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 70L) return "§4";
        return "§f";
    }
}
