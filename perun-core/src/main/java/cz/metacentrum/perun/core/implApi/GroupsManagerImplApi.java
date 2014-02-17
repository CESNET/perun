package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

/**
 * @author  Michal Prochazka
 * @author  Slavek Licehammer
 * @see Perun
 */
public interface GroupsManagerImplApi {


  /** 
   * Creates a new top-level group and associate it with the VO. (Stores group to underlaying data source)
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
   */
  Group createGroup(PerunSession perunSession, Vo vo, Group group) throws GroupExistsException, InternalErrorException;

  /** 
   * Creates a new subgroup of the existing group. (Stores group to underlaying data source)
   *
   * @param perunSession
   * @param parentGroup
   * @param group group.name must contain only shortName (without ":"). Hierarchy is defined by parentGroup parameter.
   * 
   * @return newly created sub group with full group.Name with ":"
   * 
   * @throws InternalErrorException if group.name contains ':' or other internal error occured
   * @throws GroupExistsException
   */
  Group createGroup(PerunSession perunSession, Vo vo, Group parentGroup, Group group) throws GroupExistsException, InternalErrorException;

  /**
   * Deletes group. 
   * Delete it from table of parentgroups, table of groups_members and groups table.
   * 
   * @param perunSession
   * @param vo
   * @param group group to delete
   * 
   * @throws InternalErrorException
   * @throws GroupAlreadyRemovedException if there are 0 rows affected by deleting from DB
   */
  void deleteGroup(PerunSession perunSession, Vo vo, Group group) throws InternalErrorException, GroupAlreadyRemovedException;

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
   */
  Group updateGroup(PerunSession perunSession, Group group) throws InternalErrorException;

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
   */
  Group getGroupById(PerunSession perunSession, int id) throws GroupNotExistsException, InternalErrorException;

  /** 
   * Search for the group with specified name in specified VO
   * 
   * @param perunSession
   * @param vo
   * @param name
   * 
   * @return group with specified name or throws GroupNotExistsException in specified VO
   * 
   * @throws GroupNotExistsException
   * @throws InternalErrorException
   */
  Group getGroupByName(PerunSession perunSession, Vo vo, String name) throws GroupNotExistsException, InternalErrorException;


  /** 
   * Adds member of the VO to the group in the same VO.
   * 
   * @param perunSession
   * @param group
   * @param member
   * @param type 
   * @param sourceGroupId 
   * 
   * @throws InternalErrorException
   * @throws AlreadyMemberException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * 
   * @return Member with specific MembershipType
   */
  Member addMember(PerunSession perunSession, Group group,  Member member, MembershipType type, int sourceGroupId) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException;


  /** 
   * Removes member form the group.
   * 
   * @param perunSession
   * @param group
   * @param member
   * 
   * @throws InternalErrorException
   * @throws NotGroupMemberException
   */
  void removeMember(PerunSession perunSession, Group group, Member member) throws InternalErrorException, NotGroupMemberException;

  /** Return groups by theirs id.
   * 
   * @param perunSession
   * @param groupsIds list of group ids
   * 
   * @return list groups
   * 
   * @throws InternalErrorException
   */
  List<Group> getGroupsByIds(PerunSession perunSession, List<Integer> groupsIds) throws InternalErrorException;

