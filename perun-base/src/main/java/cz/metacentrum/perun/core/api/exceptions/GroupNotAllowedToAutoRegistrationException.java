package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * This exception is thrown when group is not allowed to be added to auto registration.
 *
 * @author Metodej Klang
 */
public class GroupNotAllowedToAutoRegistrationException extends PerunException {

  private Group group;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public GroupNotAllowedToAutoRegistrationException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public GroupNotAllowedToAutoRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with the group that is not allowed to be added to auto registration
   *
   * @param group the group
   */
  public GroupNotAllowedToAutoRegistrationException(String message, Group group) {
    super(message + " " + group.toString());
    this.group = group;
  }

  public Group getGroup() {
    return group;
  }
}
