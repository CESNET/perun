package cz.metacentrum.perun.audit.events.GeneralServiceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class FreeAllDenialsOnDestination extends AuditEvent {

  private int destinationId;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public FreeAllDenialsOnDestination() {
  }

  public FreeAllDenialsOnDestination(int destinationId) {
    this.destinationId = destinationId;
    this.message = formatMessage("free all denials: on %d.", destinationId);
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
