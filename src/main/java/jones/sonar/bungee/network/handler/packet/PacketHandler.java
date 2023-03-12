package jones.sonar.bungee.network.handler.packet;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.FastThreadLocalThread;
import jones.sonar.bungee.caching.ServerPingCache;
import jones.sonar.bungee.config.Config;
import jones.sonar.bungee.config.Messages;
import jones.sonar.bungee.network.handler.PlayerHandler;
import jones.sonar.universal.blacklist.Blacklist;
import jones.sonar.universal.counter.Counter;
import jones.sonar.universal.data.player.PlayerData;
import jones.sonar.universal.data.player.manager.PlayerDataManager;
import jones.sonar.universal.platform.bungee.SonarBungee;
import jones.sonar.universal.queue.LoginCache;
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
import java.util.Queue;
import java.util.concurrent.*;

@RequiredArgsConstructor
public final class PacketHandler extends ChannelDuplexHandler {

    // we don't want people like the smog client dude to flood the proxy with BungeeCord commands since they don't have any form of spam limitation
    private static final Cache<String, Byte> cachedPlayerChatMessages = CacheBuilder.newBuilder()
            .expireAfterWrite(125L, TimeUnit.MILLISECONDS).build();
    private static final ExecutorService pool = new ThreadPoolExecutor(0, 256,
            10L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> new FastThreadLocalThread(r));
    public static final Queue<PlayerHandler> connectionQueue = new ConcurrentLinkedQueue<>();
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

                if (!Config.Values.FAKE_SERVER_CLIENT_BRAND.isEmpty()) {
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
        }

