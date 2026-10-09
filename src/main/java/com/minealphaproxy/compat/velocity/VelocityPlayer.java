package com.minealphaproxy.compat.velocity;

import com.velocitypowered.api.proxy.Player;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.minealphaproxy.network.VarInts;

import java.net.InetSocketAddress;
import java.util.UUID;

public final class VelocityPlayer implements Player {

    private static final int SYSTEM_CHAT_PACKET_ID = 0x6C;

    private final String username;
    private final UUID uuid;
    private final InetSocketAddress address;
    private final Channel channel;

    public VelocityPlayer(String username, UUID uuid,
                          InetSocketAddress address, Channel channel) {
        this.username = username;
        this.uuid = uuid;
        this.address = address;
        this.channel = channel;
    }

    @Override public String getUsername() { return username; }
    @Override public UUID getUniqueId() { return uuid; }
    @Override public InetSocketAddress getRemoteAddress() { return address; }
    @Override public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    public void disconnect(Component reason) {
        if (channel == null || !channel.isActive()) return;
        try {
            // Play Disconnect — packet id 0x1D для 1.20.x
            ByteBuf out = channel.alloc().buffer();
            VarInts.writeVarInt(out, 0x1D);
            String json = GsonComponentSerializer.gson().serialize(reason);
            VarInts.writeString(out, json);
            channel.writeAndFlush(out).addListener(f -> channel.close());
        } catch (Exception e) {
            channel.close();
        }
    }

    @Override
    public void sendMessage(Component message) {
        if (channel == null || !channel.isActive()) return;
        try {
            ByteBuf out = channel.alloc().buffer();
            VarInts.writeVarInt(out, SYSTEM_CHAT_PACKET_ID);

            // Для 1.20.3+ используется NBT-компонент.
            // Пока отправляем как String — работает для 1.20.2 и ниже.
            String json = GsonComponentSerializer.gson().serialize(message);
            VarInts.writeString(out, json);

            // overlay = false (это чат, не action bar)
            out.writeBoolean(false);

            channel.writeAndFlush(out);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void sendActionBar(Component message) {
        sendMessage(message); // TODO: real overlay=true
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    public Channel channel() { return channel; }
}