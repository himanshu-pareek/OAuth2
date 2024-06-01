package dev.javarush.oauth2.authorizationserver.token;

import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientService;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TokenService {

    private final RealmService realmService;
    private final ClientService clientService;

    public TokenService(RealmService realmService, ClientService clientService) {
        this.realmService = realmService;
        this.clientService = clientService;
    }

    public void verifyTokenRequest(TokenRequest tokenRequest) {
        verifyRealm (tokenRequest.realmId());
        verifyClient (tokenRequest);
    }

    private void verifyClient(TokenRequest tokenRequest) {
        if (tokenRequest.clientId() == null || !StringUtils.hasText(tokenRequest.clientId())) {
            throw new InvalidTokenRequestException("client_id is required");
        }
        Client client = this.clientService.getById(
                tokenRequest.realmId(),
                tokenRequest.clientId()
        );
        if (client == null) {
            throw new InvalidTokenRequestInvalidClientException ();
        }
        this.clientService.validateClientSecret (
                tokenRequest.clientId(),
                tokenRequest.clientSecret()
        );
    }

    private void verifyRealm(String realmId) {
        Realm realm = this.realmService.getRealm(realmId);
        if (realm == null) {
            throw new InvalidTokenRequestException ("Invalid realm id");
        }
    }
}
