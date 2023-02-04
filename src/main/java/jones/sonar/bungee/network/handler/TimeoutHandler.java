package jones.sonar.bungee.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public final class TimeoutHandler extends IdleStateHandler {
    private boolean closed;

    @Deprecated
    public TimeoutHandler() {
        this(12L, TimeUnit.SECONDS);
    }

    public TimeoutHandler(final long timeout) {
        this(timeout, TimeUnit.MILLISECONDS);
    }

    public TimeoutHandler(final long timeout, final TimeUnit timeUnit) {
        super(timeout, 0L, 0L, timeUnit);
    }

    @Override
    protected void channelIdle(final ChannelHandlerContext ctx,
                               final IdleStateEvent idleStateEvent) throws Exception {
        assert idleStateEvent.state() == IdleState.READER_IDLE;

        readTimedOut(ctx);
    }

    private void readTimedOut(final ChannelHandlerContext ctx) throws Exception {
        if (!closed) {

            // ==========================================================
            // The netty (default) ReadTimeoutHandler would normally just throw an Exception
            // The default ReadTimeoutHandler does only check for the boolean 'closed' and
            // still throws the Exception even if the channel is closed
            // This was discovered and fixed by @jones
            // ==========================================================

            if (ctx.channel().isActive()) {
                ctx.close();
            }

            closed = true;
        }
    }
}
