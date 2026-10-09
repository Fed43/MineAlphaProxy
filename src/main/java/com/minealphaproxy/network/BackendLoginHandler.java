package com.minealphaproxy.network;

import com.minealphaproxy.compat.velocity.VelocityCommandManager;
import com.minealphaproxy.compat.velocity.VelocityPlayer;
import com.minealphaproxy.compat.velocity.VelocityProxyServer;
import com.minealphaproxy.config.ProxyConfig;
import com.minealphaproxy.event.EventBus;
import com.minealphaproxy.util.Logger;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PostLoginEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

public final class BackendLoginHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Channel clientChannel;
    private final ProxyConfig config;
    private final Logger logger;

    private final String playerIp;
    private final UUID playerUuid;
    private final String username;
    private final String forwardingSecret;
    private final List<PlayerProfile.Property> properties;
    private final PlayerProfile.ProfileKey profileKey;
    private final EventBus eventBus;
    private final VelocityProxyServer velocityProxyServer;

    public BackendLoginHandler(Channel clientChannel, ProxyConfig config,
                               Logger logger, String playerIp,
                               UUID playerUuid, String username,
                               String forwardingSecret,
                               List<PlayerProfile.Property> properties,
                               PlayerProfile.ProfileKey profileKey,
                               EventBus eventBus,
                               VelocityProxyServer velocityProxyServer) {
        this.clientChannel = clientChannel;
        this.config = config;
        this.logger = logger;
        this.playerIp = playerIp;
        this.playerUuid = playerUuid;
        this.username = username;
        this.forwardingSecret = forwardingSecret;
        this.properties = properties;
        this.profileKey = profileKey;
        this.eventBus = eventBus;
        this.velocityProxyServer = velocityProxyServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        int packetId = VarInts.readVarInt(buf);
        switch (packetId) {
            case 0x00 -> handleDisconnect(ctx, buf);
            case 0x01 -> {
                logger.warn("Backend requested encryption — enable velocity.enabled=true");
                clientChannel.close();
                ctx.close();
            }
            case 0x02 -> handleLoginSuccess(ctx, buf);
            case 0x03 -> handleSetCompression(ctx, buf);
            case 0x04 -> handlePluginRequest(ctx, buf);
            default -> logger.warn("Unknown login packet: 0x{}",
                    Integer.toHexString(packetId));
        }
    }

    private void handleDisconnect(ChannelHandlerContext ctx, ByteBuf buf) {
        String reason = "(no reason)";
        try { reason = VarInts.readString(buf); }
        catch (Exception e) { reason = "(parse fail)"; }
        logger.warn("Backend disconnected player: {}", reason);

        ByteBuf out = ctx.alloc().buffer();
        VarInts.writeVarInt(out, 0x00);
        VarInts.writeString(out, reason);
        clientChannel.writeAndFlush(out);
        clientChannel.close();
        ctx.close();
    }

    private void handleSetCompression(ChannelHandlerContext ctx, ByteBuf buf) {
        int threshold = VarInts.readVarInt(buf);
        logger.info("Compression enabled (threshold={})", threshold);

        ctx.pipeline().addAfter("frame-encoder", "compression",
                new CompressionCodec(threshold));

        ByteBuf out = ctx.alloc().buffer();
        VarInts.writeVarInt(out, 0x03);
        VarInts.writeVarInt(out, threshold);
        clientChannel.writeAndFlush(out);

        clientChannel.pipeline().addAfter("frame-encoder", "compression",
                new CompressionCodec(threshold));
    }

    private void handleLoginSuccess(ChannelHandlerContext ctx, ByteBuf in) {
        try {
            if (in.readableBytes() < 16) {
                logger.error("Login Success too short");
                clientChannel.close();
                ctx.close();
                return;
            }

            in.skipBytes(16);
            ByteBuf rest = ctx.alloc().buffer();
            rest.writeBytes(in);

            ByteBuf out = ctx.alloc().buffer();
            VarInts.writeVarInt(out, 0x02);
            out.writeLong(playerUuid.getMostSignificantBits());
            out.writeLong(playerUuid.getLeastSignificantBits());
            out.writeBytes(rest);
            rest.release();

            clientChannel.writeAndFlush(out);
            logger.info("Login Success rewritten with UUID {}", playerUuid);

            // Регистрируем игрока
            InetSocketAddress address = clientChannel.remoteAddress()
                    instanceof InetSocketAddress isa ? isa
                    : new InetSocketAddress("127.0.0.1", 0);
            VelocityPlayer player = new VelocityPlayer(
                    username, playerUuid, address, clientChannel);
            velocityProxyServer.addPlayer(player);

            // События для плагинов
            eventBus.fireSync(new LoginEvent(player));
            eventBus.fireSync(new PostLoginEvent(player));

            // ==== Relay с перехватом команд ====

            VelocityCommandManager cm = velocityProxyServer.getCommandManager()
                    instanceof VelocityCommandManager vcm ? vcm : null;

            // client → backend: перехватываем команды
            var clientPipeline = clientChannel.pipeline();
            if (clientPipeline.get("login") != null) {
                clientPipeline.replace("login", "relay",
                        new PacketRelayHandler(ctx.channel(), true,
                                cm, player, logger));
            } else if (clientPipeline.get("encryption-response") != null) {
                clientPipeline.replace("encryption-response", "relay",
                        new PacketRelayHandler(ctx.channel(), true,
                                cm, player, logger));
            }

            // backend → client: без перехвата
            var backendPipeline = ctx.channel().pipeline();
            if (backendPipeline.get("backend-login") != null) {
                backendPipeline.replace("backend-login", "relay",
                        new PacketRelayHandler(clientChannel, false,
                                null, null, logger));
            }

        } catch (Exception e) {
            logger.error("Failed to rewrite Login Success", e);
            clientChannel.close();
            ctx.close();
        }
    }

    private void handlePluginRequest(ChannelHandlerContext ctx, ByteBuf buf) {
        int messageId = VarInts.readVarInt(buf);
        String channel = VarInts.readString(buf);

        logger.info("Backend login plugin request: channel='{}'", channel);

        if (VelocityForwarding.CHANNEL.equals(channel)) {
            logger.info("Sending modern forwarding data for {} (uuid {})",
                    username, playerUuid);

            ByteBuf payload = VelocityForwarding.createForwardingData(
                    forwardingSecret, playerIp, playerUuid, username,
                    properties, profileKey);

            ByteBuf out = ctx.alloc().buffer();
            VarInts.writeVarInt(out, 0x02);
            VarInts.writeVarInt(out, messageId);
            out.writeBoolean(true);
            out.writeBytes(payload);
            payload.release();
            ctx.writeAndFlush(out);
        } else {
            ByteBuf out = ctx.alloc().buffer();
            VarInts.writeVarInt(out, 0x02);
            VarInts.writeVarInt(out, messageId);
            out.writeBoolean(false);
            ctx.writeAndFlush(out);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("Backend login error: {}", cause.getMessage());
        if (clientChannel.isActive()) clientChannel.close();
        ctx.close();
    }
}