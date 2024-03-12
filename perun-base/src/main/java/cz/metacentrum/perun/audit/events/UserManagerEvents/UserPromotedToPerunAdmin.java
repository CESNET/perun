package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserPromotedToPerunAdmin extends AuditEvent {

  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public UserPromotedToPerunAdmin() {
  }

  public UserPromotedToPerunAdmin(User user) {
    this.user = user;
    this.message = formatMessage("%s was promoted to PERUNADMIN.", user);
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
