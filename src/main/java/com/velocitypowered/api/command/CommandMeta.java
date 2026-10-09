package com.velocitypowered.api.command;

public interface CommandMeta {

    String[] getAliases();

    Object plugin();

    interface Builder {
        Builder aliases(String... aliases);
        Builder plugin(Object plugin);
        CommandMeta build();
    }
}