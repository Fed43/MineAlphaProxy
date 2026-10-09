package com.minealphaproxy.config;

public enum ProxyBrand {
    FED43_PROXY("Fed43Proxy"),
    MINE_ALPHA_PROXY("MineAlphaProxy");

    private final String displayName;

    ProxyBrand(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static ProxyBrand fromString(String value) {
        if (value == null || value.isBlank()) return MINE_ALPHA_PROXY;
        for (ProxyBrand b : values()) {
            if (b.displayName.equalsIgnoreCase(value)
                    || b.name().equalsIgnoreCase(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException(
                "Неизвестное название прокси: '" + value + "'. " +
                "Допустимые значения: Fed43Proxy, MineAlphaProxy");
    }
}