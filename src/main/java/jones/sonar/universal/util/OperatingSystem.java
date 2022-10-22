package jones.sonar.universal.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OperatingSystem {
    public final String OS_NAME = System.getProperty("os.name");

    public final String OS_VERSION = System.getProperty("os.version");

    public final String OS_ARCH = System.getProperty("os.arch");

    public String getOSName() {
        if (OS_NAME.toLowerCase().contains("unix")) {
            return "Unix / Linux";
        }
        if (OS_NAME.toLowerCase().contains("os")) {
            return "macOS";
        }
        if (OS_NAME.toLowerCase().contains("wind")) {
            return "Windows";
        }
        if (OS_NAME.toLowerCase().contains("linu")) {
            return "Linux";
        }
        return "unknown";
    }
}
