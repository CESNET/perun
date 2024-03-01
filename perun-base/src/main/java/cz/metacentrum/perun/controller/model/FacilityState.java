package cz.metacentrum.perun.controller.model;

import cz.metacentrum.perun.core.api.Facility;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class used to interpret facility propagation state in GUI
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class FacilityState implements Comparable<FacilityState> {

  private Facility facility;

  private FacilityPropagationState state;
  private Map<String, FacilityPropagationState> results = new HashMap<String, FacilityPropagationState>();

  @Override
  public int compareTo(FacilityState facility) {
    if (facility == null || this.facility == null || this.facility.getName() == null) {
      throw new NullPointerException("Facility or facility name");
    }
    return this.facility.getName().compareTo(facility.getFacility().getName());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FacilityState other = (FacilityState) obj;
    if (facility == null) {
      if (other.facility != null) {
        return false;
      }
    } else if (!facility.equals(other.facility)) {
      return false;
    }
    return true;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public Map<String, FacilityPropagationState> getResults() {
    return results;
  }

  public void setResults(Map<String, FacilityPropagationState> results) {
    this.results = results;
  }

  public FacilityPropagationState getState() {
    return state;
  }

  public void setState(FacilityPropagationState state) {
    this.state = state;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((facility == null) ? 0 : facility.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "FacilityState [facility=" + facility + ", state=" + state + "]";
  }

  public static enum FacilityPropagationState {
    OK, ERROR, PROCESSING, NOT_DETERMINED
  }

}
