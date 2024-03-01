package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * This exception is thrown when the synchronization for the group is not enabled
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class GroupSynchronizationNotEnabledException extends PerunException {

  private Group group;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public GroupSynchronizationNotEnabledException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public GroupSynchronizationNotEnabledException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public GroupSynchronizationNotEnabledException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the group
   *
   * @param group group for which the synchronization was not enabled
   */
  public GroupSynchronizationNotEnabledException(Group group) {
    super(group.toString());
    this.group = group;
  }

  /**
   * Getter for the group
   *
   * @return group for which the synchronization was not enabled
   */
  public Group getGroup() {
    return this.group;
  }
}
