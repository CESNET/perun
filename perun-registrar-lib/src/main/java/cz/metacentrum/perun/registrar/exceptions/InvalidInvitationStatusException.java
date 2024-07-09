package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.Invitation;

/**
 * Exception thrown when invitation does not have proper status.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class InvalidInvitationStatusException extends PerunException {
  private Invitation invitation;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public InvalidInvitationStatusException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public InvalidInvitationStatusException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public InvalidInvitationStatusException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the invitation that does not have proper status
   *
   * @param invitation the invitation
   */
  public InvalidInvitationStatusException(Invitation invitation) {
    super(invitation.toString());
    this.invitation = invitation;
  }

  /**
   * Getter for the invitation that does not have proper status
   *
   * @return the invitation that does not have proper status
   */
  public Invitation getInvitation() {
    return this.invitation;
  }
}
