package com.minealphaproxy.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {

    private final Path path;
    private final ObjectMapper yaml;

    public ConfigLoader(Path path) {
        this.path = path;
        this.yaml = new ObjectMapper(new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
                .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ProxyConfig loadOrCreate() throws IOException {
        if (!Files.exists(path)) {
            ProxyConfig def = defaultFromResource();
            save(def);
            return def;
        }
        return load();
    }

    public ProxyConfig load() throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return yaml.readValue(in, ProxyConfig.class);
        }
    }

    public void save(ProxyConfig cfg) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);

        try (OutputStream out = Files.newOutputStream(path)) {
            yaml.writerWithDefaultPrettyPrinter().writeValue(out, cfg);
        }
        prependHeaderIfMissing();
    }

    public ProxyConfig defaultFromResource() throws IOException {
        try (InputStream in = ConfigLoader.class.getResourceAsStream("/core.default.yml")) {
            if (in != null) return yaml.readValue(in, ProxyConfig.class);
        }
        return new ProxyConfig();
    }

    private void prependHeaderIfMissing() throws IOException {
        String header = """
                # ============================================
                #  MineAlphaProxy - main configuration
                #  Format: YAML
                # ============================================
                """;
        String body = Files.readString(path, StandardCharsets.UTF_8);
        if (!body.startsWith("#")) {
            Files.writeString(path, header + body, StandardCharsets.UTF_8);
        }
    }
}