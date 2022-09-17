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
import io.netty.handler.codec.MessageToMessageDecoder;
import jones.sonar.SonarBungee;
import net.md_5.bungee.protocol.DefinedPacket;

import java.util.List;

public final class SonarPacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            in.skipBytes(in.readableBytes());
            return;
        }

        in.markReaderIndex();

        for(int i = 0; i < 3; ++i) {
            if (!in.isReadable()) {
                in.resetReaderIndex();
                return;
            }

            byte read = in.readByte();
            if (read >= 0) {
                in.resetReaderIndex();
                int length = DefinedPacket.readVarInt(in);
                if (in.readableBytes() < length) {
                    in.resetReaderIndex();
                    return;
                }

                out.add(in.readRetainedSlice(length));
                return;
            }
        }

        throw SonarBungee.INSTANCE.EXCEPTION;
    }
}
