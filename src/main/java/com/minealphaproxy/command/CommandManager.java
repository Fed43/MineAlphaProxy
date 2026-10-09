package com.minealphaproxy.command;

import com.minealphaproxy.lang.Messages;
import com.minealphaproxy.util.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CommandManager {

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Logger logger;

    public CommandManager(Logger logger) {
        this.logger = logger;
    }

    public void register(Command command) {
        commands.put(command.name().toLowerCase(), command);
    }

    public void dispatch(CommandSource source, String line) {
        if (line.startsWith("/")) line = line.substring(1);

        String[] parts = line.split("\\s+");
        String name = parts[0].toLowerCase();
        String[] args = parts.length > 1
                ? java.util.Arrays.copyOfRange(parts, 1, parts.length)
                : new String[0];

        Command command = commands.get(name);
        if (command == null) {
            source.sendMessage(Messages.get("cmd.unknown",
                    Map.of("name", name)));
            return;
        }

        try {
            command.execute(source, args);
        } catch (Exception e) {
            source.sendMessage("§cCommand error: " + e.getMessage());
            logger.error("Command '{}' failed", name, e);
        }
    }

    public Collection<Command> commands() {
        return commands.values();
    }
}