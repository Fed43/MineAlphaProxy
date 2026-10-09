package com.minealphaproxy.api;

public interface MineAlphaLogger {
    void info(String msg, Object... args);
    void warn(String msg, Object... args);
    void error(String msg, Object... args);
    void debug(String msg, Object... args);
    void error(String msg, Throwable t);
}