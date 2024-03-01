package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to get a role management rules which does not exist in the PerunPoliciesContainer
 *
 * @author Peter Balčirák
 */
public class RoleManagementRulesNotExistsException extends PerunException {
  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public RoleManagementRulesNotExistsException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public RoleManagementRulesNotExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public RoleManagementRulesNotExistsException(Throwable cause) {
    super(cause);
  }
}
