package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Resource;

public class ResourceUpdated extends AuditEvent {

  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ResourceUpdated() {
  }

  public ResourceUpdated(Resource resource) {
    this.resource = resource;
    this.message = formatMessage("%s updated.", resource);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Resource getResource() {
    return resource;
  }

  @Override
  public String toString() {
    return message;
  }
}
