package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.Invitation;

/**
 * Exception thrown when invitation does not exist.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class InvitationNotExistsException extends PerunException {
  private Invitation invitation;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public InvitationNotExistsException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public InvitationNotExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public InvitationNotExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the invitation that does not exist
   *
   * @param invitation the invitation
   */
  public InvitationNotExistsException(Invitation invitation) {
    super(invitation.toString());
    this.invitation = invitation;
  }

  /**
   * Getter for the invitation that doesn't exist
   *
   * @return the invitation that doesn't exist
   */
  public Invitation getInvitation() {
    return this.invitation;
  }
}
