package com.minealphaproxy.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ProxyConfig {

    /**
     * Proxy name. Allowed values: Fed43Proxy, MineAlphaProxy
     */
    public String proxyName = "MineAlphaProxy";

    /**
     * Language code. Files located in lang/<code>.yml (en, ru).
     */
    public String language = "en";

    public Bind bind = new Bind();
    public Forwarding forwarding = new Forwarding();
    public Servers servers = new Servers();
    public String defaultServer = "lobby";
    public Advanced advanced = new Advanced();
    public Query query = new Query();
    public Plugins plugins = new Plugins();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Bind {
        public String host = "0.0.0.0";
        public int port = 25577;
        public String motd = "§a%proxy_name% §7| §eWelcome!";
        public int maxPlayers = 500;
        public boolean onlineMode = true;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Forwarding {
        public String mode = "modern";
        public String secretFile = "forwarding.secret";
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Servers {
        @JsonProperty("list")
        public Map<String, String> list = new LinkedHashMap<>();
        public List<String> tryOrder = new ArrayList<>();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Advanced {
        public int compressionThreshold = 256;
        public int compressionLevel = 6;
        public int readTimeout = 30000;
        public boolean proxyProtocol = false;
        public int loginRatelimit = 3000;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Query {
        public boolean enabled = false;
        public int port = 25577;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Plugins {
        public String velocityDir = "VelocityPlugins";
        public String bungeeDir = "BungeePlugins";
    }

    public ProxyBrand brand() {
        return ProxyBrand.fromString(proxyName);
    }

    public String resolveMotd() {
        return bind.motd.replace("%proxy_name%", proxyName);
    }
}