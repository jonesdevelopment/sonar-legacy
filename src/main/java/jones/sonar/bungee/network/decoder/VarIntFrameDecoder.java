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

package jones.sonar.bungee.network.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

// © velocitypowered.com
public final class VarIntFrameDecoder extends ByteToMessageDecoder {

    private static final CorruptedFrameException EXCEPTION_IN_FRAME = new CorruptedFrameException("Corrupted/Mutated frame (Exploit?)");

    @Override
    protected void decode(final ChannelHandlerContext ctx,
                          final ByteBuf byteBuf,
                          final List<Object> out) throws Exception {
        if (!ctx.channel().isActive()) {
            byteBuf.clear();
            return;
        }

        final VarIntByteDecoder reader = new VarIntByteDecoder();

        final int end = byteBuf.forEachByte(reader);

        if (end == -1) {

            // ==========================================================
            // This is probably a good sign that the buffer was too short or empty
            // since the ByteBuf cannot hold a proper VarInt.
            // ==========================================================

            if (reader.result == VarIntByteDecoder.DecoderResult.RUN_OF_ZEROES) {
                byteBuf.clear();
            }
            return;
        }

        switch (reader.result) {

            // this will return to the point where the next varInt starts
            case RUN_OF_ZEROES: {
                byteBuf.readerIndex(end);
                break;
            }

            case SUCCESS: {
                final int readVarInt = reader.readVarInt, bytesRead = reader.bytesRead;

                if (readVarInt < 0) {
                    byteBuf.clear();
                    throw EXCEPTION_IN_FRAME;
                }

                else if (readVarInt == 0) {

                    // ==========================================================
                    // Actually, we don't want to throw an Exception if the packet is empty.
                    // The check would also false flag a lot of legit players since packets
                    // in 1.7 could sometimes be empty.
                    // ==========================================================

                    byteBuf.readerIndex(end + 1);
                }

                else {
                    final int minimumRead = bytesRead + readVarInt;

                    if (byteBuf.isReadable(minimumRead)) {
                        out.add(byteBuf.retainedSlice(end + 1, readVarInt));

                        byteBuf.skipBytes(minimumRead);
                    }
                }
                break;
            }

            default: {
                byteBuf.clear();
                throw EXCEPTION_IN_FRAME;
            }
        }
    }
}
