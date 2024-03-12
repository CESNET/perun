package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserDeleted extends AuditEvent {

  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public UserDeleted() {
  }

  public UserDeleted(User user) {
    this.user = user;
    this.message = formatMessage("%s deleted.", user);
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
