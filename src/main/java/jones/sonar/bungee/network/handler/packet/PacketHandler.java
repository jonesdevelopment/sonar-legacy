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

package jones.sonar.bungee.network.handler.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.network.handler.PlayerHandler;
import jones.sonar.universal.data.player.PlayerData;
import jones.sonar.universal.data.player.manager.PlayerDataManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.util.ExceptionHandler;
import jones.sonar.universal.util.ProtocolVersion;
import jones.sonar.universal.whitelist.Whitelist;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@RequiredArgsConstructor
public final class PacketHandler extends ChannelDuplexHandler {

    private final PlayerHandler playerHandler;

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ExceptionHandler.handle(ctx.channel(), cause);
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        if (msg instanceof PluginMessage) {
            final PluginMessage pluginMessage = (PluginMessage) msg;

            if (pluginMessage.getTag().equalsIgnoreCase("mc|brand")
                    || pluginMessage.getTag().equalsIgnoreCase("minecraft:brand")) {

                // don't send a client brand packet if the client's version is below 1.13
                // since clients below 1.13 do not use the (server) client brand anywhere
                if (playerHandler.getVersion() < ProtocolVersion.MINECRAFT_1_13) {
                    return;
                }

                String backend;

                final String data = new String(pluginMessage.getData());

                try {
                    backend = data.split(" <- ")[1];
                } catch (Exception exception) {
                    backend = "unknown";
                }

                final ByteBuf brand = ByteBufAllocator.DEFAULT.heapBuffer();

                DefinedPacket.writeString(Config.Values.FAKE_SERVER_CLIENT_BRAND
                        .replaceAll("%proxy%", SonarBungee.INSTANCE.proxy.getName())
                        .replaceAll("%backend%", backend), brand);

                pluginMessage.setData(DefinedPacket.toArray(brand));

                brand.release();
            }
        }

        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof PacketWrapper) {
            final PacketWrapper wrapper = (PacketWrapper) msg;

            final Object packet = wrapper.packet;

            check: {
                if (packet == null) break check;

                if (wrapper.packet instanceof LoginRequest) {
                    if (playerHandler.getVersion() >= ProtocolVersion.MINECRAFT_1_19_1
                            && ProtocolConstants.SUPPORTED_VERSION_IDS.contains(playerHandler.getVersion())
                            /*&& playerHandler.bungee.config.isEnforceSecureProfile()*/) {
                        if (((LoginRequest) wrapper.packet).getPublicKey() == null) {
                            throw SonarBungee.INSTANCE.EXCEPTION;
                        }
                    }
                }

                final ProxiedPlayer proxiedPlayer = SonarBungee.INSTANCE.proxy.getPlayer(playerHandler.getName());

                if (proxiedPlayer == null) break check;

                final PlayerData playerData = PlayerDataManager.create(proxiedPlayer.getName());

                final InetAddress inetAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();

                if (wrapper.packet instanceof ClientSettings) {
                    final ClientSettings clientSettings = (ClientSettings) wrapper.packet;

                    final byte viewDistance = clientSettings.getViewDistance();

                    // don't allow spoofing by the client
                    playerData.sentClientSettings = viewDistance > 1;
                }

                else if (wrapper.packet instanceof PluginMessage) {
                    final PluginMessage customPayload = (PluginMessage) wrapper.packet;

                    // we only want to check the client brand channel
                    if (customPayload.getTag().equals("MC|Brand") || customPayload.getTag().equals("minecraft:brand")) {
                        playerData.clientBrand = new String(customPayload.getData());

                        // the client brand has to match a specific validation regex
                        // the client brand cannot be empty and shouldn't be longer than 128 characters
                        if (!playerData.clientBrand.isEmpty() && playerData.clientBrand.length() < 128) {
                            playerData.sentClientBrand = true;
                        }
                    }
                }

                // 1.19 clients use signatures and an encrypted, custom chat packet, so we need to check
                // if that packet is being sent too to avoid exploits
                else if (wrapper.packet instanceof ClientChat || wrapper.packet instanceof Chat) {

                    // we don't want to allow chat packets if the client
                    // hasn't sent a client settings packet yet
                    if (!playerData.passes()) {
                        proxiedPlayer.disconnect(Messages.Values.DISCONNECT_BOT_DETECTION);
                        playerData.lastDetection = System.currentTimeMillis();

                        // remove from whitelist, if whitelisted
                        Whitelist.removeFromWhitelist(inetAddress);

                        // reset the amount of keep alive packets for automatic whitelisting
                        playerData.keepAliveSent = 0L;
                        return;
                    }
                }

                else if (wrapper.packet instanceof KeepAlive && playerData.passes() && !Whitelist.isWhitelisted(inetAddress)) {
                    playerData.keepAliveSent++;

                    // we only want to whitelist the player if they already sent
                    // more than a specific amount of keep alive packets to the server
                    if (playerData.keepAliveSent > Config.Values.MINIMUM_KEEP_ALIVE_TICK) {
                        Whitelist.addToWhitelist(inetAddress);
                    }
                }
            }
        }

        super.channelRead(ctx, msg);
    }
}
