package dev.javarush.oauth2.authorizationserver.realms;

import java.util.Random;
import org.springframework.data.repository.CrudRepository;

public interface RealmRepository extends CrudRepository<Realm, String> {
}
