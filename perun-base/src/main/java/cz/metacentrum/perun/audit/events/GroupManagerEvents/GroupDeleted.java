package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupDeleted extends AuditEvent {

  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupDeleted() {
  }

  public GroupDeleted(Group group) {
    this.group = group;
    this.message = formatMessage("%s deleted.", group);
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
