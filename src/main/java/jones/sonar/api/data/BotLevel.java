package jones.sonar.api.data;

import jones.sonar.api.APIClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;

@Getter
@APIClass(since = "1.3.1")
@RequiredArgsConstructor
public final class BotLevel {
    private final long onlineUsers;
    private final long level;
    private final InetAddress inetAddress;
}
