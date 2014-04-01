package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.NotMemberOfParentGroupException;
import cz.metacentrum.perun.core.api.exceptions.NotServiceUserExpectedException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;

/**
 * <p>Groups manager can do all work about groups in VOs.</p>
 *
 * <p>You must get an instance of GroupsManager from instance of Perun (perun si singleton - see how to get it's instance on wiki):</p>
 * <pre>
 *    GroupsManager gm = perun.getGroupsManager();
 * </pre>
 *
 * @author  Michal Prochazka
 * @author  Slavek Licehammer
 * @see Perun
 */
public interface GroupsManager {

  // Attributes related to the external groups
    // Contains query need to get the group members
  public static final String GROUPMEMBERSQUERY_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersQuery";
    // Define the external source used for accessing the data about external group
  public static final String GROUPEXTSOURCE_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupExtSource";
    // Define the external source used for accessing the data about the group members
  public static final String GROUPMEMBERSEXTSOURCE_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersExtSource";
  // If the synchronization is enabled/disabled, value is true/false
  public static final String GROUPSYNCHROENABLED_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":synchronizationEnabled";
  // Defines the interval, when the group has to be synchronized. It is fold of 5 minutes
  public static final String GROUPSYNCHROINTERVAL_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":synchronizationInterval";

  public static final String GROUP_SHORT_NAME_REGEXP = "^[-a-zA-Z.0-9_ ]+$";
  /**
   * Creates a new top-level group and associate it with the VO.
   *
   * For this method (new group) has always same shortName like Name.
   *
   * @param perunSession
   * @param vo
   * @param group with name without ":"
   *
   * @return newly created top-level group
   *
   * @throws InternalErrorException if group.name contains ':' or other internal error occured
   * @throws GroupExistsException
   * @throws PrivilegeException
   * @throws VoNotExistsException
   */
  Group createGroup(PerunSession perunSession, Vo vo, Group group) throws GroupExistsException, PrivilegeException, InternalErrorException, VoNotExistsException;

