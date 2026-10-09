package com.minealphaproxy.network;

import java.util.List;
import java.util.UUID;

public record PlayerProfile(UUID uuid, String name, List<Property> properties,
                            ProfileKey key) {

    public record Property(String name, String value, String signature) {}

    public record ProfileKey(long expire, byte[] keyBytes, byte[] signature) {}
}