package cz.metacentrum.perun.registrar.implApi;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import java.util.List;
import java.util.UUID;

/**
 * Handles invitation logic. Invitations are used for pre-approved applications sent to users via email.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public interface InvitationsManagerImplApi {

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
   * Get invitation object with the specified uuid.
   *
   * @param sess session
   * @param token uuid of the desired invitation
   * @return Invitation object with the specified uuid
   * @throws InvitationNotExistsException when invitation with this id does not exist
   */
  Invitation getInvitationByToken(PerunSession sess, UUID token) throws InvitationNotExistsException;

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
   * Creates new Invitation object.
   *
   * @param sess session
   * @param invitation invitation to create
   * @return created invitation
   */
  Invitation createInvitation(PerunSession sess, Invitation invitation);

  /**
   * Updates invitation status.
   *
   * @param sess session
   * @param invitation invitation to update
   * @param status new status
   */
  void setInvitationStatus(PerunSession sess, Invitation invitation, InvitationStatus status);
}
