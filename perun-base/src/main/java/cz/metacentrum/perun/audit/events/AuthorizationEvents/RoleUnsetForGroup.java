package cz.metacentrum.perun.audit.events.AuthorizationEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunBean;

public class RoleUnsetForGroup extends AuditEvent implements EngineIgnoreEvent {

  private PerunBean object;
  private Group group;
  private String role;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public RoleUnsetForGroup() {
  }

  public RoleUnsetForGroup(PerunBean object, Group group, String role) {
    this.object = object;
    this.group = group;
    this.role = role.toUpperCase();
    this.message = formatMessage("Role %s was unset for %s on %s.", role.toUpperCase(), group, object);
  }

  public Group getGroup() {
    return group;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public PerunBean getObject() {
    return object;
  }

  public String getRole() {
    return role;
  }

  @Override
  public String toString() {
    return message;
  }
}
