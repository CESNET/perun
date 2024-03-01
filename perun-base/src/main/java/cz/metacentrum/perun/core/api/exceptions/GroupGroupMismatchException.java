package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Group;

/**
 * Exception thrown when groups are not in the same VO, when required so.
 *
 * @author David Flor
 */
public class GroupGroupMismatchException extends PerunException {

  private Group sourceGroup;
  private Group destinationGroup;

  /**
   * Constructor with no arguments
   */
  public GroupGroupMismatchException() {
  }

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public GroupGroupMismatchException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public GroupGroupMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public GroupGroupMismatchException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with a message, group to be moved, destination group (the group to which the moving group is moved)
   *
   * @param message          message with the details
   * @param sourceGroup      group to be moved
   * @param destinationGroup the group to which the moving group is moved
   */
  public GroupGroupMismatchException(String message, Group sourceGroup, Group destinationGroup) {
    super(message);
    this.sourceGroup = sourceGroup;
    this.destinationGroup = destinationGroup;
  }

  public Group getSourceGroup() {
    return sourceGroup;
  }

  public Group getDestinationGroup() {
    return destinationGroup;
  }
}
