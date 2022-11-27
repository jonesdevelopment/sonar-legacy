package jones.sonar.bungee.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import jones.sonar.bungee.network.handler.BungeeHandler;
import jones.sonar.universal.util.ExceptionHandler;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SonarPipelines implements SonarPipeline {

    public final ChannelHandler EXCEPTION_HANDLER = new PacketExceptionHandler();

    @ChannelHandler.Sharable
    private class PacketExceptionHandler extends ChannelDuplexHandler {

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            ExceptionHandler.handle(ctx.channel(), cause);
        }
    }

    private final ChannelHandler SONAR_HANDLER = new BungeeHandler();

    public void register(final ChannelPipeline pipeline) {
        pipeline.addFirst(HANDLER, SONAR_HANDLER);
    }
}
