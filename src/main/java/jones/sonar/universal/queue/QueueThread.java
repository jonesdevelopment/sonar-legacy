package jones.sonar.universal.queue;

import jones.sonar.bungee.config.Config;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class QueueThread extends Thread implements Runnable {

    public QueueThread() {
        super("sonar#queue");
    }

    @Override
    public void run() {
        while(true) {
            try {
                try {
                    if (!PlayerQueue.QUEUE.isEmpty()) {
                        final Map<String, Long> cleaned = new HashMap<>(PlayerQueue.QUEUE);

                        final AtomicInteger currentIndex = new AtomicInteger(0);

                        cleaned.forEach((name, position) -> {
                            if (position < Config.Values.QUEUE_POLL_RATE && currentIndex.get() <= Config.Values.MAXIMUM_QUEUE_POLL_RATE) {
                                currentIndex.incrementAndGet();

                                PlayerQueue.QUEUE.replace(name, PlayerQueue.getPosition(name) - 1L);

                                if (PlayerQueue.getPosition(name) < 1) {
                                    PlayerQueue.remove(name);
                                }
                            }
                        });

                        PlayerQueue.QUEUE.forEach((name, position) -> PlayerQueue.QUEUE.replace(name, position - currentIndex.get()));
                    }

                    ConnectionDataManager.removeAllUnused();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                Thread.sleep(500);
            } catch (InterruptedException exception) {
                break;
            }
        }
    }
}
