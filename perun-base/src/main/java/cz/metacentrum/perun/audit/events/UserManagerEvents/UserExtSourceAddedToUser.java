package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;

public class UserExtSourceAddedToUser extends AuditEvent {

  private UserExtSource userExtSource;
  private User user;
  private String message;

  @SuppressWarnings("unused") // used by jackson mapper
  public UserExtSourceAddedToUser() {
  }

  public UserExtSourceAddedToUser(UserExtSource userExtSource, User user) {
    this.user = user;
    this.userExtSource = userExtSource;
    this.message = formatMessage("%s added to %s.", userExtSource, user);
  }

  @Override
  public String getMessage() {
    return message;
  }

  public UserExtSource getUserExtSource() {
    return userExtSource;
  }

  public User getUser() {
    return user;
  }

  @Override
  public String toString() {
    return message;
  }
}
