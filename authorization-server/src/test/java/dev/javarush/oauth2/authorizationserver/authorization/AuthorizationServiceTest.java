package dev.javarush.oauth2.authorizationserver.authorization;

import static org.junit.jupiter.api.Assertions.*;

import dev.javarush.oauth2.authorizationserver.client.ClientRepository;
import dev.javarush.oauth2.authorizationserver.realms.RealmRepository;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

class AuthorizationServiceTest {

  private static final String ENCODED_KEY = "/wuJXhw/t5RkxKGAInZaZSo4fDug6zlBGWGe8PJalY0=";

  @MockBean
  private RealmRepository realmRepository;
  @MockBean
  private ClientRepository clientRepository;
  @MockBean
  private AuthRequestRepository authRequestRepository;

  private AuthorizationService authorizationService;

  @BeforeEach
  public void setUp() {
    authorizationService = new AuthorizationService(realmRepository, clientRepository, authRequestRepository);
    authorizationService.setAuthCodeAesSecretKey(ENCODED_KEY);
  }

  @Test
  void testEncodeDecodeAuthCode() {
    AuthorizationCode authorizationCode = AuthorizationCode.authorizationCode(
        "realm1",
        "client1",
        "http://redirect-uri.com",
        "username1"
    );
    String code = authorizationService.encodeAuthorizationCode(authorizationCode);
    System.out.println("Code - " + code);
    AuthorizationCode decodedCode = authorizationService.decodeAuthorizationCode(code);
    assertEquals(authorizationCode.id(), decodedCode.id());
    assertEquals(authorizationCode.redirectURI(), decodedCode.redirectURI());
    assertEquals(authorizationCode.username(), decodedCode.username());
    assertEquals(authorizationCode.clientId(), decodedCode.clientId());
    assertEquals(authorizationCode.realmId(), decodedCode.realmId());
    assertEquals(
        authorizationCode.expiresAt().toEpochSecond(ZoneOffset.UTC),
        decodedCode.expiresAt().toEpochSecond(ZoneOffset.UTC)
    );
  }

}