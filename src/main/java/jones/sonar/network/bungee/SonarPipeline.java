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

package jones.sonar.network.bungee;

import io.netty.channel.WriteBufferWaterMark;
import net.md_5.bungee.protocol.Varint21LengthFieldPrepender;

public interface SonarPipeline {

    String HANDLER = "sonar-handler",
            ENCODER = "sonar-encoder",
            DECODER = "sonar-decoder";

    Varint21LengthFieldPrepender FRAME_PREPENDER = new Varint21LengthFieldPrepender();

    int LOW_MARK = Integer.getInteger("net.md_5.bungee.low_mark", 2 << 18); // 0.5 mb
    int HIGH_MARK = Integer.getInteger("net.md_5.bungee.high_mark", 2 << 20); // 2 mb

    WriteBufferWaterMark MARK = new WriteBufferWaterMark(LOW_MARK, HIGH_MARK);

}
