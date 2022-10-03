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

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import jones.sonar.bungee.config.Config;
import jones.sonar.universal.data.ServerStatistics;
import jones.sonar.universal.platform.bungee.SonarBungee;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.ClientConnectEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.*;

@UtilityClass
public class GeyserInterceptor {
    private final KickStringWriter legacyKicker = new KickStringWriter();

    protected void handle(final Channel channel, final int protocol) throws Exception {
        if (channel.remoteAddress() == null) {
            channel.close();
            return;
        }

        final ListenerInfo listener = channel.attr(PipelineUtils.LISTENER).get();

        PipelineUtils.BASE.initChannel(channel);

        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.LEGACY_DECODER, new LegacyDecoder());
        pipeline.addAfter(PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new MinecraftDecoder(Protocol.HANDSHAKE, true, protocol));
        pipeline.addAfter(PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, new MinecraftEncoder(Protocol.HANDSHAKE, true, protocol));
        pipeline.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.LEGACY_KICKER, legacyKicker);

        channel.pipeline().get(HandlerBoss.class).setHandler(new InitialHandler(BungeeCord.getInstance(), listener));

        // the proxy protocol is necessary if you want to use tcp shield
        if (Config.Values.ALLOW_PROXY_PROTOCOL) {
            if (listener.isProxyProtocol()) {
                channel.pipeline().addFirst(new HAProxyMessageDecoder());
            }
        }

        if (Config.Values.CLIENT_CONNECT_EVENT) {
            if (SonarBungee.INSTANCE.callEvent(new ClientConnectEvent(channel.remoteAddress(), listener)).isCancelled()) {
                channel.unsafe().closeForcibly();

                ServerStatistics.BLOCKED_CONNECTIONS++;
            }
        }
    }
}
