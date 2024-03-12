package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class DestinationsRemovedFromService extends AuditEvent {

  private Facility facility;
  private Service service;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public DestinationsRemovedFromService() {
  }

  public DestinationsRemovedFromService(Service service, Facility facility) {
    this.facility = facility;
    this.service = service;
    this.message = formatMessage("All destinations removed from %s and %s.", service, facility);
  }

  public Facility getFacility() {
    return facility;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Service getService() {
    return service;
  }

  @Override
  public String toString() {
    return message;
  }
}
