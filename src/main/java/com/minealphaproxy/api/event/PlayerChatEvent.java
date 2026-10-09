package com.minealphaproxy.api.event;

import com.minealphaproxy.api.MineAlphaPlayer;

public final class PlayerChatEvent extends MineAlphaEvent {

    private final MineAlphaPlayer player;
    private String message;

    public PlayerChatEvent(MineAlphaPlayer player, String message) {
        this.player = player;
        this.message = message;
    }

    public MineAlphaPlayer getPlayer() { return player; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override public boolean isCancellable() { return true; }
}