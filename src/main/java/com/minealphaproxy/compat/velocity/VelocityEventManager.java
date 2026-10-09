package com.minealphaproxy.compat.velocity;

import com.minealphaproxy.event.EventBus;
import com.velocitypowered.api.event.EventManager;

import java.util.concurrent.CompletableFuture;

public final class VelocityEventManager implements EventManager {

    private final EventBus bus;

    public VelocityEventManager(EventBus bus) {
        this.bus = bus;
    }

    @Override public void register(Object plugin, Object listener) {
        bus.register(plugin, listener);
    }

    @Override public void unregisterListener(Object plugin, Object listener) {
        bus.unregister(listener);
    }

    @Override @SuppressWarnings("unchecked")
    public <E> CompletableFuture<E> fire(E event) {
        return bus.fire(event).thenApply(o -> (E) o);
    }

    @Override public void fireAndForget(Object event) {
        bus.fireSync(event);
    }
}