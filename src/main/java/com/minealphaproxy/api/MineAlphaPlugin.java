package com.minealphaproxy.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class MineAlphaPlugin {

    private MineAlphaProxy proxy;
    private MineAlphaLogger logger;
    private File dataFolder;
    private String id;
    private String name;
    private String version;
    private Object pluginClassLoader;

    // --- Устанавливается загрузчиком ---
    public void setProxy(MineAlphaProxy proxy) { this.proxy = proxy; }
    public void setLogger(MineAlphaLogger logger) { this.logger = logger; }
    public void setDataFolder(File dataFolder) { this.dataFolder = dataFolder; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setVersion(String version) { this.version = version; }
    public void setPluginClassLoader(Object loader) { this.pluginClassLoader = loader; }

    // --- Доступ для плагина ---
    public MineAlphaProxy getProxy() { return proxy; }
    public MineAlphaLogger getLogger() { return logger; }
    public File getDataFolder() { return dataFolder; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getVersion() { return version; }

    /** Сохранение ресурса из JAR в dataFolder (если ещё не существует). */
    public void saveResource(String resourceName, boolean replace) {
        if (dataFolder == null) return;
        Path out = dataFolder.toPath().resolve(resourceName);
        if (Files.exists(out) && !replace) return;

        try (InputStream in = pluginClassLoader.getClass()
                .getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                logger.warn("Resource not found: {}", resourceName);
                return;
            }
            Files.createDirectories(out.getParent());
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to save resource " + resourceName, e);
        }
    }

    // --- Жизненный цикл ---
    public void onLoad() {}
    public void onEnable() {}
    public void onDisable() {}
}