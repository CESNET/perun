package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when it is not possible to set a given role.
 */
public class RoleCannotBeSetException extends PerunException {
  static final long serialVersionUID = 0;

  private String role;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public RoleCannotBeSetException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and the role
   *
   * @param message message with details about the cause
   * @param role    role that is not supported
   */
  public RoleCannotBeSetException(String message, String role) {
    super(message);
    this.role = role;
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public RoleCannotBeSetException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a message, the role and a throwable
   *
   * @param message message with details about the cause
   * @param role    role that is not supported
   * @param cause   Throwable that caused throwing of this exception
   */
  public RoleCannotBeSetException(String message, String role, Throwable cause) {
    super(message, cause);
    this.role = role;
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public RoleCannotBeSetException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the role and a throwable
   *
   * @param role  role that is not supported
   * @param cause Throwable that caused throwing of this exception
   */
  public RoleCannotBeSetException(Throwable cause, String role) {
    super(cause);
    this.role = role;
  }

  /**
   * Getter for the role
   *
   * @return role that is not supported
   */
  public String getRole() {
    return this.role;
  }
}
