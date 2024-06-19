package cz.metacentrum.perun.registrar.blImpl;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.bl.InvitationsManagerBl;
import cz.metacentrum.perun.registrar.exceptions.InvalidInvitationStatusException;
import cz.metacentrum.perun.registrar.exceptions.InvitationNotExistsException;
import cz.metacentrum.perun.registrar.implApi.InvitationsManagerImplApi;
import cz.metacentrum.perun.registrar.model.Invitation;
import cz.metacentrum.perun.registrar.model.InvitationStatus;
import java.util.List;

public class InvitationsManagerBlImpl implements InvitationsManagerBl {

  private final InvitationsManagerImplApi invitationsManagerImpl;
  private final RegistrarManager registrarManager;
  private PerunBl perun;

  public InvitationsManagerBlImpl(InvitationsManagerImplApi invitationsManagerImpl, RegistrarManager registrarManager) {
    this.invitationsManagerImpl = invitationsManagerImpl;
    this.registrarManager = registrarManager;
  }

  public void setPerun(PerunBl perunBl) {
    this.perun = perunBl;
  }

  @Override
  public Invitation getInvitationById(PerunSession sess, int id) throws InvitationNotExistsException {
    return invitationsManagerImpl.getInvitationById(sess, id);
  }

  @Override
  public List<Invitation> getInvitationsForSender(PerunSession sess, Group group, User user) {
    return invitationsManagerImpl.getInvitationsForSender(sess, group, user);
  }

  @Override
  public List<Invitation> getInvitationsForGroup(PerunSession sess, Group group) {
    return invitationsManagerImpl.getInvitationsForGroup(sess, group);
  }

  @Override
  public List<Invitation> getInvitationsForVo(PerunSession sess, Vo vo) {
    return invitationsManagerImpl.getInvitationsForVo(sess, vo);
  }

  @Override
  public Invitation createInvitation(PerunSession sess, Invitation invitation) {
    return invitationsManagerImpl.createInvitation(sess, invitation);
  }

  @Override
  public Invitation expireInvitation(PerunSession sess, Invitation invitation) throws InvalidInvitationStatusException {
    if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
      throw new InvalidInvitationStatusException("Invitation: " + invitation + "cannot be expired when in status: " +
                                                    invitation.getStatus());
    }
    invitationsManagerImpl.setInvitationStatus(sess, invitation, InvitationStatus.EXPIRED);
    invitation.setStatus(InvitationStatus.EXPIRED);
    return invitation;
  }

  @Override
  public Invitation revokeInvitation(PerunSession sess, Invitation invitation) throws InvalidInvitationStatusException {
    if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
      throw new InvalidInvitationStatusException("Invitation: " + invitation + "cannot be revoked when in status: " +
                                                    invitation.getStatus());
    }
    invitationsManagerImpl.setInvitationStatus(sess, invitation, InvitationStatus.REVOKED);
    invitation.setStatus(InvitationStatus.REVOKED);
    return invitation;
  }

  @Override
  public Invitation markInvitationAccepted(PerunSession sess, Invitation invitation)
      throws InvalidInvitationStatusException {
    if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
      throw new InvalidInvitationStatusException("Invitation: " + invitation + "cannot be accepted when in status: " +
                                                    invitation.getStatus());
    }
    invitationsManagerImpl.setInvitationStatus(sess, invitation, InvitationStatus.ACCEPTED);
    invitation.setStatus(InvitationStatus.ACCEPTED);
    return invitation;
  }
}
