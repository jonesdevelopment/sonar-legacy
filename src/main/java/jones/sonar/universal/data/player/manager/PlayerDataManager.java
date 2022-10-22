package jones.sonar.universal.data.player.manager;

import jones.sonar.universal.data.player.PlayerData;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class PlayerDataManager {
    public final Map<String, PlayerData> DATA = new ConcurrentHashMap<>(300000);

    public PlayerData get(final String playerName) {
        if (contains(playerName)) {
            return DATA.get(playerName);
        }

        return null;
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
