package com.minealphaproxy.compat.velocity;

import com.minealphaproxy.util.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;

public final class ViaIntegration {

    private ViaIntegration() {}

    public static boolean isViaPresent(ClassLoader loader) {
        try {
            Class.forName("com.viaversion.viaversion.api.Via", false, loader);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void initIfPresent(ClassLoader loader, Object proxyServer,
                                     Path dataFolder, Logger logger) {
        if (!isViaPresent(loader)) return;

        try {
            Class<?> viaClass = Class.forName(
                    "com.viaversion.viaversion.api.Via", true, loader);

            // Уже инициализирована?
            try {
                Method getManager = viaClass.getMethod("getManager");
                Object existing = getManager.invoke(null);
                if (existing != null) {
                    logger.info("ViaVersion platform already initialized");
                    return;
                }
            } catch (Throwable ignored) {}

            Class<?> configClass = Class.forName(
                    "com.viaversion.velocity.platform.VelocityViaConfig",
                    true, loader);

            Object config = null;
            for (Constructor<?> ctor : configClass.getConstructors()) {
                try {
                    Object[] args = buildArgs(ctor.getParameterTypes(),
                            dataFolder, loader);
                    if (args != null) {
                        ctor.setAccessible(true);
                        config = ctor.newInstance(args);
                        break;
                    }
                } catch (Throwable ignored) {}
            }
            if (config == null) {
                try {
                    Constructor<?> noArg = configClass.getDeclaredConstructor();
                    noArg.setAccessible(true);
                    config = noArg.newInstance();
                } catch (Throwable t) {
                    logger.warn("ViaVersion config init failed: {}",
                            t.getClass().getSimpleName());
                    return;
                }
            }

            Class<?> platformClass = Class.forName(
                    "com.viaversion.velocity.platform.VelocityViaPlatform",
                    true, loader);

            Object platform = null;
            for (Constructor<?> ctor : platformClass.getConstructors()) {
                try {
                    Object[] args = buildArgs(ctor.getParameterTypes(),
                            dataFolder, loader, config, proxyServer);
                    if (args != null) {
                        ctor.setAccessible(true);
                        platform = ctor.newInstance(args);
                        break;
                    }
                } catch (Throwable ignored) {}
            }
            if (platform == null) {
                logger.warn("ViaVersion platform init failed");
                return;
            }

            Class<?> injectorClass = Class.forName(
                    "com.viaversion.velocity.platform.VelocityViaInjector",
                    true, loader);
            Object injector = injectorClass.getDeclaredConstructor().newInstance();

            Class<?> loaderImplClass = Class.forName(
                    "com.viaversion.velocity.platform.VelocityViaLoader",
                    true, loader);
            Object viaLoader = loaderImplClass.getDeclaredConstructor().newInstance();

            Class<?> managerImplClass = Class.forName(
                    "com.viaversion.viaversion.ViaManagerImpl", true, loader);
            Method builderMethod = managerImplClass.getMethod("builder");
            Object builder = builderMethod.invoke(null);

            trySet(builder, "platform", platform);
            trySet(builder, "injector", injector);
            trySet(builder, "loader", viaLoader);

            Object manager = builder.getClass().getMethod("build").invoke(builder);

            Method initMethod = viaClass.getMethod("init", manager.getClass().getInterfaces()[0]);
            initMethod.invoke(null, manager);

            logger.info("ViaVersion platform initialized successfully");

        } catch (Throwable t) {
            logger.warn("ViaVersion integration failed: {}: {}",
                    t.getClass().getSimpleName(), t.getMessage());
        }
    }

    private static void trySet(Object builder, String method, Object value) {
        try {
            for (Method m : builder.getClass().getMethods()) {
                if (m.getName().equals(method) && m.getParameterCount() == 1) {
                    m.invoke(builder, value);
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }

    private static Object[] buildArgs(Class<?>[] types, Object... candidates) {
        Object[] result = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = null;
            for (Object c : candidates) {
                if (c != null && types[i].isAssignableFrom(c.getClass())) {
                    result[i] = c;
                    break;
                }
            }
            if (result[i] == null) return null;
        }
        return result;
    }
}