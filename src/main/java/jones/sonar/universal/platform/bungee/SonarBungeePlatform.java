package jones.sonar.universal.platform.bungee;

public interface SonarBungeePlatform {
    default String getVersion() {
        return SonarBungee.INSTANCE.getPlugin().getDescription().getVersion();
    }

    default void disable() {
        SonarBungee.INSTANCE.getPlugin().onDisable();
    }
}
