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

package jones.sonar.network.bungee.handler;

import io.netty.channel.ChannelHandlerContext;
import jones.sonar.SonarBungee;
import jones.sonar.counter.Counter;
import jones.sonar.network.bungee.handler.state.ConnectionState;
import jones.sonar.util.logging.Logger;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Handshake;

public final class PlayerHandler extends InitialHandler {

    public PlayerHandler(final ChannelHandlerContext ctx, final ListenerInfo listener) {
        super(BungeeCord.getInstance(), listener);

        this.ctx = ctx;
    }

    private ConnectionState currentState = ConnectionState.HANDSHAKE;

    private final SonarBungee sonar = SonarBungee.INSTANCE;

    private final BungeeCord proxy = BungeeCord.getInstance();

    private final ChannelHandlerContext ctx;

    @Override
    public void handle(final PacketWrapper packet) throws Exception {
        if (packet == null) {
            throw sonar.EXCEPTION;
        }

        if (packet.buf.readableBytes() > 1024) {
            packet.buf.clear();
            throw sonar.EXCEPTION;
        }
    }

    public void handle(final Handshake handshake) throws Exception {
        if (currentState != ConnectionState.HANDSHAKE) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        switch (handshake.getRequestedProtocol()) {

            /*
             * ID 1 -> Ping
             */

            case 1: {
                Counter.PINGS_PER_SECOND.increment();

                if (proxy.config.isLogPings()) {
                    Logger.INFO.logNoPrefix(this + " has pinged");
                }

                break;
            }

            /*
             * ID 2 -> Join
             */

            case 2: {
                Counter.JOINS_PER_SECOND.increment();
                Logger.INFO.logNoPrefix(this + " has connected");
                break;
            }

            /*
             * The requested protocol can either be 1 or 2.
             * Anything else is not possible by the default
             * Minecraft client.
             */

            default: {
                throw sonar.EXCEPTION;
            }
        }

        super.handle(handshake);
    }
}
