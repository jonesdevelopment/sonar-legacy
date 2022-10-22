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
