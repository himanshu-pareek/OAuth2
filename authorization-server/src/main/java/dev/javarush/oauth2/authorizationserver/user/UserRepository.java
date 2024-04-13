package dev.javarush.oauth2.authorizationserver.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

  List<User> users;

  public UserRepository() {
    users = new ArrayList<>();
    users.add(new User("user1", "pass1", "first-realm"));
    users.add(new User("user2", "pass2", "first-realm"));
    users.add(new User("user3", "pass3", "second-realm"));
    users.add(new User("user4", "pass4", "second-realm"));
  }

  public Optional<User> login(String username, String password, String realmId) {
    return this.users.stream().filter(
        user -> user.match(username, password, realmId)
    ).findFirst();
  }
}
