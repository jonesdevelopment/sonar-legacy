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

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import jones.sonar.SonarBungee;
import jones.sonar.bungee.network.decoder.BungeeDecoder;
import jones.sonar.bungee.network.handler.BungeeHandler;
import jones.sonar.bungee.network.handler.MainHandler;
import jones.sonar.bungee.network.handler.TimeoutHandler;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.Varint21FrameDecoder;

@UtilityClass
public class ChannelRegistrar implements SonarPipeline {
    public void registerDefaultChannel(final ChannelConfig config, final ChannelPipeline pipeline) {
        config.setOption(ChannelOption.IP_TOS, 24);
        config.setOption(ChannelOption.TCP_NODELAY, true);
        config.setOption(ChannelOption.TCP_FASTOPEN, 3);

        config.setAllocator(PooledByteBufAllocator.DEFAULT);
        config.setWriteBufferWaterMark(MARK);

        pipeline.addLast(PipelineUtils.FRAME_DECODER, new Varint21FrameDecoder());
        pipeline.addLast(PipelineUtils.TIMEOUT_HANDLER, new TimeoutHandler(SonarBungee.INSTANCE.proxy.getConfig().getTimeout()));
        pipeline.addLast(PipelineUtils.FRAME_PREPENDER, FRAME_PREPENDER);
        pipeline.addLast(PipelineUtils.BOSS_HANDLER, new MainHandler());
    }

    public void registerSonarChannel(final ChannelPipeline pipeline) {
        pipeline.addFirst(HANDLER, new BungeeHandler());
        pipeline.addFirst(DECODER, new BungeeDecoder());
    }
}
