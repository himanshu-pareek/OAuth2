package dev.javarush.oauth2.authorizationserver.token;

import dev.javarush.oauth2.authorizationserver.user.UserProfile;
import dev.javarush.oauth2.authorizationserver.user.UserRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class IdTokenGenerator {

  private final UserRepository userRepository;

  public IdTokenGenerator(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  Map<String, Object> generate(String username, Collection<String> scopes, String realm, String clientId) {
    if (!scopes.contains("openid")) {
      return Collections.emptyMap();
    }
    UserProfile profile = userRepository.findProfileByUsername(username);
    var token = generateDefaultToken(profile, realm, clientId);
    includeParametersFromProfileBasedOnScopes(token, profile, scopes);
    return token;
  }

  private Map<String, Object> generateDefaultToken(UserProfile profile, String realm, String clientId) {
    Map<String, Object> token = new HashMap<>();
    long issuedTime = System.currentTimeMillis();
    token.put("sub", profile.getUsername());
    token.put("iss", "http://auth.localhost/realms/" + realm);
    token.put("aud", clientId);
    token.put("exp", getExpiryTime(issuedTime));
    token.put("iat", issuedTime);
    token.put("auth_time", issuedTime);
    return token;
  }

  private long getExpiryTime(long issuedTime) {
    return issuedTime + 2 * 60 * 60 * 1000L;
  }

  private void includeParametersFromProfileBasedOnScopes(
      Map<String, Object> token,
      UserProfile profile,
      Collection<String> scopes
  ) {
    if (!scopes.contains("openid")) {
      return;
    }
    for (String scope: scopes) {
      includeParameterForScope(token, profile, scope);
    }
  }

  private void includeParameterForScope(Map<String, Object> token, UserProfile profile,
                                        String scope) {
    switch (scope) {
      case "email":
        token.put("email", profile.getEmail());
        break;
      case "profile":
        token.put("displayName", profile.getFirstName() + " " + profile.getLastName());
        break;
    }
  }
}
