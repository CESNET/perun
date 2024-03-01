package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupSyncFinished extends AuditEvent implements EngineIgnoreEvent {

  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupSyncFinished() {
  }

  public GroupSyncFinished(Group group) {
    this.group = group;
    this.message = formatMessage("Group synchronization for %s has been finished.", group);
  }

  public GroupSyncFinished(Group group, long startTime, long endTime) {
    this.group = group;
    String duration = String.valueOf(endTime - startTime);
    this.message = formatMessage("Group synchronization for %s has been finished in %s nano seconds.", group, duration);
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
