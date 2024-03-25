package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Service;

public class ServiceDeleted extends AuditEvent implements EngineIgnoreEvent {

  private Service service;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ServiceDeleted() {
  }

  public ServiceDeleted(Service service) {
    this.service = service;
    this.message = formatMessage("%s deleted.", service);
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
