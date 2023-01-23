package jones.sonar.bungee.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jones.sonar.bungee.config.Config;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.ExceptionHandler;

@ChannelHandler.Sharable
public final class BungeeHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ExceptionHandler.handle(ctx.channel(), cause);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            final ByteBuf byteBuf = (ByteBuf) msg;

            if (!ctx.channel().isActive() && byteBuf.refCnt() > 0) {
                byteBuf.release(byteBuf.refCnt());
                return;
            }

            if (!ctx.channel().isActive() || !byteBuf.isReadable()) {
                byteBuf.skipBytes(byteBuf.readableBytes());
                return;
            }

            byteBuf.markReaderIndex();

            if (byteBuf.readableBytes() > Config.Values.MAX_PACKET_BYTES
                    || byteBuf.capacity() > Config.Values.MAX_PACKET_CAPACITY
                    || byteBuf.writableBytes() > Config.Values.MAX_PACKET_CAPACITY
                    || byteBuf.writerIndex() > Config.Values.MAX_PACKET_INDEX
                    || byteBuf.readerIndex() > Config.Values.MAX_PACKET_BYTES
                    || byteBuf.readableBytes() <= 0) {
                byteBuf.clear();
                throw SonarBungee.INSTANCE.EXCEPTION;
            }

            final byte firstByte = byteBuf.readByte();
            final byte secondByte = byteBuf.readByte();

            // the first byte cannot be below 0 as it's the size of the first packet
            // the second byte is the handshake packet id which is always 0
            if (firstByte < 0 || secondByte != 0) {
                throw SonarBungee.INSTANCE.EXCEPTION;
            }

            byteBuf.resetReaderIndex();

            ctx.fireChannelRead(byteBuf);

            ctx.pipeline().remove(this);
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
