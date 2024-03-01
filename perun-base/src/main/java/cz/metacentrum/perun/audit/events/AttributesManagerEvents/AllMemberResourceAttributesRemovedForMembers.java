package cz.metacentrum.perun.audit.events.AttributesManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Resource;

public class AllMemberResourceAttributesRemovedForMembers extends AuditEvent implements EngineIgnoreEvent {

  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public AllMemberResourceAttributesRemovedForMembers() {
  }

  public AllMemberResourceAttributesRemovedForMembers(Resource resource) {
    this.resource = resource;
    this.message =
        formatMessage("All non-virtual member-resource attributes removed for all members and %s.", resource);
  }

  public Resource getResource() {
    return resource;
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
