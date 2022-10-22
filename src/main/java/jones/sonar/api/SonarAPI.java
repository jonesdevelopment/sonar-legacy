package jones.sonar.api;

public final class SonarAPI implements SonarAPIImpl {
    public static SonarAPI newInstance() {
        return new SonarAPI();
    }
}
