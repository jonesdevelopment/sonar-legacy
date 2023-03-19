package jones.sonar.universal.queue;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class PlayerQueue {

    public final Map<String, Long> QUEUE = new HashMap<>();

    public void remove(final String playerName) {
        if (contains(playerName)) {
            QUEUE.remove(playerName);
        }
    }

    public void addToQueue(final String playerName) {
        if (!contains(playerName)) {
            QUEUE.put(playerName, QUEUE.size() + 1L);
        }
    }

    public boolean contains(final String playerName) {
        return QUEUE.containsKey(playerName);
    }

    public long getPosition(final String playerName) {
        return QUEUE.getOrDefault(playerName, -1L);
    }

    public long getPositionOrCreate(final String playerName) {
        addToQueue(playerName);
        return QUEUE.get(playerName);
    }
}
