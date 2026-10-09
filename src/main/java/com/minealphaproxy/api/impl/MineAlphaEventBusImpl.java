package com.minealphaproxy.api.impl;

import com.minealphaproxy.api.MineAlphaEventBus;
import com.minealphaproxy.api.annotation.Subscribe;
import com.minealphaproxy.api.event.MineAlphaEvent;
import com.minealphaproxy.util.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MineAlphaEventBusImpl implements MineAlphaEventBus {

    private final Logger logger;
    private final Map<Class<?>, List<Registration>> byEvent = new ConcurrentHashMap<>();

    public MineAlphaEventBusImpl(Logger logger) { this.logger = logger; }

    @Override
    public void register(Object listener) {
        for (Method m : listener.getClass().getMethods()) {
            Subscribe sub = m.getAnnotation(Subscribe.class);
            if (sub == null) continue;
            if (m.getParameterCount() != 1) continue;
            if (!MineAlphaEvent.class.isAssignableFrom(m.getParameterTypes()[0])) continue;
            Class<?> eventType = m.getParameterTypes()[0];
            m.setAccessible(true);
            byEvent.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                    .add(new Registration(listener, m, sub.priority()));
        }
    }

    @Override
    public void unregister(Object listener) {
        byEvent.values().forEach(list -> list.removeIf(r -> r.listener == listener));
    }

    @Override
    public void fire(MineAlphaEvent event) {
        List<Registration> all = new ArrayList<>();
        collect(event.getClass(), all);
        all.sort(Comparator.comparingInt(r -> r.priority));
        for (Registration r : all) {
            try { r.method.invoke(r.listener, event); }
            catch (Throwable t) {
                Throwable cause = t.getCause() != null ? t.getCause() : t;
                logger.error("Event handler failed {}#{}: {}",
                        r.listener.getClass().getSimpleName(), r.method.getName(),
                        cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }
        }
    }

    private void collect(Class<?> type, List<Registration> out) {
        List<Registration> regs = byEvent.get(type);
        if (regs != null) out.addAll(regs);
        Class<?> sup = type.getSuperclass();
        if (sup != null && sup != Object.class) collect(sup, out);
        for (Class<?> iface : type.getInterfaces()) collect(iface, out);
    }

    private static final class Registration {
        final Object listener; final Method method; final int priority;
        Registration(Object listener, Method method, int priority) {
            this.listener = listener; this.method = method; this.priority = priority;
        }
    }
}