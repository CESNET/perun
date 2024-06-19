package cz.metacentrum.perun.registrar.bl;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.model.Invitation;
import java.util.List;

/**
 * Handles invitation logic. Invitations are used for pre-approved applications sent to users via email.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public interface InvitationsManagerBl {

  /**
   * Get invitation object with the specified id.
   *
   * @param sess session
   * @param id id of the desired invitation
   * @return Invitation object with the specified id
   * @throws InvitationNotExistsException when invitation with this id does not exist
   */
  Invitation getInvitationById(PerunSession sess, int id) throws InvitationNotExistsException;

  /**
   * Lists all invitations made by the specified user to the specified group.
   *
   * @param sess session
   * @param group group within which to look for invitations
   * @param user sender
   * @return List of all invitations send by the sender within the group
   */
  List<Invitation> getInvitationsForSender(PerunSession sess, Group group, User user);

  /**
   * Lists all invitations to the specified group.
   *
   * @param sess session
   * @param group group within which to look for invitations
   * @return List of all invitations to the group
   */
  List<Invitation> getInvitationsForGroup(PerunSession sess, Group group);

  /**
   * Lists all invitations to groups within the specified Vo.
   *
   * @param sess session
   * @param vo vo within which to look for invitations
   * @return List of all invitations to groups of the specified Vo
   */
  List<Invitation> getInvitationsForVo(PerunSession sess, Vo vo);

  /**
   * Creates new Invitation object - does not send it out or perform any other actions.
   *
   * @param sess session
   * @param invitation invitation to create
   * @return created invitation
   */
  Invitation createInvitation(PerunSession sess, Invitation invitation);

  /**
   * Checks whether the transition is allowed and expires invitation.
   *
   * @param sess session
   * @param invitation invitation to expire
   * @return invitation with updated status
   * @throws InvalidInvitationStatusException transition is not allowed from the current invitation status
   */
  Invitation expireInvitation(PerunSession sess, Invitation invitation) throws InvalidInvitationStatusException;

  /**
   * Checks whether the transition is allowed and revokes invitation.
   *
   * @param sess session
   * @param invitation invitation to revoke
   * @return invitation with updated status
   * @throws InvalidInvitationStatusException transition is not allowed from the current invitation status
   */
  Invitation revokeInvitation(PerunSession sess, Invitation invitation) throws InvalidInvitationStatusException;

  /**
   * Checks whether the transition is allowed and marks invitation as accepted.
   *
   * @param sess session
   * @param invitation invitation to accept
   * @return invitation with updated status
   * @throws InvalidInvitationStatusException transition is not allowed from the current invitation status
   */
  Invitation markInvitationAccepted(PerunSession sess, Invitation invitation) throws InvalidInvitationStatusException;
}
