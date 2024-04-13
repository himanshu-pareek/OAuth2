package dev.javarush.oauth2.authorizationserver.authorization.ui;

import dev.javarush.oauth2.authorizationserver.authorization.AuthRequest;
import dev.javarush.oauth2.authorizationserver.authorization.AuthorizationService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("realms/{realmId}/auth")
public class AuthorizationController {
  private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

  private final AuthorizationService authorizationService;

  public AuthorizationController(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @GetMapping
  public String handleAuthRequest(
      @PathVariable("realmId") String realmId,
      @RequestParam Map<String, String> query,
      Model model
  ) {
    AuthRequest authRequest = new AuthRequest(
        realmId,
        query.get("client_id"),
        query.get("redirect_uri"),
        query.get("response_type"),
        query.get("scope"),
        query.get("state")
    );
    this.authorizationService.verifyAuthRequest(authRequest);
    String authRequestId = this.authorizationService.getAuthRequestId (authRequest);
    String authActionURI = "/realms/" + realmId + "/auth/" + authRequestId;
    model.addAttribute("authActionURI", authActionURI);
    return "user-login";
  }
}
