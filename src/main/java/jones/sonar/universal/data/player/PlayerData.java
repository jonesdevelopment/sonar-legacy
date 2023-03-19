package jones.sonar.universal.data.player;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PlayerData {
    public final String username;

    public String clientBrand = "/";

    public long lastDetection, keepAliveSent;

    public boolean sentClientSettings, sentClientBrand;

    public boolean passes() {
        return clientBrand.length() > 3
                && sentClientBrand
                && sentClientSettings;
    }
}
