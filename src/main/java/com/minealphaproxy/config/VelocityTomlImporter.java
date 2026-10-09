package com.minealphaproxy.config;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class VelocityTomlImporter {

    public ProxyConfig importFrom(Path velocityToml) throws IOException {
        TomlParseResult toml = Toml.parse(velocityToml);
        if (toml.hasErrors()) {
            throw new IOException("velocity.toml содержит ошибки: " + toml.errors());
        }

        ProxyConfig out = new ProxyConfig();
        out.proxyName = "MineAlphaProxy";

        // bind = "host:port"
        String bind = toml.getString("bind");
        if (bind != null) {
            int idx = bind.lastIndexOf(':');
            if (idx > 0) {
                out.bind.host = bind.substring(0, idx);
                out.bind.port = Integer.parseInt(bind.substring(idx + 1));
            }
        }

        // motd
        String motd = toml.getString("motd");
        if (motd != null) out.bind.motd = motd;

        // show-max-players
        Long maxPlayers = toml.getLong("show-max-players");
        if (maxPlayers != null) out.bind.maxPlayers = maxPlayers.intValue();

        // online-mode
        Boolean onlineMode = toml.getBoolean("online-mode");
        if (onlineMode != null) out.bind.onlineMode = onlineMode;

        // servers
        TomlTable servers = toml.getTable("servers");
        if (servers != null) {
            for (String key : servers.keySet()) {
                if (key.equals("try")) continue;
                String value = servers.getString(key);
                if (value != null) out.servers.list.put(key, value);
            }
            if (servers.contains("try")) {
                List<Object> tryList = servers.getArray("try").toList();
                for (Object o : tryList) out.servers.tryOrder.add(o.toString());
            }
        }

        // forwarding
        String fwd = toml.getString("player-info-forwarding-mode");
        if (fwd == null) fwd = toml.getString("forwarding-mode");
        if (fwd == null) fwd = "none";

        out.forwarding.mode = switch (fwd.toLowerCase()) {
            case "modern", "velocity" -> "modern";
            case "legacy", "bungeecord" -> "legacy";
            default -> "none";
        };

        String secretFile = toml.getString("forwarding-secret-file");
        if (secretFile != null) out.forwarding.secretFile = secretFile;

        // advanced
        TomlTable adv = toml.getTable("advanced");
        if (adv != null) {
            Long ct = adv.getLong("compression-threshold");
            if (ct != null) out.advanced.compressionThreshold = ct.intValue();

            Long cl = adv.getLong("compression-level");
            if (cl != null) out.advanced.compressionLevel = cl.intValue();

            Long to = adv.getLong("connection-timeout");
            if (to != null) out.advanced.readTimeout = to.intValue();

            Long lr = adv.getLong("login-ratelimit");
            if (lr != null) out.advanced.loginRatelimit = lr.intValue();
        }

        // query
        TomlTable q = toml.getTable("query");
        if (q != null) {
            Boolean enabled = q.getBoolean("enabled");
            if (enabled != null) out.query.enabled = enabled;

            Long qPort = q.getLong("port");
            if (qPort != null) out.query.port = qPort.intValue();
        }

        out.plugins.velocityDir = "VelocityPlugins";
        out.plugins.bungeeDir = "BungeePlugins";

        out.defaultServer = out.servers.tryOrder.isEmpty()
                ? "lobby" : out.servers.tryOrder.get(0);

        return out;
    }
}