package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

/**
 * Application mail does not exist.
 *
 * @author Metodej Klang
 */
public class ApplicationMailNotExistsException extends PerunException {

  private ApplicationMail applicationMail;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ApplicationMailNotExistsException(String message) {
    super(message);
  }

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ApplicationMailNotExistsException(String message, ApplicationMail mail) {
    super(message + "- Mail with id " + mail.getId() + " and template " + mail.getMailType());
    applicationMail = mail;
  }

  /**
   * Getter for the mail
   *
   * @return the mail that does not exist
   */
  public ApplicationMail getApplicationMail() {
    return applicationMail;
  }
}
