package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserCreated extends AuditEvent {

  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public UserCreated() {
  }

  public UserCreated(User user) {
    this.user = user;
    this.message = formatMessage("%s created.", user);
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
