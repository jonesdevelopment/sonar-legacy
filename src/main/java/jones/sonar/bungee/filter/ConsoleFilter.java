package jones.sonar.bungee.filter;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;

@UtilityClass
public final class ConsoleFilter {
    public void apply() {
        if (isLog4J()) {
            new Log4JConsoleFilter(); // load the log4j filter if detected
            return;
        }

        new DefaultConsoleFilter();
    }

    private boolean isLog4J() {
        try {
            LogManager.getRootLogger();
            return true;
        } catch (NoClassDefFoundError classDefFoundError) {
            return false;
        }
    }
}
