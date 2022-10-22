package jones.sonar.bungee.network.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import jones.sonar.bungee.config.Config;
import jones.sonar.universal.platform.bungee.SonarBungee;

import java.util.List;

public final class BungeeDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf byteBuf, final List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }

        // the byteBuf is always 4 bytes or longer in a handshake packet
        if (byteBuf.readableBytes() < 4) {
            byteBuf.clear();
            throw SonarBungee.INSTANCE.EXCEPTION;
        }

        final byte[] bytes = new byte[byteBuf.readableBytes()];

        // check for maximum byte length that kills over-sized packets
        if (bytes.length > Config.Values.MAX_PACKET_INDEX) {
            byteBuf.clear();
            throw SonarBungee.INSTANCE.EXCEPTION;
        }

        byteBuf.readBytes(bytes);

        byteBuf.resetReaderIndex();

        // the first byte is always greater than 0
        // the second byte is always 0
        if (bytes[0] <= 0 || bytes[1] != 0) {
            byteBuf.clear();
            throw SonarBungee.INSTANCE.EXCEPTION;
        }

        byteBuf.markReaderIndex();

        final int unsigned = byteBuf.readUnsignedByte();

        if (unsigned == 254) {
            byteBuf.resetReaderIndex();

            out.add(byteBuf.retain().duplicate());

            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }

        else if (unsigned == 2 && byteBuf.isReadable()) {
            byteBuf.resetReaderIndex();

            out.add(byteBuf.retain().duplicate());

            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }

        byteBuf.resetReaderIndex();

        ctx.pipeline().remove(this);
    }
}
