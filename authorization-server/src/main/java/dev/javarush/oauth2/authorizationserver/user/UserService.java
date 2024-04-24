package dev.javarush.oauth2.authorizationserver.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User validateUser(String username, String password, String realmId)
      throws UserNotFoundException {
    return this.userRepository.login(username, password, realmId)
        .orElseThrow(() -> new UserNotFoundException("Invalid username / password"));
  }

  public void saveSession(User user, String realmId, HttpSession session) {
    session.removeAttribute(realmId);
    session.setAttribute(realmId, new UserSession(user));
  }
}
