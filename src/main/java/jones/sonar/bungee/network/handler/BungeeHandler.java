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
import jones.sonar.bungee.config.Config;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.ExceptionHandler;

public final class BungeeHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ExceptionHandler.handle(ctx.channel(), cause);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            final ByteBuf byteBuf = (ByteBuf) msg;

            if (!ctx.channel().isActive() && byteBuf.refCnt() > 0) {
                byteBuf.release(byteBuf.refCnt());
                return;
            }

            if (!ctx.channel().isActive() || !byteBuf.isReadable()) {
                byteBuf.skipBytes(byteBuf.readableBytes());
                return;
            }

            byteBuf.markReaderIndex();

            if (byteBuf.readableBytes() > Config.Values.MAX_PACKET_BYTES
                    || byteBuf.capacity() > Config.Values.MAX_PACKET_CAPACITY
                    || byteBuf.writableBytes() > Config.Values.MAX_PACKET_CAPACITY
                    || byteBuf.writerIndex() > Config.Values.MAX_PACKET_INDEX
                    || byteBuf.readerIndex() > Config.Values.MAX_PACKET_BYTES
                    || byteBuf.readableBytes() <= 0) {
                byteBuf.skipBytes(byteBuf.readableBytes());
                throw SonarBungee.INSTANCE.EXCEPTION;
            }

            byteBuf.resetReaderIndex();

            ctx.fireChannelRead(byteBuf);

            ctx.pipeline().remove(this);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
