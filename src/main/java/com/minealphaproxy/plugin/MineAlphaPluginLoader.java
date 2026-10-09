package com.minealphaproxy.plugin;

import com.minealphaproxy.api.MineAlphaPlugin;
import com.minealphaproxy.api.annotation.MineAlphaPluginInfo;
import com.minealphaproxy.api.impl.MineAlphaLoggerImpl;
import com.minealphaproxy.api.impl.MineAlphaProxyImpl;
import com.minealphaproxy.util.Logger;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class MineAlphaPluginLoader {

    private final Path pluginsDir;
    private final Logger logger;
    private final MineAlphaProxyImpl proxyApi;

    private final Map<String, MineAlphaPlugin> plugins = new LinkedHashMap<>();
    private URLClassLoader sharedLoader;

    public MineAlphaPluginLoader(Path pluginsDir, Logger logger,
                                 MineAlphaProxyImpl proxyApi) {
        this.pluginsDir = pluginsDir;
        this.logger = logger;
        this.proxyApi = proxyApi;
    }

    public void loadPlugins() {
        try {
            Files.createDirectories(pluginsDir);

            List<Path> jars = new ArrayList<>();
            try (DirectoryStream<Path> stream =
                         Files.newDirectoryStream(pluginsDir, "*.jar")) {
                for (Path jar : stream) jars.add(jar);
            }

            if (jars.isEmpty()) {
                logger.info("No MineAlpha plugins found in {}", pluginsDir);
                return;
            }

            URL[] urls = new URL[jars.size()];
            for (int i = 0; i < jars.size(); i++) {
                urls[i] = jars.get(i).toUri().toURL();
            }
            sharedLoader = new URLClassLoader(urls, getClass().getClassLoader());

            for (Path jar : jars) {
                try { loadPlugin(jar, sharedLoader); }
                catch (Throwable t) {
                    logger.error("Failed to load MineAlpha plugin {}: {}",
                            jar.getFileName(),
                            t.getClass().getSimpleName() + ": " + t.getMessage());
                }
            }

            for (MineAlphaPlugin p : plugins.values()) {
                try { p.onEnable(); }
                catch (Throwable t) {
                    logger.error("Plugin {} onEnable failed: {}",
                            p.getId(), t.getMessage());
                }
            }

            logger.info("Loaded {} MineAlpha plugin(s)", plugins.size());

        } catch (Exception e) {
            logger.error("Failed to scan MineAlphaPlugins/", e);
        }
    }

    private void loadPlugin(Path jar, URLClassLoader loader) throws Exception {
        String mainClass = null;
        String id = null, name = null, version = "1.0";

        try (JarFile jf = new JarFile(jar.toFile())) {
            JarEntry entry = jf.getJarEntry("minealpha-plugin.yml");
            if (entry == null) {
                logger.warn("{} is not a MineAlpha plugin (no minealpha-plugin.yml)",
                        jar.getFileName());
                return;
            }

            Object parsed;
            try (InputStream in = jf.getInputStream(entry)) {
                parsed = new org.yaml.snakeyaml.Yaml().load(in);
            }

            if (!(parsed instanceof Map<?, ?> rawMap)) {
                logger.error("Plugin {} has invalid minealpha-plugin.yml (not a map)",
                        jar.getFileName());
                return;
            }

            Object idObj = rawMap.get("id");
            Object nameObj = rawMap.get("name");
            Object versionObj = rawMap.get("version");
            Object mainObj = rawMap.get("main");

            id = idObj != null ? String.valueOf(idObj) : null;
            name = nameObj != null ? String.valueOf(nameObj) : id;
            version = versionObj != null ? String.valueOf(versionObj) : "1.0";
            mainClass = mainObj != null ? String.valueOf(mainObj) : null;
        }

        if (mainClass == null || mainClass.isBlank()) {
            logger.warn("Plugin {} has no main class", jar.getFileName());
            return;
        }

        Class<?> clazz;
        try {
            clazz = Class.forName(mainClass, true, loader);
        } catch (ClassNotFoundException e) {
            logger.error("Plugin {} main class not found: {}",
                    jar.getFileName(), mainClass);
            return;
        }

        MineAlphaPluginInfo ann = clazz.getAnnotation(MineAlphaPluginInfo.class);
        if (ann != null) {
            if (id == null || id.isEmpty()) id = ann.id();
            if (name == null || name.isEmpty()) {
                name = ann.name().isEmpty() ? id : ann.name();
            }
            if (ann.version() != null && !ann.version().isEmpty()) {
                version = ann.version();
            }
        }

        if (id == null || id.isEmpty()) {
            logger.error("Plugin {} has no id", jar.getFileName());
            return;
        }

        Object instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            logger.error("Plugin {} failed to instantiate: {}", id,
                    t.getClass().getSimpleName() + ": " + t.getMessage());
            return;
        }

        if (!(instance instanceof MineAlphaPlugin plugin)) {
            logger.error("Class {} does not extend MineAlphaPlugin", mainClass);
            return;
        }

        Path dataFolder = pluginsDir.resolve(id);
        Files.createDirectories(dataFolder);

        plugin.setProxy(proxyApi);
        plugin.setLogger(new MineAlphaLoggerImpl(id));
        plugin.setDataFolder(dataFolder.toFile());
        plugin.setId(id);
        plugin.setName(name);
        plugin.setVersion(version);
        plugin.setPluginClassLoader(loader);

        plugins.put(id, plugin);

        proxyApi.eventBusImpl().register(plugin);

        try { plugin.onLoad(); }
        catch (Throwable t) {
            logger.error("Plugin {} onLoad failed: {}", id, t.getMessage());
        }

        logger.info("Loaded MineAlpha plugin: {} v{}", id, version);
    }

    public Collection<MineAlphaPlugin> getPlugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    public void disableAll() {
        for (MineAlphaPlugin p : plugins.values()) {
            try { p.onDisable(); }
            catch (Throwable ignored) {}
            proxyApi.eventBusImpl().unregister(p);
        }
        if (sharedLoader != null) {
            try { sharedLoader.close(); } catch (Exception ignored) {}
        }
        plugins.clear();
    }
}