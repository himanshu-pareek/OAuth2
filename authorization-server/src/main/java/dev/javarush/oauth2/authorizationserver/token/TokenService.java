package dev.javarush.oauth2.authorizationserver.token;

import dev.javarush.oauth2.authorizationserver.authorization.AuthorizationCode;
import dev.javarush.oauth2.authorizationserver.authorization.AuthorizationService;
import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientService;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class TokenService {

    private final RealmService realmService;
    private final ClientService clientService;
    private final AuthorizationService authorizationService;

    public TokenService(RealmService realmService, ClientService clientService, AuthorizationService authorizationService) {
        this.realmService = realmService;
        this.clientService = clientService;
        this.authorizationService = authorizationService;
    }

    public void verifyTokenRequest(TokenRequest tokenRequest) {
        verifyRealm(tokenRequest.realmId());
        verifyClient(tokenRequest);
        verifyGrantType(tokenRequest);
        verifyCode(tokenRequest);
    }

    private void verifyCode(TokenRequest tokenRequest) {
        String code = tokenRequest.code();
        if (!StringUtils.hasText(code)) {
            throw new InvalidTokenRequestException("code is required");
        }
        AuthorizationCode authorizationCode;
        try {
            authorizationCode = this.authorizationService.decodeAuthorizationCode(tokenRequest.code());
        } catch (RuntimeException e) {
            // 0. Invalid code
            var ex = new InvalidTokenRequestException(
                    "invalid_grant",
                    "Invalid authorization code"
            );
            ex.initCause(e);
            throw ex;
        }

        // 1. Verify client id matches
        if (!tokenRequest.clientId().equals(authorizationCode.clientId())) {
            // client id does not match
            throw new InvalidTokenRequestException(
                    "invalid_grant",
                    "Client id does not match"
            );
        }

        // 2. Verify expiration
        if (authorizationCode.expiresAt().isBefore(LocalDateTime.now())) {
            // authorization code is expired
            throw new InvalidTokenRequestException(
                    "invalid_grant",
                    "Authorization code is expired"
            );
        }

        // 3. Verify redirect uri
        if (!authorizationCode.redirectURI().equals(tokenRequest.redirectUri())) {
            // invalid redirect uri
            throw new InvalidTokenRequestException(
                    "invalid_grant",
                    "Invalid redirect uri"
            );
        }
    }

    private void verifyGrantType(TokenRequest tokenRequest) {
        if (tokenRequest.grantType() == null || !StringUtils.hasText(tokenRequest.grantType())) {
            throw new InvalidTokenRequestException("grant_type is required");
        }
        if (!tokenRequest.grantType().equals("authorization_code")) {
            throw new InvalidTokenRequestException (
                    "unsupported_grant_type",
                    "grant_type must be one of [authorization_code, client_credential]"
            );
        }
    }

    private void verifyClient(TokenRequest tokenRequest) {
        if (tokenRequest.clientId() == null || !StringUtils.hasText(tokenRequest.clientId())) {
            throw new InvalidTokenRequestException("client_id is required");
        }
        Client client = this.clientService.getById(
                tokenRequest.realmId(),
                tokenRequest.clientId()
        );
        if (client == null) {
            throw new InvalidTokenRequestInvalidClientException ();
        }
        if (!client.isConfidential()) {
            return;
        }
        boolean isClientSecretValid = this.clientService.validateClientSecret (
                tokenRequest.clientId(),
                tokenRequest.clientSecret()
        );
        if (!isClientSecretValid) {
            throw new InvalidTokenRequestInvalidClientException();
        }
    }

    private void verifyRealm(String realmId) {
        Realm realm = this.realmService.getRealm(realmId);
        if (realm == null) {
            throw new InvalidTokenRequestException (
                    "Invalid realm"
            );
        }
    }

    private String generateAccessToken (TokenRequest tokenRequest) {
        return null;
    }
}
