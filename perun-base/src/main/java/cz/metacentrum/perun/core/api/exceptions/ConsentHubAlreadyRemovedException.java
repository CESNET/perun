package cz.metacentrum.perun.core.api.exceptions;

/**
 * Thrown when trying to removed consent hub which is not in a database.
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class ConsentHubAlreadyRemovedException extends PerunException {
  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ConsentHubAlreadyRemovedException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public ConsentHubAlreadyRemovedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public ConsentHubAlreadyRemovedException(Throwable cause) {
    super(cause);
  }

}

