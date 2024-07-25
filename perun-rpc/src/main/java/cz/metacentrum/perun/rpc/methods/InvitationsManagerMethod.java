package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public enum InvitationsManagerMethod implements ManagerMethod {

  getInvitationById {
    @Override
    public Invitation call(ApiCaller ac, Deserializer parms) throws PerunException {
      return null;
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
   * Checks if an invitation given by the uuid exists and if it is in a pending state. Throws exception otherwise.
   *
   * @param sess session
   * @param uuid random token assigned to the invitation
   * @throws InvitationNotExistsException invitation does not exist
   * @throws InvalidInvitationStatusException status is other than pending
   */
  canInvitationBeAccepted {
    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getInvitationsManager().canInvitationBeAccepted(ac.getSession(), parms.readUUID("uuid"));
      return null;
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
      return ac.getInvitationsManager().revokeInvitationById(ac.getSession(), parms.readInt("invitationId"));
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
}
