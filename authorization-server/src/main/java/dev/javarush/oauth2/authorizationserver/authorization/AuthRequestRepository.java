package dev.javarush.oauth2.authorizationserver.authorization;

import dev.javarush.oauth2.authorizationserver.util.Strings;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRequestRepository {

  private final Map<String, AuthRequest> authRequests;

  public AuthRequestRepository() {
    this.authRequests = new HashMap<>();
  }

  public String saveAuthRequest(AuthRequest authRequest) {
    String id = Strings.generateRandomString(50);
    while (this.authRequests.containsKey(id)) {
      id = Strings.generateRandomString(50);
    }
    this.authRequests.put(id, authRequest);
    return id;
  }
}
