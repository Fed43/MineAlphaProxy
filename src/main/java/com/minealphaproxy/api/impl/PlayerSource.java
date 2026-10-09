package com.minealphaproxy.api.impl;

import com.minealphaproxy.api.MineAlphaPlayer;
import com.minealphaproxy.api.command.CommandSource;

import java.util.Optional;

public final class PlayerSource implements CommandSource {

    private final MineAlphaPlayer player;

    public PlayerSource(MineAlphaPlayer player) { this.player = player; }

    public MineAlphaPlayer player() { return player; }

    @Override public void sendMessage(String message) { player.sendMessage(message); }
    @Override public boolean hasPermission(String permission) { return player.hasPermission(permission); }
    @Override public String getName() { return player.getName(); }
    @Override public Optional<MineAlphaPlayer> asPlayer() { return Optional.of(player); }
    @Override public boolean isPlayer() { return true; }
}