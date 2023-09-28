package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.GroupsPageQuery;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Paginated;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.RoleAssignmentType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.GroupMoveNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationAlreadyExists;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationCannotBeRemoved;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationDoesNotExist;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationNotAllowed;
import cz.metacentrum.perun.core.api.exceptions.GroupStructureSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationNotEnabledException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MembershipMismatchException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.GroupsManagerImplApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * GroupsManager entry logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class GroupsManagerEntry implements GroupsManager {

	private GroupsManagerBl groupsManagerBl;
	private PerunBl perunBl;

	public GroupsManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.groupsManagerBl = perunBl.getGroupsManagerBl();
	}

	public GroupsManagerEntry() {}

	//FIXME delete this method
	public GroupsManagerImplApi getGroupsManagerImpl() {
		throw new InternalErrorException("Unsupported method!");
	}

	@Override
	public Group createGroup(PerunSession sess, Vo vo, Group group) throws GroupExistsException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(group, "group");
		Utils.notNull(group.getName(), "group.name");

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		Utils.validateGroupName(group.getName());

		if (group.getParentGroupId() != null) throw new InternalErrorException("Top-level groups can't have parentGroupId set!");

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createGroup_Vo_Group_policy", vo)) {
			throw new PrivilegeException(sess, "createGroup");
		}

		Group createdGroup = getGroupsManagerBl().createGroup(sess, vo, group);

		//Refresh authz
		AuthzResolver.refreshAuthz(sess);
		return createdGroup;
	}

	@Override
	public Group createGroup(PerunSession sess, Group parentGroup, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException, GroupRelationNotAllowed, GroupRelationAlreadyExists, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, parentGroup);
		Utils.notNull(group, "group");
		Utils.notNull(group.getName(), "group.name");

		Utils.validateGroupName(group.getName());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createGroup_Group_Group_policy", parentGroup)) {
			throw new PrivilegeException(sess, "createGroup - subGroup");
		}

		if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, parentGroup)) {
			throw new ExternallyManagedException("Parent group " + parentGroup + " is externally managed");
		}

		Group createdGroup = getGroupsManagerBl().createGroup(sess, parentGroup, group);

		//Refresh authz
		AuthzResolver.refreshAuthz(sess);
		return createdGroup;
	}

	@Override
	public void deleteGroup(PerunSession sess, Group group, boolean forceDelete) throws GroupNotExistsException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteGroup_Group_boolean_policy", group)) {
			throw new PrivilegeException(sess, "deleteGroup");
				}

		if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group) || getGroupsManagerBl().hasGroupSynchronizedChild(sess, group)) {
			throw new ExternallyManagedException("Group " + group + " or some of the subGroups are externally managed");
		}
		getGroupsManagerBl().deleteGroup(sess, group, forceDelete);
	}

	@Override
	public void deleteGroup(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException {
		this.deleteGroup(sess, group, false);
	}

	@Override
	public void deleteAllGroups(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		for (Group group: groupsManagerBl.getAllGroups(sess, vo)) {
			if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
				// Members group is skipped in the deletion so no reason check it
				continue;
			}
			// Authorization
			if (!AuthzResolver.authorizedInternal(sess, "deleteAllGroups_Vo_policy", group)) {
				throw new PrivilegeException(sess, "deleteAllGroups");
			}
		}

		getGroupsManagerBl().deleteAllGroups(sess, vo);
	}

	@Override
	public void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws GroupNotExistsException, PrivilegeException, GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(groups, "groups");

		//Test if all groups exists and user has right to delete all of them
		for(Group group: groups) {
			getGroupsManagerBl().checkGroupExists(perunSession, group);

			if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(perunSession, group) || getGroupsManagerBl().hasGroupSynchronizedChild(perunSession, group)) {
				throw new ExternallyManagedException("Group " + group + " or some of the subGroups are externally managed!");
			}
		}

		//Authorization
		for (Group group: groups) {
			if(!AuthzResolver.authorizedInternal(perunSession, "deleteGroups_List<Group>_boolean_policy", group)) {
				throw new PrivilegeException(perunSession, "deleteGroups");
			}
		}

		getGroupsManagerBl().deleteGroups(perunSession, groups, forceDelete);
	}

	@Override
	public Group updateGroup(PerunSession sess, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		Utils.notNull(group, "group");
		Utils.notNull(group.getName(), "group.name");

		Utils.validateGroupName(group.getShortName());

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateGroup_Group_policy", group)) {
			throw new PrivilegeException(sess, "updateGroup");
		}

		return getGroupsManagerBl().updateGroup(sess, group);
	}

	@Override
	public void moveGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws GroupNotExistsException, PrivilegeException, GroupMoveNotAllowedException, WrongAttributeValueException, WrongReferenceAttributeValueException, ExternallyManagedException {
		Utils.checkPerunSession(sess);

		getGroupsManagerBl().checkGroupExists(sess, movingGroup);

		if (getGroupsManagerBl().isGroupSynchronizedFromExternallSource(sess, movingGroup)) {
			throw new ExternallyManagedException("Moving group: " + movingGroup + " is externally managed!");
		}
		//if destination group is null, moving group will be moved as top level group
		if(destinationGroup != null){
			getGroupsManagerBl().checkGroupExists(sess, destinationGroup);

			if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, destinationGroup)) {
				throw new ExternallyManagedException("Destination group: " + destinationGroup + " is externally managed!");
			}

			// Authorization (destination group is not null)
			if ((!AuthzResolver.authorizedInternal(sess, "moveGroup_Group_Group_policy", movingGroup)) ||
				(!AuthzResolver.authorizedInternal(sess, "moveGroup_Group_Group_policy", destinationGroup))) {
				throw new PrivilegeException(sess, "moveGroup");
			}
		} else {
			// Authorization (destination group is null)
			// We want to check MFA for parent groups as well, since we are changing its structure (moving one of its subgroups)
			Group parentGroup;
			try {
				parentGroup = groupsManagerBl.getParentGroup(sess, movingGroup);
				if (!AuthzResolver.authorizedInternal(sess, "destination_null-moveGroup_Group_Group_policy", movingGroup) ||
					!AuthzResolver.authorizedInternal(sess, "parentGroup-moveGroup_Group_Group_policy", parentGroup)) {
					throw new PrivilegeException(sess, "moveGroup");
				}
			} catch (ParentGroupNotExistsException ex) {
				// destination group is already top level
				if (!AuthzResolver.authorizedInternal(sess, "destination_null-moveGroup_Group_Group_policy", movingGroup)) {
					throw new PrivilegeException(sess, "moveGroup");
				}
			}
		}

		getGroupsManagerBl().moveGroup(sess, destinationGroup, movingGroup);
	}

	@Override
	public Group getGroupById(PerunSession sess, int id) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Group group = getGroupsManagerBl().getGroupById(sess, id);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupById_int_policy", group)) {
			throw new PrivilegeException(sess, "getGroupById");
		}

		return group;
	}

	@Override
	public Group getGroupByName(PerunSession sess, Vo vo, String name) throws GroupNotExistsException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		Utils.notNull(name, "name");

		Utils.validateFullGroupName(name);

		Group group = getGroupsManagerBl().getGroupByName(sess, vo, name);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupByName_Vo_String_policy", Arrays.asList(vo, group))) {
			throw new PrivilegeException(sess, "getGroupByName");
		}

		return group;
	}

	@Override
	public List<Group> getGroupsByIds(PerunSession sess, List<Integer> ids) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupsByIds_List<Integer>_policy")) {
			throw new PrivilegeException(sess, "getGroupsByIds");
		}
		List<Group> groups = getGroupsManagerBl().getGroupsByIds(sess, ids);
		groups.removeIf(group -> !AuthzResolver.authorizedInternal(sess, "filter-getGroupsByIds_List<Integer>_policy", group));

		return groups;
	}

	@Override
	public void addMembers(PerunSession sess, Group group, List<Member> members) throws MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
			// Check if the member and group are from the same VO
			if (member.getVoId() != (group.getVoId())) {
				throw new MembershipMismatchException("Member and group are form the different VO");
			}
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addMembers_Group_List<Member>_policy", group)) {
			throw new PrivilegeException(sess, "addMembers");
		}

		// Check if the group is externally synchronized
		Attribute attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GROUPSYNCHROENABLED_ATTRNAME);
		if ("true".equals(attrSynchronizeEnabled.getValue()) || getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group)) {
			throw new ExternallyManagedException("Adding of member is not allowed. Group is externally managed.");
		}

		getGroupsManagerBl().addMembers(sess, group, members);
	}

	@Override
	public void copyMembers(PerunSession sess, Group sourceGroup, List<Group> destinationGroups, List<Member> members) throws WrongReferenceAttributeValueException, WrongAttributeValueException, GroupNotExistsException, MemberNotExistsException, GroupGroupMismatchException, PrivilegeException, ExternallyManagedException, MemberGroupMismatchException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, sourceGroup);
		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "source-copyMembers_Group_List<Group>_List<Member>_boolean_policy", sourceGroup)) {
			throw new PrivilegeException(sess, "copyMembers");
		}

		for (Group destinationGroup : destinationGroups) {
			getGroupsManagerBl().checkGroupExists(sess, destinationGroup);

			if (sourceGroup.getId() == destinationGroup.getId()) {
				throw new GroupGroupMismatchException("Cannot copy from group to itself", sourceGroup, sourceGroup);
			}

			if (sourceGroup.getVoId() != destinationGroup.getVoId()) {
				throw new GroupGroupMismatchException("Groups are not from the same VO", sourceGroup, destinationGroup);
			}

			// Check if destinationGroup is externally synchronized
			if (getGroupsManagerBl().isGroupSynchronizedFromExternallSource(sess, destinationGroup)) {
				throw new ExternallyManagedException("Adding of member is not allowed. Group is externally managed.");
			}

			// Authorization
			if (!AuthzResolver.authorizedInternal(sess, "dest-copyMembers_Group_List<Group>_List<Member>_boolean_policy", destinationGroup)) {
				throw new PrivilegeException(sess, "copyMembers");
			}
		}

		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
			// Check if the member and group are from the same VO
			if (member.getVoId() != (sourceGroup.getVoId())) {
				throw new MembershipMismatchException("Member and group are from different VOs");
			}
		}


		getGroupsManagerBl().copyMembers(sess, sourceGroup, destinationGroups, members);
	}


	@Override
	public void addMember(PerunSession sess, List<Group> groups, Member member) throws MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		for (Group group : groups) {
			getGroupsManagerBl().checkGroupExists(sess, group);

			// Check if the member and group are from the same VO
			if (member.getVoId() != (group.getVoId())) {
				throw new MembershipMismatchException("Member and group are form the different VO");
			}
			// Check if the group is externally synchronized
			Attribute attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GROUPSYNCHROENABLED_ATTRNAME);
			if ("true".equals(attrSynchronizeEnabled.getValue()) || getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group)) {
				throw new ExternallyManagedException("Adding of member is not allowed. Group is externally managed.");
			}
		}

		List<Group> groupsMemberIsNotDirect = new ArrayList<>();
		for (Group group: groups) {
			// Authorization
			if (!AuthzResolver.authorizedInternal(sess, "addMember_List<Group>_Member_policy", group)) {
				throw new PrivilegeException(sess, "addMember");
			}

			// Filter groups where member is direct member
			if (!isDirectGroupMember(sess, group, member)) {
				groupsMemberIsNotDirect.add(group);
			}
		}

		getGroupsManagerBl().addMember(sess, groupsMemberIsNotDirect, member);
	}

	@Override
	public boolean isDirectGroupMember(PerunSession sess, Group group, Member member) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "isDirectGroupMember_Group_Member_policy", Arrays.asList(group, member))) {
			throw new PrivilegeException(sess, "isGroupMember");
		}

		return getGroupsManagerBl().isDirectGroupMember(sess, group, member);
	}




	@Override
	public void addMember(PerunSession sess, Group group, Member member) throws MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Check if the member and group are from the same VO
		if (member.getVoId() != (group.getVoId())) {
			throw new MembershipMismatchException("Member and group are form the different VO");
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "addMember_Group_Member_policy", group)) {
			throw new PrivilegeException(sess, "addMember");
		}

		// Check if the group is externally synchronized
		Attribute attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GROUPSYNCHROENABLED_ATTRNAME);
		if ("true".equals(attrSynchronizeEnabled.getValue()) || getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group)) {
			throw new ExternallyManagedException("Adding of member is not allowed. Group is externally managed.");
		}

		getGroupsManagerBl().addMember(sess, group, member);
	}

	@Override
	public void removeMember(PerunSession sess, Group group, Member member) throws MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "removeMember_Group_Member_policy", Arrays.asList(group, member))) {
			throw new PrivilegeException(sess, "removeMember");
		}

		// Check if the group is externally synchronized
		Attribute attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GROUPSYNCHROENABLED_ATTRNAME);
		if ("true".equals(attrSynchronizeEnabled.getValue()) || getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group)) {
			throw new ExternallyManagedException("Removing of member is not allowed. Group is externally managed.");
		}

		getGroupsManagerBl().removeMember(sess, group, member);
	}

	@Override
	public void removeMembers(PerunSession sess, Group group, List<Member> members) throws MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		for (Member member : members) {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		}

		// Authorization
		for (Member member: members) {
			if (!AuthzResolver.authorizedInternal(sess, "removeMembers_Group_List<Member>_policy", member, group)) {
				throw new PrivilegeException(sess, "removeMembers");
			}
		}

		// Check if the group is externally synchronized
		Attribute attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GROUPSYNCHROENABLED_ATTRNAME);
		if ("true".equals(attrSynchronizeEnabled.getValue()) || getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group)) {
			throw new ExternallyManagedException("Removing of member is not allowed. Group is externally managed.");
		}
		getGroupsManagerBl().removeMembers(sess, group, members);
	}

	@Override
	public void removeMember(PerunSession sess, Member member, List<Group> groups) throws MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		for (Group group : groups) {
			getGroupsManagerBl().checkGroupExists(sess, group);

			// Check if the group is externally synchronized
			Attribute attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GROUPSYNCHROENABLED_ATTRNAME);
			if ("true".equals(attrSynchronizeEnabled.getValue()) || getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group)) {
				throw new ExternallyManagedException("Removing of member is not allowed. Group is externally managed.");
			}
		}
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		for (Group group: groups) {
			if (!AuthzResolver.authorizedInternal(sess, "removeMember_Member_List<Group>_policy", member, group)) {
				throw new PrivilegeException(sess, "removeMember");
			}
		}

		getGroupsManagerBl().removeMember(sess, groups, member);
	}

	@Override
	public List<Member> getGroupMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupMembers");
		}

		return getGroupsManagerBl().getGroupMembers(sess, group);
	}

	@Override
	public List<Member> getGroupDirectMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupDirectMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupDirectMembers");
		}

		return getGroupsManagerBl().getGroupDirectMembers(sess, group);
	}

	@Override
	public List<Member> getActiveGroupMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getActiveGroupMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getActiveGroupMembers");
		}

		return getGroupsManagerBl().getActiveGroupMembers(sess, group);
	}

	@Override
	public List<Member> getInactiveGroupMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getInactiveGroupMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getInactiveGroupMembers");
		}

		return getGroupsManagerBl().getInactiveGroupMembers(sess, group);
	}

	@Override
	public List<Member> getGroupMembers(PerunSession sess, Group group, Status status) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupMembers_Group_Status_policy", group)) {
			throw new PrivilegeException(sess, "getGroupMembers");
				}

		return getGroupsManagerBl().getGroupMembers(sess, group, status);
	}

	@Override
	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupRichMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembers(sess, group), group, true);
	}

	@Override
	public List<RichMember> getGroupDirectRichMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupDirectRichMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupDirectRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupDirectRichMembers(sess, group), group, true);
	}

	@Override
	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupRichMembers_Group_Status_policy", group)) {
			throw new PrivilegeException(sess, "getGroupRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembers(sess, group, status), group, true);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupRichMembersWithAttributes_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupRichMembersWithAttributes");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembersWithAttributes(sess, group), group, true);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupRichMembersWithAttributes_Group_Status_policy", group)) {
			throw new PrivilegeException(sess, "getGroupRichMembersWithAttributes");
				}
		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembersWithAttributes(sess, group, status), group, true);
	}

	@Override
	public boolean isGroupMember(PerunSession sess, Group group, Member member) throws PrivilegeException, GroupNotExistsException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "isGroupMember_Group_Member_policy", Arrays.asList(group, member))) {
			throw new PrivilegeException(sess, "isGroupMember");
		}

		return getGroupsManagerBl().isGroupMember(sess, group, member);
	}

	@Override
	public int getGroupMembersCount(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupMembersCount_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupMembersCount");
				}

		return getGroupsManagerBl().getGroupMembersCount(sess, group);
	}

	@Override
	public int getGroupDirectMembersCount(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupDirectMembersCount_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupDirectMembersCount");
		}

		return getGroupsManagerBl().getGroupDirectMembersCount(sess, group);
	}

	@Override
	public Map<Status, Integer> getGroupMembersCountsByVoStatus(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupMembersCountsByVoStatus_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupMembersCountsByVoStatus");
		}

		return getGroupsManagerBl().getGroupMembersCountsByVoStatus(sess, group);
	}

	@Override
	public Map<MemberGroupStatus, Integer> getGroupMembersCountsByGroupStatus(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupMembersCountsByGroupStatus_Group_policy", group)) {
			throw new PrivilegeException(sess, "getGroupMembersCountsByGroupStatus");
		}

		return getGroupsManagerBl().getGroupMembersCountsByGroupStatus(sess, group);
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, User user) throws AlreadyAdminException, PrivilegeException, GroupNotExistsException, UserNotExistsException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		AuthzResolver.setRole(sess, user, group, Role.GROUPADMIN);
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws AlreadyAdminException, PrivilegeException, GroupNotExistsException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		AuthzResolver.setRole(sess, authorizedGroup, group, Role.GROUPADMIN);
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, User user) throws PrivilegeException, GroupNotExistsException, UserNotAdminException, UserNotExistsException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		AuthzResolver.unsetRole(sess, user, group, Role.GROUPADMIN);
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws PrivilegeException, GroupNotExistsException, GroupNotAdminException, RoleCannotBeManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		AuthzResolver.unsetRole(sess, authorizedGroup, group, Role.GROUPADMIN);
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Group group, boolean onlyDirectAdmins) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getAdmins_Group_boolean_policy", group)) {
			throw new PrivilegeException(perunSession, "getAdmins");
		}

		return getGroupsManagerBl().getAdmins(perunSession, group, onlyDirectAdmins);
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws PrivilegeException, GroupNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);
		//list of specific attributes must be not null if filtering is needed
		if(!allUserAttributes) {
			Utils.notNull(specificAttributes, "specificAttributes");
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getRichAdmins_Group_List<String>_boolean_boolean_policy", group)) {
			throw new PrivilegeException(perunSession, "getRichAdmins");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdmins(perunSession, group, specificAttributes, allUserAttributes, onlyDirectAdmins));
	}

	@Override
	@Deprecated
	public List<User> getAdmins(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getAdmins");
				}

		return getGroupsManagerBl().getAdmins(sess, group);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getDirectAdmins");
				}

		return getGroupsManagerBl().getDirectAdmins(sess, group);
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAdminGroups_Group_policy", group)) {
			throw new PrivilegeException(sess, "getAdminGroups");
				}

		return getGroupsManagerBl().getAdminGroups(sess, group);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getRichAdmins");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdmins(perunSession, group));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws PrivilegeException, GroupNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getRichAdminsWithAttributes");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdminsWithAttributes(perunSession, group));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getRichAdminsWithSpecificAttributes");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdminsWithSpecificAttributes(perunSession, group, specificAttributes));
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getDirectRichAdminsWithSpecificAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getDirectRichAdminsWithSpecificAttributes(perunSession, group, specificAttributes));
	}

	@Override
	public List<Group> getAllGroups(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		if (!AuthzResolver.authorizedInternal(sess, "getAllGroups_policy")) {
			throw new PrivilegeException(sess, "getAllGroups");
		}

		return groupsManagerBl.getAllGroups(sess).stream()
			.filter(group -> AuthzResolver.authorizedInternal(sess, "filter-getAllGroups_policy", group))
			.collect(toList());
	}

	@Override
	public List<RichGroup> getAllRichGroups(PerunSession sess) throws PrivilegeException {
		return getAllRichGroups(sess, null);
	}

	@Override
	public List<RichGroup> getAllRichGroups(PerunSession sess, List<String> attrNames) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		if (!AuthzResolver.authorizedInternal(sess, "getAllRichGroups_policy")) {
			throw new PrivilegeException("getAllRichGroups");
		}

		return groupsManagerBl
			.convertGroupsToRichGroupsWithAttributes(sess, groupsManagerBl.getAllGroups(sess), attrNames);
	}

	@Override
	public Paginated<RichGroup> getGroupsPage(PerunSession sess, Vo vo, GroupsPageQuery query, List<String> attrNames) throws VoNotExistsException, PrivilegeException, MemberNotExistsException, GroupNotExistsException, MemberGroupMismatchException {
		Utils.checkPerunSession(sess);
		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		Member member = query.getMemberId() == null ? null : perunBl.getMembersManagerBl().getMemberById(sess, query.getMemberId());
		if (member != null) {
			perunBl.getMembersManagerBl().checkMemberExists(sess, member);
		}

		for (String role : query.getRoles()) {
			if (!AuthzResolver.roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		// Authorization
		if (member != null) {
			if (!AuthzResolver.authorizedInternal(sess, "member-getGroupsPage_Vo_GroupsPageQuery_List<String>_policy",  member)) {
				throw new PrivilegeException(sess, "getGroupsPage");
			}
		} else if (!AuthzResolver.authorizedInternal(sess, "getGroupsPage_Vo_GroupsPageQuery_List<String>_policy", vo)) {
			throw new PrivilegeException(sess, "getGroupsPage");
		}

		Paginated<RichGroup> result = groupsManagerBl.getGroupsPage(sess, vo, query, attrNames);

		result.setData(getGroupsManagerBl().filterOnlyAllowedAttributes(sess, result.getData(), member, null, true));

		return result;
	}

	@Override
	public Paginated<RichGroup> getSubgroupsPage(PerunSession sess, Group group, GroupsPageQuery query, List<String> attrNames) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		if (!AuthzResolver.authorizedInternal(sess, "getSubgroupsPage_Group_GroupsPageQuery_List<String>_policy", group)) {
			throw new PrivilegeException(sess, "getSubgroupsPage");
		}

		Paginated<RichGroup> result = groupsManagerBl.getSubgroupsPage(sess, group, query, attrNames);
		result.setData(getGroupsManagerBl().filterOnlyAllowedAttributes(sess, result.getData(), null, true));

		return result;
	}

	@Override
	public List<Group> getAllGroups(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllGroups_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getAllGroups");
				}

		List<Group> groups = getGroupsManagerBl().getAllGroups(sess, vo);

		groups.removeIf(group -> !AuthzResolver.authorizedInternal(sess, "filter-getAllGroups_Vo_policy", group));
		return groups;
	}

	@Override
	public Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllGroupsWithHierarchy_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getAllGroupsWithHierarchy");
		}

		Map<Group, Object> groups =  getGroupsManagerBl().getAllGroupsWithHierarchy(sess, vo);

		groups.keySet().removeIf(group -> !AuthzResolver.authorizedInternal(sess, "filter-getAllGroupsWithHierarchy_Vo_policy", group));
		return groups;
	}

	@Override
	public List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getSubGroups_Group_policy", parentGroup)) {
			throw new PrivilegeException(sess, "getSubGroups");
				}

		return getGroupsManagerBl().getSubGroups(sess, parentGroup);
	}

	@Override
	public List<Group> getAllSubGroups(PerunSession sess, Group parentGroup) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllSubGroups_Group_policy", parentGroup)) {
			throw new PrivilegeException(sess, "getAllSubGroups");
				}

		return getGroupsManagerBl().getAllSubGroups(sess, parentGroup);
	}

	@Override
	public Group getParentGroup(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getParentGroup_Group_policy", group)) {
			throw new PrivilegeException(sess, "getParentGroup");
				}

		return getGroupsManagerBl().getParentGroup(sess, group);
	}

	@Override
	public List<Group> getGroups(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroups_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getGroups");
				}

		List<Group> groups =  getGroupsManagerBl().getGroups(sess, vo);

		groups.removeIf(group -> !AuthzResolver.authorizedInternal(sess, "filter-getGroups_Vo_policy", group));
		return groups;
	}

	@Override
	public int getGroupsCount(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupsCount_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getGroupsCount");
		}

		return getGroupsManagerBl().getGroupsCount(sess, vo);
	}

	@Override
	public int getGroupsCount(PerunSession sess) {
		Utils.checkPerunSession(sess);

		return getGroupsManagerBl().getGroupsCount(sess);
	}

	@Override
	public int getSubGroupsCount(PerunSession sess, Group parentGroup) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getSubGroupsCount_Group_policy", parentGroup)) {
			throw new PrivilegeException(sess, "getSubGroupsCount");
				}

		return getGroupsManagerBl().getSubGroupsCount(sess, parentGroup);
	}

	@Override
	public Vo getVo(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		Vo vo =  getGroupsManagerBl().getVo(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getVo_Group_policy", group)) {
			throw new PrivilegeException(sess, "getVo");
		}

		return vo;
	}

	@Override
	public List<Member> getParentGroupMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getParentGroupMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getParentGroupMembers");
		}

		return getGroupsManagerBl().getParentGroupMembers(sess, group);
	}

	@Override
	public List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getParentGroupRichMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getParentGroupRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getParentGroupRichMembers(sess, group), group, true);
	}

	@Override
	public List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getParentGroupMembersWithAttributes_Group_policy", group)) {
			throw new PrivilegeException(sess, "getParentGroupRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getParentGroupRichMembersWithAttributes(sess, group), group, true);
	}


	/**
	 * Gets the groupsManagerBl for this instance.
	 *
	 * @return The groupsManagerBl.
	 */
	public GroupsManagerBl getGroupsManagerBl() {
		return this.groupsManagerBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}

	/**
	 * Sets the groupsManagerBl for this instance.
	 *
	 * @param groupsManagerBl The groupsManagerBl.
	 */
	public void setGroupsManagerBl(GroupsManagerBl groupsManagerBl)
	{
		this.groupsManagerBl = groupsManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	@Override
	public void forceGroupSynchronization(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException, GroupSynchronizationAlreadyRunningException, GroupSynchronizationNotEnabledException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "forceGroupSynchronization_Group_policy", group))  {
			throw new PrivilegeException(sess, "synchronizeGroup");
		}

		getGroupsManagerBl().forceGroupSynchronization(sess, group);
	}

	@Override
	public void forceAllSubGroupsSynchronization(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "forceAllSubGroupsSynchronization_Group_policy", group))  {
			throw new PrivilegeException(sess, "forceAllSubGroupsSynchronization");
		}

		getGroupsManagerBl().forceAllSubGroupsSynchronization(sess, group);
	}

	@Override
	public void forceGroupStructureSynchronization(PerunSession sess, Group group) throws GroupNotExistsException, PrivilegeException, GroupStructureSynchronizationAlreadyRunningException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "forceGroupStructureSynchronization_Group_policy", group))  {
			throw new PrivilegeException(sess, "forceGroupStructureSynchronization");
		}

		getGroupsManagerBl().forceGroupStructureSynchronization(sess, group);
	}

	@Override
	public void synchronizeGroups(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "synchronizeGroups_policy"))  {
			throw new PrivilegeException(sess, "synchronizeGroups");
		}

		getGroupsManagerBl().synchronizeGroups(sess);
	}

	@Override
	public void synchronizeGroupsStructures(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "synchronizeGroupsStructures_policy"))  {
			throw new PrivilegeException(sess, "synchronizeGroupsStructures");
		}

		getGroupsManagerBl().synchronizeGroupsStructures(sess);
	}

	@Override
	public List<Group> getMemberGroups(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMemberGroups_Member_policy", member)) {
			throw new PrivilegeException(sess, "getMemberGroups for " + member);
		}

		List<Group> groups = getGroupsManagerBl().getMemberGroups(sess, member);
		groups.removeIf(group -> !AuthzResolver.authorizedInternal(sess, "filter-getMemberGroups_Member_policy", member, group));
		return groups;
	}


	@Override
	public List<Group> getMemberGroupsByAttribute(PerunSession sess, Member member, Attribute attribute) throws WrongAttributeAssignmentException, PrivilegeException, MemberNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, new AttributeDefinition(attribute));

		//Only group attributes are allowed
		if(!this.getPerunBl().getAttributesManagerBl().isFromNamespace(sess, attribute, AttributesManagerEntry.NS_GROUP_ATTR)) {
			throw new WrongAttributeAssignmentException(attribute);
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMemberGroupsByAttribute_Member_Attribute_policy", member)) {
			throw new PrivilegeException(sess, "getMemberGroupsByAttribute for " + member);
		}

		List<Group> groups = this.groupsManagerBl.getMemberGroupsByAttribute(sess, member, attribute);

		//If actor has no right to read attribute for group, throw exception
		for(Group group: groups) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, AttributeAction.READ, attribute, group, true)) {
					throw new PrivilegeException(sess, "Actor hasn't right to read attribute for a group.");
				}
		}

		return groups;
	}

	@Override
	public List<Group> getAllMemberGroups(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllMemberGroups_Member_policy", member)) {
			throw new PrivilegeException(sess, "getAllMemberGroups for " + member);
		}

		return getGroupsManagerBl().getAllMemberGroups(sess, member);
	}

	@Override
	public List<Group> getGroupsWhereMemberIsActive(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupsWhereMemberIsActive_Member_policy", member)) {
			throw new PrivilegeException(sess, "getGroupsWhereMemberIsActive");
		}

		return getGroupsManagerBl().getGroupsWhereMemberIsActive(sess, member);
	}

	@Override
	public List<Group> getGroupsWhereMemberIsInactive(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getGroupsWhereMemberIsInactive_Member_policy", member)) {
			throw new PrivilegeException(sess, "getGroupsWhereMemberIsInactive");
		}

		return getGroupsManagerBl().getGroupsWhereMemberIsInactive(sess, member);
	}

	@Override
	public List<Group> getAllGroupsWhereMemberIsActive(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllGroupsWhereMemberIsActive_Member_policy", member)) {
			throw new PrivilegeException(sess, "getAllGroupsWhereMemberIsActive");
		}

		return getGroupsManagerBl().getAllGroupsWhereMemberIsActive(sess, member);
	}

	@Override
	public List<RichGroup> getRichGroupsAssignedToResourceWithAttributesByNames(PerunSession sess, Resource resource, List<String> attrNames) throws ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichGroupsAssignedToResourceWithAttributesByNames_Resource_List<String>_policy", resource)) {
			throw new PrivilegeException(sess, "getRichGroupsAssignedToResourceWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getRichGroupsWithAttributesAssignedToResource(sess, resource, attrNames);

		richGroups.removeIf(richGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getRichGroupsAssignedToResourceWithAttributesByNames_Resource_List<String>_policy", richGroup, resource));

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, resource, true);
	}

	@Override
	public List<RichGroup> getRichGroupsAssignedToResourceWithAttributesByNames(PerunSession sess, Member member, Resource resource, List<String> attrNames) throws ResourceNotExistsException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);
		this.getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichGroupsAssignedToResourceWithAttributesByNames_Member_Resource_List<String>_policy", Arrays.asList(member, resource))) {
			throw new PrivilegeException(sess, "getRichGroupsAssignedToResourceWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getRichGroupsWithAttributesAssignedToResource(sess, member, resource, attrNames);

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, member, resource, true);
	}

	@Deprecated
	public List<RichGroup> getMemberRichGroupsWithAttributesByNames(PerunSession sess, Member member, List<String> attrNames) throws MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMemberRichGroupsWithAttributesByNames_Member_List<String>_policy", member)) {
			throw new PrivilegeException(sess, "getMemberRichGroupsWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getMemberRichGroupsWithAttributesByNames(sess, member, attrNames);

		richGroups.removeIf(richGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getMemberRichGroupsWithAttributesByNames_Member_List<String>_policy", Arrays.asList(member, richGroup)));

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, member, null, true);
	}

	@Override
	public List<RichGroup> getMemberRichGroupsWithAttributesByNames(PerunSession sess, Member member, List<String> attrNames, List<String> roles, List<RoleAssignmentType> types) throws MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		for (String role : roles) {
			if (!AuthzResolver.roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMemberRichGroupsWithAttributesByNames_Member_List<String>_List<String>_List<RoleAssignmentType>_policy", member)) {
			throw new PrivilegeException(sess, "getMemberRichGroupsWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getMemberRichGroupsWithAttributesByNames(sess, member, attrNames, roles, types);
		richGroups.removeIf(richGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getMemberRichGroupsWithAttributesByNames_Member_List<String>_List<String>_List<RoleAssignmentType>_policy", Arrays.asList(member, richGroup)));

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, member, null, true);
	}

	@Override
	@Deprecated
	public List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames) throws VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllRichGroupsWithAttributesByNames_Vo_List<String>_policy", vo)) {
			throw new PrivilegeException(sess, "getAllRichGroupsWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getAllRichGroupsWithAttributesByNames(sess, vo, attrNames);

		richGroups.removeIf(richGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getAllRichGroupsWithAttributesByNames_Vo_List<String>_policy", richGroup));

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	public List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames, List<String> roles, List<RoleAssignmentType> types) throws VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		for (String role : roles) {
			if (!AuthzResolver.roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllRichGroupsWithAttributesByNames_Vo_List<String>_List<String>_List<RoleAssignmentType>_policy", vo)) {
			throw new PrivilegeException(sess, "getAllRichGroupsWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getAllRichGroupsWithAttributesByNames(sess, vo, attrNames, roles, types);
		richGroups.removeIf(richGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getAllRichGroupsWithAttributesByNames_Vo_List<String>_List<String>_List<RoleAssignmentType>_policy", richGroup));

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	public List<RichGroup> getRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichSubGroupsWithAttributesByNames_Group_List<String>_policy", parentGroup)) {
			throw new PrivilegeException(sess, "getRichSubGroupsWithAttributesByNames");
		}
		List<RichGroup> richGroups = getGroupsManagerBl().getRichSubGroupsWithAttributesByNames(sess, parentGroup, attrNames);

		richGroups.removeIf(richGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getRichSubGroupsWithAttributesByNames_Group_List<String>_policy", richGroup));

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	@Deprecated
	public List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllRichSubGroupsWithAttributesByNames_Group_List<String>_policy", parentGroup)) {
			throw new PrivilegeException(sess, "getAllRichSubGroupsWithAttributesByNames");
		}
		List<RichGroup> richGroups = getGroupsManagerBl().getAllRichSubGroupsWithAttributesByNames(sess, parentGroup, attrNames);

		richGroups.removeIf(richGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getAllRichSubGroupsWithAttributesByNames_Group_List<String>_policy", richGroup));

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	public List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames, List<String> roles, List<RoleAssignmentType> types) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		for (String role : roles) {
			if (!AuthzResolver.roleExists(role)) {
				throw new InternalErrorException("Role: "+ role +" does not exists.");
			}
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllRichSubGroupsWithAttributesByNames_Group_List<String>_List<String>_List<RoleAssignmentType>_policy", parentGroup)) {
			throw new PrivilegeException(sess, "getAllRichSubGroupsWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getAllRichSubGroupsWithAttributesByNames(sess, parentGroup, attrNames, roles,  types);
		richGroups.removeIf(richGroup -> !AuthzResolver.authorizedInternal(sess, "filter-getAllRichSubGroupsWithAttributesByNames_Group_List<String>_List<String>_List<RoleAssignmentType>_policy", richGroup));

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	public RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Group group = groupsManagerBl.getGroupById(sess, groupId);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichGroupByIdWithAttributesByNames_int_List<String>_policy", group)) {
			throw new PrivilegeException(sess, "getRichGroupByIdWithAttributesByNames");
		}

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getRichGroupByIdWithAttributesByNames(sess, groupId, attrNames));

	}

	@Override
	public Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupNotExistsException, PrivilegeException, GroupRelationNotAllowed, GroupRelationAlreadyExists, WrongAttributeValueException, WrongReferenceAttributeValueException, ExternallyManagedException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, resultGroup);
		getGroupsManagerBl().checkGroupExists(sess, operandGroup);

		if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, resultGroup)) {
			throw new ExternallyManagedException("Result group: " + resultGroup + " is externally managed!");
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "result-createGroupUnion_Group_Group_policy", resultGroup) ||
			!AuthzResolver.authorizedInternal(sess, "operand-createGroupUnion_Group_Group_policy", operandGroup)) {
			throw new PrivilegeException(sess, "createGroupUnion");
		}

		return getGroupsManagerBl().createGroupUnion(sess, resultGroup, operandGroup, false);
	}

	@Override
	public void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupNotExistsException, PrivilegeException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, resultGroup);
		getGroupsManagerBl().checkGroupExists(sess, operandGroup);

		if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, resultGroup)) {
			throw new ExternallyManagedException("Result group: " + resultGroup + " is externally managed!");
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "result-removeGroupUnion_Group_Group_policy", resultGroup) ||
			!AuthzResolver.authorizedInternal(sess, "operand-removeGroupUnion_Group_Group_policy", operandGroup)) {
			throw new PrivilegeException(sess, "removeGroupUnion");
		}

		getGroupsManagerBl().removeGroupUnion(sess, resultGroup, operandGroup, false);
	}

	@Override
	public List<Group> getGroupUnions(PerunSession sess, Group group, boolean reverseDirection) throws GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if ( !AuthzResolver.authorizedInternal(sess, "getGroupUnions_Group_boolean_policy", group)) {
			throw new PrivilegeException(sess, "getGroupUnions");
		}

		return groupsManagerBl.getGroupUnions(sess, group, reverseDirection);
	}

	@Override
	public Member setMemberGroupStatus(PerunSession sess, Member member, Group group, MemberGroupStatus status) throws GroupNotExistsException, MemberNotExistsException, PrivilegeException, NotGroupMemberException {

		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if ( !AuthzResolver.authorizedInternal(sess, "setMemberGroupStatus_Member_Group_MemberGroupStatus_policy", Arrays.asList(member, group))) {
			throw new PrivilegeException(sess, "setMemberGroupStatus");
		}

		// will fail if not group member
		Member groupMember = perunBl.getGroupsManagerBl().getGroupMemberById(sess, group, member.getId());
		if (MembershipType.INDIRECT.equals(groupMember.getMembershipType())) {
			throw new InternalErrorException("Setting group membership status for indirect members is not allowed.");
		}

		if (MemberGroupStatus.VALID.equals(status)) {
			getGroupsManagerBl().validateMemberInGroup(sess, groupMember, group);
		} else {
			getGroupsManagerBl().expireMemberInGroup(sess, groupMember, group);
		}
		// refresh after change
		groupMember = perunBl.getGroupsManagerBl().getGroupMemberById(sess, group, member.getId());
		return groupMember;

	}

	@Override
	public Member getGroupMemberById(PerunSession sess, Group group, int memberId) throws NotGroupMemberException, GroupNotExistsException, PrivilegeException {

		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if ( !AuthzResolver.authorizedInternal(sess, "getGroupMemberById_Group_int_policy", group)) {
			throw new PrivilegeException(sess, "getGroupMemberById");
		}

		return perunBl.getGroupsManagerBl().getGroupMemberById(sess, group, memberId);

	}

	@Override
	public void extendMembershipInGroup(PerunSession sess, Member member, Group group) throws ExtendMembershipException, PrivilegeException, MemberNotExistsException, GroupNotExistsException {

		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "extendMembershipInGroup_Member_Group_policy", Arrays.asList(member, group))) {
			throw new PrivilegeException(sess, "extendMembershipInGroup");
		}

		getGroupsManagerBl().extendMembershipInGroup(sess, member, group);

	}

	@Override
	public boolean canExtendMembershipInGroup(PerunSession sess, Member member, Group group) throws MemberNotExistsException, GroupNotExistsException, PrivilegeException {

		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "canExtendMembershipInGroup_Member_Group_policy", Arrays.asList(member, group))) {
			throw new PrivilegeException(sess, "canExtendMembershipInGroup");
		}

		return getGroupsManagerBl().canExtendMembershipInGroup(sess, member, group);
	}

	@Override
	public boolean canExtendMembershipInGroupWithReason(PerunSession sess, Member member, Group group) throws MemberNotExistsException, GroupNotExistsException, PrivilegeException, ExtendMembershipException {

		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "canExtendMembershipInGroupWithReason_Member_Group_policy", Arrays.asList(member, group))) {
			throw new PrivilegeException(sess, "canExtendMembershipInGroupWithReason");
		}

		return getGroupsManagerBl().canExtendMembershipInGroupWithReason(sess, member, group);
	}

	@Override
	public List<List<Group>> getIndirectMembershipPaths(PerunSession sess, Member member, Group group) throws MemberNotExistsException, GroupNotExistsException, PrivilegeException {

		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getIndirectMembershipPaths_Member_Group_policy", member, group)) {
			throw new PrivilegeException(sess, "getIndirectMembershipPaths");
		}

		return getGroupsManagerBl().getIndirectMembershipPaths(sess, member, group);
	}

	@Override
	public List<RichMember> getGroupRichMembersByIds(PerunSession sess, int groupId, List<Integer> memberIds, List<String> attrNames) throws GroupNotExistsException, PrivilegeException, AttributeNotExistsException  {
		Utils.checkPerunSession(sess);

		Group group = groupsManagerBl.getGroupById(sess, groupId);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if ( !AuthzResolver.authorizedInternal(sess, "getGroupRichMembersByIds_int_List<Integer>_List<String>_policy", group)) {
			throw new PrivilegeException(sess, "getGroupRichMembersByIds");
		}

		List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		for (String attrName : attrNames) {
			attributeDefinitions.add(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrName));
		}

		List<Member> members = new ArrayList<>();
		for (Integer memberId : memberIds) {
			try {
				members.add(groupsManagerBl.getGroupMemberById(sess, group, memberId));
			} catch (NotGroupMemberException e) {
				// silently skip
			}
		}
		members.removeIf(member -> !AuthzResolver.authorizedInternal(sess, "filter-getGroupRichMembersByIds_int_List<Integer>_List<String>_policy", member));

		List<RichMember> richMembers = perunBl.getMembersManagerBl().getRichMembersWithAttributes(sess, group, members, attributeDefinitions);
		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, richMembers, group, true);
	}

	@Override
	public void allowGroupToHierarchicalVo(PerunSession sess, Group group, Vo vo) throws VoNotExistsException, GroupNotExistsException, PrivilegeException, RelationNotExistsException, RelationExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "group-allowGroupToHierarchicalVo_Group_Vo_policy", group) ||
			!AuthzResolver.authorizedInternal(sess, "vo-allowGroupToHierarchicalVo_Group_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "allowGroupToHierarchicalVo");
		}

		getGroupsManagerBl().allowGroupToHierarchicalVo(sess, group, vo);
	}

	@Override
	public void allowGroupsToHierarchicalVo(PerunSession sess, List<Group> groups, Vo vo) throws VoNotExistsException, GroupNotExistsException, PrivilegeException, RelationNotExistsException, RelationExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		for (Group group : groups) {
			getGroupsManagerBl().checkGroupExists(sess, group);

			// Authorization
			if (!AuthzResolver.authorizedInternal(sess, "group-allowGroupToHierarchicalVo_Group_Vo_policy", group) ||
				!AuthzResolver.authorizedInternal(sess, "vo-allowGroupToHierarchicalVo_Group_Vo_policy", vo)) {
				throw new PrivilegeException(sess, "allowGroupsToHierarchicalVo");
			}

			getGroupsManagerBl().allowGroupToHierarchicalVo(sess, group, vo);
		}
	}

	@Override
	public void disallowGroupToHierarchicalVo(PerunSession sess, Group group, Vo vo) throws VoNotExistsException, GroupNotExistsException, PrivilegeException, RelationNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "group-disallowGroupToHierarchicalVo_Group_Vo_policy", group) ||
			!AuthzResolver.authorizedInternal(sess, "vo-disallowGroupToHierarchicalVo_Group_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "disallowGroupToHierarchicalVo");
		}

		getGroupsManagerBl().disallowGroupToHierarchicalVo(sess, group, vo);
	}

	@Override
	public void disallowGroupsToHierarchicalVo(PerunSession sess, List<Group> groups, Vo vo) throws VoNotExistsException, GroupNotExistsException, PrivilegeException, RelationNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		for (Group group : groups) {
			getGroupsManagerBl().checkGroupExists(sess, group);

			// Authorization
			if (!AuthzResolver.authorizedInternal(sess, "group-disallowGroupToHierarchicalVo_Group_Vo_policy", group) ||
				!AuthzResolver.authorizedInternal(sess, "vo-disallowGroupToHierarchicalVo_Group_Vo_policy", vo)) {
				throw new PrivilegeException(sess, "disallowGroupsToHierarchicalVo");
			}

			getGroupsManagerBl().disallowGroupToHierarchicalVo(sess, group, vo);
		}
	}

	@Override
	public boolean isAllowedGroupToHierarchicalVo(PerunSession sess, Group group, Vo vo) throws VoNotExistsException, GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "isAllowedGroupToHierarchicalVo_Group_Vo_policy", group, vo)) {
			throw new PrivilegeException(sess, "isAllowedGroupToHierarchicalVo");
		}

		return getGroupsManagerBl().isAllowedGroupToHierarchicalVo(sess, group, vo);
	}

	@Override
	public List<Group> getAllAllowedGroupsToHierarchicalVo(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllAllowedGroupsToHierarchicalVo_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getAllAllowedGroupsToHierarchicalVo");
		}

		return getGroupsManagerBl().getAllAllowedGroupsToHierarchicalVo(sess, vo);
	}

	@Override
	public List<Group> getAllAllowedGroupsToHierarchicalVo(PerunSession sess, Vo vo, Vo memberVo) throws VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getVosManagerBl().checkVoExists(sess, memberVo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllAllowedGroupsToHierarchicalVo_Vo_Vo_policy", vo, memberVo)) {
			throw new PrivilegeException(sess, "getAllAllowedGroupsToHierarchicalVo");
		}

		return getGroupsManagerBl().getAllAllowedGroupsToHierarchicalVo(sess, vo, memberVo);
	}

	@Override
	public List<Group> getGroupsWhereUserIsActiveMember(PerunSession sess, User user, Vo vo)
		throws VoNotExistsException, UserNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		if (!AuthzResolver.authorizedInternal(sess, "getGroupsWhereUserIsActiveMember_User_Vo_policy", user, vo)) {
			throw new PrivilegeException(sess, "getGroupsWhereUserIsActiveMember");
		}

		return getGroupsManagerBl().getGroupsWhereUserIsActiveMember(sess, user, vo);
	}
}
