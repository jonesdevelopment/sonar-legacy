package jones.sonar.bungee.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.channel.*;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.network.decoder.VarIntFrameDecoder;
import jones.sonar.bungee.network.handler.InboundHandler;
import jones.sonar.bungee.network.handler.PlayerHandler;
import jones.sonar.bungee.network.handler.TimeoutHandler;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.ServerStatistics;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ConnectionThrottle;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.ClientConnectEvent;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class BungeeInterceptor extends ChannelInitializer<Channel> implements SonarPipeline {

    private final ConnectionThrottle throttler = BungeeCord.getInstance().getConnectionThrottle();

    private final KickStringWriter legacyKicker = new KickStringWriter();

    private final Cache<InetAddress, Byte> perIpCount = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.SECONDS) // expire after 1 second
            .initialCapacity(1) // only able to hold 1 ip address
            .build();

    private final int protocol;

    /*
     * initChannel() is very slow in this version of Netty
     * This is why we are using handlerAdded()
     */

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        final Channel parent = channel.parent();

        // check for geyser players and don't register the sonar handler for them
        final boolean isGeyser = parent != null && parent.getClass().getCanonicalName().startsWith("org.geysermc.geyser");

        if (isGeyser) {
            GeyserInterceptor.handle(channel, protocol);
        }
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        //
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ExceptionHandler.handle(ctx.channel(), cause);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        try {
            Counter.CONNECTIONS_PER_SECOND.increment();

            ServerStatistics.TOTAL_CONNECTIONS++;

            final Channel channel = ctx.channel();

            final SocketAddress remoteAddress = (channel.remoteAddress() == null) ? channel.parent().localAddress() : channel.remoteAddress();

            final InetAddress inetAddress = ((InetSocketAddress) remoteAddress).getAddress();

            // Increment ips per second counter if the inetAddress is
            // not in the cache that expires after 1 second (→ ips/sec)
            if (!perIpCount.asMap().containsKey(inetAddress)) {
                perIpCount.put(inetAddress, (byte) 0);

                Counter.IPS_PER_SECOND.increment();
            }

            // Just drop the connection whenever the player is blacklisted
            if (Blacklist.isBlacklisted(inetAddress)) {
                channel.close();

                ServerStatistics.BLOCKED_CONNECTIONS++;
                return;
            }

            final ChannelPipeline pipeline = channel.pipeline();

            // TCPShield and other reverse proxies already handle invalid
            // under-sized or over-sized packets → we can just exempt the player
            // from the invalid/bad packet checks if TCPShield is detected
            if (!SonarBungee.INSTANCE.isReverseProxy) {
                SonarPipelines.register(pipeline);
            }

            final ListenerInfo listener = channel.attr(PipelineUtils.LISTENER).get();

            // add the tcp fast open option
            if (Config.Values.ENABLE_TCP_FAST_OPEN) {
                channel.config().setOption(ChannelOption.TCP_FASTOPEN, Config.Values.TCP_FAST_OPEN_MODE);
            }

            // initialize the channel with the pipeline base
            // this is necessary for compatibility reasons
            PipelineUtils.BASE.initChannel(channel);

            // replace the frame decoder to avoid further exploits
            pipeline.replace(PipelineUtils.FRAME_DECODER, PipelineUtils.FRAME_DECODER, new VarIntFrameDecoder());

            // replace the timeout handler to our custom, fixed one
            pipeline.replace(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.TIMEOUT_HANDLER, new TimeoutHandler(SonarBungee.INSTANCE.proxy.getConfig().getTimeout()));

            // replace the inbound boss handler to our custom, fixed one (handle exceptions)
            pipeline.replace(PipelineUtils.BOSS_HANDLER, PipelineUtils.BOSS_HANDLER, new InboundHandler());

            // load the default BungeeCord pipelines
            pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, new LegacyDecoder());
            pipeline.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder(Protocol.HANDSHAKE, true, protocol));
            pipeline.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder(Protocol.HANDSHAKE, true, protocol));
            pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker);

            // normal players will be handled using our custom player handler
            pipeline.get(InboundHandler.class).setHandler(new PlayerHandler(ctx, listener, throttler));

            // the proxy protocol is necessary if you want to use some kind of reverse proxy
            if (Config.Values.ALLOW_PROXY_PROTOCOL) {
                if (listener.isProxyProtocol()) {
                    pipeline.addFirst(new HAProxyMessageDecoder());
                }
            }

            if (Config.Values.CLIENT_CONNECT_EVENT) {
                if (SonarBungee.INSTANCE.callEvent(new ClientConnectEvent(remoteAddress, listener)).isCancelled()) {
                    channel.close();

                    ServerStatistics.BLOCKED_CONNECTIONS++;
                }
            }
        } finally {
            if (!ctx.isRemoved()) {
                ctx.pipeline().remove(this);
            }
        }
    }
}
