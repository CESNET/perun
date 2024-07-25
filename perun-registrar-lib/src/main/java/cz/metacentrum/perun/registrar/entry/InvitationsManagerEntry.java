package cz.metacentrum.perun.registrar.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.api.InvitationsManager;
import cz.metacentrum.perun.registrar.bl.InvitationsManagerBl;
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
 * Invitations entry logic
 *
 * @author David Flor <493294@mail.muni.cz>
 */
public class InvitationsManagerEntry implements InvitationsManager {

  private InvitationsManagerBl invitationsManagerBl;
  private PerunBl perun;

  public InvitationsManagerEntry() {}

  public void setInvitationsManagerBl(InvitationsManagerBl invitationsManagerBl) {
    this.invitationsManagerBl = invitationsManagerBl;
  }

  public void setPerun(PerunBl perunBl) {
    this.perun = perunBl;
  }

  @Override
  public Invitation getInvitationById(PerunSession sess, int id) throws InvitationNotExistsException,
                                                                              PrivilegeException {
    Utils.checkPerunSession(sess);

    Invitation invitation = invitationsManagerBl.getInvitationById(sess, id);

    if (!AuthzResolver.authorizedInternal(sess, "getInvitationById_int_policy", invitation)) {
      throw new PrivilegeException("getInvitationById");
    }

    return invitation;
  }

  @Override
  public List<Invitation> getInvitationsForSender(PerunSession sess, Group group, User user)
      throws GroupNotExistsException, PrivilegeException, UserNotExistsException {
    Utils.checkPerunSession(sess);
    perun.getGroupsManagerBl().checkGroupExists(sess, group);
    perun.getUsersManagerBl().checkUserExists(sess, user);
    if (!AuthzResolver.authorizedInternal(sess, "getInvitationsForSender_int_int_policy", group)) {
      throw new PrivilegeException("getInvitationsForSender");
    }

    return invitationsManagerBl.getInvitationsForSender(sess, group, user);
  }

  @Override
  public List<Invitation> getInvitationsForGroup(PerunSession sess, Group group)
      throws GroupNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    perun.getGroupsManagerBl().checkGroupExists(sess, group);
    if (!AuthzResolver.authorizedInternal(sess, "getInvitationsForGroup_int_policy", group)) {
      throw new PrivilegeException("getInvitationsForGroup");
    }

    return invitationsManagerBl.getInvitationsForGroup(sess, group);
  }

  @Override
  public List<Invitation> getInvitationsForVo(PerunSession sess, Vo vo)
      throws VoNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);
    perun.getVosManagerBl().checkVoExists(sess, vo);
    if (!AuthzResolver.authorizedInternal(sess, "getInvitationsForVo_int_policy", vo)) {
      throw new PrivilegeException("getInvitationsForVo");
    }

    return invitationsManagerBl.getInvitationsForVo(sess, vo);
  }

  @Override
  public Invitation inviteToGroup(PerunSession sess, Vo vo, Group group, String receiverName, String receiverEmail,
                                  String language, LocalDate expiration, String redirectUrl)
      throws PrivilegeException, GroupNotExistsException, VoNotExistsException, RegistrarException {
    Utils.checkPerunSession(sess);

    perun.getGroupsManagerBl().checkGroupExists(sess, group);
    perun.getVosManagerBl().checkVoExists(sess, vo);
    if (!AuthzResolver.authorizedInternal(sess,
        "inviteToGroup_Vo_Group_String_String_String_LocalDate_String_policy", vo, group)) {
      throw new PrivilegeException("inviteToGroup");
    }

    return invitationsManagerBl.inviteToGroup(sess, vo, group, receiverName, receiverEmail, language, expiration,
        redirectUrl);
  }

  @Override
  public Map<String, String> inviteToGroupFromCsv(PerunSession sess, Vo vo, Group group, List<String> data,
                                                  String language, LocalDate expiration, String redirectUrl)
      throws GroupNotExistsException, VoNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    perun.getGroupsManagerBl().checkGroupExists(sess, group);
    perun.getVosManagerBl().checkVoExists(sess, vo);
    if (!AuthzResolver.authorizedInternal(sess,
        "inviteToGroupFromCsv_Vo_Group_List<String>_policy", vo, group)) {
      throw new PrivilegeException("inviteToGroupFromCsv");
    }
    return invitationsManagerBl.inviteToGroupFromCsv(sess, vo, group, data, language, expiration, redirectUrl);
  }

  @Override
  public Invitation createInvitation(PerunSession sess, Invitation invitation)
      throws PrivilegeException, GroupNotExistsException, VoNotExistsException {
    Utils.checkPerunSession(sess);

    perun.getGroupsManagerBl().checkGroupExists(sess, perun.getGroupsManagerBl().getGroupById(sess, invitation
                                                                                                        .getGroupId()));
    // most likely not necessary to include Vo in invitation at all, see how it unfolds in the future
    perun.getVosManagerBl().checkVoExists(sess, perun.getVosManagerBl().getVoById(sess, invitation.getVoId()));

    if (!AuthzResolver.authorizedInternal(sess, "createInvitation_Invitation_policy", invitation)) {
      throw new PrivilegeException("createInvitation");
    }

    return invitationsManagerBl.createInvitation(sess, invitation);
  }

  @Override
  public String createInvitationUrl(PerunSession sess, String authentication, String token)
      throws PrivilegeException, InvitationNotExistsException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "createInvitationUrl_String_String")) {
      throw new PrivilegeException("createInvitationUrl");
    }

    return invitationsManagerBl.createInvitationUrl(sess, authentication, token);
  }

  @Override
  public Invitation canInvitationBeAccepted(PerunSession sess, UUID uuid, Group group)
      throws PrivilegeException, InvalidInvitationStatusException, InvitationNotExistsException,
                 InvitationAlreadyAssignedToAnApplicationException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "canInvitationBeAccepted_UUID_policy")) {
      throw new PrivilegeException("canInvitationBeAccepted");
    }

    return invitationsManagerBl.canInvitationBeAccepted(sess, uuid, group);
  }
}
