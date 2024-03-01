package cz.metacentrum.perun.core.api.exceptions;


/**
 * Thrown when application form contains multiple application form items of such type that maximum 1 is allowed.
 *
 * @author Johana Supikova
 */
public class MultipleApplicationFormItemsException extends PerunException {
  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public MultipleApplicationFormItemsException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public MultipleApplicationFormItemsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public MultipleApplicationFormItemsException(Throwable cause) {
    super(cause);
  }
}
