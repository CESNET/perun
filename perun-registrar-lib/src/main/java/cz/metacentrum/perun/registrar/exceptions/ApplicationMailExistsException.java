package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

/**
 * Application mail exists but something tries to bring it into existence again.
 *
 * @author Metodej Klang
 */
public class ApplicationMailExistsException extends PerunException {

  private ApplicationMail applicationMail;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ApplicationMailExistsException(String message) {
    super(message);
  }

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ApplicationMailExistsException(String message, ApplicationMail mail) {
    super(message + "- Mail with id " + mail.getId() + " and template " + mail.getMailType());
    applicationMail = mail;
  }

  /**
   * Getter for the mail
   *
   * @return the mail that already exists
   */
  public ApplicationMail getApplicationMail() {
    return applicationMail;
  }
}
