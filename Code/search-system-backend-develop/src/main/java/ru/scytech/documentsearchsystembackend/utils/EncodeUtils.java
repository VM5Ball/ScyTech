package ru.scytech.documentsearchsystembackend.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncodeUtils {
    public static byte[] sha256Hash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("algorithm sha-256 not found !");
        }
    }

    public static String sha256HexHash(byte[] bytes) {
        byte[] hash = sha256Hash(bytes);
        return bytes2Hex(hash);
    }

    public static String bytes2Hex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    public static String toBase64(byte[] bytes) {
        return new String(Base64
                .getEncoder()
                .encode(bytes));
    }
}
