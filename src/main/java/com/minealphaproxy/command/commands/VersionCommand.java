package com.minealphaproxy.command.commands;

import com.minealphaproxy.BuildInfo;
import com.minealphaproxy.command.Command;
import com.minealphaproxy.command.CommandSource;
import com.minealphaproxy.lang.Messages;

import java.util.Map;

public final class VersionCommand implements Command {

    @Override
    public String name() { return "version"; }

    @Override
    public String description() { return "Show proxy version"; }

    @Override
    public String usage() { return "/version"; }

    @Override
    public void execute(CommandSource source, String[] args) {
        source.sendMessage(Messages.get("cmd.version.info",
                Map.of("version", BuildInfo.VERSION)));
    }
}