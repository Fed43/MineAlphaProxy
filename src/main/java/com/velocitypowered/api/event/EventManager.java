package com.velocitypowered.api.event;

import java.util.concurrent.CompletableFuture;

public interface EventManager {
    void register(Object plugin, Object listener);
    void unregisterListener(Object plugin, Object listener);
    <E> CompletableFuture<E> fire(E event);
    void fireAndForget(Object event);
}