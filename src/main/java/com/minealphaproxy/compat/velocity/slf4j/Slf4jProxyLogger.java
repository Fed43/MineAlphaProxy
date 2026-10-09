package com.minealphaproxy.compat.velocity.slf4j;

import com.minealphaproxy.util.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;

public final class Slf4jProxyLogger extends AbstractLogger {

    private final Logger delegate;

    public Slf4jProxyLogger(Logger delegate) {
        this.delegate = delegate;
    }

    @Override public boolean isTraceEnabled() { return false; }
    @Override public boolean isDebugEnabled() { return false; }
    @Override public boolean isInfoEnabled()  { return true; }
    @Override public boolean isWarnEnabled()  { return true; }
    @Override public boolean isErrorEnabled() { return true; }

    @Override public boolean isTraceEnabled(Marker marker) { return false; }
    @Override public boolean isDebugEnabled(Marker marker) { return false; }
    @Override public boolean isInfoEnabled(Marker marker)  { return true; }
    @Override public boolean isWarnEnabled(Marker marker)  { return true; }
    @Override public boolean isErrorEnabled(Marker marker) { return true; }

    @Override
    protected String getFullyQualifiedCallerName() { return null; }

    @Override
    protected void handleNormalizedLoggingCall(Level level,
                                               Marker marker,
                                               String messagePattern,
                                               Object[] arguments,
                                               Throwable throwable) {
        Object[] args = arguments != null ? arguments : new Object[0];
        switch (level) {
            case ERROR -> delegate.error(messagePattern, concat(args, throwable));
            case WARN  -> delegate.warn(messagePattern, concat(args, throwable));
            default    -> delegate.info(messagePattern, concat(args, throwable));
        }
    }

    private static Object[] concat(Object[] args, Throwable t) {
        if (t == null) return args;
        Object[] out = new Object[args.length + 1];
        System.arraycopy(args, 0, out, 0, args.length);
        out[args.length] = t;
        return out;
    }
}