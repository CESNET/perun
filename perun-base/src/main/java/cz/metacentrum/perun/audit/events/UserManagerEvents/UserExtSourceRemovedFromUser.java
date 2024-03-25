package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceRemovedFromUser extends AuditEvent {

  private User user;
  private UserExtSource userExtSource;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public UserExtSourceRemovedFromUser() {
  }

  public UserExtSourceRemovedFromUser(UserExtSource userExtSource, User user) {
    this.userExtSource = userExtSource;
    this.user = user;
    this.message = formatMessage("%s removed from %s.", userExtSource, user);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public User getUser() {
    return user;
  }

  public UserExtSource getUserExtSource() {
    return userExtSource;
  }

  @Override
  public String toString() {
    return message;
  }
}
