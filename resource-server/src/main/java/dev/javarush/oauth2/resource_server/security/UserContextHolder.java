package dev.javarush.oauth2.resource_server.security;

public class UserContextHolder {

    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();

    public static void setUserId (String userId) {
        currentUserId.set(userId);
    }

    public static String getUserId () {
        return currentUserId.get();
    }
}
