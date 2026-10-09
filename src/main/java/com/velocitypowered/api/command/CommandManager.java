package com.velocitypowered.api.command;

import java.util.Optional;

public interface CommandManager {

    CommandMeta.Builder metaBuilder(String alias);

    CommandMeta.Builder metaBuilder(String alias, String... otherAliases);

    void register(String alias, Command command, String... otherAliases);

    void register(Command command, String... aliases);

    void register(CommandMeta meta, Command command);

    void unregister(String alias);

    boolean hasCommand(String alias);

    Optional<CommandMeta> getCommandMeta(String alias);

    Optional<Command> getCommand(String alias);
}