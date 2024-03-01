package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Consent;

/**
 * This exception is thrown when trying to create a consent
 * with the same data that is already in database.
 */
public class ConsentExistsException extends PerunException {
  static final long serialVersionUID = 0;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ConsentExistsException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public ConsentExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public ConsentExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with consent
   *
   * @param consent consent
   */
  public ConsentExistsException(Consent consent) {
    super(consent.toString());

  }
}
