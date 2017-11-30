package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
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
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupOperationsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationDoesNotExist;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

/**
 * @author  Michal Prochazka
 * @author  Slavek Licehammer
 * @see Perun
 */
public interface GroupsManagerImplApi {


	/**
	 * Creates a new top-level group and associates it with the VO from parameter.
	 *
	 * For this method the new group has always same shortName like Name.
	 * Important: voId in object group is ignored
	 *
	 * @param perunSession
	 * @param vo to associates group with
	 * @param group new group with name without ":"
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
	 * Update shortName and description.
	 * Return Group with correctly set parameters
	 *
	 * @param perunSession
	 * @param group to update (use only ID, shortName and description)
	 *
	 * @return updated group with correctly set parameters
	 *
	 * @throws InternalErrorException
	 */
	Group updateGroup(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * Updates group by ID.
	 *
	 * !! IMPORTANT This method allows to change group name the way it doesn't correspond with the groups hierarchy !!
	 * Meant for updating subgroups after parent group name is updated !!
	 *
	 * Update name (with ":").
	 * Return Group with correctly set parameters
	 *
	 * @param perunSession
	 * @param group to update (use only ID and name)
	 *
	 * @return updated group with correctly set parameters
	 *
	 * @throws InternalErrorException
	 */
	Group updateGroupName(PerunSession perunSession, Group group) throws InternalErrorException;

	/**
	 * Updates parentGroupId.
	 *
	 * !! IMPORTANT This method allows to change parentGroupId, but it doesn't update group and subGroup names !!
	 *
	 * @param perunSession
	 * @param group to update
	 *
	 * @return group with updated parentGroupId
	 *
	 * @throws InternalErrorException
	 */
	Group updateParentGroupId(PerunSession perunSession, Group group) throws InternalErrorException;

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
	 * IMPORTANT: need to use full name of group (ex. 'toplevel:a:b', not the shortname which is in this example 'b')
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
	 * Removes member form the group. The member object MUST have sourceGroupId parameter.
	 *
	 * @param perunSession perun session
	 * @param group group
	 * @param member member
	 *
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 */
	void removeMember(PerunSession perunSession, Group group, Member member) throws InternalErrorException, NotGroupMemberException;

	/**
	 * Return groups by theirs id.
	 *
	 * @param perunSession
	 * @param groupsIds list of group ids
	 *
	 * @return list groups
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByIds(PerunSession perunSession, List<Integer> groupsIds) throws InternalErrorException;

	/**
	 * Return list of assigned groups on the resource.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @return list of groups, which are assigned on the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource) throws InternalErrorException;

	/**
	 * Return list of assigned groups on the resource with specified member.
	 *
	 * @param perunSession
	 * @param resource
	 * @param member
	 *
	 * @return list of groups, which are assigned on the resource with specified member
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource, Member member) throws InternalErrorException;

	/**
	 * Return list of assigned groups from all facility resources
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return list of groups, which are assigned on all facility resources
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssignedGroupsToFacility(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Return group users sorted by name.
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
	 * Return all members groups. Included 'members' group.
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
	 * Get count of all groups.
	 *
	 * @param perunSession
	 *
	 * @return count of all groups
	 *
	 * @throws InternalErrorException
	 */
	int getGroupsCount(PerunSession perunSession) throws InternalErrorException;

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
	 * @throws InternalErrorException
	 */
	public List<Integer> getGroupApplicationIds(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Return list of all reserved logins for specific application
	 * (pair is namespace and login)
	 *
	 * @param appId from which application get reserved logins
	 * @return list of pairs namespace and login
	 * @throws InternalErrorException
	 */
	public List<Pair<String, String>> getApplicationReservedLogins(Integer appId) throws InternalErrorException;

	/**
	 * Delete all Group login reservations
	 *
	 * Reserved logins must be removed from external systems
	 * (e.g. KDC) BEFORE calling this method via deletePassword() in
	 * UsersManager.
	 *
	 * @param sess
	 * @param group Group to delete all login reservations for
	 * @throws InternalErrorException
	 */
	public void deleteGroupReservedLogins(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get all groups in specific vo with assigned extSource
	 *
	 * @param sess
	 * @param source
	 * @param vo
	 * @return l
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWithAssignedExtSourceInVo(PerunSession sess, ExtSource source, Vo vo) throws InternalErrorException;

	/**
	 * Removes a union between two groups.
	 *
	 * @param sess perun session
	 * @param resultGroup result group
	 * @param operandGroup operand group
	 *
	 * @throws InternalErrorException
	 * @throws GroupRelationDoesNotExist if the union between the two groups does not exist
	 */
	void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws InternalErrorException, GroupRelationDoesNotExist;

	/**
	 * Removes all relations of this result group.
	 *
	 * @param sess perun session
	 * @param resultGroup result group
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	void removeResultGroupRelations(PerunSession sess, Group resultGroup) throws InternalErrorException;

	/**
	 * Saves union operation between result group and operand group.
	 * @param sess perun session
	 * @param resultGroup group to which members are added
	 * @param operandGroup group from which members are taken
	 * @param parentFlag if true union cannot be deleted; false otherwise (it flags relations created by hierarchical structure)
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	void saveGroupRelation(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws InternalErrorException;

	/**
	 * Checks if relation between groups exists. It checks both ways.
	 * Does not matter which one is result group and which one is operand group.
	 *
	 * @param group1 group
	 * @param group2 group
	 * @return true if there is a relation, false otherwise
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	boolean isRelationBetweenGroups(Group group1, Group group2) throws InternalErrorException;

	/**
	 * Check if the relation between given groups can be deleted.
	 * Determined by parent flag (it flags relations created by hierarchical structure).
	 * It matters which group is resultGroup and which is operandGroup!!!
	 *
	 * @return true if it can be deleted; false otherwise
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	boolean isRelationRemovable(PerunSession sess, Group resultGroup, Group operandGroup) throws InternalErrorException;

	/**
	 * Checks if relation exists between result group and operand group.
	 * It matters which one is result group and which one is operand group.
	 *
	 * @param resultGroup result group
	 * @param operandGroup operand group
	 * @return true if there is a one-way relation, false otherwise
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	boolean isOneWayRelationBetweenGroups(Group resultGroup, Group operandGroup) throws InternalErrorException;

	/**
	 * Return all result groups of requested operand group.
	 *
	 * @param sess perun session
	 * @param groupId group id
	 * @return list of Group objects
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	List<Group> getResultGroups(PerunSession sess, int groupId) throws InternalErrorException;

	/**
	 * Return all operand groups of requested result group.
	 *
	 * @param sess perun session
	 * @param groupId group id
	 * @return list of Group objects
	 */
	List<Group> getOperandGroups(PerunSession sess, int groupId) throws InternalErrorException;

	/**
	 * Return list of all result groups ids of requested operand group.
	 *
	 * @param sess perun session
	 * @param groupId group id
	 * @return list of group ids
	 * @throws InternalErrorException
	 */
	List<Integer> getResultGroupsIds(PerunSession sess, int groupId) throws InternalErrorException;
}
