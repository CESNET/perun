package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ConsentHub;

/**
 * Thrown when trying to create consent hub with name that already exists.
 *
 * @author Johana Supikova <xsupikov@fi.muni.cz>
 */
public class ConsentHubExistsException extends EntityExistsException {
  static final long serialVersionUID = 0;

  private ConsentHub consentHub;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ConsentHubExistsException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public ConsentHubExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public ConsentHubExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the consentHub
   *
   * @param consentHub consentHub that already exists
   */
  public ConsentHubExistsException(ConsentHub consentHub) {
    super(consentHub.toString());
    this.consentHub = consentHub;
  }

  /**
   * Getter for the consentHub
   *
   * @return consentHub that already exists
   */
  public ConsentHub getConsentHub() {
    return this.consentHub;
  }
}
