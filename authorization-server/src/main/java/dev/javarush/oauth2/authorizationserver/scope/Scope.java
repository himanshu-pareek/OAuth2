package dev.javarush.oauth2.authorizationserver.scope;

public record Scope(String realmId, String name, String description, boolean required) {
}
