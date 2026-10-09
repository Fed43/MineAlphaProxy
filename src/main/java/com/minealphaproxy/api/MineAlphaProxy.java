package com.minealphaproxy.api;

import com.minealphaproxy.api.command.MineAlphaCommandManager;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MineAlphaProxy {

    String getVersion();
    String getBrand();

    Optional<MineAlphaPlayer> getPlayer(String name);
    MineAlphaPlayer getPlayer(UUID uuid);
    Collection<MineAlphaPlayer> getPlayers();
    int getPlayerCount();

    Optional<MineAlphaServer> getServer(String name);
    Collection<MineAlphaServer> getServers();
    String getDefaultServer();

    MineAlphaCommandManager getCommandManager();
    MineAlphaScheduler getScheduler();
    MineAlphaEventBus getEventBus();

    void broadcastMessage(String message);
    void broadcastMessage(net.kyori.adventure.text.Component message);
}