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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import jones.sonar.SonarBungee;
import jones.sonar.config.Config;
import jones.sonar.config.Messages;
import jones.sonar.counter.Counter;
import jones.sonar.data.ServerStatistics;
import jones.sonar.data.connection.ConnectionData;
import jones.sonar.data.connection.manager.ConnectionDataManager;
import jones.sonar.detection.Detection;
import jones.sonar.detection.DetectionResult;
import jones.sonar.detection.bungee.LoginHandler;
import jones.sonar.network.bungee.handler.state.ConnectionState;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.*;

import java.net.InetAddress;

public final class PlayerHandler extends InitialHandler {

    public PlayerHandler(final ChannelHandlerContext ctx, final ListenerInfo listener) {
        super(BungeeCord.getInstance(), listener);

        this.ctx = ctx;
    }

    private ConnectionState currentState = ConnectionState.HANDSHAKE;

    private final SonarBungee sonar = SonarBungee.INSTANCE;

    private final ChannelHandlerContext ctx;

    @Override
    public void exception(final Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void handle(final PacketWrapper packet) throws Exception {
        if (packet == null) {
            throw sonar.EXCEPTION;
        }

        if (packet.buf.readableBytes() > Config.Values.MAX_PACKET_BYTES) {
            packet.buf.clear();
            throw sonar.EXCEPTION;
        }
    }

    @Override
    public void handle(final EncryptionRequest encryptionRequest) throws Exception {
        Counter.ENCRYPTIONS_PER_SECOND.increment();

        System.out.println(encryptionRequest.getPublicKey().length);
        System.out.println(encryptionRequest.getVerifyToken().length);
        System.out.println(encryptionRequest.getServerId());
        super.handle(encryptionRequest);
    }

    @Override
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

                currentState = ConnectionState.PINGING;
                break;
            }

            /*
             * ID 2 -> Join
             */

            case 2: {
                Counter.JOINS_PER_SECOND.increment();

                currentState = ConnectionState.JOINING;

                if (!ProtocolConstants.SUPPORTED_VERSION_IDS.contains(handshake.getProtocolVersion())) {
                    close(new Kick(ComponentSerializer.toString(Messages.Values.DISCONNECT_UNSUPPORTED_VERSION)));
                    return;
                }
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

    public void close(final Object packet) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(packet).addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE, ChannelFutureListener.CLOSE);
        } else {
            ctx.close();
        }
    }

    @Override
    public void handle(final PingPacket ping) throws Exception {
        if (currentState != ConnectionState.PINGING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        unsafe().sendPacket(ping);

        ctx.close();
    }

    @Override
    public void handle(final LoginRequest loginRequest) throws Exception {
        if (currentState != ConnectionState.JOINING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        final ConnectionData data = ConnectionDataManager.createOrReturn(inetAddress());

        data.username = loginRequest.getData();

        final Detection detection = LoginHandler.check(data);

        if (detection.result == DetectionResult.DENIED) {
            switch (detection.disconnectMessageKey) {
                case "1": {
                    disconnect(Messages.Values.DISCONNECT_FIRST_JOIN);
                    return;
                }

                case "2": {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect(Messages.Values.DISCONNECT_INVALID_NAME);
                    return;
                }

                case "3": {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect(Messages.Values.DISCONNECT_TOO_FAST_RECONNECT);
                    return;
                }

                default: {
                    throw sonar.EXCEPTION;
                }
            }
        }

        if (sonar.proxy.getPlayer(data.username) != null) {
            disconnect(Messages.Values.DISCONNECT_ALREADY_CONNECTED);
            return;
        }

        super.handle(loginRequest);
    }

    private InetAddress inetAddress() {
        return getAddress().getAddress();
    }
}
