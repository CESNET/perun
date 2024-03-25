package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class GroupRemovedFromResource extends AuditEvent {

  private Group group;
  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupRemovedFromResource() {
  }

  public GroupRemovedFromResource(Group group, Resource resource) {
    this.group = group;
    this.resource = resource;
    this.message = formatMessage("%s removed from %s", group, resource);
  }

  public Group getGroup() {
    return group;
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
