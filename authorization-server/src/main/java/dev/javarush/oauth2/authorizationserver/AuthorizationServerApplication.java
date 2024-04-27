package dev.javarush.oauth2.authorizationserver;

import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientService;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmService;
import dev.javarush.oauth2.authorizationserver.user.User;
import dev.javarush.oauth2.authorizationserver.user.UserRepository;
import dev.javarush.oauth2.authorizationserver.util.AESUtil;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class AuthorizationServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthorizationServerApplication.class, args);
  }

}

@Component
class Bootstrap implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

  private final RealmService realmService;
  private final ClientService clientService;
  private final UserRepository userRepository;

  Bootstrap(RealmService realmService, ClientService clientService, UserRepository userRepository) {
    this.realmService = realmService;
    this.clientService = clientService;
    this.userRepository = userRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    // Create 2 realms (java and rush)
    Realm javaRealm = this.realmService.createRealm("Java");
    Realm rushRealm = this.realmService.createRealm("Rush");

    // Create 1 confidential client (trip) for realm `java`
    Client tripClient = this.clientService.createClient(
        "java",
        new Client(
            null,
            "java",
            "Trip",
            true,
            "",
            "",
            "This is trips app",
            "",
            "http://localhost:8081/oauth/callback",
            "",
            ""
        )
    );

    // Create 1 non-confidential client (eshop) for realm `rush`
    Client shopClient = this.clientService.createClient(
        "rush",
        new Client(
            null,
            "rush",
            "Shopping App",
            false,
            "",
            "",
            "This is shopping app",
            "",
            "http://localhost:5001/oauth/callback",
            "",
            "http://localhost:5001"
        )
    );

    // Generate client secret for client `trip`
    this.clientService.generateClientSecret(tripClient.getId());

    // Create some users
    this.userRepository.save(new User("user1", "pass1", javaRealm.getId()));
    this.userRepository.save(new User("user2", "pass2", rushRealm.getId()));
    this.userRepository.save(new User("user3", "pass3", javaRealm.getId()));
    this.userRepository.save(new User("user4", "pass4", rushRealm.getId()));

    logger.info("Trip client - {}", tripClient.getId());
    logger.info("Shopping client - {}", shopClient.getId());
  }
}
