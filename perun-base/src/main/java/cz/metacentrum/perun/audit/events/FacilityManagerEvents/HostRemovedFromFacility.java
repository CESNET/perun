package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

public class HostRemovedFromFacility extends AuditEvent {

  private Host host;
  private Facility facility;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public HostRemovedFromFacility() {
  }

  public HostRemovedFromFacility(Host host, Facility facility) {
    this.host = host;
    this.facility = facility;
    this.message = formatMessage("%s removed from %s.", host, facility);
  }

  public Host getHost() {
    return host;
  }

  public Facility getFacility() {
    return facility;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return message;
  }
}
