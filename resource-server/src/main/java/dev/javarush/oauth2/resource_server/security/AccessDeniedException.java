package dev.javarush.oauth2.resource_server.security;

import org.springframework.http.HttpStatus;

import java.util.Collection;

public class AccessDeniedException extends SecurityException{
    public AccessDeniedException(String error, String errorDescription, Collection<String> scopes) {
        super(error, errorDescription, scopes, HttpStatus.FORBIDDEN.value());
    }
}
