package dev.javarush.oauth2.authorizationserver.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

  List<User> users;
  List<UserProfile> profiles;

  public UserRepository() {
    users = new ArrayList<>();
    this.profiles = new ArrayList<>();
  }

  public void save(User user) {
    this.users.add(user);
  }

  public void saveProfile(UserProfile profile) {
    profiles.add(profile);
  }

  public Optional<User> login(String username, String password, String realmId) {
    return this.users.stream().filter(
        user -> user.match(username, password, realmId)
    ).findFirst();
  }

  public UserProfile findProfileByUsername(String username) {
    return profiles.stream()
        .filter(p -> Objects.equals(username, p.getUsername()))
        .findFirst()
        .orElse(new UserProfile(username, username, "", null));
  }
}
