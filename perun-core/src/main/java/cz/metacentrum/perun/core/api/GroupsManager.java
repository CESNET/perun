package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Map;

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
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationAlreadyExists;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationCannotBeRemoved;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationDoesNotExist;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationNotAllowed;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
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
 * @author  Jan Zverina
 * @see Perun
 */
public interface GroupsManager {

	// Attributes related to the external groups
	// Contains query need to get the group members
	String GROUPMEMBERSQUERY_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersQuery";
	// Contains optional filter for members in group
	String GROUPMEMBERSFILTER_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersFilter";
	// Define the external source used for accessing the data about external group
	String GROUPEXTSOURCE_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupExtSource";
	// Define the external source used for accessing the data about the group members
	String GROUPMEMBERSEXTSOURCE_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembersExtSource";
	// If the synchronization is enabled/disabled, value is true/false
	String GROUPSYNCHROENABLED_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":synchronizationEnabled";
	// Defines the interval, when the group has to be synchronized. It is fold of 5 minutes
	String GROUPSYNCHROINTERVAL_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":synchronizationInterval";
	// Defines if we want to skip updating already existing members in group from extSource (updating attributes etc.)
	String GROUPLIGHTWEIGHTSYNCHRONIZATION_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":lightweightSynchronization";
	// Defines attribute which stores group value for change detection
	String GROUPCHANGEDETECTION_ATTRNAME = AttributesManager.NS_GROUP_ATTR_OPT + ":groupChangeDetectionValue";
	// Defines attribute which stores group value for change detection
	String GROUPCHANGEDETECTIONQUERY_ATTRNAME = AttributesManager.NS_GROUP_ATTR_OPT + ":groupChangeDetectionQuery";
	// Contains query need to get the modified group members
	String GROUPMODIFIEDMEMBERSQUERY_ATTRNAME = AttributesManager.NS_GROUP_ATTR_OPT + ":groupModifiedMembersQuery";
	// Defines timestamp with start of last successful synchronization
	String GROUPSTARTOFLASTSUCCESSSYNC_ATTRNAME = AttributesManager.NS_GROUP_ATTR_DEF + ":startOfLastSuccessSynchronizationTimestamp";

	String GROUP_SHORT_NAME_REGEXP = "^[-a-zA-Z.0-9_ ]+$";
	String GROUP_FULL_NAME_REGEXP = "^[-a-zA-Z.0-9_ ]+([:][-a-zA-Z.0-9_ ]+)*";

