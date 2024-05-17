package dev.javarush.oauth2.authorizationserver.scope;

import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class ScopeRepository {

    private final JdbcClient jdbcClient;

    public ScopeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Collection<Scope> findAllByRealmId(String realmId) {
        return this.jdbcClient.sql("SELECT name, description FROM realm_scopes WHERE realm_id = :realmId")
                .param("realmId", realmId)
                .query((rs, row) -> new Scope(
                        realmId,
                        rs.getString("name"),
                        rs.getString("description"),
                        false
                ))
                .set();
    }

    public Long countByRealmIdAndName(String realmId, String name) {
        return (Long) this.jdbcClient.sql("SELECT COUNT(*) AS cnt FROM realm_scopes WHERE realm_id = :realmId AND name = :name LIMIT 1")
                .param("realmId", realmId)
                .param("name", name)
                .query()
                .singleValue();
    }

    public int insertScope(Scope scope) {
        return this.jdbcClient.sql("INSERT INTO realm_scopes (realm_id, name, description) VALUES (:realmId, :name, :description)")
                .param("realmId", scope.realmId())
                .param("name", scope.name())
                .param("description", scope.description())
                .update();
    }
}
