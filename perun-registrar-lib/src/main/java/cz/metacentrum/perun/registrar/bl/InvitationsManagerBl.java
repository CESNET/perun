package cz.metacentrum.perun.registrar.bl;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.exceptions.InvitationAlreadyAssignedToAnApplicationException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import cz.metacentrum.perun.registrar.model.InvitationWithSender;
import cz.metacentrum.perun.registrar.model.InvitationsPageQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles invitation logic. Invitations are used for pre-approved applications sent to users via email.
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public interface InvitationsManagerBl {

  /**
   * Get invitation object associated with the application
   *
   * @param sess session
   * @param application application to get invitation for
   * @return invitation object or null if such invitation doesn't exist
   */
  Invitation getInvitationByApplication(PerunSession sess, Application application);

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
   * @param token uuid token of the desired invitation
   * @return Invitation object with the specified uuid
   * @throws InvitationNotExistsException when invitation with this uuid does not exist
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
   * Lists all invitations with a given status
   *
   * @param sess session
   * @return List of all invitations
   */
  List<Invitation> getAllInvitations(PerunSession sess, InvitationStatus status);

  /**
   * Creates new Invitation object - does not send it out or perform any other actions.
   *
   * @param sess session
   * @param invitation invitation to create
   * @return created invitation
   */
  Invitation createInvitation(PerunSession sess, Invitation invitation);

  /**
   * Builds the url to the registrar application form of the group/vo of the invitation associated with the passed
   * token.
   *
   * @param sess  session
   * @param token token of the invitation
   * @return the url
   */
  String createInvitationUrl(PerunSession sess, String token)
      throws InvitationNotExistsException;

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
   * @throws RegistrarException when email address format is incorrect
   * @return created Invitation object
   */
  Invitation inviteToGroup(PerunSession sess, Vo vo, Group group, String receiverName, String receiverEmail,
                           String language, LocalDate expiration, String redirectUrl) throws RegistrarException;

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
   */
  Map<String, String> inviteToGroupFromCsv(PerunSession sess, Vo vo, Group group, List<String> data, String language,
                                           LocalDate expiration, String redirectUrl);

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

  /**
   * Checks if an invitation given by the uuid exists and if it is in a pending state.
   * If yes return the invitation, otherwise throws an exception.
   *
   * @param sess session
   * @param uuid random token assigned to the invitation
   * @param group the group for which the invitation is to be used
   * @return the invitation
   * @throws InvitationNotExistsException invitation does not exist
   * @throws InvalidInvitationStatusException status is other than pending
   */
  Invitation canInvitationBeAccepted(PerunSession sess, UUID uuid, Group group)
      throws InvalidInvitationStatusException, InvitationNotExistsException,
                 InvitationAlreadyAssignedToAnApplicationException;

  /**
   * Extend the invitation date on the expiration to some later date.
   *
   * @param invitation invitation to be extended
   * @param newExpirationDate of the invitation, +1 month if null
   * @throws InvalidInvitationStatusException when invitation is not PENDING
   */
  Invitation extendInvitationExpiration(PerunSession sess, Invitation invitation, LocalDate newExpirationDate)
      throws InvalidInvitationStatusException;

  /**
   * Get page of invitations for the given group.
   *
   * @param sess  session
   * @param group group
   * @param query query with page information
   * @return page of invitations with sender's information
   */
  Paginated<InvitationWithSender> getInvitationsPage(PerunSession sess, Group group, InvitationsPageQuery query);
}
