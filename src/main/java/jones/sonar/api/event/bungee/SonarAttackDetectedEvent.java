package jones.sonar.api.event.bungee;

import jones.sonar.api.APIClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Event;

@APIClass(since = "1.3.1")
@RequiredArgsConstructor
public final class SonarAttackDetectedEvent extends Event {

    @Getter
    private final long connectionsPerSecond, ipAddressesPerSecond, loginsPerSecond, pingsPerSecond, encryptionsPerSecond;

}
