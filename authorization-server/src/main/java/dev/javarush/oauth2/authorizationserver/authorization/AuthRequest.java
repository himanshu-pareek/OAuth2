package dev.javarush.oauth2.authorizationserver.authorization;

public record AuthRequest(
    String realmId,
    String clientId,
    String redirectUri,
    String responseType,
    String scope,
    String state,
    String codeChallenge,
    String codeChallengeMethod
) {

}
