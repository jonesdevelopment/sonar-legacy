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

package jones.sonar.network.bungee.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import jones.sonar.SonarBungee;

public interface SonarHandler {
    default void intercept(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            final ByteBuf byteBuf = (ByteBuf) msg;

            final Channel channel = ctx.channel();

            if (!channel.isActive() && byteBuf.refCnt() > 0) {
                byteBuf.release(byteBuf.refCnt());
                return;
            }

            if (!byteBuf.isReadable()) {
                ctx.close();
                throw SonarBungee.INSTANCE.EXCEPTION;
            }

            byteBuf.markReaderIndex();

            final int bytes = byteBuf.readableBytes(),
                    capacity = byteBuf.capacity(),
                    writerIndex = byteBuf.writerIndex(),
                    readerIndex = byteBuf.readerIndex();

            if (bytes > 2048 || bytes <= 0 || capacity > 4096 || writerIndex > 1024 || readerIndex > 2048) {
                byteBuf.clear();
                ctx.close();
                throw SonarBungee.INSTANCE.EXCEPTION;
            }

            byteBuf.resetReaderIndex();

            ctx.fireChannelRead(msg);
        }
    }
}
