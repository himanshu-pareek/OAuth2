package dev.javarush.oauth2.authorizationserver.token;

import dev.javarush.oauth2.authorizationserver.util.Strings;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Repository
public class TokenRepository {

    private final JdbcClient jdbcClient;

    public TokenRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public String saveAccessToken (AccessToken accessToken) {
        String refreshToken = Strings.generateRandomString(32);
        long expiresAt = LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC);
        this.jdbcClient.sql("INSERT INTO refresh_tokens (id, issuer, audience, subject, client_id, scopes, expires_at) VALUES (:id, :issuer, :audience, :subject, :clientId, :scopes, :expiresAt)")
                .param("id", refreshToken)
                .param("issuer", accessToken.issuer())
                .param("audience", accessToken.audience())
                .param("subject", accessToken.subject())
                .param("clientId", accessToken.clientId())
                .param("scopes", accessToken.scopes())
                .param("expiresAt", expiresAt)
                .update();
        return refreshToken;
    }

}
