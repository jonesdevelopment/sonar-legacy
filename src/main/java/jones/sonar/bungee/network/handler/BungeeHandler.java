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

package jones.sonar.bungee.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.data.ServerStatistics;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class BungeeHandler extends ChannelInboundHandlerAdapter implements SonarHandler {

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ctx.close();
        ServerStatistics.BLOCKED_CONNECTIONS++;

        if (cause instanceof IOException) return;

        Blacklist.addToBlacklist(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            intercept(ctx, (ByteBuf) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
