package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Class representing Resource object with its attributes
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class EnrichedResource {
  private Resource resource;
  private List<Attribute> attributes;

  public EnrichedResource() {
  }

  public EnrichedResource(Resource resource, List<Attribute> attributes) {
    this.resource = resource;
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedResource that = (EnrichedResource) o;
    return Objects.equals(getResource(), that.getResource());
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getResource());
  }

  @Override
  public String toString() {
    return "EnrichedResource{" + "resource=" + resource + ", attributes=" + attributes + '}';
  }
}
