package dev.javarush.oauth2.authorizationserver.client;

import dev.javarush.oauth2.authorizationserver.util.Strings;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class ClientService {

  private final ClientRepository clientRepository;
  private final ClientSecretRepository clientSecretRepository;

  public ClientService(ClientRepository clientRepository,
                       ClientSecretRepository clientSecretRepository) {
    this.clientRepository = clientRepository;
    this.clientSecretRepository = clientSecretRepository;
  }

  public Client createClient(String realmId, Client client) {
    String clientId = Strings.generateRandomString(32);
    while (this.clientRepository.findById(clientId).orElse(null) != null) {
      clientId = Strings.generateRandomString(32);
    }
    client.setId(clientId);
    client.setRealmId(realmId);
    client.setCreate(true);
    return this.clientRepository.save(client);
  }

  Iterable<Client> getAll(String realmId) {
    return this.clientRepository.findAllByRealmId(realmId);
  }

  public Client getById(String realmId, String clientId) {
    Client client = this.clientRepository.findById(clientId).orElse(null);
    if (client == null) {
      return null;
    }
    if (!client.getRealmId().equals(realmId)) {
      return null;
    }
    return client;
  }

  public ClientSecret generateClientSecret(String clientId) {
    Client client = this.clientRepository.findById(clientId).orElse(null);
    if (client == null) {
      throw new RuntimeException("Client with id = " + clientId + " does not exists.");
    }
    if (!client.isConfidential()) {
      throw new RuntimeException("Client with id = " + clientId + " is not confidential client.");
    }
    byte[] secureRandomBytes = Strings.generateSecureRandomBytes(256);
    String secret = Strings.bytesToHex(secureRandomBytes);
    byte[] salt = Strings.generateSecureRandomBytes(32);
    String saltString = Base64.getEncoder().encodeToString(salt);
    String secretHash = Strings.hash(secret, salt);
    ClientSecret secretToSave = new ClientSecret(null, clientId, secretHash);
    secretToSave.setSalt(saltString);
    secretToSave = this.clientSecretRepository.save(secretToSave);
    return new ClientSecret(secretToSave.getId(), clientId, secret);
  }

  public void deleteClientSecret(Integer secretId) {
    this.clientSecretRepository.deleteById(secretId);
  }

  public boolean validateClientSecret(String clientId, String clientSecret) {
    Iterable<ClientSecret> availableSecrets = this.clientSecretRepository.findByClientId(clientId);
    for (ClientSecret secret: availableSecrets) {
      if (this.matchSecret(clientSecret, secret)) {
        return true;
      }
    }
    return false;
  }

  private boolean matchSecret (
          String plainSecret,
          ClientSecret secret
  ) {
    byte[] salt = Base64.getDecoder().decode(secret.getSalt());
    String hashSecret = Strings.hash(plainSecret, salt);
    return hashSecret.equals(secret.getSecret());
  }
}
