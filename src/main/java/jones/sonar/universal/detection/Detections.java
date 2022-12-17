package jones.sonar.universal.detection;

public interface Detections {
    Detection FIRST_JOIN_KICK    = new Detection(DetectionResult.DENIED,
            1);

    Detection INVALID_NAME       = new Detection(DetectionResult.DENIED,
            2);

    Detection TOO_FAST_RECONNECT = new Detection(DetectionResult.DENIED,
            3);

    Detection TOO_MANY_ONLINE    = new Detection(DetectionResult.DENIED,
            4);

    Detection PLAYER_IN_QUEUE    = new Detection(DetectionResult.DENIED,
            5);

    Detection DURING_ATTACK      = new Detection(DetectionResult.DENIED,
            6);

    Detection SUSPICIOUS         = new Detection(DetectionResult.DENIED,
            7);

    Detection VPN_OR_PROXY       = new Detection(DetectionResult.DENIED,
            8);

    Detection BLACKLIST          = new Detection(DetectionResult.DENIED,
            0);

    Detection ALLOW              = new Detection(DetectionResult.ALLOWED,
            0);
}
