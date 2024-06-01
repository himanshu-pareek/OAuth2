package dev.javarush.oauth2.confidentialclient;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${oauth2.auth-server.user-endpoint}")
  private String userEndpoint;

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

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.put("Authorization", Collections.singletonList(String.format(
        "Bearer %s",
        accessToken
    )));

    Map<String, Object> user = template.exchange(
        userEndpoint,
        HttpMethod.GET,
        new HttpEntity<>(
            null,
            headers
        ),
        Map.class
    ).getBody();
    model.addAttribute("user", user);

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
        "%s?grant_type=code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
        tokenEndpoint,
        code,
        redirectUri,
        clientId,
        clientSecret
    );

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
