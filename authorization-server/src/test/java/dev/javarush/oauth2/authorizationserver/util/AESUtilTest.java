package dev.javarush.oauth2.authorizationserver.util;

import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.junit.jupiter.api.Test;

class AESUtilTest {

  private static final String ENCODED_KEY = "/wuJXhw/t5RkxKGAInZaZSo4fDug6zlBGWGe8PJalY0=";

  @Test
  void encryptAndDecrypt() throws Exception {
    IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);
    String message = "Hello World!";
    byte[] bytes = message.getBytes();
    SecretKey secretKey = AESUtil.generateKey(ENCODED_KEY);
    byte[] encryptedBytes = AESUtil.encrypt(secretKey, bytes, ivParameterSpec);
    byte[] decryptedBytes = AESUtil.decrypt(secretKey, encryptedBytes, ivParameterSpec);
    String decryptedMessage = new String(decryptedBytes);
    assertEquals(message, decryptedMessage);
  }

}