	// Detects that group synchronization is obtaining all data about members from external source
	String GROUP_SYNC_STATUS_FULL = "full";
	// Detects that group synchronization is lightweight
	String GROUP_SYNC_STATUS_LIGHTWEIGHT = "lightweight";
	// Detects that group synchronization is obtaining only modified data about members from external source
	String GROUP_SYNC_STATUS_MODIFIED = "modified";

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
	 * @throws GroupNotExistsException
	 * @throws GroupExistsException
	 * @throws PrivilegeException
	 * @throws GroupRelationNotAllowed
	 * @throws GroupRelationAlreadyExists
	 */
	Group createGroup(PerunSession perunSession, Group parentGroup, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException, InternalErrorException, GroupRelationNotAllowed, GroupRelationAlreadyExists;

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
	void deleteGroup(PerunSession perunSession, Group group, boolean forceDelete) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

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
	void deleteGroup(PerunSession perunSession, Group group) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

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
	void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws GroupNotExistsException, InternalErrorException, PrivilegeException, GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

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
	 * @throws GroupRelationDoesNotExist
	 * @throws GroupRelationCannotBeRemoved
	 */
	void deleteAllGroups(PerunSession perunSession, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

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
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws ExternallyManagedException
	 */
	void addMember(PerunSession perunSession, Group group, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException;

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
	 * @throws WrongAttributeAssignmentException
	 * @throws AttributeNotExistsException
	 * @throws ExternallyManagedException
	 */
	void removeMember(PerunSession perunSession, Group group, Member member) throws InternalErrorException, MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException;

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
	 * Return all direct group members.
	 *
	 * @param perunSession perun session
	 * @param group group
	 * @return list of direct members
	 * @throws InternalErrorException internal error
	 * @throws PrivilegeException insufficient permission
	 * @throws GroupNotExistsException when group does not exist
	 */
	List<Member> getGroupDirectMembers(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;;

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
	 * Returns direct group members in the RichMember object, which contains Member+User data.
	 *
	 * @param sess session
	 * @param group group
	 * @return list of direct RichMembers
	 * @throws InternalErrorException internal error
	 * @throws PrivilegeException insufficient permission
	 * @throws GroupNotExistsException when group does not exist
	 */
	List<RichMember> getGroupDirectRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

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
	boolean isGroupMember(PerunSession sess, Group group, Member member) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

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
	List<User> getAdmins(PerunSession perunSession, Group group, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

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
	List<RichUser> getRichAdmins(PerunSession perunSession, Group group, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException, PrivilegeException, GroupNotExistsException;

	/**
	 * Get list of all richGroups with selected attributes assigned to resource.
	 * Allowed namespaces of attributes are group and group-resource.
	 *
	 * Last step is filtration of attributes:
	 * Attributes are filtered by rights of user in session. User get only those selected attributes he has rights to read.
	 *
	 * @param sess
	 * @param resource resource to get assigned groups for
	 * @param attrNames If empty, return all non-empty attributes. If not empty, return all selected attributes in allowed namespaces.
	 * @return
	 * @throws InternalErrorException
	 * @throws ResourceNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichGroup> getRichGroupsAssignedToResourceWithAttributesByNames(PerunSession sess, Resource resource, List<String> attrNames) throws InternalErrorException, ResourceNotExistsException, PrivilegeException;

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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
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
	@Deprecated
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException;

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
	 * @throws UserNotExistsException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException;


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
	 * Get count of all groups
	 *
	 * @param sess
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 *
	 * @return count of all groups
	 */
	int getGroupsCount(PerunSession sess) throws InternalErrorException, PrivilegeException;

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
	 * @throws VoNotExistsException
	 * @throws MemberNotExistsException
	 * @throws AttributeNotExistsException
	 */
	List<Group> getMemberGroupsByAttribute(PerunSession sess, Member member, Attribute attribute) throws PrivilegeException, VoNotExistsException, WrongAttributeAssignmentException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException;

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
	List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames) throws InternalErrorException, VoNotExistsException, PrivilegeException;

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
	List<RichGroup> getRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) throws InternalErrorException, GroupNotExistsException, VoNotExistsException, PrivilegeException;

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
	List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) throws InternalErrorException, GroupNotExistsException, VoNotExistsException, PrivilegeException;

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
	RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames) throws InternalErrorException, GroupNotExistsException, VoNotExistsException, PrivilegeException;

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
	Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupRelationNotAllowed, GroupRelationAlreadyExists, WrongAttributeValueException, WrongReferenceAttributeValueException;

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
	void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved;

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
	List<Group> getGroupUnions(PerunSession sess, Group group, boolean reverseDirection) throws InternalErrorException, GroupNotExistsException, PrivilegeException;

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
	void moveGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupMoveNotAllowedException, WrongAttributeValueException, WrongReferenceAttributeValueException;

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
	Member setMemberGroupStatus(PerunSession sess, Member member, Group group, MemberGroupStatus status) throws InternalErrorException, GroupNotExistsException, MemberNotExistsException, PrivilegeException, NotGroupMemberException;

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
	Member getGroupMemberById(PerunSession sess, Group group, int memberId) throws InternalErrorException, NotGroupMemberException, GroupNotExistsException, PrivilegeException;

	/**
	 * Extend member membership in given group using membershipExpirationRules attribute defined in Group.
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @throws InternalErrorException internal error
	 * @throws ExtendMembershipException extend membership exception
	 */
	void extendMembershipInGroup(PerunSession sess, Member member, Group group) throws InternalErrorException, ExtendMembershipException, PrivilegeException, MemberNotExistsException, GroupNotExistsException;

	/**
	 * Returns true if member in given group can extend membership or if no rules were set for the membershipExpiration
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @return true if given member can extend membership in given group  or if no rules were set for the
	 * membership expiration, false otherwise
	 */
	boolean canExtendMembershipInGroup(PerunSession sess, Member member, Group group) throws InternalErrorException, MemberNotExistsException, GroupNotExistsException, PrivilegeException;

	/**
	 * Returns true if member in given group can extend membership or throws exception with reason why use can't extends membership
	 *
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @throws ExtendMembershipException reason why user can't extend membership
	 * @return true if given member can extend membership in given group or throws exception with reason why not
	 */
	boolean canExtendMembershipInGroupWithReason(PerunSession sess, Member member, Group group) throws InternalErrorException, MemberNotExistsException, GroupNotExistsException, PrivilegeException, ExtendMembershipException;

}
