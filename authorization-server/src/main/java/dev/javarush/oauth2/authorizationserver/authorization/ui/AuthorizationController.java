package dev.javarush.oauth2.authorizationserver.authorization.ui;

import dev.javarush.oauth2.authorizationserver.authorization.AuthRequest;
import dev.javarush.oauth2.authorizationserver.authorization.AuthorizationService;
import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientService;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmService;
import dev.javarush.oauth2.authorizationserver.user.User;
import dev.javarush.oauth2.authorizationserver.user.UserNotFoundException;
import dev.javarush.oauth2.authorizationserver.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("realms/{realmId}/auth")
public class AuthorizationController {
  private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

  private final AuthorizationService authorizationService;
  private final UserService userService;
  private final RealmService realmService;
  private final ClientService clientService;

  public AuthorizationController(AuthorizationService authorizationService, UserService userService, RealmService realmService,
                                 ClientService clientService) {
    this.authorizationService = authorizationService;
    this.userService = userService;
    this.realmService = realmService;
    this.clientService = clientService;
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
    logger.info("Authorization request: {}", authRequest);
    Realm realm = this.authorizationService.verifyAuthRequest(authRequest);
    String authRequestId = this.authorizationService.getAuthRequestId (authRequest);
    String authActionURI = "/realms/" + realmId + "/auth/" + authRequestId + "/login";
    model.addAttribute("realm", realm);
    model.addAttribute("authActionURI", authActionURI);
    return "user-login";
  }

  @PostMapping("{authRequestId}/login")
  public String handleLogin(
      @PathVariable("realmId") String realmId,
      @PathVariable("authRequestId") String authRequestId,
      @RequestParam("username") String username,
      @RequestParam("password") String password,
      HttpServletRequest request,
      Model model
  ) {
    AuthRequest authRequest = this.authorizationService.getAuthRequestById (realmId, authRequestId);
    User user;
    try {
      user = this.userService.validateUser (username, password, realmId);
    } catch (UserNotFoundException e) {
      String authActionURI = "/realms/" + realmId + "/auth/" + authRequestId + "/login";
      model.addAttribute("realm", this.realmService.getRealm(realmId));
      model.addAttribute("authActionURI", authActionURI);
      model.addAttribute("error", e.getMessage());
      return "user-login";
    }
    this.userService.saveSession(user, realmId, request.getSession());
    Client client = this.clientService.getById(realmId, authRequest.clientId());
    model.addAttribute("client", client);
    model.addAttribute("user", user);
    return "authorization-interface";
  }
}
