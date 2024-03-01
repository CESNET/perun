package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;

public class DestinationAddedToServiceAndFacility extends AuditEvent {

  private Destination destination;
  private Service service;
  private Facility facility;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public DestinationAddedToServiceAndFacility() {
  }

  public DestinationAddedToServiceAndFacility(Destination destination, Service service, Facility facility) {
    this.destination = destination;
    this.facility = facility;
    this.service = service;
    this.message = formatMessage("%s added to %s and %s.", destination, service, facility);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Destination getDestination() {
    return destination;
  }

  public Service getService() {
    return service;
  }

  public Facility getFacility() {
    return facility;
  }

  @Override
  public String toString() {
    return message;
  }
}
