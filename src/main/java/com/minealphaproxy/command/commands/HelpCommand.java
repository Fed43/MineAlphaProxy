package com.minealphaproxy.command.commands;

import com.minealphaproxy.command.Command;
import com.minealphaproxy.command.CommandManager;
import com.minealphaproxy.command.CommandSource;
import com.minealphaproxy.lang.Messages;

import java.util.Map;

public final class HelpCommand implements Command {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public String name() { return "help"; }

    @Override
    public String description() { return "Show all available commands"; }

    @Override
    public String usage() { return "/help"; }

    @Override
    public void execute(CommandSource source, String[] args) {
        source.sendMessage(Messages.get("cmd.help.header"));
        for (Command cmd : manager.commands()) {
            source.sendMessage(Messages.get("cmd.help.line", Map.of(
                    "name", cmd.name(),
                    "usage", cmd.usage(),
                    "description", cmd.description()
            )));
        }
    }
}