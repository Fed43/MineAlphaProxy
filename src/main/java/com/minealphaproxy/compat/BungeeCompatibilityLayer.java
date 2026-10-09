package com.minealphaproxy.compat;

import com.minealphaproxy.util.Logger;

public final class BungeeCompatibilityLayer {

    private static ClassLoader apiClassLoader;

    public static void init(Logger logger) {
        apiClassLoader = BungeeCompatibilityLayer.class.getClassLoader();
        logger.info("Bungee compatibility layer initialized");
    }

    public static ClassLoader getApiClassLoader() {
        return apiClassLoader;
    }

    private BungeeCompatibilityLayer() {}
}