package jones.sonar.bungee.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import java.util.concurrent.TimeUnit;

public final class TimeoutHandler extends IdleStateHandler {
    private boolean closed;

    public TimeoutHandler(int timeoutSeconds) {
        this(timeoutSeconds, TimeUnit.SECONDS);
    }

    public TimeoutHandler(long timeout, TimeUnit unit) {
        super(timeout, 0L, 0L, unit);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        assert evt.state() == IdleState.READER_IDLE;

        this.readTimedOut(ctx);
    }

    private void readTimedOut(ChannelHandlerContext ctx) throws Exception {
        if (!this.closed) {
            ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
            ctx.close();
            this.closed = true;
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        //
    }
}
