package dev.javarush.oauth2.authorizationserver.authorization;

import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientRepository;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmRepository;
import dev.javarush.oauth2.authorizationserver.user.User;
import dev.javarush.oauth2.authorizationserver.util.AESUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
  private final Set<String> VALID_RESPONSE_TYPES = Set.of("code", "token");
  private final Set<String> AVAILABLE_SCOPES = Set.of("email", "profile");

  @Value("${auth.code.aes.secret.key}")
  private String authCodeAesSecretKey;

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

  public AuthRequest getAuthRequestById(String realmId, String authRequestId) {
    AuthRequest authRequest = this.authRequestRepository.findById (authRequestId).orElseThrow(
        () -> new InvalidAuthRequestException("Invalid request")
    );
    if (!realmId.equals(authRequest.realmId())) {
      throw new InvalidAuthRequestException("Invalid request");
    }
    return authRequest;
  }

  public Map<String, String> getAuthRequestMap(AuthRequest authRequest) {
    Map<String, String> authRequestMap = new HashMap<>();
    authRequestMap.put("client_id", authRequest.clientId());
    authRequestMap.put("redirect_uri", authRequest.redirectUri());
    authRequestMap.put("response_type", authRequest.responseType());
    authRequestMap.put("scope", authRequest.scope());
    authRequestMap.put("state", authRequest.state());
    return authRequestMap;
  }

  public void denyAuthRequest(AuthRequest authRequest) {
    throw new InvalidAuthRequestException(
        authRequest.redirectUri(),
        "access_denied",
        "Access denied"
    );
  }

  public String allowAuthRequest(AuthRequest authRequest, User user) {
    AuthorizationCode authorizationCode =
        AuthorizationCode.authorizationCode(authRequest.realmId(), authRequest.clientId(),
            authRequest.redirectUri(), user.getUsername());
    try {
      return encodeAuthorizationCode(authorizationCode);
    } catch (RuntimeException e) {
      throw new InvalidAuthRequestException(
          authRequest.redirectUri(),
          "server_error",
          "Something went wrong"
      );
    }
  }

  public String encodeAuthorizationCode (AuthorizationCode authorizationCode) {
    String code = authorizationCode.toString();
    return encryptCode(code);
  }

  public AuthorizationCode decodeAuthorizationCode (String encryptedCode) {
    String code = decryptCode(encryptedCode);
    return AuthorizationCode.authCodeFromString(code);
  }

  private String encryptCode (String code) {
    IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);
    byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
    try {
      SecretKey secretKey = AESUtil.generateKey(authCodeAesSecretKey);
      byte[] encryptedCodeBytes = AESUtil.encrypt(secretKey, codeBytes, ivParameterSpec);
      return Base64.getEncoder().encodeToString(encryptedCodeBytes);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String decryptCode (String encryptedCode) {
    IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);
    byte[] encryptedCodeBytes = Base64.getDecoder().decode(encryptedCode);
    try {
      SecretKey secretKey = AESUtil.generateKey(authCodeAesSecretKey);
      byte[] codeBytes = AESUtil.decrypt(secretKey, encryptedCodeBytes, ivParameterSpec);
      return new String(codeBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setAuthCodeAesSecretKey(String authCodeAesSecretKey) {
    this.authCodeAesSecretKey = authCodeAesSecretKey;
  }
}
