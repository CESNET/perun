package cz.metacentrum.perun.core.bl;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.NotMemberOfParentGroupException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

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
public interface GroupsManagerBl {


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
	 * @throws InternalErrorException if group.name contains ':' or other internal error occur
	 * @throws GroupExistsException
	 */
	Group createGroup(PerunSession perunSession, Vo vo, Group group) throws GroupExistsException, InternalErrorException;

	/**
	 * Creates a new subgroup of the existing group.
	 *
	 * @param perunSession
	 * @param parentGroup
	 * @param group group.name must contain only shortName (without ":"). Hierarchy is defined by parentGroup parameter.
	 *
	 * @return newly created sub group with full group.Name with ":"
	 *
	 * @throws InternalErrorException if group.name contains ':' or other internal error occur
	 * @throws GroupExistsException
	 */
	Group createGroup(PerunSession perunSession, Group parentGroup, Group group) throws GroupExistsException, InternalErrorException;

	/**
	 * Gets all groups which have enabled synchronization.
	 *
	 * @param sess
	 * @return list of groups to synchronize
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	List<Group> getGroupsToSynchronize(PerunSession sess) throws InternalErrorException;

	/**
	 * If forceDelete is false, delete only group and if this group has members or subgroups, throw an exception.
	 * If forceDelete is true, delete group with all subgroups, members and administrators, then delete this group.
	 *
	 * @param perunSession
	 * @param group group to delete
	 * @param forceDelete if forceDelete is false, delete group only if is empty and has no subgroups, if is true, delete anyway with all connections
	 *
	 * @throws InternalErrorException
	 * @throws RelationExistsException raise only if group has subgroups or members and forceDelete is false
	 * @throws GroupAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on resource not affected by removing from DB
	 */
	void deleteGroup(PerunSession perunSession, Group group, boolean forceDelete) throws InternalErrorException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Deletes built-in members group.
	 *
	 * @param sess
	 * @param vo
	 * @throws InternalErrorException
	 * @throws GroupAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on resource not affected by deliting from DB
	 */
	void deleteMembersGroup(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Deletes all groups under the VO except built-in groups (members, admins groups).
	 *
	 * @param perunSession
	 * @param vo VO
	 *
	 * @throws InternalErrorException
	 * @throws GroupAlreadyRemovedException if there is at least 1 group not affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on resource not affected by deleting from DB
	 *
	 */
	void deleteAllGroups(PerunSession perunSession, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

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
	 * @return group with specified id or throws
	 *
	 * @throws InternalErrorException
	 */
	Group getGroupById(PerunSession perunSession, int id) throws InternalErrorException, GroupNotExistsException;

	/**
	 * Search for the group with specified name in specified VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param name
	 *
	 * @return group with specified name or throws   in specified VO
	 *
	 * @throws InternalErrorException
	 */
	Group getGroupByName(PerunSession perunSession, Vo vo, String name) throws InternalErrorException, GroupNotExistsException;


	/**
	 * Adds member of the VO to the group in the same VO. But not to administrators and members group.
	 *
	 * @param perunSession
	 * @param group
	 * @param member
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException if any member attribute value, required by resource (on which the group is assigned), is wrong
	 * @throws RelationExistsException
	 * @throws WrongReferenceAttributeValueException
	 */
	void addMember(PerunSession perunSession, Group group,  Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException;

	/**
	 * Special addMember which is able to add members into the members and administrators group.
	 *
	 * @param perunSession
	 * @param group
	 * @param member
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws NotMemberOfParentGroupException
	 */
	void addMemberToMembersGroup(PerunSession perunSession, Group group,  Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException;

	/** Return list of assigned groups on the resource (without subgroups unless they are assigned too)
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @return list of groups, which are assigned on the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/** Return list of assigned groups on the resource.
	 *
	 * @param perunSession
	 * @param resource
	 * @param withSubGroups if true returns also all subgroups of assigned groups
	 *
	 * @return list of groups, which are assigned on the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource, boolean withSubGroups) throws InternalErrorException;

	/**
	 * Removes member form the group. But not from members or administrators group.
	 *
	 * @param perunSession
	 * @param group
	 * @param member
	 *
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 */
	void removeMember(PerunSession perunSession, Group group, Member member) throws InternalErrorException, NotGroupMemberException;

	/**
	 * Removes member from members or administrators group only.
	 *
	 * @param perunSession
	 * @param group
	 * @param member
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 */
	void removeMemberFromMembersOrAdministratorsGroup(PerunSession perunSession, Group group, Member member) throws InternalErrorException, NotGroupMemberException;

	/**
	 * Return all group members.
	 *
	 * @param perunSession
	 * @param group
	 * @return list of users or empty list if the group is empty
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembers(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * Return only valid, suspended and expired group members.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list users or empty list if there are no users on specified page
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembersExceptInvalid(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * Return group members.
	 *
	 * @param perunSession
	 * @param group
	 * @param status
	 *
	 * @return list users or empty list if there are no users on specified page
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembers(PerunSession perunSession, Group group, Status status) throws InternalErrorException;

	/**
	 * Return group users sorted by name.
	 *
	 * @param perunSession
	 * @param group
	 * @return list users sorted or empty list if there are no users on specified page
	 */
	List<User> getGroupUsers(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * Returns group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Returns only valid, suspended and expired group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembersExceptInvalid(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Returns group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess
	 * @param group
	 * @param status
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws InternalErrorException;

	/**
	 * Returns group members in the RichMember object, which contains Member+User data. Also contains user and member attributes.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Returns only valid, suspended and expired group members in the RichMember object, which contains Member+User data. Also contains user and member attributes.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembersWithAttributesExceptInvalid(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Returns group members in the RichMember object, which contains Member+User data. Also contains user and member attributes.
	 *
	 * @param sess
	 * @param group
	 * @param status
	 *
	 * @return list of RichMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws InternalErrorException;

	/**
	 * @param perunSession
	 * @param group
	 *
	 * @return count of members of specified group
	 *
	 * @throws InternalErrorException
	 */
	int getGroupMembersCount(PerunSession perunSession, Group group) throws InternalErrorException;

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
	 * Get all groups of the VO.
	 *
	 * @param sess
	 * @param vo
	 *
	 * @return list of groups
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAllGroups(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get all groups of the VO stored in the map reflecting the hierarchy.
	 *
	 * @param sess
	 * @param vo
	 *
	 * @return map of the groups hierarchically organized
	 *
	 * @throws InternalErrorException
	 */
	Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get parent group.
	 * If group is topLevel group or Members group, return Members group.
	 *
	 * @param sess
	 * @param group
	 * @return parent group
	 * @throws InternalErrorException
	 * @throws ParentGroupNotExistsException
	 */
	Group getParentGroup(PerunSession sess, Group group) throws InternalErrorException, ParentGroupNotExistsException;

	/**
	 * Get all subgroups of the parent group under the VO.
	 *
	 * @param sess
	 * @param parentGroup parent group
	 *
	 * @return list of groups
	 * @throws InternalErrorException
	 */
	List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException;

	/**
	 * Get all subgroups of the parentGroup recursively.
	 * (parentGroup subgroups, their subgroups etc...)
	 *
	 * @param sess
	 * @param parentGroup parent group
	 *
	 * @return list of groups
	 * @throws InternalErrorException
	 */
	List<Group> getAllSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException;

	/**
	 * Adds an administrator of the group.
	 *
	 * @param perunSession
	 * @param group
	 * @param user
	 *
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession perunSession, Group group,  User user) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Adds a group administrator to the group.
	 *
	 * @param perunSession
	 * @param group - group that will be assigned admins (users) from authorizedGroup
	 * @param authorizedGroup - group that will be given the privilege
	 *
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession perunSession, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Removes an administrator form the group.
	 *
	 * @param perunSession
	 * @param group
	 * @param user
	 *
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, Group group, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Removes a group administrator of the group.
	 *
	 * @param perunSession
	 * @param group
	 * @param authorizedGroup group that will be removed the privilege
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Gets list of all user administrators of this group.
	 * If some group is administrator of the given group, all members are included in the list.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 *
	 * @return list of administrators
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

	/**
	 * Gets list of all group administrators of this group.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 *
	 * @return list of group administrators
	 */
	List<Group> getAdminGroups(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * Gets list of all administrators of this group like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @throws  UserNotExistsException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException;

	/**
	 * Gets list of all administrators of this group, which are assigned directly, like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @throws  UserNotExistsException
	 */
	List<RichUser> getDirectRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException;

	/**
	 * Gets list of all administrators of this group like RichUsers with attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get list of Group administrators with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param group
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get list of Group administrators, which are directly assigned (not by group membership) with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param group
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException;


	/**
	 * Get all groups of users under the VO.
	 *
	 * @param sess
	 * @param vo vo
	 *
	 * @throws InternalErrorException
	 *
	 * @return list of groups
	 */
	List<Group> getGroups(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get groups by theirs Id.
	 *
	 * @param sess
	 * @param groupsIds
	 * @return list of groups
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByIds(PerunSession sess, List<Integer> groupsIds) throws InternalErrorException;

	/**
	 * @param sess
	 * @param vo
	 *
	 * @return count of VO's groups
	 *
	 * @throws InternalErrorException
	 */
	int getGroupsCount(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Returns number of immediate subgroups of the parent group.
	 *
	 * @param sess
	 * @param parentGroup
	 *
	 * @return count of parent group immediate subgroups
	 *
	 * @throws InternalErrorException
	 */
	int getSubGroupsCount(PerunSession sess, Group parentGroup) throws InternalErrorException;

	/**
	 * Gets the Vo which is owner of the group.
	 *
	 * @param sess
	 * @param group
	 *
	 * @return Vo which is owner of the group.
	 *
	 * @throws InternalErrorException
	 */
	Vo getVo(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get members from parent group. If the parent group doesn't exist (this is top level group) return all VO (from which the group is) members instead.
	 *
	 * @param sess
	 * @param group
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getParentGroupMembers(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get members form the parent group in RichMember format.
	 * @param sess
	 * @param group
	 * @return list of parent group rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get members form the parent group in RichMember format including user/member attributes.
	 * @param sess
	 * @param group
	 * @return list of parent group rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Synchronizes the group with the external group without checking if the synchronization is already in progress.
	 *
	 * @param sess
	 * @param group
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberAlreadyRemovedException if there is at least one member who need to be deleted, but DB returns 0 affected rows
	 */
	void synchronizeGroup(PerunSession sess, Group group) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, MemberAlreadyRemovedException;

	/**
	 * Synchronize the group with external group. It checks if the synchronization of the same group is already in progress.
	 *
	 * @param sess
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupSynchronizationAlreadyRunningException
	 */
	void forceGroupSynchronization(PerunSession sess, Group group) throws InternalErrorException, GroupSynchronizationAlreadyRunningException;

	/**
	 * Synchronize all groups which have enabled synchronization. This method is run by the scheduler every 5 minutes.
	 *
	 * @throws InternalErrorException
	 */
	void synchronizeGroups(PerunSession sess) throws InternalErrorException;

	/**
	 * Returns all member's groups. Except members and administrators groups.
	 *
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	List<Group> getMemberGroups(PerunSession sess, Member member) throws InternalErrorException;

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
	 * Returns all member's groups which are assigned to at least one resource. Except members and administrators groups.
	 *
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	List<Group> getMemberGroupsForResources(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Returns all groups which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return list of groups
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Group> getGroupsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Returns all group-resource which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Pair<Group, Resource>> getGroupResourcePairsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

	/**
	 * Return true if Member is member of the Group
	 *
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
	 * !!! Not Complete yet, need to implement all perunBeans !!!
	 *
	 * Get perunBean and try to find all connected Groups
	 *
	 * @param sess
	 * @param perunBean
	 * @return list of groups connected with perunBeans
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException;

	void checkGroupExists(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException;

	/**
	 * This method take list of members (also with duplicit) and:
	 * 1] add all members with direct membership to target list
	 * 2] add all members with indirect membership who are not already in target list to the target list
	 *
	 * @param members list of members to filtering
	 * @return filteredMembers list of members without duplicit after filtering
	 */
	List<Member> filterMembersByMembershipTypeInGroup(List<Member> members) throws InternalErrorException;

}
