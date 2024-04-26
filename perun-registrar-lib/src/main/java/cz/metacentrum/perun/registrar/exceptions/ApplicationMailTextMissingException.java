package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

/**
 * Thrown when application mail is being updated with no complete message for any locale
 */
public class ApplicationMailTextMissingException extends PerunException {
  private ApplicationMail applicationMail;

  public ApplicationMailTextMissingException(String message) {
    super(message);
  }

  public ApplicationMailTextMissingException(String message, ApplicationMail mail) {
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
