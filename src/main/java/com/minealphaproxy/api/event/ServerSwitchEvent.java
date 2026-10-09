package com.minealphaproxy.api.event;

import com.minealphaproxy.api.MineAlphaPlayer;

public final class ServerSwitchEvent extends MineAlphaEvent {
    private final MineAlphaPlayer player;
    private final String fromServer;
    private final String toServer;

    public ServerSwitchEvent(MineAlphaPlayer player,
                             String fromServer, String toServer) {
        this.player = player;
        this.fromServer = fromServer;
        this.toServer = toServer;
    }

    public MineAlphaPlayer getPlayer() { return player; }
    public String getFromServer() { return fromServer; }
    public String getToServer() { return toServer; }
}