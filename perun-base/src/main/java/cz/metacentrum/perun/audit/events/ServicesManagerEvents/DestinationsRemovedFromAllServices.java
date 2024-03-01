package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;

public class DestinationsRemovedFromAllServices extends AuditEvent {

  private Facility facility;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public DestinationsRemovedFromAllServices() {
  }

  public DestinationsRemovedFromAllServices(Facility facility) {
    this.facility = facility;
    this.message = formatMessage("All destinations removed from %s for all services.", facility);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Facility getFacility() {
    return facility;
  }

  @Override
  public String toString() {
    return message;
  }
}