        super.write(ctx, msg, promise);
    }

    // The concept for this verification queue was taken from Hyperion
    public static void runQueue() {
        synchronized (connectionQueue) {
            for (int i = 0; i < Math.min(connectionQueue.size(), Config.Values.MINIMUM_JOINS_PER_SECOND); i++) {
                pool.execute(() -> {
                    final PlayerHandler handler = connectionQueue.poll();

                    if (handler == null
                            || !handler.isConnected()
                            || handler.usernameForVerification == null) {
                        return;
                    }

                    if (!handler.channel.isActive()) {
                        ExceptionHandler.handle(handler.channel, SonarBungee.EXCEPTION);
                        return;
                    }

                    LoginCache.HAVE_LOGGED_IN.add(handler.usernameForVerification);

                    handler.disconnect_(Messages.Values.DISCONNECT_FIRST_JOIN);
                });
            }
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof PacketWrapper) {
            final PacketWrapper wrapper = (PacketWrapper) msg;

            final Object packet = wrapper.packet;

            check: {
                if (packet == null) break check;

                if (wrapper.packet instanceof LoginRequest) {
                    if (Config.Values.FORCE_PUBLIC_KEY
                            && playerHandler.getVersion() > ProtocolVersion.MINECRAFT_1_19
                            && playerHandler.getVersion() < ProtocolVersion.MINECRAFT_1_19_3
                            && ProtocolConstants.SUPPORTED_VERSION_IDS.contains(playerHandler.getVersion())) {
                        if (((LoginRequest) wrapper.packet).getPublicKey() == null) {
                            throw SonarBungee.EXCEPTION;
                        }
                    }

                    Counter.JOINS_PER_SECOND.increment();

                    final String username = ((LoginRequest) wrapper.packet).getData();

                    if (username.isEmpty()) {
                        throw SonarBungee.EXCEPTION;
                    }

                    if (!LoginCache.HAVE_LOGGED_IN.contains(username)) {
                        if (Config.Values.ENABLE_FIRST_JOIN) {
                            playerHandler.channel.eventLoop().execute(() -> {
                                if (!playerHandler.channel.isActive()) {
                                    ExceptionHandler.handle(playerHandler.channel, SonarBungee.EXCEPTION);
                                    return;
                                }

                                playerHandler.usernameForVerification = username;

                                if (!connectionQueue.isEmpty() && connectionQueue.stream()
                                        .anyMatch(handler -> handler.usernameForVerification.equals(username)
                                                || handler.getAddress().getAddress().toString().equals(playerHandler.inetAddress.toString()))) {
                                    // TODO: Check if it's safe to temp blacklist here
                                    playerHandler.disconnect_(Messages.Values.TEMP_BLACKLISTED);
                                    Blacklist.addToTempBlacklist(playerHandler.inetAddress);
                                    return;
                                }

                                connectionQueue.add(playerHandler);
                            });
                            return;
                        }

                        LoginCache.HAVE_LOGGED_IN.add(username);
                    }

                    if (!ServerPingCache.HAS_PINGED.asMap().containsKey(playerHandler.inetAddress)
                            && (Config.Values.PING_BEFORE_JOIN || Counter.JOINS_PER_SECOND.get() >= Config.Values.MINIMUM_JOINS_PER_SECOND)) {
                        playerHandler.disconnect_(Messages.Values.DISCONNECT_PING_BEFORE_JOIN);
                        return;
                    }

                    if (Blacklist.isTempBlacklisted(playerHandler.inetAddress)) {
                        playerHandler.disconnect_(Messages.Values.TEMP_BLACKLISTED);
                        return;
                    }
                }

                final ProxiedPlayer proxiedPlayer = SonarBungee.INSTANCE.proxy.getPlayer(playerHandler.getName());

                if (proxiedPlayer == null) break check;

                final PlayerData playerData = PlayerDataManager.create(proxiedPlayer.getName());

                final InetAddress inetAddress = ((InetSocketAddress) proxiedPlayer.getSocketAddress()).getAddress();

                // ClientSettings packet
                if (wrapper.packet instanceof ClientSettings) {
                    final ClientSettings clientSettings = (ClientSettings) wrapper.packet;

                    final byte viewDistance = clientSettings.getViewDistance();

                    // don't allow spoofing by the client
                    playerData.sentClientSettings = viewDistance > 0;
                }

                // PluginMessage packet
                else if (wrapper.packet instanceof PluginMessage) {
                    final PluginMessage pluginMessage = (PluginMessage) wrapper.packet;

                    // we only want to check the client brand channel
                    if (pluginMessage.getTag().equals("MC|Brand") || pluginMessage.getTag().equals("minecraft:brand")) {

                        // safely read the client brand
                        final ByteBuf brand = Unpooled.wrappedBuffer(pluginMessage.getData());

                        playerData.clientBrand = DefinedPacket.readString(brand);

                        brand.release(); // important; we don't want memory leaks

                        // the client brand has to match a specific validation regex
                        // the client brand cannot be empty and shouldn't be longer than 128 characters
                        if (!playerData.clientBrand.isEmpty() && playerData.clientBrand.length() < 128) {
                            playerData.sentClientBrand = true;
                        }
                    }
                }

                // 1.19 clients use signatures and an encrypted, custom chat packet, so we need to check
                // if that packet is being sent too to avoid exploits
                else if (wrapper.packet instanceof Chat || wrapper.packet.toString().startsWith("ClientChat(")) {

                    // we don't want to allow chat packets if the client
                    // hasn't sent a client settings packet yet
                    if (!playerData.passes() && !Whitelist.isWhitelisted(inetAddress)) {
                        proxiedPlayer.disconnect(Messages.Values.DISCONNECT_BOT_DETECTION);
                        playerData.lastDetection = System.currentTimeMillis();

                        // remove from whitelist, if whitelisted
                        Whitelist.removeFromWhitelist(inetAddress);

                        // add to temporary blacklist
                        Blacklist.addToTempBlacklist(inetAddress);

                        // reset the amount of keep alive packets for automatic whitelisting
                        playerData.keepAliveSent = 0L;
                        return;
                    }

                    // spam limit - 125ms per chat message (3 ticks)
                    // the concept of this check was taken from Hyperion
                    // feel free to steal this :D
                    if (cachedPlayerChatMessages.asMap().containsKey(playerData.username)) {
                        return;
                    }

                    cachedPlayerChatMessages.put(playerData.username, (byte) 0); // cache
                }

                else if (wrapper.packet instanceof KeepAlive) {
                    if (playerData.passes() && !Whitelist.isWhitelisted(inetAddress)) {
                        playerData.keepAliveSent++; // increment amount of keep alive packets sent by the player

                        // we only want to whitelist the player if they already sent
                        // more than a specific amount of keep alive packets to the server
                        if (playerData.keepAliveSent > Config.Values.MINIMUM_KEEP_ALIVE_TICK) {
                            Whitelist.addToWhitelist(inetAddress);
                        }
                    }
                }
            }
        }

        super.channelRead(ctx, msg);
    }
}
