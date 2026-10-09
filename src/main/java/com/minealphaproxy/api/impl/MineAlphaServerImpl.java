package com.minealphaproxy.api.impl;

import com.minealphaproxy.api.MineAlphaPlayer;
import com.minealphaproxy.api.MineAlphaServer;

import java.util.Collection;
import java.util.Collections;

public final class MineAlphaServerImpl implements MineAlphaServer {

    private final String name;
    private final String host;
    private final int port;

    public MineAlphaServerImpl(String name, String address) {
        this.name = name;
        int idx = address.lastIndexOf(':');
        if (idx > 0) {
            this.host = address.substring(0, idx);
            this.port = Integer.parseInt(address.substring(idx + 1));
        } else {
            this.host = address;
            this.port = 25565;
        }
    }

    @Override public String getName() { return name; }
    @Override public String getAddress() { return host; }
    @Override public int getPort() { return port; }
    @Override public int getPlayerCount() { return 0; }
    @Override public Collection<MineAlphaPlayer> getPlayers() { return Collections.emptyList(); }
}