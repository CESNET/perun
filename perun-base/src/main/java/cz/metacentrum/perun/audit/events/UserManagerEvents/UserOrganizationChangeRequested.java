package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserOrganizationChangeRequested extends AuditEvent {
  private User user;
  private String organization;
  private String message;

  public UserOrganizationChangeRequested() {
  }

  public UserOrganizationChangeRequested(User user, String organization) {
    this.user = user;
    this.organization = organization;
    this.message = formatMessage("%s requested change of organization to '%s'.", user, organization);
  }

  public User getUser() {
    return user;
  }

  public String getOrganization() {
    return organization;
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
