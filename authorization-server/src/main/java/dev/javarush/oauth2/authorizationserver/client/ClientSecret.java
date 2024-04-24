package dev.javarush.oauth2.authorizationserver.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("client_secrets")
@JsonIgnoreProperties({"salt"})
public class ClientSecret {
  @Id
  private Integer id;
  private String clientId;
  private String secret;
  private String salt;

  public ClientSecret(Integer id, String clientId, String secret) {
    this.id = id;
    this.clientId = clientId;
    this.secret = secret;
  }

  public String getClientId() {
    return clientId;
  }

  public Integer getId() {
    return id;
  }

  public String getSecret() {
    return secret;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }
}
