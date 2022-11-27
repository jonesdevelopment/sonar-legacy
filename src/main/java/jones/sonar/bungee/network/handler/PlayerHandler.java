package jones.sonar.bungee.network.handler;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import jones.sonar.bungee.caching.ServerDataProvider;
import jones.sonar.bungee.caching.ServerPingCache;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.detection.LoginHandler;
import jones.sonar.bungee.network.SonarPipeline;
import jones.sonar.bungee.network.SonarPipelines;
import jones.sonar.bungee.network.handler.packet.PacketHandler;
import jones.sonar.bungee.network.handler.state.ConnectionState;
import jones.sonar.bungee.util.json.LegacyGsonFormat;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.ServerStatistics;
import jones.sonar.universal.data.connection.ConnectionData;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.detection.Detection;
import jones.sonar.universal.detection.DetectionResult;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.queue.LoginCache;
import jones.sonar.universal.queue.PlayerQueue;
import jones.sonar.universal.util.ExceptionHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ConnectionThrottle;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.*;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public final class PlayerHandler extends InitialHandler implements SonarPipeline {

    public PlayerHandler(final ChannelHandlerContext ctx, final ListenerInfo listener, final ConnectionThrottle throttler) {
        super(BungeeCord.getInstance(), listener);

        this.ctx = ctx;

        pipeline = ctx.channel().pipeline();
        this.throttler = throttler;
    }

    private ConnectionState currentState = ConnectionState.HANDSHAKE;

    public final BungeeCord bungee = BungeeCord.getInstance();

    private final SonarBungee sonar = SonarBungee.INSTANCE;

    private final ConnectionThrottle throttler;

    private final ChannelHandlerContext ctx;

    private final ChannelPipeline pipeline;

    @Override
    public void exception(final Throwable cause) throws Exception {
        ExceptionHandler.handle(ctx.channel(), cause);
    }

    @Override
    public void handle(final PacketWrapper packet) throws Exception {
        if (packet == null) {
            throw sonar.EXCEPTION;
        }

        if (packet.buf.readableBytes() > Config.Values.MAX_PACKET_BYTES) {
            packet.buf.clear();
            throw sonar.EXCEPTION;
        }
    }

    @Override
    public void handle(final EncryptionResponse encryptionResponse) throws Exception {
        Counter.ENCRYPTIONS_PER_SECOND.increment();

        if (currentState != ConnectionState.JOINING) {
            throw sonar.EXCEPTION;
        }

        super.handle(encryptionResponse);
    }

    @Override
    public void handle(final Handshake handshake) throws Exception {
        Counter.HANDSHAKES_PER_SECOND.increment();

        if (currentState != ConnectionState.HANDSHAKE) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        if (throttler != null && throttler.throttle(getSocketAddress())) {
            ctx.close();
            return;
        }

        final InetAddress inetAddress = inetAddress();

        switch (handshake.getRequestedProtocol()) {

            /*
             * ID 1 -> Ping
             */

            case 1: {
                if (Config.Values.PING_BEFORE_JOIN) {
                    ServerPingCache.HAS_PINGED.add(inetAddress);
                }

                currentState = ConnectionState.PINGING;
                break;
            }

            /*
             * ID 2 -> Join
             */

            case 2: {
                currentState = ConnectionState.JOINING;
                break;
            }

            /*
             * The requested protocol can either be 1 or 2.
             * Anything else is not possible by the default
             * Minecraft client.
             */

            default: {
                throw sonar.EXCEPTION;
            }
        }

        pipeline.addBefore(PipelineUtils.BOSS_HANDLER, PACKET_INTERCEPTOR, new PacketHandler(this));
        pipeline.addLast(LAST_PACKET_INTERCEPTOR, SonarPipelines.EXCEPTION_HANDLER);

        super.handle(handshake);
    }

    public void disconnect_(final String reason) {
        if (reason != null && ctx.channel().isActive()) {
            ctx.channel().writeAndFlush(new Kick(ComponentSerializer.toString(new TextComponent(reason))));
        }

        ctx.close();
    }

    @Override
    public void handle(final StatusRequest statusRequest) throws Exception {
        Counter.PINGS_PER_SECOND.increment();

        if (currentState != ConnectionState.PINGING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        // credits to Velocity
        getAsyncServerPing()
                .thenAcceptAsync(pingBack -> {

                    // minecraft keeps the channel opened
                    // most botting tools or crashers instantly close it
                    if (!isConnected()) {
                        ServerStatistics.BLOCKED_CONNECTIONS++;
                        return;
                    }

                    final ListenerInfo listener = getListener();
                    final int protocolVersion = getVersion();

                    final ServerInfo forced = ServerDataProvider.getForcedHost(listener, getVirtualHost());

                    if (forced != null && listener.isPingPassthrough() && Config.Values.ALLOW_PING_PASS_THROUGH) {
                        ((BungeeServerInfo) forced).ping(pingBack, protocolVersion);
                    } else {
                        final ServerPing serverPing = ServerPingCache.getCached(listener, forced != null ? forced.getMotd() : listener.getMotd());

                        serverPing.getVersion().setProtocol(protocolVersion);

                        final Gson gson = getVersion() <= 4 /* 1.7.2 */ ? LegacyGsonFormat.LEGACY : bungee.gson;

                        pingBack.done(serverPing, null);

                        unsafe().sendPacket(new StatusResponse(gson.toJson(serverPing)));
                    }
                });

        currentState = ConnectionState.PINGING;
    }

    private CompletableFuture<Callback<ServerPing>> getAsyncServerPing() {
        return CompletableFuture.completedFuture((result, error) -> {
            if (error != null) return;

            if (!ctx.channel().isActive()) return;

            sonar.callEvent(new ProxyPingEvent(this, result, (pingResult, error1) -> {
                if (error1 != null) return;

                if (!ctx.channel().isActive()) return;

                if (bungee.getConnectionThrottle() != null) {
                    bungee.getConnectionThrottle().unthrottle(getSocketAddress());
                }
            }));
        });
    }

    @Override
    public void handle(final PingPacket ping) throws Exception {
        if (currentState != ConnectionState.PINGING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        /*
        final long timeStamp = System.currentTimeMillis();
        final long delay = timeStamp - ping.getTime();

        // check if the ping packet is extremely delayed
        if (delay > 10000L && delay < timeStamp) {
            return;
        }
        */

        unsafe().sendPacket(ping);

        ctx.close();
    }

    @Override
    public void handle(final LoginRequest loginRequest) throws Exception {
        Counter.JOINS_PER_SECOND.increment();

        if (currentState != ConnectionState.JOINING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        final String username = loginRequest.getData();

        if (!LoginCache.HAVE_LOGGED_IN.contains(username)) {
            LoginCache.HAVE_LOGGED_IN.add(username);

            if (Config.Values.ENABLE_FIRST_JOIN) {
                disconnect_(Messages.Values.DISCONNECT_FIRST_JOIN);
                ServerStatistics.BLOCKED_CONNECTIONS++;
                return;
            }
        }

        final InetAddress inetAddress = inetAddress();

        if (Config.Values.PING_BEFORE_JOIN && !ServerPingCache.HAS_PINGED.contains(inetAddress)) {
            disconnect_(Messages.Values.DISCONNECT_PING_BEFORE_JOIN);
            ServerStatistics.BLOCKED_CONNECTIONS++;
            return;
        }

        final ConnectionData data = ConnectionDataManager.create(inetAddress);

        data.username = username;

        final Detection detection = LoginHandler.check(data);

        if (detection.result == DetectionResult.DENIED) {
            switch (detection.key) {
                default: {
                    LoginCache.HAVE_LOGGED_IN.remove(username);
                    ConnectionDataManager.remove(data);
                    throw sonar.EXCEPTION;
                }

                case 2: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect_(Messages.Values.DISCONNECT_INVALID_NAME);
                    return;
                }

                case 3: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect_(Messages.Values.DISCONNECT_TOO_FAST_RECONNECT);
                    return;
                }

                case 4: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect_(Messages.Values.DISCONNECT_TOO_MANY_ONLINE);
                    return;
                }

                case 5: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect_(Messages.Values.DISCONNECT_QUEUED
                            .replaceAll("%position%", sonar.FORMAT.format(PlayerQueue.getPosition(data.username)))
                            .replaceAll("%size%", sonar.FORMAT.format(PlayerQueue.QUEUE.size())));
                    return;
                }

                case 6: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect_(Messages.Values.DISCONNECT_ATTACK);
                    return;
                }

                case 7: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect_(Messages.Values.DISCONNECT_BOT_BEHAVIOUR);
                    return;
                }
            }
        }

        super.handle(loginRequest);

        currentState = ConnectionState.JOINING;
    }

    public InetAddress inetAddress() {
        return getAddress().getAddress();
    }

    @Override
    public String toString() {
        return "§7(§f" + getSocketAddress() + (getName() != null ? "|" + getName() : "") + "§7) <-> InitialHandler";
    }
}
