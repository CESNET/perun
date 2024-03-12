package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Service;

public class ServiceCreated extends AuditEvent implements EngineIgnoreEvent {

  private Service service;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ServiceCreated() {
  }

  public ServiceCreated(Service service) {
    this.service = service;
    this.message = formatMessage("%s created.", service);
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
