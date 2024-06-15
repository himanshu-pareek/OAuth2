package dev.javarush.oauth2.authorizationserver.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.javarush.oauth2.authorizationserver.authorization.AuthorizationCode;
import dev.javarush.oauth2.authorizationserver.authorization.AuthorizationService;
import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientService;
import dev.javarush.oauth2.authorizationserver.crypto.JWTSigner;
import dev.javarush.oauth2.authorizationserver.crypto.KeyPairRepository;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TokenService {

    private static Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final RealmService realmService;
    private final ClientService clientService;
    private final AuthorizationService authorizationService;
    private final KeyPairRepository keyPairRepository;
    private final TokenRepository tokenRepository;

    public TokenService(RealmService realmService, ClientService clientService, AuthorizationService authorizationService, KeyPairRepository keyPairRepository, TokenRepository tokenRepository) {
        this.realmService = realmService;
        this.clientService = clientService;
        this.authorizationService = authorizationService;
        this.keyPairRepository = keyPairRepository;
        this.tokenRepository = tokenRepository;
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

    private String encodeAccessToken (AccessToken accessToken, String realmId) {
        PrivateKey privateKey = keyPairRepository.getPrivateKey(realmId);
        try {
            return new JWTSigner().sign(accessToken, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | JsonProcessingException | SignatureException e) {
            logger.error("Error while encoding access token: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> generateAccessTokenResponse (String refreshToken, AccessToken accessToken, String realmId) {
        String accessTokenEncoded = encodeAccessToken(accessToken, realmId);
        long expiresIn = accessToken.expiresAt() - accessToken.issuedAt();
        long refreshTokenExpiresIn = 24 * 60 * 60;
        return Map.of(
                "access_token", accessTokenEncoded,
                "expires_in", expiresIn,
                "token_type", "Bearer",
                "refresh_token", refreshToken,
                "refresh_token_expires_in", refreshTokenExpiresIn
        );
    }

    public Map<String, Object> generateAccessTokenResponse (TokenRequest tokenRequest) {
        var authorizationCode = this.authorizationService.decodeAuthorizationCode(tokenRequest.code());
        AccessToken accessToken = AccessToken.accessToken(
                "http://localhost:9000" + "/realms/" + authorizationCode.realmId(),
                "api://default",
                authorizationCode.username(),
                authorizationCode.clientId(),
                authorizationCode.scopes()
        );
        String refreshToken = this.tokenRepository.saveAccessToken(accessToken);
        return generateAccessTokenResponse(refreshToken, accessToken, authorizationCode.realmId());
    }

    public List<String> getAllowedOrigins(String realmId) {
        List<String> origins = new ArrayList<>();
        this.clientService.getAll(realmId).forEach(client -> {
            String[] allowedOrigins = client.getWebOrigins().split(",");
            Collections.addAll(origins, allowedOrigins);
        });
        return origins;
    }
}
