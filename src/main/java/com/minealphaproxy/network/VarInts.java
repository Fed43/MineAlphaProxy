package com.minealphaproxy.network;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public final class VarInts {

    private VarInts() {}

    /** Максимум 5 байт для VarInt в Minecraft-протоколе. */
    public static int readVarInt(ByteBuf in) {
        int value = 0;
        int position = 0;
        while (true) {
            byte b = in.readByte();
            value |= (b & 0x7F) << position;
            if ((b & 0x80) == 0) return value;
            position += 7;
            if (position >= 32) {
                throw new RuntimeException("VarInt is too big");
            }
        }
    }

    public static void writeVarInt(ByteBuf out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            }
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    public static String readString(ByteBuf in) {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuf out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.writeBytes(bytes);
    }
}