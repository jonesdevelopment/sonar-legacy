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

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import jones.sonar.SonarBungee;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.network.decoder.SonarPacketDecoder;
import jones.sonar.bungee.network.handler.BungeeHandler;
import jones.sonar.bungee.network.handler.MainHandler;
import jones.sonar.bungee.network.handler.PlayerHandler;
import jones.sonar.bungee.network.handler.TimeoutHandler;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.ServerStatistics;
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
        ctx.close();
        ServerStatistics.BLOCKED_CONNECTIONS++;
        Blacklist.addToBlacklist(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
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

            if (Blacklist.isBlacklisted(inetAddress)) {
                ctx.close();
                ServerStatistics.BLOCKED_CONNECTIONS++;
                return;
            }

            ctx.pipeline().addFirst(HANDLER, new BungeeHandler());

            if (throttler != null
                    && throttler.throttle(ctx.channel().remoteAddress())) {
                ctx.close();
                return;
            }

            final ListenerInfo listener = ctx.channel().attr(PipelineUtils.LISTENER).get();

            initBaseChannel(ctx.channel());

            ctx.pipeline().addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, new LegacyDecoder());
            ctx.pipeline().addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new SonarPacketDecoder(Protocol.HANDSHAKE, true, protocol));
            ctx.pipeline().addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder(Protocol.HANDSHAKE, true, protocol));
            ctx.pipeline().addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker);

            ctx.pipeline().get(MainHandler.class).setHandler(new PlayerHandler(ctx, listener));

            if (Config.Values.ALLOW_PROXY_PROTOCOL) {
                if (listener.isProxyProtocol()) {
                    ctx.pipeline().addFirst(new HAProxyMessageDecoder());
                }
            }

            if (Config.Values.CLIENT_CONNECT_EVENT) {
                if (SonarBungee.INSTANCE.callEvent(new ClientConnectEvent(remoteAddress, listener)).isCancelled()) {
                    ctx.close();
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                }
            }
        } finally {
            if (!ctx.isRemoved()) {
                ctx.pipeline().remove(this);
            }
        }
    }

    private void initBaseChannel(final Channel channel) throws Exception {
        channel.config().setOption(ChannelOption.IP_TOS, 24);
        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
        channel.config().setOption(ChannelOption.TCP_FASTOPEN, 3);

        channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
        channel.config().setWriteBufferWaterMark(MARK);

        channel.pipeline().addLast("frame-decoder", new Varint21FrameDecoder());
        channel.pipeline().addLast("timeout", new TimeoutHandler(SonarBungee.INSTANCE.proxy.getConfig().getTimeout()));
        channel.pipeline().addLast("frame-prepender", FRAME_PREPENDER);
        channel.pipeline().addLast("inbound-boss", new MainHandler());
    }
}
