package com.velocitypowered.api.plugin;

import java.util.Collection;
import java.util.Optional;

public interface PluginManager {
    Optional<PluginContainer> fromInstance(Object instance);
    Optional<PluginContainer> getPlugin(String id);
    Collection<PluginContainer> getPlugins();
    boolean isLoaded(String id);
}