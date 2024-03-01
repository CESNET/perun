package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Service;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class BanServiceOnDestination extends AuditEvent {

  private Service service;
  private int destinationId;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public BanServiceOnDestination() {
  }

  public BanServiceOnDestination(Service service, int destinationId) {
    this.service = service;
    this.destinationId = destinationId;
    this.message = formatMessage("ban : %s on %s.", service, destinationId);
  }

  public int getDestinationId() {
    return destinationId;
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
