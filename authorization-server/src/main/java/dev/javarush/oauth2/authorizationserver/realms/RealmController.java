package dev.javarush.oauth2.authorizationserver.realms;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("realms")
public class RealmController {

  private final RealmService service;

  public RealmController(RealmService service) {
    this.service = service;
  }

  @PostMapping
  public Realm create(@RequestBody Map<String, String> body) {
    String realmName = body.get("name");
    return this.service.createRealm(realmName);
  }

  @GetMapping
  public Iterable<Realm> getAll() {
    return this.service.getAllRealms();
  }

  @GetMapping("{id}")
  public Realm get(@PathVariable("id") String id) {
    return this.service.getRealm(id);
  }

  @GetMapping("{id}/jwk")
  public Map<String, Object> jwks (
          @PathVariable("id") String id
  ) {
    return this.service.getPublicKey(id);
  }
}
