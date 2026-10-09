package com.velocitypowered.api.proxy;

import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.UUID;

public interface Player {
    String getUsername();
    UUID getUniqueId();
    InetSocketAddress getRemoteAddress();
    boolean isActive();
    void disconnect(Component reason);
    void sendMessage(Component message);
    void sendActionBar(Component message);
    boolean hasPermission(String permission);
}