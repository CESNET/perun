package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.Consent;

/**
 * Thrown when trying to retrieve a consent that doesn't exist from the database
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class ConsentNotExistsException extends EntityNotExistsException {
  static final long serialVersionUID = 0;

  private Consent consent;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ConsentNotExistsException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public ConsentNotExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public ConsentNotExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the consent that does not exist
   *
   * @param consent the consent
   */
  public ConsentNotExistsException(Consent consent) {
    super(consent.toString());
    this.consent = consent;
  }

  /**
   * Getter for the consent that doesn't exist
   *
   * @return the consent that doesn't exist
   */
  public Consent getConsent() {
    return this.consent;
  }
}
