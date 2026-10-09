package com.minealphaproxy.network;

import com.minealphaproxy.api.impl.MineAlphaApiHolder;
import com.minealphaproxy.api.impl.PlayerSource;
import com.minealphaproxy.compat.velocity.PlayerCommandSource;
import com.minealphaproxy.compat.velocity.VelocityCommandManager;
import com.minealphaproxy.util.Logger;
import com.velocitypowered.api.proxy.Player;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public final class PacketRelayHandler extends ChannelInboundHandlerAdapter {

    private static final int SERVERBOUND_CHAT_COMMAND = 0x04;
    private static final int SERVERBOUND_CHAT_MESSAGE = 0x05;

    private final Channel target;
    private final boolean interceptCommands;
    private final VelocityCommandManager commandManager;
    private final Player player;
    private final Logger logger;

    public PacketRelayHandler(Channel target, boolean interceptCommands,
                              VelocityCommandManager commandManager,
                              Player player, Logger logger) {
        this.target = target;
        this.interceptCommands = interceptCommands;
        this.commandManager = commandManager;
        this.player = player;
        this.logger = logger;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ByteBuf buf)) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (interceptCommands && player != null) {
            buf.markReaderIndex();
            try {
                int packetId = VarInts.readVarInt(buf);

                if (packetId == SERVERBOUND_CHAT_COMMAND) {
                    String command = VarInts.readString(buf);
                    buf.resetReaderIndex();

                    // 1. MineAlpha-команды (alert, mlist, ...)
                    if (MineAlphaApiHolder.isAvailable()) {
                        var api = MineAlphaApiHolder.get();
                        var source = new PlayerSource(api.getPlayer(player.getUsername()).orElse(null) != null
                                ? api.getPlayer(player.getUsername()).get()
                                : null);
                        if (source.asPlayer().isPresent() &&
                                api.getCommandManager().dispatch(source, command)) {
                            buf.release();
                            return;
                        }
                    }

                    // 2. Velocity-команды (/viaver, ...)
                    if (commandManager != null) {
                        var vSource = new PlayerCommandSource(player);
                        if (commandManager.execute(vSource, command)) {
                            buf.release();
                            return;
                        }
                    }

                    // 3. Не наша — пропускаем на бэкенд
                    if (target.isActive()) target.writeAndFlush(buf);
                    else buf.release();
                    return;
                }

                buf.resetReaderIndex();
            } catch (Exception e) {
                buf.resetReaderIndex();
            }
        }

        if (target.isActive()) target.writeAndFlush(buf);
        else { buf.release(); ctx.close(); }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (target.isActive()) target.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}