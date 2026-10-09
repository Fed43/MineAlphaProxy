package com.minealphaproxy.command.commands;

import com.minealphaproxy.BuildInfo;
import com.minealphaproxy.MineAlphaProxy;
import com.minealphaproxy.command.Command;
import com.minealphaproxy.command.CommandSource;
import com.minealphaproxy.config.ProxyConfig;
import com.minealphaproxy.lang.Messages;

import java.util.Map;

public final class ProxyCommand implements Command {

    private final MineAlphaProxy core;

    public ProxyCommand(MineAlphaProxy core) {
        this.core = core;
    }

    @Override
    public String name() { return "proxy"; }

    @Override
    public String description() { return "Show proxy information"; }

    @Override
    public String usage() { return "/proxy"; }

    @Override
    public void execute(CommandSource source, String[] args) {
        ProxyConfig cfg = core.config();

        source.sendMessage(Messages.get("cmd.proxy.info", Map.of(
                "brand", cfg.brand().displayName(),
                "version", BuildInfo.VERSION,
                "host", cfg.bind.host,
                "port", String.valueOf(cfg.bind.port),
                "server", cfg.defaultServer
        )));
    }
}