package com.minealphaproxy.api.impl;

import com.minealphaproxy.api.MineAlphaLogger;
import com.minealphaproxy.util.Logger;

public final class MineAlphaLoggerImpl implements MineAlphaLogger {

    private final Logger delegate;

    public MineAlphaLoggerImpl(String pluginId) {
        this.delegate = new Logger("plugin." + pluginId);
    }

    @Override public void info(String msg, Object... args) { delegate.info(msg, args); }
    @Override public void warn(String msg, Object... args) { delegate.warn(msg, args); }
    @Override public void error(String msg, Object... args) { delegate.error(msg, args); }
    @Override public void debug(String msg, Object... args) { delegate.debug(msg, args); }
    @Override public void error(String msg, Throwable t) { delegate.error(msg, t); }
}