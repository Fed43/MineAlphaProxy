package com.minealphaproxy.network;

import com.minealphaproxy.compat.velocity.VelocityProxyServer;
import com.minealphaproxy.config.ProxyConfig;
import com.minealphaproxy.event.EventBus;
import com.minealphaproxy.lang.Messages;
import com.minealphaproxy.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BackendLoginFlow {

    private static final int BACKEND_CONNECT_TIMEOUT_MS = 5000;

    private BackendLoginFlow() {}

    public static void connect(ChannelHandlerContext ctx, ProxyConfig config,
                               Logger logger, String username, UUID uuid,
                               List<PlayerProfile.Property> properties,
                               EventBus eventBus,
                               VelocityProxyServer velocityProxyServer) {
        String serverName = config.defaultServer;
        String address = config.servers.list.get(serverName);
        if (address == null) {
            disconnect(ctx, Messages.get("default-server-missing",
                    Map.of("server", serverName)));
            return;
        }

        String host;
        int port;
        int colon = address.lastIndexOf(':');
        if (colon <= 0) { host = address; port = 25565; }
        else { host = address.substring(0, colon);
               port = Integer.parseInt(address.substring(colon + 1)); }

        String ip = extractIp(ctx);
        String secret = readSecret(config);
        PlayerProfile.ProfileKey key =
                ctx.channel().attr(LoginStartHandler.CLIENT_KEY).get();

        logger.info("Connecting to backend '{}' ({}:{})...", serverName, host, port);

        BackendConnector.connect(ctx.channel().eventLoop(), host, port,
                BACKEND_CONNECT_TIMEOUT_MS,
                backendChannel -> {
                    logger.info("Backend '{}' reached", serverName);
                    sendHandshake(ctx, config, backendChannel);
                    sendLoginStart(ctx, backendChannel, username, uuid, key);

                    backendChannel.pipeline().addLast("backend-login",
                            new BackendLoginHandler(ctx.channel(), config, logger,
                                    ip, uuid, username, secret, properties, key,
                                    eventBus, velocityProxyServer));
                },
                (h, p, cause) -> {
                    String reason = cause.getMessage() != null
                            ? cause.getMessage() : cause.getClass().getSimpleName();
                    logger.warn("Backend '{}' unavailable: {}", serverName, reason);
                    disconnect(ctx, Messages.get("backend-unavailable",
                            Map.of("server", serverName, "host", h,
                                   "port", String.valueOf(p), "reason", reason)));
                });
    }

    private static void sendHandshake(ChannelHandlerContext ctx, ProxyConfig config,
                                      Channel backend) {
        Integer protocol = ctx.channel().attr(HandshakeHandler.PROTOCOL_VERSION).get();
        String serverAddress = ctx.channel().attr(HandshakeHandler.SERVER_ADDRESS).get();
        if (protocol == null) protocol = 0;
        if (serverAddress == null) serverAddress = "minecraft";

        ByteBuf out = ctx.alloc().buffer();
        VarInts.writeVarInt(out, 0x00);
        VarInts.writeVarInt(out, protocol);
        VarInts.writeString(out, serverAddress);
        out.writeShort(config.bind.port);
        VarInts.writeVarInt(out, 2);
        backend.writeAndFlush(out);
    }

    /**
     * Login Start с UUID и ключом профиля — как клиент отправил.
     * Paper сам построит Login Success с корректным hasKey.
     */
    private static void sendLoginStart(ChannelHandlerContext ctx, Channel backend,
                                       String username, UUID uuid,
                                       PlayerProfile.ProfileKey key) {
        ByteBuf out = ctx.alloc().buffer();
        VarInts.writeVarInt(out, 0x00);
        VarInts.writeString(out, username);
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
        if (key != null) {
            out.writeLong(key.expire());
            VarInts.writeVarInt(out, key.keyBytes().length);
            out.writeBytes(key.keyBytes());
            VarInts.writeVarInt(out, key.signature().length);
            out.writeBytes(key.signature());
        }
        backend.writeAndFlush(out);
    }

    private static String extractIp(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress isa) {
            return isa.getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }

    private static String readSecret(ProxyConfig config) {
        try {
            Path p = Path.of(config.forwarding.secretFile);
            if (Files.exists(p)) return Files.readString(p).trim();
        } catch (Exception ignored) {}
        return "";
    }

    private static void disconnect(ChannelHandlerContext ctx, String message) {
        try {
            String json = "{\"text\":\"" + message.replace("\"", "\\\"")
                    .replace("\n", "\\n") + "\"}";
            ByteBuf out = ctx.alloc().buffer();
            VarInts.writeVarInt(out, 0x00);
            VarInts.writeString(out, json);
            ctx.writeAndFlush(out).addListener(f -> ctx.close());
        } catch (Exception e) { ctx.close(); }
    }
}