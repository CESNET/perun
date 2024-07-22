package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.Invitation;

/**
 * Exception thrown when trying to set an application id to an invitation with one already set.
 *
 * @author Rastislav Kruták <492918@mail.muni.cz>
 */
public class InvitationAlreadyAssignedToAnApplicationException extends PerunException {
  private Invitation invitation;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public InvitationAlreadyAssignedToAnApplicationException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and Throwable object
   *
   * @param message message with details about the cause
   * @param cause   Throwable that caused throwing of this exception
   */
  public InvitationAlreadyAssignedToAnApplicationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor with a Throwable object
   *
   * @param cause Throwable that caused throwing of this exception
   */
  public InvitationAlreadyAssignedToAnApplicationException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor with the invitation that does not have proper status
   *
   * @param invitation the invitation
   */
  public InvitationAlreadyAssignedToAnApplicationException(Invitation invitation) {
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
