package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Objects;

/**
 * Represents facility with its owners, destinations and hosts.
 *
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class EnrichedFacility {
  private Facility facility;
  private List<Owner> owners;
  private List<Destination> destinations;
  private List<Host> hosts;

  public EnrichedFacility() {
  }

  public EnrichedFacility(Facility facility, List<Owner> owners, List<Destination> destinations, List<Host> hosts) {
    this.facility = facility;
    this.owners = owners;
    this.destinations = destinations;
    this.hosts = hosts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrichedFacility that = (EnrichedFacility) o;
    return Objects.equals(getFacility(), that.getFacility());
  }

  public List<Destination> getDestinations() {
    return destinations;
  }

  public void setDestinations(List<Destination> destinations) {
    this.destinations = destinations;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public List<Host> getHosts() {
    return hosts;
  }

  public void setHosts(List<Host> hosts) {
    this.hosts = hosts;
  }

  @Deprecated
  public List<Owner> getOwners() {
    return owners;
  }

  @Deprecated
  public void setOwners(List<Owner> owners) {
    this.owners = owners;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFacility());
  }

  @Override
  public String toString() {
    return "EnrichedFacility{" + "facility=" + facility + ", owners=" + owners + ", destinations=" + destinations +
           ", hosts=" + hosts + '}';
  }
}
