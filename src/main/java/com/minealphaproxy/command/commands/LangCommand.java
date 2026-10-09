package com.minealphaproxy.command.commands;

import com.minealphaproxy.MineAlphaProxy;
import com.minealphaproxy.command.Command;
import com.minealphaproxy.command.CommandSource;
import com.minealphaproxy.lang.Messages;

import java.util.Map;

public final class LangCommand implements Command {

    private final MineAlphaProxy core;

    public LangCommand(MineAlphaProxy core) {
        this.core = core;
    }

    @Override
    public String name() { return "lang"; }

    @Override
    public String description() { return "Change the language"; }

    @Override
    public String usage() { return "/lang <en|ru|...>"; }

    @Override
    public void execute(CommandSource source, String[] args) {
        if (args.length < 1) {
            source.sendMessage(Messages.get("cmd.lang.usage"));
            return;
        }

        String code = args[0].toLowerCase();
        try {
            core.switchLanguage(code);
            source.sendMessage(Messages.get("cmd.lang.switched",
                    Map.of("code", code)));
        } catch (Exception e) {
            source.sendMessage(Messages.get("cmd.lang.error", Map.of(
                    "code", code,
                    "reason", e.getMessage() == null
                            ? e.getClass().getSimpleName() : e.getMessage()
            )));
        }
    }
}