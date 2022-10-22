package jones.sonar.api.event.bungee;

import jones.sonar.api.APIClass;
import jones.sonar.api.enums.PeakType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Event;

@Getter
@APIClass(since = "1.3.1")
@RequiredArgsConstructor
public final class SonarPeakResetEvent extends Event {
    private final PeakType peakType;
}
