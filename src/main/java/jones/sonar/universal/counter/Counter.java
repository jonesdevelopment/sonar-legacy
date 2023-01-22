package jones.sonar.universal.counter;

public interface Counter {
    CounterMap CONNECTIONS_PER_SECOND       = new CounterMap(1000).build();
    CounterMap JOINS_PER_SECOND             = new CounterMap(1000).build();
    CounterMap HANDSHAKES_PER_SECOND        = new CounterMap(1000).build();
    CounterMap PINGS_PER_SECOND             = new CounterMap(1000).build();
    CounterMap STATUSES_PER_SECOND          = new CounterMap(1000).build();
    CounterMap IPS_PER_SECOND               = new CounterMap(1000).build();
    CounterMap ENCRYPTIONS_PER_SECOND       = new CounterMap(1000).build();
}
