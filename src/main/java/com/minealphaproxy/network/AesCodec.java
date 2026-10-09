package com.minealphaproxy.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

public final class AesCodec extends MessageToMessageCodec<ByteBuf, ByteBuf> {

    private final Cipher decryptCipher;
    private final Cipher encryptCipher;

    public AesCodec(byte[] sharedSecret) throws Exception {
        SecretKeySpec key = new SecretKeySpec(sharedSecret, "AES");
        IvParameterSpec iv = new IvParameterSpec(sharedSecret);

        decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        decryptCipher.init(Cipher.DECRYPT_MODE, key, iv);

        encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key, iv);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
            throws Exception {
        byte[] input = new byte[msg.readableBytes()];
        msg.readBytes(input);
        out.add(Unpooled.wrappedBuffer(decryptCipher.update(input)));
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
            throws Exception {
        byte[] input = new byte[msg.readableBytes()];
        msg.readBytes(input);
        out.add(Unpooled.wrappedBuffer(encryptCipher.update(input)));
    }
}