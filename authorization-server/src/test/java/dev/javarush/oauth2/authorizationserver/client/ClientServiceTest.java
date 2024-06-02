package dev.javarush.oauth2.authorizationserver.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientSecretRepository clientSecretRepository;

    private ClientService clientService;

    @Test
    void validateClientSecret() {
        String clientId = "ab1xdnohwbfn";
        ClientSecret clientSecret = new ClientSecret(
                1,
                clientId,
                "jQp0Sgrr8L+LK48r2Syjw/J+ve/v2QppQbjCjftBlBtgv2T7EnflqLfB65Mv7VnuYInhCygFOJdVSNpmiWEyhA=="
        );
        clientSecret.setSalt("j+vrGM/PO0CpbSSJk5GQaWQDj19PsPRJg9lTXO30g0I=");
        when(this.clientSecretRepository.findByClientId(clientId))
                .thenReturn(List.of(
                        clientSecret
                ));

        this.clientService = new ClientService(this.clientRepository, this.clientSecretRepository);

        boolean valid = this.clientService.validateClientSecret(clientId, "1A4541AAAB6C5967BB58F2D96451FB220A2FF75160C41531DE8CCBD60566362B381BCB803F9279EED3F32FAECF76DD3D82027ED59FA1093FFFBA640EE6A3AE7140340D0DE0905E1E97BBEB2E8D0FEC5E84CFA76FE171DB17DFA78BEEA74BA3381DAA70B89BA211B942C32B771F04C34464831B64EE167FC7ECDF52C2B9467A6490FF9249D972AEC20BE9624C927665E55FF8C0308B3F6C219C6A7B0D18CEA9BC8EAC989CBC9FEFE8A5770E44E9387D82EE0BF563FBF532689099A806F76460E2B978D8398D87EA6666C0B8A782F82454611A446D88973E5F20AF211F30B9D011E6FA4D84504B377A4ECAA26D219306752DFEDA12934734C17EC103ACB0F926BF");
        assertTrue(valid);
    }
}