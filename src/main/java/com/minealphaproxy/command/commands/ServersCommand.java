package com.minealphaproxy.command.commands;

import com.minealphaproxy.MineAlphaProxy;
import com.minealphaproxy.command.Command;
import com.minealphaproxy.command.CommandSource;
import com.minealphaproxy.config.ProxyConfig;
import com.minealphaproxy.lang.Messages;

import java.util.Map;

public final class ServersCommand implements Command {

    private final MineAlphaProxy core;

    public ServersCommand(MineAlphaProxy core) {
        this.core = core;
    }

    @Override
    public String name() { return "servers"; }

    @Override
    public String description() { return "List configured servers"; }

    @Override
    public String usage() { return "/servers"; }

    @Override
    public void execute(CommandSource source, String[] args) {
        ProxyConfig cfg = core.config();

        if (cfg.servers.list.isEmpty()) {
            source.sendMessage(Messages.get("cmd.servers.empty"));
            return;
        }

        source.sendMessage(Messages.get("cmd.servers.header"));
        for (Map.Entry<String, String> e : cfg.servers.list.entrySet()) {
            String tag = e.getKey().equals(cfg.defaultServer)
                    ? Messages.get("cmd.servers.default-tag")
                    : "";
            source.sendMessage(Messages.get("cmd.servers.entry", Map.of(
                    "name", e.getKey(),
                    "address", e.getValue(),
                    "default", tag
            )));
        }
    }
}