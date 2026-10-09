package com.minealphaproxy.api.command;

import com.minealphaproxy.api.MineAlphaPlayer;

import java.util.Optional;

public interface CommandSource {
    void sendMessage(String message);
    boolean hasPermission(String permission);
    String getName();

    default Optional<MineAlphaPlayer> asPlayer() {
        return Optional.empty();
    }

    boolean isPlayer();
}