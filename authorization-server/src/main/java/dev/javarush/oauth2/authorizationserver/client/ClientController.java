package dev.javarush.oauth2.authorizationserver.client;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("realms/{realmId}/clients")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @PostMapping
  public Client create(@PathVariable("realmId") String realmId, @RequestBody Client client) {
    return this.clientService.createClient(realmId, client);
  }

  @GetMapping
  public Iterable<Client> getAll(@PathVariable("realmId") String realmId) {
    return this.clientService.getAll(realmId);
  }

  @GetMapping("{id}")
  public Client get(@PathVariable("realmId") String realmId, @PathVariable("id") String clientId) {
    return this.clientService.getById(realmId, clientId);
  }

  @PostMapping("{id}/secrets")
  public ClientSecret generateSecret(@PathVariable("id") String clientId) {
    return this.clientService.generateClientSecret(clientId);
  }

  @DeleteMapping("{id}/secrets/{secretId}")
  public void deleteClientSecret(@PathVariable("secretId") Integer secretId) {
    this.clientService.deleteClientSecret(secretId);
  }
}
