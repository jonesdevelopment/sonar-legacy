package jones.sonar.universal.queue;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class PlayerQueue {

    public final Map<String, Long> QUEUE = new ConcurrentHashMap<>(50000);

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
        if (contains(playerName)) {
            return QUEUE.get(playerName);
        } else {
            return -1;
        }
    }

    public long getPositionOrCreate(final String playerName) {
        if (!contains(playerName)) {
            addToQueue(playerName);
        }
        return QUEUE.get(playerName);
    }
}
