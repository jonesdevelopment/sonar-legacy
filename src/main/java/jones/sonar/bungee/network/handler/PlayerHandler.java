package jones.sonar.bungee.network.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import jones.sonar.universal.blacklist.Blacklist;
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
import java.util.concurrent.TimeUnit;

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

    private static final Cache<InetAddress, Long> handshaking = CacheBuilder.newBuilder()
            .expireAfterWrite(500L, TimeUnit.MILLISECONDS).build();

    @Override
    public void handle(final Handshake handshake) throws Exception {
        Counter.HANDSHAKES_PER_SECOND.increment();

        if (currentState != ConnectionState.HANDSHAKE) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        final InetAddress inetAddress = inetAddress();

        if (handshaking.asMap().containsKey(inetAddress)) {
            handshaking.asMap().replace(inetAddress, handshaking.asMap().get(inetAddress) + 1L);

            if (handshaking.asMap().get(inetAddress) >= Config.Values.MAXIMUM_HANDSHAKES_PER_IP_SEC_BLACKLIST) {
                disconnect_(Messages.Values.DISCONNECT_BOT_BEHAVIOUR);

                Blacklist.addToTempBlacklist(inetAddress);

                handshaking.invalidate(inetAddress);
                return;
            }

            if (handshaking.asMap().get(inetAddress) >= Config.Values.MAXIMUM_HANDSHAKES_PER_IP_SEC) {
                disconnect_(Messages.Values.DISCONNECT_TOO_FAST_RECONNECT);
                return;
            }
        } else {
            handshaking.put(inetAddress, 1L);
        }

        switch (handshake.getRequestedProtocol()) {

            // ID 1 -> Ping
            case 1: {
                currentState = ConnectionState.STATUS;
                break;
            }

            // ID 2 -> Join
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

    private static final Cache<String, Kick> kickPacketCache = CacheBuilder.newBuilder()
            .expireAfterWrite(3L, TimeUnit.MINUTES)
            .build();

    public void disconnect_(final String reason) {
        ServerStatistics.BLOCKED_CONNECTIONS++;

        if (reason != null && ctx.channel().isActive()) {
            cache: {
                if (kickPacketCache.asMap().containsKey(reason)) {
                    ctx.channel().writeAndFlush(kickPacketCache.asMap().get(reason));
                    break cache;
                }

                final Kick kickPacket = new Kick(ComponentSerializer.toString(new TextComponent(reason)));

                kickPacketCache.put(reason, kickPacket); // cache

                ctx.channel().writeAndFlush(kickPacket);
            }
        }

        ctx.close();
    }

    private boolean hasRequestedPing, hasSuccessfullyPinged;

    @Override
    public void handle(final StatusRequest statusRequest) throws Exception {
        Counter.STATUSES_PER_SECOND.increment();

        // even though we already have the states, this can fail and the states do not
        // work properly (I don't really know why, BungeeCord is trash)
        if (hasRequestedPing || hasSuccessfullyPinged || currentState != ConnectionState.STATUS) {
            throw sonar.EXCEPTION;
        }

        hasRequestedPing = true;

        currentState = ConnectionState.PROCESSING;

        // credits to Velocity
        getAsyncServerPing()
                .thenAcceptAsync(pingBack -> {

                    // most botting tools or crashers instantly close the channel/connection
                    if (!isConnected()) {
                        throw sonar.EXCEPTION; // clients always keep the channel opened, so it's safe to blacklist here
                    }

                    if (!ServerPingCache.HAS_PINGED.asMap().containsKey(inetAddress())
                            && (Config.Values.PING_BEFORE_JOIN || Counter.JOINS_PER_SECOND.get() >= Config.Values.MINIMUM_JOINS_PER_SECOND)) {
                        ServerPingCache.HAS_PINGED.put(inetAddress(), (byte) 0);
                    }

                    currentState = ConnectionState.STATUS;

                    final ListenerInfo listener = getListener();
                    final int protocolVersion = getVersion();

                    final ServerInfo forced = ServerDataProvider.getForcedHost(listener, getVirtualHost());

                    if (Config.Values.ALLOW_PING_PASS_THROUGH && forced != null && listener.isPingPassthrough()) {
                        ((BungeeServerInfo) forced).ping(pingBack, protocolVersion);
                        return;
                    }

                    final ServerPing serverPing = ServerPingCache.getCached(listener, forced != null ? forced.getMotd() : listener.getMotd());

                    serverPing.getVersion().setProtocol(protocolVersion);

                    // handle legacy gson, if needed
                    final Gson gson = getVersion() <= 4 /* 1.7.2 */ ? LegacyGsonFormat.LEGACY : bungee.gson;

                    pingBack.done(serverPing, null);

                    unsafe().sendPacket(new StatusResponse(gson.toJson(serverPing)));

                    // clients cannot send multiple status packets without closing the channel once
                    hasSuccessfullyPinged = true;
                });
    }

    private CompletableFuture<Callback<ServerPing>> getAsyncServerPing() {
        return CompletableFuture.completedFuture((result, error) -> {
            if (error != null) return;

            if (!isConnected() || (Config.Values.NO_PING_EVENT_DURING_ATTACK && Counter.STATUSES_PER_SECOND.get() > Config.Values.MAX_STATUS_DURING_ATTACK)) return;

            sonar.callEvent(new ProxyPingEvent(this, result, (pingResult, error1) -> {
                if (error1 != null) return;

                if (!isConnected()) return;

                // un-throttle connection if needed
                if (bungee.getConnectionThrottle() != null) {
                    bungee.getConnectionThrottle().unthrottle(getSocketAddress());
                }
            }));
        });
    }

    @Override
    public void handle(final PingPacket ping) throws Exception {
        Counter.PINGS_PER_SECOND.increment();

        // clients cannot send multiple ping packets without closing the channel once
        if (currentState != ConnectionState.STATUS || !hasRequestedPing || !hasSuccessfullyPinged) {
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
        if (currentState != ConnectionState.JOINING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        // throttle connection on login only, not handshake
        if (throttler != null && throttler.throttle(getSocketAddress())) {
            ctx.close();
            return;
        }

        final ConnectionData data = ConnectionDataManager.create(inetAddress());

        data.username = loginRequest.getData();

        final Detection detection = LoginHandler.check(data);

        if (detection.result == DetectionResult.DENIED) {
            switch (detection.key) {
                default: {
                    LoginCache.HAVE_LOGGED_IN.remove(loginRequest.getData());
                    ConnectionDataManager.remove(data);
                    throw sonar.EXCEPTION;
                }

                case 2: {
                    disconnect_(Messages.Values.DISCONNECT_INVALID_NAME);
                    return;
                }

                case 3: {
                    disconnect_(Messages.Values.DISCONNECT_TOO_FAST_RECONNECT);
                    return;
                }

                case 4: {
                    disconnect_(Messages.Values.DISCONNECT_TOO_MANY_ONLINE);
                    return;
                }

                case 5: {
                    disconnect_(Messages.Values.DISCONNECT_QUEUED
                            .replaceAll("%position%", sonar.FORMAT.format(PlayerQueue.getPosition(data.username)))
                            .replaceAll("%size%", sonar.FORMAT.format(PlayerQueue.QUEUE.size())));
                    return;
                }

                case 6: {
                    disconnect_(Messages.Values.DISCONNECT_ATTACK);
                    return;
                }

                case 7: {
                    disconnect_(Messages.Values.DISCONNECT_BOT_BEHAVIOUR);
                    return;
                }

                case 8: {
                    disconnect_(Messages.Values.DISCONNECT_VPN_OR_PROXY);
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
