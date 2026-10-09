package com.minealphaproxy.api.impl;

public final class MineAlphaApiHolder {

    private static MineAlphaProxyImpl instance;

    public static void set(MineAlphaProxyImpl api) { instance = api; }
    public static MineAlphaProxyImpl get() { return instance; }
    public static boolean isAvailable() { return instance != null; }

    private MineAlphaApiHolder() {}
}