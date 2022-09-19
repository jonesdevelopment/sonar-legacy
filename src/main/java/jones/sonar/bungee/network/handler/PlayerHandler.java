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

package jones.sonar.bungee.network.handler;

import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import jones.sonar.SonarBungee;
import jones.sonar.bungee.caching.ServerDataProvider;
import jones.sonar.bungee.caching.ServerPingCache;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.detection.LoginHandler;
import jones.sonar.bungee.network.handler.state.ConnectionState;
import jones.sonar.bungee.util.json.LegacyGsonFormat;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.ServerStatistics;
import jones.sonar.universal.data.connection.ConnectionData;
import jones.sonar.universal.data.connection.manager.ConnectionDataManager;
import jones.sonar.universal.detection.Detection;
import jones.sonar.universal.detection.DetectionResult;
import jones.sonar.universal.queue.PlayerQueue;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.*;

import java.net.InetAddress;

public final class PlayerHandler extends InitialHandler {

    public PlayerHandler(final ChannelHandlerContext ctx, final ListenerInfo listener) {
        super(BungeeCord.getInstance(), listener);

        this.ctx = ctx;
    }

    private ConnectionState currentState = ConnectionState.HANDSHAKE;

    private final BungeeCord bungee = BungeeCord.getInstance();

    private final SonarBungee sonar = SonarBungee.INSTANCE;

    private final ChannelHandlerContext ctx;

    private ChannelWrapper channelWrapper;

    private boolean queue = false;

    @Override
    public void connected(final ChannelWrapper channelWrapper) throws Exception {
        this.channelWrapper = channelWrapper;
        super.connected(channelWrapper);
    }

    @Override
    public void exception(final Throwable cause) throws Exception {
        ctx.channel().unsafe().closeForcibly();
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
    public void handle(final EncryptionResponse encryptionResponse) throws Exception {
        Counter.ENCRYPTIONS_PER_SECOND.increment();

        if (currentState != ConnectionState.JOINING) {
            throw sonar.EXCEPTION;
        }

        super.handle(encryptionResponse);
    }

    @Override
    public void handle(final Handshake handshake) throws Exception {
        Counter.HANDSHAKES_PER_SECOND.increment();

        if (currentState != ConnectionState.HANDSHAKE) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        final InetAddress inetAddress = inetAddress();

        switch (handshake.getRequestedProtocol()) {

            /*
             * ID 1 -> Ping
             */

            case 1: {
                currentState = ConnectionState.PINGING;
                break;
            }

            /*
             * ID 2 -> Join
             */

            case 2: {
                currentState = ConnectionState.JOINING;
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

    @Override
    public void handle(final StatusRequest statusRequest) throws Exception {
        Counter.PINGS_PER_SECOND.increment();

        if (currentState != ConnectionState.PINGING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        final Handshake handshake = getHandshake();
        final ListenerInfo listener = getListener();

        final ServerInfo forced = ServerDataProvider.getForcedHost(listener, getVirtualHost());

        final Callback<ServerPing> pingBack = (result, error) -> {
            if (error != null) return;

            if (!ctx.channel().isActive()) return;

            final Callback<ProxyPingEvent> callback = (pingResult, error1) -> {
                if (error1 != null) return;

                if (!ctx.channel().isActive()) return;

                final Gson gson = handshake.getProtocolVersion() <= 4 ? LegacyGsonFormat.LEGACY : bungee.gson;

                unsafe().sendPacket(new StatusResponse(gson.toJson(pingResult.getResponse())));

                if (bungee.getConnectionThrottle() != null) {
                    bungee.getConnectionThrottle().unthrottle(getSocketAddress());
                }
            };

            sonar.callEvent(new ProxyPingEvent(this, result, callback));
        };

        if (forced != null && listener.isPingPassthrough() && Config.Values.ALLOW_PING_PASS_THROUGH) {
            ((BungeeServerInfo) forced).ping(pingBack, handshake.getProtocolVersion());
        } else {
            final ServerPing serverPing = ServerPingCache.getCached(listener, forced != null ? forced.getMotd() : listener.getMotd());

            serverPing.getVersion().setProtocol(getVersion());

            pingBack.done(serverPing, null);
        }

        currentState = ConnectionState.PINGING;
    }

    @Override
    public void handle(final PingPacket ping) throws Exception {
        if (currentState != ConnectionState.PINGING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        unsafe().sendPacket(ping);

        ctx.channel().unsafe().closeForcibly();
    }

    @Override
    public void handle(final LoginRequest loginRequest) throws Exception {
        Counter.JOINS_PER_SECOND.increment();

        if (currentState != ConnectionState.JOINING) {
            throw sonar.EXCEPTION;
        }

        currentState = ConnectionState.PROCESSING;

        final ConnectionData data = ConnectionDataManager.createOrReturn(inetAddress());

        data.username = loginRequest.getData();

        final Detection detection = LoginHandler.check(data);

        if (detection.result == DetectionResult.DENIED) {
            switch (detection.key) {
                default: {
                    throw sonar.EXCEPTION;
                }

                case 1: {
                    disconnect(Messages.Values.DISCONNECT_FIRST_JOIN);
                    return;
                }

                case 2: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect(Messages.Values.DISCONNECT_INVALID_NAME);
                    return;
                }

                case 3: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect(Messages.Values.DISCONNECT_TOO_FAST_RECONNECT);
                    return;
                }

                case 4: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect(Messages.Values.DISCONNECT_TOO_MANY_ONLINE);
                    return;
                }

                case 5: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect(Messages.Values.DISCONNECT_QUEUED
                            .replaceAll("%position%", sonar.FORMAT.format(PlayerQueue.getPosition(data.username)))
                            .replaceAll("%size%", sonar.FORMAT.format(PlayerQueue.QUEUE.size())));
                    return;
                }

                case 6: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect(Messages.Values.DISCONNECT_ATTACK);
                    return;
                }

                case 7: {
                    ServerStatistics.BLOCKED_CONNECTIONS++;
                    disconnect(Messages.Values.DISCONNECT_BOT_BEHAVIOUR);
                    return;
                }
            }
        }

        if (sonar.proxy.getPlayer(data.username) != null) {
            disconnect(Messages.Values.DISCONNECT_ALREADY_CONNECTED);
            return;
        }

        currentState = ConnectionState.JOINING;

        super.handle(loginRequest);
    }

    private InetAddress inetAddress() {
        return getAddress().getAddress();
    }
}
