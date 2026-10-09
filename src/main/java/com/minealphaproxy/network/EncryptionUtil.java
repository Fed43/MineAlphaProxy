package com.minealphaproxy.network;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class EncryptionUtil {

    private static final KeyPair KEY_PAIR = generateKeyPair();

    private EncryptionUtil() {}

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(1024);
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA keypair", e);
        }
    }

    public static PublicKey publicKey() {
        return KEY_PAIR.getPublic();
    }

    public static PrivateKey privateKey() {
        return KEY_PAIR.getPrivate();
    }

    public static byte[] rsaDecrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey());
        return cipher.doFinal(data);
    }

    /**
     * Minecraft-specific server hash: SHA-1 over ("" + sharedSecret + publicKeyDer).
     * Result converted to signed hex via BigInteger.toString(16).
     */
    public static String serverHash(byte[] sharedSecret) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update("".getBytes(StandardCharsets.ISO_8859_1));
        digest.update(sharedSecret);
        digest.update(publicKey().getEncoded());
        byte[] hash = digest.digest();
        return new BigInteger(hash).toString(16);
    }
}