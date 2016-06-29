package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.PerunPrincipal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.GroupsManagerImplApi;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GroupsManager business logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class GroupsManagerBlImpl implements GroupsManagerBl {

	private final static Logger log = LoggerFactory.getLogger(GroupsManagerBlImpl.class);

	private final GroupsManagerImplApi groupsManagerImpl;
	private PerunBl perunBl;

	private Map<Integer, GroupSynchronizerThread> groupSynchronizerThreads;
	private static final String A_G_D_AUTHORITATIVE_GROUP = AttributesManager.NS_GROUP_ATTR_DEF + ":authoritativeGroup";

	/**
	 * Create new instance of this class.
	 *
	 */
	public GroupsManagerBlImpl(GroupsManagerImplApi groupsManagerImpl) {
		this.groupsManagerImpl = groupsManagerImpl;
		this.groupSynchronizerThreads = new HashMap<Integer, GroupSynchronizerThread>();
	}

	public Group createGroup(PerunSession sess, Vo vo, Group group) throws GroupExistsException, InternalErrorException {
		group = getGroupsManagerImpl().createGroup(sess, vo, group);
		getPerunBl().getAuditer().log(sess, "{} created in {}.", group, vo);
		group.setVoId(vo.getId());


		//set creator as group admin unless he already have authz right on the group (he is VO admin)
		User user = sess.getPerunPrincipal().getUser();
		if(user != null) {   //user can be null in tests
			if(!AuthzResolverBlImpl.isAuthorized(sess, Role.VOADMIN, vo)) {
				try {
					AuthzResolverBlImpl.setRole(sess, user, group, Role.GROUPADMIN);
				} catch (AlreadyAdminException e) {
					throw new ConsistencyErrorException("Newly created group already have an admin.", e);
				}
			}
		}

		return group;
	}

	public Group createGroup(PerunSession sess, Group parentGroup, Group group) throws GroupExistsException, InternalErrorException {
		Vo vo = this.getVo(sess, parentGroup);

		group = getGroupsManagerImpl().createGroup(sess, vo, parentGroup, group);

		getPerunBl().getAuditer().log(sess, "{} created in {} as subgroup of {}", group, vo, parentGroup);

		return group;
	}

	public void deleteGroup(PerunSession sess, Group group, boolean forceDelete)  throws InternalErrorException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new java.lang.IllegalArgumentException("Built-in " + group.getName() + " group cannot be deleted separately.");
		}

		this.deleteAnyGroup(sess, group, forceDelete);
	}
	
	public void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws InternalErrorException, GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException {
		//Use sorting by group names reverse order (first name A:B:c then A:B etc.)
		Collections.sort(groups, Collections.reverseOrder(
				new Comparator<Group>() {
					@Override
					public int compare(Group groupToCompare,Group groupToCompareWith) {
						return groupToCompare.getName().compareTo(groupToCompareWith.getName());
					}
				}));

		for(Group group: groups) {
			this.deleteGroup(perunSession, group, forceDelete);
		}
	}

	public void deleteMembersGroup(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		Group group;
		try {
			group = getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
		} catch (GroupNotExistsException e) {
			throw new ConsistencyErrorException("Built-in members group must exists.",e);
		}
		try {
			this.deleteAnyGroup(sess, group, true);
		} catch (RelationExistsException e) {
			throw new ConsistencyErrorException("Built-in members group cannot have any relation in this stage.",e);
		}
	}

	/**
	 * If forceDelete is false, delete only group which has no subgroup and no member.
	 * If forceDelete is true, delete group with all subgroups and members.
	 *
	 * @param sess
	 * @param group
	 * @param forceDelete if false, delete only empty group without subgroups. If true, delete group including subgroups and members.
	 * @throws InternalErrorException
	 * @throws RelationExistsException Raise only if forceDelete is false and the group has any subgroup or member.
	 * @throws GroupAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 */
	protected void deleteAnyGroup(PerunSession sess, Group group, boolean forceDelete) throws InternalErrorException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		Vo vo = this.getVo(sess, group);

		if (getGroupsManagerImpl().getSubGroupsCount(sess, group) > 0) {
			if (!forceDelete) throw new RelationExistsException("Group group="+group+" contains subgroups");
			List<Group> subGroups = getGroupsManagerImpl().getSubGroups(sess, group);
			for (Group g : subGroups) {
				//For auditer
				List<Resource> subGroupResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, g);
				for(Resource resource : subGroupResources) {
					try {
						getPerunBl().getResourcesManagerBl().removeGroupFromResource(sess, g, resource);
					} catch(GroupNotDefinedOnResourceException ex) {
						throw new ConsistencyErrorException(ex);
					}
				}

				//remove subgroups' attributes
				try {
					getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, g);
				} catch(AttributeValueException ex) {
					throw new ConsistencyErrorException("All resources was removed from this group. So all attributes values can be removed.", ex);
				}

				// delete all sub-groups reserved logins from KDC
				List<Integer> list = getGroupsManagerImpl().getGroupApplicationIds(sess, group);
				for (Integer appId : list) {
					// for each application
					for (Pair<String, String> login : getGroupsManagerImpl().getApplicationReservedLogins(appId)) {
						// for all reserved logins - delete them in ext. system (e.g. KDC)
						try {
							// left = namespace / right = login
							getPerunBl().getUsersManagerBl().deletePassword(sess, login.getRight(), login.getLeft());
						} catch (LoginNotExistsException ex) {
							log.error("Login: {} not exists in namespace: {} while deleting passwords.", login.getRight(), login.getLeft());
						} catch (PasswordDeletionFailedException ex) {
							throw new InternalErrorException("Failed to delete reserved login "+login.getRight()+" from KDC.", ex);
						}
					}
				}
				// delete all Groups reserved logins from DB
				getGroupsManagerImpl().deleteGroupReservedLogins(sess, group);

				// Group applications, submitted data and app_form are deleted on cascade with "deleteGroup()"

				List<Member> membersFromDeletedGroup = getGroupMembers(sess, g);
				getGroupsManagerImpl().deleteGroup(sess, vo, g);

				Integer parentGroupId = g.getParentGroupId();
				while(parentGroupId != null) {
					Group parentGroup;
					try {
						parentGroup = getGroupById(sess, parentGroupId);
					} catch (GroupNotExistsException ex) {
						throw new ConsistencyErrorException(ex);
					}
					List<Member> membersFromParentGroup = getGroupMembers(sess, parentGroup);
					membersFromDeletedGroup.removeAll(membersFromParentGroup);
					for(Member m: membersFromDeletedGroup) {
						getPerunBl().getAuditer().log(sess, "{} was removed from {} totally.", m, parentGroup);
					}
					parentGroupId=parentGroup.getParentGroupId();
				}

				getPerunBl().getAuditer().log(sess, "{} deleted.", g);

			}
		}
		if ((this.getGroupMembersCount(sess, group) > 0) && !forceDelete) {
			throw new RelationExistsException("Group group="+group+" contains members");
		}

		List<Resource> assignedResources  = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		try {
			for(Resource resource : assignedResources) {
				getPerunBl().getResourcesManagerBl().removeGroupFromResource(sess, group, resource);
			}
			//remove group's attributes
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, group);
		} catch(GroupNotDefinedOnResourceException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(AttributeValueException ex) {
			throw new ConsistencyErrorException("All resources was removed from this group, so no attributes should remain assigned.", ex);
		}

		// delete all Groups reserved logins from KDC
		List<Integer> list = getGroupsManagerImpl().getGroupApplicationIds(sess, group);
		for (Integer appId : list) {
			// for each application
			for (Pair<String, String> login : getGroupsManagerImpl().getApplicationReservedLogins(appId)) {
				// for all reserved logins - delete them in ext. system (e.g. KDC)
				try {
					// left = namespace / right = login
					getPerunBl().getUsersManagerBl().deletePassword(sess, login.getRight(), login.getLeft());
				} catch (LoginNotExistsException ex) {
					log.error("Login: {} not exists in namespace: {} while deleting passwords.", login.getRight(), login.getLeft());
				} catch (PasswordDeletionFailedException ex) {
					throw new InternalErrorException("Failed to delete reserved login "+login.getRight()+" from KDC.", ex);
				}
			}
		}
		// delete all Groups reserved logins from DB
		getGroupsManagerImpl().deleteGroupReservedLogins(sess, group);

		//Remove all information about group on facilities (facilities contacts)
		List<ContactGroup> groupContactGroups = getPerunBl().getFacilitiesManagerBl().getFacilityContactGroups(sess, group);
		if(!groupContactGroups.isEmpty()) {
			if(forceDelete) {
				getPerunBl().getFacilitiesManagerBl().removeAllGroupContacts(sess, group);
			} else {
				throw new RelationExistsException("Group has still some facilities contacts: " + groupContactGroups);
			}
		}

		//remove all assigned ExtSources to this group
		List<ExtSource> assignedSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group);
		for(ExtSource source: assignedSources) {
			try {
				getPerunBl().getExtSourcesManagerBl().removeExtSource(sess, group, source);
			} catch (ExtSourceNotAssignedException | ExtSourceAlreadyRemovedException ex) {
				//Just log this, because if method can't remove it, it is probably not assigned now
				log.error("Try to remove not existing extSource {} from group {} when deleting group.", source, group);
			}
		}

		// Group applications, submitted data and app_form are deleted on cascade with "deleteGroup()"
		List<Member> membersFromDeletedGroup = getGroupMembers(sess, group);
		getGroupsManagerImpl().deleteGroup(sess, vo, group);

		Integer parentGroupId = group.getParentGroupId();
		while(parentGroupId != null) {
			Group parentGroup;
			try {
				parentGroup = getGroupById(sess, parentGroupId);
			} catch (GroupNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
			List<Member> membersFromParentGroup = getGroupMembers(sess, parentGroup);
			membersFromDeletedGroup.removeAll(membersFromParentGroup);
			for(Member m: membersFromDeletedGroup) {
				getPerunBl().getAuditer().log(sess, "{} was removed from {} totally.", m, parentGroup);
			}
			parentGroupId=parentGroup.getParentGroupId();
		}

		getPerunBl().getAuditer().log(sess, "{} deleted.", group);
	}

	public void deleteAllGroups(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException {
		for(Group group: getGroupsManagerImpl().getGroups(sess, vo)) {

			if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
				// Do not delete built-in groups, they must be deleted using separate functions deleteMembersGroup
				continue;
			}
			List<Resource> assignedResources  = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
			try {
				for(Resource resource : assignedResources) {
					getPerunBl().getResourcesManagerBl().removeGroupFromResource(sess, group, resource);
					getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource, group);
				}
				//remove group's attributes
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, group);
			} catch(GroupNotDefinedOnResourceException ex) {
				throw new ConsistencyErrorException(ex);
			} catch(AttributeValueException ex) {
				throw new ConsistencyErrorException("All resources was removed from this group. So all attributes values can be removed.", ex);
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}

			try {
				this.deleteGroup(sess, group, true);
			} catch (RelationExistsException e) {
				throw new ConsistencyErrorException(e);
			}
		}
		getPerunBl().getAuditer().log(sess, "All group in {} deleted.", vo);
	}

	public Group updateGroup(PerunSession sess, Group group) throws InternalErrorException {

		// return group with correct updated name and shortName
		group = getGroupsManagerImpl().updateGroup(sess, group);
		getPerunBl().getAuditer().log(sess, "{} updated.", group);

		List<Group> allSubgroups = this.getAllSubGroups(sess, group);
		String[] groupNames = group.getName().split(":");

		for(Group g: allSubgroups) {
			String[] subGroupNames = g.getName().split(":");
			for (int i=0; i<groupNames.length; i++) {
				if (!subGroupNames[i].equals(groupNames[i])) {
					// this part of name changed
					subGroupNames[i] = groupNames[i];
				}
			}
			// create new name
			StringBuilder sb = new StringBuilder();
			for (String sgName : subGroupNames) {
				sb.append(sgName).append(":");
			}
			// set name without last ":"
			g.setName(sb.toString().substring(0, sb.length()-1));
			// for subgroups we must update whole name
			getGroupsManagerImpl().updateGroupName(sess, g);
			// create auditer message for every updated group
			getPerunBl().getAuditer().log(sess, "{} updated.", g);
		}

		return group;
	}

	public Group getGroupById(PerunSession sess, int id) throws InternalErrorException, GroupNotExistsException {
		return getGroupsManagerImpl().getGroupById(sess, id);
	}

	public List<Group> getGroupsToSynchronize(PerunSession sess) throws InternalErrorException{
		return getGroupsManagerImpl().getGroupsToSynchronize(sess);
	}

	public Group getGroupByName(PerunSession sess, Vo vo, String name) throws InternalErrorException, GroupNotExistsException {
		return getGroupsManagerImpl().getGroupByName(sess, vo, name);
	}

	public void addMemberToMembersGroup(PerunSession sess, Group group,  Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.addMemberInternal(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.addAdmin and MembersManager.createMember.");
		}
	}

	public void addMember(PerunSession sess, Group group, Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException {
		// Check if the group is NOT members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Cannot add member directly to the members group.");
		} else {
			this.addMemberInternal(sess, group, member);
		}
	}

	private List<Group> getParentGroups(PerunSession sess, Group group)throws InternalErrorException {
		if(group == null) return new ArrayList<Group>();
		try {
			if (group.getParentGroupId() == null) return new ArrayList<Group>();
			List<Group> groups = getParentGroups(sess,getGroupById(sess,group.getParentGroupId()));
			groups.add(getGroupById(sess, group.getParentGroupId()));
			return groups;
		} catch(GroupNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	protected void addMemberInternal(PerunSession sess, Group group, Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException {

		if(this.groupsManagerImpl.isDirectGroupMember(sess, group, member)) throw new AlreadyMemberException(member);

		/*// Check if the member is from parentGroup
			if(group.getParentGroupId() != null) {
			try {
			Group parentGroup = this.getGroupById(sess, group.getParentGroupId());
			if (!this.getGroupMembers(sess, parentGroup).contains(member)) {
		// The member is not member of the parent group, so deny the request
		throw new NotMemberOfParentGroupException("Parent group " + parentGroup);
			}
			} catch (GroupNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
			}
			}
			Member nemusí být obsažen v rodiči, bude tam automaticky zapsán jako indirect */

		member = getGroupsManagerImpl().addMember(sess, group, member, MembershipType.DIRECT, group.getId());
		getPerunBl().getAuditer().log(sess, "{} added to {}.", member, group);

		// only if group have parent !!
		if (group.getParentGroupId() != null) {
			for(Group parentGroups : getParentGroups(sess, group)){
				member = getGroupsManagerImpl().addMember(sess, parentGroups, member, MembershipType.INDIRECT, group.getId());
				getPerunBl().getAuditer().log(sess, "{} added to {}.", member, parentGroups);
			}
		}

		User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for (Resource resource : resources) {
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			// check members attributes
			try {
				getPerunBl().getAttributesManagerBl().setRequiredAttributes(sess, facility, resource, user, member);
			} catch(WrongAttributeAssignmentException ex) {
				throw new ConsistencyErrorException(ex);
			} catch(AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}
	}

	public void removeMember(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException {
		// Check if the group is NOT members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Cannot remove member directly from the members group.");
		} else {
			this.removeMemberInternal(sess, group, member);
		}
	}

	public void removeMemberFromMembersOrAdministratorsGroup(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.removeMemberInternal(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.removeAdmin and MembersManager.deleteMember.");
		}
	}

	protected void removeMemberInternal(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException {

		member.setMembershipType(MembershipType.DIRECT);
		getGroupsManagerImpl().removeMember(sess, group, member);
		if (this.getGroupsManagerImpl().isGroupMember(sess, group, member)) {
			getPerunBl().getAuditer().log(sess, "{} was removed from {}.", member, group);
		} else {
			getPerunBl().getAuditer().log(sess, "{} was removed from {} totally.", member, group);
		}

		// only if group have parents !!
		if (group.getParentGroupId() != null) {
			member.setMembershipType(MembershipType.INDIRECT);
			for(Group parentGroup: getParentGroups(sess, group)) {
				// there's no need to call remove, since remove of direct removes all indirect too.
				if (this.getGroupsManagerImpl().isGroupMember(sess, parentGroup, member)) {
					getPerunBl().getAuditer().log(sess, "{} was removed from {}.", member, parentGroup);
				} else {
					getPerunBl().getAuditer().log(sess, "{} was removed from {} totally.", member, parentGroup);
				}
			}
		}

	}

	public List<Member> getGroupMembers(PerunSession sess, Group group) throws InternalErrorException {
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupMembers(sess, group));
	}

	public List<Member> getGroupMembers(PerunSession sess, Group group, Status status) throws InternalErrorException {
		if (status == null) {
			return this.getGroupMembers(sess, group);
		}
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupMembers(sess, group, Arrays.asList(status), false));
	}

	@Override
	public List<User> getGroupUsers(PerunSession perunSession, Group group) throws InternalErrorException {
		return new ArrayList<User>(new HashSet<User>(getGroupsManagerImpl().getGroupUsers(perunSession, group)));
	}

	public List<Member> getGroupMembersExceptInvalid(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupMembers(sess, group, Arrays.asList(Status.INVALID), true);
	}

	public List<Member> getGroupMembersExceptInvalidAndDisabled(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupMembers(sess, group, Arrays.asList(Status.INVALID, Status.DISABLED), true);
	}

	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException {
		return this.getGroupRichMembers(sess, group, null);
	}

	public List<RichMember> getGroupRichMembersExceptInvalid(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> members = this.getGroupMembersExceptInvalid(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws InternalErrorException {
		List<Member> members = this.getGroupMembers(sess, group, status);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException {
		return this.getGroupRichMembersWithAttributes(sess, group, null);
	}

	public List<RichMember> getGroupRichMembersWithAttributesExceptInvalid(PerunSession sess, Group group) throws InternalErrorException {
		List<RichMember> richMembers = this.getGroupRichMembersExceptInvalid(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws InternalErrorException {
		List<RichMember> richMembers = this.getGroupRichMembers(sess, group, status);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	public int getGroupMembersCount(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> members = this.getGroupMembers(sess, group);
		return members.size();
	}

	public void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException {
		AuthzResolverBlImpl.setRole(sess, user, group, Role.GROUPADMIN);
		getPerunBl().getAuditer().log(sess, "{} was added as admin of {}.", user, group);
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException {
		List<Group> listOfAdmins = getAdminGroups(sess, group);
		if (listOfAdmins.contains(authorizedGroup)) throw new AlreadyAdminException(authorizedGroup);

		AuthzResolverBlImpl.setRole(sess, authorizedGroup, group, Role.GROUPADMIN);
		getPerunBl().getAuditer().log(sess, "Group {} was added as admin of {}.", authorizedGroup, group);
	}

	public void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, UserNotAdminException {
		AuthzResolverBlImpl.unsetRole(sess, user, group, Role.GROUPADMIN);
		getPerunBl().getAuditer().log(sess, "{} was removed from admins of {}.", user, group);
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException {
		List<Group> listOfAdmins = getAdminGroups(sess, group);
		if (!listOfAdmins.contains(authorizedGroup)) throw new GroupNotAdminException(authorizedGroup);

		AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, group, Role.GROUPADMIN);
		getPerunBl().getAuditer().log(sess, "Group {} was removed from admins of {}.", authorizedGroup, group);
	}

	public List<User> getAdmins(PerunSession perunSession, Group group, boolean onlyDirectAdmins) throws InternalErrorException {
		if(onlyDirectAdmins) {
			return getGroupsManagerImpl().getDirectAdmins(perunSession, group);
		} else {
			return getGroupsManagerImpl().getAdmins(perunSession, group);
		}
	}

	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, group, onlyDirectAdmins);
		List<RichUser> richUsers;

		if(allUserAttributes) {
			richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
		} else {
			try {
				richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("One of Attribute not exist.", ex);
			}
		}

		return richUsers;
	}

	@Deprecated
	public List<User> getAdmins(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getAdmins(sess, group);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getDirectAdmins(sess, group);
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupAdmins(sess, group);
	}

	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
		return richUsers;
	}

	@Deprecated
	public List<RichUser> getDirectRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getDirectAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
		return richUsers;
	}

	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
		return richUsers;
	}

	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getRichAdmins(perunSession, group), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getDirectRichAdmins(perunSession, group), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	public List<Group> getAssignedGroupsToResource(PerunSession sess, Resource resource) throws InternalErrorException {
		return getAssignedGroupsToResource(sess, resource, false);
	}

	public List<Group> getAssignedGroupsToResource(PerunSession sess, Resource resource, boolean withSubGroups) throws InternalErrorException {
		List<Group> assignedGroups = getGroupsManagerImpl().getAssignedGroupsToResource(sess, resource);
		if(!withSubGroups) return assignedGroups;

		boolean done = assignedGroups.isEmpty();
		List<Group> groupsToProcess = new ArrayList<Group>(assignedGroups);
		while(!done) {
			List<Group> groupsToAdd = new ArrayList<Group>();
			for(Group group : groupsToProcess) {
				//FIXME Do not get subgroups of the members group
				if (!group.getName().equals(VosManager.MEMBERS_GROUP)) {
					groupsToAdd.addAll(this.getSubGroups(sess, group));
				}
			}
			groupsToAdd.removeAll(assignedGroups);
			assignedGroups.addAll(groupsToAdd);
			groupsToProcess = groupsToAdd;
			done = groupsToProcess.isEmpty();
		}
		return assignedGroups;
	}

	public List<Group> getAllGroups(PerunSession sess, Vo vo) throws InternalErrorException {
		List<Group> groups = getGroupsManagerImpl().getAllGroups(sess, vo);

		// Sort
		Collections.sort(groups);

		return groups;
	}

	public Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo) throws InternalErrorException {
		Map<Group,Object> groupHierarchy = new TreeMap<Group, Object>();

		// Get the top level group = members
		try {
			groupHierarchy.put(this.getGroupByName(sess, vo, VosManager.MEMBERS_GROUP), null);
		} catch (GroupNotExistsException e) {
			throw new ConsistencyErrorException("Built-in members group must exists.",e);
		}

		// Call recursively getGroupsForHierarchy, which finds all subgroups
		return getGroupsForHierarchy(sess, groupHierarchy);
	}

	/**
	 *
	 * @param sess
	 * @param groups initialized HashMap containing pair <topLevelGropu, null>
	 * @return HashMap containing all VO groups hierarchically organized
	 */
	private Map<Group, Object> getGroupsForHierarchy(PerunSession sess, Map<Group, Object> groups) throws InternalErrorException {
		for (Group group: groups.keySet()) {
			List<Group> subGroups = this.getSubGroups(sess, group);

			Map<Group,Object> subGroupHierarchy = new TreeMap<Group, Object>();
			for (Group subGroup: subGroups) {
				subGroupHierarchy.put(subGroup, null);
			}

			groups.put(group, this.getGroupsForHierarchy(sess, subGroupHierarchy));
		}

		return groups;
	}

	public List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException {
		List<Group> subGroups = getGroupsManagerImpl().getSubGroups(sess, parentGroup);

		// Sort
		Collections.sort(subGroups);

		return subGroups;
	}

	public List<Group> getAllSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException {
		Queue<Group> groupsInQueue = new ConcurrentLinkedQueue<Group>();
		groupsInQueue.addAll(getGroupsManagerImpl().getSubGroups(sess, parentGroup));
		List<Group> allSubGroups = new ArrayList<Group>();
		while(groupsInQueue.peek() != null) {
			groupsInQueue.addAll(getGroupsManagerImpl().getSubGroups(sess, groupsInQueue.peek()));
			allSubGroups.add(groupsInQueue.poll());
		}
		return allSubGroups;
	}

	public Group getParentGroup(PerunSession sess, Group group) throws InternalErrorException, ParentGroupNotExistsException {
		if(group.getParentGroupId() == null) {
			Vo vo = this.getVo(sess, group);
			try {
				return this.getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
			} catch (GroupNotExistsException ex) {
				throw new ParentGroupNotExistsException("Members group not exist for vo" + vo);
			}
		} else {
			return getGroupsManagerImpl().getParentGroup(sess, group);
		}
	}

	public List<Group> getGroups(PerunSession sess, Vo vo) throws InternalErrorException {
		List<Group> groups = getGroupsManagerImpl().getGroups(sess, vo);

		Collections.sort(groups);

		return groups;
	}

	public List<Group> getGroupsByIds(PerunSession sess, List<Integer> groupsIds) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupsByIds(sess, groupsIds);
	}

	public int getGroupsCount(PerunSession sess, Vo vo) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupsCount(sess, vo);
	}

	public int getGroupsCount(PerunSession sess) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupsCount(sess);
	}

	public int getSubGroupsCount(PerunSession sess, Group parentGroup) throws InternalErrorException {
		return getGroupsManagerImpl().getSubGroupsCount(sess, parentGroup);
	}

	public Vo getVo(PerunSession sess, Group group) throws InternalErrorException {
		int voId = getGroupsManagerImpl().getVoId(sess, group);
		try {
			return getPerunBl().getVosManagerBl().getVoById(sess, voId);
		} catch (VoNotExistsException e) {
			throw new ConsistencyErrorException("Group belongs to the non-existent VO", e);
		}
	}

	public List<Group> getMemberGroups(PerunSession sess, Member member) throws InternalErrorException {
		List<Group> groups = this.getAllMemberGroups(sess, member);
		//Remove members group
		if(!groups.isEmpty()) {
			Iterator<Group> iterator = groups.iterator();
			while(iterator.hasNext()) {
				Group g = iterator.next();
				if(g.getName().equals(VosManager.MEMBERS_GROUP)) iterator.remove();
			}
		}
		// Sort
		Collections.sort(groups);
		return groups;
	}

	public List<Group> getMemberDirectGroups(PerunSession sess, Member member) throws InternalErrorException {
		List<Group> memberGroups = this.getMemberGroups(sess, member);

		Iterator<Group> groupIterator = memberGroups.iterator();
		while(groupIterator.hasNext()) {
			if(!getGroupsManagerImpl().isDirectGroupMember(sess, groupIterator.next(), member)) {
				groupIterator.remove();
			}
		}

		return memberGroups;
	}

	public List<Group> getMemberGroupsByAttribute(PerunSession sess, Member member, Attribute attribute) throws WrongAttributeAssignmentException,InternalErrorException {
		List<Group> memberGroups = this.getAllMemberGroups(sess, member);
		memberGroups.retainAll(this.getGroupsByAttribute(sess, attribute));
		return memberGroups;
	}
		
	public List<Group> getAllMemberGroups(PerunSession sess, Member member) throws InternalErrorException {
		return getGroupsManagerImpl().getAllMemberGroups(sess, member);
	}

	public List<Member> getParentGroupMembers(PerunSession sess, Group group) throws InternalErrorException {
		try {
			Group parentGroup = getParentGroup(sess, group);
			return getGroupMembers(sess, parentGroup);
		} catch(ParentGroupNotExistsException ex) {
			//group (from param) is top level group. Return VO members instead.
			Vo vo = getVo(sess, group);
			return getPerunBl().getMembersManagerBl().getMembers(sess, vo);
		}
	}

	public List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> members = this.getParentGroupMembers(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	public List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException {
		List<RichMember> richMembers = this.getParentGroupRichMembers(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	public boolean isUserMemberOfGroup(PerunSession sess, User user, Group group) throws InternalErrorException {
		return groupsManagerImpl.isUserMemberOfGroup(sess, user, group);
	}

	/**
	 * Compare richMember userExtSources with Candidate's userExtSources, if some of the useExtSource fits.
	 *
	 * @param richMember
	 * @param candidate
	 * @return true if richMember.userExtSources contains some of the candidate.useExtSource
	 */
	protected boolean hasCandidateExistingMember(Candidate candidate, RichMember richMember) {
		for (UserExtSource ues: richMember.getUserExtSources()) {
			if (candidate.getUserExtSources().contains(ues)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method run in separate transaction.
	 */
	public List<String> synchronizeGroup(PerunSession sess, Group group) throws InternalErrorException, MemberAlreadyRemovedException, AttributeNotExistsException, WrongAttributeAssignmentException, ExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//needed variables for whole method
		List<String> skippedMembers = new ArrayList<>();
		ExtSource source = null;
		ExtSource membersSource = null;

		try {
			log.info("Group synchronization {}: started.", group);

			//Initialization of group extSource
			source = getGroupExtSourceForSynchronization(sess, group);

			//Initialization of groupMembers extSource (if it is set), in other case set membersSource = source
			membersSource = getGroupMembersExtSourceForSynchronization(sess, group, source);

			//Prepare info about userAttributes which need to be overwrite (not just updated)
			List<String> overwriteUserAttributesList = getOverwriteUserAttributesListFromExtSource(membersSource);

			//Get info about type of synchronization (with or without update)
			boolean lightweightSynchronization = isThisLightweightSynchronization(sess, group);

			log.info("Group synchronization {}: using configuration extSource for membership {}, extSource for members {}", new Object[] {group, membersSource, membersSource.getName()});

			//Prepare containers for work with group members
			List<Candidate> candidatesToAdd = new ArrayList<>();
			Map<Candidate, RichMember> membersToUpdate = new HashMap<>();
			List<RichMember> membersToRemove = new ArrayList<>();

			//get all actual members of group
			List<RichMember> actualGroupMembers = getPerunBl().getGroupsManagerBl().getGroupRichMembers(sess, group);

			if(lightweightSynchronization) {
				//Do not care about updating users, just create new one and remove former members (membership is important)
				categorizeMembersForLightweightSynchronization(sess, group, source, membersSource, actualGroupMembers, candidatesToAdd, membersToRemove, skippedMembers);
			} else {
				//Also care about updating attributes of members
				categorizeMembersForSynchronization(sess, group, source, membersSource, actualGroupMembers, candidatesToAdd, membersToRemove, membersToUpdate, skippedMembers);
			}

			//Update members already presented in group
			updateExistingMembersWhileSynchronization(sess, group, membersToUpdate, overwriteUserAttributesList);

			//Add not presented candidates to group
			addMissingMembersWhileSynchronization(sess, group, candidatesToAdd, overwriteUserAttributesList, skippedMembers);

			//Remove presented members in group who are not presented in synchronized ExtSource
			removeFormerMembersWhileSynchronization(sess, group, membersToRemove);
			
			log.info("Group synchronization {}: ended.", group);
		} catch (ExtSourceUnsupportedOperationException ex) {
			throw new InternalErrorException("ExtSource do not support specific operation.", ex);
		} finally {
			closeExtSourcesAfterSynchronization(membersSource, source);
		}

		return skippedMembers;
	}

	/**
	 * Force group synchronization.
	 *
	 * Adds the group synchronization process in the groupSynchronizerThreads.
	 *
	 * @param group
	 */
	public void forceGroupSynchronization(PerunSession sess, Group group) throws GroupSynchronizationAlreadyRunningException {
		// First check if the group is not currently in synchronization process
		if (groupSynchronizerThreads.containsKey(group.getId()) && groupSynchronizerThreads.get(group.getId()).getState() != Thread.State.TERMINATED) {
			throw new GroupSynchronizationAlreadyRunningException(group);
		} else {
			// Remove from groupSynchronizerThreads if the thread was terminated
			if (groupSynchronizerThreads.containsKey(group.getId())) {
				groupSynchronizerThreads.remove(group.getId());
			}
			// Start and run the new thread
			GroupSynchronizerThread thread = new GroupSynchronizerThread(sess, group);
			thread.start();
			log.info("Group synchronization thread started for group {}.", group);

			groupSynchronizerThreads.put(group.getId(), thread);
		}
	}

	/**
	 * Synchronize all groups which have enabled synchronization. This method is run by the scheduler every 5 minutes.
	 *
	 * @throws InternalErrorException
	 */
	public void synchronizeGroups(PerunSession sess) throws InternalErrorException {
		Random rand = new Random();

		// Firstly remove all terminated threads
		List<Integer> threadsToRemove = new ArrayList<Integer>();
		for (Integer groupId: groupSynchronizerThreads.keySet()) {
			if (groupSynchronizerThreads.get(groupId).getState() == Thread.State.TERMINATED) {
				threadsToRemove.add(groupId);
			}
		}
		for (Integer groupId: threadsToRemove) {
			groupSynchronizerThreads.remove(groupId);
			log.debug("Removing terminated group synchronization thread for group id={}", groupId);
		}

		// Get the default synchronization interval and synchronization timeout from the configuration file
		int intervalMultiplier = Integer.parseInt(BeansUtils.getPropertyFromConfiguration("perun.group.synchronization.interval"));
		int timeout = Integer.parseInt(BeansUtils.getPropertyFromConfiguration("perun.group.synchronization.timeout"));

		// Get the number of seconds from the epoch, so we can divide it by the synchronization interval value
		long minutesFromEpoch = System.currentTimeMillis()/1000/60;

		// Get the groups with synchronization enabled
		List<Group> groups = groupsManagerImpl.getGroupsToSynchronize(sess);

		int numberOfNewSynchronizations = 0;
		int numberOfActiveSynchronizations = 0;
		int numberOfTerminatedSynchronizations = 0;
		for (Group group: groups) {
			// Get the synchronization interval
			try {
				Attribute intervalAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME);
				if (intervalAttribute.getValue() != null) {
					intervalMultiplier = Integer.parseInt((String) intervalAttribute.getValue());
				} else {
					log.warn("Group {} hasn't set synchronization interval, using default {} seconds", group, intervalMultiplier);
				}
			} catch (AttributeNotExistsException e) {
				throw new ConsistencyErrorException("Required attribute " + GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME + " isn't defined in Perun!",e);
			} catch (WrongAttributeAssignmentException e) {
				log.error("Cannot synchronize group " + group +" due to exception:", e);
				continue;
			}

			// Multiply with 5 to get real minutes
			intervalMultiplier = intervalMultiplier*5;

			// If the minutesFromEpoch can be divided by the intervalMultiplier, then synchronize
			if ((minutesFromEpoch % intervalMultiplier) == 0) {
				// It's time to synchronize
				log.info("Scheduling synchronization for the group {}. Interval {} minutes.", group, intervalMultiplier);

				// Run each synchronization in separate thread, but do not start new one, if previous hasn't finished yet
				if (groupSynchronizerThreads.containsKey(group)) {
					// Check the running time of the thread
					long timeDiff = System.currentTimeMillis() - groupSynchronizerThreads.get(group).getStartTime();

					// If the time is greater than timeout set in the configuration file (in minutes)
					if (timeDiff/1000/60 > timeout) {
						// Timeout reach, stop the thread
						log.warn("Timeout {} minutes of the synchronization thread for the group {} reached.", timeout, group);
						groupSynchronizerThreads.get(group).interrupt();
						groupSynchronizerThreads.remove(group);
						numberOfTerminatedSynchronizations++;
					} else {
						numberOfActiveSynchronizations++;
					}
				} else {
					// Start and run the new thread
					try {
						// Do not overload externalSource, run each synchronization in 0-30s steps
						Thread.sleep(rand.nextInt(30000));
					} catch (InterruptedException e) {
						// Do nothing
					}
					GroupSynchronizerThread thread = new GroupSynchronizerThread(sess, group);
					thread.start();
					log.info("Group synchronization thread started for group {}.", group);

					groupSynchronizerThreads.put(group.getId(), thread);
					numberOfNewSynchronizations++;
				}
			}
		}

		if (groups.size() > 0) {
			log.info("Synchronizing {} groups, active {}, new {}, terminated {}.",
					new Object[] {groups.size(), numberOfActiveSynchronizations, numberOfNewSynchronizations, numberOfTerminatedSynchronizations});
		}
	}

	private static class GroupSynchronizerThread extends Thread {

		// all synchronization runs under synchronizer identity.
		final PerunPrincipal pp = new PerunPrincipal("perunSynchronizer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		private PerunBl perunBl;
		private PerunSession sess;
		private Group group;
		private long startTime;

		public GroupSynchronizerThread(PerunSession sess, Group group) {
			// take only reference to perun
			this.perunBl = (PerunBl) sess.getPerun();
			this.group = group;
			try {
				// create own session
				this.sess = perunBl.getPerunSession(pp);
			} catch (InternalErrorException ex) {
				log.error("Unable to create internal session for Synchronizer with credentials {} because of exception {}", pp, ex);
			}
		}

		public void run() {
			//text of exception if was thrown, null in exceptionMessage means "no exception, it's ok"
			String exceptionMessage = null;
			//text with all skipped members and reasons of this skipping
			String skippedMembersMessage = null;
			//if exception which produce fail of whole synchronization was thrown
			boolean failedDueToException = false;
			
			try {
				log.debug("Synchronization thread for group {} has started.", group);
				// Set the start time, so we can check the timeout of the thread
				startTime = System.currentTimeMillis();

				//synchronize Group and get information about skipped Members
				List<String> skippedMembers = perunBl.getGroupsManagerBl().synchronizeGroup(sess, group);

				//prepare variables for checking max length of message and create human readable text
				boolean exceedMaxChars = false;
				int maxChars = 3000;
				if(!skippedMembers.isEmpty()) {
					skippedMembersMessage = "These members from extSource were skipped: { ";
					//Exception message can't be longer than 3000 chars
					for(String skippedMember: skippedMembers) {
						if(skippedMember == null) continue;
						if(!exceedMaxChars && (skippedMembersMessage.length() + skippedMember.length()) > maxChars) {
							exceptionMessage = skippedMembersMessage + " ... message is too long, other info is in perun log file. If needed, please ask perun administrators.";
							exceedMaxChars = true;
						}
						skippedMembersMessage+= skippedMember + ", ";
					}
					skippedMembersMessage+= " }";
					if(!exceedMaxChars) exceptionMessage = skippedMembersMessage;
					log.info("Info about exception from synchronization: " + skippedMembersMessage);
				}

				log.debug("Synchronization thread for group {} has finished in {} ms.", group, System.currentTimeMillis()-startTime);
			} catch (WrongAttributeValueException | WrongReferenceAttributeValueException | InternalErrorException | WrongAttributeAssignmentException | MemberAlreadyRemovedException | AttributeNotExistsException | ExtSourceNotExistsException e) {
				failedDueToException = true;
				exceptionMessage = "Cannot synchronize group ";
				log.error(exceptionMessage + group, e);
				exceptionMessage+= "due to exception: " + e.getName() + " => " + e.getMessage();
			} catch (Exception e) {
				//If some other exception has been thrown, log it and throw again
				failedDueToException = true;
				exceptionMessage = "Cannot synchronize group ";
				log.error(exceptionMessage + group, e);
				exceptionMessage+= "due to unexpected exception: " + e.getClass().getName() + " => " + e.getMessage();
				throw e;
			} finally {
				//Save information about group synchronization, this method run in new transaction
				try {
					perunBl.getGroupsManagerBl().saveInformationAboutGroupSynchronization(sess, group, failedDueToException, exceptionMessage);
				} catch (Exception ex) {
					log.error("When synchronization group " + group + ", exception was thrown.", ex);
					log.info("Info about exception from synchronization: " + skippedMembersMessage);
				}
				log.debug("GroupSynchronizerThread finished for group: {}", group);
			}
		}

		public long getStartTime() {
			return startTime;
		}
	}

	/**
	 * Get all groups of member (except members group) where authoritativeGroup attribute is set to 1 (true)
	 *
	 * @param sess
	 * @param member
	 * @return list of groups with authoritativeAttribute set to 1
	 *
	 * @throws AttributeNotExistsException if authoritativeGroup attribute not exists
	 * @throws InternalErrorException
	 */
	List<Group> getAllAuthoritativeGroupsOfMember(PerunSession sess, Member member) throws AttributeNotExistsException, InternalErrorException {
		//Get all member groups except membersGroup
		List<Group> memberGroups = this.getMemberGroups(sess, member);
		Iterator<Group> groupsIter = memberGroups.iterator();
		//Iterate through all groups and remove those which have not authoritativeGroup attribute set to 1
		while(groupsIter.hasNext()) {
			Group group = groupsIter.next();
			try {
				boolean isThisGroupAuthoritative = false;
				Attribute authoritativeGroup = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_D_AUTHORITATIVE_GROUP);
				if(authoritativeGroup.getValue() != null) {
					Integer attrValue = (Integer) authoritativeGroup.getValue();
					if(attrValue == 1) isThisGroupAuthoritative = true;
				}
				//If group is not authoritative group, remove it from list of memberAuthoritativeGroups
				if(!isThisGroupAuthoritative) groupsIter.remove();
			} catch(WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}
		}
		
		return memberGroups;
	}

	/**
	 * Gets the groupsManagerImpl for this instance.
	 *
	 * @return The groupsManagerImpl.
	 */
	public GroupsManagerImplApi getGroupsManagerImpl() {
		return this.groupsManagerImpl;
	}

	/**
	 * Gets the perunBl.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public List<Group> getGroupsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR);
		if(!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
		return getGroupsManagerImpl().getGroupsByAttribute(sess, attribute);
	}

	public List<Pair<Group, Resource>> getGroupResourcePairsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		if(!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
		return getGroupsManagerImpl().getGroupResourcePairsByAttribute(sess, attribute);
	}

	public boolean isGroupMember(PerunSession sess, Group group, Member member) throws InternalErrorException {
		return getGroupsManagerImpl().isGroupMember(sess, group, member);
	}

	public void checkGroupExists(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException {
		getGroupsManagerImpl().checkGroupExists(sess, group);
	}

	public List<Group> getGroupsByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException {
		List<Group> groups = new ArrayList<Group>();

		//All possible useful objects
		Vo vo = null;
		Facility facility = null;
		Group group = null;
		Member member = null;
		User user = null;
		Host host = null;
		Resource resource = null;

		if(perunBean != null) {
			if(perunBean instanceof Vo) vo = (Vo) perunBean;
			else if(perunBean instanceof Facility) facility = (Facility) perunBean;
			else if(perunBean instanceof Group) group = (Group) perunBean;
			else if(perunBean instanceof Member) member = (Member) perunBean;
			else if(perunBean instanceof User) user = (User) perunBean;
			else if(perunBean instanceof Host) host = (Host) perunBean;
			else if(perunBean instanceof Resource) resource = (Resource) perunBean;
			else {
				throw new InternalErrorException("There is unrecognized object in primaryHolder of aidingAttr.");
			}
		} else {
			throw new InternalErrorException("Aiding attribute must have primaryHolder which is not null.");
		}

		//Important For Groups not work with Subgroups! Invalid members are executed too.

		if(group != null) {
			groups.add(group);
		} else if(member != null) {
			groups.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member));
		} else if(resource != null) {
			groups.addAll(getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource));
		} else if(user != null) {
			List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
			for(Member memberElement: members) {
				groups.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, memberElement));
			}
		} else if(host != null) {
			facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			for(Resource resourceElement: resourcesFromFacility) {
				groups.addAll(getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resourceElement));
			}
		} else if(facility != null) {
			List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
			for(Resource resourceElement: resourcesFromFacility) {
				groups.addAll(getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resourceElement));
			}
		} else if(vo != null) {
			groups.addAll(getPerunBl().getGroupsManagerBl().getAllGroups(sess, vo));
		}

		groups = new ArrayList<Group>(new HashSet<Group>(groups));
		return groups;
	}

	public List<Member> filterMembersByMembershipTypeInGroup(List<Member> members) throws InternalErrorException {
		List<Member> filteredMembers = new ArrayList<Member>();
		Iterator<Member> membersIterator = members.iterator();

		//Add members with direct membership type
		while(membersIterator.hasNext()) {
			Member m = membersIterator.next();
			if(m.getMembershipType().equals(MembershipType.DIRECT)) {
				filteredMembers.add(m);
				membersIterator.remove();
			}
		}

		//Add not containing members with indirect membership type
		for(Member m: members) {
			if(!filteredMembers.contains(m)) filteredMembers.add(m);
		}

		return filteredMembers;
	}

	public RichGroup filterOnlyAllowedAttributes(PerunSession sess, RichGroup richGroup) throws InternalErrorException {
		if(richGroup == null) throw new InternalErrorException("RichGroup can't be null.");

		//Filtering richGroup attributes
		if(richGroup.getAttributes() != null) {
			List<Attribute> groupAttributes = richGroup.getAttributes();
			List<Attribute> allowedGroupAttributes = new ArrayList<Attribute>();
			for(Attribute groupAttr : groupAttributes) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, groupAttr, richGroup, null)) {
					groupAttr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttr, richGroup, null));
					allowedGroupAttributes.add(groupAttr);
				}
			}

			richGroup.setAttributes(allowedGroupAttributes);
		}
		return richGroup;
	}

	public List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups) throws InternalErrorException {
		List<RichGroup> filteredRichGroups = new ArrayList<RichGroup>();
		if(richGroups == null || richGroups.isEmpty()) return filteredRichGroups;

		for(RichGroup rg : richGroups) {
			filteredRichGroups.add(this.filterOnlyAllowedAttributes(sess, rg));
		}

		return filteredRichGroups;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public RichGroup convertGroupToRichGroupWithAttributes(PerunSession sess, Group group) throws InternalErrorException{
		return new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group));
	}

	public RichGroup convertGroupToRichGroupWithAttributesByName(PerunSession sess, Group group, List<String> attrNames) throws InternalErrorException{
		if (attrNames == null) return convertGroupToRichGroupWithAttributes(sess, group);
		return new RichGroup(group,this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group, attrNames));
	}

	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, List<Group> groups) throws InternalErrorException {
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group)));
		}
		return richGroups;
	}

	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, List<Group> groups, List<String> attrNames) throws InternalErrorException {
		if (attrNames == null) return convertGroupsToRichGroupsWithAttributes(sess, groups);
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group, attrNames)));
		}
		return richGroups;
	}

	public List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames)throws InternalErrorException{
		return convertGroupsToRichGroupsWithAttributes(sess, this.getAllGroups(sess, vo), attrNames);
	}
	
	public List<RichGroup> getRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames)throws InternalErrorException{
		return convertGroupsToRichGroupsWithAttributes(sess, this.getSubGroups(sess, parentGroup), attrNames);
	}

	public RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames)throws InternalErrorException, GroupNotExistsException{
		return convertGroupToRichGroupWithAttributesByName(sess, this.getGroupById(sess, groupId), attrNames);
	}

	public void saveInformationAboutGroupSynchronization(PerunSession sess, Group group, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		//get current timestamp of this synchronization
		Date currentTimestamp = new Date();
		//If session is null, throw an exception
		if (sess == null) {
			throw new InternalErrorException("Session is null when trying to save information about synchronization. Group: " + group + ", timestamp: " + currentTimestamp + ",message: " + exceptionMessage);
		}

		//If group is null, throw an exception
		if (group == null) {
			throw new InternalErrorException("Object group is null when trying to save information about synchronization. Timestamp: " + currentTimestamp + ", message: " + exceptionMessage);
		}

		//if exceptionMessage is empty, use "Empty message" instead
		if (exceptionMessage != null && exceptionMessage.isEmpty()) {
			exceptionMessage = "Empty message.";
		}

		//Set correct format of currentTimestamp
		String correctTimestampString = BeansUtils.getDateFormatter().format(currentTimestamp);

		//Get both attribute definition lastSynchroTimestamp and lastSynchroState
		//Get definitions and values, set values
		Attribute lastSynchronizationTimestamp = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lastSynchronizationTimestamp"));
		Attribute lastSynchronizationState = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lastSynchronizationState"));
		lastSynchronizationTimestamp.setValue(correctTimestampString);
		//if exception is null, set null to value => remove attribute instead of setting in method setAttributes
		lastSynchronizationState.setValue(exceptionMessage);

		//attributes to set
		List<Attribute> attrsToSet = new ArrayList<>();

		//null in exceptionMessage means no exception, success
		//Set lastSuccessSynchronizationTimestamp if this one is success
		if(exceptionMessage == null) {
			String attrName = AttributesManager.NS_GROUP_ATTR_DEF + ":lastSuccessSynchronizationTimestamp";
			try {
				Attribute lastSuccessSynchronizationTimestamp = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, attrName));
				lastSuccessSynchronizationTimestamp.setValue(correctTimestampString);
				attrsToSet.add(lastSuccessSynchronizationTimestamp);
			} catch (AttributeNotExistsException ex) {
				log.error("Can't save lastSuccessSynchronizationTimestamp, because there is missing attribute with name {}",attrName);
			}
		} else {
			//Log to auditer_log that synchronization failed or finished with some errors
			if(failedDueToException) {
				getPerunBl().getAuditer().log(sess, "{} synchronization failed because of {}.", group, exceptionMessage);
			} else {
				getPerunBl().getAuditer().log(sess, "{} synchronization finished with errors: {}.", group, exceptionMessage);
			}
		}

		//set lastSynchronizationState and lastSynchronizationTimestamp
		attrsToSet.add(lastSynchronizationState);
		attrsToSet.add(lastSynchronizationTimestamp);
		((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttributes(sess, group, attrsToSet);
	}

	@Override
	public List<Group> getGroupsWithAssignedExtSourceInVo(PerunSession sess, ExtSource source, Vo vo) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupsWithAssignedExtSourceInVo(sess, source, vo);
	}

	//----------- PRIVATE METHODS FOR  GROUP SYNCHRONIZATION -----------

	/**
	 * For lightweight synchronization prepare candidates to add and members to remove.
	 *
	 * Get all subjects from loginSource and try to find users in Perun by their login and this ExtSource.
	 * If found, look if this user is already in synchronized Group. If yes skip him, if not add him to candidateToAdd
	 * If not found, add him to candidatesToAdd (from source itself or from memberSource if they are different)
	 *
	 * Rest of former members need to be add to membersToRemove to remove them from group.
	 *
	 * This method fill 2 member structures which get as parameters:
	 * 1. candidateToAdd - New members of the group
	 * 2. membersToRemove - Former members who are not in synchronized ExtSource now
	 *
	 * @param sess
	 * @param group  to be synchronized
	 * @param loginSource extSource for getting logins
	 * @param memberSource extSource for getting members (can be same if there is just one extSource for both)
	 * @param groupMembers actual members of group before synchronization
	 * @param candidatesToAdd 1. container (more above)
	 * @param membersToRemove 2. container (more above)
	 * @param skippedMembers list of all skipped members
	 * 
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	private void categorizeMembersForLightweightSynchronization(PerunSession sess, Group group, ExtSource loginSource, ExtSource memberSource, List<RichMember> groupMembers, List<Candidate> candidatesToAdd, List<RichMember> membersToRemove, List<String> skippedMembers) throws InternalErrorException, ExtSourceNotExistsException, ExtSourceUnsupportedOperationException {
		//Get subjects from loginSource
		List<Map<String, String>> subjects;
		subjects = getSubjectsFromExtSource(sess, loginSource, group, null);

		//Prepare structure of userIds with richMembers to better work with actual members
		Map<Integer, RichMember> idsOfUsersInGroup = new HashMap<>();
		for(RichMember richMember: groupMembers) {
			idsOfUsersInGroup.put(richMember.getUserId(), richMember);
		}

		//try to find users by login and loginSource
		List<String> loginsToAdd = new ArrayList<>();
		for(Map<String, String> subjectFromLoginSource : subjects) {
			String login = subjectFromLoginSource.get("login");
			// Skip subjects, which doesn't have login
			if (login == null || login.isEmpty()) {
				log.debug("Subject {} doesn't contain attribute login, skipping.", subjectFromLoginSource);
				skippedMembers.add("MemberEntry:[" + subjectFromLoginSource + "] was skipped because login is missing");
				continue;
			}

			//try to find user from perun by login and member extSource (need to use memberSource because loginSource is not saved by synchronization)
			User user = null;
			Candidate candidate = null;
			try {
				UserExtSource userExtSource = getPerunBl().getUsersManagerBl().getUserExtSourceByExtLogin(sess, memberSource, login);
				user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);
				if(!idsOfUsersInGroup.containsKey(user.getId())) {
					candidate = new Candidate(user, userExtSource);
					candidatesToAdd.add(candidate);
				} else {
					idsOfUsersInGroup.remove(user.getId());
				}
			//If not found,
			} catch (UserExtSourceNotExistsException | UserNotExistsException ex) {
				loginsToAdd.add(login);
			}
		}

		//If possible get subjects from ExtSource by bulk, if not, get them one by one
		try {
			subjects = getSubjectsFromExtSource(sess, memberSource, group, loginsToAdd);
			candidatesToAdd.addAll(convertSubjectsToCandidates(sess, subjects, memberSource, skippedMembers, false));
		} catch (ExtSourceUnsupportedOperationException ex) {
			for(String login: loginsToAdd) {
				Map<String, String> subjectByLogin = new HashMap<>();
				subjectByLogin.put("login", login);
				List<Map<String, String>> subjectToConvert = Arrays.asList(subjectByLogin);
				candidatesToAdd.addAll(convertSubjectsToCandidates(sess, subjectToConvert, memberSource, skippedMembers, false));
			}
		}

		//Rest of them need to be removed
		membersToRemove.addAll(idsOfUsersInGroup.values());
	}

	/**
	 * For normal synchronization prepare candidates to add, members to remove and members for update.
	 *
	 * Get all subjects by loginSource and try to convert them to Candidates. It can be done
	 * from the list of subjects itself (if there are all attributes) or by logins one
	 * by one (or by bulks) from membersSource.
	 *
	 * This method fill 3 member structures which get as parameters:
	 * 1. candidateToAdd - New members of the group
	 * 2. membersToRemove - Former members who are not in synchronized ExtSource now
	 * 3. membersToUpdate - Candidates with equivalent Members from Perun for purpose of updating attributes and statuses
	 *
	 * @param sess
	 * @param group  to be synchronized
	 * @param loginSource extSource for getting logins
	 * @param memberSource extSource for getting members (can be same if there is just one extSource for both)
	 * @param groupMembers actual members of group before synchronization
	 * @param candidatesToAdd 1. container (more above)
	 * @param membersToRemove 2. container (more above)
	 * @param membersToUpdate 3. container (more above)
	 * @param skippedMembers list of all skipped members
	 * 
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	private void categorizeMembersForSynchronization(PerunSession sess, Group group, ExtSource loginSource, ExtSource membersSource, List<RichMember> groupMembers, List<Candidate> candidatesToAdd, List<RichMember> membersToRemove, Map<Candidate, RichMember> membersToUpdate, List<String> skippedMembers) throws InternalErrorException, ExtSourceNotExistsException, ExtSourceUnsupportedOperationException {
		//Get subjects from login extSource
		List<Map<String, String>> subjectsFromLoginSource = getSubjectsFromExtSource(sess, loginSource, group, null);
		//Convert subjects to candidates
		List<Candidate> candidates;

		//Choose the way converting subjects to candidates (get from loginSource itself, get by login again from memberSource or get by list of logins from membersSource)
		if(!loginSource.equals(membersSource)) {
			//get all logins from map
			List<String> logins = new ArrayList<>();
			for(Map<String, String> subject: subjectsFromLoginSource) {
				if(subject.containsKey("login")) logins.add(subject.get("login"));
			}
			try {
				List<Map<String, String>> subjectsFromMemberSource = getSubjectsFromExtSource(sess, membersSource, group, logins);
				candidates = convertSubjectsToCandidates(sess, subjectsFromMemberSource, membersSource, skippedMembers, false);
			} catch (ExtSourceUnsupportedOperationException ex) {
				//do not support getting subject by list of logins, so use the old way
				candidates = convertSubjectsToCandidates(sess, subjectsFromLoginSource, membersSource, skippedMembers, true);
			}

		} else if (membersSource instanceof ExtSourceApi) {
			//They are the same and extSourceApi is
			candidates = convertSubjectsToCandidates(sess, subjectsFromLoginSource, membersSource, skippedMembers, false);
		} else if (membersSource instanceof ExtSourceSimpleApi) {
			candidates = convertSubjectsToCandidates(sess, subjectsFromLoginSource, membersSource, skippedMembers, true);
		} else {
			// this should not happen without change in extSource API code
			throw new InternalErrorException("ExtSource is other instance than SimpleApi or Api and this is not supported!");
		}

		candidatesToAdd.addAll(candidates);
		membersToRemove.addAll(groupMembers);
		//mapping structure for more efficient searching
		Map<UserExtSource, RichMember> mappingStructure = new HashMap<>();
		for(RichMember rm: groupMembers) {
			for(UserExtSource ues: rm.getUserExtSources()) {
				mappingStructure.put(ues, rm);
			}
		}

		//try to find already existing candidates between members in group
		for(Candidate candidate: candidates) {
			List<UserExtSource> candidateExtSources = candidate.getUserExtSources();
			for(UserExtSource key: candidateExtSources) {
				//candidate exists, will be updated
				if(mappingStructure.containsKey(key)) {
					membersToUpdate.put(candidate, mappingStructure.get(key));
					candidatesToAdd.remove(candidate);
					membersToRemove.remove(mappingStructure.get(key));
					break;
				}
			}
		}
	}

	/**
	 * Get ExtSource by name from attribute group:groupMembersExtSource.
	 * Attribute can be null so if is not set, use default source.
	 *
	 * @param sess
	 * @param group to be synchronized
	 * @param defaultSource we need to have already default group source (for synchronization)
	 *
	 * @return if exists, return membersExtSource, if not, return default group extSource
	 *
	 * @throws InternalErrorException if some internal error happens
	 * @throws WrongAttributeAssignmentException if bad assignment of groupMembersExtSource attribute
	 * @throws AttributeNotExistsException if groupMembersExtSource attribute not exists in perun Database
	 * @throws ExtSourceNotExistsException if extSource set in Group attribute not exists
	 */
	private ExtSource getGroupMembersExtSourceForSynchronization(PerunSession sess, Group group, ExtSource defaultSource) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException, ExtSourceNotExistsException {
		//Prepare the groupMembersExtSource if it is set
		Attribute membersExtSourceNameAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPMEMBERSEXTSOURCE_ATTRNAME);
		ExtSource membersSource = null;
		// If the different extSource name for the members was set use it
		if (membersExtSourceNameAttr != null && membersExtSourceNameAttr.getValue() != null) {
			String membersExtSourceName = (String) membersExtSourceNameAttr.getValue();
			membersSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, membersExtSourceName);
			return membersSource;
		//Otherwise use use the group one
		} else {
			return defaultSource;
		}
	}

	/**
	 * Get ExtSource by name from attribute group:groupExtSource
	 *
	 * @param sess
	 * @param group to be synchronized
	 *
	 * @return default group extSource for synchronization
	 *
	 * @throws InternalErrorException if some internal error happens or attribute with extSource name is null
	 * @throws WrongAttributeAssignmentException if bad assignment of groupExtSource attribute
	 * @throws AttributeNotExistsException if groupExtSource attribute not exists in perun Database
	 * @throws ExtSourceNotExistsException if extSource set in Group attribute not exists
	 */
	private ExtSource getGroupExtSourceForSynchronization(PerunSession sess, Group group) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException, ExtSourceNotExistsException {
		//Get extSource name from group attribute
		Attribute extSourceNameAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
		if (extSourceNameAttr == null || extSourceNameAttr.getValue() == null) {
			throw new InternalErrorException("ExtSource is not set for group: " + group);
		}
		//return extSource by name
		return getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, ((String) extSourceNameAttr.getValue()));
	}

	/**
	 * From membersSource extSource get attribute overwriteUserAttributes and prepare
	 * list of attributes names to be overwrite for synchronized users.
	 *
	 * Attribute has value (if set) in format "name,name2,name3..."
	 * Method parse these names to list of names.
	 * Return empty array if attribute is not set for extSource or if it is empty.
	 *
	 * @param membersSource to get attributes from
	 *
	 * @return list of attribute names to be overwrite
	 *
	 * @throws InternalErrorException if something happens in getting attributes from membersSource
	 */
	private List<String> getOverwriteUserAttributesListFromExtSource(ExtSource membersSource) throws InternalErrorException {
		Map<String, String> membersSourceAttributes = getPerunBl().getExtSourcesManagerBl().getAttributes(membersSource);
		List<String> overwriteUserAttributesList = new ArrayList<>();
		String overwriteUserAttributes = membersSourceAttributes.get("overwriteUserAttributes");
		if(overwriteUserAttributes != null && !overwriteUserAttributes.isEmpty()) {
			//remove all white spaces and invisible characters
			overwriteUserAttributes = overwriteUserAttributes.replaceAll("\\s", "");
			overwriteUserAttributesList = Arrays.asList(overwriteUserAttributes.split(","));
		}
		return overwriteUserAttributesList;
	}

	/**
	 * Return true if attribute group:lightweightSynchronization is set to true.
	 * False if not.
	 *
	 * True means: we don't want to update existing members (attributes, statuses etc.), just
	 * add new members and remove former members
	 * False means: we want to do whole synchronization process including updating operations
	 *
	 * @param sess
	 * @param group to be synchronized
	 *
	 * @return true if this is lightweightSynchronization, false if not
	 *
	 * @throws InternalErrorException if something happens while getting lightweightSynchronization attribute
	 * @throws WrongAttributeAssignmentException if bad assignment of lightweightSynchronization attribute
	 * @throws AttributeNotExistsException if lightweightSynchronization attribute not exists in perun Database
	 */
	private boolean isThisLightweightSynchronization(PerunSession sess, Group group) throws InternalErrorException, WrongAttributeAssignmentException, AttributeNotExistsException {
		Attribute lightweightSynchronzationAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPLIGHTWEIGHTSYNCHRONIZATION_ATTRNAME);
		boolean lightweightSynchronization = false;
		if(lightweightSynchronzationAttr != null && lightweightSynchronzationAttr.getValue() != null) {
			lightweightSynchronization = (Boolean) lightweightSynchronzationAttr.getValue();
		}
		return lightweightSynchronization;
	}

	/**
	 * Return List of subjects, where subject is map of attribute names and attribute values.
	 * Every subject is structure for creating Candidate from ExtSource.
	 *
	 * @param sess
	 * @param source to get subjects from
	 * @param group to be synchronized
	 * @param logins if not null, use it for filtering logins from extSource
	 *
	 * @return list of subjects
	 *
	 * @throws InternalErrorException if internal error occurs
	 * @throws ExtSourceUnsupportedOperationException if extSource do not support getGroupBySubject with or without logins
	 */
	private List<Map<String, String>> getSubjectsFromExtSource(PerunSession sess, ExtSource source, Group group, List<String> logins) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		//Get all group attributes and store tham to map (info like query, time interval etc.)
		List<Attribute> groupAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, group);
		Map<String, String> groupAttributesMap = new HashMap<String, String>();
		for (Attribute attr: groupAttributes) {
			String value = BeansUtils.attributeValueToString(attr);
			String name = attr.getName();
			groupAttributesMap.put(name, value);
		}
		//-- Get Subjects in form of map where left string is name of attribute and right string is value of attribute, every subject is one map
		List<Map<String, String>> subjects;
		if(logins == null) {
			subjects = ((ExtSourceSimpleApi) source).getGroupSubjects(groupAttributesMap);
			log.debug("Group synchronization {}: get members for external group. It contains {} members.", group, subjects.size());
		} else {
			subjects = ((ExtSourceSimpleApi) source).getGroupSubjects(groupAttributesMap, logins);
			log.debug("Group synchronization {}: get members for external group by list of logins. It contains {} members.", group, subjects.size());
		}
			
		return subjects;
	}

	/**
	 * Convert all subjects to candidates.
	 *
	 * If "onlyLoginInMap" is true, it means we need to get all data from membersSource by login (one by one).
	 * If "onlyLoginInMap" is false, it means we have all data already so we can just create candidate without
	 * query to membersSource.
	 *
	 * @param sess
	 * @param subjects list of subjects or just their logins
	 * @param membersSource extSource for getting members with attributes
	 * @param skippedMembers list of skipped members
	 * @param onlyLoginsInMap true if only logins in subjects, false if all other attributes are already there
	 *
	 * @return list of converted subjects to candidates
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 */
	private List<Candidate> convertSubjectsToCandidates(PerunSession sess, List<Map<String, String>> subjects, ExtSource membersSource, List<String> skippedMembers, boolean onlyLoginsInMap) throws InternalErrorException, ExtSourceNotExistsException {
		List<Candidate> candidates = new ArrayList<>();
		for (Map<String, String> subject: subjects) {
			String login = subject.get("login");
			// Skip subjects, which doesn't have login
			if (login == null || login.isEmpty()) {
				log.debug("Subject {} doesn't contain attribute login, skipping.", subject);
				skippedMembers.add("MemberEntry:[" + subject + "] was skipped because login is missing");
				continue;
			}
			try {
				if(onlyLoginsInMap) {
					candidates.add((getPerunBl().getExtSourcesManagerBl().getCandidate(sess, membersSource, login)));
				} else {
					candidates.add((getPerunBl().getExtSourcesManagerBl().getCandidate(sess, subject, membersSource, login)));
				}
			} catch (CandidateNotExistsException e) {
				log.warn("getGroupSubjects subjects returned login {}, but it cannot be obtained using getCandidate()", login);
				skippedMembers.add("MemberEntry:[" + subject + "] was skipped because candidate can't be found by login:'" + login + "' in extSource " + membersSource);
			} catch (ExtSourceUnsupportedOperationException e) {
				log.warn("ExtSource {} doesn't support getCandidate operation.", membersSource);
				skippedMembers.add("MemberEntry:[" + subject + "] was skipped because extSource " + membersSource + " not support method getCandidate");
			} catch (ParserException e) {
				log.warn("Can't parse value {} from candidate with login {}", e.getParsedValue(), login);
				skippedMembers.add("MemberEntry:[" + subject + "] was skipped because of problem with parsing value '" + e.getParsedValue() + "'");
			}
		}

		return candidates;
	}

	/**
	 * Get Map membersToUpdate and update their attributes, extSources, expirations and statuses.
	 *
	 * For Member - updateAttributes
	 * For User - updateAttributes if exists in list of overwriteUserAttributesList,
	 *            in other case just mergeAttributes.
	 *
	 * updateAttributes = store new values
	 * mergeAttributes = for List and Map add new values, do not remove old one,
	 *                   for other cases store new values (like String, Integer etc.)
	 *
	 *
	 * @param sess
	 * @param group to be synchronized
	 * @param membersToUpdate list of members for updating in Perun by information from extSource
	 * @param overwriteUserAttributesList list of user attributes to be updated instead of merged
	 *
	 * @throws InternalErrorException if some internal error occurs
	 * @throws AttributeNotExistsException if some attributes not exists and for this reason can't be updated
	 * @throws WrongAttributeAssignmentException if some attribute is updated in bad way (bad assignment)
	 */
	private void updateExistingMembersWhileSynchronization(PerunSession sess, Group group, Map<Candidate, RichMember> membersToUpdate, List<String> overwriteUserAttributesList) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		//Iterate through all subject attributes
		for(Candidate candidate: membersToUpdate.keySet()) {
			RichMember richMember = membersToUpdate.get(candidate);

			//If member not exists in this moment (somebody remove him before start of updating), skip him and log it
			try {
				getPerunBl().getMembersManagerBl().checkMemberExists(sess, richMember);
			} catch (MemberNotExistsException ex) {
				//log it and skip this member
				log.debug("Someone removed member {} from group {} before updating process. Skip him.", richMember, group);
				continue;
			}

			for (String attributeName : candidate.getAttributes().keySet()) {
				//get RichMember with attributes
				richMember = getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, Arrays.asList(richMember)).get(0);

				//update member attribute
				if(attributeName.startsWith(AttributesManager.NS_MEMBER_ATTR)) {
					boolean attributeFound = false;
					for (Attribute memberAttribute: richMember.getMemberAttributes()) {
						if(memberAttribute.getName().equals(attributeName)) {
							attributeFound = true;
							Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), memberAttribute.getType());
							if (subjectAttributeValue != null && !memberAttribute.getValue().equals(subjectAttributeValue)) {
								log.trace("Group synchronization {}: value of the attribute {} for memberId {} changed. Original value {}, new value {}.",
										new Object[] {group, memberAttribute, richMember.getId(), memberAttribute.getValue(), subjectAttributeValue});
								memberAttribute.setValue(subjectAttributeValue);
								try {
									getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember, memberAttribute);
								} catch (AttributeValueException e) {
									// There is a problem with attribute value, so set INVALID status for the member
									getPerunBl().getMembersManagerBl().invalidateMember(sess, richMember);
								} catch	(WrongAttributeAssignmentException e) {
									throw new ConsistencyErrorException(e);
								}
							}
							//we found it, but there is no change;
							break;
						}
					}
					//member has not set this attribute so set it now if possible
					if(!attributeFound) {
						Attribute newAttribute = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName));
						Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), newAttribute.getType());
						newAttribute.setValue(subjectAttributeValue);
						try {
							// Try to set member's attributes
							getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember, newAttribute);
							log.trace("Setting the {} value {}", newAttribute, candidate.getAttributes().get(attributeName));
						} catch (AttributeValueException e) {
							// There is a problem with attribute value, so set INVALID status for the member
							getPerunBl().getMembersManagerBl().invalidateMember(sess, richMember);
						}
					}
				//update user attribute
				} else if(attributeName.startsWith(AttributesManager.NS_USER_ATTR)) {
					boolean attributeFound = false;
					for (Attribute userAttribute: richMember.getUserAttributes()) {
						if(userAttribute.getName().equals(attributeName)) {
							attributeFound = true;
							Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), userAttribute.getType());
							if (!userAttribute.getValue().equals(subjectAttributeValue)) {
								log.trace("Group synchronization {}: value of the attribute {} for memberId {} changed. Original value {}, new value {}.",
										new Object[] {group, userAttribute, richMember.getId(), userAttribute.getValue(), subjectAttributeValue});
								userAttribute.setValue(subjectAttributeValue);
								try {
									//Choose set or merge by extSource attribute overwriteUserAttributes (if contains this one)
									if(overwriteUserAttributesList.contains(userAttribute.getName())) {
										getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember.getUser(), userAttribute);
									} else {
										getPerunBl().getAttributesManagerBl().mergeAttributeValueInNestedTransaction(sess, richMember.getUser(), userAttribute);
									}
								} catch (AttributeValueException e) {
									// There is a problem with attribute value, so set INVALID status for the member
									getPerunBl().getMembersManagerBl().invalidateMember(sess, richMember);
								} catch (WrongAttributeAssignmentException e) {
									throw new ConsistencyErrorException(e);
								}
							}
							//we found it, but there is no change
							break;
						}
					}
					//user has not set this attribute so set it now if
					if(!attributeFound) {
						Attribute newAttribute = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName));
						Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), newAttribute.getType());
						newAttribute.setValue(subjectAttributeValue);
						try {
							// Try to set user's attributes
							getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember.getUser(), newAttribute);
							log.trace("Setting the {} value {}", newAttribute, candidate.getAttributes().get(attributeName));
						} catch (AttributeValueException e) {
							// There is a problem with attribute value, so set INVALID status for the member
							getPerunBl().getMembersManagerBl().invalidateMember(sess, richMember);
						}
					}
				} else {
					//we are not supporting other attributes then member or user so skip it without error, but log it
					log.error("Attribute {} can't be set, because it is not member or user attribute.", attributeName);
				}
			}

			//Synchronize userExtSources (add not existing)
			for (UserExtSource ues : candidate.getUserExtSources()) {
				if (!getPerunBl().getUsersManagerBl().userExtSourceExists(sess, ues)) {
					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, richMember.getUser(), ues);
					} catch (UserExtSourceExistsException e) {
						throw new ConsistencyErrorException("Adding already existing userExtSource " + ues, e);
					}
				}
			}

			//Set correct member Status
			// If the member has expired or disabled status, try to expire/validate him (depending on expiration date)
			if (richMember.getStatus().equals(Status.DISABLED) || richMember.getStatus().equals(Status.EXPIRED)) {
				Date now = new Date();
				Attribute membershipExpiration = getPerunBl().getAttributesManagerBl().getAttribute(sess, richMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
				if(membershipExpiration.getValue() != null) {
					try {
						Date currentMembershipExpirationDate = BeansUtils.getDateFormatterWithoutTime().parse((String) membershipExpiration.getValue());
						if (currentMembershipExpirationDate.before(now)) {
							//disabled members which are after expiration date will be expired
							if (richMember.getStatus().equals(Status.DISABLED)) {
								try {
									perunBl.getMembersManagerBl().expireMember(sess, richMember);
									log.info("Switching member id {} to EXPIRE state, due to expiration {}.", richMember.getId(), (String) membershipExpiration.getValue());
									log.debug("Switching member to EXPIRE state, additional info: membership expiration date='{}', system now date='{}'", currentMembershipExpirationDate, now);
								} catch (MemberNotValidYetException e) {
									log.error("Consistency error while trying to expire member id {}, exception {}", richMember.getId(), e);
								}
							}
						} else {
							//disabled and expired members which are before expiration date will be validated
							try {
								perunBl.getMembersManagerBl().validateMember(sess, richMember);
								log.info("Switching member id {} to VALID state, due to expiration {}.", richMember.getId(), (String) membershipExpiration.getValue());
								log.debug("Switching member to VALID state, additional info: membership expiration date='{}', system now date='{}'", currentMembershipExpirationDate, now);
							} catch (WrongAttributeValueException e) {
								log.error("Error during validating member id {}, exception {}", richMember.getId(), e);
							} catch (WrongReferenceAttributeValueException e) {
								log.error("Error during validating member id {}, exception {}", richMember.getId(), e);
							}
						}
					} catch (ParseException ex) {
						log.error("Group synchronization: memberId {} expiration String cannot be parsed, exception {}.",richMember.getId(), ex);
					}
				}
			}

			// If the member has INVALID status, try to validate the member
			try {
				if (richMember.getStatus().equals(Status.INVALID)) {
					getPerunBl().getMembersManagerBl().validateMember(sess, richMember);
				}
			} catch (WrongAttributeValueException e) {
				log.info("Member id {} will stay in INVALID state, because there was problem with attributes {}.", richMember.getId(), e);
			} catch (WrongReferenceAttributeValueException e) {
				log.info("Member id {} will stay in INVALID state, because there was problem with attributes {}.", richMember.getId(), e);
			}

			// If the member has still DISABLED status, try to validate the member
			try {
				if (richMember.getStatus().equals(Status.DISABLED)) {
					getPerunBl().getMembersManagerBl().validateMember(sess, richMember);
				}
			} catch (WrongAttributeValueException e) {
				log.info("Switching member id {} into INVALID state from DISABLED, because there was problem with attributes {}.", richMember.getId(), e);
			} catch (WrongReferenceAttributeValueException e) {
				log.info("Switching member id {} into INVALID state from DISABLED, because there was problem with attributes {}.", richMember.getId(), e);
			}
		}
	}

	/**
	 * Get list of new candidates and add them to the Group.
	 *
	 * If Candidate can't be added to Group, skip him and add this information to skippedMembers list.
	 *
	 * When creating new member from Candidate, if user already exists, merge his attributes,
	 * if attribute exists in list of overwriteUserAttributesList, update it instead of merging.
	 *
	 * @param sess
	 * @param group to be synchronized
	 * @param candidatesToAdd list of new members (candidates)
	 * @param overwriteUserAttributesList list of attributes to be updated for user if found
	 * @param skippedMembers list of not successfully synchronized members
	 *
	 * @throws InternalErrorException if some internal error occurs
	 */
	private void addMissingMembersWhileSynchronization(PerunSession sess, Group group, List<Candidate> candidatesToAdd, List<String> overwriteUserAttributesList, List<String> skippedMembers) throws InternalErrorException {
		// Now add missing members
		for (Candidate candidate: candidatesToAdd) {
			Member member = null;
			try {
				// Check if the member is already in the VO (just not in the group)
				member = getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate.getUserExtSources());
			} catch (MemberNotExistsException e) {
				try {
					// We have new member (candidate), so create him using synchronous createMember (and overwrite chosed user attributes)
					member = getPerunBl().getMembersManagerBl().createMemberSync(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate, null, overwriteUserAttributesList);
					log.info("Group synchronization {}: New member id {} created during synchronization.", group, member.getId());
				} catch (AlreadyMemberException e1) {
					throw new ConsistencyErrorException("Trying to add existing member");
				} catch (AttributeValueException e1) {
					log.warn("Can't create member from candidate {} due to attribute value exception {}.", candidate, e1);
					skippedMembers.add("MemberEntry:[" + candidate + "] was skipped because there was problem when createing member from candidate: Exception: " + e1.getName() + " => '" + e1.getMessage() + "'");
					continue;
				} catch (ExtendMembershipException ex) {
					log.warn("Can't create member from candidate {} due to membership expiration exception {}.", candidate, ex);
					skippedMembers.add("MemberEntry:[" + candidate + "] was skipped because membership expiration: Exception: " + ex.getName() + " => " + ex.getMessage() + "]");
					continue;
				}
			}

			try {
				// Add the member to the group
				if (!group.getName().equals(VosManager.MEMBERS_GROUP)) {
					// Do not add members to the generic members group
					try {
						getPerunBl().getGroupsManagerBl().addMember(sess, group, member);
					} catch(NotMemberOfParentGroupException ex) {
						// Shouldn't happen, because every group has at least Members group as a parent
						throw new ConsistencyErrorException(ex);
					}
				}
				log.info("Group synchronization {}: New member id {} added.", group, member.getId());
			} catch (AlreadyMemberException e) {
				//This part is ok, it means someone add member before synchronization ends, log it and skip this member
				log.debug("Member {} was added to group {} before adding process. Skip this member.", member, group);
				continue;
			} catch (AttributeValueException e) {
				// There is a problem with attribute value, so set INVALID status of the member
				getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
			}

			// Try to validate member
			try {
				getPerunBl().getMembersManagerBl().validateMember(sess, member);
			} catch (AttributeValueException e) {
				log.warn("Member id {} will be in INVALID status due to wrong attributes {}.", member.getId(), e);
			}
		}
	}

	/**
	 * Remove former members from group (if they are not listed in ExtSource yet).
	 *
	 * If this is membersGroup (of some Vo) try to disableMember, if not possible then delete him.
	 * If this is regular group (of some Vo) remove him and if this group is also
	 * his last authoritative group, disable or delete him also in the Vo.
	 *
	 * @param sess
	 * @param group to be synchronized
	 * @param membersToRemove list of members to be removed from Group
	 *
	 * @throws InternalErrorException if some internal error occurs
	 * @throws WrongAttributeAssignmentException if there is some problem with assignment of attribute
	 * @throws MemberAlreadyRemovedException if member is already out of group when we trying to do this by synchronization
	 */
	private void removeFormerMembersWhileSynchronization(PerunSession sess, Group group, List<RichMember> membersToRemove) throws InternalErrorException, WrongAttributeAssignmentException, MemberAlreadyRemovedException {
		//First get information if this group is authoritative group
		boolean thisGroupIsAuthoritativeGroup = false;
		try {
			Attribute authoritativeGroupAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_D_AUTHORITATIVE_GROUP);
			if(authoritativeGroupAttr.getValue() != null) {
				Integer authoritativeGroupValue = (Integer) authoritativeGroupAttr.getValue();
				if(authoritativeGroupValue == 1) thisGroupIsAuthoritativeGroup = true;
			}
		} catch (AttributeNotExistsException ex) {
			//Means that this group is not authoritative
			log.error("Attribute {} doesn't exists.", A_G_D_AUTHORITATIVE_GROUP);
		}

		//Second remove members (use authoritative group where is needed)
		for (RichMember member: membersToRemove) {
			// Member is missing in the external group, so remove him from the perun group
			try {
				//members group
				if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
					// If the group is members group, the member must be disabled as a member of VO
					try {
						getPerunBl().getMembersManagerBl().disableMember(sess, member);
						log.info("Group synchronization {}: Member id {} disabled.", group, member.getId());
					} catch(MemberNotValidYetException ex) {
						//Member is still invalid in perun. We can delete him.
						getPerunBl().getMembersManagerBl().deleteMember(sess, member);
						log.info("Group synchronization {}: Member id {} would have been disabled but he has been deleted instead because he was invalid.", group, member.getId());
					}
					//not members group
				} else {
					//If this group is authoritative group, check if this is last authoritative group of this member
					//If Yes = deleteMember (from Vo), if No = only removeMember
					if(thisGroupIsAuthoritativeGroup) {
						List<Group> memberAuthoritativeGroups = null;
						try {
							memberAuthoritativeGroups = getAllAuthoritativeGroupsOfMember(sess, member);
						} catch (AttributeNotExistsException ex) {
							//This means that no authoritative group can exists without this attribute
							log.error("Attribute {} doesn't exists.", A_G_D_AUTHORITATIVE_GROUP);
						}

						//If list of member authoritativeGroups is not null, attribute exists
						if(memberAuthoritativeGroups != null) {
							memberAuthoritativeGroups.remove(group);
							if(memberAuthoritativeGroups.isEmpty()) {
								//First try to disable member, if is invalid, delete him from Vo
								try {
									getPerunBl().getMembersManagerBl().disableMember(sess, member);
									log.info("Group synchronization {}: Member id {} disabled because synchronizer wants to remove him from last authoritativeGroup in Vo.", group, member.getId());
									getPerunBl().getGroupsManagerBl().removeMember(sess, group, member);
									log.info("Group synchronization {}: Member id {} removed.", group, member.getId());
								} catch(MemberNotValidYetException ex) {
									//Member is still invalid in perun. We can delete him.
									getPerunBl().getMembersManagerBl().deleteMember(sess, member);
									log.info("Group synchronization {}: Member id {} would have been disabled but he has been deleted instead because he was invalid and synchronizer wants to remove him from last authoritativeGroup in Vo.", group, member.getId());
								}
							} else {
								//If there is still some other authoritative group for this member, only remove him from group
								getPerunBl().getGroupsManagerBl().removeMember(sess, group, member);
								log.info("Group synchronization {}: Member id {} removed.", group, member.getId());
							}
							//If list of member authoritativeGroups is null, attribute not exists, only remove member from Group
						} else {
							getPerunBl().getGroupsManagerBl().removeMember(sess, group, member);
							log.info("Group synchronization {}: Member id {} removed.", group, member.getId());
						}
					} else {
						getPerunBl().getGroupsManagerBl().removeMember(sess, group, member);
						log.info("Group synchronization {}: Member id {} removed.", group, member.getId());
					}
				}
			} catch (NotGroupMemberException e) {
				throw new ConsistencyErrorException("Trying to remove non-existing user");
			} catch (MemberAlreadyRemovedException ex) {
				//Member was probably removed before starting of synchronization removing process, log it and skip this member
				log.debug("Member {} was removed from group {} before removing process. Skip this member.", member, group);
				continue;
			}
		}
	}

	/**
	 * Try to close both extSources (membersSource and group source)
	 *
	 * @param membersSource optional membersSource
	 * @param source default groupSource
	 */
	private void closeExtSourcesAfterSynchronization(ExtSource membersSource, ExtSource source) {
		//Close open extSources (not empty ones) if they support this operation
		if(membersSource != null) {
			try {
				((ExtSourceSimpleApi) membersSource).close();
			} catch (ExtSourceUnsupportedOperationException e) {
				// ExtSource doesn't support that functionality, so silently skip it.
			} catch (InternalErrorException e) {
				log.info("Can't close membersSource connection. Cause: {}", e);
			}
		}
		if(source != null) {
			try {
				((ExtSourceSimpleApi) source).close();
			} catch (ExtSourceUnsupportedOperationException e) {
				// ExtSource doesn't support that functionality, so silently skip it.
			} catch (InternalErrorException e) {
				log.info("Can't close extSource connection. Cause: {}", e);
			}
		}
	}
}
