package jones.sonar.universal.webhook.embed.object;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Field {
    public final String name, value;

    public final boolean inline;
}
