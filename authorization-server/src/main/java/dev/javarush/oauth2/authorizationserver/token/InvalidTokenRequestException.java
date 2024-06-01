package dev.javarush.oauth2.authorizationserver.token;

public class InvalidTokenRequestException extends RuntimeException {
    public InvalidTokenRequestException(String message) {
        super(message);
    }
}
