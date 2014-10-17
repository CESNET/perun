package cz.metacentrum.perun.core.blImpl;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
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
		//Use sorting by group names reverse order (frist name A:B:c then A:B etc.)
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
		group = getGroupsManagerImpl().updateGroup(sess, group);
		getPerunBl().getAuditer().log(sess, "{} updated.", group);

		List<Group> allSubgroups = this.getAllSubGroups(sess, group);
		for(Group g: allSubgroups) {
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

	public List<User> getAdmins(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getAdmins(sess, group);
	}

	@Override
	public List<User> getDirectAdmins(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getDirectAdmins(sess, group);
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupAdmins(sess, group);
	}

	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
		return richUsers;
	}

	public List<RichUser> getDirectRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getDirectAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
		return richUsers;
	}

	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
		return richUsers;
	}

	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getRichAdmins(perunSession, group), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

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
		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		List<Integer> groupsIds = new ArrayList<Integer>(new HashSet<Integer>(this.groupsManagerImpl.getMemberGroupsIds(sess, member, vo)));
		List<Group> groups = getPerunBl().getGroupsManagerBl().getGroupsByIds(sess, groupsIds);

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
		return new ArrayList<Group>(new HashSet<Group>(getGroupsManagerImpl().getAllMemberGroups(sess, member)));
	}

	public List<Group> getMemberGroupsForResources(PerunSession sess, Member member) throws InternalErrorException {
		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);
		List<Integer> groupsIds = this.groupsManagerImpl.getMemberGroupsIdsForResources(sess, member, vo);

		List<Group> groups = getPerunBl().getGroupsManagerBl().getGroupsByIds(sess, groupsIds);

		// Sort
		Collections.sort(groups);

		return groups;
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
	 * This method is run in separate transaction.
	 */
	public void synchronizeGroup(PerunSession sess, Group group) throws InternalErrorException, MemberAlreadyRemovedException {

		ExtSource source = null;
		ExtSource membersSource = null;
		try {
			log.info("Group synchronization {}: started.", group);
			int remainingNumberOfUsersToSynchronize;

			// INITIALIZATION OF EXTSOURCE

			// Get the extSource name from the group attribute
			String membersExtSourceName;
			try {
				Attribute extSourceNameAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPEXTSOURCE_ATTRNAME);
				if (extSourceNameAttr == null || (extSourceNameAttr != null && extSourceNameAttr.getValue() == null)) {
					throw new InternalErrorException("ExtSource is not set");
				}
				membersExtSourceName = (String) extSourceNameAttr.getValue();
				source = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, membersExtSourceName);
			} catch (AttributeNotExistsException e) {
				throw new InternalErrorException(e);
			} catch (WrongAttributeAssignmentException e) {
				throw new InternalErrorException(e);
			} catch (ExtSourceNotExistsException e) {
				throw new InternalErrorException(e);
			}

			// Check if the groupMembersExtSource is set
			try {
				Attribute membersExtSourceNameAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPMEMBERSEXTSOURCE_ATTRNAME);

				// If the different extSource name for the members was set use it, otherwise use the group one
				if (membersExtSourceNameAttr != null && membersExtSourceNameAttr.getValue() != null) {
					membersExtSourceName = (String) membersExtSourceNameAttr.getValue();
					membersSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, membersExtSourceName);
				} else {
					// No such groupMembersExtSource, so use groupExtSource also for searching candidates
					membersSource = source;
				}
			} catch (AttributeNotExistsException e) {
				// Ignore
			} catch (WrongAttributeAssignmentException e) {
				// Ignore
			} catch (ExtSourceNotExistsException e) {
				throw new InternalErrorException(e);
			}

			// Get all group attributes and store them in simple Map<String, String> in order to use method getGroupSubjects
			Map<String, String> attributes = new HashMap<String, String>();
			List<Attribute> groupAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, group);

			for (Attribute attr: groupAttributes) {
				// Store only string attributes
				// FIXME neslo by to udelat lepe?
				if (attr.getType().equals(String.class.getName())) {
					if (attr.getValue() != null) attributes.put(attr.getName(), (String) attr.getValue());
				}
			}
			log.info("Group synchronization {}: using configuration extSource for membership {}, extSource for members {}", new Object[] {group, membersSource, membersExtSourceName});
			// END - INITIALIZATION OF EXTSOURCE

			// GET ALL SUBJECTS' LOGINS FROM EXTERNAL GROUP


			// Get the subjects from the external group
			List<Map<String, String>> subjects;
			try {
				subjects = ((ExtSourceApi) source).getGroupSubjects(attributes);
				log.debug("Group synchronization {}: external group contains {} members.", group, subjects.size());
			} catch (ExtSourceUnsupportedOperationException e2) {
				throw new InternalErrorException("ExtSrouce " + source.getName() + " doesn't support getGroupSubjects", e2);
			}

			// Get total number of users to synchronize
			remainingNumberOfUsersToSynchronize = subjects.size();
			// END - GET ALL SUBJECTS' LOGINS FROM EXTERNAL GROUP

			// GET CANDIDATES
			// Get all subjects with attributes
			List<Candidate> candidates = new ArrayList<Candidate>();
			for (Map<String, String> subject: subjects) {
				String login = subject.get("login");
				// Skip subjects, which doesn't have login
				if (login == null || login == "") {
					log.debug("Subject {} doesn't contain attribute login, skipping.", subject);
					continue;
				}
				try {
					candidates.add((getPerunBl().getExtSourcesManagerBl().getCandidate(sess, membersSource, login)));
				} catch (ExtSourceNotExistsException e) {
					throw new InternalErrorException("ExtSource " + membersSource + " doesn't exists.");
				} catch (CandidateNotExistsException e) {
					log.warn("getGroupSubjects subjects returned login {}, but it cannot be obtained using getCandidate()", login);
					continue;
				} catch (ExtSourceUnsupportedOperationException e) {
					log.warn("ExtSource {} doesn't support getCandidate operation.", membersSource);
					continue;
				}
			}
			// END - GET CANDIDATES

			// GET CURRENT MEMBERS IN PERUN

			// Get current group members
			List<RichMember> currentMembers = this.getGroupRichMembersWithAttributes(sess, group);
			log.debug("Group synchronization {}: perun group contains {} members.", group, currentMembers.size());

			// END - GET CURRENT MEMBERS IN PERUN

			// List of members which will be removed from the perun group. Firstly fill the list with all
			// perun group members, remove entry from the list, if the member is in the external source
			List<RichMember> membersToRemove = new ArrayList<RichMember>(currentMembers);

			// List of candidates which will be finally added to the perun group. Firstly fill the list and then remove those who are already in the Group
			List<Candidate> candidatesToAdd = new ArrayList<Candidate>(candidates);

			// Iterate through members from the external group and find the differences with the perun group
			for (Candidate candidate: candidates) {

				// Try to find, if the subject is in the perun group
				for (RichMember richMember: currentMembers) {
					// FIXME urcite existuje lepsi reseni
					Member member = new Member(richMember.getId(), richMember.getUserId(), richMember.getVoId(), richMember.getStatus());

					if (this.hasCandidateExistingMember(candidate, richMember)) {

						log.trace("Group synchronization {}: member id {} is already in the group.", group, member.getId());
						// Remove the member from the list membersIdsToRemove and remove candidate from candidatesToAdd
						membersToRemove.remove(richMember);
						candidatesToAdd.remove(candidate);

						// Iterate through all subject attributes
						log.trace("Group synchronization {}: checking the state of the attributes for member {}", group, member);
						for (String attributeName : candidate.getAttributes().keySet()) {
							boolean existingMemberAttributeFound = false;
							boolean existingUserAttributeFound = false;

							// look if the member already has subject attributes assigned
							for (Attribute memberAttribute: richMember.getMemberAttributes()) {
								if (memberAttribute.getName().equals(attributeName)) {
									// if yes, look if the values is the same
									// Get attribute type and convert attribute value from subject into that type
									Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), memberAttribute.getType());
									if (subjectAttributeValue != null && !memberAttribute.getValue().equals(subjectAttributeValue)) {
										log.trace("Group synchronization {}: value of the attribute {} for member {} changed. Original value {}, new value {}.",
												new Object[] {group, memberAttribute, member, memberAttribute.getValue(), subjectAttributeValue});

										// value differs, so store the new one
										memberAttribute.setValue(subjectAttributeValue);
										try {
											getPerunBl().getAttributesManagerBl().setAttribute(sess, member, memberAttribute);
										} catch (AttributeValueException e) {
											// There is a problem with attribute value, so set INVALID status for the member
											getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
										} catch (WrongAttributeAssignmentException e) {
											throw new ConsistencyErrorException(e);
										}

										existingMemberAttributeFound = true;
									}
								}
							}

							if (!existingMemberAttributeFound) {
								for (Attribute userAttribute: richMember.getUserAttributes()) {
									if (userAttribute.getName().equals(attributeName)) {
										// if yes, look if the values is the same
										Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), userAttribute.getType());
										if (subjectAttributeValue != null && !userAttribute.getValue().equals(subjectAttributeValue)) {
											// value differs, so store the new one
											log.trace("Group synchronization {}: value of the attribute {} for member {} changed. Original value {}, new value {}.",
													new Object[] {group, userAttribute, member, userAttribute.getValue(), subjectAttributeValue});

											userAttribute.setValue(subjectAttributeValue);
											try {
												getPerunBl().getAttributesManagerBl().mergeAttributeValue(sess, richMember.getUser(), userAttribute);
											} catch (AttributeValueException e) {
												// There is a problem with attribute value, so set INVALID status for the member
												getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
											} catch (WrongAttributeAssignmentException e) {
												throw new ConsistencyErrorException(e);
											}
										}
										existingUserAttributeFound = true;
									}
								}


								if (!existingUserAttributeFound) {
									// New attribute, so add it
									AttributeDefinition attributeDefinition;
									try {
										// look if the attribute definition exists
										attributeDefinition = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
									} catch (AttributeNotExistsException e) {
										log.error("Atttribute {} doesn't exists.", attributeName);
										throw new ConsistencyErrorException(e);
									}

									Attribute newAttribute = new Attribute(attributeDefinition);
									Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), newAttribute.getType());
									newAttribute.setValue(subjectAttributeValue);
									try {
										if (attributeDefinition.getEntity().equals(AttributesManager.ENTITY_MEMBER)) {
											try {
												// Try to set member's attributes
												getPerunBl().getAttributesManagerBl().setAttribute(sess, member, newAttribute);
											} catch (AttributeValueException e) {
												// There is a problem with attribute value, so set INVALID status for the member
												getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
											}
										} else if (attributeDefinition.getEntity().equals(AttributesManager.ENTITY_USER)) {
											try {
												// Try to set user's attributes
												getPerunBl().getAttributesManagerBl().setAttribute(sess, richMember.getUser(), newAttribute);
											} catch (AttributeValueException e) {
												// There is a problem with attribute value, so set INVALID status of the member
												getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
												try {
													// The member is invalid, so try to set the value again, and check if the change has influence also on other members
													getPerunBl().getAttributesManagerBl().setAttribute(sess, richMember.getUser(), newAttribute);
												} catch (AttributeValueException e1) {
													// The change of the attribute value influences also members in other VOs, so we have to invalidate the whole user
													//FIXME invalidate all members
												}
											}
										}
									} catch (WrongAttributeAssignmentException e) {
										throw new ConsistencyErrorException(e);
									}
									log.trace("Setting the {} value {}", newAttribute, candidate.getAttributes().get(attributeName));
								}
							}
						}

						// Synchronize userExtSources
						for (UserExtSource ues : candidate.getUserExtSources()) {
							if (!getPerunBl().getUsersManagerBl().userExtSourceExists(sess, ues)) {
								try {
									getPerunBl().getUsersManagerBl().addUserExtSource(sess, richMember.getUser(), ues);
								} catch (UserExtSourceExistsException e) {
									throw new ConsistencyErrorException("Adding already existing userExtSource " + ues, e);
								}
							}
						}

						// If the member has expired or disabled status, try to expire/validate him (depending on expiration date)
						try {
							if (richMember.getStatus().equals(Status.DISABLED) || richMember.getStatus().equals(Status.EXPIRED)) {
								Date now = new Date();

								Attribute memberExpiration = null;

								for(Attribute att: richMember.getMemberAttributes()) {
									if((AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration").equals(att.getName())){
										memberExpiration = att;
										break;
									}
								}

								if (memberExpiration != null && memberExpiration.getValue() != null) {
									Date currentMembershipExpirationDate = BeansUtils.DATE_FORMATTER.parse((String) memberExpiration.getValue());

									if (currentMembershipExpirationDate.before(now)) {
										//disabled members which are after expiration date will be expired
										if (richMember.getStatus().equals(Status.DISABLED)) {
											try {
												perunBl.getMembersManagerBl().expireMember(sess, member);
												log.info("Switching member id {} to EXPIRE state, due to expiration {}.", richMember.getId(), (String) memberExpiration.getValue());
												log.debug("Switching member to EXPIRE state, additional info: membership expiration date='{}', system now date='{}'", currentMembershipExpirationDate, now);
											} catch (MemberNotValidYetException e) {
												log.error("Consistency error while trying to expire member id {}, exception {}", richMember.getId(), e);
											}
										}
									} else {
										//disabled and expired members which are before expiration date will be validated
										try {
											perunBl.getMembersManagerBl().validateMember(sess, member);
											log.info("Switching member id {} to VALID state, due to expiration {}.", richMember.getId(), (String) memberExpiration.getValue());
											log.debug("Switching member to VALID state, additional info: membership expiration date='{}', system now date='{}'", currentMembershipExpirationDate, now);
										} catch (WrongAttributeValueException e) {
											log.error("Error during validating member id {}, exception {}", richMember.getId(), e);
										} catch (WrongReferenceAttributeValueException e) {
											log.error("Error during validating member id {}, exception {}", richMember.getId(), e);
										}
									}
								}
							}
						} catch (ParseException e) {
							log.error("Group synchronization: member expiration String cannot be parsed, exception {}", e);
						}

						// If the member has INVALID status, try to validate the member
						try {
							if (richMember.getStatus().equals(Status.INVALID)) {
								getPerunBl().getMembersManagerBl().validateMember(sess, member);
							}
						} catch (WrongAttributeValueException e) {
							log.info("Member id {} will stay in INVALID state, because there was problem with attributes {}.", richMember.getId(), e);
						} catch (WrongReferenceAttributeValueException e) {
							log.info("Member id {} will stay in INVALID state, because there was problem with attributes {}.", richMember.getId(), e);
						}

						// If the member has DISABLED status, try to validate the member
						try {
							if (richMember.getStatus().equals(Status.DISABLED)) {
								getPerunBl().getMembersManagerBl().validateMember(sess, member);
							}
						} catch (WrongAttributeValueException e) {
							log.info("Switching member id {} into INVALID state from DISABLED, because there was problem with attributes {}.", richMember.getId(), e);
						} catch (WrongReferenceAttributeValueException e) {
							log.info("Switching member id {} into INVALID state from DISABLED, because there was problem with attributes {}.", richMember.getId(), e);
						}
					}
				}
				remainingNumberOfUsersToSynchronize--;
				log.trace("Group synchronization: remaining number of users to synchronize {}.", remainingNumberOfUsersToSynchronize);
			}

			// Now add missing members
			for (Candidate candidate: candidatesToAdd) {
				Member member = null;
				try {
					// Check if the member is already in the VO
					member = getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate.getUserExtSources());
				} catch (MemberNotExistsException e) {
					try {
						// We have new member, so create him using synchronous createMember
						member = getPerunBl().getMembersManagerBl().createMemberSync(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate);
						log.info("Group synchronization {}: New member id {} created during synchronization.", group, member.getId());
					} catch (AlreadyMemberException e1) {
						throw new ConsistencyErrorException("Trying to add existing member");
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
					throw new ConsistencyErrorException("Trying to add existing member");
				} catch (AttributeValueException e) {
					// There is a problem with attribute value, so set INVALID status of the member
					getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
				}

				// Try to validate member
				try {
					getPerunBl().getMembersManagerBl().validateMember(sess, member);
				} catch (WrongAttributeValueException e) {
					log.warn("Member id {} will be in INVALID status due to wrong attributes {}.", member.getId(), e);
				} catch (WrongReferenceAttributeValueException e) {
					log.warn("Member id {} will be in INVALID status due to wrong attributes {}.", member.getId(), e);
				}
			}

			//Get information if this group is authoritative group
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
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}

			// Now remove members who is no longer member of the external group
			for (RichMember member: membersToRemove) {
				// Member is missing in the external group, so remove him from the perun group
				try {
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
					} else {

						// START - mechanic for removing member from group which is not membersGroup

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
										log.info("Group synchronization {}: Member id {} disabled because synchronizator wants to remove him from last authoritativeGroup in Vo.", group, member.getId());
										getPerunBl().getGroupsManagerBl().removeMember(sess, group, member);
										log.info("Group synchronization {}: Member id {} removed.", group, member.getId());
									} catch(MemberNotValidYetException ex) {
										//Member is still invalid in perun. We can delete him.
										getPerunBl().getMembersManagerBl().deleteMember(sess, member);
										log.info("Group synchronization {}: Member id {} would have been disabled but he has been deleted instead because he was invalid and synchronizator wants to remove him from last authoritativeGroup in Vo.", group, member.getId());
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

						// END - mechanic for removing member from group which is not membersGroup

					}
				} catch (NotGroupMemberException e) {
					throw new ConsistencyErrorException("Trying to remove non-existing user");
				}
			}

			log.info("Group synchronization {}: ended.", group);
			// FIXME temporarily disabled
			//getPerunBl().getAuditer().log(sess, "{} successfully synchronized.", group);
		} finally {
			if(membersSource != null) {
				try {
					((ExtSourceApi) membersSource).close();
				} catch (ExtSourceUnsupportedOperationException e) {
					// ExtSource doesn't support that functionality, so silently skip it.
				} catch (InternalErrorException e) {
					log.error("Can't close membersSource connection. Cause: {}", e);
				}
			}
			if(source != null) {
				try {
					((ExtSourceApi) source).close();
				} catch (ExtSourceUnsupportedOperationException e) {
					// ExtSource doesn't support that functionality, so silently skip it.
				} catch (InternalErrorException e) {
					log.error("Can't close extSource connection. Cause: {}", e);
				}
			}
		}

	}

	/**
	 * Force group synchronization.
	 *
	 * Adds the group synchronization process in the groupSynchronizerThreads.
	 *
	 * @param sess
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
		int intervalMultiplier = Integer.parseInt(Utils.getPropertyFromConfiguration("perun.group.synchronization.interval"));
		int timeout = Integer.parseInt(Utils.getPropertyFromConfiguration("perun.group.synchronization.timeout"));

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
						log.warn("Timeout {} minutes of the synchronizatin thread for the group {} reached.", timeout, group);
						groupSynchronizerThreads.get(group).interrupt();
						groupSynchronizerThreads.remove(group);
						numberOfTerminatedSynchronizations++;
					} else {
						numberOfActiveSynchronizations++;
					}
				} else {
					// Start and run the new thread
					try {
						// Do not overload externalSource, run each synchronizatin in 0-30s steps
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
		private PerunSession sess;
		private Group group;
		private long startTime;

		public GroupSynchronizerThread(PerunSession sess, Group group) {
			this.sess = sess;
			this.group = group;
		}

		public void run() {
			//if some exception was thrown during synchronization
			boolean exceptionThrown = false;
			//text of exception if was thrown
			String exceptionMessage = null;
			
			try {
				log.debug("Synchronization thread for group {} has started.", group);
				// Set the start time, so we can check the timeout of the thread
				startTime = System.currentTimeMillis();

				((PerunBl) sess.getPerun()).getGroupsManagerBl().synchronizeGroup(sess, group);

				log.debug("Synchronization thread for group {} has finished in {} ms.", group, System.currentTimeMillis()-startTime);
			} catch (WrongAttributeValueException e) {
				exceptionThrown = true;
				exceptionMessage = "Cannot synchronize group " + group +" due to exception:";
				log.error(exceptionMessage, e);
				exceptionMessage+= e.getName() + " => " + e.getMessage();
			} catch (WrongReferenceAttributeValueException e) {
				exceptionThrown = true;
				exceptionMessage = "Cannot synchronize group " + group +" due to exception:";
				log.error(exceptionMessage, e);
				exceptionMessage+= e.getName() + " => " + e.getMessage();
			} catch (InternalErrorException e) {
				exceptionThrown = true;
				exceptionMessage = "Internal Error Exception while synchronizing the group " + group + ":";
				log.error(exceptionMessage, e);
				exceptionMessage+= e.getName() + " => " + e.getMessage();
			} catch (WrongAttributeAssignmentException e) {
				exceptionThrown = true;
				exceptionMessage = "Wrong Attribute Assignment Exception while synchronizing the group " + group + ":";
				log.error(exceptionMessage, e);
				exceptionMessage+= e.getName() + " => " + e.getMessage();
			} catch (MemberAlreadyRemovedException e) {
				exceptionThrown = true;
				exceptionMessage = "Member Already Removed Exception while synchronizing the group " + group + " due to exception: ";
				log.error(exceptionMessage, e);
				exceptionMessage+= e.getName() + " => " + e.getMessage();
			} finally {
				Date currentTimestamp = new Date();
				if(!exceptionThrown) {
					exceptionMessage = "OK";
				}
				//Save information about group synchronization, this method run in new transaction
				try {
					((PerunBl) sess.getPerun()).getGroupsManagerBl().saveInformationAboutGroupSynchronization(sess, group, currentTimestamp, exceptionMessage);
				} catch (Exception ex) {
					log.error("When synchronization group " + group + ", exception was thrown.", ex);
				}
				log.debug("GroupSynchronizerThread finnished for group: {}", group);
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
			throw new InternalErrorException("Aiding attribtue must have primaryHolder which is not null.");
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

	public void saveInformationAboutGroupSynchronization(PerunSession sess, Group group, Date currentTimestamp, String exceptionMessage) throws AttributeNotExistsException, InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		//If session is null, throw an exception
		if (sess == null) {
			throw new InternalErrorException("Session is null when trying to save information about synchronization. Group: " + group + ", timestamp: " + currentTimestamp + ",message: " + exceptionMessage);
		}

		//If group is null, throw an exception
		if (group == null) {
			throw new InternalErrorException("Object group is null when trying to save information about synchronization. Timestamp: " + currentTimestamp + ", message: " + exceptionMessage);
		}

		//if currentTimestamp is null, create new date and log this
		if (currentTimestamp == null){
			currentTimestamp = new Date();
			log.error("When synchronize group " + group + " timestamp was null. Was create a new one and use it.");
		}

		//if exceptionMessage is null or empty, use "Empty message"
		if (exceptionMessage == null || exceptionMessage.isEmpty()) {
			exceptionMessage = "Empty message.";
		}

		//Set correct format of currentTimestamp
		String currectTimestampString = BeansUtils.DATE_FORMATTER.format(currentTimestamp);

		//Get both attribute defintion lastSynchroTimestamp and lastSynchroState
		//Get definitions and values, set values
		Attribute lastSynchronizationTimestamp = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lastSynchronizationTimestamp"));
		Attribute lastSynchronizationState = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lastSynchronizationState"));
		lastSynchronizationTimestamp.setValue(currectTimestampString);
		lastSynchronizationState.setValue(exceptionMessage);
		//setAttributes for group
		List<Attribute> attrsToSet = new ArrayList<>();
		attrsToSet.add(lastSynchronizationState);
		attrsToSet.add(lastSynchronizationTimestamp);
		((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttributes(sess, group, attrsToSet);
	}
}
