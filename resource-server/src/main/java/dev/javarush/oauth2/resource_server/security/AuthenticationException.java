package dev.javarush.oauth2.resource_server.security;

import org.springframework.http.HttpStatus;

import java.util.Collection;

public class AuthenticationException extends SecurityException {

    public AuthenticationException(String error, String errorDescription, Collection<String> scopes) {
        super(error, errorDescription, scopes, HttpStatus.UNAUTHORIZED.value());
    }
}
