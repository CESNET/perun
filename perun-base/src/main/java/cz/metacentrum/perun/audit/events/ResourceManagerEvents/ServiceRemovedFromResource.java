package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;

public class ServiceRemovedFromResource extends AuditEvent {

  private Service service;
  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ServiceRemovedFromResource() {
  }

  public ServiceRemovedFromResource(Service service, Resource resource) {
    this.service = service;
    this.resource = resource;
    this.message = formatMessage("%s removed from %s", service, resource);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Service getService() {
    return service;
  }

  public Resource getResource() {
    return resource;
  }

  @Override
  public String toString() {
    return message;
  }
}
