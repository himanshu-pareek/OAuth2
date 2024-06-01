package dev.javarush.oauth2.authorizationserver.client;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.stream.Stream;

public interface ClientSecretRepository extends CrudRepository<ClientSecret, Integer> {

    Stream<ClientSecret> findByClientId (String clientId);

}
