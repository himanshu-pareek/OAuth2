package dev.javarush.oauth2.authorizationserver.scope;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("realms/{realmId}/scopes")
public class ScopeController {

    private final ScopeService scopeService;

    public ScopeController(ScopeService scopeService) {
        this.scopeService = scopeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@PathVariable("realmId") String realmId, @RequestBody Scope scope) {
        this.scopeService.createNewScope (realmId, scope);
    }
}
