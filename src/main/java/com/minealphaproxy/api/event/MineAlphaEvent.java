package com.minealphaproxy.api.event;

public abstract class MineAlphaEvent {

    private boolean cancelled;

    public boolean isCancellable() { return false; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) {
        if (!isCancellable()) {
            throw new UnsupportedOperationException("Event is not cancellable");
        }
        this.cancelled = cancelled;
    }
}