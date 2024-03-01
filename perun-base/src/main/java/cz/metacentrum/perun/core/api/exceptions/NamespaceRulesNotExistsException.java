package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when trying to get a namespace rules which does not exist in the
 * LoginNamespacesRulesConfigContainer
 *
 * @author Peter Balčirák
 */
public class NamespaceRulesNotExistsException extends PerunException {

  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public NamespaceRulesNotExistsException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public NamespaceRulesNotExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public NamespaceRulesNotExistsException(Throwable cause) {
    super(cause);
  }
}
