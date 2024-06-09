package dev.javarush.oauth2.authorizationserver.token;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("realms/{realmId}/token")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> handleRequest (
            @PathVariable("realmId") String realmId,
            @RequestParam Map<String, String> query,
            HttpServletResponse response
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

        // Set the header
        response.setHeader("Cache-Control", "no-store");
        return this.tokenService.generateAccessTokenResponse(tokenRequest);
    }
}
