package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Schema of the resource.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SchemaSCIM {

  @JsonProperty
  private String id;

  @JsonProperty
  private String name;

  @JsonProperty
  private String description;

  public SchemaSCIM(String id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public SchemaSCIM() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SchemaSCIM)) {
      return false;
    }

    SchemaSCIM that = (SchemaSCIM) o;

    if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
      return false;
    }
    if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
      return false;
    }
    return getDescription() != null ? getDescription().equals(that.getDescription()) : that.getDescription() == null;

  }

  public String getDescription() {
    return description;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    int result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + (getName() != null ? getName().hashCode() : 0);
    result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
    return result;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "SchemaSCIM{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", description='" + description + '\'' + '}';
  }
}
