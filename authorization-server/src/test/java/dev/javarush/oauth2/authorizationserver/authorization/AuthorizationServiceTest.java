package dev.javarush.oauth2.authorizationserver.authorization;

import static org.junit.jupiter.api.Assertions.*;

import dev.javarush.oauth2.authorizationserver.client.ClientRepository;
import dev.javarush.oauth2.authorizationserver.realms.RealmRepository;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import dev.javarush.oauth2.authorizationserver.scope.ScopeService;
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
  @MockBean
  private ScopeService scopeService;

  private AuthorizationService authorizationService;

  @BeforeEach
  public void setUp() {
    authorizationService = new AuthorizationService(realmRepository, clientRepository, authRequestRepository, scopeService);
    authorizationService.setAuthCodeAesSecretKey(ENCODED_KEY);
  }

  @Test
  void testEncodeDecodeAuthCode() {
    AuthorizationCode authorizationCode = AuthorizationCode.authorizationCode(
        "realm1",
        "client1",
        "http://redirect-uri.com",
        "username1",
            List.of("email", "profile", "openid", "contact.read", "contact.write"),
            "abcd",
            "S256"
    );
    String code = authorizationService.encodeAuthorizationCode(authorizationCode);
    AuthorizationCode decodedCode = authorizationService.decodeAuthorizationCode(code);
    assertEquals(authorizationCode, decodedCode); 
  }

}