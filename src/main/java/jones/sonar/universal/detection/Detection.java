package jones.sonar.universal.detection;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
public final class Detection {
    public final DetectionResult result;
    public final @Nullable String kickReason;
    public final boolean blacklist;
}
