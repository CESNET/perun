package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupMoved extends AuditEvent {

  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupMoved() {
  }

  public GroupMoved(Group group) {
    this.group = group;
    this.message = formatMessage("%s was moved.", group);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Group getGroup() {
    return group;
  }

  @Override
  public String toString() {
    return message;
  }
}
