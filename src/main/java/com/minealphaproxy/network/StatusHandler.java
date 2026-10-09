package com.minealphaproxy.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minealphaproxy.BuildInfo;
import com.minealphaproxy.config.ProxyConfig;
import com.minealphaproxy.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatusHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final ProxyConfig config;
    private final Logger logger;

    public StatusHandler(ProxyConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        int packetId = VarInts.readVarInt(buf);

        if (packetId == 0x00) {
            sendStatusResponse(ctx);
        } else if (packetId == 0x01) {
            long payload = buf.readLong();
            ByteBuf out = ctx.alloc().buffer();
            VarInts.writeVarInt(out, 0x01);
            out.writeLong(payload);
            ctx.writeAndFlush(out).addListener(f -> ctx.close());
        } else {
            ctx.close();
        }
    }

    private void sendStatusResponse(ChannelHandlerContext ctx) throws Exception {
        Integer protocol = ctx.channel().attr(HandshakeHandler.PROTOCOL_VERSION).get();
        int proto = (protocol != null) ? protocol : -1;

        Map<String, Object> version = new LinkedHashMap<>();
        version.put("name", config.brand().displayName() + " " + BuildInfo.VERSION);
        version.put("protocol", proto);

        Map<String, Object> players = new LinkedHashMap<>();
        players.put("max", config.bind.maxPlayers);
        players.put("online", 0);
        players.put("sample", List.of());

        Map<String, Object> description = new LinkedHashMap<>();
        description.put("text", config.resolveMotd());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("version", version);
        response.put("players", players);
        response.put("description", description);

        String json = JSON.writeValueAsString(response);

        ByteBuf out = ctx.alloc().buffer();
        VarInts.writeVarInt(out, 0x00);
        VarInts.writeString(out, json);
        ctx.writeAndFlush(out);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.debug("Status handler error: {}", cause.getMessage());
        ctx.close();
    }
}