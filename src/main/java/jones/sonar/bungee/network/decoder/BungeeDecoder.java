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

package jones.sonar.bungee.network.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import jones.sonar.SonarBungee;

import java.util.List;

public final class BungeeDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf byteBuf, final List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }

        // the byteBuf is always 4 bytes or longer in a handshake packet
        if (byteBuf.readableBytes() < 4) {
            byteBuf.skipBytes(byteBuf.readableBytes());
            throw SonarBungee.INSTANCE.EXCEPTION;
        }

        final byte[] bytes = new byte[byteBuf.readableBytes()];

        byteBuf.readBytes(bytes);

        byteBuf.resetReaderIndex();

        // the first byte is always greater than 0
        // the second byte is always 0
        if (bytes[0] <= 0 || bytes[1] != 0) {
            byteBuf.skipBytes(byteBuf.readableBytes());
            throw SonarBungee.INSTANCE.EXCEPTION;
        }

        /*
        if (byteBuf.writerIndex() == 18
                && byteBuf.readerIndex() == 0
                && byteBuf.readableBytes() > 9) {
            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }
        */

        byteBuf.markReaderIndex();

        final int unsigned = byteBuf.readUnsignedByte();

        if (unsigned == 254) {
            byteBuf.resetReaderIndex();

            out.add(byteBuf.retain().duplicate());

            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }

        else if (unsigned == 2 && byteBuf.isReadable()) {
            byteBuf.resetReaderIndex();

            out.add(byteBuf.retain().duplicate());

            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }

        byteBuf.resetReaderIndex();

        ctx.pipeline().remove(this);
    }
}
