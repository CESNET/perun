package cz.metacentrum.perun.audit.events.ServicesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.ServicesPackage;

public class ServicesPackageUpdated extends AuditEvent implements EngineIgnoreEvent {

  private ServicesPackage servicesPackage;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ServicesPackageUpdated() {
  }

  public ServicesPackageUpdated(ServicesPackage servicesPackage) {
    this.servicesPackage = servicesPackage;
    this.message = formatMessage("%s updated.", servicesPackage);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public ServicesPackage getServicesPackage() {
    return servicesPackage;
  }

  @Override
  public String toString() {
    return message;
  }
}
