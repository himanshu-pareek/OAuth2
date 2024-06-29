package dev.javarush.oauth2.resource_server.token;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessToken(
        @JsonProperty("iss") String issuer,
        @JsonProperty("exp") long expiresAt,
        @JsonProperty("aud") String audience,
        @JsonProperty("sub") String subject,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("iat") long issuedAt,
        @JsonProperty("jti") String tokenIdentifier,
        @JsonProperty("scope") String scopes
    ) {
}
