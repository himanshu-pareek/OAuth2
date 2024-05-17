package dev.javarush.oauth2.authorizationserver.scope;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class ScopeService {

    private static final Set<String> RESERVED_SCOPES = Set.of(
            "email",
            "profile",
            "openid"
    );
    private static final String PERMITTED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz.";

    private final ScopeRepository scopeRepository;

    public ScopeService(ScopeRepository scopeRepository) {
        this.scopeRepository = scopeRepository;
    }

    public Collection<Scope> getScopesByRealmId(String realmId) {
        return this.scopeRepository.findAllByRealmId(realmId);
    }

    public void createNewScope(String realmId, Scope scope) {
        if (!realmId.equals(scope.realmId())) {
            throw new InvalidScopeException("Realm id does not match");
        }
        validateScope(scope);
        int updatedRows = this.scopeRepository.insertScope (scope);
        if (updatedRows != 1) {
            throw new RuntimeException("Something went wrong.");
        }
    }

    private void validateScope (Scope scope) {
        if (scope.name().length() < 4) {
            throw new InvalidScopeException("name must have at least 4 characters.");
        }
        for (int i = 0; i < scope.name().length(); i++) {
            if (PERMITTED_CHARACTERS.indexOf(scope.name().charAt(i)) == -1) {
                throw new InvalidScopeException("Invalid scope " + scope.name());
            }
        }
        if (RESERVED_SCOPES.contains(scope.name())) {
            throw new InvalidScopeException("Invalid scope " + scope.name());
        }
        Long count = this.scopeRepository.countByRealmIdAndName(scope.realmId(), scope.name());
        if (count > 0) {
            throw new InvalidScopeException("Scope " + scope.name() + " already exists.");
        }
    }
}
