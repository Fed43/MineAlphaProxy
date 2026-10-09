package com.velocitypowered.api.plugin;

public interface PluginContainer {
    String getId();
    String getName();
    String getVersion();
    Object getInstance();
}