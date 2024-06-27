package dev.javarush.oauth2.resource_server.security;

import java.util.Collection;

public class SecurityException extends RuntimeException {
    private final String error;
    private final String errorDescription;
    private final Collection<String> scopes;
    private final int status;

    public SecurityException(String error, String errorDescription, Collection<String> scopes, int status) {
        super(errorDescription);
        this.error = error;
        this.errorDescription = errorDescription;
        this.scopes = scopes;
        this.status = status;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getError() {
        return error;
    }

    public Collection<String> getScopes() {
        return scopes;
    }

    public int getStatus() {
        return status;
    }
}
