package dev.javarush.oauth2.authorizationserver.realms;

import dev.javarush.oauth2.authorizationserver.crypto.KeyPairRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RealmService {

  private final RealmRepository repository;
  private final KeyPairRepository keyPairRepository;


  public RealmService(RealmRepository repository, KeyPairRepository keyPairRepository) {
    this.repository = repository;
      this.keyPairRepository = keyPairRepository;
  }

  public Realm createRealm(String realmName) {
    String realmId = generateIdFromName(realmName);
    if (this.repository.findById(realmId).orElse(null) != null) {
      throw new RuntimeException("Realm already present.");
    }
    Realm realm = new Realm(realmId, realmName);
    realm.setCreate(true);
    Realm savedRealm = this.repository.save(realm);
    this.keyPairRepository.generateKeysForRealm(realm.getId());
    return savedRealm;
  }

  public Realm getRealm(String id) {
    return this.repository.findById(id).orElse(null);
  }

  public Iterable<Realm> getAllRealms() {
    return this.repository.findAll();
  }

  private String generateIdFromName(String name) {
    String[] words = name.split(" ");
    for (int i = 0; i < words.length; i++) {
      words[i] = words[i].toLowerCase();
    }
    return String.join("-", words);
  }

  public Map<String, Object> getPublicKey(String id) {
    String publicKeyString = keyPairRepository.getPublicKeyString(id);
    return Map.of(
            "typ", "JWT",
            "alg", "RS256",
            "key", publicKeyString
    );
  }
}
