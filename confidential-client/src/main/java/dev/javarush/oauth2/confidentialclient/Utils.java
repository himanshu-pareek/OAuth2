package dev.javarush.oauth2.confidentialclient;

import java.util.Base64;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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

	public static String generateSecureRandomString(int length) {
		byte[] secureBytes = new byte[length];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(secureBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(secureBytes);
	}
	
	public static String sha256Hash (String plainText) throws NoSuchAlgorithmException {
		var plainBytes = Base64.getUrlDecoder().decode(plainText);
		MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
		messageDigest.update(plainBytes);
        byte[] digest = messageDigest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
