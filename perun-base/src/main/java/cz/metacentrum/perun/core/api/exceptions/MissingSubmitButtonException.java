package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when application form doesn't contain submit or auto-submit button when it should
 *
 * @author Michal Berky
 */
public class MissingSubmitButtonException extends PerunException {
  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public MissingSubmitButtonException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public MissingSubmitButtonException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public MissingSubmitButtonException(Throwable cause) {
    super(cause);
  }
}
