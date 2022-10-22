package jones.sonar.bungee.util.logging;

public interface ILogger {
    void log(final String data);

    void logNoPrefix(final String data);

    void log(final String data, final String prefix);
}
