package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a list of facilities with joint consent management.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class ConsentHub extends Auditable {

  private String name;
  private boolean enforceConsents = true;
  private List<Facility> facilities;

  public ConsentHub() {
  }

  public ConsentHub(int id, String name, boolean enforceConsents, List<Facility> facilities) {
    super(id);
    this.name = name;
    this.enforceConsents = enforceConsents;
    this.facilities = facilities;
  }

  public ConsentHub(int id, String createdAt, String createdBy, String modifiedAt, String modifiedBy,
                    Integer createdByUid, Integer modifiedByUid, String name, boolean enforceConsents,
                    List<Facility> facilities) {
    super(id, createdAt, createdBy, modifiedAt, modifiedBy, createdByUid, modifiedByUid);
    this.name = name;
    this.enforceConsents = enforceConsents;
    this.facilities = facilities;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isEnforceConsents() {
    return enforceConsents;
  }

  public void setEnforceConsents(boolean enforceConsents) {
    this.enforceConsents = enforceConsents;
  }

  public List<Facility> getFacilities() {
    return facilities;
  }

  public void setFacilities(List<Facility> facilities) {
    this.facilities = facilities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ConsentHub that = (ConsentHub) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), name);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();

    return str.append(
        "ConsentHub:[id='").append(getId()).append(
        "', name='").append(name).append(
        "', enforceConsents='").append(enforceConsents).append(
        "', facilities='").append(facilities).append(
        "']").toString();
  }

  @Override
  public String serializeToString() {
    StringBuilder str = new StringBuilder();

    List<String> facilities = new ArrayList<>();
    String facilitiesString;

    if (getFacilities() == null) {
      facilitiesString = "\\0";
    } else {
      for (Facility facility : getFacilities()) {
        facilities.add(facility.serializeToString());
      }
      facilitiesString = facilities.toString();
    }
    return str.append(this.getClass().getSimpleName()).append(":[").append(
        "id=<").append(getId()).append(">").append(
        ", name=<").append(getName() == null ? "\\0" : BeansUtils.createEscaping(getName())).append(">").append(
        ", enforceConsents=<").append(isEnforceConsents()).append(">").append(
        ", facilities=<").append(facilitiesString).append(">").append(
        ']').toString();
  }
}
