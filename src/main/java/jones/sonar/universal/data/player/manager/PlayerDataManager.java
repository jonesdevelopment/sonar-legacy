package jones.sonar.universal.data.player.manager;

import jones.sonar.universal.data.player.PlayerData;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class PlayerDataManager {
    private final Map<String, PlayerData> DATA = new HashMap<>();

    public PlayerData get(final String playerName) {
        return DATA.get(playerName);
    }

    public PlayerData getOrDefault(final String playerName, final PlayerData def) {
        return DATA.getOrDefault(playerName, def);
    }

    public boolean contains(final String playerName) {
        return DATA.containsKey(playerName);
    }

    public boolean remove(final PlayerData playerData) {
        return remove(playerData.username);
    }

    public boolean remove(final String playerName) {
        if (contains(playerName)) {
            DATA.remove(playerName);
            return true;
        }
        return false;
    }

    public PlayerData create(final String username) {
        if (contains(username)) {
            return get(username);
        }

        DATA.put(username, new PlayerData(username));

        return DATA.get(username);
    }
}
