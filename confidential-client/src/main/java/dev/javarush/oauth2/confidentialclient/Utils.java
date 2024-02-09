package dev.javarush.oauth2.confidentialclient;

import java.util.Random;

public class Utils {
  private static final String lake = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  public static String generateRandomString(int len) {
    return generateRandomString(lake, len);
  }

  public static String generateRandomString (String lake, int len) {
    final int n = lake.length();
    assert n >= 1;
    Random random = new Random();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++) {
      int index = random.nextInt(0, n);
      sb.append(lake.charAt(index));
    }
    return sb.toString();
  }

}
