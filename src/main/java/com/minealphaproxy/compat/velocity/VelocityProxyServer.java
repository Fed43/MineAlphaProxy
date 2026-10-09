package com.minealphaproxy.compat.velocity;

import com.minealphaproxy.BuildInfo;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;
import com.velocitypowered.api.scheduler.Scheduler;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class VelocityProxyServer implements ProxyServer {

    private final EventManager eventManager;
    private final CommandManager commandManager;
    private final Scheduler scheduler;
    private final ChannelRegistrar channelRegistrar;
    private final Map<String, PluginContainer> plugins = new ConcurrentHashMap<>();
    private final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private final InetSocketAddress bound;

    public VelocityProxyServer(EventManager eventManager,
                               CommandManager commandManager,
                               Scheduler scheduler,
                               InetSocketAddress bound) {
        this.eventManager = eventManager;
        this.commandManager = commandManager;
        this.scheduler = scheduler;
        this.channelRegistrar = new VelocityChannelRegistrar();
        this.bound = bound;
    }

    public void addPlayer(Player player) {
        players.put(player.getUniqueId(), player);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public void addPlugin(PluginContainer container) {
        plugins.put(container.getId(), container);
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        for (Player p : players.values()) {
            if (p.getUsername().equalsIgnoreCase(name)) return Optional.of(p);
        }
        return Optional.empty();
    }

    @Override
    public Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    @Override
    public Collection<Player> getAllPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    @Override
    public int getPlayerCount() {
        return players.size();
    }

    @Override
    public String getVersion() {
        return "MineAlphaProxy " + BuildInfo.VERSION;
    }

    @Override
    public InetSocketAddress getBoundAddress() {
        return bound;
    }

    @Override
    public void sendMessage(Component message) {
        // Сообщения от консоли — можно залогировать
    }

    @Override
    public PluginManager getPluginManager() {
        return new PluginManager() {
            @Override
            public Optional<PluginContainer> fromInstance(Object instance) {
                for (PluginContainer c : plugins.values()) {
                    if (c.getInstance() == instance) return Optional.of(c);
                }
                return Optional.empty();
            }

            @Override
            public Optional<PluginContainer> getPlugin(String id) {
                return Optional.ofNullable(plugins.get(id));
            }

            @Override
            public Collection<PluginContainer> getPlugins() {
                return Collections.unmodifiableCollection(plugins.values());
            }

            @Override
            public boolean isLoaded(String id) {
                return plugins.containsKey(id);
            }
        };
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public ChannelRegistrar getChannelRegistrar() {
        return channelRegistrar;
    }
}