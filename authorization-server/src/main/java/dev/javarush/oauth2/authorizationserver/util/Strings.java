package dev.javarush.oauth2.authorizationserver.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Strings {
  private static final List<Character> DEFAULT_LAKE = new ArrayList<>();
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  static {
    for (char c = 'a'; c <= 'z'; c++) {
      DEFAULT_LAKE.add(c);
    }
    for (char c = 'A'; c <= 'Z'; c++) {
      DEFAULT_LAKE.add(c);
    }
    for (char c = '0'; c <= '9'; c++) {
      DEFAULT_LAKE.add(c);
    }
  }

  public static String generateRandomString(int length) {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < length; i++) {
      int index = random.nextInt(0, DEFAULT_LAKE.size());
      sb.append(DEFAULT_LAKE.get(index));
    }
    return sb.toString();
  }

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static byte[] generateSecureRandomBytes(int length) {
    SecureRandom secureRandom = new SecureRandom();
    byte[] values = new byte[length];
    secureRandom.nextBytes(values);
    return values;
  }

  public static String hash(String secret, byte[] salt) {
    try {
      return Pbkdf2.computeHash(secret, salt);
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong.");
    }
  }
}
