package com.minealphaproxy.compat.velocity;

import com.velocitypowered.api.plugin.PluginContainer;

public final class PluginContainerImpl implements PluginContainer {

    private final String id;
    private final String name;
    private final String version;
    private final Object instance;

    public PluginContainerImpl(String id, String name, String version, Object instance) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.instance = instance;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public String getVersion() { return version; }
    @Override public Object getInstance() { return instance; }
}