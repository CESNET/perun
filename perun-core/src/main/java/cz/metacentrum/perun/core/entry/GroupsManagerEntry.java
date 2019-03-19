package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.GroupsManagerImplApi;
import java.util.Objects;

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
		throw new InternalErrorRuntimeException("Unsupported method!");
	}

	@Override
	public Group createGroup(PerunSession sess, Vo vo, Group group) throws GroupExistsException, PrivilegeException, InternalErrorException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(group, "group");
		Utils.notNull(group.getName(), "group.name");


		if (!group.getName().matches(GroupsManager.GROUP_SHORT_NAME_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong group name, group name must matches " + GroupsManager.GROUP_SHORT_NAME_REGEXP));
		}

		if (group.getParentGroupId() != null) throw new InternalErrorException("Top-level groups can't have parentGroupId set!");

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
		    !AuthzResolver.isAuthorized(sess, Role.TOPGROUPCREATOR, vo)) {
			throw new PrivilegeException(sess, "createGroup");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		Group createdGroup = getGroupsManagerBl().createGroup(sess, vo, group);

		//Refresh authz
		AuthzResolver.refreshAuthz(sess);
		return createdGroup;
	}

	@Override
	public Group createGroup(PerunSession sess, Group parentGroup, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException, InternalErrorException, GroupRelationNotAllowed, GroupRelationAlreadyExists, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, parentGroup);
		Utils.notNull(group, "group");
		Utils.notNull(group.getName(), "group.name");


		if (!group.getName().matches(GroupsManager.GROUP_SHORT_NAME_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong group name, group name must matches " + GroupsManager.GROUP_SHORT_NAME_REGEXP));
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
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
	public void deleteGroup(PerunSession sess, Group group, boolean forceDelete) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "deleteGroup");
				}

		if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group) || getGroupsManagerBl().hasGroupSynchronizedChild(sess, group)) {
			throw new ExternallyManagedException("Group " + group + " or some of the subGroups are externally managed");
		}
		getGroupsManagerBl().deleteGroup(sess, group, forceDelete);
	}

	@Override
	public void deleteGroup(PerunSession sess, Group group) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException {
		this.deleteGroup(sess, group, false);
	}

	@Override
	public void deleteAllGroups(PerunSession sess, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "deleteAllGroups");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		getGroupsManagerBl().deleteAllGroups(sess, vo);
	}

	@Override
	public void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws GroupNotExistsException, InternalErrorException, PrivilegeException, GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(groups, "groups");

		//Test if all groups exists and user has right to delete all of them
		for(Group group: groups) {
			getGroupsManagerBl().checkGroupExists(perunSession, group);

			if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(perunSession, group) || getGroupsManagerBl().hasGroupSynchronizedChild(perunSession, group)) {
				throw new ExternallyManagedException("Group " + group + " or some of the subGroups are externally managed!");
			}

			//test of privileges on group
			if(!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group) && !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
				throw new PrivilegeException(perunSession, "deleteGroups");
			}
		}

		getGroupsManagerBl().deleteGroups(perunSession, groups, forceDelete);
	}

	@Override
	public Group updateGroup(PerunSession sess, Group group) throws GroupNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		Utils.notNull(group, "group");
		Utils.notNull(group.getName(), "group.name");

		if (!group.getShortName().matches(GroupsManager.GROUP_SHORT_NAME_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong group shortName, group shortName must matches " + GroupsManager.GROUP_SHORT_NAME_REGEXP));
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "updateGroup");
				}

		return getGroupsManagerBl().updateGroup(sess, group);
	}

	@Override
	public void moveGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupMoveNotAllowedException, WrongAttributeValueException, WrongReferenceAttributeValueException, ExternallyManagedException, AttributeNotExistsException, WrongAttributeAssignmentException {
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
			if ((!AuthzResolver.isAuthorized(sess, Role.VOADMIN, movingGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, movingGroup)) ||
					(!AuthzResolver.isAuthorized(sess, Role.VOADMIN, destinationGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, destinationGroup))) {
				throw new PrivilegeException(sess, "moveGroup");
			}
		} else {
			// Authorization (destination group is null)
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, movingGroup)) {
				throw new PrivilegeException(sess, "moveGroup");
			}
		}

		getGroupsManagerBl().moveGroup(sess, destinationGroup, movingGroup);
	}

	@Override
	public Group getGroupById(PerunSession sess, int id) throws GroupNotExistsException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Group group = getGroupsManagerBl().getGroupById(sess, id);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC)) {
			throw new PrivilegeException(sess, "getGroupById");
				}

		return group;
	}

	@Override
	public Group getGroupByName(PerunSession sess, Vo vo, String name) throws GroupNotExistsException, InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		Utils.notNull(name, "name");

		if (!name.matches(GroupsManager.GROUP_FULL_NAME_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong group name, group name must matches " + GroupsManager.GROUP_FULL_NAME_REGEXP));
		}

		Group group = getGroupsManagerBl().getGroupByName(sess, vo, name);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.TOPGROUPCREATOR, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupByName");
				}

		return group;
	}

	@Override
	public void addMember(PerunSession sess, Group group, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
		    && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "addMember");
		}

		// Check if the member and group are from the same VO
		if (member.getVoId() != (group.getVoId())) {
			throw new MembershipMismatchException("Member and group are form the different VO");
		}

		// Check if the group is externally synchronized
		Attribute attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GROUPSYNCHROENABLED_ATTRNAME);
		if ("true".equals(attrSynchronizeEnabled.getValue()) || getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, group)) {
			throw new ExternallyManagedException("Adding of member is not allowed. Group is externally managed.");
		}

		getGroupsManagerBl().addMember(sess, group, member);
	}

	@Override
	public void removeMember(PerunSession sess, Group group, Member member) throws InternalErrorException, MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
		    && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
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
	public List<Member> getGroupMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupMembers");
				}

		return getGroupsManagerBl().getGroupMembers(sess, group);
	}

	@Override
	public List<Member> getGroupDirectMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupDirectMembers");
		}

		return getGroupsManagerBl().getGroupDirectMembers(sess, group);
	}

	@Override
	public List<Member> getActiveGroupMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
			&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
			&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getActiveGroupMembers");
		}

		return getGroupsManagerBl().getActiveGroupMembers(sess, group);
	}

	@Override
	public List<Member> getInactiveGroupMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
			&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
			&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getInactiveGroupMembers");
		}

		return getGroupsManagerBl().getInactiveGroupMembers(sess, group);
	}

	@Override
	public List<Member> getGroupMembers(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupMembers");
				}

		return getGroupsManagerBl().getGroupMembers(sess, group, status);
	}

	@Override
	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembers(sess, group), group, true);
	}

	@Override
	public List<RichMember> getGroupDirectRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupDirectRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupDirectRichMembers(sess, group), group, true);
	}

	@Override
	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembers(sess, group, status), group, true);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupRichMembersWithAttributes");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembersWithAttributes(sess, group), group, true);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupRichMembersWithAttributes");
				}
		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembersWithAttributes(sess, group, status), group, true);
	}

	@Override
	public boolean isGroupMember(PerunSession sess, Group group, Member member) throws PrivilegeException, GroupNotExistsException, InternalErrorException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "isGroupMember");
		}

		return getGroupsManagerBl().isGroupMember(sess, group, member);
	}

	@Override
	public int getGroupMembersCount(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupMembersCount");
				}

		return getGroupsManagerBl().getGroupMembersCount(sess, group);
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "addAdmin");
				}

		getGroupsManagerBl().addAdmin(sess, group, user);
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {

			throw new PrivilegeException(sess, "addAdmin");
				}

		getGroupsManagerBl().addAdmin(sess, group, authorizedGroup);
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotAdminException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "removeAdmin");
				}

		getGroupsManagerBl().removeAdmin(sess, group, user);
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, GroupNotAdminException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getGroupsManagerBl().checkGroupExists(sess, authorizedGroup);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "removeAdmin");
				}

		getGroupsManagerBl().removeAdmin(sess, group, authorizedGroup);
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Group group, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group) &&
		    !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group) &&
		    !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(perunSession, "getAdmins");
		}

		return getGroupsManagerBl().getAdmins(perunSession, group, onlyDirectAdmins);
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);
		//list of specific attributes must be not null if filtering is needed
		if(!allUserAttributes) {
			Utils.notNull(specificAttributes, "specificAttributes");
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(perunSession, "getRichAdmins");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdmins(perunSession, group, specificAttributes, allUserAttributes, onlyDirectAdmins));
	}

	@Override
	@Deprecated
	public List<User> getAdmins(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getAdmins");
				}

		return getGroupsManagerBl().getAdmins(sess, group);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getDirectAdmins");
				}

		return getGroupsManagerBl().getDirectAdmins(sess, group);
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getAdminGroups");
				}

		return getGroupsManagerBl().getAdminGroups(sess, group);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(perunSession, "getRichAdmins");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdmins(perunSession, group));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(perunSession, "getRichAdminsWithAttributes");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdminsWithAttributes(perunSession, group));
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(perunSession, "getRichAdminsWithSpecificAttributes");
				}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getRichAdminsWithSpecificAttributes(perunSession, group, specificAttributes));
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, GroupNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(perunSession);
		getGroupsManagerBl().checkGroupExists(perunSession, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(perunSession, "getDirectRichAdminsWithSpecificAttributes");
		}

		return getPerunBl().getUsersManagerBl().filterOnlyAllowedAttributes(perunSession, getGroupsManagerBl().getDirectRichAdminsWithSpecificAttributes(perunSession, group, specificAttributes));
	}

	@Override
	public List<Group> getAllGroups(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getAllGroups");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);


		List<Group> groups = getGroupsManagerBl().getAllGroups(sess, vo);

		// Return all groups for VOADMIN and PERUNADMIN
		if (AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				|| AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
				|| AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN)) {
			return groups;
				}

		// Check access rights for each group for GROUPADMIN
		if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.GROUPADMIN)) {
			Iterator<Group> eachGroup = groups.iterator();
			while (eachGroup.hasNext()) {
				if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, eachGroup.next())) {
					eachGroup.remove();
				}
			}
			return groups;
		}

		// This shouldn't happen
		throw new PrivilegeException(sess, "getAllGroups");
	}

	@Override
	public Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getAllGroupsWithHierarchy");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);


		Map<Group, Object> groups =  getGroupsManagerBl().getAllGroupsWithHierarchy(sess, vo);

		// Return all groups for VOADMIN and PERUNADMIN
		if (AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				|| AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN)
				|| AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			return groups;
				}

		// Check access rights for each group for GROUPADMIN
		if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.GROUPADMIN)) {
			Iterator<Group> eachGroup = groups.keySet().iterator();
			while (eachGroup.hasNext()) {
				if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, eachGroup.next())) {
					eachGroup.remove();
				}
			}
			return groups;
		}

		// This shouldn't happen
		throw new PrivilegeException(sess, "getAllGroupsWithHierarchy");
	}

	@Override
	public List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
			throw new PrivilegeException(sess, "getSubGroups");
				}

		return getGroupsManagerBl().getSubGroups(sess, parentGroup);
	}

	@Override
	public List<Group> getAllSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
			throw new PrivilegeException(sess, "getAllSubGroups");
				}

		return getGroupsManagerBl().getAllSubGroups(sess, parentGroup);
	}

	@Override
	public Group getParentGroup(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getParentGroup");
				}

		return getGroupsManagerBl().getParentGroup(sess, group);
	}

	@Override
	public List<Group> getGroups(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			throw new PrivilegeException(sess, "getGroups");
				}

		List<Group> groups =  getGroupsManagerBl().getGroups(sess, vo);

		// Return all groups for VOADMIN and PERUNADMIN
		if (AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				|| AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN)
				|| AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			return groups;
				}

		// Check access rights for each group for GROUPADMIN
		if (AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.GROUPADMIN)) {
			Iterator<Group> eachGroup = groups.iterator();
			while (eachGroup.hasNext()) {
				if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, eachGroup.next())) {
					eachGroup.remove();
				}
			}
			return groups;
		}

		// This shouldn't happen
		throw new PrivilegeException(sess, "getGroups");
	}

	@Override
	public int getGroupsCount(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getGroupsCount");
		}

		return getGroupsManagerBl().getGroupsCount(sess, vo);
	}

	@Override
	public int getGroupsCount(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		return getGroupsManagerBl().getGroupsCount(sess);
	}

	@Override
	public int getSubGroupsCount(PerunSession sess, Group parentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
			throw new PrivilegeException(sess, "getSubGroupsCount for " + parentGroup.getName());
				}

		return getGroupsManagerBl().getSubGroupsCount(sess, parentGroup);
	}

	@Override
	public Vo getVo(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		Vo vo =  getGroupsManagerBl().getVo(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getVo");
				}

		return vo;
	}

	@Override
	public List<Member> getParentGroupMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getParentGroupMembers for " + group.getName());
				}

		return getGroupsManagerBl().getParentGroupMembers(sess, group);
	}

	@Override
	public List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getParentGroupRichMembers for " + group.getName());
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getParentGroupRichMembers(sess, group), group, true);
	}

	@Override
	public List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getParentGroupRichMembers for " + group.getName());
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
	public void forceGroupSynchronization(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupSynchronizationAlreadyRunningException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group))  {
			throw new PrivilegeException(sess, "synchronizeGroup");
		}

		getGroupsManagerBl().forceGroupSynchronization(sess, group);
	}

	@Override
	public void forceGroupStructureSynchronization(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupStructureSynchronizationAlreadyRunningException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
			&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group))  {
			throw new PrivilegeException(sess, "forceGroupStructureSynchronization");
		}

		getGroupsManagerBl().forceGroupStructureSynchronization(sess, group);
	}

	@Override
	public void synchronizeGroups(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))  {
			throw new PrivilegeException(sess, "synchronizeGroups");
		}

		getGroupsManagerBl().synchronizeGroups(sess);
	}

	@Override
	public void synchronizeGroupsStructures(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))  {
			throw new PrivilegeException(sess, "synchronizeGroupsStructures");
		}

		getGroupsManagerBl().synchronizeGroupsStructures(sess);
	}

	@Override
	public List<Group> getMemberGroups(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getMemberGroups for " + member);
				}

		return getGroupsManagerBl().getMemberGroups(sess, member);
	}


	@Override
	public List<Group> getMemberGroupsByAttribute(PerunSession sess, Member member, Attribute attribute) throws WrongAttributeAssignmentException, PrivilegeException,InternalErrorException, VoNotExistsException, MemberNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getPerunBl().getAttributesManagerBl().checkAttributeExists(sess, new AttributeDefinition(attribute));

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		//Only group attributes are allowed
		if(!this.getPerunBl().getAttributesManagerBl().isFromNamespace(sess, attribute, AttributesManagerEntry.NS_GROUP_ATTR)) {
			throw new WrongAttributeAssignmentException(attribute);
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getMemberGroupsByAttribute for " + member);
		}

		List<Group> groups = this.groupsManagerBl.getMemberGroupsByAttribute(sess, member, attribute);

		//If actor has no right to read attribute for group, throw exception
		for(Group group: groups) {
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attribute, group)) {
					throw new PrivilegeException(sess, "Actor hasn't right to read attribute for a group.");
				}
		}

		return groups;
	}

	@Override
	public List<Group> getAllMemberGroups(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
				&& !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getAllMemberGroups for " + member);
				}

		return getGroupsManagerBl().getAllMemberGroups(sess, member);
	}

	@Override
	public List<Group> getGroupsWhereMemberIsActive(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getGroupsWhereMemberIsActive");
		}

		return getGroupsManagerBl().getGroupsWhereMemberIsActive(sess, member);
	}

	@Override
	public List<Group> getGroupsWhereMemberIsInactive(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getGroupsWhereMemberIsInactive");
		}

		return getGroupsManagerBl().getGroupsWhereMemberIsInactive(sess, member);
	}

	@Override
	public List<Group> getAllGroupsWhereMemberIsActive(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getAllGroupsWhereMemberIsActive");
		}

		return getGroupsManagerBl().getAllGroupsWhereMemberIsActive(sess, member);
	}

	@Override
	public List<RichGroup> getRichGroupsAssignedToResourceWithAttributesByNames(PerunSession sess, Resource resource, List<String> attrNames) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getResourcesManagerBl().checkResourceExists(sess, resource);

		Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, resource) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "getRichGroupsAssignedToResourceWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getRichGroupsWithAttributesAssignedToResource(sess, resource, attrNames);

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, resource, true);
	}

	public List<RichGroup> getMemberRichGroupsWithAttributesByNames(PerunSession sess, Member member, List<String> attrNames) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getMemberRichGroupsWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getMemberRichGroupsWithAttributesByNames(sess, member, attrNames);

		// Check access rights for each richGroup for GROUPADMIN
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
			&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
			&& AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			Iterator<RichGroup> groupByName = richGroups.iterator();
			while (groupByName.hasNext()) {
				if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, groupByName.next())) {
					groupByName.remove();
				}
			}
		}

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	public List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames) throws InternalErrorException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
		        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
		        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getAllRichGroupsWithAttributesByNames");
		}

		List<RichGroup> richGroups = getGroupsManagerBl().getAllRichGroupsWithAttributesByNames(sess, vo, attrNames);

		// Check access rights for each richGroup for GROUPADMIN
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)
		        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)
		        && AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			Iterator<RichGroup> groupByName = richGroups.iterator();
			while (groupByName.hasNext()) {
				if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, groupByName.next())) {
					groupByName.remove();
				}
			}
		}

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	public List<RichGroup> getRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) throws InternalErrorException, GroupNotExistsException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
		        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
		        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
			throw new PrivilegeException(sess, "getRichSubGroupsWithAttributesByNames");
		}
		List<RichGroup> richGroups = getGroupsManagerBl().getRichSubGroupsWithAttributesByNames(sess, parentGroup, attrNames);

		// Check access rights for each richGroup for GROUPADMIN
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
				&& AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			Iterator<RichGroup> eachGroup = richGroups.iterator();
			while (eachGroup.hasNext()) {
				if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, eachGroup.next())) {
					eachGroup.remove();
				}
			}
		}

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	public List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) throws InternalErrorException, GroupNotExistsException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		this.getGroupsManagerBl().checkGroupExists(sess, parentGroup);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, parentGroup)) {
			throw new PrivilegeException(sess, "getAllRichSubGroupsWithAttributesByNames");
		}
		List<RichGroup> richGroups = getGroupsManagerBl().getAllRichSubGroupsWithAttributesByNames(sess, parentGroup, attrNames);

		// Check access rights for each richGroup for GROUPADMIN
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, parentGroup)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, parentGroup)
				&& AuthzResolver.isAuthorized(sess, Role.GROUPADMIN)) {
			Iterator<RichGroup> eachGroup = richGroups.iterator();
			while (eachGroup.hasNext()) {
				if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, eachGroup.next())) {
					eachGroup.remove();
				}
			}
		}

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups, null, true);
	}

	@Override
	public RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames) throws InternalErrorException, GroupNotExistsException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Group group = groupsManagerBl.getGroupById(sess, groupId);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
		        && !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
		        && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getRichGroupByIdWithAttributesByNames");
		}

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getRichGroupByIdWithAttributesByNames(sess, groupId, attrNames));

	}

	@Override
	public Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupRelationNotAllowed, GroupRelationAlreadyExists, WrongAttributeValueException, WrongReferenceAttributeValueException, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, resultGroup);
		getGroupsManagerBl().checkGroupExists(sess, operandGroup);

		if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, resultGroup)) {
			throw new ExternallyManagedException("Result group: " + resultGroup + " is externally managed!");
		}

		// Authorization
		if ((!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resultGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, resultGroup)) ||
				(!AuthzResolver.isAuthorized(sess, Role.VOADMIN, operandGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, operandGroup))) {
			throw new PrivilegeException(sess, "createGroupUnion");
		}

		return getGroupsManagerBl().createGroupUnion(sess, resultGroup, operandGroup, false);
	}

	@Override
	public void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, ExternallyManagedException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, resultGroup);
		getGroupsManagerBl().checkGroupExists(sess, operandGroup);

		if (getGroupsManagerBl().isGroupInStructureSynchronizationTree(sess, resultGroup)) {
			throw new ExternallyManagedException("Result group: " + resultGroup + " is externally managed!");
		}

		// Authorization
		if ( (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resultGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, resultGroup)) ||
				(!AuthzResolver.isAuthorized(sess, Role.VOADMIN, operandGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, operandGroup)) ) {
			throw new PrivilegeException(sess, "removeGroupUnion");
		}

		getGroupsManagerBl().removeGroupUnion(sess, resultGroup, operandGroup, false);
	}

	@Override
	public List<Group> getGroupUnions(PerunSession sess, Group group, boolean reverseDirection) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if ( !AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupUnions");
		}

		return groupsManagerBl.getGroupUnions(sess, group, reverseDirection);
	}

	@Override
	public Member setMemberGroupStatus(PerunSession sess, Member member, Group group, MemberGroupStatus status) throws InternalErrorException, GroupNotExistsException, MemberNotExistsException, PrivilegeException, NotGroupMemberException {

		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if ( !AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
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
	public Member getGroupMemberById(PerunSession sess, Group group, int memberId) throws InternalErrorException, NotGroupMemberException, GroupNotExistsException, PrivilegeException {

		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if ( !AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupMemberById");
		}

		return perunBl.getGroupsManagerBl().getGroupMemberById(sess, group, memberId);

	}

	@Override
	public void extendMembershipInGroup(PerunSession sess, Member member, Group group) throws InternalErrorException, ExtendMembershipException, PrivilegeException, MemberNotExistsException, GroupNotExistsException {

		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "extendMembershipInGroup");
		}

		getGroupsManagerBl().extendMembershipInGroup(sess, member, group);

	}

	@Override
	public boolean canExtendMembershipInGroup(PerunSession sess, Member member, Group group) throws InternalErrorException, MemberNotExistsException, GroupNotExistsException, PrivilegeException {

		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "canExtendMembershipInGroup");
		}

		return getGroupsManagerBl().canExtendMembershipInGroup(sess, member, group);
	}

	@Override
	public boolean canExtendMembershipInGroupWithReason(PerunSession sess, Member member, Group group) throws InternalErrorException, MemberNotExistsException, GroupNotExistsException, PrivilegeException, ExtendMembershipException {

		Utils.checkPerunSession(sess);
		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "canExtendMembershipInGroupWithReason");
		}

		return getGroupsManagerBl().canExtendMembershipInGroupWithReason(sess, member, group);
	}

}
