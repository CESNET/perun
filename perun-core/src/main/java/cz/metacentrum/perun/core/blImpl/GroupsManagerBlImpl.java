package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.PerunPrincipal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.entry.GroupsManagerEntry;
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
		if (group.getParentGroupId() != null) throw new InternalErrorException("Top-level groups can't have parentGroupId set!");
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

	public Group createGroup(PerunSession sess, Group parentGroup, Group group) throws GroupExistsException, InternalErrorException, GroupOperationsException, GroupRelationNotAllowed, GroupRelationAlreadyExists {
		Vo vo = this.getVo(sess, parentGroup);

		group = getGroupsManagerImpl().createGroup(sess, vo, parentGroup, group);
		parentGroup = createGroupUnion(sess, parentGroup, group, true);

		getPerunBl().getAuditer().log(sess, "{} created in {} as subgroup of {}", group, vo, parentGroup);

		return group;
	}

	public void deleteGroup(PerunSession sess, Group group, boolean forceDelete) throws InternalErrorException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new java.lang.IllegalArgumentException("Built-in " + group.getName() + " group cannot be deleted separately.");
		}

		this.deleteAnyGroup(sess, group, forceDelete);
	}

	public void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws InternalErrorException, GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupOperationsException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
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

	public void deleteMembersGroup(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
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
	protected void deleteAnyGroup(PerunSession sess, Group group, boolean forceDelete) throws InternalErrorException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		Vo vo = this.getVo(sess, group);

		if (getGroupsManagerImpl().getSubGroupsCount(sess, group) > 0) {
			if (!forceDelete) throw new RelationExistsException("Group group="+group+" contains subgroups");

			// make sure we delete all subgroups !!
			List<Group> subGroups = getAllSubGroups(sess, group);

			// Use sorting by group names reverse order (first A:B:c then A:B etc.)
			// to make sure we delete from the bottom in a hierarchy
			Collections.sort(subGroups, Collections.reverseOrder(
					new Comparator<Group>() {
						@Override
						public int compare(Group groupToCompare,Group groupToCompareWith) {
							return groupToCompare.getName().compareTo(groupToCompareWith.getName());
						}
					}));

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
						} catch (PasswordDeletionFailedException | PasswordOperationTimeoutException ex) {
							throw new InternalErrorException("Failed to delete reserved login "+login.getRight()+" from KDC.", ex);
						}
					}
				}
				// delete all Groups reserved logins from DB
				getGroupsManagerImpl().deleteGroupReservedLogins(sess, group);

				//Remove all information about group on facilities (facilities contacts)
				List<ContactGroup> groupContactGroups = getPerunBl().getFacilitiesManagerBl().getFacilityContactGroups(sess, group);
				if(!groupContactGroups.isEmpty()) {
					getPerunBl().getFacilitiesManagerBl().removeAllGroupContacts(sess, group);
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

				// 1. remove all relations with group g as an operand group.
				// this removes all relations that depend on this group
				List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, g.getId());
				for (Integer groupId : relations) {
					removeGroupUnion(sess, groupsManagerImpl.getGroupById(sess, groupId), g, true);
				}

				// 2. remove all relations with group as a result group
				// We can remove relations without recalculation (@see processRelationMembers)
				// because all dependencies of group were deleted in step 1.
				groupsManagerImpl.removeResultGroupRelations(sess, g);

				// Group applications, submitted data and app_form are deleted on cascade with "deleteGroup()"

				List<Member> membersFromDeletedGroup = getGroupMembers(sess, g);
				// Deletes also all direct and indirect members of the group
				getGroupsManagerImpl().deleteGroup(sess, vo, g);

				logTotallyRemovedMembers(sess, g.getParentGroupId(), membersFromDeletedGroup);

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
				} catch (PasswordDeletionFailedException | PasswordOperationTimeoutException ex) {
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

		// 1. remove all relations with group g as an operand group.
		// this removes all relations that depend on this group
		List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, group.getId());
		for (Integer groupId : relations) {
			removeGroupUnion(sess, groupsManagerImpl.getGroupById(sess, groupId), group, true);
		}

		// 2. remove all relations with group as a result group
		// We can remove relations without recalculation (@see processRelationMembers)
		// because all dependencies of group were deleted in step 1.
		groupsManagerImpl.removeResultGroupRelations(sess, group);

		// Group applications, submitted data and app_form are deleted on cascade with "deleteGroup()"
		List<Member> membersFromDeletedGroup = getGroupMembers(sess, group);
		// Deletes also all direct and indirect members of the group
		getGroupsManagerImpl().deleteGroup(sess, vo, group);

		logTotallyRemovedMembers(sess, group.getParentGroupId(), membersFromDeletedGroup);

		getPerunBl().getAuditer().log(sess, "{} deleted.", group);
	}

	/**
	 * Log members that were deleted from parent group totally to auditer.
	 *
	 * @param sess perun session
	 * @param parentGroupId group id
	 * @param membersFromDeletedGroup deleted members from child group
	 * @throws InternalErrorException
	 */
	private void logTotallyRemovedMembers(PerunSession sess, Integer parentGroupId, List<Member> membersFromDeletedGroup) throws InternalErrorException {
		while(parentGroupId != null) {
			Group parentGroup;
			try {
				parentGroup = getGroupById(sess, parentGroupId);
			} catch (GroupNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
			// getting members from parent group AFTER the indirect members from subgroup were removed from this group.
			List<Member> membersFromParentGroup = getGroupMembers(sess, parentGroup);
			// removeAll will remove all members which remains in parent group even after they removal of INDIRECT records.
			membersFromDeletedGroup.removeAll(membersFromParentGroup);
			// now all members which left in membersFromDeletedGroup list are totally removed members from this group,
			// so we need to log them to auditer
			for(Member m: membersFromDeletedGroup) {
				getPerunBl().getAuditer().log(sess, "{} was removed from {} totally.", m, parentGroup);
			}
			parentGroupId=parentGroup.getParentGroupId();
		}
	}

	public void deleteAllGroups(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
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
			} catch (RelationExistsException | GroupNotExistsException e) {
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

	public void addMemberToMembersGroup(PerunSession sess, Group group,  Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotMemberOfParentGroupException, GroupNotExistsException, GroupOperationsException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.addDirectMember(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.addAdmin and MembersManager.createMember.");
		}
	}

	public void addMember(PerunSession sess, Group group, Member member) throws InternalErrorException, WrongReferenceAttributeValueException, GroupOperationsException, AlreadyMemberException, WrongAttributeValueException, GroupNotExistsException {
		// Check if the group is NOT members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Cannot add member directly to the members group.");
		} else {
			this.addDirectMember(sess, group, member);
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

	/**
	 * Add a record of the member with a DIRECT membership type to the group.
	 *
	 * @param sess perun session
	 * @param group group to add member to
	 * @param member member to be added as DIRECT
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupNotExistsException
	 * @throws GroupOperationsException
	 */
	protected void addDirectMember(PerunSession sess, Group group, Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException, GroupOperationsException {

		if(this.groupsManagerImpl.isDirectGroupMember(sess, group, member)) throw new AlreadyMemberException(member);

		boolean memberWasIndirectInGroup = this.isGroupMember(sess, group, member);

		member = getGroupsManagerImpl().addMember(sess, group, member, MembershipType.DIRECT, group.getId());
		getPerunBl().getAuditer().log(sess, "{} added to {}.", member, group);

		//If member was indirect in group before, we don't need to change anything in other groups
		if(memberWasIndirectInGroup) return;
		// check all relations with this group and call processRelationMembers to reflect changes of adding member to group
		List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, group.getId());
		for (Integer groupId : relations) {
			processRelationMembers(sess, groupsManagerImpl.getGroupById(sess, groupId), Collections.singletonList(member), group.getId(), true);
		}

		setRequiredAttributes(sess, member, group);
	}

	/**
	 * Add records of the members with an INDIRECT membership type to the group.
	 *
	 * @param sess perun session
	 * @param group group to add members to
	 * @param members list of members to add as INDIRECT
	 * @param sourceGroupId id of a group from which members originate
	 * @return list of members that were not members already
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	protected List<Member> addIndirectMembers(PerunSession sess, Group group, List<Member> members, int sourceGroupId) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		// save list of old group members
		List<Member> oldMembers = this.getGroupMembers(sess, group);

		for (Member member : members) {
			groupsManagerImpl.addMember(sess, group, member, MembershipType.INDIRECT, sourceGroupId);
		}

		// get list of new members
		List<Member> newMembers = this.getGroupMembers(sess, group);
		// select only newly added members
		newMembers.removeAll(oldMembers);

		for (Member member : newMembers) {
			setRequiredAttributes(sess, member, group);
			getPerunBl().getAuditer().log(sess, "{} added to {}.", member, group);
		}

		return newMembers;
	}

	/**
	 * Set required attributes when adding new direct or indirect members.
	 * @param sess perun session
	 * @param member member
	 * @param group group
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	private void setRequiredAttributes(PerunSession sess, Member member, Group group) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		// setting required attributes
		User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
		List<Resource> resources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
		for (Resource resource : resources) {
			Facility facility = getPerunBl().getResourcesManagerBl().getFacility(sess, resource);
			// check members attributes
			try {
				getPerunBl().getAttributesManagerBl().setRequiredAttributes(sess, facility, resource, user, member);
			} catch(WrongAttributeAssignmentException | AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}
	}

	/**
	 * Remove records of the members with an INDIRECT membership type from the group.
	 *
	 * @param sess perun session
	 * @param group group to remove records of INDIRECT members from
	 * @param members list of members to remove
	 * @param sourceGroupId id of a group from which members originate
	 * @return list of members that were removed (their only record in the group was deleted)
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws NotGroupMemberException
	 */
	protected List<Member> removeIndirectMembers(PerunSession sess, Group group, List<Member> members, int sourceGroupId) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotGroupMemberException {
		// save list of old group members
		List<Member> oldMembers = this.getGroupMembers(sess, group);

		for (Member member: members) {
			member.setSourceGroupId(sourceGroupId);
			groupsManagerImpl.removeMember(sess, group, member);
		}

		// get list of new members
		List<Member> newMembers = this.getGroupMembers(sess, group);
		// get only removed members
		oldMembers.removeAll(newMembers);

		for(Member removedIndirectMember: oldMembers) {
			getPerunBl().getAuditer().log(sess, "{} was removed from {} totally.", removedIndirectMember, group);
		}

		return oldMembers;
	}

	public void removeMember(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException, GroupNotExistsException, GroupOperationsException {
		// Check if the group is NOT members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Cannot remove member directly from the members group.");
		} else {
			this.removeDirectMember(sess, group, member);
		}
	}

	public void removeMemberFromMembersOrAdministratorsGroup(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException, GroupNotExistsException, GroupOperationsException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.removeDirectMember(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.removeAdmin and MembersManager.deleteMember.");
		}
	}

	protected void removeDirectMember(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException, GroupNotExistsException, GroupOperationsException {
		member.setSourceGroupId(group.getId());
		getGroupsManagerImpl().removeMember(sess, group, member);
		if (this.getGroupsManagerImpl().isGroupMember(sess, group, member)) {
			getPerunBl().getAuditer().log(sess, "{} was removed from {}.", member, group);
			//If member was indirect in group before, we don't need to change anything in other groups
			return;
		} else {
			getPerunBl().getAuditer().log(sess, "{} was removed from {} totally.", member, group);
		}

		// Remove member-group attributes
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, member, group);
		} catch (WrongAttributeAssignmentException | WrongAttributeValueException | WrongReferenceAttributeValueException e ) {
			throw new InternalErrorException(e);
		}

		// check all relations with this group and call processRelationMembers to reflect changes of removing member from group
		List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, group.getId());
		for (Integer groupId : relations) {
			processRelationMembers(sess, groupsManagerImpl.getGroupById(sess, groupId), Collections.singletonList(member), group.getId(), false);
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

	public List<Group> getAssignedGroupsToFacility(PerunSession sess, Facility facility) throws InternalErrorException {
		return getGroupsManagerImpl().getAssignedGroupsToFacility(sess, facility);
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
	 * @param groups initialized HashMap containing pair <topLevelGroup, null>
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
	public List<String> synchronizeGroup(PerunSession sess, Group group) throws InternalErrorException, MemberAlreadyRemovedException, AttributeNotExistsException, WrongAttributeAssignmentException, ExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupOperationsException, NotMemberOfParentGroupException, GroupNotExistsException {
		// Needed variables for whole method
		List<String> skippedMembers = new ArrayList<>();
		ExtSource source = null;
		ExtSource membersSource = null;

		try {
			log.info("Group synchronization {}: started.", group);

			// Initialization of group extSource
			source = getGroupExtSourceForSynchronization(sess, group);

			// Initialization of groupMembers extSource (if it is set), in other case set membersSource = source
			membersSource = getGroupMembersExtSourceForSynchronization(sess, group, source);

			// Prepare info about userAttributes which need to be overwrite (not just updated)
			List<String> overwriteUserAttributesList = getOverwriteUserAttributesListFromExtSource(membersSource);

			// Get info about type of synchronization (with or without update)
			boolean lightweightSynchronization = isThisLightweightSynchronization(sess, group);

			log.info("Group synchronization {}: using configuration extSource for membership {}, extSource for members {}", new Object[] {group, membersSource, membersSource.getName()});

			// Prepare containers for work with group members
			List<Candidate> candidatesToAdd = new ArrayList<>();
			Map<Candidate, RichMember> membersToUpdate = new HashMap<>();
			List<RichMember> membersToRemove = new ArrayList<>();
			// Get all actual members of group
			List<RichMember> actualGroupMembers = getPerunBl().getGroupsManagerBl().getGroupRichMembers(sess, group);
			// Subjects gained from external source
			List<Map<String, String>> subjects = new ArrayList<>();

			String status;
			if(lightweightSynchronization) {
				status = GroupsManager.GROUP_SYNC_STATUS_LIGHTWEIGHT;
				// Get subjects from extSource
				status = getSubjectsFromExtSource(sess, source, group, status, subjects);

				if (status.equals(GroupsManager.GROUP_SYNC_STATUS_FULL)) {
					categorizeMembersForLightweightSynchronization(sess, source, membersSource, actualGroupMembers,
							candidatesToAdd, membersToRemove, skippedMembers, subjects);
				}
			} else {
				status = GroupsManager.GROUP_SYNC_STATUS_FULL;
				// Get subjects from extSource
				status = getSubjectsFromExtSource(sess, source, group, status, subjects);

				// Categorizes members for synchronization
				categorizeMembersForSynchronization(sess, status, source, membersSource, actualGroupMembers, group,
						candidatesToAdd, membersToUpdate, membersToRemove, skippedMembers, subjects, overwriteUserAttributesList);
			}

			log.debug("Candidates to add: {} for group: {}", candidatesToAdd.size(), group);
			log.debug("Members to update: {} for group: {}", membersToUpdate.size(), group);
			log.debug("Members to remove: {} for group: {}", membersToRemove.size(), group);

			// Update members already presented in group
			updateExistingMembersWhileSynchronization(sess, group, membersToUpdate, overwriteUserAttributesList);
			// Add not presented candidates to group
			addMissingMembersWhileSynchronization(sess, group, candidatesToAdd, overwriteUserAttributesList, skippedMembers);
			// If status is full, remove members which are not already members of group
			if (status.equals(GroupsManager.GROUP_SYNC_STATUS_FULL)) {
				removeFormerMembersWhileSynchronization(sess, group, membersToRemove);
			}

			log.info("Group synchronization {}: ended.", group);
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
		int intervalMultiplier = BeansUtils.getCoreConfig().getGroupSynchronizationInterval();
		int timeout = BeansUtils.getCoreConfig().getGroupSynchronizationTimeout();

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
				if (groupSynchronizerThreads.containsKey(group.getId())) {
					// Check the running time of the thread
					long timeDiff = System.currentTimeMillis() - groupSynchronizerThreads.get(group.getId()).getStartTime();

					// If the time is greater than timeout set in the configuration file (in minutes)
					if (timeDiff/1000/60 > timeout) {
						// Timeout reach, stop the thread
						log.warn("Timeout {} minutes of the synchronization thread for the group {} reached.", timeout, group);
						groupSynchronizerThreads.get(group.getId()).interrupt();
						groupSynchronizerThreads.remove(group.getId());
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
				this.sess = perunBl.getPerunSession(pp, new PerunClient());
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

				if(!skippedMembers.isEmpty()) {
					skippedMembersMessage = "These members from extSource were skipped: { ";

					for(String skippedMember: skippedMembers) {
						if(skippedMember == null) continue;

						skippedMembersMessage+= skippedMember + ", ";
					}
					skippedMembersMessage+= " }";
					exceptionMessage = skippedMembersMessage;
				}

				log.debug("Synchronization thread for group {} has finished in {} ms.", group, System.currentTimeMillis()-startTime);
			} catch (WrongAttributeValueException | WrongReferenceAttributeValueException | InternalErrorException |
					WrongAttributeAssignmentException | MemberAlreadyRemovedException | GroupNotExistsException |
					GroupOperationsException | NotMemberOfParentGroupException | AttributeNotExistsException | ExtSourceNotExistsException e) {
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
					perunBl.getGroupsManagerBl().saveInformationAboutGroupSynchronization(sess, group, startTime, failedDueToException, exceptionMessage);
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

	public List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups, boolean useContext) throws InternalErrorException {

		//If no context should be used - every attribute is unique in context of group (for every group test access rights for all attributes again)
		if(!useContext) return filterOnlyAllowedAttributes(sess, richGroups);

		//If context should be used - every attribute is unique in a context of users authz_roles for a group + attribute URN
		// (every attribute test only once per authz+friendlyName)
		List<RichGroup> filteredRichGroups = new ArrayList<RichGroup>();
		if(richGroups == null || richGroups.isEmpty()) return filteredRichGroups;

		// context+attr_name to boolean where null means - no rights at all, false means no write rights, true means read and write rights
		Map<String, Boolean> contextMap = new HashMap<>();

		for(RichGroup rg : richGroups) {

			String voadmin = ((AuthzResolver.isAuthorized(sess, Role.VOADMIN, rg) ? "VOADMIN" : ""));
			String voobserver = ((AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, rg) ? "VOOBSERVER" : ""));
			String groupadmin = ((AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, rg) ? "GROUPADMIN" : ""));
			String key = voadmin + voobserver + groupadmin;

			//Filtering group attributes
			if(rg.getAttributes() != null) {
				List<Attribute> groupAttributes = rg.getAttributes();
				List<Attribute> allowedGroupAttributes = new ArrayList<Attribute>();
				for(Attribute groupAttr: groupAttributes) {
					//if there is record in contextMap, use it
					if(contextMap.containsKey(key + groupAttr.getName())) {
						Boolean isWritable = contextMap.get(key + groupAttr.getName());
						if(isWritable != null) {
							groupAttr.setWritable(isWritable);
							allowedGroupAttributes.add(groupAttr);
						}
						// no READ for attribute
					} else {
						//if not, get information about authz rights and set record to contextMap
						if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, groupAttr, rg, null)) {
							boolean isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttr, rg, null);
							groupAttr.setWritable(isWritable);
							allowedGroupAttributes.add(groupAttr);
							contextMap.put(key + groupAttr.getName(), isWritable);
						} else {
							contextMap.put(key + groupAttr.getName(), null);
						}
					}
				}
				rg.setAttributes(allowedGroupAttributes);
			}
			filteredRichGroups.add(rg);
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

	public List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames)throws InternalErrorException{
		return convertGroupsToRichGroupsWithAttributes(sess, this.getAllSubGroups(sess, parentGroup), attrNames);
	}

	public RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames)throws InternalErrorException, GroupNotExistsException{
		return convertGroupToRichGroupWithAttributesByName(sess, this.getGroupById(sess, groupId), attrNames);
	}

	public void saveInformationAboutGroupSynchronization(PerunSession sess, Group group, long startTime, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		//get current timestamp of this synchronization
		Date currentTimestamp = new Date();
		String originalExceptionMessage = exceptionMessage;
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
		//else trim the message on 1000 characters if not null
		} else if (exceptionMessage != null && exceptionMessage.length() > 1000) {
			exceptionMessage = exceptionMessage.substring(0, 1000) + " ... message is too long, other info is in perun log file. If needed, please ask perun administrators.";
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
			String lastSuccessSync = AttributesManager.NS_GROUP_ATTR_DEF + ":lastSuccessSynchronizationTimestamp";
			// Make string from start of synchronization in correct format
			String startOfSyncString = BeansUtils.getDateFormatter().format(new Date(startTime));
			try {
				// Create attribute with last success synchronization timestamp
				Attribute lastSuccessSynchronizationTimestamp = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, lastSuccessSync));
				lastSuccessSynchronizationTimestamp.setValue(correctTimestampString);
				attrsToSet.add(lastSuccessSynchronizationTimestamp);
			} catch (AttributeNotExistsException ex) {
				log.error("Can't save lastSuccessSynchronizationTimestamp, because there is missing attribute with name {}", lastSuccessSync);
			}
			try {
				// Create attribute with start of last success synchronization timestamp
				Attribute startOfLastSuccessSynchronization = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, GroupsManager.GROUPSTARTOFLASTSUCCESSSYNC_ATTRNAME));
				startOfLastSuccessSynchronization.setValue(startOfSyncString);
				attrsToSet.add(startOfLastSuccessSynchronization);
			} catch (AttributeNotExistsException ex) {
				log.error("Can't save startOfLastSuccessSynchronizationTimestamp, because there is missing attribute with name {}", GroupsManager.GROUPSTARTOFLASTSUCCESSSYNC_ATTRNAME);
			}
		} else {
			//Log to auditer_log that synchronization failed or finished with some errors
			if(failedDueToException) {
				getPerunBl().getAuditer().log(sess, "{} synchronization failed because of {}.", group, originalExceptionMessage);
			} else {
				getPerunBl().getAuditer().log(sess, "{} synchronization finished with errors: {}.", group, originalExceptionMessage);
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
	 * For lightweight synchronization prepare candidate to add and members to remove.
	 *
	 * Get all subjects from loginSource and try to find users in Perun by their login and this ExtSource.
	 * If found, look if this user is already in synchronized Group. If yes skip him, if not add him to candidateToAdd
	 * If not found, add him to candidatesToAdd (from source itself or from memberSource if they are different)
	 *
	 * Rest of former members need to be add to membersToRemove to remove them from group.
	 *
	 * This method fill 2 member structures which gets as parameters:
	 * 1. candidatesToAdd - New members of the group
	 * 2. membersToRemove - Former members who are not in synchronized ExtSource now
	 *
	 * @param sess
	 * @param loginSource
	 * @param memberSource
	 * @param candidatesToAdd
	 * @param membersToRemove
	 * @param skippedMembers
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 */
	private void categorizeMembersForLightweightSynchronization(PerunSession sess, ExtSource loginSource, ExtSource memberSource, List<RichMember> actualGroupMembers, List<Candidate> candidatesToAdd, List<RichMember> membersToRemove, List<String> skippedMembers, List<Map<String,String>> subjects) throws InternalErrorException, ExtSourceNotExistsException {
		// Create map where key is login of member in loginSource and value is RichMember
		Map<String, RichMember> loginsOfMembers = getMapOfLoginsAndRichMembers(loginSource, actualGroupMembers);

		// Try to find users by login
		for (Map<String, String> subject: subjects) {
			String login = subject.get("login");
			// Skip subjects, which doesn't have login
			if (login == null || login.isEmpty()) {
				log.debug("Subject {} doesn't contain attribute login, skipping.", subject);
				skippedMembers.add("MemberEntry:[" + subject + "] was skipped because login is missing");
				continue;
			}

			User user = null;
			Candidate candidate = null;
			try {
				// If login is not in map of actual members, get user and userExtSource from Perun
				if (!loginsOfMembers.containsKey(login)) {
					UserExtSource userExtSource = getPerunBl().getUsersManagerBl().getUserExtSourceByExtLogin(sess, memberSource, login);
					user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, userExtSource);

					candidate = new Candidate(user, userExtSource);
					// For lightweight synchronization we want to skip all updates of attributes
					candidate.setAttributes(new HashMap<>());
				} else {
					// Get user from richMember, if he is member of group
					user = loginsOfMembers.get(login).getUser();
				}
			} catch (UserExtSourceNotExistsException | UserNotExistsException ex) {
				// If not find, get more information about him from member extSource
				candidate = convertSubjectToCandidate(sess, subject, memberSource, loginSource, skippedMembers);
			}

			// If user is not null now, we found it so we can use it from Perun, in other case he is not in Perun at all
			if(user != null && candidate == null) {
				// We can skip this one, because he is already in group, and remove him from the map
				// But first we need to also validate him if he was disabled before (invalidate and then validate)
				RichMember richMember = loginsOfMembers.get(login);
				if(richMember != null && Status.DISABLED.equals(richMember.getStatus())) {
					getPerunBl().getMembersManagerBl().invalidateMember(sess, richMember);
					try {
						getPerunBl().getMembersManagerBl().validateMember(sess, richMember);
					} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
						log.info("Switching member id {} into INVALID state from DISABLED, because there was problem with attributes {}.", richMember.getId(), e);
					}
				}
				loginsOfMembers.remove(login);
			} else if (candidate != null) {
				candidatesToAdd.add(candidate);
			} else {
				// Both null means that we can't find subject by login in extSource at all (will be in skipped members)
				log.debug("Subject with login {} was skipped because can't be found in extSource {}.", login, memberSource);
			}
		}
		// Rest of them need to be removed
		membersToRemove.addAll(loginsOfMembers.values());
	}

	/**
	 * This method fill 3 member structures which get as parameters:
	 * 1. membersToUpdate - Candidates with equivalent Members from Perun for purpose of updating attributes and statuses
	 * 2. candidatesToAdd - New members of the group
	 * 3. membersToRemove - Former members who are not in synchronized ExtSource now
	 *
	 * @param sess
	 * @param loginSource
	 * @param memberSource
	 * @param actualGroupMembers
	 * @param group
	 * @param skippedMembers
	 * @param subjects
	 *
	 * @param candidatesToAdd 1. container (more above)
	 * @param membersToUpdate 2. container (more above)
	 * @param membersToRemove 3. container (more above)
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 */
	private void categorizeMembersForSynchronization(PerunSession sess, String status, ExtSource loginSource, ExtSource memberSource, List<RichMember> actualGroupMembers,
								  Group group, List<Candidate> candidatesToAdd, Map<Candidate, RichMember> membersToUpdate, List<RichMember> membersToRemove,
								  List<String> skippedMembers, List<Map<String,String>> subjects, List<String> overwriteUserAttributesList) throws InternalErrorException, ExtSourceNotExistsException {

		// Create map where key is login of member in loginSource and value is RichMember
		Map<String, RichMember> loginsOfMembers = getMapOfLoginsAndRichMembers(loginSource, actualGroupMembers);

		// Try to find users by login
		for (Map<String, String> subject: subjects) {
			String login = subject.get("login");
			// Skip subjects, which doesn't have login
			if (login == null || login.isEmpty()) {
				log.debug("Subject {} doesn't contain attribute login, skipping.", subject);
				skippedMembers.add("MemberEntry:[" + subject + "] was skipped because login is missing");
				continue;
			}

			Candidate candidate = null;
			RichMember richMember = null;
			try {
				if (!loginsOfMembers.containsKey(login)) {
					// If login is not in map of actual members, create candidate from subject
					candidate = convertSubjectToCandidate(sess, subject, memberSource, loginSource, skippedMembers);
					if (candidate != null) {
						candidatesToAdd.add(candidate);
					}
				} else {
					// Get hash code of richMember
					richMember = loginsOfMembers.get(login);
					Attribute hashCodeMember = getPerunBl().getAttributesManagerBl().getAttribute(sess, richMember, group, MembersManager.MEMBERGROUPHASHCODE_ATTRNAME);

					// If subject has all attributes and memberSource is not different, we can hash subject and compare,
					// if its different from previous synchronization
					if (loginSource.equals(memberSource) && memberSource instanceof ExtSourceApi) {
						// Count hash code of subject
						String hashCode = Integer.toString(subject.hashCode());

						// Compare hash code of subject and member
						if (!hashCode.equals(hashCodeMember.getValue())) {
							// If they are different, create candidate and put him to membersToUpdate
							candidate = convertSubjectToCandidate(sess, subject, memberSource, loginSource, skippedMembers);
							if (candidate != null) {
								membersToUpdate.put(candidate, richMember);
							}
						}
					} else {
						// If we don't have subject with attributes, get him and convert him to candidate
						candidate = convertSubjectToCandidate(sess, subject, memberSource, loginSource, skippedMembers);
						if (candidate != null) {
							// Get hashCode of subject with attributes from candidate
							String hashCodeSubject = candidate.getAttributes().get(MembersManager.MEMBERGROUPHASHCODE_ATTRNAME);
							// Compare hash code of subject and member
							if (hashCodeSubject != null && !hashCodeSubject.equals(hashCodeMember.getValue())) {
								// If they are different, put candidate to membersToUpdate
								membersToUpdate.put(candidate, richMember);
							}
						}
					}
					// Remove this login from map
					loginsOfMembers.remove(login);
				}
			} catch (AttributeNotExistsException e) {
				// Attribute does not exists yet, create candidate and add him to members to update
				log.info("Attribute " + MembersManager.MEMBERGROUPHASHCODE_ATTRNAME + " does not exist for member: " + richMember + ", group: " + group + ", login: " + login);
				if (candidate == null) {
					candidate = convertSubjectToCandidate(sess, subject, memberSource, loginSource, skippedMembers);
				}
				if (candidate != null) {
					membersToUpdate.put(candidate, richMember);
				}
				// Remove this login from map
				loginsOfMembers.remove(login);
			} catch (WrongAttributeAssignmentException e) {
				// Should not happen
				throw new InternalErrorException(e);
			}
		}

		// Rest of them need to be removed, if its full synchronization
		if (status.equals(GroupsManager.GROUP_SYNC_STATUS_FULL)) {
			membersToRemove.addAll(loginsOfMembers.values());
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
	 * Fill list of subjects, where subject is map of attribute names and attribute values.
	 * Every subject is structure for creating Candidate from ExtSource.
	 *
	 * Returns string with status of synchronization:
	 * 		- full - all subjects from extSource were obtained
	 *		- modified - only modified subjects from extSource were obtained
	 *
	 * @param sess
	 * @param source to get subjects from
	 * @param group to be synchronized
	 *
	 * @param subjects to fill
	 * @return string with status of synchronization
	 *
	 * @throws InternalErrorException if internal error occurs
	 */
	private String getSubjectsFromExtSource(PerunSession sess, ExtSource source, Group group, String status,
											List<Map<String, String>> subjects) throws InternalErrorException {
		// Get subjects in form of list of maps where left string is name of attribute and right string is value of attribute.
		// Every subject is one map
		String result;
		try {
			result = ((ExtSourceSimpleApi) source).getGroupSubjects(sess, group, status, subjects);
			log.debug("Group synchronization {}: external group contains {} members.", group, subjects.size());
		} catch (ExtSourceUnsupportedOperationException e2) {
			throw new InternalErrorException("ExtSource " + source.getName() + " doesn't support getGroupSubjects", e2);
		}
		return result;
	}

	/**
	 * Convert List of subjects to list of Candidates.
	 * This method calls convertSubjectToCandidate method for each subject.
	 *
	 * @param sess
	 * @param subjects list of subjects from ExtSource (at least login should be here)
	 * @param membersSource optional member ExtSource (if members attributes are from other source then their logins)
	 * @param source default group ExtSource
	 * @param skippedMembers not successfully synchronized members are skipped and information about it should be added here
	 *
	 * @return list of successfully created candidates from subjects
	 *
	 * @throws InternalErrorException if some internal error occurs
	 * @throws ExtSourceNotExistsException if membersSource not exists in Perun
	 */
	private List<Candidate> convertSubjectsToCandidates(PerunSession sess, List<Map<String, String>> subjects, ExtSource membersSource, ExtSource source, List<String> skippedMembers) throws InternalErrorException, ExtSourceNotExistsException {
		List<Candidate> candidates = new ArrayList<>();
		for (Map<String, String> subject: subjects) {
			Candidate candidate = convertSubjectToCandidate(sess, subject, membersSource, source, skippedMembers);
			if (candidate != null) {
				candidates.add(candidate);
			}
		}
		return candidates;
	}

	/**
	 * Convert subject to candidate.
	 *
	 * To getting Candidate can use 1 of 3 possible options:
	 * 1] membersSource and source are not equals => we have just login, other attributes neet to get from membersSource
	 * 2] membersSource==source and membersSource is instance of ExtSourceApi => we already have all attributes in subject
	 * 3] membersSource==source and membersSource is instance of SimpleExtSourceApi => we have just login, need to read other attributes again
	 *
	 * If candidate cannot be get for some reason, add this reason to skippedMembers list and skip him.
	 *
	 * @param sess
	 * @param subject subject from ExtSource
	 * @param membersSource optional member ExtSource (if members attributes are from other source then login)
	 * @param source default group ExtSource
	 * @param skippedMembers not successfully synchronized members are skipped and information about it should be added here
	 *
	 * @return candidate or null, if conversion failed
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceNotExistsException
	 */
	private Candidate convertSubjectToCandidate(PerunSession sess, Map<String, String> subject, ExtSource membersSource, ExtSource source, List<String> skippedMembers) throws InternalErrorException, ExtSourceNotExistsException {
		String login = subject.get("login");
		// Skip subjects, which doesn't have login
		if (login == null || login.isEmpty()) {
			log.debug("Subject {} doesn't contain attribute login, skipping.", subject);
			skippedMembers.add("MemberEntry:[" + subject + "] was skipped because login is missing");
			return null;
		}
		Candidate candidate = null;
		try {
			// One of three possible ways should happen to get Candidate
			// 1] Sources of login and other attributes are not same
			if(!membersSource.equals(source)) {
				// Need to read attributes from the new memberSource, we can't use locally data there (there are from other extSource)
				candidate = getPerunBl().getExtSourcesManagerBl().getCandidate(sess, membersSource, login);
				// 2] Sources are same and we work with source which is instance of ExtSourceApi
			} else if (membersSource instanceof ExtSourceApi) {
				// We can use the data from this source without reading them again (all exists in the map of subject attributes)
				candidate = getPerunBl().getExtSourcesManagerBl().getCandidate(sess, subject, membersSource, login);
				// 3] Sources are same and we work with source which is instance of ExtSourceSimpleApi
			} else if (membersSource instanceof ExtSourceSimpleApi) {
				// We can't use the data from this source, we need to read them again (they are not in the map of subject attributes)
				candidate = getPerunBl().getExtSourcesManagerBl().getCandidate(sess, membersSource, login);
			} else {
				// This could not happen without change in extSource API code
				throw new InternalErrorException("ExtSource is other instance than SimpleApi or Api and this is not supported!");
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
		return candidate;
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
		List<AttributeDefinition> attrDefs = new ArrayList<>();
		// Iterate through all subject attributes
		for(Candidate candidate: membersToUpdate.keySet()) {
			RichMember richMember = membersToUpdate.get(candidate);

			// If member not exists in this moment (somebody remove him before start of updating), skip him and log it
			try {
				getPerunBl().getMembersManagerBl().checkMemberExists(sess, richMember);
			} catch (MemberNotExistsException ex) {
				// Log it and skip this member
				log.debug("Someone removed member {} from group {} before updating process. Skip him.", richMember, group);
				continue;
			}

			// Load attrDefinitions just once for first candidate
			if(attrDefs.isEmpty()) {
				for(String attrName : candidate.getAttributes().keySet()) {
					try {
						AttributeDefinition attrDef = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrName);
						attrDefs.add(attrDef);
					} catch (AttributeNotExistsException ex) {
						log.error("Can't synchronize attribute " + attrName + " for candidate " + candidate + " and for group " + group);
						// Skip this attribute
					}
				}
			}

			// Get RichMember with attributes
			richMember = getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, Arrays.asList(richMember), attrDefs).get(0);

			overwriteUserCoreAttributes(sess, richMember, candidate, overwriteUserAttributesList);
			updateAttributes(sess, richMember, group, candidate, overwriteUserAttributesList);

			// Synchronize userExtSources (add not existing)
			for (UserExtSource ues : candidate.getUserExtSources()) {
				if (!getPerunBl().getUsersManagerBl().userExtSourceExists(sess, ues)) {
					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, richMember.getUser(), ues);
					} catch (UserExtSourceExistsException e) {
						throw new ConsistencyErrorException("Adding already existing userExtSource " + ues, e);
					}
				}
			}

			setMemberStatus(sess, richMember);
		}
	}

	/**
	 * Update user core attributes mentioned in list of attributes to overwrite.
	 *
	 * @param sess
	 * @param richMember Member to gain user from
	 * @param candidate Candidate with new values of attributes
	 * @param overwriteUserAttributesList List of attributes which will be overwrite
	 * @throws InternalErrorException
	 */
	private void overwriteUserCoreAttributes(PerunSession sess, RichMember richMember, Candidate candidate, List<String> overwriteUserAttributesList) throws InternalErrorException {
		// Try to find user core attributes and update user -> update name and titles
		if (overwriteUserAttributesList != null) {
			boolean someFound = false;
			User user = richMember.getUser();
			for (String attrName : overwriteUserAttributesList) {
				if (attrName.startsWith(AttributesManager.NS_USER_ATTR_CORE+":firstName")) {
					user.setFirstName(candidate.getFirstName());
					someFound = true;
				} else if (attrName.startsWith(AttributesManager.NS_USER_ATTR_CORE+":middleName")) {
					user.setMiddleName(candidate.getMiddleName());
					someFound = true;
				} else if (attrName.startsWith(AttributesManager.NS_USER_ATTR_CORE+":lastName")) {
					user.setLastName(candidate.getLastName());
					someFound = true;
				} else if (attrName.startsWith(AttributesManager.NS_USER_ATTR_CORE+":titleBefore")) {
					user.setTitleBefore(candidate.getTitleBefore());
					someFound = true;
				} else if (attrName.startsWith(AttributesManager.NS_USER_ATTR_CORE+":titleAfter")) {
					user.setTitleAfter(candidate.getTitleAfter());
					someFound = true;
				}
			}
			if (someFound) {
				try {
					perunBl.getUsersManagerBl().updateUser(sess, user);
				} catch (UserNotExistsException e) {
					throw new ConsistencyErrorException("User from perun not exists when should - removed during sync.", e);
				}
			}
		}
	}

	/**
	 * Method which will update attributes of RichMember with new values stored in Candidate.
	 *
	 * For Member attributes - updateAttributes.
	 * For User attributes - updateAttributes if exists in list of overwriteUserAttributesList, in other case just mergeAttributes.
	 *
	 * updateAttributes = store new values
	 * mergeAttributes = for List and Map add new values, do not remove old one, for other cases store new values (like String, Integer etc.)
	 *
	 * @param sess
	 * @param richMember Member with attributes, which will be updated
	 * @param group Group where richMember is member
	 * @param candidate Candidate with new values of attributes
	 * @param overwriteUserAttributesList List of attributes to be overwrite
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 */
	private void updateAttributes(PerunSession sess, RichMember richMember, Group group, Candidate candidate, List<String> overwriteUserAttributesList) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		for (String attributeName : candidate.getAttributes().keySet()) {
			// Update member attribute
			if(attributeName.startsWith(AttributesManager.NS_MEMBER_ATTR)) {
				boolean attributeFound = false;
				for (Attribute memberAttribute: richMember.getMemberAttributes()) {
					if(memberAttribute.getName().equals(attributeName)) {
						attributeFound = true;
						Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), memberAttribute.getType());
						if (subjectAttributeValue != null && !Objects.equals(memberAttribute.getValue(), subjectAttributeValue)) {
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
						// We found it, but there is no change;
						break;
					}
				}
				// Member has not set this attribute so set it now if possible
				if(!attributeFound) {
					// FIXME - this whole section probably can be removed. Previously null attributes were not retrieved with member
					// FIXME - they are now always present, if not the same, then they are set in a code above.
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
			// Update user attribute
			} else if(attributeName.startsWith(AttributesManager.NS_USER_ATTR)) {
				boolean attributeFound = false;
				for (Attribute userAttribute: richMember.getUserAttributes()) {
					if(userAttribute.getName().equals(attributeName)) {
						attributeFound = true;
						Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), userAttribute.getType());
						if (!Objects.equals(userAttribute.getValue(), subjectAttributeValue)) {
							log.trace("Group synchronization {}: value of the attribute {} for memberId {} changed. Original value {}, new value {}.",
									new Object[] {group, userAttribute, richMember.getId(), userAttribute.getValue(), subjectAttributeValue});
							userAttribute.setValue(subjectAttributeValue);
							try {
								// Choose set or merge by extSource attribute overwriteUserAttributes (if contains this one)
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
						// We found it, but there is no change
						break;
					}
				}
				// User has not set this attribute so set it now if
				if(!attributeFound) {
					// FIXME - this whole section probably can be removed. Previously null attributes were not retrieved with member
					// FIXME - they are now always present, if not the same, then they are set in a code above.
					Attribute newAttribute = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName));
					Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), newAttribute.getType());
					newAttribute.setValue(subjectAttributeValue);
					try {
						// Try to set user's attributes
						getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember.getUser(), newAttribute);
						log.trace("Group synchronization: Setting the {} value {}", newAttribute, candidate.getAttributes().get(attributeName));
					} catch (AttributeValueException e) {
						// There is a problem with attribute value, so set INVALID status for the member
						getPerunBl().getMembersManagerBl().invalidateMember(sess, richMember);
					}
				}
			// If it is attribute with hash code of data gained from external source, update the hash code attribute of member-group relationship
			} else if(attributeName.equals(MembersManager.MEMBERGROUPHASHCODE_ATTRNAME)) {
				AttributeDefinition attributeDefinition = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, MembersManager.MEMBERGROUPHASHCODE_ATTRNAME);
				Attribute hashCode = new Attribute(attributeDefinition);
				hashCode.setValue(candidate.getAttributes().get(attributeName));
				if (hashCode.getValue() != null && !hashCode.getValue().equals("")) {
					// Set hash code to member-group relationship
					try {
						getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember, group, hashCode);
						log.trace("Group synchronization: Setting the {} hashCode {} to member {} in group {}", new Object[] {hashCode, richMember, group});
					} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
						log.error("Group synchronization: Attribute with hash code {} can't be set to member {} in group {}", new Object[] {hashCode, richMember, group});
					}
				}
			} else {
				// We are not supporting other attributes than member or user so skip it without error, but log it
				log.error("Attribute {} can't be set, because it is not member or user attribute.", attributeName);
			}
		}
	}

	/**
	 * Get map where key is login of member in ExtSource and value is RichMember
	 *
	 * @param loginSource
	 * @param actualGroupMembers
	 *
	 * @return map of logins and rich members
	 */
	private Map<String, RichMember> getMapOfLoginsAndRichMembers(ExtSource loginSource, List<RichMember> actualGroupMembers) {
		// Create map where key is login of member in external source and value is RichMember
		Map<String, RichMember> loginsOfMembers = new HashMap<>();
		for (RichMember actualMember: actualGroupMembers) {
			List<UserExtSource> userExtSources = actualMember.getUserExtSources();
			for (UserExtSource ues: userExtSources) {
				if (ues.getExtSource().equals(loginSource)) {
					loginsOfMembers.put(ues.getLogin(), actualMember);
					break;
				}
			}
		}
		return loginsOfMembers;
	}

	/**
	 * Set correct status of member.
	 *
	 * @param sess
	 * @param richMember Member whose status will be updated
	 * @throws WrongAttributeAssignmentException
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	private void setMemberStatus(PerunSession sess, RichMember richMember) throws WrongAttributeAssignmentException, InternalErrorException, AttributeNotExistsException {
		// Set correct member Status
		// If the member has expired or disabled status, try to expire/validate him (depending on expiration date)
		if (richMember.getStatus().equals(Status.DISABLED) || richMember.getStatus().equals(Status.EXPIRED)) {
			Date now = new Date();
			Attribute membershipExpiration = getPerunBl().getAttributesManagerBl().getAttribute(sess, richMember, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
			if(membershipExpiration.getValue() != null) {
				try {
					Date currentMembershipExpirationDate = BeansUtils.getDateFormatterWithoutTime().parse((String) membershipExpiration.getValue());
					if (currentMembershipExpirationDate.before(now)) {
						// Disabled members which are after expiration date will be expired
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
						// Disabled and expired members which are before expiration date will be validated
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
	private void addMissingMembersWhileSynchronization(PerunSession sess, Group group, List<Candidate> candidatesToAdd, List<String> overwriteUserAttributesList, List<String> skippedMembers) throws InternalErrorException, GroupOperationsException {
		// Now add missing members
		for (Candidate candidate: candidatesToAdd) {
			Member member = null;
			User user = null;

			try {
				// Check if candidate represents duplicated user
				try {
					checkDuplicationOfUser(sess, candidate);
				} catch (UserDuplicateException e) {
					// If we have duplicated user, log it and continue with next candidate
					log.info("Duplication of Users detected by synchronizing candidate [{}]. User duplicate exception was thrown [{}].", candidate, e);
					continue;
				}
				// Check if the member is already in the VO (just not in the group)
				member = getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate.getUserExtSources());

				// member exists - update attributes
				Map<Candidate,RichMember> memberMap = new HashMap<>();
				memberMap.put(candidate, getPerunBl().getMembersManagerBl().getRichMember(sess, member));
				try {
					updateExistingMembersWhileSynchronization(sess, group, memberMap, overwriteUserAttributesList);
				} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
					// if update fails, skip him
					log.warn("Can't update member from candidate {} due to attribute value exception {}.", candidate, e);
					skippedMembers.add("MemberEntry:[" + candidate + "] was skipped because there was problem when updating member from candidate: Exception: " + e.getName() + " => '" + e.getMessage() + "'");
					continue;
				}

			} catch (MemberNotExistsException e) {
				try {
					// We have new member (candidate), so create him using synchronous createMember (and overwrite chosed user attributes)
 					member = getPerunBl().getMembersManagerBl().createMemberSync(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate, null, overwriteUserAttributesList);
					log.info("Group synchronization {}: New member id {} created during synchronization.", group, member.getId());
				} catch (AlreadyMemberException e1) {
					//Probably race condition, give him another chance to fix this mess
					// Check if the member is already in the VO (just not in the group)
					try {
						member = getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate.getUserExtSources());
						// member exists - update attribute
						Map<Candidate,RichMember> memberMap = new HashMap<>();
						memberMap.put(candidate, getPerunBl().getMembersManagerBl().getRichMember(sess, member));
						try {
							updateExistingMembersWhileSynchronization(sess, group, memberMap, overwriteUserAttributesList);
						} catch (WrongAttributeAssignmentException | AttributeNotExistsException e2) {
							// if update fails, skip him
							log.warn("Can't update member from candidate {} due to attribute value exception {}.", candidate, e);
							skippedMembers.add("MemberEntry:[" + candidate + "] was skipped because there was problem when updating member from candidate: Exception: " + e.getName() + " => '" + e2.getMessage() + "'");
							continue;
						}
					} catch (Exception e2) {
						//Something is still wrong, thrown consistency exception
						throw new ConsistencyErrorException("Trying to add existing member (it is not possible to get him by userExtSource even if is also not possible to create him in DB)!");
					}
				} catch (AttributeValueException e1) {
					log.warn("Can't create member from candidate {} due to attribute value exception {}.", candidate, e1);
					skippedMembers.add("MemberEntry:[" + candidate + "] was skipped because there was problem when createing member from candidate: Exception: " + e1.getName() + " => '" + e1.getMessage() + "'");
					continue;
				} catch (ExtendMembershipException e1) {
					log.warn("Can't create member from candidate {} due to membership expiration exception {}.", candidate, e1);
					skippedMembers.add("MemberEntry:[" + candidate + "] was skipped because membership expiration: Exception: " + e1.getName() + " => " + e1.getMessage() + "]");
					continue;
				}

			}

			try {
				// Add the member to the group, do not add members to the generic members group
				if (!group.getName().equals(VosManager.MEMBERS_GROUP)) {
					Attribute hashCode = null;
					try {
						getPerunBl().getGroupsManagerBl().addMember(sess, group, member);
						// Get hash code of attributes gained from external source
						String value = candidate.getAttributes().get(MembersManager.MEMBERGROUPHASHCODE_ATTRNAME);
						if (value != null && !value.isEmpty()) {
							AttributeDefinition attributeDefinition;
							attributeDefinition = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, MembersManager.MEMBERGROUPHASHCODE_ATTRNAME);
							hashCode = new Attribute(attributeDefinition);
							hashCode.setValue(value);
							// Set hash code to member-group relationship
							getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, member, group, hashCode);
						}
					} catch(NotMemberOfParentGroupException | GroupNotExistsException ex) {
						// Shouldn't happen, because every group has at least Members group as a parent
						// Shouldn't happen, group should always exist
						throw new ConsistencyErrorException(ex);
					} catch (WrongAttributeAssignmentException ex) {
						throw new InternalErrorException("Attribute " + hashCode + " cannot be assigned to member - group relationship. Member:  " + member + " and group: " + group, ex);
					} catch (AttributeNotExistsException ex) {
						throw new InternalErrorException("Attribute " + hashCode + " does not exist.", ex);
					}
				}
				log.info("Group synchronization {}: New member id {} added.", group, member.getId());
			} catch (AlreadyMemberException e) {
				// This part is ok, it means someone add member before synchronization ends, log it and skip this member
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
	private void removeFormerMembersWhileSynchronization(PerunSession sess, Group group, List<RichMember> membersToRemove) throws InternalErrorException, WrongAttributeAssignmentException, MemberAlreadyRemovedException, GroupOperationsException, GroupNotExistsException {
		// First get information if this group is authoritative group
		boolean thisGroupIsAuthoritativeGroup = false;
		try {
			Attribute authoritativeGroupAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_D_AUTHORITATIVE_GROUP);
			if(authoritativeGroupAttr.getValue() != null) {
				Integer authoritativeGroupValue = (Integer) authoritativeGroupAttr.getValue();
				if(authoritativeGroupValue == 1) thisGroupIsAuthoritativeGroup = true;
			}
		} catch (AttributeNotExistsException ex) {
			// Means that this group is not authoritative
			log.error("Attribute {} doesn't exists.", A_G_D_AUTHORITATIVE_GROUP);
		}

		// Second remove members (use authoritative group where is needed)
		for (RichMember member: membersToRemove) {
			// Member is missing in the external group, so remove him from the Perun group
			try {
				// Members group
				if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
					// If the group is members group, the member must be disabled as a member of VO
					try {
						getPerunBl().getMembersManagerBl().disableMember(sess, member);
						log.info("Group synchronization {}: Member id {} disabled.", group, member.getId());
					} catch(MemberNotValidYetException ex) {
						// Member is still invalid in perun. We can delete him.
						getPerunBl().getMembersManagerBl().deleteMember(sess, member);
						log.info("Group synchronization {}: Member id {} would have been disabled but he has been deleted instead because he was invalid.", group, member.getId());
					}
					// Not members group
				} else {
					// If this group is authoritative group, check if this is last authoritative group of this member
					// If Yes = deleteMember (from Vo), if No = only removeMember
					if(thisGroupIsAuthoritativeGroup) {
						List<Group> memberAuthoritativeGroups = null;
						try {
							memberAuthoritativeGroups = getAllAuthoritativeGroupsOfMember(sess, member);
						} catch (AttributeNotExistsException ex) {
							// This means that no authoritative group can exists without this attribute
							log.error("Attribute {} doesn't exists.", A_G_D_AUTHORITATIVE_GROUP);
						}

						// If list of member authoritativeGroups is not null, attribute exists
						if(memberAuthoritativeGroups != null) {
							memberAuthoritativeGroups.remove(group);
							if(memberAuthoritativeGroups.isEmpty()) {
								// First try to disable member, if is invalid, delete him from Vo
								try {
									getPerunBl().getMembersManagerBl().disableMember(sess, member);
									log.info("Group synchronization {}: Member id {} disabled because synchronizer wants to remove him from last authoritativeGroup in Vo.", group, member.getId());
									getPerunBl().getGroupsManagerBl().removeMember(sess, group, member);
									log.info("Group synchronization {}: Member id {} removed.", group, member.getId());
								} catch(MemberNotValidYetException ex) {
									// Member is still invalid in perun. We can delete him.
									getPerunBl().getMembersManagerBl().deleteMember(sess, member);
									log.info("Group synchronization {}: Member id {} would have been disabled but he has been deleted instead because he was invalid and synchronizer wants to remove him from last authoritativeGroup in Vo.", group, member.getId());
								}
							} else {
								// If there is still some other authoritative group for this member, only remove him from group
								getPerunBl().getGroupsManagerBl().removeMember(sess, group, member);
								log.info("Group synchronization {}: Member id {} removed.", group, member.getId());
							}
							// If list of member authoritativeGroups is null, attribute not exists, only remove member from Group
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
				// Member was probably removed before starting of synchronization removing process, log it and skip this member
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
		// Close open extSources (not empty ones) if they support this operation
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

	@Override
	public void processRelationMembers(PerunSession sess, Group resultGroup, List<Member> changedMembers, int sourceGroupId, boolean addition) throws GroupOperationsException {

		List<Member> newMembers;

		try {
			if (addition) {
				newMembers = addIndirectMembers(sess, resultGroup, changedMembers, sourceGroupId);
			} else {
				newMembers = removeIndirectMembers(sess, resultGroup, changedMembers, sourceGroupId);
			}

			if (newMembers.isEmpty()) {
				return;
			}

			List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, resultGroup.getId());
			for (Integer groupId : relations) {
				processRelationMembers(sess, groupsManagerImpl.getGroupById(sess, groupId), newMembers, resultGroup.getId(), addition);
			}
		} catch (WrongReferenceAttributeValueException | WrongAttributeValueException | AlreadyMemberException |
				InternalErrorException | NotGroupMemberException | GroupNotExistsException ex) {
			throw new GroupOperationsException(ex);
		}
	}

	@Override
	public Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws GroupOperationsException, InternalErrorException, GroupRelationAlreadyExists, GroupRelationNotAllowed {

		// block inclusion to members group, since it doesn't make sense
		// allow inclusion of members group, since we want to delegate privileges on assigning all vo members to some service for group manager.
		if(resultGroup.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new GroupRelationNotAllowed("Union cannot be created when result group " + resultGroup + " is members group.");
		}

		// check if both groups are from same VO
		if (resultGroup.getVoId() != operandGroup.getVoId()) {
			throw new GroupRelationNotAllowed("Union cannot be created on groups: " + resultGroup + ", " + operandGroup + ". They are not from the same VO.");
		}

		// check if result group is the same as operand group
		if (resultGroup.getId() == operandGroup.getId()) {
			throw new GroupRelationNotAllowed("Result group " + resultGroup + " cannot be the same as operand group " + operandGroup);
		}

		// check if there is already a record of these two groups
		if (this.groupsManagerImpl.isRelationBetweenGroups(resultGroup, operandGroup)) {
			throw new GroupRelationAlreadyExists("There is already a relation defined between result group " + resultGroup +
					" and operand group " + operandGroup + " or they are in hierarchical structure.");
		}

		// check cycle between groups
		if (checkGroupsCycle(sess, resultGroup.getId(), operandGroup.getId())) {
			throw new GroupRelationNotAllowed("Union between result group " + resultGroup + " and operand group " + operandGroup + " would create group cycle.");
		}

		// save group relation
		groupsManagerImpl.saveGroupRelation(sess, resultGroup, operandGroup, parentFlag);

		// do the operation logic
		processRelationMembers(sess, resultGroup, getGroupMembers(sess, operandGroup), operandGroup.getId(), true);

		return resultGroup;
	}

	@Override
	public void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws GroupOperationsException, InternalErrorException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		if (!groupsManagerImpl.isOneWayRelationBetweenGroups(resultGroup, operandGroup)) {
			throw new GroupRelationDoesNotExist("Union does not exist between result group " + resultGroup + " and operand group" + operandGroup + ".");
		}

		if (parentFlag || groupsManagerImpl.isRelationRemovable(sess, resultGroup, operandGroup)) {
			processRelationMembers(sess, resultGroup, getGroupMembers(sess, operandGroup), operandGroup.getId(), false);
		} else {
			throw new GroupRelationCannotBeRemoved("Union between result group " + resultGroup + " and operand group" + operandGroup +
					" cannot be removed, because it's part of the hierarchical structure of the groups.");
		}

		groupsManagerImpl.removeGroupUnion(sess, resultGroup, operandGroup);
	}

	@Override
	public List<Group> getGroupUnions(PerunSession session, Group group, boolean reverseDirection) throws InternalErrorException {
		if (reverseDirection) {
			return groupsManagerImpl.getResultGroups(session, group.getId());
		} else {
			return groupsManagerImpl.getOperandGroups(session, group.getId());
		}
	}

	/**
	 * Check if cycle would be created by adding union between these groups.
	 *
	 * @param sess perun session
	 * @param resultGroupId result group id
	 * @param operandGroupId operand group id
	 * @return true if cycle would be created; false otherwise
	 * @throws InternalErrorException
	 */
	private boolean checkGroupsCycle(PerunSession sess, int resultGroupId, int operandGroupId) throws InternalErrorException {
		List<Integer> groupsIds = groupsManagerImpl.getResultGroupsIds(sess, resultGroupId);

		if (groupsIds.contains(operandGroupId)) {
			return true;
		}

		for (Integer id: groupsIds) {
			if(checkGroupsCycle(sess, id, operandGroupId)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if candidate represents duplicated User.
	 * If yes, the UserDuplicateException will be thrown. Nothing will happen otherwise.
	 *
	 * @param sess
	 * @param candidate
	 * @throws InternalErrorException
	 * @throws UserDuplicateException
	 */
	private void checkDuplicationOfUser(PerunSession sess, Candidate candidate) throws InternalErrorException, UserDuplicateException {
		User user = null;
		// Check if user does not exist in Perun multiple times
		if (candidate.getUserExtSources() != null) {
			for (UserExtSource ues: candidate.getUserExtSources()) {
				try {
					// Try to find the user by userExtSource
					User tmp = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, ues);
					if (user != null && !tmp.equals(user)) {
						throw new UserDuplicateException(user, tmp);
					}
					user = tmp;
				} catch (UserNotExistsException IGNORE) {
					// Ignore, we are only checking if the user exists
				}
			}
		}
	}
}
