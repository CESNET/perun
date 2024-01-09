package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsPageQuery;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.FormItemNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationDoesNotExist;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;

import java.util.List;
import java.util.Map;

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
	 * This method sets ids and uuid to the given group object but returns a new
	 * group object loaded from the DB.
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
	Group createGroup(PerunSession perunSession, Vo vo, Group group) throws GroupExistsException;

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
	Group createGroup(PerunSession perunSession, Vo vo, Group parentGroup, Group group) throws GroupExistsException;

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
	void deleteGroup(PerunSession perunSession, Vo vo, Group group) throws GroupAlreadyRemovedException;

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
	 * @throws GroupExistsException if group with same name already exists in the same VO
	 */
	Group updateGroup(PerunSession perunSession, Group group) throws GroupExistsException;

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
	Group updateGroupName(PerunSession perunSession, Group group);

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
	Group updateParentGroupId(PerunSession perunSession, Group group);

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
	Group getGroupById(PerunSession perunSession, int id) throws GroupNotExistsException;

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
	Group getGroupByName(PerunSession perunSession, Vo vo, String name) throws GroupNotExistsException;

	/**
	 * Gets groups by their ids. Silently skips non-existing groups.
	 *
	 * @param ids
	 * @param perunSession
	 *
	 * @return list of groups with specified ids
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByIds(PerunSession perunSession, List<Integer> ids);

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
	 * @return Member with specific MembershipType
	 */
	Member addMember(PerunSession perunSession, Group group,  Member member, MembershipType type, int sourceGroupId) throws AlreadyMemberException;


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
	void removeMember(PerunSession perunSession, Group group, Member member) throws NotGroupMemberException;

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
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource);

	/**
	 * Return list of assigned groups on the resource.
	 * Similar to assigned groups, but does not require ACTIVE group-resource status.
	 *
	 * @param perunSession
	 * @param resource
	 *
	 * @return list of groups, which are assigned on the resource
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssociatedGroupsToResource(PerunSession perunSession, Resource resource);

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
	List<Group> getAssignedGroupsToResource(PerunSession perunSession, Resource resource, Member member);

	/**
	 * Return list of groups associated with the resource with specified member.
	 * Does not require ACTIVE group-resource status.
	 *
	 * @param perunSession
	 * @param resource
	 * @param member
	 *
	 * @return list of groups, which are assigned on the resource with specified member
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssociatedGroupsToResource(PerunSession perunSession, Resource resource, Member member);

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
	List<Group> getAssignedGroupsToFacility(PerunSession perunSession, Facility facility);

	/**
	 * Return list of all associated groups from all facility resources (does not require ACTIVE group-resource status)
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return list of groups, which are associated with all facility resources
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAssociatedGroupsToFacility(PerunSession perunSession, Facility facility);

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
	List<User> getGroupUsers(PerunSession sess, Group group);

	/**
	 * Return groups where user is member.
	 *
	 * @param sess
	 * @param user
	 *
	 * @return list of groups
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getUserGroups(PerunSession sess, User user);

	/**
	 * Return groups where user is member with allowed statuses in vo and group.
	 * If statuses are empty or null, all statuses are used.
	 *
	 * @param sess
	 * @param user
	 * @param memberStatuses allowed statuses of member in VO
	 * @mparam emberGroupStatuses allowed statuses of member in group
	 * @return list of groups
	 * @throws InternalErrorException
	 */
	List<Group> getUserGroups(PerunSession sess, User user, List<Status> memberStatuses, List<MemberGroupStatus> memberGroupStatuses);

	/**
	 * Checks whether the user is member of the group.
	 *
	 * @param sess
	 * @param user
	 * @param group
	 * @return true if the user is member of the group
	 * @throws InternalErrorException
	 */
	boolean isUserMemberOfGroup(PerunSession sess, User user, Group group);

	/**
	 * Return all members groups. Included 'members' group.
	 *
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	List<Group> getAllMemberGroups(PerunSession sess, Member member);

	/**
	 * Returns all member's groups where member is in active state (is valid there)
	 * Included members group.
	 *
	 * @param sess perun session
	 * @param member member to get groups for
	 * @return list of groups where member is in active state (valid)
	 * @throws InternalErrorException
	 */
	List<Group> getAllGroupsWhereMemberIsActive(PerunSession sess, Member member);

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
	List<Member> getGroupMembers(PerunSession sess, Group group, List<Status> statuses, boolean excludeStatusInsteadOfIncludeStatus);

	/**
	 * Get all group members ignoring theirs status.
	 *
	 * @param sess
	 * @param group
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembers(PerunSession sess, Group group);

	/**
	 * Get group members by member ID -> meaning we will get all (DIRECT/INDIRECT)
	 * group memberships for specified member (or user, since it will be the same).
	 *
	 * @param sess
	 * @param group
	 * @param memberId
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> getGroupMembersById(PerunSession sess, Group group, int memberId);

	/**
	 * Get only group members which has given membership type ignoring their status.
	 *
	 * @param sess session
	 * @param group group
	 * @param membershipType type of membership
	 * @return list of direct members
	 * @throws InternalErrorException internal error
	 */
	List<Member> getGroupMembersByMembership(PerunSession sess, Group group, MembershipType membershipType);

	/**
	 * Get all groups from all vos.
	 *
	 * @param sess session
	 * @return list of all groups
	 */
	List<Group> getAllGroups(PerunSession sess);

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
	List<Group> getAllGroups(PerunSession perunSession, Vo vo);


	/**
	 * Get page of groups from the given vo.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param query query with page information
	 *
	 * @return page of requested groups
	 */
	Paginated<Group> getGroupsPage(PerunSession sess, Vo vo, GroupsPageQuery query);

	/**
	 * Get page of subgroups from the given parent group.
	 *
	 * @param sess session
	 * @param group parent group
	 * @param query query with page information
	 *
	 * @return page of requested groups
	 */
	Paginated<Group> getSubgroupsPage(PerunSession sess, Group group, GroupsPageQuery query);

	/**
	 * Get parent group.
	 *
	 * @param sess
	 * @param group
	 * @return parent group
	 * @throws InternalErrorException
	 * @throws ParentGroupNotExistsException
	 */
	Group getParentGroup(PerunSession sess, Group group) throws ParentGroupNotExistsException;

	/**
	 * Get all immediate subgroups of the parent group under the VO.
	 *
	 * @param perunSession
	 * @param parentGroup
	 *
	 * @throws InternalErrorException
	 * @return list of groups
	 */
	List<Group> getSubGroups(PerunSession perunSession, Group parentGroup);

	/**
	 * Gets list of all administrators of this group.
	 * If some group is administrator of the given group, all VALID members are included in the list.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list of all administrators
	 *
	 * @throws InternalErrorException
	 */
	List<User> getAdmins(PerunSession perunSession, Group group);

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
	List<User> getDirectAdmins(PerunSession perunSession, Group group);

	/** Gets list of all group administrators of this group.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @return list of all group administrators
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getGroupAdmins(PerunSession perunSession, Group group);

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
	boolean groupExists(PerunSession perunSession, Group group);

	/**
	 * Check if group exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	void checkGroupExists(PerunSession perunSession, Group group) throws GroupNotExistsException;

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
	List<Group> getGroups(PerunSession perunSession, Vo vo);

	/**
	 * Get all groups ids for given vo.
	 *
	 * @param sess perun session
	 * @param vo vo
	 *
	 * @return list of groups ids
	 */
	List<Integer> getGroupsIds(PerunSession sess, Vo vo);

	/**
	 * @param perunSession
	 * @param vo
	 *
	 * @return count of VO's groups
	 *
	 * @throws InternalErrorException
	 */
	int getGroupsCount(PerunSession perunSession, Vo vo);

	/**
	 * Get count of all groups.
	 *
	 * @param perunSession
	 *
	 * @return count of all groups
	 *
	 * @throws InternalErrorException
	 */
	int getGroupsCount(PerunSession perunSession);

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
	int getSubGroupsCount(PerunSession perunSession, Group parentGroup);

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
	int getVoId(PerunSession perunSession, Group group);

	/**
	 * Gets all groups which have enabled synchronization.
	 *
	 * @param sess
	 * @return list of groups to synchronize
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsToSynchronize(PerunSession sess);

	/**
	 * Gets all groups which have enabled group structure synchronization.
	 *
	 * @param sess
	 * @return list of groups structures to synchronize
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsStructuresToSynchronize(PerunSession sess);

	/**
	 * Returns all groups which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return list of groups
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsByAttribute(PerunSession sess, Attribute attribute);

	/**
	 * Returns all group-resource which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 */
	List<Pair<Group, Resource>> getGroupResourcePairsByAttribute(PerunSession sess, Attribute attribute);

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
	boolean isGroupMember(PerunSession sess, Group group, Member member);


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
	boolean isDirectGroupMember(PerunSession sess, Group group, Member member);

	/**
	 * Return list of IDs of all applications, which belongs to Group.
	 *
	 * @param sess
	 * @param group
	 * @return list of all group applications ids
	 * @throws InternalErrorException
	 */
	List<Integer> getGroupApplicationIds(PerunSession sess, Group group);

	/**
	 * Get all groups in specific vo with assigned extSource
	 *
	 * @param sess
	 * @param source
	 * @param vo
	 * @return l
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWithAssignedExtSourceInVo(PerunSession sess, ExtSource source, Vo vo);

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
	void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupRelationDoesNotExist;

	/**
	 * Removes all relations of this result group.
	 *
	 * @param sess perun session
	 * @param resultGroup result group
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	void removeResultGroupRelations(PerunSession sess, Group resultGroup);

	/**
	 * Saves union operation between result group and operand group.
	 * @param sess perun session
	 * @param resultGroup group to which members are added
	 * @param operandGroup group from which members are taken
	 * @param parentFlag if true union cannot be deleted; false otherwise (it flags relations created by hierarchical structure)
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	void saveGroupRelation(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag);

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
	boolean isRelationBetweenGroups(Group group1, Group group2);

	/**
	 * Check if the relation between given groups can be deleted.
	 * Determined by parent flag (it flags relations created by hierarchical structure).
	 * It matters which group is resultGroup and which is operandGroup!!!
	 *
	 * @return true if it can be deleted; false otherwise
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	boolean isRelationRemovable(PerunSession sess, Group resultGroup, Group operandGroup);

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
	boolean isOneWayRelationBetweenGroups(Group resultGroup, Group operandGroup);

	/**
	 * Return all result groups of requested operand group.
	 *
	 * @param sess perun session
	 * @param groupId group id
	 * @return list of Group objects
	 *
	 * @throws cz.metacentrum.perun.core.api.exceptions.InternalErrorException
	 */
	List<Group> getResultGroups(PerunSession sess, int groupId);

	/**
	 * Return all operand groups of requested result group.
	 *
	 * @param sess perun session
	 * @param groupId group id
	 * @return list of Group objects
	 */
	List<Group> getOperandGroups(PerunSession sess, int groupId);

	/**
	 * Return list of all result groups ids of requested operand group.
	 *
	 * @param sess perun session
	 * @param groupId group id
	 * @return list of group ids
	 * @throws InternalErrorException
	 */
	List<Integer> getResultGroupsIds(PerunSession sess, int groupId);

	/**
	 * Set status of the member to specified status for indirect relations
	 * where the given group is the source group.
	 *
	 * @param member member whose status will be changed
	 * @param group group where member's status will be changed
	 * @param status status that will be set
	 * @throws InternalErrorException internal error
	 */
	void setIndirectGroupStatus(PerunSession sess, Member member, Group group, MemberGroupStatus status);

	/**
	 * Set direct status of the member to specified status in given group.
	 *
	 * @param member member whose status will be changed
	 * @param group group where member's status will be changed
	 * @param status status that will be set
	 * @throws InternalErrorException internal error
	 */
	void setDirectGroupStatus(PerunSession sess, Member member, Group group, MemberGroupStatus status);

	/**
	 * Returns direct members status in given group.
	 * If there is no relation, null is returned.
	 * @param session session
	 * @param member member
	 * @param group group
	 * @return status of member in given group, if there is no relation, null is returned
	 * @throws InternalErrorException internal error
	 */
	MemberGroupStatus getDirectMemberGroupStatus(PerunSession session, Member member, Group group);

	/**
	 * Returns total member's status in given group.
	 * If there is no relation, null is returned.
	 *
	 * @param session session
	 * @param member member
	 * @param group group
	 * @return total status of member in given group, if there is no relation, null is returned
	 * @throws InternalErrorException internal error
	 */
	MemberGroupStatus getTotalMemberGroupStatus(PerunSession session, Member member, Group group);

	/**
	 * Returns total member's status of given members in given group.
	 *
	 * @param session session
	 * @param group group
	 * @param members members
	 * @return total status of members in given group
	 */
	Map<Integer, MemberGroupStatus> getTotalGroupStatusForMembers(PerunSession session, Group group, List<Member> members);

	/**
	 * Returns all facilities where given group is FACILITYADMIN.
	 *
	 * @param session session
	 * @param group group
	 * @return list of all facilities where given group is FACILITYADMIN
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesWhereGroupIsAdmin(PerunSession session, Group group);

	/**
	 * Returns all groups where given group is GROUPADMIN.
	 *
	 * @param session session
	 * @param group group
	 * @return list of all groups where given group is GROUPADMIN
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereGroupIsAdmin(PerunSession session, Group group);

	/**
	 * Returns all vos where given group si VOADMIN.
	 *
	 * @param session session
	 * @param group group
	 * @return list of all vos where given group is VOADMIN
	 * @throws InternalErrorException
	 */
	List<Vo> getVosWhereGroupIsAdmin(PerunSession session, Group group);

	/**
	 * Checks if the given group has any related manager roles
	 *
	 * @param session session
	 * @param group group
	 * @throws InternalErrorException for the SQL error
	 */
	boolean hasGroupAnyManagerRole(PerunSession session, Group group) throws InternalErrorException;

	/**
	 * Removes all manager roles related to the given group
	 *
	 * @param session session
	 * @param group group
	 * @throws InternalErrorException for the SQL error
	 */
	void removeAllManagerRolesOfGroup(PerunSession session, Group group) throws InternalErrorException;

	/**
	 * Returns all groups which can be registered into during vo registration and are representing options of the
	 * specified application form item.
	 *
	 * @param sess session
	 * @param vo vo
	 * @return list of groups
	 */
	List<Group> getGroupsForAutoRegistration(PerunSession sess, Vo vo);

	/**
	 * Returns all groups which can be registered into during any vo registration.
	 * This method serves only for migration to new functionality related to the new table.
	 *
	 * @param sess session
	 * @return list of groups
	 */
	List<Group> getAllGroupsForAutoRegistration(PerunSession sess);

	/**
	 * Returns all groups which can be registered into during vo registration and are representing options of the
	 * specified application form item.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param formItem application form item
	 * @return list of groups
	 */
	List<Group> getGroupsForAutoRegistration(PerunSession sess, Vo vo, ApplicationFormItem formItem);

	/**
	 * Returns all groups which can be registered into during group registration and are representing options of the
	 * specified application form item.
	 *
	 * @param sess session
	 * @param group group
	 * @param formItem application form item
	 * @return list of groups
	 */
	List<Group> getGroupsForAutoRegistration(PerunSession sess, Group group, ApplicationFormItem formItem);

	/**
	 * Deletes group from list of groups which can be registered into during vo registration.
	 *
	 * @param sess session
	 * @param group group to delete
	 */
	void deleteGroupFromAutoRegistration(PerunSession sess, Group group);

	/**
	 * Deletes group from list of groups which can be registered into during vo or group registration
	 * and are representing options of the specified application form item.
	 *
	 * @param sess session
	 * @param group group to delete
	 */
	void deleteGroupFromAutoRegistration(PerunSession sess, Group group, ApplicationFormItem applicationFormItem) throws FormItemNotExistsException;

	/**
	 * Adds group to the list of groups which can be registered into during vo registration.
	 *
	 * @param sess session
	 * @param group group to add
	 */
	void addGroupToAutoRegistration(PerunSession sess, Group group);

	/**
	 * Adds group from list of groups which can be registered into during vo or group registration
	 * and are representing options of the specified application form item.
	 *
	 * @param sess session
	 * @param group group to add
	 */
	void addGroupToAutoRegistration(PerunSession sess, Group group, ApplicationFormItem formItem) throws FormItemNotExistsException;

	/**
	 * Check if group has automatic registration enabled in any form item.
	 *
	 * @param sess session
	 * @param group group to check
	*/
	boolean isGroupForAnyAutoRegistration(PerunSession sess, Group group);

	/**
	 * Check if group has automatic registration enabled in the given form item.
	 *
	 * @param sess session
	 * @param group group to check
	 * @param formItems form items for which the group can be configured
	 */
	boolean isGroupForAutoRegistration(PerunSession sess, Group group, List<Integer> formItems);

	/**
	 * Get parent form for auto registration group where this group is involved in auto registration process
	 *
	 * @param group auto registration group
	 * @return
	 */
	ApplicationForm getParentApplicationFormForAutoRegistrationGroup(Group group);

	/**
	 * Sets flag required for including group to parent vo in a vo hierarchy.
	 * @param sess perun session
	 * @param group group
	 * @param vo parent vo
	 */
	void allowGroupToHierarchicalVo(PerunSession sess, Group group, Vo vo);

	/**
	 * Unsets flag required for including group to parent vo in a vo hierarchy
	 * @param sess perun session
	 * @param group group
	 * @param vo parent vo
	 */
	void disallowGroupToHierarchicalVo(PerunSession sess, Group group, Vo vo);

	/**
	 * Returns flag representing if the group can be included in the (parent) vo's groups
	 * @param sess perun session
	 * @param group group
	 * @param vo parent vo
	 * @return true if group can be included in vo's groups, false otherwise
	 */
	boolean isAllowedGroupToHierarchicalVo(PerunSession sess, Group group, Vo vo);

	/**
	 * Returns all groups which can be included to VO.
	 *
	 * @param sess session
	 * @param vo vo
	 * @return list of allowed groups to hierarchical VO
	 */
	List<Group> getAllAllowedGroupsToHierarchicalVo(PerunSession sess, Vo vo);

	/**
	 * Returns groups which can be included to VO from specific member VO.
	 *
	 * @param sess session
	 * @param vo parent VO
	 * @param memberVo member VO
	 * @return list of allowed groups to hierarchical VO
	 */
	List<Group> getAllAllowedGroupsToHierarchicalVo(PerunSession sess, Vo vo, Vo memberVo);


	/**
	 * Returns groups in which the user is active member. Groups are looked up only for the specified VO.
	 *
	 * @param sess session
	 * @param user user object
	 * @param vo VO object
	 * @return List of groups
	 */
	List<Group> getGroupsWhereUserIsActiveMember(PerunSession sess, User user, Vo vo);
}
