package dev.javarush.oauth2.authorizationserver.user;

public class UserProfile {
  private final String username;
  private final String firstName;
  private final String lastName;
  private final String email;

  public UserProfile(String username, String firstName, String lastName, String email) {
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getUsername() {
    return username;
  }
}
