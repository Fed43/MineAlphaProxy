package com.velocitypowered.api.command;

import java.util.List;

public interface SimpleCommand extends Command {

    void execute(Invocation invocation);

    interface Invocation {
        CommandSource source();
        String alias();
        List<String> arguments();
    }
}