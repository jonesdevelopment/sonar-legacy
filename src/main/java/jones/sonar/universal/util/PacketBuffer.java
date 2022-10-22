package jones.sonar.universal.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class PacketBuffer {

    private final ByteBuf byteBuf = Unpooled.buffer();

    public byte[] toArray() {
        if (byteBuf.hasArray()) {
            return Arrays.copyOfRange(byteBuf.array(), byteBuf.arrayOffset(), byteBuf.arrayOffset() + byteBuf.writerIndex());
        }

        final byte[] bytes = new byte[byteBuf.writerIndex()];

        byteBuf.readBytes(bytes);

        return bytes;
    }

    public void write(final String data) {
        byteBuf.writeBytes(data.getBytes(StandardCharsets.UTF_8));
    }
}
