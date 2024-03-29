package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.audit.events.EngineIgnoreEvent;
import cz.metacentrum.perun.core.api.User;

public class UserAddedToOwnersOfSpecificUser extends AuditEvent implements EngineIgnoreEvent {

  private User user;
  private User specificUser;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public UserAddedToOwnersOfSpecificUser() {
  }

  public UserAddedToOwnersOfSpecificUser(User user, User specificUser) {
    this.user = user;
    this.specificUser = specificUser;
    this.message = formatMessage("%s was added to owners of %s.", user, specificUser);
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