  /**
   * Creates a new subgroup of the existing group.
   *
   *
   * @param perunSession
   * @param parentGroup
   * @param group group.name must contain only shortName (without ":"). Hierarchy is defined by parentGroup parameter.
   *
   * @return newly created sub group with full group.Name with ":"
   *
   * @throws InternalErrorException if group.name contains ':' or other internal error occured
   * @throws GroupExistsException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   */
  Group createGroup(PerunSession perunSession, Group parentGroup, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException, InternalErrorException;

  /**
   * If forceDelete is false, delete only group and if this group has members or subgroups, throw an exception.
   * If forceDelete is true, delete group with all subgroups, members and administrators, then delete this group.
   *
   * @param perunSession
   * @param group group to delete
   * @param forceDelete if forceDelete is false, delete group only if is empty and has no subgroups, if is true, delete anyway with all connections
   *
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws PrivilegeException if user has no rights to do this operation
   * @throws RelationExistsException raise only if group has subgroups or members and forceDelete is false
   * @throws GroupAlreadyRemovedException if there are 0 rows affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on resource affected by deleting from DB
   */
  void deleteGroup(PerunSession perunSession, Group group, boolean forceDelete) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

  /**
   * Deletes group only if has no subgroups and no members. Other way throw exception.
   * This method is same like deleteGroup(sess, group, false) with false for forceDelete
   *
   * @param perunSession
   * @param group group to delete
   *
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws RelationExistsException raise if group has subgroups or member
   * @throws PrivilegeException
   * @throws GroupAlreadyRemovedException if there are 0 rows affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on resource affected by deleting from DB
   */
  void deleteGroup(PerunSession perunSession, Group group) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

  /**
   * Deletes all groups under the VO except built-in groups (members, admins groups).
   *
   * @param perunSession
   * @param vo VO
   *
   * @throws InternalErrorException
   * @throws VoNotExistsException
   * @throws PrivilegeException
   * @throws InternalErrorRuntimeException
   * @throws GroupAlreadyRemovedException if there is at least 1 group not affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on resource affected by deleting from DB
   */
  void deleteAllGroups(PerunSession perunSession, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

  /**
   * Updates group by ID.
   *
   * Update shortName (use shortName) and description. Group.name is ignored.
   * Return Group with correctly set parameters (including group.name)
   *
   * @param perunSession
   * @param group to update (use only ID, shortName and description)
   *
   * @return updated group with correctly set parameters (including group.name)
   *
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   * @throws InternalErrorRuntimeException
   */
  Group updateGroup(PerunSession perunSession, Group group) throws GroupNotExistsException, InternalErrorException, PrivilegeException;

  /**
   * Search for the group with specified id in all VOs.
   *
   * @param id
   * @param perunSession
   *
   * @return group with specified id or throws GroupNotExistsException
   *
   * @throws GroupNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws InternalErrorRuntimeException
   */
  Group getGroupById(PerunSession perunSession, int id) throws GroupNotExistsException, InternalErrorException, PrivilegeException;

  /**
   * Search for the group with specified name in specified VO.
   *
   * @param perunSession
   * @param vo
   * @param name
   *
   * @return group with specified name or throws GroupNotExistsException  in specified VO
   *
   * @throws GroupNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws InternalErrorRuntimeException
   */
  Group getGroupByName(PerunSession perunSession, Vo vo, String name) throws GroupNotExistsException, InternalErrorException, PrivilegeException, VoNotExistsException;


  /**
   * Adds member of the VO to the group in the same VO.
   *
   * @param perunSession
   * @param group
   * @param member
   *
   * @throws InternalErrorException
   * @throws MemberNotExistsException
   * @throws PrivilegeException
   * @throws AlreadyMemberException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   * @throws WrongAttributeValueException if any member attribute value, required by resource (on which the group is assigned), is wrong
   * @throws WrongReferenceAttributeValueException
   */
  void addMember(PerunSession perunSession, Group group,  Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException;


  /**
   * Removes member form the group.
   *
   * @param perunSession
   * @param group
   * @param member
   *
   * @throws InternalErrorException
   * @throws MemberNotExistsException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws NotGroupMemberException
   * @throws InternalErrorRuntimeException
   */
  void removeMember(PerunSession perunSession, Group group, Member member) throws InternalErrorException, MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException;

  /**
   * Return all group members.
   *
   * @param perunSession
   * @param group
   * @return list of members or empty list if the group is empty
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   */
  List<Member> getGroupMembers(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Return group members with specified vo membership status.
   *
   * @param perunSession
   * @param group
   * @param status
   * @return list of members with specified membership status or empty list if no such member is found in group
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   */
  List<Member> getGroupMembers(PerunSession perunSession, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException;


  /**
   * Returns group members in the RichMember object, which contains Member+User data.
   *
   * @param sess
   * @param group
   * @return list of RichMembers
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   */
  List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Returns group members with specified membership status in the RichMember object, which contains Member+User data.
   *
   * @param sess
   * @param group
   * @param status
   * @return list of RichMembers
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   */
  List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Returns group members in the RichMember object, which contains Member+User data. Also contains user and member attributes.
   *
   * @param sess
   * @param group
   * @return list of RichMembers
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   */
  List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Returns group members with specified membership status in the RichMember object, which contains Member+User data. Also contains user and member attributes.
   *
   * @param sess
   * @param group
   * @param status
   * @return list of RichMembers
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   */
  List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * @param perunSession
   * @param group
   *
   * @return count of members of specified group
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   */
  int getGroupMembersCount(PerunSession perunSession, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException;

  /**
   * Get groups of Vo by ACCESS RIGHTS:
   * If user is:
   * - PERUNADMIN or VOADMIN : all groups in vo
   * - GROUPADMIN : only groups where user is admin
   *
   * @param sess
   * @param vo
   *
   * @return list of groups
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException
   */
  List<Group> getAllGroups(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

  /**
   * Get groups of the VO stored in the map reflecting the hierarchy by ACCESS RIGHTS:
   * If user is:
   * - PERUNADMIN or VOADMIN : all Groups
   * - GROUPADMIN : only groups where user is groupAdmin
   *
   * @param sess
   * @param vo
   *
   * @return map of the groups hierarchically organized
   *
   * @throws InternalErrorException
   */
  Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;;

  /**
   * Get parent group.
   * If group is topLevel group or Members group, return Members group.
   *
   * @param sess
   * @param group
   * @return parent group
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws ParentGroupNotExistsException
   */
  Group getParentGroup(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException, ParentGroupNotExistsException;

  /**
   * Get all subgroups of the parent group under the VO.
   *
   * @param sess
   * @param parentGroup parent group
   *
   * @return list of groups
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   */
  List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Get all subgroups of the parentGroup recursively.
   * (parentGroup subgroups, their subgroups etc...)
   *
   * @param sess
   * @param parentGroup parent group
   *
   * @return list of groups
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   */
  List<Group> getAllSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Adds a user administrator of the group.
   *
   * @param perunSession
   * @param group
   * @param user
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws AlreadyAdminException
   * @throws InternalErrorRuntimeException
   * @throws UserNotExistsException
   */
  void addAdmin(PerunSession perunSession, Group group,  User user) throws InternalErrorException, AlreadyAdminException, PrivilegeException, GroupNotExistsException, UserNotExistsException;

   /**
   * Adds a group administrator to the group.
   *
   * @param perunSession
   * @param group - group that will be assigned admins (users) from authorizedGroup
   * @param authorizedGroup - group that will be given the privilege
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws AlreadyAdminException
   * @throws InternalErrorRuntimeException
   */
  void addAdmin(PerunSession perunSession, Group group,  Group authorizedGroup) throws InternalErrorException, AlreadyAdminException, PrivilegeException, GroupNotExistsException;


  /**
   * Removes a user administrator form the group.
   *
   * @param perunSession
   * @param group
   * @param user
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   * @throws UserNotAdminException
   * @throws UserNotExistsException
   */
  void removeAdmin(PerunSession perunSession, Group group, User user) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotAdminException, UserNotExistsException;

  /**
   * Removes a group administrator of the group.
   *
   * @param perunSession
   * @param group
   * @param authorizedGroup group that will be removed the privilege
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   * @throws GroupNotAdminException
   */
  void removeAdmin(PerunSession perunSession, Group group, Group authorizedGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, GroupNotAdminException;

  /**
   * Gets list of all user administrators of this group.
   * If some group is administrator of the given group, all members are included in the list.
   *
   * @param perunSession
   * @param group
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   */
  List<User> getAdmins(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

   /**
   * Gets list of direct user administrators of this group.
   * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
   *
   * @param perunSession
   * @param group
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   */
  List<User> getDirectAdmins(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Gets list of all group administrators of given group.
   *
   * @param perunSession
   * @param group
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws InternalErrorRuntimeException
   *
   * @return list of all group administrators of the given group
   */
  List<Group> getAdminGroups(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Gets list of all administrators of this group like RichUsers without attributes.
   *
   * @param perunSession
   * @param group
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws UserNotExistsException
   * @throws InternalErrorRuntimeException
   */
  List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException;

  /**
   * Gets list of all administrators of this group like RichUsers with attributes.
   *
   * @param perunSession
   * @param group
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   * @throws UserNotExistsException
   * @throws InternalErrorRuntimeException
   */
  List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException;

  /**
   * Get list of Group administrators with specific attributes.
   * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
   *
   * @param perunSession
   * @param group
   * @param specificAttributes
   * @return list of RichUsers with specific attributes.
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException
   * @throws UserNotExistsException
   */
  List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException;

  /**
   * Get groups of users under the VO by ACCESS RIGHTS:
   * If user is:
   * - PERUNADMIN or VOADMIN : all groups
   * - GROUPADMIN : only groups where user is GroupAdmin
   *
   * @param sess
   * @param vo vo

   * @throws InternalErrorException
   * @throws VoNotExistsException
   * @throws PrivilegeException
   * @return list of groups
   */
  List<Group> getGroups(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

  /**
   * @param sess
   * @param vo
   *
   * @return count of VO's groups
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException
   */
  int getGroupsCount(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

  /**
   * Returns number of immediate subgroups of the parent group.
   *
   * @param sess
   * @param parentGroup
   *
   * @return count of parent group immediate subgroups
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   */
  int getSubGroupsCount(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Gets the Vo which is owner of the group.
   *
   * @param sess
   * @param group
   *
   * @return Vo which is owner of the group.
   *
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   */
  Vo getVo(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException;

  /**
   * Get members from parent group. If the parent group doesn't exist (this is top level group) return all VO (from which the group is) members instead.
   *
   * @param sess
   * @param group
   * @return
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   */
  List<Member> getParentGroupMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Get members form the parent group in RichMember format.
   * @param sess
   * @param group
   * @return list of parent group rich members
   * @throws InternalErrorException
   */
  List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Get members form the parent group in RichMember format including user/member attributes.
   * @param sess
   * @param group
   * @return list of parent group rich members
   * @throws InternalErrorException
   */
  List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Synchronizes the group with the external group.
   *
   * @param sess
   * @param group
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   * @throws GroupSynchronizationAlreadyRunningException
   */
  void forceGroupSynchronization(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupSynchronizationAlreadyRunningException;

  /**
   * Synchronize all groups which have enabled synchronization. This method is run by the scheduler every 5 minutes.
   *
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  void synchronizeGroups(PerunSession sess) throws InternalErrorException, PrivilegeException;

  /**
   * Returns all member's groups. Except members groups.
   *
   * @param sess
   * @param member
   * @return
   * @throws InternalErrorException
   */
  List<Group> getMemberGroups(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

  /**
   * Return all member's groups. Included members groups.
   *
   * @param sess
   * @param member
   * @return
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws MemberNotExistsException
   */
  List<Group> getAllMemberGroups(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException;
}
