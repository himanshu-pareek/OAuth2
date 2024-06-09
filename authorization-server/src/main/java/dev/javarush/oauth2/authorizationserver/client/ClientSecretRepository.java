package dev.javarush.oauth2.authorizationserver.client;

import org.springframework.data.repository.CrudRepository;

public interface ClientSecretRepository extends CrudRepository<ClientSecret, Integer> {

    Iterable<ClientSecret> findByClientId (String clientId);

}
