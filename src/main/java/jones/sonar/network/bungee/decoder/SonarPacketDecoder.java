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

package jones.sonar.network.bungee.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import jones.sonar.SonarBungee;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.Protocol;

import java.util.List;

public final class SonarPacketDecoder extends MinecraftDecoder {

    private final Protocol protocol;

    public SonarPacketDecoder(final Protocol protocol, final boolean server, final int protocolVersion) {
        super(protocol, server, protocolVersion);

        this.protocol = protocol;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (in.writerIndex() == 18
                && in.readerIndex() == 0
                && in.readableBytes() > 9
                && protocol == Protocol.HANDSHAKE) {
            in.clear();
            throw SonarBungee.INSTANCE.EXCEPTION;
        }

        super.decode(ctx, in, out);
    }
}
