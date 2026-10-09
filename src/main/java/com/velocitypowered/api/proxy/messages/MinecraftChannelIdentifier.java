package com.velocitypowered.api.proxy.messages;

import java.nio.charset.StandardCharsets;

public final class MinecraftChannelIdentifier implements ChannelIdentifier {

    private final String namespace;
    private final String name;

    private MinecraftChannelIdentifier(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    public static MinecraftChannelIdentifier create(String namespace, String name) {
        return new MinecraftChannelIdentifier(namespace, name);
    }

    public static MinecraftChannelIdentifier from(String id) {
        int idx = id.indexOf(':');
        if (idx < 0) return new MinecraftChannelIdentifier("minecraft", id);
        return new MinecraftChannelIdentifier(
                id.substring(0, idx), id.substring(idx + 1));
    }

    public String getNamespace() { return namespace; }
    public String getName() { return name; }

    @Override public String getId() { return namespace + ":" + name; }
    @Override public byte[] getBytes() { return getId().getBytes(StandardCharsets.UTF_8); }

    @Override
    public boolean equals(Object o) {
        return o instanceof MinecraftChannelIdentifier m && m.getId().equals(getId());
    }

    @Override public int hashCode() { return getId().hashCode(); }

    @Override public String toString() { return getId(); }
}