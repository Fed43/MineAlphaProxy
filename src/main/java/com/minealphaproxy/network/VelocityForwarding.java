package com.minealphaproxy.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public final class VelocityForwarding {

    public static final String CHANNEL = "velocity:player_info";

    private static final String ALGORITHM = "HmacSHA256";

    public static final int MODERN_DEFAULT = 1;
    public static final int MODERN_WITH_KEY = 2;
    public static final int MODERN_WITH_KEY_V2 = 3;
    public static final int MODERN_LAZY_SESSION = 4;
    public static final int MODERN_MAX_VERSION = MODERN_LAZY_SESSION;

    /**
     * Точная копия PlayerDataForwarding.createForwardingData из Velocity.
     *
     * Для клиента 1.19.3+ используется MODERN_LAZY_SESSION (4).
     * Ключ профиля для версии 4 НЕ пишется — это важно.
     */
    public static ByteBuf createForwardingData(String secret, String address,
                                               UUID uuid, String username,
                                               List<PlayerProfile.Property> properties,
                                               PlayerProfile.ProfileKey key) {
        ByteBuf forwarded = Unpooled.buffer(2048);

        final int actualVersion = MODERN_LAZY_SESSION;

        VarInts.writeVarInt(forwarded, actualVersion);
        VarInts.writeString(forwarded, address);

        // writeUuid = 2 longs
        forwarded.writeLong(uuid.getMostSignificantBits());
        forwarded.writeLong(uuid.getLeastSignificantBits());

        VarInts.writeString(forwarded, username);

        // writeProperties = VarInt count + entries
        VarInts.writeVarInt(forwarded, properties.size());
        for (PlayerProfile.Property prop : properties) {
            VarInts.writeString(forwarded, prop.name());
            VarInts.writeString(forwarded, prop.value());
            if (prop.signature() != null) {
                forwarded.writeBoolean(true);
                VarInts.writeString(forwarded, prop.signature());
            } else {
                forwarded.writeBoolean(false);
            }
        }

        // MODERN_LAZY_SESSION (4): ключ НЕ пишется

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            mac.update(forwarded.array(), forwarded.arrayOffset(),
                    forwarded.readableBytes());
            byte[] sig = mac.doFinal();

            // signature ИДЁТ ПЕРВЫМ, потом data
            return Unpooled.wrappedBuffer(Unpooled.wrappedBuffer(sig), forwarded);
        } catch (Exception e) {
            forwarded.release();
            throw new RuntimeException("Failed to build forwarding data", e);
        }
    }

    public static UUID offlineUuid(String username) {
        return UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    private VelocityForwarding() {}
}