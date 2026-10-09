package com.velocitypowered.api.command;

import java.util.List;

public interface CommandInvocation {
    CommandSource source();
    String alias();
    List<String> arguments();
}