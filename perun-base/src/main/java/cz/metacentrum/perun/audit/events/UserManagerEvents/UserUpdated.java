package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserUpdated extends AuditEvent {

  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public UserUpdated() {
  }

  public UserUpdated(User user) {
    this.user = user;
    this.message = formatMessage("%s updated.", user);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public User getUser() {
    return user;
  }

  @Override
  public String toString() {
    return message;
  }
}
