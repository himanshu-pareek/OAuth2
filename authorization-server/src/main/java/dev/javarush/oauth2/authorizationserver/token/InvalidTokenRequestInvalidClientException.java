package dev.javarush.oauth2.authorizationserver.token;

public class InvalidTokenRequestInvalidClientException extends InvalidTokenRequestException {
    public InvalidTokenRequestInvalidClientException() {
        super("Invalid client");
    }
}
