package dev.javarush.oauth2.authorizationserver.authorization;

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
    String id
) {
  public static AuthorizationCode authorizationCode(
      String realmId,
      String clientId,
      String redirectURI,
      String username,
      Collection<String> scopes
  ) {
    return new AuthorizationCode(
        realmId,
        clientId,
        redirectURI,
        username,
        scopes,
        LocalDateTime.now().plusMinutes(10),
        Strings.generateRandomString(32)
    );
  }

  @Override
  public String toString() {
    return String.format(
        "%s##%s##%s##%s##%s##%d##%s",
        realmId,
        clientId,
        redirectURI,
        username,
        String.join(" ", scopes),
        expiresAt.toEpochSecond(ZoneOffset.UTC),
        id
    );
  }

  public static AuthorizationCode authCodeFromString(String code) {
    String[] properties = code.split("##");
    LocalDateTime expiresAt = LocalDateTime.ofEpochSecond(Long.parseLong(properties[5]), 0, ZoneOffset.UTC);
    Collection<String> scopes = new HashSet<>(Arrays.asList(properties[4].split(" ")));
    return new AuthorizationCode(
        properties[0],
        properties[1],
        properties[2],
        properties[3],
        scopes,
        expiresAt,
        properties[6]
    );
  }
}
