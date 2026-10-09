package com.minealphaproxy.api.command;

import java.util.Collection;

public interface MineAlphaCommandManager {
    void register(MineAlphaCommand command);
    void unregister(String name);
    boolean dispatch(CommandSource sender, String commandLine);
    Collection<MineAlphaCommand> getCommands();
}