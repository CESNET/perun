package cz.metacentrum.perun.core.api.exceptions;

/**
 * Exception thrown when IDs in passed API object do not match real state in Perun.
 *
 * @author Pavel Zlamal
 */
public class ObjectIDMismatchException extends PerunException {

  /**
   * Constructor without arguments
   */
  public ObjectIDMismatchException() {
  }

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ObjectIDMismatchException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public ObjectIDMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public ObjectIDMismatchException(Throwable cause) {
    super(cause);
  }
}
