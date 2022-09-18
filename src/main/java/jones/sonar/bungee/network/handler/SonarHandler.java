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
import jones.sonar.SonarBungee;
import jones.sonar.bungee.config.Config;

public interface SonarHandler {
    default void intercept(final ChannelHandlerContext ctx, final ByteBuf byteBuf) throws Exception {
        if (!ctx.channel().isActive() && byteBuf.refCnt() > 0) {
            byteBuf.release(byteBuf.refCnt());
            return;
        }

        if (!byteBuf.isReadable()) {
            ctx.close();
            return;
        }

        final int readableBytes = byteBuf.readableBytes();

        byteBuf.markReaderIndex();

        if (readableBytes > Config.Values.MAX_PACKET_BYTES
                || byteBuf.capacity() > Config.Values.MAX_PACKET_CAPACITY
                || byteBuf.writableBytes() > Config.Values.MAX_PACKET_CAPACITY
                || byteBuf.writerIndex() > Config.Values.MAX_PACKET_INDEX
                || byteBuf.readerIndex() > Config.Values.MAX_PACKET_BYTES
                || readableBytes <= 0) {
            byteBuf.clear();
            throw SonarBungee.INSTANCE.EXCEPTION;
        }

        byteBuf.resetReaderIndex();

        ctx.fireChannelRead(byteBuf);
    }
}
