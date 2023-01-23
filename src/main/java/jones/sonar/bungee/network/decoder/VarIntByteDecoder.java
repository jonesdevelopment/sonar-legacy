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

import io.netty.util.ByteProcessor;

// © velocitypowered.com
public final class VarIntByteDecoder implements ByteProcessor {
    public DecoderResult result = DecoderResult.INVALID;
    public int readVarInt, bytesRead;

    @Override
    public boolean process(final byte by) throws Exception {
        if (by == 0 && bytesRead == 0) {
            // tentatively say it's invalid, but there's a possibility of redemption
            result = DecoderResult.RUN_OF_ZEROES;
            return true;
        }

        if (result == DecoderResult.RUN_OF_ZEROES) {
            return false;
        }

        readVarInt |= (by & 0x7F) << bytesRead++ * 7;

        if (bytesRead > 3) {
            result = DecoderResult.INVALID;
            return false;
        }

        if ((by & 0x80) != 128) {
            result = DecoderResult.SUCCESS;
            return false;
        }

        return true;
    }

    public enum DecoderResult {
        SUCCESS,
        INVALID,
        RUN_OF_ZEROES
    }
}
