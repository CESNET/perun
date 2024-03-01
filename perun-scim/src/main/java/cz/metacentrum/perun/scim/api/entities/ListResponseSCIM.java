package cz.metacentrum.perun.scim.api.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;

/**
 * ListResponse type containing list of some resources, number of resources and type of resource schema.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class ListResponseSCIM {

  @JsonProperty
  private Long totalResults;

  @JsonProperty
  private String schemas;

  @JsonProperty(value = "Resources")
  private List resources;

  public ListResponseSCIM(Long totalResults, String schemas, List resources) {
    this.totalResults = totalResults;
    this.schemas = schemas;
    this.resources = resources;
  }

  public ListResponseSCIM() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListResponseSCIM)) {
      return false;
    }

    ListResponseSCIM that = (ListResponseSCIM) o;

    if (getTotalResults() != null ? !getTotalResults().equals(that.getTotalResults()) :
        that.getTotalResults() != null) {
      return false;
    }
    if (getSchemas() != null ? !getSchemas().equals(that.getSchemas()) : that.getSchemas() != null) {
      return false;
    }
    return getResources() != null ? getResources().equals(that.getResources()) : that.getResources() == null;

  }

  public List getResources() {
    return resources;
  }

  public String getSchemas() {
    return schemas;
  }

  public Long getTotalResults() {
    return totalResults;
  }

  @Override
  public int hashCode() {
    int result = getTotalResults() != null ? getTotalResults().hashCode() : 0;
    result = 31 * result + (getSchemas() != null ? getSchemas().hashCode() : 0);
    result = 31 * result + (getResources() != null ? getResources().hashCode() : 0);
    return result;
  }

  public void setResources(List resources) {
    this.resources = resources;
  }

  public void setSchemas(String schemas) {
    this.schemas = schemas;
  }

  public void setTotalResults(Long totalResults) {
    this.totalResults = totalResults;
  }

  @Override
  public String toString() {
    return "ListResponseSCIM{" + "totalResults=" + totalResults + ", schemas='" + schemas + '\'' + ", resources=" +
           resources + '}';
  }
}
