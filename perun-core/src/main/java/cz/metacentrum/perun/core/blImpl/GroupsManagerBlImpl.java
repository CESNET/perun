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

	public void deleteGroup(PerunSession sess, Group group, boolean forceDelete) throws GroupAlreadyRemovedException, GroupNotExistsException, RelationExistsException, GroupAlreadyRemovedFromResourceException, InternalErrorException, GroupOperationsException {
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new java.lang.IllegalArgumentException("Built-in " + group.getName() + " group cannot be deleted separately.");
		}

		this.deleteAnyGroup(sess, group, forceDelete);
	}

	public void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws GroupAlreadyRemovedException, GroupNotExistsException, RelationExistsException, GroupAlreadyRemovedFromResourceException, InternalErrorException, GroupOperationsException {
		//Use sorting by group names reverse order (first name A:B:c then A:B etc.)
		Collections.sort(groups, Collections.reverseOrder(
			new Comparator<Group>() {
				@Override
				public int compare(Group groupToCompare, Group groupToCompareWith) {
					return groupToCompare.getName().compareTo(groupToCompareWith.getName());
				}
			}));

		for(Group group: groups) {
			this.deleteGroup(perunSession, group, forceDelete);
		}
	}

	public void deleteMembersGroup(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException, GroupNotExistsException {
		Group group;
		try {
			group = getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
		} catch (GroupNotExistsException e) {
			throw new ConsistencyErrorException("Built-in members group must exist.",e);
		}
		try {
			this.deleteAnyGroup(sess, group, true);
		} catch (RelationExistsException e) {
			throw new ConsistencyErrorException("Built-in members group cannot have any relation in this stage.", e);
		}
	}

	/**
	 * If forceDelete is false, delete only group which has no subgroup and no member.
	 * If forceDelete is true, delete group with all subgroups and members.
	 *
	 * @param sess perun session
	 * @param group group
	 * @param forceDelete if false, delete only empty group without subgroups. If true, delete group including subgroups and members.
	 * @throws InternalErrorException
	 * @throws RelationExistsException Raise only if forceDelete is false and the group has any subgroup or member.
	 * @throws GroupAlreadyRemovedFromResourceException
	 * @throws GroupOperationsException
	 * @throws GroupAlreadyRemovedException if there are 0 rows affected by deleting from DB
	 * @throws GroupNotExistsException
	 */
	protected void deleteAnyGroup(PerunSession sess, Group group, boolean forceDelete) throws InternalErrorException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupOperationsException, GroupAlreadyRemovedException, GroupNotExistsException {
		Vo vo = this.getVo(sess, group);

		if (getGroupsManagerImpl().getSubGroupsCount(sess, group) > 0) {
			if (!forceDelete) throw new RelationExistsException("Group group=" + group + " contains subgroups");

			List<Group> subGroups = this.getAllSubGroups(sess, group);
			//sorting subgroups by name length, because we need to delete the lowest level subgroups (those with the longest name) first
			Collections.sort(subGroups, Collections.reverseOrder(
					new Comparator<Group>() {
						@Override
						public int compare(Group groupToCompare, Group groupToCompareWith) {
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
						} catch (PasswordDeletionFailedException ex) {
							throw new InternalErrorException("Failed to delete reserved login " + login.getRight() + " from KDC.", ex);
						}
					}
				}
				// delete all Groups reserved logins from DB
				getGroupsManagerImpl().deleteGroupReservedLogins(sess, group);

				// 1. remove all relations with group g as an operand group.
				// this removes all relations that depend on this group
				List<Pair<Integer, GroupOperations>> relations = groupsManagerImpl.getGroupRelations(sess, g.getId());
				for (Pair<Integer, GroupOperations> relation : relations) {
					if (relation.getRight() == GroupOperations.UNION) {
						removeUnionRelation(sess, group, g);
					} else if (relation.getRight() == GroupOperations.DIFFERENCE){
						removeDifferenceRelation(sess, group, g);
					} else {
						throw new InternalErrorException("Operation " + relation.getRight() + " not supported.");
					}
				}

				// 2. remove all relations with group g as a result group
				groupsManagerImpl.removeResultGroupRelations(sess, g);

				// 3. removes all excluded and indirect records of members in group g. 
				// We can remove members and relations(@see 2.) without recalculation (@see processRelationMembers) 
				// because all dependencies of group g were deleted in step 1.
				removeAllIndirectMembers(sess, g);
				removeAllExcludedMembers(sess, g);

				// Group applications, submitted data and app_form are deleted on cascade with "deleteGroup()"
				List<Member> directMembersFromDeletedGroup = getDirectGroupMembers(sess, g);
				getGroupsManagerImpl().deleteGroup(sess, vo, g);
				
				for (Member member: directMembersFromDeletedGroup) {
					getPerunBl().getAuditer().log(sess, "{} was removed from {}.", member, g);
					getPerunBl().getAuditer().log(sess, "{} is inactive in {}.", member, g);
				}

				getPerunBl().getAuditer().log(sess, "{} deleted.", g);
			}
		}

		if ((this.getGroupMembersCount(sess, group) > 0) && !forceDelete) {
			throw new RelationExistsException("Group group=" + group + " contains members");
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
					throw new InternalErrorException("Failed to delete reserved login " + login.getRight() + " from KDC.", ex);
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
		List<Pair<Integer, GroupOperations>> relations = groupsManagerImpl.getGroupRelations(sess, group.getId());
		for (Pair<Integer, GroupOperations> relation : relations) {
			if (relation.getRight() == GroupOperations.UNION) {
				removeUnionRelation(sess, groupsManagerImpl.getGroupById(sess, relation.getLeft()), group);
			} else if (relation.getRight() == GroupOperations.DIFFERENCE) {
				removeDifferenceRelation(sess, groupsManagerImpl.getGroupById(sess, relation.getLeft()), group);
			} else {
				throw new InternalErrorException("Operation " + relation.getRight() + " not supported.");
			}
		}

		// 2. remove all relations with group as a result group
		groupsManagerImpl.removeResultGroupRelations(sess, group);

		// 3. removes all excluded and indirect records of members in group. 
		// We can remove members and relations(@see 2.) without recalculation (@see processRelationMembers) 
		// because all dependencies of group were deleted in step 1.
		removeAllIndirectMembers(sess, group);
		removeAllExcludedMembers(sess, group);

		// Group applications, submitted data and app_form are deleted on cascade with "deleteGroup()"
		List<Member> directMembersFromDeletedGroup = getGroupActiveMembers(sess, group);
		getGroupsManagerImpl().deleteGroup(sess, vo, group);

		for (Member member: directMembersFromDeletedGroup) {
			getPerunBl().getAuditer().log(sess, "{} was removed from {}.", member, group);
			getPerunBl().getAuditer().log(sess, "{} is inactive in {}.", member, group);
		}

		getPerunBl().getAuditer().log(sess, "{} deleted.", group);
	}

	/**
	 * Removes all remaining records of excluded members from group. It also logs members new state into auditer log.
	 *
	 * Method is only used before group deletion, SO we can remove all records of excluded members without 
	 * recalculation (@see processRelationMembers) because all dependencies of the group were already deleted.
	 *
	 * @param sess perun session
	 * @param group to remove members from
	 * @throws InternalErrorException
	 */
	private void removeAllExcludedMembers(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> oldMembers = getGroupActiveMembers(sess, group);

		groupsManagerImpl.removeMembersByMembershipType(sess, group, MembershipType.EXCLUDED);

		List<Member> newMembers = getGroupActiveMembers(sess, group);
		newMembers.removeAll(oldMembers);

		for (Member m: newMembers) {
			getPerunBl().getAuditer().log(sess, "{} is active in {}.", m, group);
		}
	}

	/**
	 * Removes all remaining records of indirect members from group. It also logs members new state into auditer log.
	 *
	 * Method is only used before group deletion, SO we can remove all records of indirect members without 
	 * recalculation (@see processRelationMembers) because all dependencies of the group were already deleted.
	 *
	 * @param sess perun session
	 * @param group to remove members from
	 * @throws InternalErrorException
	 */
	private void removeAllIndirectMembers(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> oldMembers = getGroupActiveMembers(sess, group);

		groupsManagerImpl.removeMembersByMembershipType(sess, group, MembershipType.INDIRECT);

		List<Member> newMembers = getGroupActiveMembers(sess, group);
		oldMembers.removeAll(newMembers);

		for (Member m: newMembers) {
			getPerunBl().getAuditer().log(sess, "{} is inactive in {}.", m, group);
		}
	}

	public void deleteAllGroups(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupOperationsException {
		for(Group group: getGroupsManagerImpl().getGroups(sess, vo)) {

			if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
				// Do not delete built-in groups, they must be deleted using separate functions deleteMembersGroup
				continue;
			}
			List<Resource> assignedResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, group);
			try {
				for (Resource resource : assignedResources) {
					getPerunBl().getResourcesManagerBl().removeGroupFromResource(sess, group, resource);
					getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource, group);
				}
				//remove group's attributes
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, group);
			} catch (GroupNotDefinedOnResourceException ex) {
				throw new ConsistencyErrorException(ex);
			} catch (AttributeValueException ex) {
				throw new ConsistencyErrorException("All resources were removed from this group. So all attributes values can be removed.", ex);
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

	public void addMemberToMembersGroup(PerunSession sess, Group group,  Member member) throws GroupNotExistsException, AlreadyMemberException, GroupOperationsException, WrongAttributeValueException, InternalErrorException, WrongReferenceAttributeValueException {
		// Check if the group IS members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.addDirectMember(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.addAdmin and MembersManager.createMember.");
		}
	}

	public void addMember(PerunSession sess, Group group, Member member) throws InternalErrorException, WrongReferenceAttributeValueException, GroupNotExistsException, GroupOperationsException, AlreadyMemberException, WrongAttributeValueException {
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

		member = getGroupsManagerImpl().addMember(sess, group, member, MembershipType.DIRECT, group.getId());
		getPerunBl().getAuditer().log(sess, "{} was added to {}.", member, group);

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

		// member is already excluded in group, adding him as direct to it will not cause any propagation to other groups
		if (!getGroupActiveMembers(sess, group).contains(member)) {
			return;
		} else {
			getPerunBl().getAuditer().log(sess, "{} is active in {}.", member, group);
		}

		// LEFT is group id and RIGHT is operation
		// check all relations with this group and call processRelationMembers to reflect changes of adding member to group
		List<Pair<Integer, GroupOperations>> relations = groupsManagerImpl.getGroupRelations(sess, group.getId());
		for (Pair<Integer, GroupOperations> relation : relations) {
			processRelationMembers(sess, groupsManagerImpl.getGroupById(sess, relation.getLeft()), Collections.singletonList(member), group.getId(), relation.getRight(), true);
		}
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
		List<Member> oldMembers = this.getGroupActiveMembers(sess, group);

		for(Member member: members) {
			groupsManagerImpl.addMember(sess, group, member, MembershipType.INDIRECT, sourceGroupId);

			// setting required attributes
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

		// get list of new members
		List<Member> newMembers = this.getGroupActiveMembers(sess, group);
		// select only newly added members
		newMembers.removeAll(oldMembers);

		return newMembers;
	}

	/**
	 * Add records of the members with an EXCLUDED membership type to the group. 
	 *
	 * @param sess perun session
	 * @param group group to add records of EXCLUDED members to
	 * @param members list of members to add as EXCLUDED
	 * @param sourceGroupId id of a group from which members originate
	 * @return list of members that were inactivated by addition of EXCLUDED records
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	protected List<Member> addExcludedMembers(PerunSession sess, Group group, List<Member> members, int sourceGroupId) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		// save list of old group members
		List<Member> oldMembers = this.getGroupActiveMembers(sess, group);

		for(Member member: members) {
			groupsManagerImpl.addMember(sess, group, member, MembershipType.EXCLUDED, sourceGroupId);
		}

		// get list of new members
		List<Member> newMembers = this.getGroupActiveMembers(sess, group);
		// get only excluded members
		oldMembers.removeAll(newMembers);

		return oldMembers;
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
		List<Member> oldMembers = this.getGroupActiveMembers(sess, group);

		for (Member member: members) {
			member.setSourceGroupId(sourceGroupId);
			groupsManagerImpl.removeMember(sess, group, member);
		}

		// get list of new members
		List<Member> newMembers = this.getGroupActiveMembers(sess, group);
		// get only removed members
		oldMembers.removeAll(newMembers);

		return oldMembers;
	}

	/**
	 * Remove records of the members with an EXCLUDED membership type from the group. 
	 *
	 * @param sess perun session
	 * @param group group to remove records of EXCLUDED members from
	 * @param members list of members to remove
	 * @param sourceGroupId id of a group from which members originate
	 * @return list of members that were activated by removal of EXCLUDED records
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws NotGroupMemberException
	 */
	protected List<Member> removeExcludedMembers(PerunSession sess, Group group, List<Member> members, int sourceGroupId) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotGroupMemberException {
		// save list of old group members
		List<Member> oldMembers = this.getGroupActiveMembers(sess, group);

		for (Member member: members) {
			member.setSourceGroupId(sourceGroupId);
			groupsManagerImpl.removeMember(sess, group, member);
		}

		// get list of new members
		List<Member> newMembers = this.getGroupActiveMembers(sess, group);
		// get only new acquired members after their EXCLUDED status was removed
		newMembers.removeAll(oldMembers);

		return newMembers;
	}

	public void removeMember(PerunSession sess, Group group, Member member) throws InternalErrorException, GroupOperationsException, NotGroupMemberException, GroupNotExistsException {
		// Check if the group is NOT members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Cannot remove member directly from the members group.");
		} else {
			this.removeDirectMember(sess, group, member);
		}
	}

	public void removeMemberFromMembersOrAdministratorsGroup(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException, GroupOperationsException, GroupNotExistsException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.removeDirectMember(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.removeAdmin and MembersManager.deleteMember.");
		}
	}

	protected void removeDirectMember(PerunSession sess, Group group, Member member) throws InternalErrorException, GroupOperationsException, GroupNotExistsException, NotGroupMemberException {

		member.setSourceGroupId(group.getId());

		boolean wasMember = isGroupMember(sess, group, member);

		getGroupsManagerImpl().removeMember(sess, group, member);
		getPerunBl().getAuditer().log(sess, "{} was removed from {}.", member, group);

		if (wasMember && !isGroupMember(sess, group, member)) {
			getPerunBl().getAuditer().log(sess, "{} is inactive in {}.", member, group);
		}

		List<Pair<Integer, GroupOperations>> relations = groupsManagerImpl.getGroupRelations(sess, group.getId());
		for (Pair<Integer, GroupOperations> relation : relations) {
			processRelationMembers(sess, groupsManagerImpl.getGroupById(sess, relation.getLeft()), Collections.singletonList(member), group.getId(), relation.getRight(), false);
		}
	}

	public List<Member> getGroupActiveMembers(PerunSession sess, Group group) throws InternalErrorException {
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupActiveMembers(sess, group));
	}

	@Override
	public List<Member> getDirectGroupMembers(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getDirectGroupMembers(sess, group);
	}

	@Override
	public List<Member> getAllGroupMembers(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getAllGroupMembers(sess, group);
	}

	@Override
	public List<Member> getAllGroupMembersWithStatuses(PerunSession sess, Group group, Status status) throws InternalErrorException {
		if (status == null) {
			return getAllGroupMembers(sess, group);
		}
		return getGroupsManagerImpl().getAllGroupMembersWithStatuses(sess, group, Arrays.asList(status), false);
	}

	public List<Member> getGroupActiveMembers(PerunSession sess, Group group, Status status) throws InternalErrorException {
		if (status == null) {
			return this.getGroupActiveMembers(sess, group);
		}
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupActiveMembers(sess, group, Arrays.asList(status), false));
	}

	@Override
	public List<User> getGroupUsers(PerunSession perunSession, Group group) throws InternalErrorException {
		return new ArrayList<User>(new HashSet<User>(getGroupsManagerImpl().getGroupUsers(perunSession, group)));
	}

	public List<Member> getGroupMembersExceptInvalid(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupActiveMembers(sess, group, Arrays.asList(Status.INVALID), true);
	}

	public List<Member> getGroupMembersExceptInvalidAndDisabled(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupActiveMembers(sess, group, Arrays.asList(Status.INVALID, Status.DISABLED), true);
	}

	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException {
		return this.getGroupRichMembers(sess, group, null);
	}

	public List<RichMember> getGroupRichMembersExceptInvalid(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> members = this.getGroupMembersExceptInvalid(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws InternalErrorException {
		List<Member> members = this.getGroupActiveMembers(sess, group, status);

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
		List<Member> members = this.getGroupActiveMembers(sess, group);
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
			return getGroupActiveMembers(sess, parentGroup);
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
	public List<String> synchronizeGroup(PerunSession sess, Group group) throws InternalErrorException, MemberAlreadyRemovedException, GroupNotExistsException, AlreadyMemberException, NotMemberOfParentGroupException, WrongReferenceAttributeValueException, WrongAttributeValueException, GroupOperationsException {

		List<String> skippedMembers = new ArrayList<>();
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
				subjects = ((ExtSourceSimpleApi) source).getGroupSubjects(attributes);
				log.debug("Group synchronization {}: external group contains {} members.", group, subjects.size());
			} catch (ExtSourceUnsupportedOperationException e2) {
				throw new InternalErrorException("ExtSource " + source.getName() + " doesn't support getGroupSubjects", e2);
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
				if (login == null || login.isEmpty()) {
					log.debug("Subject {} doesn't contain attribute login, skipping.", subject);
					skippedMembers.add("MemberEntry:[" + subject + "] was skipped because login is missing");
					continue;
				}
				try {
					// One of three possible ways should happen to get Candidate
					// 1] sources of login and other attributes are not same
					if(!membersSource.equals(source)) {
						//need to read attributes from the new memberSource, we can't use locally data there (there are from other extSource)
						candidates.add((getPerunBl().getExtSourcesManagerBl().getCandidate(sess, membersSource, login)));
					// 2] sources are same and we work with source which is instance of ExtSourceApi
					} else if (membersSource instanceof ExtSourceApi) {
						// we can use the data from this source without reading them again (all exists in the map of subject attributes)
						candidates.add((getPerunBl().getExtSourcesManagerBl().getCandidate(sess, subject, membersSource, login)));
					// 3] sources are same and we work with source which is instace of ExtSourceSimpleApi
					} else if (membersSource instanceof ExtSourceSimpleApi) {
						// we can't use the data from this source, we need to read them again (they are not in the map of subject attributes)
						candidates.add((getPerunBl().getExtSourcesManagerBl().getCandidate(sess, membersSource, login)));
					} else {
						// this could not happen without change in extSource API code
						throw new InternalErrorException("ExtSource is other instance than SimpleApi or Api and this is not supported!");
					}
				} catch (ExtSourceNotExistsException e) {
					throw new InternalErrorException("ExtSource " + membersSource + " doesn't exists.");
				} catch (CandidateNotExistsException e) {
					log.warn("getGroupSubjects subjects returned login {}, but it cannot be obtained using getCandidate()", login);
					skippedMembers.add("MemberEntry:[" + subject + "] was skipped because candidate can't be found by login:'" + login + "' in extSource " + membersSource);
					continue;
				} catch (ExtSourceUnsupportedOperationException e) {
					log.warn("ExtSource {} doesn't support getCandidate operation.", membersSource);
					skippedMembers.add("MemberEntry:[" + subject + "] was skipped because extSource " + membersSource + " not support method getCandidate");
					continue;
				} catch (ParserException e) {
					log.warn("Can't parse value {} from candidate with login {}", e.getParsedValue(), login);
					skippedMembers.add("MemberEntry:[" + subject + "] was skipped because of problem with parsing value '" + e.getParsedValue() + "'");
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
											getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, member, memberAttribute);
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
												getPerunBl().getAttributesManagerBl().mergeAttributeValueInNestedTransaction(sess, richMember.getUser(), userAttribute);
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
										log.error("Attribute {} doesn't exists.", attributeName);
										throw new ConsistencyErrorException(e);
									}

									Attribute newAttribute = new Attribute(attributeDefinition);
									Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), newAttribute.getType());
									newAttribute.setValue(subjectAttributeValue);
									try {
										if (attributeDefinition.getEntity().equals(AttributesManager.ENTITY_MEMBER)) {
											try {
												// Try to set member's attributes
												getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, member, newAttribute);
											} catch (AttributeValueException e) {
												// There is a problem with attribute value, so set INVALID status for the member
												getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
											}
										} else if (attributeDefinition.getEntity().equals(AttributesManager.ENTITY_USER)) {
											try {
												// Try to set user's attributes
												getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember.getUser(), newAttribute);
											} catch (AttributeValueException e) {
												// There is a problem with attribute value, so set INVALID status of the member
												getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
												try {
													// The member is invalid, so try to set the value again, and check if the change has influence also on other members
													getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember.getUser(), newAttribute);
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
									Date currentMembershipExpirationDate = BeansUtils.getDateFormatterWithoutTime().parse((String) memberExpiration.getValue());

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
						getPerunBl().getGroupsManagerBl().addMember(sess, group, member);
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
			} catch (InternalErrorException | GroupOperationsException | NotMemberOfParentGroupException | AlreadyMemberException | 
					GroupNotExistsException | MemberAlreadyRemovedException | WrongAttributeAssignmentException | 
					WrongReferenceAttributeValueException | WrongAttributeValueException e) {
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

		List<Member> excludedMembers = new ArrayList<>();
		List<Member> filteredMembers = new ArrayList<>();

		for (Member member : members) {
			if (member.getMembershipType().equals(MembershipType.EXCLUDED) && !excludedMembers.contains(member)) {
				excludedMembers.add(member);
			} else if (member.getMembershipType().equals(MembershipType.DIRECT)) {
				filteredMembers.add(member);
			}
		}

		// add members with indirect membership type that are not already in the filteredMembers list
		for(Member m: members) {
			if(!filteredMembers.contains(m)) {
				filteredMembers.add(m);
			}
		}

		filteredMembers.removeAll(excludedMembers);

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

	@Override
	public void processRelationMembers(PerunSession sess, Group resultGroup, List<Member> changedMembers, int sourceGroupId, GroupOperations operation, boolean addition) throws GroupOperationsException {

		List<Member> newMembers;

		try {
			if (addition && operation == GroupOperations.UNION) {
				newMembers = addIndirectMembers(sess, resultGroup, changedMembers, sourceGroupId);
			} else if (addition && operation == GroupOperations.DIFFERENCE) {
				newMembers = addExcludedMembers(sess, resultGroup, changedMembers, sourceGroupId);
			} else if (!addition && operation == GroupOperations.UNION) {
				newMembers = removeIndirectMembers(sess, resultGroup, changedMembers, sourceGroupId);
			} else if (!addition && operation == GroupOperations.DIFFERENCE) {
				newMembers = removeExcludedMembers(sess, resultGroup, changedMembers, sourceGroupId);
			} else {
				throw new InternalErrorException("Operation " + operation + " not supported.");
			}

			if (newMembers.isEmpty()) {
				return;
			}

			if (addition && operation == GroupOperations.UNION || 
					!addition && operation == GroupOperations.DIFFERENCE) {
				for (Member member : newMembers) {
					getPerunBl().getAuditer().log(sess, "{} is active in {}.", member, resultGroup);
				}
			} else if (!addition && operation == GroupOperations.UNION ||
					addition && operation == GroupOperations.DIFFERENCE) {
				for (Member member : newMembers) {
					getPerunBl().getAuditer().log(sess, "{} is inactive in {}.", member, resultGroup);
				}
			}

			List<Pair<Integer, GroupOperations>> relations = groupsManagerImpl.getGroupRelations(sess, resultGroup.getId());
			for (Pair<Integer, GroupOperations> relation : relations) {
				if (operation == GroupOperations.UNION) {
					processRelationMembers(sess, groupsManagerImpl.getGroupById(sess, relation.getLeft()), newMembers, resultGroup.getId(), relation.getRight(), addition);
				} else if (operation == GroupOperations.DIFFERENCE) {
					processRelationMembers(sess, groupsManagerImpl.getGroupById(sess, relation.getLeft()), newMembers, resultGroup.getId(), relation.getRight(), !addition);
				} else {
					throw new InternalErrorException("Operation " + operation + " not supported.");
				}
			}
		} catch (WrongReferenceAttributeValueException | WrongAttributeValueException | AlreadyMemberException | 
				InternalErrorException | NotGroupMemberException | GroupNotExistsException ex) {
			throw new GroupOperationsException(ex);
		}
	}

	@Override
	public Group groupUnion(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupOperationsException, InternalErrorException {

		//check if any of the groups is members group
		if(resultGroup.getName().equals(VosManager.MEMBERS_GROUP) || operandGroup.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Union cannot be created on members group.");
		}

		// check if both groups are from same VO
		if (resultGroup.getVoId() != operandGroup.getVoId()) {
			throw new InternalErrorException("Groups are not from same VO");
		}

		// check if result group is the same as operand group
		if (resultGroup.getId() == operandGroup.getId()) {
			throw new InternalErrorException("Result group cannot be the same as operand group.");
		}

		// check if there is already a record of these two groups
		if (this.groupsManagerImpl.isRelationBetweenGroups(resultGroup, operandGroup)) {
			throw new InternalErrorException("There is already an operation defined between these two groups: " + resultGroup + " and " + operandGroup);
		}

		// check cycle between groups
		if (checkGroupsCycle(sess, resultGroup.getId(), operandGroup.getId())) {
			throw new InternalErrorException("This relation would create group transitivity.");
		}

		// save group relation
		groupsManagerImpl.saveGroupRelation(sess, resultGroup, operandGroup, GroupOperations.UNION);

		// do the operation logic
		processRelationMembers(sess, resultGroup, getGroupActiveMembers(sess, operandGroup), operandGroup.getId(), GroupOperations.UNION, true);

		return resultGroup;
	}

	@Override
	public Group groupDifference(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupOperationsException, InternalErrorException {

		//check if any of the groups is members group
		if(resultGroup.getName().equals(VosManager.MEMBERS_GROUP) || operandGroup.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Difference cannot be created on members group.");
		}

		// check if both groups are from same VO
		if (resultGroup.getVoId() != operandGroup.getVoId()) {
			throw new InternalErrorException("Groups are not from same VO");
		}

		// check if result group is the same as operand group
		if (resultGroup.getId() == operandGroup.getId()) {
			throw new InternalErrorException("Result group cannot be the same as operand group.");
		}

		// check if there is already a record of these two groups
		if (this.groupsManagerImpl.isRelationBetweenGroups(resultGroup, operandGroup)) {
			throw new InternalErrorException("There is already an operation defined between these two groups: " + resultGroup + " and " + operandGroup);
		}

		// check cycle between groups
		if (checkGroupsCycle(sess, resultGroup.getId(), operandGroup.getId())) {
			throw new InternalErrorException("This relation would create group transitivity.");
		}

		// save group relation
		groupsManagerImpl.saveGroupRelation(sess, resultGroup, operandGroup, GroupOperations.DIFFERENCE);

		// do the operation logic
		processRelationMembers(sess, resultGroup, getGroupActiveMembers(sess, operandGroup), operandGroup.getId(), GroupOperations.DIFFERENCE, true);

		return resultGroup;
	}

	@Override
	public void removeUnionRelation(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupOperationsException, InternalErrorException {
		processRelationMembers(sess, resultGroup, getGroupActiveMembers(sess, operandGroup), operandGroup.getId(), GroupOperations.UNION, false);

		groupsManagerImpl.removeGroupRelation(sess, resultGroup, operandGroup, GroupOperations.UNION);
	}

	@Override
	public void removeDifferenceRelation(PerunSession sess, Group resultGroup, Group operandGroup) throws GroupOperationsException, InternalErrorException {
		processRelationMembers(sess, resultGroup, getGroupActiveMembers(sess, operandGroup), operandGroup.getId(), GroupOperations.DIFFERENCE, false);

		groupsManagerImpl.removeGroupRelation(sess, resultGroup, operandGroup, GroupOperations.DIFFERENCE);
	}

	public boolean checkGroupsCycle(PerunSession sess, int resultGroupId, int operandGroupId) throws InternalErrorException {
		List<Integer> groupsIds = groupsManagerImpl.getRelatedGroupsIds(sess, resultGroupId);

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

	@Override
	public boolean isRelationBetweenGroups(Group group1, Group group2) {
		return groupsManagerImpl.isRelationBetweenGroups(group1, group2);
	}

	@Override
	public boolean isOneWayRelationBetweenGroups(Group resultGroup, Group operandGroup) {
		return groupsManagerImpl.isOneWayRelationBetweenGroups(resultGroup, operandGroup);
	}
}
