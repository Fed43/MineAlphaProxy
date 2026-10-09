package com.minealphaproxy.command.commands;

import com.minealphaproxy.command.Command;
import com.minealphaproxy.command.CommandSource;
import com.minealphaproxy.lang.Messages;

public final class StopCommand implements Command {

    @Override
    public String name() { return "stop"; }

    @Override
    public String description() { return "Stop the proxy"; }

    @Override
    public String usage() { return "/stop"; }

    @Override
    public void execute(CommandSource source, String[] args) {
        source.sendMessage(Messages.get("cmd.stop"));
        System.exit(0);
    }
}