package com.minealphaproxy.command;

import com.minealphaproxy.api.impl.ConsoleSource;
import com.minealphaproxy.api.impl.MineAlphaApiHolder;
import com.minealphaproxy.lang.Colors;
import com.minealphaproxy.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ConsoleCommandSource implements CommandSource, Runnable {

    private final CommandManager manager;
    private final Logger logger;
    private final BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

    public ConsoleCommandSource(CommandManager manager, Logger logger) {
        this.manager = manager;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;

                if (MineAlphaApiHolder.isAvailable()) {
                    try {
                        var cm = MineAlphaApiHolder.get().getCommandManager();
                        if (cm.dispatch(ConsoleSource.INSTANCE, line)) {
                            continue;
                        }
                    } catch (Throwable t) {
                        logger.error("MineAlpha command dispatch failed", t);
                    }
                }

                manager.dispatch(this, line);

            } catch (IOException e) {
                break;
            } catch (Exception e) {
                logger.error("Command execution error", e);
            }
        }
    }

    @Override
    public void sendMessage(String message) {
        logger.info(Colors.toAnsi(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public String name() {
        return "CONSOLE";
    }
}