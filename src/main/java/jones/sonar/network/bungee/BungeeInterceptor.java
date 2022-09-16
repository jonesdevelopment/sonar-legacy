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

package jones.sonar.network.bungee;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import jones.sonar.counter.Counter;
import jones.sonar.network.bungee.handler.BungeeHandler;
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

@RequiredArgsConstructor
public final class BungeeInterceptor extends ChannelInitializer<Channel> implements SonarPipeline {

    private final ConnectionThrottle throttler = BungeeCord.getInstance().getConnectionThrottle();

    private final KickStringWriter legacyKicker = new KickStringWriter();

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
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        try {
            final SocketAddress remoteAddress = ctx.channel().remoteAddress();

            if (remoteAddress == null) {
                ctx.close();
                return;
            }

            Counter.CONNECTIONS_PER_SECOND.increment();

            final InetAddress inetAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();

            ctx.pipeline().addFirst(HANDLER, new BungeeHandler());

            ctx.channel().config().setOption(ChannelOption.TCP_FASTOPEN, 3);

            if (throttler != null
                    && throttler.throttle(ctx.channel().remoteAddress())) {
                ctx.close();
                return;
            }

            final ListenerInfo listener = ctx.channel().attr(PipelineUtils.LISTENER).get();

            PipelineUtils.BASE.initChannel(ctx.channel());

            ctx.pipeline().addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, new LegacyDecoder());
            ctx.pipeline().addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder(Protocol.HANDSHAKE, true, protocol));
            ctx.pipeline().addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder(Protocol.HANDSHAKE, true, protocol));
            ctx.pipeline().addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker);

            ctx.pipeline().get(HandlerBoss.class).setHandler(new InitialHandler(BungeeCord.getInstance(), listener));

            if (listener.isProxyProtocol()) {
                ctx.pipeline().addFirst(new HAProxyMessageDecoder());
            }

            if (BungeeCord.getInstance().getPluginManager().callEvent(new ClientConnectEvent(remoteAddress, listener)).isCancelled()) {
                ctx.close();
            }
        } finally {
            if (!ctx.isRemoved()) {
                ctx.pipeline().remove(this);
            }
        }
    }
}
