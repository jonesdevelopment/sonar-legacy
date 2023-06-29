package jones.sonar.bungee.network.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import jones.sonar.bungee.caching.ServerPingCache;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.detection.LoginHandler;
import jones.sonar.bungee.network.SonarPipeline;
import jones.sonar.bungee.network.SonarPipelines;
import jones.sonar.bungee.network.handler.packet.PacketHandler;
import jones.sonar.bungee.network.handler.state.ConnectionState;
import jones.sonar.bungee.util.Logger;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.ServerStatistics;
import jones.sonar.universal.data.connection.ConnectionData;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.detection.Detection;
import jones.sonar.universal.detection.DetectionResult;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.queue.LoginCache;
import jones.sonar.universal.util.ExceptionHandler;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ConnectionThrottle;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.*;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class PlayerHandler extends InitialHandler implements SonarPipeline {

    public PlayerHandler(final ChannelHandlerContext ctx,
                         final ListenerInfo listener,
                         final ConnectionThrottle throttler) {
        super(BungeeCord.getInstance(), listener);

        this.ctx = ctx;
        this.throttler = throttler;
    }

    @Getter
    private ConnectionState currentState = ConnectionState.HANDSHAKE;

    private final ConnectionThrottle throttler;

    public InetAddress inetAddress;

    public final ChannelHandlerContext ctx;

    public ChannelPipeline pipeline;

    // This is just for private testing of Sonar
    private static void debug(final Object info) {
        if (false) Logger.INFO.log(String.valueOf(info));
    }

    @Override
    public void connected(final ChannelWrapper wrapper) throws Exception {
        super.connected(wrapper);

        pipeline = wrapper.getHandle().pipeline();
        inetAddress = ((InetSocketAddress) getSocketAddress()).getAddress();
    }

    @Override
    public void exception(final Throwable cause) throws Exception {
        ExceptionHandler.handle(ctx.channel(), cause);
    }

    @Override
    public void handle(final PacketWrapper packet) throws Exception {
        if (packet == null) {
            debug("null packet");
            throw SonarBungee.EXCEPTION;
        }

        if (packet.buf.readableBytes() > Config.Values.MAX_PACKET_BYTES) {
            debug("over-sized packet " + packet.buf.readableBytes());
            packet.buf.clear();
            throw SonarBungee.EXCEPTION;
        }
    }

    @Override
    public void handle(final EncryptionResponse encryptionResponse) throws Exception {
        Counter.ENCRYPTIONS_PER_SECOND.increment();

        super.handle(encryptionResponse);
    }

    private static final Cache<InetAddress, Short> handshaking = CacheBuilder.newBuilder()
            .expireAfterWrite(500L, TimeUnit.MILLISECONDS).build();

    @Override
    public void handle(final Handshake handshake) throws Exception {
        Counter.HANDSHAKES_PER_SECOND.increment();

        if (currentState != ConnectionState.HANDSHAKE) {
            //debug("invalid handshake state"); // Do not log - this can be executed millions of times per second
            throw SonarBungee.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

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
                //debug("invalid protocol"); // Do not log - this can be executed millions of times per second
                throw SonarBungee.EXCEPTION;
            }
        }

        if (handshaking.asMap().containsKey(inetAddress)) {
            handshaking.asMap().replace(inetAddress, (short) (handshaking.asMap().get(inetAddress) + 1));

            if (handshaking.asMap().get(inetAddress) >= Config.Values.MAXIMUM_HANDSHAKES_PER_IP_SEC_BLACKLIST) {
                handshaking.invalidate(inetAddress);

                debug("too many handshakes per second");
                disconnect_(Messages.Values.DISCONNECT_BOT_BEHAVIOUR);

                Blacklist.addToTempBlacklist(inetAddress);
                return;
            }

            if (handshaking.asMap().get(inetAddress) >= Config.Values.MAXIMUM_HANDSHAKES_PER_IP_SEC) {
                //debug("many handshakes per second"); // Do not log - this can be executed millions of times per second
                disconnect_(Messages.Values.DISCONNECT_TOO_FAST_RECONNECT);
                return;
            }
        } else {
            handshaking.put(inetAddress, (short) 1);
        }

        pipeline.addBefore(PipelineUtils.BOSS_HANDLER, PACKET_INTERCEPTOR, new PacketHandler(this));
        pipeline.addLast(LAST_PACKET_INTERCEPTOR, SonarPipelines.EXCEPTION_HANDLER);

        super.handle(handshake);
    }

    private static final Map<String, Kick> kickPacketCache = new HashMap<>();

    public void disconnect_(final String reason) {
        ServerStatistics.BLOCKED_CONNECTIONS++;

        if (reason != null && ctx.channel().isActive()) {
            cache: {
                Kick kickPacket = kickPacketCache.get(reason);

                if (kickPacket != null) {
                    ctx.channel().writeAndFlush(kickPacket);
                    break cache;
                }

                kickPacket = new Kick(ComponentSerializer.toString(new TextComponent(reason)));

                // Cache the kick packet, so we can reuse it without
                // needing to create a new ByteBuf
                // TODO: check if this can potentially cause memory leaks
                kickPacketCache.put(reason, kickPacket);

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
            //debug("invalid status #1"); // Do not log - this can be executed millions of times per second
            throw SonarBungee.EXCEPTION;
        }

        hasRequestedPing = true;

        currentState = ConnectionState.PROCESSING;

        // TODO: Perform some tests; seems to work for now
        CompletableFuture.runAsync(() -> {

            // most botting tools or crashers instantly close the channel/connection
            if (!isConnected()) {

                // clients ALWAYS keep the channel opened, so it's safe to blacklist here
                //debug("invalid status #2"); // Do not log - this can be executed millions of times per second
                throw SonarBungee.EXCEPTION;
            }

            currentState = ConnectionState.PINGING;

            // clients cannot send multiple status packets without closing the channel once
            hasSuccessfullyPinged = true;

            try {

                // You may ask why I am doing this even though I had a different method before.
                // I am only using `super.handle(...)` because many MOTD plugins were not supported
                // and I want to ensure compatibility.
                // All of this is run asynchronously, and it SHOULD have the same performance
                super.handle(statusRequest);
            } catch (Exception exception) {
                exception.printStackTrace();
                //debug("invalid status #3"); // Do not log - this can be executed millions of times per second
                throw SonarBungee.EXCEPTION; // TODO: Different handling?
            }
        });
    }

    @Override
    public void handle(final PingPacket ping) throws Exception {
        Counter.PINGS_PER_SECOND.increment();

        // clients cannot send multiple ping packets without closing the channel once
        if (currentState != ConnectionState.PINGING || !hasRequestedPing || !hasSuccessfullyPinged) {
            debug("invalid ping #1");
            throw SonarBungee.EXCEPTION;
        }

        if (!ServerPingCache.HAS_PINGED.asMap().containsKey(inetAddress)
                && (Config.Values.PING_BEFORE_JOIN || Counter.JOINS_PER_SECOND.get() >= Config.Values.MINIMUM_JOINS_PER_SECOND)) {
            ServerPingCache.HAS_PINGED.put(inetAddress, (byte) 0);
        }

        currentState = ConnectionState.PROCESSING;

        unsafe().sendPacket(ping);

        ctx.close();
    }

    @Override
    public void handle(final LoginRequest loginRequest) throws Exception {
        if (currentState != ConnectionState.JOINING) {
            //debug("invalid login #1"); // Do not log - this can be executed millions of times per second
            throw SonarBungee.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        // throttle connection on login only, not in ClientConnectEvent
        if (throttler != null && throttler.throttle(getSocketAddress())) {
            ctx.close();
            return;
        }

        final ConnectionData data = ConnectionDataManager.create(inetAddress);

        data.username = loginRequest.getData();

        final Detection detection = LoginHandler.check(data, this);

        if (detection.result == DetectionResult.ALLOWED) {
            super.handle(loginRequest);
            return;
        }

        if (detection.kickReason != null) {
            disconnect_(detection.kickReason);
        }

        if (detection.blacklist) {
            LoginCache.HAVE_LOGGED_IN.remove(data.username);
            ConnectionDataManager.remove(data);
            throw SonarBungee.EXCEPTION;
        }
    }

    // Can through reflection to modify uniqueId - @FallenCrystal
    // -- Start
    private UUID uniqueId;

    @Override
    public void setUniqueId(UUID uuid) {
        this.uniqueId=uuid;
        Class<InitialHandler> parent = InitialHandler.class;
        try {
            Field field = parent.getDeclaredField("uniqueId");
            field.setAccessible(true);
            field.set(parent, uniqueId);
        } catch (NoSuchFieldException | IllegalAccessException exception) { exception.printStackTrace(); }
    }

    @Override
    public UUID getUniqueId() { return uniqueId != null ? uniqueId : super.getUniqueId(); }

    // -- End

    @Override
    public String toString() {
        return "§7(§f" + getSocketAddress() + (getName() != null ? "|" + getName() : "") + "§7) <-> InitialHandler";
    }
}
