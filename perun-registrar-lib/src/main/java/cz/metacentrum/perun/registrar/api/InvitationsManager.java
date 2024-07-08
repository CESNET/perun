package cz.metacentrum.perun.registrar.api;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.exceptions.InvitationAlreadyAssignedToAnApplicationException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Invitation;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

  // TODO determine whether to add this to all layers + RPC/openapi
  String createInvitationUrl(PerunSession sess, String authentication, String token)
      throws PrivilegeException, InvitationNotExistsException;

  /**
   * Creates invitation based on the passed parameters, generates the UUID token, creates invitation link to the group
   * application form with the token as parameter and sends it to the receiver's email. Optionally a redirect url can be
   * passed, which the user will be redirected to after filling out the form.
   * Should an error occur in the process, the created invitation is set to the UNSENT state.
   *
   * @param sess session
   * @param vo vo of the group
   * @param group group to be invited to
   * @param receiverName receiver's name
   * @param receiverEmail receiver's email
   * @param language language of the invitation
   * @param expiration expiration of the invitation link
   * @param redirectUrl optional redirect url to redirect to upon filling out the form
   * @return created Invitation object
   * @throws PrivilegeException insufficient rights
   * @throws GroupNotExistsException group does not exist
   * @throws VoNotExistsException vo does not exist
   * @throws RegistrarException when email address format is incorrect
   */
  Invitation inviteToGroup(PerunSession sess, Vo vo, Group group, String receiverName, String receiverEmail,
                           String language, LocalDate expiration, String redirectUrl) throws PrivilegeException,
                                                                                             GroupNotExistsException,
                                                                                             VoNotExistsException,
                                                                                             RegistrarException;

  /**
   * Creates invitations based on the CSV parameters, for each generates the UUID token, creates invitation link
   * to the group application form with the token as parameter and sends it to the receiver's email.
   * Optionally a redirect url can be passed, which the user will be redirected to after filling out the form.
   * Should an error occur in the process, the created invitation is set to the UNSENT state.
   * <p>
   * Expected format: `receiverEmail;receiverName\n`
   * @param sess session
   * @param vo vo of the group
   * @param group group to be invited to
   * @param data CSV data
   * @param language language of the invitations
   * @param expiration expiration of the invitation link
   * @param redirectUrl optional redirect url to redirect to upon filling out the form
   * @return Map containing the results. The key is name and email of receiver, value is either 'OK' or 'ERROR' with the
   *  error message
   * @throws GroupNotExistsException group does not exist
   * @throws VoNotExistsException vo does not exist
   * @throws PrivilegeException insufficient rights
   */
  Map<String, String> inviteToGroupFromCsv(PerunSession sess, Vo vo, Group group, List<String> data, String language,
                                           LocalDate expiration, String redirectUrl) throws GroupNotExistsException,
                                                                      VoNotExistsException, PrivilegeException;

  /**
   * Checks if an invitation given by the uuid exists and if it is in a pending state. Throws exception otherwise.
   *
   * @param sess session
   * @param uuid random token assigned to the invitation
   * @throws InvitationNotExistsException invitation does not exist
   * @throws InvalidInvitationStatusException status is other than pending
   * @throws PrivilegeException insufficient rights
   */
  void canInvitationBeAccepted(PerunSession sess, UUID uuid)
      throws PrivilegeException, InvalidInvitationStatusException, InvitationNotExistsException,
                 InvitationAlreadyAssignedToAnApplicationException;
}
