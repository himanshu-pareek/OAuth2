package dev.javarush.oauth2.resource_server.crypto;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Lazy
@Component
public class CryptoRepository {

    private static final Logger log = LoggerFactory.getLogger(CryptoRepository.class);

    private PublicKeyEntry publicKeyEntry;

    @Value("${oauth2.auth-server.jwk-set-uri}")
    private String jwkUri;

    private final RestClient restClient;

    public CryptoRepository(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @PostConstruct
    public void init() {
        // Get publicKeyEntry from internet
        log.info("Getting public key from {}", jwkUri);
        this.publicKeyEntry = this.restClient.get().uri(jwkUri)
                .retrieve()
                .body(PublicKeyEntry.class);
        log.info("Got public key with type {} and algorithm {}", this.publicKeyEntry.type(), this.publicKeyEntry.algorithm());
    }

    public PublicKeyEntry getPublicKeyEntry() {
        return publicKeyEntry;
    }
}
