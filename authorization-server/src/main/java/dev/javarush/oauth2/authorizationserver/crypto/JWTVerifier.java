package dev.javarush.oauth2.authorizationserver.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.Map;

public class JWTVerifier<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private T token;
    private final Class<? extends T> clazz;
    private final String jwt;
    private String inputToSignature;
    private String encodedSignature;
    private Map header;

    public JWTVerifier(String jwt, Class<T> clazz) {
        this.jwt = jwt;
        this.clazz = clazz;
    }

    private void parse() throws InvalidJWTException, JsonProcessingException {
        String[] parts = this.jwt.split("\\.");
        if (parts.length != 3) {
            throw new InvalidJWTException();
        }

        String encodedHeader = parts[0];
        String encodedData = parts[1];
        this.encodedSignature = parts[2];
        this.inputToSignature = encodedHeader + "." + encodedData;

        byte[] data = Base64.getUrlDecoder().decode(encodedData);
        byte[] headerBytes = Base64.getUrlDecoder().decode(encodedHeader);

        this.token = objectMapper.readValue(
                new String(data),
                this.clazz
        );
        this.header = objectMapper.readValue(
            new String(headerBytes),
            Map.class
        );
    }

    public JWTVerifier<T> verify(PublicKey publicKey) throws InvalidJWTException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, JsonProcessingException {
        if (this.token == null) {
            parse();
        }

        Signature rsaSign = Signature.getInstance("SHA256WITHRSA");
        rsaSign.initVerify(publicKey);
        rsaSign.update(inputToSignature.getBytes(StandardCharsets.UTF_8));
        boolean isValid = rsaSign.verify(Base64.getUrlDecoder().decode(this.encodedSignature));

        if (!isValid) {
            throw new InvalidJWTException("Signature does not match");
        }

        return this;
    }

    public T getToken() throws InvalidJWTException, JsonProcessingException {
        if (this.token == null) {
            parse();
        }
        return this.token;
    }

    public Map getHeader() throws InvalidJWTException, JsonProcessingException {
        if (this.header == null) {
            parse();
        }
        return this.header;
    }
}
