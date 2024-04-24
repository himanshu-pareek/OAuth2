package dev.javarush.oauth2.authorizationserver.realms;

import org.springframework.stereotype.Service;

@Service
public class RealmService {

  private final RealmRepository repository;


  public RealmService(RealmRepository repository) {
    this.repository = repository;
  }

  public Realm createRealm(String realmName) {
    String realmId = generateIdFromName(realmName);
    if (this.repository.findById(realmId).orElse(null) != null) {
      throw new RuntimeException("Realm already present.");
    }
    Realm realm = new Realm(realmId, realmName);
    realm.setCreate(true);
    return this.repository.save(realm);
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
}
