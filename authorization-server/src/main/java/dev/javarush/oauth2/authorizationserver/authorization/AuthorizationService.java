package dev.javarush.oauth2.authorizationserver.authorization;

import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientRepository;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthorizationService {
    private final Set<String> AVAILABLE_SCOPES = Set.of("code", "token");

    private final RealmRepository realmRepository;
    private final ClientRepository clientRepository;

    public AuthorizationService(RealmRepository realmRepository, ClientRepository clientRepository) {
        this.realmRepository = realmRepository;
        this.clientRepository = clientRepository;
    }

    public void verifyAuthRequest(AuthRequest authRequest) {
        Realm realm = validateRealm(authRequest);
        Client client = validateClient(authRequest);
        String redirectUri = validateRedirectUri(client, authRequest);
        String responseType = validateResponseType(authRequest, client, redirectUri);
    }

    private String validateResponseType(
            AuthRequest authRequest,
            Client client,
            String redirectUri
    ) {
        String responseType = authRequest.responseType();
        if (responseType == null || responseType.isBlank()) {
            throw new InvalidAuthRequestException(
                    redirectUri,
                    "invalid_request",
                    "Invalid response_type"
            );
        }
        responseType = responseType.trim();
        if (!AVAILABLE_SCOPES.contains(responseType)) {
            throw new InvalidAuthRequestException(
                    redirectUri,
                    "invalid_request",
                    "Invalid response_type"
            );
        }
        if (client.isConfidential() && !responseType.equals("code")) {
            throw new InvalidAuthRequestException(
                    redirectUri,
                    "unauthorized_client",
                    "Invalid response_type"
            );
        }
        return responseType;
    }

    private String validateRedirectUri(Client client, AuthRequest authRequest) {
        String redirectUri = authRequest.redirectUri();
        if (redirectUri == null) {
            throw new InvalidAuthRequestException("Redirect uri is not present.");
        }
        redirectUri = redirectUri.trim();
        if (redirectUri.isBlank()) {
            throw new InvalidAuthRequestException("Redirect uri is not present.");
        }
        String[] registeredUris = client.getSignInRedirectUris().split(",");
        for (String registeredUri: registeredUris) {
            if (registeredUri.equals(redirectUri)) {
                return redirectUri;
            }
        }
        throw new InvalidAuthRequestException("Invalid redirect uri");
    }

    private Realm validateRealm(AuthRequest authRequest) {
        if (authRequest.realmId() == null) {
            throw new InvalidAuthRequestException("Realm is not present");
        }
        return this.realmRepository.findById(authRequest.realmId())
                .orElseThrow(() -> new InvalidAuthRequestException("Invalid realm"));
    }

    private Client validateClient(AuthRequest authRequest) {
        if (authRequest.clientId() == null) {
            throw new InvalidAuthRequestException("Client id not present");
        }
        Client client = this.clientRepository.findById(authRequest.clientId())
                .orElseThrow(() -> new InvalidAuthRequestException("Invalid client id"));
        if (!client.getRealmId().equals(authRequest.realmId())) {
            throw new InvalidAuthRequestException("Invalid client");
        }
        return client;
    }


}
