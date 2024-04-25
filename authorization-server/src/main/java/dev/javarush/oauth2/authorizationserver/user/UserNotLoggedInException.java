package dev.javarush.oauth2.authorizationserver.user;

public class UserNotLoggedInException extends Exception{
  public UserNotLoggedInException(String message) {
    super(message);
  }

  public UserNotLoggedInException() {
    this("User not logged in");
  }
}
