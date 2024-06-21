package dev.javarush.oauth2.resource_server.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PublicKeyEntry(
        @JsonProperty("typ") String type,
        @JsonProperty("alg") String algorithm,
        @JsonProperty("key") String publicKey
) {}
