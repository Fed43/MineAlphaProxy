package com.velocitypowered.api.event.player;

import com.velocitypowered.api.proxy.Player;

public final class PostLoginEvent {
    private final Player player;
    public PostLoginEvent(Player player) { this.player = player; }
    public Player getPlayer() { return player; }
}