package dev.javarush.oauth2.authorizationserver.scope;

public class InvalidScopeException extends RuntimeException {
    public InvalidScopeException(String message) {
        super(message);
    }
}
