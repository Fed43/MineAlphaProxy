package com.minealphaproxy.command;

public interface Command {
    String name();
    String description();
    String usage();
    void execute(CommandSource source, String[] args);
}