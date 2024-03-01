package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class FreeDenialServiceOnDestination extends AuditEvent {

  private Service service;
  private int destinationId;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public FreeDenialServiceOnDestination() {
  }

  public FreeDenialServiceOnDestination(Service service, int destinationId) {
    this.service = service;
    this.destinationId = destinationId;
    this.message = formatMessage("free denial: %s on %s.", service, destinationId);
  }

  public Service getService() {
    return service;
  }

  public int getDestinationId() {
    return destinationId;
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
