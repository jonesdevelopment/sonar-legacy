package jones.sonar.bungee.network.handler.state;

public enum ConnectionState {
    HANDSHAKE,
    STATUS,
    PINGING,
    JOINING,
    PROCESSING
}
