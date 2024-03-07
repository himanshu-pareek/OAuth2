package dev.javarush.oauth2.authorizationserver.client;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table("clients")
public class Client implements Persistable<String> {
  @Id private String id;
  private String realmId;
  private String name;
  private boolean isConfidential;
  private String iconUrl;
  private String homePageUrl;
  private String description;
  private String privacyPolicyUrl;
  private String signInRedirectUris;
  private String signOutRedirectUris;
  private String webOrigins;

  @Transient
  private boolean isCreate;

  public void setCreate(boolean create) {
    isCreate = create;
  }

  public Client(String id, String realmId, String name, boolean isConfidential, String iconUrl, String homePageUrl, String description,
                String privacyPolicyUrl, String signInRedirectUris, String signOutRedirectUris, String webOrigins) {
    this.id = id;
    this.realmId = realmId;
    this.name = name;
    this.isConfidential = isConfidential;
    this.iconUrl = iconUrl;
    this.homePageUrl = homePageUrl;
    this.description = description;
    this.privacyPolicyUrl = privacyPolicyUrl;
    this.signInRedirectUris = signInRedirectUris;
    this.signOutRedirectUris = signOutRedirectUris;
    this.webOrigins = webOrigins;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean isNew() {
    return isCreate;
  }

  public void setRealmId(String realmId) {
    this.realmId = realmId;
  }

  public String getRealmId() {
    return realmId;
  }

  public String getName() {
    return name;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getHomePageUrl() {
    return homePageUrl;
  }

  public String getDescription() {
    return description;
  }

  public String getPrivacyPolicyUrl() {
    return privacyPolicyUrl;
  }

  public String getSignInRedirectUris() {
    return signInRedirectUris;
  }

  public String getSignOutRedirectUris() {
    return signOutRedirectUris;
  }

  public String getWebOrigins() {
    return webOrigins;
  }

  public boolean isCreate() {
    return isCreate;
  }

  public boolean isConfidential() {
    return isConfidential;
  }
}
