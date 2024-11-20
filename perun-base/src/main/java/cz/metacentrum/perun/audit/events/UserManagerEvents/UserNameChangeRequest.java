package cz.metacentrum.perun.audit.events.UserManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.User;

public class UserNameChangeRequest extends AuditEvent {
  private User user;
  private String titleBefore;
  private String firstName;
  private String middleName;
  private String lastName;
  private String titleAfter;

  private String message;

  public UserNameChangeRequest() {
  }

  public UserNameChangeRequest(User user, String titleBefore, String firstName, String middleName, String lastName,
                               String titleAfter) {
    this.user = user;
    this.titleBefore = titleBefore;
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
    this.titleAfter = titleAfter;
    this.message = formatMessage(
        "%s requested change of name to title before: '%s', first name: '%s', middle name: '%s', " +
            "last name: '%s', title after: '%s'.",
        user, titleBefore, firstName, middleName, lastName, titleAfter);
  }

  public User getUser() {
    return user;
  }

  public String getTitleBefore() {
    return titleBefore;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getTitleAfter() {
    return titleAfter;
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
