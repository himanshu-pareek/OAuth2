package dev.javarush.oauth2.authorizationserver.user;

public class UserNotFoundException extends Exception {
  public UserNotFoundException(String message) {
    super(message);
  }
}
