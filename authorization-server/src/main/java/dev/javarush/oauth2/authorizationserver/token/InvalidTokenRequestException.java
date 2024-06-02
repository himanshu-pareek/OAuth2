package dev.javarush.oauth2.authorizationserver.token;

public class InvalidTokenRequestException extends RuntimeException {
    private final String error;
    private final String errorDescription;
    private final String errorUri;

    public InvalidTokenRequestException () {
        this (null);
    }

    public InvalidTokenRequestException(String errorDescription) {
        this ("invalid_request", errorDescription);
    }

    public InvalidTokenRequestException (
            String error,
            String errorDescription
    ) {
        this (error, errorDescription, null);
    }

    public InvalidTokenRequestException (
            String error,
            String errorDescription,
            String errorUri
    ) {
        super(error);
        this.error = error;
        this.errorDescription = errorDescription;
        this.errorUri = errorUri;
    }

    public String getError() {
        return error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorUri() {
        return errorUri;
    }
}
