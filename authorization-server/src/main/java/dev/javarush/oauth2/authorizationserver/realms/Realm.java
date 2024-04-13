package dev.javarush.oauth2.authorizationserver.realms;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Table("realms")
public class Realm implements Persistable<String> {
  @Id
  private String id;
  private String name;

  @Transient
  private boolean isCreate;

  public Realm(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setCreate(boolean create) {
    isCreate = create;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  @Transient
  public boolean isNew() {
    return isCreate;
  }
}