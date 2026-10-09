package com.velocitypowered.api.command;

import net.kyori.adventure.text.Component;

public interface CommandSource {
    void sendMessage(Component component);
    boolean hasPermission(String permission);
}