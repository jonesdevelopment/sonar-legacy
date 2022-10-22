package jones.sonar.bungee.network.handler;

import io.netty.channel.ChannelHandlerContext;
import jones.sonar.universal.util.ExceptionHandler;
import net.md_5.bungee.netty.HandlerBoss;

public final class InboundHandler extends HandlerBoss {

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        ExceptionHandler.handle(ctx.channel(), cause);
    }
}
