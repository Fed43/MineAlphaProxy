package com.minealphaproxy;

public final class BuildInfo {

    public static final String VERSION;

    static {
        String v = BuildInfo.class.getPackage().getImplementationVersion();
        VERSION = (v != null) ? v : "dev";
    }

    private BuildInfo() {}
}