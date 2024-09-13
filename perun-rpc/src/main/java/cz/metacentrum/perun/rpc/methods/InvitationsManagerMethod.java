package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationsPageQuery;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public enum InvitationsManagerMethod implements ManagerMethod {

  /*#
   * Get invitation object associated with the application. Return null if such invitation doesn't exist.
   *
   * @param appId int <code>id</code> of application to send notification for
   * @throw PrivilegeException insufficient rights
   */
  getInvitationByApplication {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getInvitationsManager().getInvitationByApplication(ac.getSession(), ac.getApplicationById(
          parms.readInt("appId")
      ));
    }
  },

  /*#
   * Returns invitation by ID
   *
   * @param sess session
   * @param invitation id of the invitation
   * @throw InvitationNotExistsException invitation does not exist
   * @throw PrivilegeException insufficient rights
   */
  getInvitationById {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getInvitationsManager().getInvitationById(ac.getSession(), parms.readInt("invitation"));
    }
  },

  getInvitationsForSender {
    @Override
    public List<Invitation> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  },

  getInvitationsForGroup {
    @Override
    public List<Invitation> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  },

  getInvitationsForVo {
    @Override
    public List<Invitation> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  },

  //TODO most likely delete this method and use better ways to submit invitation, wanted to try RPC
  createInvitation {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
    }
  },

  /*#
   * Send invitation link to end user's email. The link leads to the application form of the group, when filled out a
   * pre-approved application is created and associated with the invitation. Pre-approved applications bypass the
   * application process and always are automatically approved.
   *
   * @param vo int VO <code>id</code>
   * @param group int Group <code>id</code>
   * @param receiverName String Name of the receiving user
   * @param receiverEmail String Email of the receiving user
   * @param language String Language to be used in invitation, should match available locales of the instance
   * @param expiration String Last date, when the expiration link is valid, yyyy-mm-dd format.
   * @throw PrivilegeException Insufficient rights
   * @throw GroupNotExistsException Group does not exist
   * @throw VoNotExistsException VO does not exist
   */
  /*#
   * Send invitation link to end user's email. The link leads to the application form of the group, when filled out a
   * pre-approved application is created and associated with the invitation. Pre-approved applications bypass the
   * application process and always are automatically approved.
   *
   * @param vo int VO <code>id</code>
   * @param group int Group <code>id</code>
   * @param receiverName String Name of the receiving user
   * @param receiverEmail String Email of the receiving user
   * @param language String Language to be used in invitation, should match available locales of the instance
   * @throw PrivilegeException Insufficient rights
   * @throw GroupNotExistsException Group does not exist
   * @throw VoNotExistsException VO does not exist
   */
  /*#
   * Send invitation link to end user's email. The link leads to the application form of the group, when filled out a
   * pre-approved application is created and associated with the invitation. Pre-approved applications bypass the
   * application process and always are automatically approved.
   *
   * @param vo int VO <code>id</code>
   * @param group int Group <code>id</code>
   * @param receiverName String Name of the receiving user
   * @param receiverEmail String Email of the receiving user
   * @param language String Language to be used in invitation, should match available locales of the instance
   * @param expiration String Last date, when the expiration link is valid, yyyy-mm-dd format.
   * @param redirectUrl String Url to redirect to after filling out the application.
   * @throw PrivilegeException Insufficient rights
   * @throw GroupNotExistsException Group does not exist
   * @throw VoNotExistsException VO does not exist
   * @throw RegistrarException when email address format is incorrect
   */
  /*#
   * Send invitation link to end user's email. The link leads to the application form of the group, when filled out a
   * pre-approved application is created and associated with the invitation. Pre-approved applications bypass the
   * application process and always are automatically approved.
   *
   * @param vo int VO <code>id</code>
   * @param group int Group <code>id</code>
   * @param receiverName String Name of the receiving user
   * @param receiverEmail String Email of the receiving user
   * @param language String Language to be used in invitation, should match available locales of the instance
   * @param redirectUrl String Url to redirect to after filling out the application.
   * @throw PrivilegeException Insufficient rights
   * @throw GroupNotExistsException Group does not exist
   * @throw VoNotExistsException VO does not exist
   * @throw RegistrarException when email address format is incorrect
   */
  inviteToGroup {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      String redirectUrl = "";
      if (parms.contains("redirectUrl")) {
        redirectUrl = parms.readString("redirectUrl");
      }
      LocalDate expiration = parms.contains("expiration") ? parms.readLocalDate("expiration") : null;
      return ac.getInvitationsManager().inviteToGroup(ac.getSession(), ac.getVoById(parms.readInt("vo")),
          ac.getGroupById(parms.readInt("group")), parms.readString("receiverName"),
          parms.readString("receiverEmail"), parms.readString("language"),
          expiration, redirectUrl);
    }
  },

  /*#
   * Creates invitations based on the CSV parameters, for each generates the UUID token, creates invitation link
   * to the group application form with the token as parameter and sends it to the receiver's email.
   * Optionally a redirect url can be passed, which the user will be redirected to after filling out the form.
   * Should an error occur in the process, the created invitation is set to the UNSENT state.
   * <p>
   * Expected format: `receiverName;receiverEmail\n`
   * @param vo int VO <code>id</code>
   * @param group int Group <code>id</code>
   * @param invitationData csv file values separated by semicolon ';'. Only [name; email] is valid format.
   * @exampleParam invitationData ["user2;mail2@mail.cz"]
   * @param language String Language to be used in invitation, should match available locales of the instance
   * @param expiration String Last date, when the expiration link is valid, yyyy-mm-dd format.
   * @throw GroupNotExistsException group does not exist
   * @throw VoNotExistsException vo does not exist
   * @throw PrivilegeException insufficient rights
   */
  /*#
   * Creates invitations based on the CSV parameters, for each generates the UUID token, creates invitation link
   * to the group application form with the token as parameter and sends it to the receiver's email.
   * Optionally a redirect url can be passed, which the user will be redirected to after filling out the form.
   * Should an error occur in the process, the created invitation is set to the UNSENT state.
   * <p>
   * Expected format: `receiverName;receiverEmail\n`
   * @param vo int VO <code>id</code>
   * @param group int Group <code>id</code>
   * @param invitationData csv file values separated by semicolon ';'. Only [name; email] is valid format.
   * @exampleParam invitationData ["user2;mail2@mail.cz"]
   * @param language String Language to be used in invitation, should match available locales of the instance
   * @param expiration String Last date, when the expiration link is valid, yyyy-mm-dd format.
   * @param redirectUrl String Url to redirect to after filling out the application.
   * @throw GroupNotExistsException group does not exist
   * @throw VoNotExistsException vo does not exist
   * @throw PrivilegeException insufficient rights
   */
  inviteToGroupFromCsv {
    @Override
    public Map<String, String> call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      String redirectUrl = "";
      if (parms.contains("redirectUrl")) {
        redirectUrl = parms.readString("redirectUrl");
      }
      return ac.getInvitationsManager().inviteToGroupFromCsv(ac.getSession(), ac.getVoById(parms.readInt("vo")),
          ac.getGroupById(parms.readInt("group")), parms.readList("invitationData", String.class),
          parms.readString("language"), parms.readLocalDate("expiration"), redirectUrl);
    }
  },

  /*#
   * Checks if an invitation given by the uuid exists and if it is in a pending state.
   * If yes return the invitation, otherwise throws an exception.
   *
   * @param sess session
   * @param uuid random token assigned to the invitation
   * @param group the id of the group for which the invitation is to be used
   * @throw InvitationNotExistsException invitation does not exist
   * @throw InvalidInvitationStatusException status is other than pending
   * @throw PrivilegeException insufficient rights
   */
  canInvitationBeAccepted {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getInvitationsManager().canInvitationBeAccepted(
          ac.getSession(), parms.readUUID("uuid"), ac.getGroupById(parms.readInt("group")));
    }
  },

  /*#
   * Changes status of invitation with specified id to revoked.
   *
   * @param sess session
   * @param id id of the invitation
   * @return Invitation object with updated status
   * @throws InvitationNotExistsException when invitation with this id does not exist
   * @throws PrivilegeException insufficient rights
   * @throws InvalidInvitationStatusException transition is not allowed from the current invitation status
   */
  revokeInvitationById {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      return ac.getInvitationsManager().revokeInvitationById(ac.getSession(), parms.readInt("invitation"));
    }
  },

  /*#
   * Changes status of invitation with specified uuid to revoked.
   *
   * @param sess session
   * @param uuid uuid of the invitation
   * @return Invitation object with updated status
   * @throws InvitationNotExistsException when invitation with this uuid does not exist
   * @throws PrivilegeException insufficient rights
   * @throws InvalidInvitationStatusException transition is not allowed from the current invitation status
   */
  revokeInvitationByUuid {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();
      return ac.getInvitationsManager().revokeInvitationByUuid(ac.getSession(), parms.readUUID("invitationUuid"));
    }
  },

  /*#
   * Extend the invitation date on the expiration to some later date.
   *
   * @param invitation int Invitation <code>id</code>
   * @param expiration String New expiration date of the invitation, should be later than the old, yyyy-mm-dd format.
   * @throw PrivilegeException Insufficient rights
   * @throw InvitationNotExistsException Invitation does not exist
   * @throw InvalidInvitationStatusException when invitation is not PENDING
   */
  /*#
   * Extend the invitation date on the expiration by one month.
   *
   * @param invitation int Invitation <code>id</code>
   * @throw PrivilegeException Insufficient rights
   * @throw InvitationNotExistsException Invitation does not exist
   * @throw InvalidInvitationStatusException when invitation is not PENDING
   */
  extendInvitationExpiration {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      LocalDate expiration = parms.contains("expiration") ? parms.readLocalDate("expiration") : null;
      return ac.getInvitationsManager().extendInvitationExpiration(ac.getSession(),
          ac.getInvitationsManager().getInvitationById(ac.getSession(), parms.readInt("invitation")),
          expiration);
    }
  },

  /*#
   * Extend the invitation date on the expiration to some later date.
   *
   * @param uuid token of the Invitation
   * @param expiration String New expiration date of the invitation, should be later than the old, yyyy-mm-dd format.
   * @throw PrivilegeException Insufficient rights
   * @throw InvitationNotExistsException Invitation does not exist
   * @throw InvalidInvitationStatusException when invitation is not PENDING
   */
  /*#
   * Extend the invitation date on the expiration by one month.
   *
   * @param uuid token of the Invitation
   * @throw PrivilegeException Insufficient rights
   * @throw InvitationNotExistsException Invitation does not exist
   * @throw InvalidInvitationStatusException when invitation is not PENDING
   */
  extendInvitationExpirationByUUID {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      parms.stateChangingCheck();

      LocalDate expiration = parms.contains("expiration") ? parms.readLocalDate("expiration") : null;
      return ac.getInvitationsManager().extendInvitationExpiration(ac.getSession(),
          ac.getInvitationsManager().getInvitationByToken(ac.getSession(), parms.readUUID("invitation")),
          expiration);
    }
  },

  /*#
   * Get page of invitations for the given group.
   * Query parameter specifies offset, page size, sorting order, sorting column, statuses of the invitations,
   * searched range of expiration date and string to search invitations by id, receiver name/email or sender name/email.
   *
   * @param group int Group <code>id</code>
   * @param query InvitationsPageQuery Query with page information
   *
   * @return Paginated<InvitationWithSender> page of requested invitations with sender's information
   * @throw GroupNotExistsException if there is no such group
   * @throw PrivilegeException if user doesn't have sufficient privileges
   */
  getInvitationsPage {
    @Override
    public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getInvitationsManager().getInvitationsPage(ac.getSession(), ac.getGroupById(parms.readInt("group")),
              parms.read("query", InvitationsPageQuery.class));
    }

  }
}
