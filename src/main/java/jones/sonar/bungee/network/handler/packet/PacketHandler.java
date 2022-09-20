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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import jones.sonar.SonarBungee;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.network.handler.PlayerHandler;
import jones.sonar.universal.data.player.PlayerData;
import jones.sonar.universal.data.player.manager.PlayerDataManager;
import jones.sonar.universal.util.ExceptionHandler;
import jones.sonar.universal.whitelist.Whitelist;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.*;

import java.net.InetSocketAddress;

@RequiredArgsConstructor
public final class PacketHandler extends ChannelDuplexHandler {

    private final PlayerHandler playerHandler;

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ExceptionHandler.handle(ctx.channel(), cause);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof PacketWrapper) {
            final PacketWrapper wrapper = (PacketWrapper) msg;

            final Object packet = wrapper.packet;

            check: {
                if (packet == null) break check;

                final ProxiedPlayer proxiedPlayer = SonarBungee.INSTANCE.proxy.getPlayer(playerHandler.getName());

                if (proxiedPlayer == null) break check;

                final PlayerData playerData = PlayerDataManager.create(proxiedPlayer.getName());

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
                        return;
                    }
                }

                else if (wrapper.packet instanceof KeepAlive && playerData.passes()) {
                    Whitelist.addToWhitelist(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
                }
            }
        }

        super.channelRead(ctx, msg);
    }
}
