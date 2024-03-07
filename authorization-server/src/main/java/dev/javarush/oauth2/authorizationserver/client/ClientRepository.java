package dev.javarush.oauth2.authorizationserver.client;

import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client, String> {
  Iterable<Client> findAllByRealmId(String realmId);
}
