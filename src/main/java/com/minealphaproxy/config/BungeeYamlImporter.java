package com.minealphaproxy.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

public final class BungeeYamlImporter {

    public ProxyConfig importFrom(Path bungeeConfig) throws IOException {
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        JsonNode root = yaml.readTree(bungeeConfig.toFile());

        ProxyConfig out = new ProxyConfig();
        out.proxyName = "MineAlphaProxy";

        JsonNode listener = root.path("listeners").path(0);
        String host = listener.path("host").asText("0.0.0.0:25577");
        int idx = host.lastIndexOf(':');
        out.bind.host = host.substring(0, idx);
        out.bind.port = Integer.parseInt(host.substring(idx + 1));
        out.bind.motd = listener.path("motd").asText(out.bind.motd);
        out.bind.maxPlayers = listener.path("max_players").asInt(500);
        out.bind.onlineMode = root.path("online_mode").asBoolean(true);

        JsonNode servers = root.path("servers");
        if (servers.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = servers.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                out.servers.list.put(e.getKey(), e.getValue().asText());
            }
        }

        out.defaultServer = root.path("default_server").asText("lobby");
        out.forwarding.mode = "legacy";
        out.forwarding.secretFile = "forwarding.secret";

        return out;
    }
}