package dev.javarush.oauth2.authorizationserver.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.*;

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

}