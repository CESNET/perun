package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.User;

public class OwnershipDisabledForSpecificUser extends AuditEvent implements EngineIgnoreEvent {

  private User user;
  private User specificUser;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public OwnershipDisabledForSpecificUser() {
  }

  public OwnershipDisabledForSpecificUser(User user, User specificUser) {
    this.user = user;
    this.specificUser = specificUser;
    this.message = formatMessage("%s ownership was disabled for specificUser %s.", user, specificUser);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public User getSpecificUser() {
    return specificUser;
  }

  public User getUser() {
    return user;
  }

  @Override
  public String toString() {
    return message;
  }
}
