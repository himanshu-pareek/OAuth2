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
      JWKSet jwkSet = getJWKSetFromUri("https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com");
      System.out.println(jwkSet);
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
