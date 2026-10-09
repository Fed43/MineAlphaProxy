package com.minealphaproxy.compat.velocity.slf4j;

import com.minealphaproxy.util.Logger;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public final class MineSlf4jProvider implements SLF4JServiceProvider {

    public static final String REQUESTED_API_VERSION = "2.0.99";

    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;

    @Override public ILoggerFactory getLoggerFactory() { return loggerFactory; }
    @Override public IMarkerFactory getMarkerFactory() { return markerFactory; }
    @Override public MDCAdapter getMDCAdapter() { return mdcAdapter; }
    @Override public String getRequestedApiVersion() { return REQUESTED_API_VERSION; }

    @Override
    public void initialize() {
        loggerFactory = name -> new Slf4jProxyLogger(new Logger(name));
        markerFactory = new BasicMarkerFactory();
        mdcAdapter = new NOPMDCAdapter();
    }
}