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
  }

  public void save(User user) {
    this.users.add(user);
  }

  public Optional<User> login(String username, String password, String realmId) {
    return this.users.stream().filter(
        user -> user.match(username, password, realmId)
    ).findFirst();
  }
}
