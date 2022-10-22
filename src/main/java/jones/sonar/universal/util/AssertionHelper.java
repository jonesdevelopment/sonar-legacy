package jones.sonar.universal.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AssertionHelper {
    public void check(final boolean condition, final String exception) {
        if (!condition) {
            throw new AssertionError(exception);
        }
    }

    @Deprecated
    public void check(final boolean condition) {
        check(condition, "Error while parsing condition");
    }
}
