package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Authentication Schemes for ServiceProviderConfigs endpoint.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

public class AuthenticationSchemes {

  @JsonProperty
  private String name;

  @JsonProperty
  private String description;

  public AuthenticationSchemes(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public AuthenticationSchemes() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AuthenticationSchemes)) {
      return false;
    }

    AuthenticationSchemes that = (AuthenticationSchemes) o;

    if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
      return false;
    }
    return getDescription() != null ? getDescription().equals(that.getDescription()) : that.getDescription() == null;

  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    int result = getName() != null ? getName().hashCode() : 0;
    result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
    return result;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "AuthenticationSchemes{" + "name='" + name + '\'' + ", description='" + description + '\'' + '}';
  }
}
