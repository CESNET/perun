package cz.metacentrum.perun.audit.events.ResourceManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;

public class GroupAssignedToResource extends AuditEvent {

  private Group group;
  private Resource resource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupAssignedToResource() {
  }

  public GroupAssignedToResource(Group group, Resource resource) {
    this.group = group;
    this.resource = resource;
    this.message = formatMessage("%s assigned to %s", group, resource);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Group getGroup() {
    return group;
  }

  public Resource getResource() {
    return resource;
  }

  @Override
  public String toString() {
    return message;
  }
}
