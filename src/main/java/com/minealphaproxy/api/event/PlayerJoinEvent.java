package com.minealphaproxy.api.event;

import com.minealphaproxy.api.MineAlphaPlayer;

public final class PlayerJoinEvent extends MineAlphaEvent {
    private final MineAlphaPlayer player;
    public PlayerJoinEvent(MineAlphaPlayer player) { this.player = player; }
    public MineAlphaPlayer getPlayer() { return player; }
}