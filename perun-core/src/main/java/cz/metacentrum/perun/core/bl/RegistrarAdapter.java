package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDeletionFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.registrar.model.Application;

public interface RegistrarAdapter {

  /**
   * Reject all applications of user (consider deleting?), while freeing all reserved logins.
   * Probably skip rejection notifications for this?
   *
   * @param sess
   * @param user
   */
  void onDeleteUser(PerunSession sess, User user);

  /**
   * Reject all applications of member - vo and its groups (consider deleting?), while freeing all reserved logins.
   * Probably skip rejection notifications for this?
   *
   * @param sess
   * @param member
   */
  void onDeleteMember(PerunSession sess, Member member);

  /**
   * Reject all applications of member in the group (consider deleting?)
   * Probably skip rejection notifications for this?
   * TODO is there a reason why Old reg did not do this?
   * @param sess
   * @param group
   * @param member
   */
  void onRemoveMemberFromGroup(PerunSession sess, Group group, Member member);

  /**
   * Reject all applications into the group and all its subgroups (consider deleting?) not already rejected by
   * deleting the subgroups and members. Free the logins.
   * Probably skip rejection notifications for this?
   *
   * @param sess
   * @param group
   */
  void onDeleteGroup(PerunSession sess, Group group);

  /**
   * Reject all applications into the Vo and all its groups (consider deleting?) not already rejected by
   * deleting the groups and members. Free the logins.
   * Probably skip rejection notifications for this?
   *
   * @param sess
   * @param vo
   */
  void onDeleteVo(PerunSession sess, Vo vo)
      throws PasswordOperationTimeoutException, InvalidLoginException, PasswordDeletionFailedException;

  /**
   * Check that the attribute is not used in any form items as source/destination. If it is, throw
   * an exception.
   *
   * @param sess
   * @param attributeDefinition
   */
  void onDeleteAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition)
      throws RelationExistsException;

  void onUserIdentityJoined(PerunSession sess, UserExtSource userExtSource);

  void onRejectApplication(PerunSession sess, Application application);

  String getInviteUrlForVo(Vo vo);

  String getInviteUrlForGroup(Group group);
}
