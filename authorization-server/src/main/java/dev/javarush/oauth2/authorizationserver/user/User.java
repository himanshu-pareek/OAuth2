package dev.javarush.oauth2.authorizationserver.user;

public class User {

  private final String username;
  private final String password;
  private final String realmId;

  public User(String username, String password, String realmId) {
    this.username = username;
    this.password = password;
    this.realmId = realmId;
  }

  public String getRealmId() {
    return realmId;
  }

  public String getUsername() {
    return username;
  }

  public boolean match (String username, String password, String realmId) {
    return this.username.equals(username) && this.password.equals(password) && this.realmId.equals(realmId);
  }
}
