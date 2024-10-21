package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class LastSpecificUserOwnerRemoved extends AuditEvent {
  private User specificUser;
  private String message;

  @SuppressWarnings("unused")
  public LastSpecificUserOwnerRemoved() {
  }

  public LastSpecificUserOwnerRemoved(User specificUser) {
    this.specificUser = specificUser;
    this.message = formatMessage("Last owner was removed from specific user %s", specificUser);
  }

  public User getSpecificuser() {
    return specificUser;
  }


  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return message;
  }
}
