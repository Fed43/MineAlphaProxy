package com.minealphaproxy.plugin;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class PluginDescription {

    public final String name;
    public final String main;
    public final String version;
    public final String author;
    public final List<String> depends;
    public final List<String> softDepends;

    public PluginDescription(String name, String main, String version,
                             String author, List<String> depends,
                             List<String> softDepends) {
        this.name = name;
        this.main = main;
        this.version = version;
        this.author = author;
        this.depends = depends;
        this.softDepends = softDepends;
    }

    @SuppressWarnings("unchecked")
    public static PluginDescription fromJar(Path jar) throws Exception {
        try (JarFile jf = new JarFile(jar.toFile())) {
            JarEntry entry = jf.getJarEntry("bungee.yml");
            if (entry == null) entry = jf.getJarEntry("plugin.yml");
            if (entry == null) {
                throw new IllegalArgumentException(
                        "JAR не содержит bungee.yml/plugin.yml: " + jar);
            }
            try (InputStream in = jf.getInputStream(entry)) {
                Map<String, Object> map = new Yaml().load(in);
                return new PluginDescription(
                        String.valueOf(map.get("name")),
                        String.valueOf(map.get("main")),
                        String.valueOf(map.getOrDefault("version", "1.0")),
                        String.valueOf(map.getOrDefault("author", "unknown")),
                        (List<String>) map.getOrDefault("depends", List.of()),
                        (List<String>) map.getOrDefault("softdepends", List.of())
                );
            }
        }
    }
}