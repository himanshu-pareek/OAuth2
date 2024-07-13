package dev.javarush.oauth2.authorizationserver.token;

public record TokenRequest(
        String realmId,
        String clientId,
        String clientSecret,
        String grantType,
        String code,
        String redirectUri,
        String codeVerifier
) {
}
