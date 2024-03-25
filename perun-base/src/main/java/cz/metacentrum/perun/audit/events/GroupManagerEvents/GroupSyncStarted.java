package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupSyncStarted extends AuditEvent implements EngineIgnoreEvent {

  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupSyncStarted() {
  }

  public GroupSyncStarted(Group group) {
    this.group = group;
    this.message = formatMessage("Group synchronization for %s has been started.", group);
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
