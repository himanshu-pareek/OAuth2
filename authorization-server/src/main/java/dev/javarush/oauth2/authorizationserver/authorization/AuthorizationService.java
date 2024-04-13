package dev.javarush.oauth2.authorizationserver.authorization;

import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientRepository;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmRepository;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
  private final Set<String> VALID_RESPONSE_TYPES = Set.of("code", "token");
  private final Set<String> AVAILABLE_SCOPES = Set.of("email", "profile");

  private final RealmRepository realmRepository;
  private final ClientRepository clientRepository;
  private final AuthRequestRepository authRequestRepository;

  public AuthorizationService(
      RealmRepository realmRepository,
      ClientRepository clientRepository, AuthRequestRepository authRequestRepository
  ) {
    this.realmRepository = realmRepository;
    this.clientRepository = clientRepository;
    this.authRequestRepository = authRequestRepository;
  }

  public Realm verifyAuthRequest(AuthRequest authRequest) {
    Realm realm = validateRealm(authRequest);
    Client client = validateClient(authRequest);
    validateRedirectUri(client, authRequest);
    validateResponseType(authRequest, client, authRequest.redirectUri());
    validateScope(authRequest, client, authRequest.redirectUri());
    return realm;
  }

  private void validateScope(
      AuthRequest authRequest,
      Client client,
      String redirectUri
  ) {
    if (authRequest.scope() == null || authRequest.scope().isBlank()) {
      throw new InvalidAuthRequestException(
          redirectUri,
          "invalid_scope",
          "Scope not present"
      );
    }
    String[] scopes = authRequest.scope().split(" ");
    for (String scope : scopes) {
      if (!AVAILABLE_SCOPES.contains(scope)) {
        throw new InvalidAuthRequestException(
            redirectUri,
            "invalid_scope",
            "Scope not allowed"
        );
      }
    }
  }

  private void validateResponseType(
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
    if (!VALID_RESPONSE_TYPES.contains(responseType)) {
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
  }

  private void validateRedirectUri(Client client, AuthRequest authRequest) {
    String redirectUri = authRequest.redirectUri();
    if (redirectUri == null) {
      throw new InvalidAuthRequestException("Redirect uri is not present.");
    }
    if (redirectUri.isBlank()) {
      throw new InvalidAuthRequestException("Redirect uri is not present.");
    }
    String[] registeredUris = client.getSignInRedirectUris().split(",");
    for (String registeredUri : registeredUris) {
      if (registeredUri.equals(redirectUri)) {
        return;
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


  public String getAuthRequestId(AuthRequest authRequest) {
    return this.authRequestRepository.saveAuthRequest(authRequest);
  }
}
