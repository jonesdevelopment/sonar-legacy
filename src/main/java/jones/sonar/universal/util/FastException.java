package jones.sonar.universal.util;

public final class FastException extends RuntimeException {

    @Override
    public Throwable initCause(final Throwable cause) {
        return this;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
