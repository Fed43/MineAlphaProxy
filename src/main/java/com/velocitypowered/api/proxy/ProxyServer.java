package com.velocitypowered.api.proxy;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;
import com.velocitypowered.api.scheduler.Scheduler;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ProxyServer {

    Optional<Player> getPlayer(String name);

    Player getPlayer(UUID uuid);

    Collection<Player> getAllPlayers();

    int getPlayerCount();

    String getVersion();

    InetSocketAddress getBoundAddress();

    void sendMessage(Component message);

    PluginManager getPluginManager();

    EventManager getEventManager();

    CommandManager getCommandManager();

    Scheduler getScheduler();

    ChannelRegistrar getChannelRegistrar();
}