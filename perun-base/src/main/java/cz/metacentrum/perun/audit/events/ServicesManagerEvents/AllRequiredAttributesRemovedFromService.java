package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Service;

public class AllRequiredAttributesRemovedFromService extends AuditEvent implements EngineIgnoreEvent {

  private Service service;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllRequiredAttributesRemovedFromService() {
  }

  public AllRequiredAttributesRemovedFromService(Service service) {
    this.service = service;
    this.message = formatMessage("All required attributes removed from %s.", service);
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
