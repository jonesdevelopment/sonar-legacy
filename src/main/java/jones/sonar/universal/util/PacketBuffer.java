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
