package dev.javarush.oauth2.authorizationserver.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public interface SHAUtil {
    static String sha256Hash(String data) throws NoSuchAlgorithmException {
        byte[] hash = sha256Hash(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().encodeToString(hash)
                .replaceAll("=", "");
    }

    static byte[] sha256Hash (byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA256");
        md.update(data);
        return md.digest();
    }
}
