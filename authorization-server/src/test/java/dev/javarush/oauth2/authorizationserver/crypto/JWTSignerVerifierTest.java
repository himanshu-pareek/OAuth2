package dev.javarush.oauth2.authorizationserver.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.*;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

record Token (
        String id,
        String name,
        String email
) {}

class JWTSignerVerifierTest {

    private JWTSigner signer;
    private JWTVerifier<Token> verifier;
    private KeyPair keyPair;

    @BeforeEach
    public void beforeEach() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.keyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1 () throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, JsonProcessingException, InvalidJWTException {
        Token token = new Token("1", "user", "user@email.com");
        this.signer = new JWTSigner();
        String jwt = signer.sign(token, keyPair.getPrivate());

        this.verifier = new JWTVerifier<>(jwt, Token.class);
        Token verifiedToken = this.verifier.verify(keyPair.getPublic()).getToken();
        assertEquals(token, verifiedToken);
    }

    @Test
    public void test2() throws Exception {
        var mapJWTVerifier = getMapJWTVerifier();
        String kid = (String) mapJWTVerifier.getHeader().get("kid");
        JWKSet jwkSet = getJWKSetFromUri("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com");
        JWK jwk = jwkSet.keys().stream()
            .filter(jw -> jw.kid().equals(kid))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("JWK not found for " + kid));
        PublicKey publicKey = createPublicKeyFromJWK(jwk);
        Map token = mapJWTVerifier.verify(publicKey).getToken();
        System.out.println(token);
    }

    private static JWTVerifier<Map> getMapJWTVerifier() {
        String jwt = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFkYmUwNmI1ZDdjMmE3YzA0NDU2MzA2MWZmMGZlYTM3NzQwYjg2YmMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vaW5kaWhvb2QtZGV2LWluIiwiYXVkIjoiaW5kaWhvb2QtZGV2LWluIiwiYXV0aF90aW1lIjoxNzIyNjAwODc5LCJ1c2VyX2lkIjoicUhUWTlkUHFCQ1ZqemJPeDJTaUhYQzZmQWt5MSIsInN1YiI6InFIVFk5ZFBxQkNWanpiT3gyU2lIWEM2ZkFreTEiLCJpYXQiOjE3MjI2MDA4NzksImV4cCI6MTcyMjYwNDQ3OSwiZW1haWwiOiJ0ZXN0MWluZGlAeWFob28uY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZW1haWwiOlsidGVzdDFpbmRpQHlhaG9vLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.GhCoXrrprjctIGRbrHeWSnB-MwNz778D3cyj5dMY0MoUAO8HuXTJ692BuX2Trk2i3j6BfxaOO7SuAuCpuKfq8J5lo2NEXSxCZqoJQXUU4aTU9Y1bKO6iPKlLa0cuFU8uqspvq_E57rSU74izkMLFEeoApfz1dTIQrFTQL5WZy0VCeQk5G1lMZvLlBsKjUramwKdV9LGXcM72I3GTZe9MVtMpaM523_JoOu_mSll6Ygu0G_at6whgz2e72du9wVUDIjg7jno2T9KkJKf0ZXLwr-KAH7QAnTqVBnyPNAF-ivSPI-ovVu5FporBzFZaYaoguKOVtyCBGc9VgnyaxjfbVg";
      return new JWTVerifier<>(jwt, Map.class);
    }

    private PublicKey createPublicKeyFromJWK(JWK jwk) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            var rsaPublicKeySpec = new RSAPublicKeySpec(jwk.getModulus(), jwk.getExponent());
            return keyFactory.generatePublic(rsaPublicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private JWKSet getJWKSetFromUri(String jwksUri) {
        RestClient restClient = RestClient.builder().build();
        return restClient.get()
            .uri(jwksUri)
            .retrieve()
            .body(JWKSet.class);
    }
}

record JWK(String kid, String e, String n, String kty, String use, String alg) {
    BigInteger getExponent() {
        return new BigInteger(1, Base64.getUrlDecoder().decode(e));
    }

    BigInteger getModulus() {
        return new BigInteger(1, Base64.getUrlDecoder().decode(n));
    }
}

record JWKSet (List<JWK> keys) {}
