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

import jones.sonar.bungee.config.Config;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;

@UtilityClass
public class ColorUtil {
    public String format(final String data) {
        return formatColorCodeOnly(formatHex(data));
    }

    public String formatColorCodeOnly(final String data) {
        return ChatColor.translateAlternateColorCodes('&', data);
    }

    public String formatHex(String data) {
        if (!data.matches("#[a-fA-F0-9]{6}")) return formatColorCodeOnly(data);

        final StringBuilder colorCodeBuilder = new StringBuilder();

        colorCodeBuilder.append("§x");

        for (final char character : data.toCharArray()) {
            if (character == '#') continue;

            colorCodeBuilder.append("&").append(character);
        }

        return formatColorCodeOnly(colorCodeBuilder.toString());
    }

    public String format(final String data, final char color) {
        return ChatColor.translateAlternateColorCodes(color, data);
    }

    public String getColorForCounter(final long counterResult) {
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 450L) return format("§4");
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 340L) return format("§c");
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 200L) return format("§6");
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 70L) return format("§e");
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND * 25L) return format("§a");
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND) return format("§2");
        return "§f";
    }

    @Deprecated
    public String getColorForCounterHEX(final long counterResult) {
        if (counterResult > Config.Values.MINIMUM_JOINS_PER_SECOND) {
            final long max = Config.Values.MINIMUM_JOINS_PER_SECOND * 450L;

            final double ratio = Math.max(Math.min(counterResult / (double) max, 1D), 0D);

            final Color color = new Color((int) (255 * ratio), (int) (255 - (255 * ratio)), 0);

            return format(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        }

        return "§f";
    }
}
