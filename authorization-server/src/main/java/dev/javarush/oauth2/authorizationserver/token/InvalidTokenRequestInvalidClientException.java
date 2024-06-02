package dev.javarush.oauth2.authorizationserver.token;

public class InvalidTokenRequestInvalidClientException extends InvalidTokenRequestException {
    public InvalidTokenRequestInvalidClientException() {
        super ("invalid_client", "Client is not valid");
    }
}
