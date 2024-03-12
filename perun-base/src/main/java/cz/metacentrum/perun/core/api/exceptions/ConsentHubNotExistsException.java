package cz.metacentrum.perun.core.api.exceptions;

import cz.metacentrum.perun.core.api.ConsentHub;

/**
 * Thrown when the Consent Hub has not been found in the database
 *
 * @author Jakub Hejda <Jakub.Hejda@cesnet.cz>
 */
public class ConsentHubNotExistsException extends EntityNotExistsException {

  static final long serialVersionUID = 0;

  private ConsentHub consentHub;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ConsentHubNotExistsException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public ConsentHubNotExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public ConsentHubNotExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the consent hub that does not exist
   *
   * @param consentHub the consent hub
   */
  public ConsentHubNotExistsException(ConsentHub consentHub) {
    super(consentHub.toString());
    this.consentHub = consentHub;
  }

  /**
   * Getter for the consent hub that does not exist
   *
   * @return the consent hub that does not exist
   */
  public ConsentHub getConsentHub() {
    return this.consentHub;
  }
}
