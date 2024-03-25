package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Represents Facility with its attributes
 *
 * @author David Flor <davidflor@seznam.cz>
 */
public class FacilityWithAttributes {
  List<Attribute> attributes;
  private Facility facility;

  public FacilityWithAttributes(Facility facility, List<Attribute> attributes) {
    this.facility = facility;
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
    FacilityWithAttributes that = (FacilityWithAttributes) o;
    return getFacility().equals(that.getFacility());
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFacility());
  }

  @Override
  public String toString() {
    return "FacilityWithAttributes{" + "facility=" + facility + ", attributes=" + attributes + '}';
  }
}
