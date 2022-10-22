package jones.sonar.bungee.network;

public interface SonarPipeline {

    String HANDLER = "sonar-handler", DECODER = "sonar-decoder";

    String PACKET_INTERCEPTOR = "sonar-packet-interceptor", LAST_PACKET_INTERCEPTOR = "sonar-packet-exception";

}
