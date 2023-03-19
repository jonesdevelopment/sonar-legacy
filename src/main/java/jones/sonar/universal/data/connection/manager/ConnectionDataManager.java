package jones.sonar.universal.data.connection.manager;

import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.connection.ConnectionData;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class ConnectionDataManager {
    public final Map<InetAddress, ConnectionData> DATA = new ConcurrentHashMap<>(500000);

    public long getVerifying() {
        return getVerifyingData().count();
    }

    public Stream<ConnectionData> getVerifyingData() {
        return DATA.values().stream()
                .filter(connectionData -> connectionData.checkState <= 1);
    }

    public ConnectionData get(final InetAddress inetAddress) {
        if (contains(inetAddress)) {
            return DATA.get(inetAddress);
        }

        return null;
    }

    public boolean contains(final InetAddress inetAddress) {
        return DATA.containsKey(inetAddress);
    }

    public void resetCheckStage(final int newStage) {
        DATA.values().forEach(data -> data.checkState = newStage);
    }

    public void removeAllUnused() {
        DATA.keySet().stream()
                .filter(Blacklist::isBlacklisted)
                .collect(Collectors.toSet())
                .forEach(DATA::remove);
    }

    public void remove(final ConnectionData connectionData) {
        DATA.remove(connectionData.inetAddress);
    }

    public void remove(final InetAddress inetAddress) {
        DATA.remove(inetAddress);
    }

    public ConnectionData create(final InetAddress inetAddress) {
        final ConnectionData got = get(inetAddress);

        if (got != null) {
            return got;
        }

        DATA.put(inetAddress, new ConnectionData(inetAddress));

        return DATA.get(inetAddress);
    }
}
