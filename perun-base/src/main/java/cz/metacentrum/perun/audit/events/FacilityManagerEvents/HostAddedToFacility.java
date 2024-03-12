package cz.metacentrum.perun.audit.events.FacilityManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Host;

public class HostAddedToFacility extends AuditEvent {

  private Host host;
  private Facility facility;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public HostAddedToFacility() {
  }

  public HostAddedToFacility(Host host, Facility facility) {
    this.facility = facility;
    this.host = host;
    this.message = formatMessage("%s added to %s.", host, facility);
  }

  public Facility getFacility() {
    return facility;
  }

  public Host getHost() {
    return host;
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
