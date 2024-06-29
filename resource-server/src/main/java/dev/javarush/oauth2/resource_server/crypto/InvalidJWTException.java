package dev.javarush.oauth2.resource_server.crypto;

public class InvalidJWTException extends Exception {
    public InvalidJWTException() {
        this ("Invalid JWT Token");
    }

    public InvalidJWTException(String message) {
        super(message);
    }
}
