package com.minealphaproxy.command.commands;

import com.minealphaproxy.MineAlphaProxy;
import com.minealphaproxy.command.Command;
import com.minealphaproxy.command.CommandSource;
import com.minealphaproxy.lang.Messages;

import java.util.Map;

public final class ReloadCommand implements Command {

    private final MineAlphaProxy core;

    public ReloadCommand(MineAlphaProxy core) {
        this.core = core;
    }

    @Override
    public String name() { return "reload"; }

    @Override
    public String description() { return "Reload core.yml and language files"; }

    @Override
    public String usage() { return "/reload"; }

    @Override
    public void execute(CommandSource source, String[] args) {
        try {
            core.reload();
            source.sendMessage(Messages.get("cmd.reload.success"));
        } catch (Exception e) {
            source.sendMessage(Messages.get("cmd.reload.error",
                    Map.of("reason", e.getMessage() == null
                            ? e.getClass().getSimpleName() : e.getMessage())));
        }
    }
}