package jones.sonar.api.event.bungee;

import jones.sonar.api.APIClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Event;

@Getter
@APIClass(since = "1.3.1")
@RequiredArgsConstructor
public final class SonarWebhookSentEvent extends Event {
    private final String webhookUrl;
}
