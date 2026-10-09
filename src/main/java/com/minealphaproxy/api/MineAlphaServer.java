package com.minealphaproxy.api;

import java.util.Collection;

public interface MineAlphaServer {
    String getName();
    String getAddress();
    int getPort();
    int getPlayerCount();
    Collection<MineAlphaPlayer> getPlayers();
}