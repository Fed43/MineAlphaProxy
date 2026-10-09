package com.minealphaproxy.api.impl;

import com.minealphaproxy.api.MineAlphaPlayer;
import com.minealphaproxy.api.command.CommandSource;
import com.minealphaproxy.lang.Colors;
import com.minealphaproxy.util.Logger;

import java.util.Optional;

public final class ConsoleSource implements CommandSource {

    public static final ConsoleSource INSTANCE = new ConsoleSource();

    private static final Logger LOG = new Logger("CONSOLE");

    private ConsoleSource() {}

    @Override
    public void sendMessage(String message) {
        LOG.info(Colors.toAnsi(message));
    }

    @Override public boolean hasPermission(String permission) { return true; }
    @Override public String getName() { return "CONSOLE"; }
    @Override public Optional<MineAlphaPlayer> asPlayer() { return Optional.empty(); }
    @Override public boolean isPlayer() { return false; }
}