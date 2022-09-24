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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public final class TimeoutHandler extends IdleStateHandler {

    private boolean closed;

    public TimeoutHandler(final long timeout) {
        super(timeout, 0L, 0L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        //
    }

    @Override
    protected void channelIdle(final ChannelHandlerContext ctx, final IdleStateEvent evt) throws Exception {
        assert evt.state() == IdleState.READER_IDLE;

        if (!closed) {
            ctx.channel().unsafe().closeForcibly();

            closed = true;
        }
    }
}
