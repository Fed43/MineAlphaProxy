package com.minealphaproxy.api;

import com.minealphaproxy.api.event.MineAlphaEvent;

public interface MineAlphaEventBus {
    void register(Object listener);
    void unregister(Object listener);
    void fire(MineAlphaEvent event);
}