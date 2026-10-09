package com.velocitypowered.api.event.player;

import com.velocitypowered.api.proxy.Player;

public final class DisconnectEvent {
    private final Player player;
    public DisconnectEvent(Player player) { this.player = player; }
    public Player getPlayer() { return player; }
}