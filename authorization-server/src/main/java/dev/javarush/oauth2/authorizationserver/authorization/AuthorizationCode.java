package dev.javarush.oauth2.authorizationserver.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.javarush.oauth2.authorizationserver.util.Strings;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public record AuthorizationCode(
    String realmId,
    String clientId,
    String redirectURI,
    String username,
    Collection<String> scopes,
    LocalDateTime expiresAt,
    String id,
    String codeChallenge,
    String codeChallengeMethod
) {
  public static AuthorizationCode authorizationCode(
      String realmId,
      String clientId,
      String redirectURI,
      String username,
      Collection<String> scopes,
      String codeChallenge,
      String codeChallengeMethod
  ) {
    return new AuthorizationCode(
        realmId,
        clientId,
        redirectURI,
        username,
        scopes,
        LocalDateTime.now().plusMinutes(10),
        Strings.generateRandomString(32),
        codeChallenge,
        codeChallengeMethod
    );
  }

  @Override
  public String toString() {
      try {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
          return objectMapper.writeValueAsString(this);
      } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
      }
  }

  public static AuthorizationCode authCodeFromString(String code) {
      try {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
          return objectMapper.readValue(code, AuthorizationCode.class);
      } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
      }
  }
}
