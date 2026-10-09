package com.velocitypowered.api.event.connection;

import com.velocitypowered.api.proxy.Player;

public final class LoginEvent {
    private final Player player;
    public LoginEvent(Player player) { this.player = player; }
    public Player getPlayer() { return player; }
}