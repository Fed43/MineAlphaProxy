package com.minealphaproxy.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minealphaproxy.compat.velocity.PluginContainerImpl;
import com.minealphaproxy.compat.velocity.VelocityEventManager;
import com.minealphaproxy.compat.velocity.VelocityProxyServer;
import com.minealphaproxy.compat.velocity.ViaIntegration;
import com.minealphaproxy.compat.velocity.slf4j.Slf4jProxyLogger;
import com.minealphaproxy.event.EventBus;
import com.minealphaproxy.util.Logger;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class VelocityPluginManager {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final Path pluginsDir;
    private final Logger logger;
    private final EventBus eventBus;
    private final VelocityEventManager eventManager;
    private final VelocityProxyServer proxyServer;

    private URLClassLoader sharedLoader;
    private final Map<String, Object> instances = new LinkedHashMap<>();

    public VelocityPluginManager(Path pluginsDir, Logger logger,
                                 EventBus eventBus,
                                 VelocityProxyServer proxyServer) {
        this.pluginsDir = pluginsDir;
        this.logger = logger;
        this.eventBus = eventBus;
        this.proxyServer = proxyServer;
        this.eventManager = new VelocityEventManager(eventBus);
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
                logger.info("No Velocity plugins found");
                return;
            }

            List<PluginEntry> entries = new ArrayList<>();
            for (Path jar : jars) {
                PluginEntry e = readDescriptor(jar);
                if (e != null) entries.add(e);
            }
            entries.sort(Comparator.comparingInt(PluginEntry::priority));

            URL[] urls = new URL[jars.size()];
            for (int i = 0; i < jars.size(); i++) {
                urls[i] = jars.get(i).toUri().toURL();
            }
            sharedLoader = new URLClassLoader(urls, getClass().getClassLoader());

            List<Object> loaded = new ArrayList<>();
            for (PluginEntry entry : entries) {
                try {
                    Object instance = loadPluginInstance(entry, sharedLoader);
                    if (instance != null) {
                        loaded.add(instance);
                        logger.info("Loaded Velocity plugin: {} v{}",
                                entry.id, entry.version);
                    }
                } catch (Throwable t) {
                    logger.error("Failed to load Velocity plugin {}: {}",
                            entry.jar.getFileName(),
                            t.getClass().getSimpleName() + ": " + t.getMessage());
                }
            }

            // === Р ВР Р…Р С‘РЎвЂ Р С‘Р В°Р В»Р С‘Р В·Р В°РЎвЂ Р С‘РЎРЏ Via Р С—Р ВµРЎР‚Р ВµР Т‘ РЎРѓР С•Р В±РЎвЂ№РЎвЂљР С‘Р ВµР С ===
            if (!loaded.isEmpty()) {
                eventBus.fireSync(new ProxyInitializeEvent());
                logger.info("Fired ProxyInitializeEvent to {} plugin(s)", loaded.size());
            }

        } catch (Exception e) {
            logger.error("Failed to scan VelocityPlugins/", e);
        }
    }

    private int priorityFor(String id, List<String> depends) {
        if (id == null) return 100;
        if (id.equalsIgnoreCase("viaversion")) return 0;
        if (depends.stream().anyMatch(d -> d.equalsIgnoreCase("viaversion"))) return 50;
        return 100;
    }

    private PluginEntry readDescriptor(Path jar) {
        try (JarFile jf = new JarFile(jar.toFile())) {
            JarEntry entry = jf.getJarEntry("velocity-plugin.json");
            if (entry == null) return null;
            JsonNode desc;
            try (InputStream in = jf.getInputStream(entry)) {
                desc = JSON.readTree(in);
            }
            String id = desc.path("id").asText(null);
            String name = desc.path("name").asText(id);
            String version = desc.path("version").asText("1.0");
            String main = desc.path("main").asText(null);

            List<String> depends = new ArrayList<>();
            JsonNode deps = desc.path("dependencies");
            if (deps.isArray()) {
                for (JsonNode d : deps) {
                    String depId = d.path("id").asText(null);
                    boolean optional = d.path("optional").asBoolean(false);
                    if (depId != null && !optional) depends.add(depId);
                }
            }

            PluginEntry e = new PluginEntry(jar, id, name, version, main, depends);
            e.priority = priorityFor(id, depends);
            return e;
        } catch (Exception e) {
            logger.error("Failed to read descriptor of {}: {}",
                    jar.getFileName(), e.getMessage());
            return null;
        }
    }

    private Object loadPluginInstance(PluginEntry entry, URLClassLoader loader) throws Exception {
        if (entry.main == null || entry.main.isBlank()) {
            logger.warn("Plugin {} has no main class", entry.jar.getFileName());
            return null;
        }

        Class<?> clazz = Class.forName(entry.main, false, loader);

        Plugin ann = clazz.getAnnotation(Plugin.class);
        String id = entry.id;
        String name = entry.name;
        String version = entry.version;
        if (ann != null) {
            if (id == null || id.isEmpty()) id = ann.id();
            if (name == null || name.isEmpty()) {
                name = ann.name().isEmpty() ? id : ann.name();
            }
            if (ann.version() != null && !ann.version().isEmpty()) version = ann.version();
        }

        Path dataFolder = pluginsDir.resolve(id);
        Files.createDirectories(dataFolder);

        Object instance = instantiate(clazz, id, dataFolder);
        if (instance == null) {
            logger.error("Failed to instantiate plugin {} ({})", id, entry.main);
            return null;
        }

        instances.put(id, instance);
        eventManager.register(instance, instance);
        proxyServer.addPlugin(new PluginContainerImpl(id, name, version, instance));

        return instance;
    }

    private Object instantiate(Class<?> clazz, String id, Path dataFolder) {
        Logger pluginLogger = new Logger("plugin." + id);

        Constructor<?>[] ctors = clazz.getDeclaredConstructors();
        Arrays.sort(ctors, (a, b) -> Integer.compare(
                b.getParameterCount(), a.getParameterCount()));

        for (Constructor<?> ctor : ctors) {
            Parameter[] params = ctor.getParameters();
            Object[] args = new Object[params.length];
            boolean ok = true;

            for (int i = 0; i < params.length; i++) {
                Class<?> type = params[i].getType();
                if (type.getName().equals("com.velocitypowered.api.proxy.ProxyServer")
                        || type.isAssignableFrom(proxyServer.getClass())) {
                    args[i] = proxyServer;
                } else if (type.getName().equals("org.slf4j.Logger")) {
                    args[i] = new Slf4jProxyLogger(pluginLogger);
                } else if (type == Path.class) {
                    args[i] = dataFolder;
                } else {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            try {
                ctor.setAccessible(true);
                Object instance = ctor.newInstance(args);
                logger.info("Plugin {}: instantiated via ctor({})",
                        id, ctor.getParameterCount());
                injectFields(instance, id, dataFolder, pluginLogger);
                return instance;
            } catch (Throwable t) {
                Throwable cause = t.getCause() != null ? t.getCause() : t;
                logger.warn("Plugin {}: ctor({}) failed: {}",
                        id, ctor.getParameterCount(),
                        cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }
        }

        try {
            Constructor<?> noArg = clazz.getDeclaredConstructor();
            noArg.setAccessible(true);
            Object instance = noArg.newInstance();
            logger.info("Plugin {}: instantiated via no-arg ctor + field injection", id);
            injectFields(instance, id, dataFolder, pluginLogger);
            return instance;
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable t) {
            logger.warn("Plugin {}: no-arg ctor failed: {}", id, t.toString());
        }

        logger.error("Plugin {}: no suitable constructor found", id);
        return null;
    }

    private void injectFields(Object instance, String id, Path dataFolder, Logger pluginLogger) {
        Class<?> clazz = instance.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (!needsInjection(field)) continue;

                Class<?> type = field.getType();
                try {
                    field.setAccessible(true);

                    if (type.getName().equals("com.velocitypowered.api.proxy.ProxyServer")
                            || type.isAssignableFrom(proxyServer.getClass())) {
                        field.set(instance, proxyServer);
                        logger.info("Plugin {}: injected ProxyServer into field {}",
                                id, field.getName());
                    } else if (type.getName().equals("org.slf4j.Logger")) {
                        field.set(instance, new Slf4jProxyLogger(pluginLogger));
                    } else if (type == Path.class) {
                        field.set(instance, dataFolder);
                    }
                } catch (Throwable t) {
                    logger.warn("Plugin {}: failed to inject field {}: {}",
                            id, field.getName(), t.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private boolean needsInjection(Field field) {
        for (Annotation a : field.getAnnotations()) {
            String name = a.annotationType().getName();
            if (name.equals("com.google.inject.Inject")) return true;
            if (name.endsWith(".DataDirectory")) return true;
        }
        return false;
    }

    public Set<String> names() {
        return Collections.unmodifiableSet(instances.keySet());
    }

    public void disableAll() {
        if (!instances.isEmpty()) {
            try { eventBus.fireSync(new ProxyShutdownEvent()); }
            catch (Throwable ignored) {}
        }
        if (sharedLoader != null) {
            try { sharedLoader.close(); } catch (Exception ignored) {}
        }
        instances.clear();
    }

    private static final class PluginEntry {
        final Path jar;
        final String id;
        final String name;
        final String version;
        final String main;
        final List<String> depends;
        int priority = 100;

        PluginEntry(Path jar, String id, String name, String version,
                    String main, List<String> depends) {
            this.jar = jar;
            this.id = id;
            this.name = name;
            this.version = version;
            this.main = main;
            this.depends = depends;
        }

        int priority() { return priority; }
    }
}
