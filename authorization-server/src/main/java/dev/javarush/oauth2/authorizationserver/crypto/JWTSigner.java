package dev.javarush.oauth2.authorizationserver.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class JWTSigner {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public String sign (Object data, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException, SignatureException {
        Signature signRSA = Signature.getInstance("SHA256WITHRSA");
        signRSA.initSign(privateKey);

        String jwtHeader = "{\"typ\": \"JWT\", \"alg\": \"RS256\"}";
        String jwtHeaderEncoded = Base64.getUrlEncoder().encodeToString(jwtHeader.getBytes(StandardCharsets.UTF_8));

        String dataString = objectMapper.writeValueAsString(data);
        String dataEncoded = Base64.getUrlEncoder().encodeToString(dataString.getBytes(StandardCharsets.UTF_8));

        String inputToSign = jwtHeaderEncoded + "." + dataEncoded;

        signRSA.update(inputToSign.getBytes(StandardCharsets.UTF_8));
        byte[] signature = signRSA.sign();
        String encodedSignature = Base64.getUrlEncoder().encodeToString(signature);

        return inputToSign + "." + encodedSignature;
    }
}
