package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Resource;

public class AllGroupResourceAttributesRemovedForGroups extends AuditEvent implements EngineIgnoreEvent {

  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllGroupResourceAttributesRemovedForGroups() {
  }

  public AllGroupResourceAttributesRemovedForGroups(Resource resource) {
    this.resource = resource;
    this.message = formatMessage("All non-virtual group-resource attributes removed for all groups and %s.", resource);
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
