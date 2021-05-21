package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupMoveNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAllowedToAutoRegistrationException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationAlreadyExists;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationCannotBeRemoved;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationDoesNotExist;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationNotAllowed;
import cz.metacentrum.perun.core.api.exceptions.GroupStructureSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationNotEnabledException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidGroupNameException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

import java.util.List;
import java.util.Map;

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
	// Contains query need to get the subject groups
	String GROUPSQUERY_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupsQuery";
	// Contains query need to get the group members
	String GROUPMEMBERSQUERY_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersQuery";
	// Contains optional filter for members in group
	String GROUPMEMBERSFILTER_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersFilter";
	// Define the external source used for accessing the data about external group
	String GROUPEXTSOURCE_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupExtSource";
	// Define the external source used for accessing the data about the group members
	String GROUPMEMBERSEXTSOURCE_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersExtSource";
	// Define name of attribute in Perun where logins are saved for a group structure
	String GROUPS_STRUCTURE_LOGIN_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupStructureLogin";
	//Define prefix of group's login in the structure
	String GROUPS_STRUCTURE_LOGIN_PREFIX_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupStructureLoginPrefix";
	// If the synchronization is enabled/disabled, value is true/false
	String GROUPSYNCHROENABLED_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":synchronizationEnabled";
	// If the group structure synchronization is enabled/disabled, value is true/false
	String GROUPS_STRUCTURE_SYNCHRO_ENABLED_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupStructureSynchronizationEnabled";
	// Defines the interval, when the group has to be synchronized. It is fold of 5 minutes
	String GROUPSYNCHROINTERVAL_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":synchronizationInterval";
	// Defines the interval, when the group structure has to be synchronized. It is fold of 5 minutes
	String GROUP_STRUCTURE_SYNCHRO_INTERVAL_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupStructureSynchronizationInterval";
	// Defines if we want to skip updating already existing members in group from extSource (updating attributes etc.)
	String GROUPLIGHTWEIGHTSYNCHRONIZATION_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization";
	// Defines if we want to synchronize group structure without group hierarchy
	String GROUP_FLAT_SYNCHRONIZATION_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":flatGroupStructureEnabled";
	// Defines the times, when the group has to be synchronized.
	String GROUP_SYNCHRO_TIMES_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupSynchronizationTimes";
	// Defines the times, when the group has to be synchronized.
	String GROUP_STRUCTURE_SYNCHRO_TIMES_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupStructureSynchronizationTimes";
	// Defines timestamp with start of last successful synchronization
	String GROUP_START_OF_LAST_SUCCESSFUL_SYNC_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":startOfLastSuccessfulSynchronization";
	// Defines timestamp with start of last synchronization
	String GROUP_START_OF_LAST_SYNC_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":startOfLastSynchronization";

	String GROUP_SHORT_NAME_REGEXP = "^[-a-zA-Z.0-9_ ]+$";
	String GROUP_FULL_NAME_REGEXP = "^[-a-zA-Z.0-9_ ]+([:][-a-zA-Z.0-9_ ]+)*";

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
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws InvalidGroupNameException when the given group name is invalid
	 */
	Group createGroup(PerunSession perunSession, Vo vo, Group group) throws GroupExistsException, PrivilegeException, VoNotExistsException, InvalidGroupNameException;

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
	 * @throws GroupNotExistsException
	 * @throws GroupExistsException
	 * @throws PrivilegeException
	 * @throws GroupRelationNotAllowed
	 * @throws GroupRelationAlreadyExists
	 * @throws InvalidGroupNameException when the given group name is invalid
	 */
	Group createGroup(PerunSession perunSession, Group parentGroup, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException, GroupRelationNotAllowed, GroupRelationAlreadyExists, ExternallyManagedException, InvalidGroupNameException;

	/**
	 * If forceDelete is false, delete only group and if this group has members or subgroups, throw an exception.
	 * If forceDelete is true, delete group with all subgroups, members and administrators, then delete this group.
	 *
	 * @param perunSession
	 * @param group group to delete
	 * @param forceDelete if forceDelete is false, delete group only if is empty and has no subgroups, if is true, delete anyway with all connections
	 *
	 * @throws GroupNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws GroupAlreadyRemovedException
	 * @throws GroupAlreadyRemovedFromResourceException
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteGroup(PerunSession perunSession, Group group, boolean forceDelete) throws GroupNotExistsException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException;

	/**
	 * Deletes group only if has no subgroups and no members. Other way throw exception.
	 * This method is same like deleteGroup(sess, group, false) with false for forceDelete
	 *
	 * @param perunSession
	 * @param group group to delete
	 *
	 * @throws GroupNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RelationExistsException
	 * @throws GroupAlreadyRemovedException
	 * @throws GroupAlreadyRemovedFromResourceException
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteGroup(PerunSession perunSession, Group group) throws GroupNotExistsException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException;

	/**
	 * Delete all groups in list from perun. (Except members group)
	 *
	 * If forceDelete is false, delete groups only if none of them (IN MOMENT OF DELETING) has subgroups and members, in other case throw exception.
	 * if forceDelete is true, delete groups with all subgroups and members.
	 *
	 * Groups are deleted in order: from longest name to the shortest
	 *	- ex: Group A:b:c will be deleted sooner than Group A:b etc.
	 *	- reason for this: with group are deleted its subgroups too
	 *
	 * Important: Groups can be from different VOs.
	 *
	 * @param perunSession
	 * @param groups list of groups to deleted
	 * @param forceDelete if forceDelete is false, delete groups only if all of them have no subgroups and no members, if is true, delete anyway with all connections
	 *
	 * @throws GroupNotExistsException If any group not exists in perun
	 * @throws InternalErrorException
	 * @throws PrivilegeException if user has no right to call delete operation on any of these groups
	 * @throws GroupAlreadyRemovedException if any groups is already deleted
	 * @throws RelationExistsException raise if group has subgroups or member (forceDelete is false)
	 * @throws GroupAlreadyRemovedFromResourceException  if any group is already removed from resource
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws GroupNotExistsException, PrivilegeException, GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException;

	/**
	 * Deletes all groups under the VO except built-in groups (members, admins groups).
	 *
	 * @param perunSession
	 * @param vo VO
	 *
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws GroupAlreadyRemovedException if there is at least 1 group not affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on resource affected by deleting from DB
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteAllGroups(PerunSession perunSession, Vo vo) throws VoNotExistsException, PrivilegeException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

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
	 * @throws GroupExistsException if group with same name already exists in the same VO
	 * @throws PrivilegeException
	 * @throws InvalidGroupNameException when the given group name is invalid
	 */
	Group updateGroup(PerunSession perunSession, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException, InvalidGroupNameException;

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
	 */
	Group getGroupById(PerunSession perunSession, int id) throws GroupNotExistsException, PrivilegeException;

	/**
	 * Search for the group with specified name in specified VO.
	 *
	 * IMPORTANT: need to use full name of group (ex. 'toplevel:a:b', not the shortname which is in this example 'b')
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
	 * @throws InvalidGroupNameException when the given group name is invalid
	 */
	Group getGroupByName(PerunSession perunSession, Vo vo, String name) throws GroupNotExistsException, PrivilegeException, VoNotExistsException, InvalidGroupNameException;

	/**
	 * Search for the groups with specified ids in all VOs.
	 *
	 * @param ids
	 * @param perunSession
	 *
	 * @return groups with specified ids
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Group> getGroupsByIds(PerunSession perunSession, List<Integer> ids) throws PrivilegeException;

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
	 * @throws WrongAttributeValueException if any member attribute value, required by resource (on which the group is assigned), is wrong
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws ExternallyManagedException
	 */
	void addMember(PerunSession perunSession, Group group, Member member) throws MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException;


	/**
	 * Return true if Member is direct member of the Group
	 *
	 *
	 * @param sess session
	 * @param group group where the membership is to be checked
	 * @param member member whose membership is to be checked
	 * @return true if Member is direct member of the Group
	 *
	 * @throws InternalErrorException
	 */
	boolean isDirectGroupMember(PerunSession sess, Group group, Member member) throws GroupNotExistsException, PrivilegeException;




	/**
	 * Adds member of the VO to the groups in the same VO.
	 *
	 * @param perunSession
	 * @param groups list of groups, the member will be added to
	 * @param member member to be added
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 * @throws AlreadyMemberException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeValueException if any member attribute value, required by resource (on which the group is assigned), is wrong
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws ExternallyManagedException
	 */
	void addMember(PerunSession perunSession, List<Group> groups, Member member) throws MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException;

	/**
	 * Adds members of the VO to the group in the same VO.
	 *
	 * @param perunSession
	 * @param group list of groups, the member will be added to
	 * @param members member to be added
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 * @throws AlreadyMemberException
	 * @throws GroupNotExistsException
	 * @throws WrongAttributeValueException if any member attribute value, required by resource (on which the group is assigned), is wrong
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws ExternallyManagedException
	 */
	void addMembers(PerunSession perunSession, Group group, List<Member> members) throws MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException;



	/**
	 * Removes member form the group.
	 *
	 * @param perunSession
	 * @param member Member to be removed
	 * @param group group, from which the member is to be removed
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException when member doesn't exist
	 * @throws NotGroupMemberException  when member is not in the group
	 * @throws GroupNotExistsException when the group doesn't exist
	 * @throws WrongAttributeAssignmentException when assigning atribute to wrong entity
	 * @throws AttributeNotExistsException when attribute doesn't exist
	 * @throws ExternallyManagedException when the group is externally managed
	 */
	void removeMember(PerunSession perunSession, Group group, Member member) throws MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException;

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
	 */
	List<Member> getGroupMembers(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Removes a member from a list of groups.
	 *
	 * @param perunSession
	 * @param member Member to be removed
	 * @param groups list of groups, from which the member is to be removed, can be empty
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException when member doesn't exist
	 * @throws NotGroupMemberException  when member is not in the group
	 * @throws GroupNotExistsException when the group doesn't exist
	 * @throws WrongAttributeAssignmentException when assigning atribute to wrong entity
	 * @throws AttributeNotExistsException when attribute doesn't exist
	 * @throws ExternallyManagedException when the group is externally managed
	 */
	void removeMember(PerunSession perunSession, Member member, List<Group> groups) throws MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException;


	/**
	 * Removes members from a group.
	 *
	 * @param perunSession
	 * @param members list of members to be removed, can be empty
	 * @param group group, from which the members are to be removed
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException when member doesn't exist
	 * @throws NotGroupMemberException  when member is not in the group
	 * @throws GroupNotExistsException when the group doesn't exist
	 * @throws WrongAttributeAssignmentException when assigning atribute to wrong entity
	 * @throws AttributeNotExistsException when attribute doesn't exist
	 * @throws ExternallyManagedException when the group is externally managed
	 */
	void removeMembers(PerunSession perunSession, Group group, List<Member> members) throws MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException;


	/**
	 * Return all direct group members.
	 *
	 * @param perunSession perun session
	 * @param group group
	 * @return list of direct members
	 * @throws InternalErrorException internal error
	 * @throws PrivilegeException insufficient permission
	 * @throws GroupNotExistsException when group does not exist
	 */
	List<Member> getGroupDirectMembers(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Return all members of the group who are active (valid) in the group.
	 *
	 * Do not return expired members of the group.
	 *
	 * @param sess perun session
	 * @param group to get members from
	 * @return list of active (valid) members
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException insufficient permission
	 * @throws GroupNotExistsException when group does not exist
	 */
	List<Member> getActiveGroupMembers(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Return all members of the group who are inactive (expired) in the group.
	 *
	 * Do not return active members of the group.
	 *
	 * @param sess perun session
	 * @param group to get members from
	 * @return list of inactive (expired) members
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException insufficient permission
	 * @throws GroupNotExistsException when group does not exist
	 */
	List<Member> getInactiveGroupMembers(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

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
	 */
	List<Member> getGroupMembers(PerunSession perunSession, Group group, Status status) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Returns direct group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess session
	 * @param group group
	 * @return list of direct RichMembers
	 * @throws InternalErrorException internal error
	 * @throws PrivilegeException insufficient permission
	 * @throws GroupNotExistsException when group does not exist
	 */
	List<RichMember> getGroupDirectRichMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Return true if Member is member of the Group
	 *
	 * @param sess
	 * @param group
	 * @param member
	 * @return true if Member is member of the Group
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	boolean isGroupMember(PerunSession sess, Group group, Member member) throws PrivilegeException, GroupNotExistsException, MemberNotExistsException;

	/**
	 * @param perunSession
	 * @param group
	 *
	 * @return count of members of specified group
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	int getGroupMembersCount(PerunSession perunSession, Group group) throws GroupNotExistsException, PrivilegeException;

	/**
	 * Returns counts of group members by their status in VO.
	 *
	 * @param sess
	 * @param group
	 * @return map of member status in VO to count of group members with the status
	 * @throws GroupNotExistsException when the group doesn't exist
	 * @throws PrivilegeException
	 */
	Map<Status, Integer> getGroupMembersCountsByVoStatus(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException;

	/**
	 * Returns counts of group members by their group status.
	 *
	 * @param sess
	 * @param group
	 * @return map of member status in group to count of group members with the status
	 * @throws GroupNotExistsException when the group doesn't exist
	 * @throws PrivilegeException
	 */
	Map<MemberGroupStatus, Integer> getGroupMembersCountsByGroupStatus(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException;

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
	List<Group> getAllGroups(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

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
	Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

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
	Group getParentGroup(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException, ParentGroupNotExistsException;

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
	List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws PrivilegeException, GroupNotExistsException;

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
	List<Group> getAllSubGroups(PerunSession sess, Group parentGroup) throws PrivilegeException, GroupNotExistsException;

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
	 * @throws UserNotExistsException
	 */
	void addAdmin(PerunSession perunSession, Group group,  User user) throws AlreadyAdminException, PrivilegeException, GroupNotExistsException, UserNotExistsException, RoleCannotBeManagedException;

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
	 */
	void addAdmin(PerunSession perunSession, Group group,  Group authorizedGroup) throws AlreadyAdminException, PrivilegeException, GroupNotExistsException, RoleCannotBeManagedException;


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
	 * @throws UserNotAdminException
	 * @throws UserNotExistsException
	 */
	void removeAdmin(PerunSession perunSession, Group group, User user) throws PrivilegeException, GroupNotExistsException, UserNotAdminException, UserNotExistsException, RoleCannotBeManagedException;

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
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, Group group, Group authorizedGroup) throws PrivilegeException, GroupNotExistsException, GroupNotAdminException, RoleCannotBeManagedException;

	/**
	 * Get list of all user administrators for supported role and specific group.
	 *
	 * If onlyDirectAdmins is true, return only direct users of the group for supported role.
	 *
	 * Supported roles: GroupAdmin
	 *
	 * @param perunSession
	 * @param group
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of all user administrators of the given group for supported role
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	List<User> getAdmins(PerunSession perunSession, Group group, boolean onlyDirectAdmins) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Get list of all richUser administrators for the group and supported role with specific attributes.
	 *
	 * Supported roles: GroupAdmin
	 *
	 * If "onlyDirectAdmins" is "true", return only direct users of the group for supported role with specific attributes.
	 * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @param specificAttributes list of specified attributes which are needed in object richUser
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of RichUser administrators for the group and supported role with attributes
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws GroupNotExistsException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Group group, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws UserNotExistsException, PrivilegeException, GroupNotExistsException;

	/**
	 * Get list of all richGroups with selected attributes assigned to resource.
	 * Allowed namespaces of attributes are group and group-resource.
	 *
	 * Last step is filtration of attributes:
	 * Attributes are filtered by rights of user in session. User get only those selected attributes he has rights to read.
	 *
	 * @param sess
	 * @param resource resource to get assigned groups for
	 * @param attrNames list of selected attribute names,
	 *                  if it is null, return all possible non-empty attributes,
	 *                  empty list in attrNames means - no attributes needed
	 * @return list of RichGroup objects with specific attributes specified by object Resource and object Member.
	 * @return
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichGroup> getRichGroupsAssignedToResourceWithAttributesByNames(PerunSession sess, Resource resource, List<String> attrNames) throws ResourceNotExistsException, PrivilegeException;

	/**
	 * Get list of all richGroups with selected attributes assigned to the resource filtered by specific member.
	 * Allowed namespaces of attributes are group, group-resource, member-group
	 *
	 * Last step is filtration of attributes:
	 * Attributes are filtered by rights of user in session. User get only those selected attributes he has rights to read.
	 *
	 * @param sess
	 * @param member member used for filtering returned groups (groups have to contain this member to be returned)
	 * @param resource resource to get assigned groups for
	 * @param attrNames list of selected attribute names,
	 *                  if it is null, return all possible non-empty attributes,
	 *                  empty list in attrNames means - no attributes needed
	 * @return list of RichGroup objects with specific attributes specified by object Resource and object Member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichGroup> getRichGroupsAssignedToResourceWithAttributesByNames(PerunSession sess, Member member, Resource resource, List<String> attrNames) throws ResourceNotExistsException, PrivilegeException, MemberNotExistsException;

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
	 */
	@Deprecated
	List<User> getAdmins(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

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
	 */
	@Deprecated
	List<User> getDirectAdmins(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Gets list of all group administrators of given group.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 *
	 * @return list of all group administrators of the given group
	 */
	List<Group> getAdminGroups(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Gets list of all administrators of this group like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	@Deprecated
	List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException;

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
	 */
	@Deprecated
	List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException, UserNotExistsException;

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
	 */
	@Deprecated
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Get list of Group administrators, which are directly assigned (not by group membership) with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param group
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws PrivilegeException, GroupNotExistsException;


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
	List<Group> getGroups(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

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
	int getGroupsCount(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

	/**
	 * Get count of all groups
	 *
	 * @param sess
	 *
	 * @throws InternalErrorException
	 * @return count of all groups
	 */
	int getGroupsCount(PerunSession sess);

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
	int getSubGroupsCount(PerunSession sess, Group parentGroup) throws PrivilegeException, GroupNotExistsException;

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
	Vo getVo(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException;

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
	List<Member> getParentGroupMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Get members form the parent group in RichMember format.
	 * @param sess
	 * @param group
	 * @return list of parent group rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Get members form the parent group in RichMember format including user/member attributes.
	 * @param sess
	 * @param group
	 * @return list of parent group rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException;

	/**
	 * Synchronizes the group with the external group.
	 *
	 * @param sess
	 * @param group
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 * @throws GroupSynchronizationAlreadyRunningException when synchronization for the group is already running
	 * @throws GroupSynchronizationNotEnabledException when group doesn't have synchronization enabled
	 */
	void forceGroupSynchronization(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException, GroupSynchronizationAlreadyRunningException, GroupSynchronizationNotEnabledException;

	/**
	 * Force synchronization for all subgroups (recursively - whole tree) of the group (useful for group structure)
	 *
	 * @param sess
	 * @param group the group where all its subgroups will be forced to synchronize
	 *
	 * @throws PrivilegeException user is not privileged to call this method
	 * @throws GroupNotExistsException when group not exists in Perun
	 */
	void forceAllSubGroupsSynchronization(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException;

    /**
     * Puts the group on the first place to the queue of groups waiting for group structure synchronization.
     *
     * @param sess
     * @param group
     * @throws InternalErrorException
     * @throws GroupNotExistsException
     * @throws PrivilegeException
     * @throws GroupStructureSynchronizationAlreadyRunningException
     */
    void forceGroupStructureSynchronization(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException, GroupStructureSynchronizationAlreadyRunningException;

	/**
	 * Synchronize all groups which have enabled synchronization. This method is run by the scheduler every 5 minutes.
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void synchronizeGroups(PerunSession sess) throws PrivilegeException;

	/**
	 * Synchronize all groups structures (with members) which have enabled group structure synchronization. This method is run by the scheduler every 5 minutes.
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void synchronizeGroupsStructures(PerunSession sess) throws PrivilegeException;

	/**
	 * Returns all member's groups. Except members groups.
	 *
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	List<Group> getMemberGroups(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

	/**
	 * Method return list of groups for selected member which (groups) has set specific attribute.
	 * Attribute can be only from namespace "GROUP"
	 *
	 * @param sess sess
	 * @param member memer
	 * @param attribute attribute from "GROUP" namespace
	 *
	 * @return list of groups which contain member and have attribute with same value
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws WrongAttributeAssignmentException
	 * @throws MemberNotExistsException
	 * @throws AttributeNotExistsException
	 */
	List<Group> getMemberGroupsByAttribute(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, WrongAttributeAssignmentException, MemberNotExistsException, AttributeNotExistsException;

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
	List<Group> getAllMemberGroups(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

	/**
	 * Returns all member's groups where member is in active state (is valid there)
	 * Excluded members group.
	 *
	 * @param sess perun session
	 * @param member member to get groups for
	 * @return list of groups where member is in active state (valid)
	 *
	 * @throws MemberNotExistsException member in parameter not exists in perun
	 * @throws PrivilegeException user is not privileged to call this method
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereMemberIsActive(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

	/**
	 * Returns all member's groups where member is in inactive state (it is not valid and it is expired there)
	 * Excluded members group.
	 *
	 * @param sess perun session
	 * @param member member to get groups for
	 * @return list of groups where member is in inactive state (expired)
	 *
	 * @throws MemberNotExistsException member in parameter not exists in perun
	 * @throws PrivilegeException user is not privileged to call this method
	 * @throws InternalErrorException
	 */
	List<Group> getGroupsWhereMemberIsInactive(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

	/**
	 * Returns all member's groups where member is in active state (is valid there)
	 * Included members group.
	 *
	 * @param sess perun session
	 * @param member member to get groups for
	 * @return list of groups where member is in active state (valid)
	 *
	 * @throws MemberNotExistsException member in parameter not exists in perun
	 * @throws PrivilegeException user is not privileged to call this method
	 * @throws InternalErrorException
	 */
	List<Group> getAllGroupsWhereMemberIsActive(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

	/**
	 * Return all RichGroups for specified member, containing selected attributes.
	 * "members" group is not included.
	 *
	 * Supported are attributes from these namespaces:
	 *  - group
	 *  - member-group
	 *
	 * @param sess internal session
	 * @param member the member to get the rich groups for
	 * @param attrNames list of selected attributes from supported namespaces
	 * @return list of rich groups with selected attributes
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichGroup> getMemberRichGroupsWithAttributesByNames(PerunSession sess, Member member, List<String> attrNames) throws MemberNotExistsException, PrivilegeException;

	/**
	 * Return all RichGroups containing selected attributes
	 *
	 * @param sess
	 * @param vo
	 * @param attrNames if attrNames is null method will return RichGroups containing all attributes
	 * @return List of RichGroups
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 */
	List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames) throws VoNotExistsException, PrivilegeException;

	/**
	 * Return RichSubGroups in parentGroup (only 1 level subgroups) containing selected attributes
	 *
	 * @param sess
	 * @param parentGroup
	 * @param attrNames if attrNames is null method will return RichGroups containing all attributes
	 * @return List of RichGroups
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	List<RichGroup> getRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) throws GroupNotExistsException, PrivilegeException;

	/**
	 * Return all RichSubGroups in parentGroup (all levels sub groups) containing selected attributes
	 *
	 * @param sess
	 * @param parentGroup
	 * @param attrNames if attrNames is null method will return RichGroups containing all attributes
	 * @return List of RichGroups
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) throws GroupNotExistsException, PrivilegeException;

	/**
	 * Return RichGroup selected by id containing selected attributes
	 *
	 * @param sess
	 * @param groupId
	 * @param attrNames if attrNames is null method will return RichGroup containing all attributes
	 * @return RichGroup
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 */
	RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames) throws GroupNotExistsException, PrivilegeException;

	/**
	 * Performs union operation on two groups. Members from operand group are added to result group as indirect.
	 *
	 * @param sess perun session
	 * @param resultGroup group to which members are added
	 * @param operandGroup group from which members are taken
	 * @return result group
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws GroupRelationNotAllowed
	 * @throws GroupRelationAlreadyExists
	 * @throws PrivilegeException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupNotExistsException, PrivilegeException, GroupRelationNotAllowed, GroupRelationAlreadyExists, WrongAttributeValueException, WrongReferenceAttributeValueException, ExternallyManagedException;

	/**
	 * Removes a union relation between two groups. All indirect members that originate from operand group are removed from result group.
	 *
	 * @param sess perun session
	 * @param resultGroup group from which members are removed
	 * @param operandGroup group which members are removed from result group
	 *
	 * @throws GroupNotExistsException
	 * @throws InternalErrorException
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 * @throws PrivilegeException
	 */
	void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupNotExistsException, PrivilegeException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException;

	/**
	 * Get list of group unions for specified group.
	 * @param sess perun session
	 * @param group group
	 * @param reverseDirection if false get all operand groups of requested result group
	 *                         if true get all result groups of requested operand group
	 * @return list of groups.
	 *
	 * @throws GroupNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Group> getGroupUnions(PerunSession sess, Group group, boolean reverseDirection) throws GroupNotExistsException, PrivilegeException;

	/**
	 * Move one group structure under another group in same vo or as top level group
	 *
	 * @param sess perun session
	 * @param destinationGroup group to which is moving group moved, if it's null group will be moved as top level group
	 * @param movingGroup group which is moved to destination group
	 *
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	void moveGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws GroupNotExistsException, PrivilegeException, GroupMoveNotAllowedException, WrongAttributeValueException, WrongReferenceAttributeValueException, ExternallyManagedException;

	/**
	 * Set Members Group status for specified DIRECT member and group.
	 * Member with newly calculated group membership status is returned.
	 *
	 * Please note, that if member is also sourced from sub-groups or groups in relation
	 * and has VALID status in any of them, then resulting status is still VALID.
	 * In order to really expire such member is to set EXPIRED status also to all
	 * sourcing sub-groups or groups in relation.
	 *
	 * @param sess perun session
	 * @param member member to set status for
	 * @param group group to set status in
	 * @param status status to set (VALID/EXPIRED)
	 * @return Member with newly calculated status.
	 */
	Member setMemberGroupStatus(PerunSession sess, Member member, Group group, MemberGroupStatus status) throws GroupNotExistsException, MemberNotExistsException, PrivilegeException, NotGroupMemberException;

	/**
	 * Get group member by member ID.
	 *
	 * @param sess
	 * @param group
	 * @param memberId
	 * @return Member
	 * @throws InternalErrorException
	 * @throws NotGroupMemberException
	 */
	Member getGroupMemberById(PerunSession sess, Group group, int memberId) throws NotGroupMemberException, GroupNotExistsException, PrivilegeException;

	/**
	 * Extend member membership in given group using membershipExpirationRules attribute defined in Group.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @throws InternalErrorException internal error
	 * @throws ExtendMembershipException extend membership exception
	 */
	void extendMembershipInGroup(PerunSession sess, Member member, Group group) throws ExtendMembershipException, PrivilegeException, MemberNotExistsException, GroupNotExistsException;

	/**
	 * Returns true if member in given group can extend membership or if no rules were set for the membershipExpiration
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @return true if given member can extend membership in given group  or if no rules were set for the
	 * membership expiration, false otherwise
	 */
	boolean canExtendMembershipInGroup(PerunSession sess, Member member, Group group) throws MemberNotExistsException, GroupNotExistsException, PrivilegeException;

	/**
	 * Returns true if member in given group can extend membership or throws exception with reason why use can't extends membership
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @throws ExtendMembershipException reason why user can't extend membership
	 * @return true if given member can extend membership in given group or throws exception with reason why not
	 */
	boolean canExtendMembershipInGroupWithReason(PerunSession sess, Member member, Group group) throws MemberNotExistsException, GroupNotExistsException, PrivilegeException, ExtendMembershipException;

	/**
	 * Get unique paths of groups via which member is indirectly included to the group.
	 * Cuts off after first included group.
	 *
	 * @param sess perun session
	 * @param member member
	 * @param group group in which the member is indirectly included
	 * @return lists of groups [CURRENT GROUP -> SUBGROUP -> ... -> MEMBER'S SOURCE GROUP]
	 */
	List<List<Group>> getIndirectMembershipPaths(PerunSession sess, Member member, Group group) throws MemberNotExistsException, GroupNotExistsException, PrivilegeException;
}
