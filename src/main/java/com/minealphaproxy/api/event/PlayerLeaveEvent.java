package com.minealphaproxy.api.event;

import com.minealphaproxy.api.MineAlphaPlayer;

public final class PlayerLeaveEvent extends MineAlphaEvent {
    private final MineAlphaPlayer player;
    public PlayerLeaveEvent(MineAlphaPlayer player) { this.player = player; }
    public MineAlphaPlayer getPlayer() { return player; }
}