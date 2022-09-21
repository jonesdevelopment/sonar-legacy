/*
 *  Copyright (c) 2022, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.bungee.network;

import io.netty.channel.*;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import jones.sonar.SonarBungee;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.network.handler.InboundHandler;
import jones.sonar.bungee.network.handler.PlayerHandler;
import jones.sonar.bungee.network.handler.TimeoutHandler;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.ServerStatistics;
import jones.sonar.universal.util.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ConnectionThrottle;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.ClientConnectEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public final class BungeeInterceptor extends ChannelInitializer<Channel> implements SonarPipeline {

    private final ConnectionThrottle throttler = BungeeCord.getInstance().getConnectionThrottle();

    private final KickStringWriter legacyKicker = new KickStringWriter();

    private final Map<InetAddress, Long> perIpCount = new ConcurrentHashMap<>();

    private final int protocol;

    /*
     * initChannel() is very slow in this version of Netty
     * This is why we are using handlerAdded()
     */

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        channel.close();
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

            final SocketAddress remoteAddress = ctx.channel().remoteAddress();

            if (remoteAddress == null) {
                ctx.close();
                return;
            }

            final InetAddress inetAddress = ((InetSocketAddress) remoteAddress).getAddress();

            final long timeStamp = System.currentTimeMillis();

            if (!perIpCount.containsKey(inetAddress)) {
                perIpCount.put(inetAddress, timeStamp);

                Counter.IPS_PER_SECOND.increment();
            } else {
                if (timeStamp - perIpCount.get(inetAddress) > 1000L) {
                    perIpCount.replace(inetAddress, timeStamp);

                    Counter.IPS_PER_SECOND.increment();
                }
            }

            final Channel channel = ctx.channel();

            if (Blacklist.isBlacklisted(inetAddress)) {
                channel.unsafe().closeForcibly();

                ServerStatistics.BLOCKED_CONNECTIONS++;
                return;
            }

            final ChannelPipeline pipeline = channel.pipeline();

            final Channel parent = channel.parent();

            // check for geyser players and don't register the sonar handler for them
            final boolean isGeyser = parent != null && parent.getClass().getCanonicalName().startsWith("org.geysermc.geyser");

            if (!isGeyser) {
                SonarPipelines.register(pipeline);
            }

            if (throttler != null && throttler.throttle(channel.remoteAddress())) {
                channel.unsafe().closeForcibly();

                ServerStatistics.BLOCKED_CONNECTIONS++;
                return;
            }

            final ListenerInfo listener = channel.attr(PipelineUtils.LISTENER).get();

            // add the tcp fast open option to non-geyser channels
            if (!isGeyser) {
                channel.config().setOption(ChannelOption.TCP_FASTOPEN, 3);
            }

            // initialize the channel with the pipeline base
            // this is necessary for compatibility reasons
            PipelineUtils.BASE.initChannel(channel);

            // don't modify the channels for geyser connections
            if (!isGeyser) {

                // replace the timeout handler to our custom, fixed one
                pipeline.replace(PipelineUtils.TIMEOUT_HANDLER, PipelineUtils.TIMEOUT_HANDLER, new TimeoutHandler(SonarBungee.INSTANCE.proxy.getConfig().getTimeout()));

                // replace the inbound boss handler to our custom, fixed one (handle exceptions)
                pipeline.replace(PipelineUtils.BOSS_HANDLER, PipelineUtils.BOSS_HANDLER, new InboundHandler());
            }

            // load the default BungeeCord pipelines
            pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, new LegacyDecoder());
            pipeline.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder(Protocol.HANDSHAKE, true, protocol));
            pipeline.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder(Protocol.HANDSHAKE, true, protocol));
            pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker);

            // we don't want geyser players to be falsely flagged
            // this is why we need to load the original initial handler
            if (isGeyser) {
                pipeline.get(HandlerBoss.class).setHandler(new InitialHandler(BungeeCord.getInstance(), listener));
            } else {

                // normal players will be handled using our custom player handler
                pipeline.get(InboundHandler.class).setHandler(new PlayerHandler(ctx, listener));
            }

            // the proxy protocol is necessary if you want to use tcp shield
            if (Config.Values.ALLOW_PROXY_PROTOCOL) {
                if (listener.isProxyProtocol()) {
                    pipeline.addFirst(new HAProxyMessageDecoder());
                }
            }

            if (Config.Values.CLIENT_CONNECT_EVENT) {
                if (SonarBungee.INSTANCE.callEvent(new ClientConnectEvent(remoteAddress, listener)).isCancelled()) {
                    ctx.channel().unsafe().closeForcibly();

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
