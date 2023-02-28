package jones.sonar.universal.data.player;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PlayerData {
    public final String username;

    public String clientBrand = "/";

    public long lastDetection = 0L, keepAliveSent = 0L;

    public boolean sentClientSettings = false, sentClientBrand = false;

    public void handleLogin() {
        sentClientBrand = false;
        sentClientSettings = false;
        clientBrand = "/";
    }

    public boolean passes() {
        return clientBrand.length() > 3
                && sentClientBrand
                && sentClientSettings;
    }
}
