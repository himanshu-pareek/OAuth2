package dev.javarush.oauth2.authorizationserver.authorization;

import dev.javarush.oauth2.authorizationserver.util.Strings;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record AuthorizationCode(
    String realmId,
    String clientId,
    String redirectURI,
    String username,
    LocalDateTime expiresAt,
    String id
) {
  public static AuthorizationCode authorizationCode(
      String realmId,
      String clientId,
      String redirectURI,
      String username
  ) {
    return new AuthorizationCode(
        realmId,
        clientId,
        redirectURI,
        username,
        LocalDateTime.now().plusSeconds(30),
        Strings.generateRandomString(32)
    );
  }

  @Override
  public String toString() {
    return String.format(
        "%s##%s##%s##%s##%d##%s",
        realmId,
        clientId,
        redirectURI,
        username,
        expiresAt.toEpochSecond(ZoneOffset.UTC),
        id
    );
  }

  public static AuthorizationCode authCodeFromString(String code) {
    String[] properties = code.split("##");
    LocalDateTime expiresAt = LocalDateTime.ofEpochSecond(Long.parseLong(properties[4]), 0, ZoneOffset.UTC);
    return new AuthorizationCode(
        properties[0],
        properties[1],
        properties[2],
        properties[3],
        expiresAt,
        properties[5]
    );
  }
}
