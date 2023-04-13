package jones.sonar.bungee.util;

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.RequiredArgsConstructor;

import java.util.logging.Level;

@RequiredArgsConstructor
public enum Logger {

    WARNING(Level.WARNING),
    ERROR(Level.SEVERE),
    INFO(Level.INFO);

    private final Level logLevel;

    public void log(final String data) {
        SonarBungee.INSTANCE.proxy.getLogger().log(logLevel, "[Sonar] " + data);
    }

    public void log(final String data, final String prefix) {
        SonarBungee.INSTANCE.proxy.getLogger().log(logLevel, prefix + " " + data);
    }
}
