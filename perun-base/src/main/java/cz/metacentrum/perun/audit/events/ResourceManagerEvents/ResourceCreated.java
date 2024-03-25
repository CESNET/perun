package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Resource;

public class ResourceCreated extends AuditEvent implements EngineIgnoreEvent {

  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public ResourceCreated() {
  }

  public ResourceCreated(Resource resource) {
    this.resource = resource;
    this.message = formatMessage("%s created.", resource);
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
