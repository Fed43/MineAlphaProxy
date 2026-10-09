package com.minealphaproxy.event;

import com.minealphaproxy.util.Logger;
import com.velocitypowered.api.event.PostOrder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {

    private final Logger logger;
    private final Map<Class<?>, List<Registration>> byEvent = new ConcurrentHashMap<>();

    public EventBus(Logger logger) {
        this.logger = logger;
    }

    public void register(Object owner, Object listener) {
        for (Method m : listener.getClass().getMethods()) {
            com.velocitypowered.api.event.Subscribe sub =
                    m.getAnnotation(com.velocitypowered.api.event.Subscribe.class);
            if (sub == null) continue;
            if (m.getParameterCount() != 1) continue;

            Class<?> eventType = m.getParameterTypes()[0];
            m.setAccessible(true);
            byEvent.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                    .add(new Registration(owner, listener, m, sub.order()));
        }
    }

    public void unregister(Object listener) {
        byEvent.values().forEach(list -> list.removeIf(r -> r.listener == listener));
    }

    public CompletableFuture<Object> fire(Object event) {
        return CompletableFuture.supplyAsync(() -> {
            fireSync(event);
            return event;
        });
    }

    public void fireSync(Object event) {
        List<Registration> all = new ArrayList<>();
        collectAll(event.getClass(), all);
        all.sort(Comparator.comparingInt(r -> r.order.ordinal()));

        for (Registration r : all) {
            try {
                r.method.invoke(r.listener, event);
            } catch (Throwable t) {
                Throwable cause = t;
                while (cause.getCause() != null && cause != cause.getCause()) {
                    cause = cause.getCause();
                }
                logger.error("Event {} handler {}#{} failed: {}: {}",
                        event.getClass().getSimpleName(),
                        r.listener.getClass().getSimpleName(),
                        r.method.getName(),
                        cause.getClass().getName(),
                        cause.getMessage());
                // Печатаем полный стектрейс
                logger.error("Stacktrace:", cause);
            }
        }
    }

    private void collectAll(Class<?> type, List<Registration> out) {
        List<Registration> regs = byEvent.get(type);
        if (regs != null) out.addAll(regs);

        Class<?> sup = type.getSuperclass();
        if (sup != null && sup != Object.class) collectAll(sup, out);
        for (Class<?> iface : type.getInterfaces()) collectAll(iface, out);
    }

    private static final class Registration {
        final Object owner;
        final Object listener;
        final Method method;
        final PostOrder order;

        Registration(Object owner, Object listener, Method method, PostOrder order) {
            this.owner = owner;
            this.listener = listener;
            this.method = method;
            this.order = order;
        }
    }
}