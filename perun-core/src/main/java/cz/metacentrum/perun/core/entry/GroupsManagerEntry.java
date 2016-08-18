package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExternallyManagedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupOperationsException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MembershipMismatchException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.NotMemberOfParentGroupException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
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

	public Group createGroup(PerunSession sess, Vo vo, Group group) throws GroupExistsException, PrivilegeException, InternalErrorException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(group, "group");
		Utils.notNull(group.getName(), "group.name");


		if (!group.getName().matches(GroupsManager.GROUP_SHORT_NAME_REGEXP)) {
			throw new InternalErrorException(new IllegalArgumentException("Wrong group name, group name must matches " + GroupsManager.GROUP_SHORT_NAME_REGEXP));
		}

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

	public Group createGroup(PerunSession sess, Group parentGroup, Group group) throws GroupNotExistsException, GroupExistsException, PrivilegeException, InternalErrorException, GroupOperationsException {
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

		Group createdGroup = getGroupsManagerBl().createGroup(sess, parentGroup, group);

		//Refresh authz
		AuthzResolver.refreshAuthz(sess);
		return createdGroup;
	}

	public void deleteGroup(PerunSession sess, Group group, boolean forceDelete) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "deleteGroup");
				}

		getGroupsManagerBl().deleteGroup(sess, group, forceDelete);
	}

	public void deleteGroup(PerunSession sess, Group group) throws GroupNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException {
		this.deleteGroup(sess, group, false);
	}

	public void deleteAllGroups(PerunSession sess, Vo vo) throws VoNotExistsException, InternalErrorException, PrivilegeException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "deleteAllGroups");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		getGroupsManagerBl().deleteAllGroups(sess, vo);
	}
	
	public void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws GroupNotExistsException, InternalErrorException, PrivilegeException, GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupOperationsException {
		Utils.checkPerunSession(perunSession);
		Utils.notNull(groups, "groups");
		
		//Test if all groups exists and user has right to delete all of them
		for(Group group: groups) {
			getGroupsManagerBl().checkGroupExists(perunSession, group);
			//test of privileges on group
			if(!AuthzResolver.isAuthorized(perunSession, Role.VOADMIN, group) && !AuthzResolver.isAuthorized(perunSession, Role.GROUPADMIN, group)) {
				throw new PrivilegeException(perunSession, "deleteGroups");
			}
		}
		
		getGroupsManagerBl().deleteGroups(perunSession, groups, forceDelete);
	}

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

	public void addMember(PerunSession sess, Group group, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, AlreadyMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException, GroupOperationsException {
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
		if (Objects.equals("true", (String) attrSynchronizeEnabled.getValue())) {
			throw new ExternallyManagedException("Adding of member is not allowed. Group is externally managed.");
		}
		
		getGroupsManagerBl().addMember(sess, group, member);
	}

	public void removeMember(PerunSession sess, Group group, Member member) throws InternalErrorException, MemberNotExistsException, NotGroupMemberException, PrivilegeException, GroupNotExistsException, WrongAttributeAssignmentException, AttributeNotExistsException, ExternallyManagedException, GroupOperationsException {
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
		if (Objects.equals("true", (String) attrSynchronizeEnabled.getValue())) {
			throw new ExternallyManagedException("Removing of member is not allowed. Group is externally managed.");
		}

		getGroupsManagerBl().removeMember(sess, group, member);
	}

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

	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembers(sess, group), true);
	}

	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembers(sess, group, status), true);
	}

	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupRichMembersWithAttributes");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembersWithAttributes(sess, group), true);
	}

	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getGroupRichMembersWithAttributes");
				}
		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getGroupRichMembersWithAttributes(sess, group, status), true);
	}

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

	public List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getParentGroupRichMembers for " + group.getName());
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getParentGroupRichMembers(sess, group), true);
	}

	public List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group)
				&& !AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group)
				&& !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getParentGroupRichMembers for " + group.getName());
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getGroupsManagerBl().getParentGroupRichMembersWithAttributes(sess, group), true);
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

	public void synchronizeGroups(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN))  {
			throw new PrivilegeException(sess, "synchronizeGroups");
		}

		getGroupsManagerBl().synchronizeGroups(sess);
	}

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
				if(!AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, attribute, group, null)) {
					throw new PrivilegeException(sess, "Actor hasn't right to read attribute for a group.");
				}
		}
		
		return groups;
	}
	
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

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups);
	}

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

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups);
	}

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

		return getGroupsManagerBl().filterOnlyAllowedAttributes(sess, richGroups);
	}

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
	public Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupOperationsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, resultGroup);
		getGroupsManagerBl().checkGroupExists(sess, operandGroup);

		// Authorization
		if ((!AuthzResolver.isAuthorized(sess, Role.VOADMIN, resultGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, resultGroup)) ||
				(!AuthzResolver.isAuthorized(sess, Role.VOADMIN, operandGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, operandGroup))) {
			throw new PrivilegeException(sess, "createGroupUnion");
		}
		
		return getGroupsManagerBl().createGroupUnion(sess, resultGroup, operandGroup, false);
	}

	@Override
	public void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws InternalErrorException, GroupNotExistsException, PrivilegeException, GroupOperationsException {
		Utils.checkPerunSession(sess);
		getGroupsManagerBl().checkGroupExists(sess, resultGroup);
		getGroupsManagerBl().checkGroupExists(sess, operandGroup);

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
}
