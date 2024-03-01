package cz.metacentrum.perun.audit.events.GroupManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;

public class GroupSyncFailed extends AuditEvent implements EngineIgnoreEvent {

  private Group group;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public GroupSyncFailed() {
  }

  public GroupSyncFailed(Group group) {
    this.group = group;
    this.message = formatMessage("%s synchronization failed.", group);
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
