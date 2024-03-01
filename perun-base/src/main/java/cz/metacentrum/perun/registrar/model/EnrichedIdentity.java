package cz.metacentrum.perun.registrar.model;

import cz.metacentrum.perun.core.api.EnrichedExtSource;
import java.util.List;
import java.util.Objects;

/**
 * Class represents user enriched identity for Consolidator purposes.
 *
 * @author Lucie Kureckova <luckureckova@gmail.com>
 */
public class EnrichedIdentity {
  private int id;
  private String name;
  private String organization;
  private String email;
  private List<EnrichedExtSource> identities;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<EnrichedExtSource> getIdentities() {
    return identities;
  }

  public void setIdentities(List<EnrichedExtSource> identities) {
    this.identities = identities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedIdentity that = (EnrichedIdentity) o;
    return id == that.id && Objects.equals(name, that.name) && Objects.equals(organization, that.organization) &&
        Objects.equals(email, that.email) && Objects.equals(identities, that.identities);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, organization, email, identities);
  }

  @Override
  public String toString() {
    return "EnrichedIdentity{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", organization='" + organization + '\'' +
        ", email='" + email + '\'' +
        ", identities=" + identities +
        '}';
  }
}
