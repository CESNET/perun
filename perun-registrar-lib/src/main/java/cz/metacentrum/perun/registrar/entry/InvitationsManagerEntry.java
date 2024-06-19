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
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.model.Invitation;
import java.util.List;

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
  public Invitation createInvitation(PerunSession sess, Invitation invitation)
      throws PrivilegeException, GroupNotExistsException, VoNotExistsException {
    Utils.checkPerunSession(sess);

    if (!Utils.EMAIL_PATTERN.matcher(invitation.getReceiverEmail()).matches()) {
      throw new IllegalArgumentException("Invalid email address: " + invitation.getReceiverEmail());
    }

    perun.getGroupsManagerBl().checkGroupExists(sess, perun.getGroupsManagerBl().getGroupById(sess, invitation
                                                                                                        .getGroupId()));
    // most likely not necessary to include Vo in invitation at all, see how it unfolds in the future
    perun.getVosManagerBl().checkVoExists(sess, perun.getVosManagerBl().getVoById(sess, invitation.getVoId()));

    if (!AuthzResolver.authorizedInternal(sess, "createInvitation_Invitation_policy", invitation)) {
      throw new PrivilegeException("createInvitation");
    }

    return invitationsManagerBl.createInvitation(sess, invitation);
  }
}
