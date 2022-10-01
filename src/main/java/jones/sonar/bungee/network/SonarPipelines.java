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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import jones.sonar.bungee.network.decoder.BungeeDecoder;
import jones.sonar.bungee.network.handler.BungeeHandler;
import jones.sonar.universal.util.ExceptionHandler;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SonarPipelines implements SonarPipeline {

    public final ChannelHandler EXCEPTION_HANDLER = new PacketExceptionHandler();

    @ChannelHandler.Sharable
    public class PacketExceptionHandler extends ChannelDuplexHandler {

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            ExceptionHandler.handle(ctx.channel(), cause);
        }
    }

    public void register(final ChannelPipeline pipeline) {
        pipeline.addFirst(HANDLER, new BungeeHandler());
        pipeline.addFirst(DECODER, new BungeeDecoder());
    }
}
