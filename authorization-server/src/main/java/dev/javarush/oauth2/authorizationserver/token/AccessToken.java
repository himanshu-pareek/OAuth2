package dev.javarush.oauth2.authorizationserver.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.javarush.oauth2.authorizationserver.util.Strings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

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

    // Static method to create access token
    public static AccessToken accessToken (
            String issuer,
            String audience,
            String subject,
            String clientId,
            Collection<String> scopes
    ) {
        long issuedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        return new AccessToken(
                issuer,
                issuedAt + 2 * 60 * 60,
                audience,
                subject,
                clientId,
                issuedAt,
                Strings.generateRandomString(32),
                String.join(" ", scopes)
        );
    }

}
