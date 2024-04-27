package dev.javarush.oauth2.authorizationserver.authorization.ui;

import dev.javarush.oauth2.authorizationserver.authorization.AuthRequest;
import dev.javarush.oauth2.authorizationserver.authorization.AuthorizationService;
import dev.javarush.oauth2.authorizationserver.client.Client;
import dev.javarush.oauth2.authorizationserver.client.ClientService;
import dev.javarush.oauth2.authorizationserver.realms.Realm;
import dev.javarush.oauth2.authorizationserver.realms.RealmService;
import dev.javarush.oauth2.authorizationserver.user.User;
import dev.javarush.oauth2.authorizationserver.user.UserNotFoundException;
import dev.javarush.oauth2.authorizationserver.user.UserNotLoggedInException;
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
import org.springframework.web.servlet.ModelAndView;

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
      Model model,
      HttpServletRequest request
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
    try {
      User user = this.userService.getLoggedInUser(request.getSession(), realmId);
      return presentAuthorizationInterface(realmId, model, authRequest, authRequestId, user);
    } catch (UserNotLoggedInException e) {
      String authActionURI = "/realms/" + realmId + "/auth/" + authRequestId + "/login";
      model.addAttribute("realm", realm);
      model.addAttribute("authActionURI", authActionURI);
      return "user-login";
    }
  }

  private String presentAuthorizationInterface(
      @PathVariable("realmId") String realmId, Model model,
      AuthRequest authRequest, String authRequestId, User user) {
    Client client = this.clientService.getById(realmId, authRequest.clientId());
    model.addAttribute("client", client);
    model.addAttribute("user", user);
    String denyActionURI = "/realms/" + realmId + "/auth/" + authRequestId + "/deny";
    String allowActionURI = "/realms/" + realmId + "/auth/" + authRequestId + "/allow";
    model.addAttribute("denyActionURI", denyActionURI);
    model.addAttribute("allowActionURI", allowActionURI);
    return "authorization-interface";
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
    return presentAuthorizationInterface(realmId, model, authRequest, authRequestId, user);
  }

  @GetMapping("{authRequestId}/deny")
  public ModelAndView handleAuthorizationDeny(
      @PathVariable("realmId") String realmId,
      @PathVariable("authRequestId") String authRequestId,
      HttpServletRequest request,
      Model model
  ) {
    var authRequest = this.authorizationService.getAuthRequestById (realmId, authRequestId);
    try {
      this.userService.getLoggedInUser(request.getSession(), realmId);
    } catch (UserNotLoggedInException e) {
      var query = this.authorizationService.getAuthRequestMap(authRequest);
      model.addAllAttributes(query);
      return new ModelAndView("redirect:/realms/" + realmId + "/auth", model.asMap());
    }
    this.authorizationService.denyAuthRequest(authRequest);
    model.addAttribute("error", "You are not authorized to access this resource");
    return new ModelAndView("error", model.asMap());
  }

  @GetMapping("{authRequestId}/allow")
  public ModelAndView handleAuthorizationAllow(
      @PathVariable("realmId") String realmId,
      @PathVariable("authRequestId") String authRequestId,
      HttpServletRequest request,
      Model model
  ) {
    var authRequest = this.authorizationService.getAuthRequestById (realmId, authRequestId);
    try {
      User loggedInUser = this.userService.getLoggedInUser(request.getSession(), realmId);
      String code = this.authorizationService.allowAuthRequest(authRequest, loggedInUser);
      model.addAttribute("code", code);
      model.addAttribute("code", code);
      model.addAttribute("state", authRequest.state());
      return new ModelAndView("redirect:" + authRequest.redirectUri(), model.asMap());
    } catch (UserNotLoggedInException e) {
      var query = this.authorizationService.getAuthRequestMap(authRequest);
      model.addAllAttributes(query);
      return new ModelAndView("redirect:/realms/" + realmId + "/auth", model.asMap());
    }
  }
}
