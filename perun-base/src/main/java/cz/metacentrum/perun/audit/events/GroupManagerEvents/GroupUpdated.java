package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupUpdated extends AuditEvent {

  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupUpdated() {
  }

  public GroupUpdated(Group group) {
    this.group = group;
    this.message = formatMessage("%s updated.", group);
  }

  public Group getGroup() {
    return group;
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
