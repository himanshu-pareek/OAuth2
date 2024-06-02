package dev.javarush.oauth2.authorizationserver.token;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("realms/{realmId}/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void handleRequest (
            @PathVariable("realmId") String realmId,
            @RequestParam Map<String, String> query
    ) {
        var tokenRequest = new TokenRequest(
                realmId,
                query.get("client_id"),
                query.get("client_secret"),
                query.get("grant_type"),
                query.get("code"),
                query.get("redirect_uri")
        );
        this.tokenService.verifyTokenRequest (tokenRequest);

        // Now is the hard part - Create access_token and send it

    }
}
