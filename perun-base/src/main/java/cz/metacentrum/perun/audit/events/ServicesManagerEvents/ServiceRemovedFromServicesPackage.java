package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServiceRemovedFromServicesPackage extends AuditEvent implements EngineIgnoreEvent {

  private Service service;
  private ServicesPackage servicesPackage;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ServiceRemovedFromServicesPackage() {
  }

  public ServiceRemovedFromServicesPackage(Service service, ServicesPackage servicesPackage) {
    this.service = service;
    this.servicesPackage = servicesPackage;
    this.message = formatMessage("%s removed from %s.", service, servicesPackage);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Service getService() {
    return service;
  }

  public ServicesPackage getServicesPackage() {
    return servicesPackage;
  }

  @Override
  public String toString() {
    return message;
  }
}
