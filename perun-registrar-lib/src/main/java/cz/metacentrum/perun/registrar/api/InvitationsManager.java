package cz.metacentrum.perun.registrar.api;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.model.Invitation;
import java.util.List;

/**
 * Handles invitations logic.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public interface InvitationsManager {

  /**
   * Get invitation object with the specified id.
   *
   * @param sess session
   * @param id id of the desired invitation
   * @return Invitation object with the specified id
   * @throws InvitationNotExistsException when invitation with this id does not exist
   * @throws PrivilegeException insufficient rights
   */
  Invitation getInvitationById(PerunSession sess, int id) throws InvitationNotExistsException, PrivilegeException;

  /**
   * Lists all invitations made by the specified user to the specified group.
   *
   * @param sess session
   * @param group group within which to look for invitations
   * @param user sender
   * @return List of all invitations send by the sender within the group
   * @throws GroupNotExistsException group does not exist
   * @throws UserNotExistsException user does not exist
   * @throws PrivilegeException insufficient rights
   */
  List<Invitation> getInvitationsForSender(PerunSession sess, Group group, User user)
      throws GroupNotExistsException, PrivilegeException, UserNotExistsException;

  /**
   * Lists all invitations to the specified group.
   *
   * @param sess session
   * @param group group within which to look for invitations
   * @return List of all invitations to the group
   * @throws GroupNotExistsException group does not exist
   * @throws PrivilegeException insufficient rights
   */
  List<Invitation> getInvitationsForGroup(PerunSession sess, Group group)
      throws GroupNotExistsException, PrivilegeException;

  /**
   * Lists all invitations to groups within the specified Vo.
   *
   * @param sess session
   * @param vo vo within which to look for invitations
   * @return List of all invitations to groups of the specified Vo
   * @throws VoNotExistsException vo does not exist
   * @throws PrivilegeException insufficient rights
   */
  List<Invitation> getInvitationsForVo(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException;

  /**
   * Creates new Invitation object - does not send it out or perform any other actions.
   *
   * @param sess session
   * @param invitation invitation to create
   * @return created invitation
   * @throws PrivilegeException insufficient rights
   * @throws GroupNotExistsException group does not exist
   * @throws VoNotExistsException vo does not exist
   */
  Invitation createInvitation(PerunSession sess, Invitation invitation)
      throws PrivilegeException, GroupNotExistsException, VoNotExistsException;
}
