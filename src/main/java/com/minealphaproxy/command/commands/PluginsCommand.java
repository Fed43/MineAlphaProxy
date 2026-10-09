package com.minealphaproxy.command.commands;

import com.minealphaproxy.MineAlphaProxy;
import com.minealphaproxy.command.Command;
import com.minealphaproxy.command.CommandSource;
import com.minealphaproxy.lang.Messages;

import java.util.Map;
import java.util.Set;

public final class PluginsCommand implements Command {

    private final MineAlphaProxy core;

    public PluginsCommand(MineAlphaProxy core) {
        this.core = core;
    }

    @Override
    public String name() { return "plugins"; }

    @Override
    public String description() { return "List loaded plugins"; }

    @Override
    public String usage() { return "/plugins"; }

    @Override
    public void execute(CommandSource source, String[] args) {
        Set<String> velocity = core.velocityPluginManager().names();

        if (velocity.isEmpty()) {
            source.sendMessage(Messages.get("cmd.plugins.none"));
            return;
        }

        source.sendMessage(Messages.get("cmd.plugins.header"));

        source.sendMessage(Messages.get("cmd.plugins.velocity",
                Map.of("count", String.valueOf(velocity.size()))));
        for (String name : velocity) {
            source.sendMessage("  §7- §f" + name);
        }
    }
}