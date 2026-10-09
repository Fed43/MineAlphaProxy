package com.minealphaproxy.plugin;

import com.minealphaproxy.util.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class BungeePluginManager {

    private final Path pluginsDir;
    private final Logger logger;
    private final ClassLoader apiLoader;
    private final Map<String, URLClassLoader> loaders = new LinkedHashMap<>();
    private final Map<String, Object> plugins = new LinkedHashMap<>();

    public BungeePluginManager(Path pluginsDir, Logger logger,
                               ClassLoader apiLoader) {
        this.pluginsDir = pluginsDir;
        this.logger = logger;
        this.apiLoader = apiLoader;
    }

    public void loadPlugins() {
        try {
            Files.createDirectories(pluginsDir);
            try (DirectoryStream<Path> stream =
                         Files.newDirectoryStream(pluginsDir, "*.jar")) {
                for (Path jar : stream) {
                    try {
                        loadPlugin(jar);
                    } catch (Exception e) {
                        logger.error("Failed to load Bungee plugin {}",
                                jar.getFileName(), e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan BungeePlugins/", e);
        }
    }

    private void loadPlugin(Path jar) throws Exception {
        PluginDescription desc = PluginDescription.fromJar(jar);

        for (String dep : desc.depends) {
            if (!plugins.containsKey(dep)) {
                throw new IllegalStateException(
                        "Missing dependency " + dep + " for " + desc.name);
            }
        }

        URLClassLoader loader = new URLClassLoader(
                new URL[]{jar.toUri().toURL()},
                apiLoader
        );

        Class<?> mainClass = Class.forName(desc.main, true, loader);
        Object plugin = mainClass.getDeclaredConstructor().newInstance();

        Path dataFolder = pluginsDir.resolve(desc.name);
        Files.createDirectories(dataFolder);

        injectDataFolder(plugin, dataFolder);

        plugins.put(desc.name, plugin);
        loaders.put(desc.name, loader);

        invokeLifecycle(plugin, "onLoad");
        invokeLifecycle(plugin, "onEnable");

        logger.info("Loaded Bungee plugin: {} v{}", desc.name, desc.version);
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(plugins.keySet());
    }

    private void injectDataFolder(Object plugin, Path dataFolder) {
        Class<?> clazz = plugin.getClass();
        while (clazz != null && clazz != Object.class) {
            for (var f : clazz.getDeclaredFields()) {
                if (f.getType() == Path.class) {
                    try {
                        f.setAccessible(true);
                        f.set(plugin, dataFolder);
                    } catch (Exception ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private void invokeLifecycle(Object plugin, String method) {
        try {
            plugin.getClass().getMethod(method).invoke(plugin);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            logger.error("Failed to invoke {} on plugin", method, e);
        }
    }

    public void disableAll() {
        for (Object p : plugins.values()) invokeLifecycle(p, "onDisable");
        for (URLClassLoader l : loaders.values()) {
            try { l.close(); } catch (IOException ignored) {}
        }
        plugins.clear();
        loaders.clear();
    }
}