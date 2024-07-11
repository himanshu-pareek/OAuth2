package dev.javarush.oauth2.confidentialclient;

import jakarta.servlet.http.HttpServletRequest;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jdk.jshell.execution.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class OAuth2ClientController {

  private static final Logger logger = LoggerFactory.getLogger(OAuth2ClientController.class);

  @Value("${oauth2.auth-server.auth-endpoint}")
  private String authorizationEndpoint;

  @Value("${oauth2.client.id}")
  private String clientId;

  @Value("${oauth2.client.redirect-uri}")
  private String redirectUri;

  @Value("${oauth2.auth-server.token-endpoint}")
  private String tokenEndpoint;

  @Value("${oauth2.client.secret}")
  private String clientSecret;

  @Value("${oauth2.resource-server.contacts-endpoint}")
  private String contactsEndpoint;

  private final RestTemplate template;

  public OAuth2ClientController(RestTemplate template) {
    this.template = template;
  }

  @GetMapping
  public String index(
      HttpServletRequest request,
      Model model
  ) {
    Object accessTokenObj = request.getSession().getAttribute("access_token");
    if (accessTokenObj == null) {
      return "index";
    }
    String accessToken = (String) accessTokenObj;

    model.addAttribute("accessToken", accessToken);

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.put("Authorization", Collections.singletonList(String.format(
        "Bearer %s",
        accessToken
    )));

    List<Map<String, Object>> contacts = template.exchange(
            contactsEndpoint,
            HttpMethod.GET,
            new HttpEntity<>(
                    null,
                    headers
            ),
            new ParameterizedTypeReference<List<Map<String, Object>>>() {
            }
    ).getBody();
    model.addAttribute("contacts", contacts);

    return "index";
  }

  @GetMapping("login")
  public RedirectView login(
      HttpServletRequest request
  ) {
    String state = Utils.generateRandomString(16);
    request.getSession().setAttribute("state", state);
    request.getSession().removeAttribute("access_token");
    String authorizationUrl = String.format(
        "%s?client_id=%s&response_type=code&state=%s&redirect_uri=%s&scope=email profile openid contact.read contact.write",
        authorizationEndpoint,
        clientId,
        state,
        redirectUri
    );

    String codeVerifier = Utils.generateSecureRandomString (50);
    try {
      String codeChallenge = Utils.sha256Hash(codeVerifier);
      request.getSession().setAttribute("code_verifier", codeVerifier);
      authorizationUrl += String.format("&code_challenge=%s", codeChallenge);
      authorizationUrl += "&code_challenge_method=S256";
    } catch (NoSuchAlgorithmException e) {
      logger.warn("Not using PKCE due to {}", e.getMessage());
    }

    RedirectView redirectView = new RedirectView();
    redirectView.setUrl(authorizationUrl);
    return redirectView;
  }

  @GetMapping("oauth/callback")
  public String authCallback(
      @RequestParam Map<String, String> query,
      HttpServletRequest request,
      Model model
  ) {
    String code = query.get("code");
    String state = query.get("state");
    if (code == null) {
      model.addAttribute("error", "Something went wrong. Try again.");
      return "index";
    }


    if (state == null || !state.equals(request.getSession().getAttribute("state"))) {
      model.addAttribute("error", "State does not match. Try again.");
      return "index";
    }

    // Get Access Token
    String accessTokenUrl = String.format(
        "%s?grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
        tokenEndpoint,
        code,
        redirectUri,
        clientId,
        clientSecret
    );

    Object codeVerifier = request.getSession().getAttribute("code_verifier");
    if (codeVerifier != null) {
      accessTokenUrl += String.format("&code_verifier=%s", codeVerifier);
    }

    Map<String, Object> tokenResponse = template.postForObject(
        accessTokenUrl,
        null,
        Map.class,
        Collections.emptyMap()
    );
    String accessToken = (String) tokenResponse.get("access_token");
    request.getSession().setAttribute("access_token", accessToken);
    return "redirect:/";
  }
}
