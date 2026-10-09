package com.minealphaproxy.compat.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public final class PlayerCommandSource implements CommandSource {

    private final Player player;

    public PlayerCommandSource(Player player) {
        this.player = player;
    }

    public Player player() { return player; }

    @Override
    public void sendMessage(Component component) {
        player.sendMessage(component);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }
}