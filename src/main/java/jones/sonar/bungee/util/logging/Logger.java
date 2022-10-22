package jones.sonar.bungee.util.logging;

import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.RequiredArgsConstructor;

import java.util.logging.Level;

@RequiredArgsConstructor
public enum Logger implements ILogger {

    WARNING(Level.WARNING),
    ERROR(Level.SEVERE),
    INFO(Level.INFO);

    private final Level logLevel;

    @Override
    public void log(final String data) {
        SonarBungee.INSTANCE.proxy.getLogger().log(logLevel, "[Sonar] " + data);
    }

    @Override
    public void log(final String data, final String prefix) {
        SonarBungee.INSTANCE.proxy.getLogger().log(logLevel, prefix + " " + data);
    }

    @Override
    public void logNoPrefix(final String data) {
        SonarBungee.INSTANCE.proxy.getLogger().log(logLevel, data);
    }
}
