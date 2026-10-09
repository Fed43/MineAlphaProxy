package com.minealphaproxy.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class ConfigValidator {

    private static final Set<String> VALID_PROXY_NAMES =
            Set.of("Fed43Proxy", "MineAlphaProxy");

    public static void validate(ProxyConfig cfg) {
        if (cfg.proxyName == null || cfg.proxyName.isBlank()) {
            throw new IllegalStateException("Field 'proxy-name' is not set in core.yml");
        }
        if (!VALID_PROXY_NAMES.contains(cfg.proxyName)) {
            throw new IllegalStateException(
                    "Invalid proxy-name: '" + cfg.proxyName +
                    "'. Allowed values: " + VALID_PROXY_NAMES);
        }

        if (cfg.bind.port < 1 || cfg.bind.port > 65535) {
            throw new IllegalStateException("Invalid bind.port: " + cfg.bind.port);
        }
        if (cfg.servers.list.isEmpty()) {
            throw new IllegalStateException("Section servers.list is empty");
        }
        if (!cfg.servers.list.containsKey(cfg.defaultServer)) {
            throw new IllegalStateException(
                    "default-server '" + cfg.defaultServer +
                    "' is missing in servers.list");
        }
        if ("modern".equalsIgnoreCase(cfg.forwarding.mode)) {
            Path secret = Path.of(cfg.forwarding.secretFile);
            if (!Files.exists(secret)) {
                throw new IllegalStateException(
                        "forwarding.mode=modern, but file " +
                        cfg.forwarding.secretFile + " not found");
            }
        }
    }

    private ConfigValidator() {}
}