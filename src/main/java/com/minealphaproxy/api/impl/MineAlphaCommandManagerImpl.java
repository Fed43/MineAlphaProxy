package com.minealphaproxy.api.impl;

import com.minealphaproxy.api.command.CommandSource;
import com.minealphaproxy.api.command.MineAlphaCommand;
import com.minealphaproxy.api.command.MineAlphaCommandManager;
import com.minealphaproxy.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MineAlphaCommandManagerImpl implements MineAlphaCommandManager {

    private final Logger logger;
    private final Map<String, MineAlphaCommand> commands = new ConcurrentHashMap<>();

    public MineAlphaCommandManagerImpl(Logger logger) { this.logger = logger; }

    @Override
    public void register(MineAlphaCommand command) {
        commands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
        logger.info("Registered command: /{}", command.getName());
    }

    @Override
    public void unregister(String name) {
        MineAlphaCommand cmd = commands.get(name.toLowerCase());
        if (cmd == null) return;
        commands.values().removeIf(c -> c == cmd);
        logger.info("Unregistered command: /{}", name);
    }

    @Override
    public boolean dispatch(CommandSource sender, String commandLine) {
        String line = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;
        String[] parts = line.split("\\s+");
        if (parts.length == 0) return false;
        MineAlphaCommand cmd = commands.get(parts[0].toLowerCase());
        if (cmd == null) return false;
        if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
            sender.sendMessage("§cНедостаточно прав.");
            return true;
        }
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
        try { cmd.execute(sender, args); }
        catch (Exception e) {
            logger.error("Command /{} failed", cmd.getName(), e);
            sender.sendMessage("§cОшибка выполнения команды.");
        }
        return true;
    }

    @Override
    public Collection<MineAlphaCommand> getCommands() {
        return Collections.unmodifiableCollection(new HashSet<>(commands.values()));
    }
}