  /** Return list of assigned groups on the resource.
   * 
   * @param perunSession
   * @param resource
   * 
   * @return list of groups, which are assigned on the resource
   * 
   * @throws InternalErrorException 
   */
  List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource) throws InternalErrorException;
  
  /** Return group users sorted by name.
   * 
   * @param sess
   * @param group
   * 
   * @return list users sorted or empty list if there are no users on specified page
   * 
   * @throws InternalErrorException
   */
  List<User> getGroupUsers(PerunSession sess, Group group) throws InternalErrorException;
   
  /**
   * Checks whether the user is member of the group.
   * 
   * @param sess
   * @param user
   * @param group
   * @return true if the user is member of the group
   * @throws InternalErrorException
   */
  boolean isUserMemberOfGroup(PerunSession sess, User user, Group group) throws InternalErrorException;
  
  /**
   * Return all member's groups. Included members and administrators groups.
   * 
   * @param sess
   * @param member
   * @return
   * @throws InternalErrorException
   */
  List<Group> getAllMemberGroups(PerunSession sess, Member member) throws InternalErrorException;  

  /**
   * Return group members.
   * 
   * @param sess
   * @param group
   * @param statuses list of statuses, if status is null then return all members
   * @param excludeStatusInsteadOfIncludeStatus does the list of statuses means exclude members or include members with these statuses
   * @return list of members
   * @throws InternalErrorException
   */
  List<Member> getGroupMembers(PerunSession sess, Group group, List<Status> statuses, boolean excludeStatusInsteadOfIncludeStatus) throws InternalErrorException;
  
  /**
   * Get all group members ignoring theirs status.
   * 
   * @param sess
   * @param group
   * @return list of members
   * @throws InternalErrorException
   */
  List<Member> getGroupMembers(PerunSession sess, Group group) throws InternalErrorException;
  
  /**
   * Get all groups of the VO.
   *
   * @param perunSession
   * @param vo
   * 
   * @return list of groups
   * 
   * @throws InternalErrorException
   */
  List<Group> getAllGroups(PerunSession perunSession, Vo vo) throws InternalErrorException;
  
  /**
   * Get parent group.
   * 
   * @param sess
   * @param group
   * @return parent group
   * @throws InternalErrorException
   * @throws ParentGroupNotExistsException
   */
  Group getParentGroup(PerunSession sess, Group group) throws InternalErrorException, ParentGroupNotExistsException;
  
  /**
   * Get all immediate subgroups of the parent group under the VO.
   *
   * @param perunSession
   * @param parentGroup
   * 
   * @throws InternalErrorException
   * @return list of groups
   */
  List<Group> getSubGroups(PerunSession perunSession, Group parentGroup) throws InternalErrorException;

  /** Adds an administrator of the group.
   * 
   * @param perunSession
   * @param group
   * @param user
   * 
   * @throws InternalErrorException
   * @throws AlreadyAdminException
   */
  void addAdmin(PerunSession perunSession, Group group,  User user) throws InternalErrorException, AlreadyAdminException;

  /** Adds a group administrator to the group.
   * 
   * @param perunSession
   * @param group group that will be assigned admins (users) from authorizedGroup
   * @param authorizedGroup group that will be given the privilege
   * 
   * @throws InternalErrorException
   * @throws AlreadyAdminException
   */
  void addAdmin(PerunSession perunSession, Group group,  Group authorizedGroup) throws InternalErrorException, AlreadyAdminException;

  /** Removes an administrator form the group.
   * 
   * @param perunSession
   * @param group
   * @param user
   * 
   * @throws InternalErrorException
   * @throws UserNotAdminException
   */
  void removeAdmin(PerunSession perunSession, Group group, User user) throws InternalErrorException, UserNotAdminException;

  
  /** Removes a group administrator of the group.
   * 
   * @param perunSession
   * @param group
   * @param authorizedGroup group that will be removed the privilege
   * 
   * @throws InternalErrorException
   * @throws GroupNotAdminException
   */
  void removeAdmin(PerunSession perunSession, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException;

  /** Gets list of all administrators of this group.
   * If some group is administrator of the given group, all members are included in the list.
   * 
   * @param perunSession
   * @param group
   * 
   * @return list of all administrators
   * 
   * @throws InternalErrorException
   */
  List<User> getAdmins(PerunSession perunSession, Group group) throws InternalErrorException;

  /** 
   * Gets list of direct user administrators of this group.
   * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
   * 
   * @param perunSession
   * @param group
   * 
   * @throws InternalErrorException
   * 
   * @return list of direct administrators
   */
  List<User> getDirectAdmins(PerunSession perunSession, Group group) throws InternalErrorException;

  /** Gets list of all group administrators of this group.
   * 
   * @param perunSession
   * @param group
   * 
   * @return list of all group administrators
   * 
   * @throws InternalErrorException
   */
  List<Group> getGroupAdmins(PerunSession perunSession, Group group) throws InternalErrorException;

  /**
   * Check if group exists in underlaying data source.
   * 
   * @param perunSession
   * @param group
   * 
   * @return true if group exists in underlaying data source, false otherwise
   * 
   * @throws InternalErrorException
   */
  boolean groupExists(PerunSession perunSession, Group group) throws InternalErrorException;

  /**
   * Check if group exists in underlaying data source.
   * 
   * @param perunSession
   * @param group
   * 
   * @throws InternalErrorException
   * @throws GroupNotExistsException
   */
  void checkGroupExists(PerunSession perunSession, Group group) throws InternalErrorException, GroupNotExistsException;

  /**
   * Get all groups of users under the VO.
   *
   * @param perunSession
   * @param vo
   * 
   * @return list of groups
   * 
   * @throws InternalErrorException
   */
  List<Group> getGroups(PerunSession perunSession, Vo vo) throws InternalErrorException;
  
  /**
   * @param perunSession
   * @param vo
   * 
   * @return count of VO's groups
   * 
   * @throws InternalErrorException
   */
  int getGroupsCount(PerunSession perunSession, Vo vo) throws InternalErrorException;

  /**
   * Returns number of immediate subgroups of the parent group.
   * 
   * @param perunSession
   * @param parentGroup
   * 
   * @return count of parent group immediate subgroups
   * 
   * @throws InternalErrorException
   */
  int getSubGroupsCount(PerunSession perunSession, Group parentGroup) throws InternalErrorException;

  /**
   * Get the id of the VO which is owner of the group.
   * 
   * @param perunSession
   * @param group
   * 
   * @return id of the VO
   * 
   * @throws InternalErrorException
   */
  int getVoId(PerunSession perunSession, Group group) throws InternalErrorException;
  
  /**
   * Gets all groups which have enabled synchronization.
   * 
   * @param sess
   * @return list of groups to synchronize
   * @throws InternalErrorException
   */
  List<Group> getGroupsToSynchronize(PerunSession sess) throws InternalErrorException;
  
  /**
   * Returns list of groups' id where the member is member.
   * 
   * @param sess
   * @param member
   * @param vo
   * 
   * @return list of groups' id
   * 
   * @throws InternalErrorException
   */
  List<Integer> getMemberGroupsIds(PerunSession sess, Member member, Vo vo) throws InternalErrorException;
  
  /**
   * Returns list of groups' id where the member is member and the groups are under the defined resource.
   * 
   * @param sess
   * @param member
   * @param vo
   * 
   * @return list of groups' id
   * 
   * @throws InternalErrorException
   */
  List<Integer> getMemberGroupsIdsForResources(PerunSession sess, Member member, Vo vo) throws InternalErrorException;

  /**
   * Returns all groups which have set the attribute with the value. Searching only def and opt attributes.
   * 
   * @param sess
   * @param attribute
   * @return list of groups
   * @throws InternalErrorException
   */
  List<Group> getGroupsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException;

  /**
   * Returns all group-resource which have set the attribute with the value. Searching only def and opt attributes.
   * 
   * @param sess
   * @param attribute
   * @return 
   * @throws InternalErrorException
   */
  List<Pair<Group, Resource>> getGroupResourcePairsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException;

  /**
   * Return true if Member is member of the Group
   * 
   * @param sess
   * @param group
   * @param member
   * @return true if Member is member of the Group
   * 
   * @throws InternalErrorException
   */
  boolean isGroupMember(PerunSession sess, Group group, Member member) throws InternalErrorException;

  
  /**
   * Return true if Member is direct member of the Group
   * 
   * 
   * @param sess
   * @param group
   * @param member
   * @return true if Member is direct member of the Group
   * 
   * @throws InternalErrorException
   */
  boolean isDirectGroupMember(PerunSession sess, Group group, Member member) throws InternalErrorException;

    /**
     * Return list of IDs of all applications, which belongs to Group.
     *
     * @param sess
     * @param group
     * @return list of all group applications ids
     */
    public List<Integer> getGroupApplicationIds(PerunSession sess, Group group);

    /**
     * Return list of all reserved logins for specific application
     * (pair is namespace and login)
     *
     * @param appId from which application get reserved logins
     * @return list of pairs namespace and login
     */
    public List<Pair<String, String>> getApplicationReservedLogins(Integer appId);

    /**
     * Delete all Group login reservations
     *
     * Reserved logins must be removed from external systems
     * (e.g. KDC) BEFORE calling this method via deletePassword() in
     * UsersManager.
     *
     * @param sess
     * @param group Group to delete all login reservations for
     */
    public void deleteGroupReservedLogins(PerunSession sess, Group group);

}
