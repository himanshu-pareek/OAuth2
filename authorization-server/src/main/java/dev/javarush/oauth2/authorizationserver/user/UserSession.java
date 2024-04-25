package dev.javarush.oauth2.authorizationserver.user;

import java.time.LocalDateTime;

public class UserSession {

  private final User user;
  private final LocalDateTime expiresAt;

  public UserSession(User user) {
    this (user, LocalDateTime.now().plusMinutes(1));
  }

  public UserSession(User user, LocalDateTime expiresAt) {
    this.user = user;
    this.expiresAt = expiresAt;
  }

  public User getUser() {
    return user;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }
}
