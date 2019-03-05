package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.GroupManagerEvents.AdminAddedForGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.AdminGroupAddedForGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.AdminGroupRemovedFromGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.AdminRemovedForGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.AllGroupsFromVoDeleted;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberRemovedFromGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupCreatedAsSubgroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupCreatedInVo;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupDeleted;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupMoved;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupSyncFailed;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupSyncFinished;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupSyncFinishedWithErrors;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupSyncStarted;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupUpdated;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.IndirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.IndirectMemberRemovedFromGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberExpiredInGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberRemovedFromGroupTotally;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.MemberValidatedInGroup;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberExpired;
import cz.metacentrum.perun.core.api.PerunPrincipal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private Integer maxConcurentGroupsToSynchronize;
	private final PerunBeanProcessingPool<Group> poolOfGroupsToBeSynchronized;
	private final ArrayList<GroupSynchronizerThread> groupSynchronizerThreads;
	private static final String A_G_D_AUTHORITATIVE_GROUP = AttributesManager.NS_GROUP_ATTR_DEF + ":authoritativeGroup";
	private static final String A_G_D_EXPIRATION_RULES = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembershipExpirationRules";
	private static final String A_MG_D_MEMBERSHIP_EXPIRATION = AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration";
	private static final String A_M_V_LOA = AttributesManager.NS_MEMBER_ATTR_VIRT + ":loa";

	/**
	 * Create new instance of this class.
	 *
	 */
	public GroupsManagerBlImpl(GroupsManagerImplApi groupsManagerImpl) {
		this.groupsManagerImpl = groupsManagerImpl;
		this.groupSynchronizerThreads = new ArrayList<>();
		this.poolOfGroupsToBeSynchronized = new PerunBeanProcessingPool<>();
		//set maximum concurrent groups to synchronize by property
		this.maxConcurentGroupsToSynchronize = BeansUtils.getCoreConfig().getGroupMaxConcurentGroupsToSynchronize();
	}

	@Override
	public Group createGroup(PerunSession sess, Vo vo, Group group) throws GroupExistsException, InternalErrorException {
		if (group.getParentGroupId() != null) throw new InternalErrorException("Top-level groups can't have parentGroupId set!");
		group = getGroupsManagerImpl().createGroup(sess, vo, group);
		getPerunBl().getAuditer().log(sess, new GroupCreatedInVo(group, vo));
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

	@Override
	public Group createGroup(PerunSession sess, Group parentGroup, Group group) throws GroupExistsException, InternalErrorException, GroupRelationNotAllowed, GroupRelationAlreadyExists {
		Vo vo = this.getVo(sess, parentGroup);

		group = getGroupsManagerImpl().createGroup(sess, vo, parentGroup, group);
		try {
			parentGroup = createGroupUnion(sess, parentGroup, group, true);

			//We catch those exceptions, but they should never be thrown, so we just log them.
		} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
			log.error("Exception thrown in createGroup method, while it shouldn't be thrown. Cause:{}",e);
		} catch (GroupNotExistsException e) {
			throw new ConsistencyErrorException("Database consistency error while creating group: {}",e);
		}

		getPerunBl().getAuditer().log(sess, new GroupCreatedAsSubgroup(group, vo, parentGroup));

		return group;
	}

	@Override
	public void deleteGroup(PerunSession sess, Group group, boolean forceDelete) throws InternalErrorException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new java.lang.IllegalArgumentException("Built-in " + group.getName() + " group cannot be deleted separately.");
		}
		try {
			this.deleteAnyGroup(sess, group, forceDelete);
		} catch (NotGroupMemberException ex) {
			throw new ConsistencyErrorException("Database consistency error while deleting group: {}",ex);
		} catch (WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws InternalErrorException, GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
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

	@Override
	public void deleteMembersGroup(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, NotGroupMemberException {
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
		} catch (WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException(ex);
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
	private void deleteAnyGroup(PerunSession sess, Group group, boolean forceDelete) throws InternalErrorException, RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, WrongReferenceAttributeValueException, NotGroupMemberException, WrongAttributeValueException {
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

			for (Group subGroup : subGroups) {
				//For auditer
				List<Resource> subGroupResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, subGroup);
				for(Resource resource : subGroupResources) {
					try {
						getPerunBl().getResourcesManagerBl().removeGroupFromResource(sess, subGroup, resource);
					} catch(GroupNotDefinedOnResourceException ex) {
						throw new ConsistencyErrorException(ex);
					}
				}

				//remove subgroups' attributes
				try {
					getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, subGroup);
				} catch(AttributeValueException ex) {
					throw new ConsistencyErrorException("All resources was removed from this group. So all attributes values can be removed.", ex);
				}

				// delete all sub-groups reserved logins from KDC
				List<Integer> list = getGroupsManagerImpl().getGroupApplicationIds(sess, subGroup);
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
				getGroupsManagerImpl().deleteGroupReservedLogins(sess, subGroup);

				//Remove all information about group on facilities (facilities contacts)
				List<ContactGroup> groupContactGroups = getPerunBl().getFacilitiesManagerBl().getFacilityContactGroups(sess, subGroup);
				if(!groupContactGroups.isEmpty()) {
					getPerunBl().getFacilitiesManagerBl().removeAllGroupContacts(sess, subGroup);
				}

				//remove all assigned ExtSources to this group
				List<ExtSource> assignedSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, subGroup);
				for(ExtSource source: assignedSources) {
					try {
						getPerunBl().getExtSourcesManagerBl().removeExtSource(sess, subGroup, source);
					} catch (ExtSourceNotAssignedException | ExtSourceAlreadyRemovedException ex) {
						//Just log this, because if method can't remove it, it is probably not assigned now
						log.warn("Try to remove not existing extSource {} from group {} when deleting group.", source, subGroup);
					}
				}

				// 1. remove all relations with group g as an operand group.
				// this removes all relations that depend on this group
				List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, subGroup.getId());
				for (Integer groupId : relations) {
					removeGroupUnion(sess, groupsManagerImpl.getGroupById(sess, groupId), subGroup, true);
				}

				// 2. remove all relations with group as a result group
				// We can remove relations without recalculation (@see removeRelationMembers)
				// because all dependencies of group were deleted in step 1.
				groupsManagerImpl.removeResultGroupRelations(sess, subGroup);

				// Group applications, submitted data and app_form are deleted on cascade with "deleteGroup()"

				List<Member> membersFromDeletedGroup = getGroupMembers(sess, subGroup);

				// delete all member-group attributes
				for (Member member : membersFromDeletedGroup) {
					try {
						perunBl.getAttributesManagerBl().removeAllAttributes(sess, member, subGroup);
					} catch (AttributeValueException | WrongAttributeAssignmentException ex) {
						throw new ConsistencyErrorException("All resources were removed from this group. So all member-group attribute values can be removed.", ex);
					}
				}

				// Deletes also all direct and indirect members of the group
				getGroupsManagerImpl().deleteGroup(sess, vo, subGroup);

				logTotallyRemovedMembers(sess, subGroup.getParentGroupId(), membersFromDeletedGroup);

				getPerunBl().getAuditer().log(sess, new GroupDeleted(subGroup));

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
				log.warn("Try to remove not existing extSource {} from group {} when deleting group.", source, group);
			}
		}

		// 1. remove all relations with group g as an operand group.
		// this removes all relations that depend on this group
		List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, group.getId());
		for (Integer groupId : relations) {
			removeGroupUnion(sess, groupsManagerImpl.getGroupById(sess, groupId), group, true);
		}

		// 2. remove all relations with group as a result group
		// We can remove relations without recalculation (@see removeRelationMembers)
		// because all dependencies of group were deleted in step 1.
		groupsManagerImpl.removeResultGroupRelations(sess, group);

		// Group applications, submitted data and app_form are deleted on cascade with "deleteGroup()"
		List<Member> membersFromDeletedGroup = getGroupMembers(sess, group);

		// delete all member-group attributes
		for (Member member : membersFromDeletedGroup) {
			try {
				perunBl.getAttributesManagerBl().removeAllAttributes(sess, member, group);
			} catch (AttributeValueException | WrongAttributeAssignmentException ex) {
				throw new ConsistencyErrorException("All resources were removed from this group. So all member-group attribute values can be removed.", ex);
			}
		}

		// remove admin roles of group
		List<Facility> facilitiesWhereGroupIsAdmin = getGroupsManagerImpl().getFacilitiesWhereGroupIsAdmin(sess, group);
		for (Facility facility : facilitiesWhereGroupIsAdmin) {
			try {
				perunBl.getFacilitiesManagerBl().removeAdmin(sess, facility, group);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of facility {} due to group not admin exception {}.", group, facility, e);
			}
		}

		List<Group> groupsWhereGroupIsAdmin = getGroupsManagerImpl().getGroupsWhereGroupIsAdmin(sess, group);
		for (Group group1 : groupsWhereGroupIsAdmin) {
			try {
				removeAdmin(sess, group1, group);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of group {} due to group not admin exception {}.", group, group1, e);
			}
		}

		List<Resource> resourcesWhereGroupIsAdmin = getGroupsManagerImpl().getResourcesWhereGroupIsAdmin(sess, group);
		for (Resource resource : resourcesWhereGroupIsAdmin) {
			try {
				perunBl.getResourcesManagerBl().removeAdmin(sess, resource, group);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of resource {} due to group not admin exception {}.", group, resource, e);
			}
		}

		List<Resource> resourcesWhereGroupIsResourceSelfService = getGroupsManagerImpl().getResourcesWhereGroupIsResourceSelfService(sess, group);
		for (Resource resource : resourcesWhereGroupIsResourceSelfService) {
			try {
				perunBl.getResourcesManagerBl().removeResourceSelfServiceGroup(sess, resource, group);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of resource {} due to group not admin exception {}.", group, resource, e);
			}
		}

		List<SecurityTeam> securityTeamsWhereGroupIsAdmin = getGroupsManagerImpl().getSecurityTeamsWhereGroupIsAdmin(sess, group);
		for (SecurityTeam securityTeam : securityTeamsWhereGroupIsAdmin) {
			try {
				perunBl.getSecurityTeamsManagerBl().removeAdmin(sess, securityTeam, group);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of security team {} due to group not admin exception {}.", group, securityTeam, e);
			}
		}

		List<Vo> vosWhereGroupIsAdmin = getGroupsManagerImpl().getVosWhereGroupIsAdmin(sess, group);
		for (Vo vo1 : vosWhereGroupIsAdmin) {
			try {
				perunBl.getVosManagerBl().removeAdmin(sess, vo1, group);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of facility {} due to group not admin exception {}.", group, vo1, e);
			}
		}

		// Deletes also all direct and indirect members of the group
		getGroupsManagerImpl().deleteGroup(sess, vo, group);

		logTotallyRemovedMembers(sess, group.getParentGroupId(), membersFromDeletedGroup);

		getPerunBl().getAuditer().log(sess, new GroupDeleted(group));
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
				notifyMemberRemovalFromGroup(sess, parentGroup, m);
				getPerunBl().getAuditer().log(sess, new MemberRemovedFromGroupTotally(m, parentGroup));
			}
			parentGroupId=parentGroup.getParentGroupId();
		}
	}

	@Override
	public void deleteAllGroups(PerunSession sess, Vo vo) throws InternalErrorException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
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
			} catch (GroupResourceMismatchException ex) {
				throw new InternalErrorException(ex);
			}

			try {
				this.deleteGroup(sess, group, true);
			} catch (RelationExistsException | GroupNotExistsException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	@Override
	public Group updateGroup(PerunSession sess, Group group) throws InternalErrorException {

		// return group with correct updated name and shortName
		group = getGroupsManagerImpl().updateGroup(sess, group);
		getPerunBl().getAuditer().log(sess,new GroupUpdated(group));

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
			getPerunBl().getAuditer().log(sess, new GroupUpdated(g));
		}

		return group;
	}

	@Override
	public Group updateParentGroupId(PerunSession sess, Group group) throws InternalErrorException {
		if(group == null) throw new InternalErrorException("Group can't be null.");

		//return group with updated parentGroupId
		group = getGroupsManagerImpl().updateParentGroupId(sess, group);

		return group;
	}

//	/**
//	 * Returns map containing statuses for each group and for each member
//	 *
//	 * @param sess session
//	 * @param groups groups where statuses are calculated
//	 * @return Map containing groups with members and their statuses
//	 * @throws InternalErrorException internal error
//	 */
//	private Map<Group, Map<Member, MemberGroupStatus>> getMemberGroupStatusesForGroups(PerunSession sess, List<Group> groups) throws InternalErrorException {
//		Map<Group, Map<Member, MemberGroupStatus>> previousStatuses = new HashMap<>();
//		for (Group previousResultGroup : groups) {
//			previousStatuses.put(previousResultGroup, getMemberGroupStatusesForGroup(sess, previousResultGroup));
//		}
//		return previousStatuses;
//	}
//
//	/**
//	 * Returns map containing statuses for each member in given group
//	 *
//	 * @param sess session
//	 * @param group group
//	 * @return map containing statuses for each member in given group
//	 * @throws InternalErrorException internal error
//	 */
//	private Map<Member, MemberGroupStatus> getMemberGroupStatusesForGroup(PerunSession sess, Group group) throws InternalErrorException {
//		return getMemberGroupStatusesForGroupAndForMembersFromOtherGroup(sess, group, group);
//	}

	/**
	 * Returns map containing statuses for members from 'fromGroup' inside given group
	 *
	 * @param sess session
	 * @param group group
	 * @return map containing statuses for each member in given group
	 * @throws InternalErrorException internal error
	 */
	private Map<Member, MemberGroupStatus> getMemberGroupStatusesForGroupAndForMembersFromOtherGroup(PerunSession sess, Group group, Group fromGroup) throws InternalErrorException {
		Map<Member, MemberGroupStatus> groupStatuses = new HashMap<>();
		List<Member> groupMembers = getGroupMembers(sess, fromGroup);
		for (Member groupMember : groupMembers) {
			groupStatuses.put(groupMember, getTotalMemberGroupStatus(sess, groupMember, group));
		}
		return groupStatuses;
	}

	@Override
	public void moveGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws InternalErrorException, GroupMoveNotAllowedException, WrongAttributeValueException, WrongReferenceAttributeValueException {

		// check if moving group is null
		if (movingGroup == null) {
			throw new GroupMoveNotAllowedException("Moving group: " + movingGroup + " cannot be null.", movingGroup, destinationGroup);
		}

		// check if moving group is members group
		if (movingGroup.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new GroupMoveNotAllowedException("It is not possible to move Members group.", movingGroup, destinationGroup);
		}

		// check if had parent group
		Group previousParent;
		try {
			previousParent = getParentGroup(sess, movingGroup);
		} catch (ParentGroupNotExistsException e) {
			previousParent = null;
		}

		//if destination group is null, it means group will be moved as top level group
		if (destinationGroup != null) {

			// check if both groups are from same VO
			if (destinationGroup.getVoId() != movingGroup.getVoId()) {
				throw new GroupMoveNotAllowedException("Groups are not from same VO. Moving group: " + movingGroup + " has VO:" + movingGroup.getVoId() + " and destination group: " + destinationGroup + " has VO:" + movingGroup.getVoId() + ".", movingGroup, destinationGroup);
			}

			// check if moving group is the same as destination group
			if (destinationGroup.getId() == movingGroup.getId()) {
				throw new GroupMoveNotAllowedException("Moving group: " + movingGroup + " cannot be the same as destination group: " + destinationGroup + ".", movingGroup, destinationGroup);
			}

			// check if destination group is members group
			if (destinationGroup.getName().equals(VosManager.MEMBERS_GROUP)) {
				throw new GroupMoveNotAllowedException("It is not possible to move group under Members group.", movingGroup, destinationGroup);
			}

			// check if moving group is already under destination group
			if (movingGroup.getParentGroupId() != null && destinationGroup.getId() == movingGroup.getParentGroupId()) {
				throw new GroupMoveNotAllowedException("Moving group: " + movingGroup + " is already under destination group: " + destinationGroup + " as subGroup.", movingGroup, destinationGroup);
			}

			List<Group> movingGroupAllSubGroups = getAllSubGroups(sess, movingGroup);

			// check if destination group exists as subGroup in moving group subGroups
			if (movingGroupAllSubGroups.contains(destinationGroup)) {
				throw new GroupMoveNotAllowedException("Destination group: " + destinationGroup + " is subGroup of Moving group: " + movingGroup + ".", movingGroup, destinationGroup);
			}

			// check if this operation would create cycle
			if (checkGroupsCycle(sess, destinationGroup.getId(), movingGroup.getId())) {
				throw new GroupMoveNotAllowedException("There would be cycle created after moving group: " + movingGroup + " under destination group: " + destinationGroup + ".", movingGroup, destinationGroup);
			}

			List<Group> destinationGroupSubGroups = getSubGroups(sess, destinationGroup);

			// check if under destination group is group with same short name as Moving group short name
			for (Group group: destinationGroupSubGroups) {
				if(movingGroup.getShortName().equals(group.getShortName())){
					throw new GroupMoveNotAllowedException("Under destination group: " + destinationGroup + " is group with the same name as moving group: " + movingGroup + ".", movingGroup, destinationGroup);
				}
			}

			//check if there is union between destination group and moving group
			if(groupsManagerImpl.isOneWayRelationBetweenGroups(destinationGroup, movingGroup)) {
				throw new GroupMoveNotAllowedException("There is already group union between moving group: " + movingGroup + " and destination group: " + destinationGroup + ".", movingGroup, destinationGroup);
			}

			processRelationsWhileMovingGroup(sess, destinationGroup, movingGroup);

			// We have to set group attributes so we can update it in database
			movingGroup.setParentGroupId(destinationGroup.getId());
			movingGroup.setName(destinationGroup.getName() + ":" + movingGroup.getShortName());

		} else {

			// check if moving group is already top level group
			if (movingGroup.getParentGroupId() == null) {
				throw new GroupMoveNotAllowedException("Moving group: " + movingGroup + " is already top level group.", movingGroup, destinationGroup);
			}

			List<Group> destinationGroupSubGroups = getGroups(sess, getVo(sess, movingGroup));

			// check if there is top level group with same short name as Moving group short name
			for (Group group: destinationGroupSubGroups) {
				if(movingGroup.getShortName().equals(group.getName())){
					throw new GroupMoveNotAllowedException("There is already top level group with the same name as moving group: " + movingGroup + ".", movingGroup, destinationGroup);
				}
			}

			processRelationsWhileMovingGroup(sess, destinationGroup, movingGroup);

			// We have to set group attributes so we can update it in database
			movingGroup.setParentGroupId(null);
			movingGroup.setName(movingGroup.getShortName());

		}

		//we have to update group name in database
		getGroupsManagerImpl().updateGroupName(sess, movingGroup);

		// We have to properly set all subGroups names level by level
		setSubGroupsNames(sess, getSubGroups(sess, movingGroup), movingGroup);

		// And finally update parentGroupId for moving group in database
		this.updateParentGroupId(sess, movingGroup);

		List<Member> movingGroupMembers = getGroupMembers(sess, movingGroup);

		for(Member member : movingGroupMembers) {
			if (previousParent != null) {

				// calculate new member-group statuses for members from previous moving group parent
				recalculateMemberGroupStatusRecursively(sess, member, previousParent);
			}
		}

		getPerunBl().getAuditer().log(sess, new GroupMoved(movingGroup));
	}

	@Override
	public Group getGroupById(PerunSession sess, int id) throws InternalErrorException, GroupNotExistsException {
		return getGroupsManagerImpl().getGroupById(sess, id);
	}

	@Override
	public List<Group> getGroupsToSynchronize(PerunSession sess) throws InternalErrorException{
		return getGroupsManagerImpl().getGroupsToSynchronize(sess);
	}

	@Override
	public Group getGroupByName(PerunSession sess, Vo vo, String name) throws InternalErrorException, GroupNotExistsException {
		return getGroupsManagerImpl().getGroupByName(sess, vo, name);
	}

	@Override
	public void addMemberToMembersGroup(PerunSession sess, Group group, Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.addDirectMember(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.addAdmin and MembersManager.createMember.");
		}
	}

	@Override
	public void addMember(PerunSession sess, Group group, Member member) throws InternalErrorException, WrongReferenceAttributeValueException, AlreadyMemberException, WrongAttributeValueException, GroupNotExistsException {
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
	 */
	protected void addDirectMember(PerunSession sess, Group group, Member member) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException {

		if(this.groupsManagerImpl.isDirectGroupMember(sess, group, member)) throw new AlreadyMemberException(member);

		boolean memberWasIndirectInGroup = this.isGroupMember(sess, group, member);

		member = getGroupsManagerImpl().addMember(sess, group, member, MembershipType.DIRECT, group.getId());
		getPerunBl().getAuditer().log(sess, new DirectMemberAddedToGroup(member, group));

		//If member was indirect in group before, we don't need to change anything in other groups
		if(memberWasIndirectInGroup) return;
		// check all relations with this group and call addRelationMembers to reflect changes of adding member to group
		List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, group.getId());
		for (Integer groupId : relations) {
			addRelationMembers(sess, groupsManagerImpl.getGroupById(sess, groupId), Collections.singletonList(member), group.getId());
		}
		setRequiredAttributes(sess, member, group);

		// try to set init expiration
		try {
			extendMembershipInGroup(sess, member, group);
		} catch (ExtendMembershipException e) {
			throw new InternalErrorException("Failed to set initial member-group expiration date.");
		}

		if (!VosManager.MEMBERS_GROUP.equals(group.getName())) {

			// recalculate member group state
			recalculateMemberGroupStatusRecursively(sess, member, group);
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
		List<Member> oldMembers = this.getGroupMembers(sess, group);
		List<Member> membersToAdd = new ArrayList<>(members);

		for (Member member : membersToAdd) {
			groupsManagerImpl.addMember(sess, group, member, MembershipType.INDIRECT, sourceGroupId);
		}

		// select only newly added members
		membersToAdd.removeAll(oldMembers);

		for (Member member : membersToAdd) {
			setRequiredAttributes(sess, member, group);
			getPerunBl().getAuditer().log(sess, new IndirectMemberAddedToGroup(member, group));
		}

		return membersToAdd;
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
			} catch(WrongAttributeAssignmentException | AttributeNotExistsException | MemberResourceMismatchException ex) {
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
	 */
	private List<Member> removeIndirectMembers(PerunSession sess, Group group, List<Member> members, int sourceGroupId) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, NotGroupMemberException {
		List<Member> membersToRemove = new ArrayList<>(members);
		for (Member member: membersToRemove) {
			member.setSourceGroupId(sourceGroupId);
			groupsManagerImpl.removeMember(sess, group, member);
		}

		// get list of new members
		List<Member> newMembers = this.getGroupMembers(sess, group);
		// get only removed members
		membersToRemove.removeAll(newMembers);

		for(Member removedIndirectMember: membersToRemove) {
			notifyMemberRemovalFromGroup(sess, group, removedIndirectMember);
			//remove all member-group attributes because member is not part of group any more
			try {
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, removedIndirectMember, group);
			} catch (WrongAttributeAssignmentException ex) {
				//This should not happen
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, new IndirectMemberRemovedFromGroup(removedIndirectMember, group));
		}

		return membersToRemove;
	}

	@Override
	public void removeMember(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException, GroupNotExistsException {
		// Check if the group is NOT members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Cannot remove member directly from the members group.");
		} else {
			try {
				this.removeDirectMember(sess, group, member);
			} catch (WrongAttributeValueException | WrongReferenceAttributeValueException ex){
				throw new InternalErrorException(ex);
			}
		}
	}

	@Override
	public void removeMemberFromMembersOrAdministratorsGroup(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.removeDirectMember(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.removeAdmin and MembersManager.deleteMember.");
		}
	}

	private void removeDirectMember(PerunSession sess, Group group, Member member) throws InternalErrorException, NotGroupMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {

		member.setSourceGroupId(group.getId());
		getGroupsManagerImpl().removeMember(sess, group, member);
		if (this.getGroupsManagerImpl().isGroupMember(sess, group, member)) {
			getPerunBl().getAuditer().log(sess, new DirectMemberRemovedFromGroup(member, group));
			//If member was indirect in group before, we don't need to change anything in other groups
			return;
		} else {
			notifyMemberRemovalFromGroup(sess, group, member);
			//remove all member-group attributes because member is not part of group any more
			try {
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, member, group);
			} catch (WrongAttributeAssignmentException ex) {
				//This should not happen
				throw new InternalErrorException(ex);
			}
			getPerunBl().getAuditer().log(sess, new MemberRemovedFromGroupTotally(member, group));
		}

		// check all relations with this group and call removeRelationMembers to reflect changes of removing member from group
		List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, group.getId());
		for (Integer groupId : relations) {
			removeRelationMembers(sess, groupsManagerImpl.getGroupById(sess, groupId), Collections.singletonList(member), group.getId());
		}

		if (!VosManager.MEMBERS_GROUP.equals(group.getName())) {
			recalculateMemberGroupStatusRecursively(sess, member, group);
		}
	}

	/**
	 * When a member is removed from a group, and the group is in a role, the member's user loses that role, which may need processing.
	 *
	 * @param sess perun session
	 * @param group group
	 * @param member member
	 * @throws InternalErrorException
	 */
	private void notifyMemberRemovalFromGroup(PerunSession sess, Group group, Member member) throws InternalErrorException {
		log.debug("notifyMemberRemovalFromGroup(group={},member={})",group.getName(),member);
		User user = perunBl.getUsersManagerBl().getUserByMember(sess, member);
		//list of VOs for which the group is in role SPONSOR
		List<Vo> vos = AuthzResolverBlImpl.getVosForGroupInRole(sess, group, Role.SPONSOR);
		for (Vo vo : vos) {
			log.debug("Group {} has role SPONSOR in vo {}",group.getName(),vo.getShortName());
			//if the user is not SPONSOR directly or through another group, he/she loses the role
			if(!perunBl.getVosManagerBl().isUserInRoleForVo(sess, user, Role.SPONSOR, vo, true)) {
				log.debug("user {} lost role SPONSOR when removed from group {}",user.getLastName(),group.getName());
				perunBl.getVosManagerBl().handleUserLostVoRole(sess, user, vo, Role.SPONSOR);
			}
		}
	}

	@Override
	public List<Member> getGroupMembers(PerunSession sess, Group group) throws InternalErrorException {
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupMembers(sess, group));
	}

	@Override
	public Member getGroupMemberById(PerunSession sess, Group group, int memberId) throws InternalErrorException, NotGroupMemberException {
		List<Member> members = getGroupsManagerImpl().getGroupMembersById(sess, group, memberId);
		if (members.isEmpty()) throw new NotGroupMemberException("Member with ID="+memberId+" is not member of "+group+" or doesn't exists at all.");
		List<Member> filteredMembers = this.filterMembersByMembershipTypeInGroup(members);
		if (filteredMembers.size() == 0) throw new InternalErrorException("Filtering DIRECT/INDIRECT members resulted in empty members list.");
		if (filteredMembers.size() > 1) throw new ConsistencyErrorException("Filtering DIRECT/INDIRECT members resulted with >1 members with same ID.");
		return filteredMembers.get(0);
	}

	@Override
	public List<Member> getGroupDirectMembers(PerunSession sess, Group group) throws InternalErrorException {
		return groupsManagerImpl.getGroupMembersByMembership(sess, group, MembershipType.DIRECT);
	}

	@Override
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

	@Override
	public List<Member> getGroupMembersExceptInvalid(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupMembers(sess, group, Arrays.asList(Status.INVALID), true);
	}

	@Override
	public List<Member> getGroupMembersExceptInvalidAndDisabled(PerunSession sess, Group group) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupMembers(sess, group, Arrays.asList(Status.INVALID, Status.DISABLED), true);
	}

	@Override
	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException {
		return this.getGroupRichMembers(sess, group, null);
	}

	@Override
	public List<RichMember> getGroupDirectRichMembers(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> directMembers = getGroupDirectMembers(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, directMembers);
	}

	@Override
	public List<RichMember> getGroupRichMembersExceptInvalid(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> members = this.getGroupMembersExceptInvalid(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) throws InternalErrorException {
		List<Member> members = this.getGroupMembers(sess, group, status);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException {
		return this.getGroupRichMembersWithAttributes(sess, group, null);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributesExceptInvalid(PerunSession sess, Group group) throws InternalErrorException {
		List<RichMember> richMembers = this.getGroupRichMembersExceptInvalid(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) throws InternalErrorException {
		List<RichMember> richMembers = this.getGroupRichMembers(sess, group, status);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	@Override
	public int getGroupMembersCount(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> members = this.getGroupMembers(sess, group);
		return members.size();
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, AlreadyAdminException {
		AuthzResolverBlImpl.setRole(sess, user, group, Role.GROUPADMIN);
		getPerunBl().getAuditer().log(sess, new AdminAddedForGroup(user, group));
	}

	@Override
	public void addAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, AlreadyAdminException {
		List<Group> listOfAdmins = getAdminGroups(sess, group);
		if (listOfAdmins.contains(authorizedGroup)) throw new AlreadyAdminException(authorizedGroup);

		AuthzResolverBlImpl.setRole(sess, authorizedGroup, group, Role.GROUPADMIN);
		getPerunBl().getAuditer().log(sess, new AdminGroupAddedForGroup(authorizedGroup, group));
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, User user) throws InternalErrorException, UserNotAdminException {
		AuthzResolverBlImpl.unsetRole(sess, user, group, Role.GROUPADMIN);
		getPerunBl().getAuditer().log(sess, new AdminRemovedForGroup(user, group));
	}

	@Override
	public void removeAdmin(PerunSession sess, Group group, Group authorizedGroup) throws InternalErrorException, GroupNotAdminException {
		List<Group> listOfAdmins = getAdminGroups(sess, group);
		if (!listOfAdmins.contains(authorizedGroup)) throw new GroupNotAdminException(authorizedGroup);

		AuthzResolverBlImpl.unsetRole(sess, authorizedGroup, group, Role.GROUPADMIN);
		getPerunBl().getAuditer().log(sess, new AdminGroupRemovedFromGroup(authorizedGroup, group));
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Group group, boolean onlyDirectAdmins) throws InternalErrorException {
		if(onlyDirectAdmins) {
			return getGroupsManagerImpl().getDirectAdmins(perunSession, group);
		} else {
			return getGroupsManagerImpl().getAdmins(perunSession, group);
		}
	}

	@Override
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

	@Override
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

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
		return richUsers;
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdmins(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getDirectAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
		return richUsers;
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, group);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
		return richUsers;
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getRichAdmins(perunSession, group), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getDirectRichAdmins(perunSession, group), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	public List<Group> getAssignedGroupsToResource(PerunSession sess, Resource resource) throws InternalErrorException {
		return getAssignedGroupsToResource(sess, resource, false);
	}

	@Override
	public List<Group> getAssignedGroupsToResource(PerunSession sess, Resource resource, Member member) throws InternalErrorException {
		return getGroupsManagerImpl().getAssignedGroupsToResource(sess, resource, member);
	}

	@Override
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

	@Override
	public List<Group> getAssignedGroupsToFacility(PerunSession sess, Facility facility) throws InternalErrorException {
		return getGroupsManagerImpl().getAssignedGroupsToFacility(sess, facility);
	}

	@Override
	public List<Group> getAllGroups(PerunSession sess, Vo vo) throws InternalErrorException {
		List<Group> groups = getGroupsManagerImpl().getAllGroups(sess, vo);

		// Sort
		Collections.sort(groups);

		return groups;
	}

	@Override
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

	@Override
	public List<Group> getSubGroups(PerunSession sess, Group parentGroup) throws InternalErrorException {
		List<Group> subGroups = getGroupsManagerImpl().getSubGroups(sess, parentGroup);

		// Sort
		Collections.sort(subGroups);

		return subGroups;
	}

	@Override
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

	@Override
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

	@Override
	public List<Group> getGroups(PerunSession sess, Vo vo) throws InternalErrorException {
		List<Group> groups = getGroupsManagerImpl().getGroups(sess, vo);

		Collections.sort(groups);

		return groups;
	}

	@Override
	public List<Group> getGroupsByIds(PerunSession sess, List<Integer> groupsIds) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupsByIds(sess, groupsIds);
	}

	@Override
	public int getGroupsCount(PerunSession sess, Vo vo) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupsCount(sess, vo);
	}

	@Override
	public int getGroupsCount(PerunSession sess) throws InternalErrorException {
		return getGroupsManagerImpl().getGroupsCount(sess);
	}

	@Override
	public int getSubGroupsCount(PerunSession sess, Group parentGroup) throws InternalErrorException {
		return getGroupsManagerImpl().getSubGroupsCount(sess, parentGroup);
	}

	@Override
	public Vo getVo(PerunSession sess, Group group) throws InternalErrorException {
		int voId = getGroupsManagerImpl().getVoId(sess, group);
		try {
			return getPerunBl().getVosManagerBl().getVoById(sess, voId);
		} catch (VoNotExistsException e) {
			throw new ConsistencyErrorException("Group belongs to the non-existent VO", e);
		}
	}

	@Override
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

	@Override
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

	@Override
	public List<Group> getMemberGroupsByAttribute(PerunSession sess, Member member, Attribute attribute) throws WrongAttributeAssignmentException,InternalErrorException {
		List<Group> memberGroups = this.getAllMemberGroups(sess, member);
		memberGroups.retainAll(this.getGroupsByAttribute(sess, attribute));
		return memberGroups;
	}

	@Override
	public List<Group> getAllMemberGroups(PerunSession sess, Member member) throws InternalErrorException {
		return getGroupsManagerImpl().getAllMemberGroups(sess, member);
	}

	@Override
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

	@Override
	public List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> members = this.getParentGroupMembers(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) throws InternalErrorException {
		List<RichMember> richMembers = this.getParentGroupRichMembers(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	@Override
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
	@Override
	public List<String> synchronizeGroup(PerunSession sess, Group group) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException, ExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException {
		//needed variables for whole method
		List<String> skippedMembers = new ArrayList<>();
		ExtSource source = null;
		ExtSource membersSource = null;

		try {
			long startTime = System.nanoTime();
			getPerunBl().getAuditer().log(sess,new GroupSyncStarted(group));
			log.debug("Group synchronization for {} has been started.", group);

			// Initialization of group extSource
			source = getGroupExtSourceForSynchronization(sess, group);

			// Initialization of groupMembers extSource (if it is set), in other case set membersSource = source
			membersSource = getGroupMembersExtSourceForSynchronization(sess, group, source);

			// Prepare info about userAttributes which need to be overwrite (not just updated) and memberAttributes which need to be merged )not overwrite
			List<String> overwriteUserAttributesList = getOverwriteUserAttributesListFromExtSource(membersSource);
			List<String> mergeMemberAttributesList = getMemberAttributesListToBeMergedFromExtSource(membersSource);

			// Get info about type of synchronization (with or without update)
			boolean lightweightSynchronization = isThisLightweightSynchronization(sess, group);

			log.debug("Group synchronization {}: using configuration extSource for membership {}, extSource for members {}", new Object[] {group, membersSource, membersSource.getName()});

			// Prepare containers for work with group members
			List<Candidate> candidatesToAdd = new ArrayList<>();
			Map<Candidate, RichMember> membersToUpdate = new HashMap<>();
			List<RichMember> membersToRemove = new ArrayList<>();

			//get all direct members of synchronized group (only direct, because we want to set direct membership with this group by synchronization)
			List<RichMember> actualGroupMembers = getPerunBl().getGroupsManagerBl().getGroupDirectRichMembers(sess, group);

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
			updateExistingMembersWhileSynchronization(sess, group, membersToUpdate, overwriteUserAttributesList, mergeMemberAttributesList);
			// Add not presented candidates to group
			addMissingMembersWhileSynchronization(sess, group, candidatesToAdd, overwriteUserAttributesList, mergeMemberAttributesList, skippedMembers);
			// If status is full, remove members which are not already members of group
			if (status.equals(GroupsManager.GROUP_SYNC_STATUS_FULL)) {
				removeFormerMembersWhileSynchronization(sess, group, membersToRemove);
			}

			long endTime = System.nanoTime();
			getPerunBl().getAuditer().log(sess,new GroupSyncFinished(group, startTime, endTime));
			log.info("Group synchronization for {} has been finished.", group);
		} finally {
			closeExtSourcesAfterSynchronization(membersSource, source);
		}

		return skippedMembers;
	}

	/**
	 * Adds the group on the first place to the queue of groups waiting for synchronization.
	 *
	 * @param group the group to be forced this way
	 *
	 * @throws GroupSynchronizationAlreadyRunningException when group synchronization is already running at this moment
	 */
	@Override
	public void forceGroupSynchronization(PerunSession sess, Group group) throws GroupSynchronizationAlreadyRunningException, InternalErrorException {
		//Check if the group is not currently in synchronization process
		if(poolOfGroupsToBeSynchronized.putJobIfAbsent(group, true)) {
			log.debug("Scheduling synchronization for the group {} by force!", group);
		} else {
			throw new GroupSynchronizationAlreadyRunningException(group);
		}
	}

	/**
	 * Start and check threads with synchronization of groups. (max threads is defined by constant)
	 * It also add new groups to the queue.
	 * This method is run by the scheduler every 5 minutes.
	 *
	 * Note: this method is synchronized
	 *
	 * @throws InternalErrorException
	 */
	@Override
	public synchronized void synchronizeGroups(PerunSession sess) throws InternalErrorException {
		// Get the default synchronization interval and synchronization timeout from the configuration file
		int timeout = BeansUtils.getCoreConfig().getGroupSynchronizationTimeout();
		int defaultIntervalMultiplier = BeansUtils.getCoreConfig().getGroupSynchronizationInterval();
		// Get the number of seconds from the epoch, so we can divide it by the synchronization interval value
		long minutesFromEpoch = System.currentTimeMillis()/1000/60;

		int numberOfNewlyRemovedThreads = 0;
		// Firstly interrupt threads after timeout, then remove all interrupted threads
		Iterator<GroupSynchronizerThread> threadIterator = groupSynchronizerThreads.iterator();
		while(threadIterator.hasNext()) {
			GroupSynchronizerThread thread = threadIterator.next();
			long threadStart = thread.getStartTime();
			//If thread start time is 0, this thread is waiting for another job, skip it
			if(threadStart == 0) continue;

			long timeDiff = System.currentTimeMillis() - threadStart;
			//If thread was interrupted by anything, remove it from the pool of active threads
			if (thread.isInterrupted()) {
				numberOfNewlyRemovedThreads++;
				threadIterator.remove();
			} else if(timeDiff/1000/60 > timeout) {
				// If the time is greater than timeout set in the configuration file (in minutes), interrupt and remove this thread from pool
				log.error("One of threads was interrupted because of timeout!");
				thread.interrupt();
				threadIterator.remove();
				numberOfNewlyRemovedThreads++;
			}
		}

		int numberOfNewlyCreatedThreads = 0;
		// Start new threads if there is place for them
		while(groupSynchronizerThreads.size() < maxConcurentGroupsToSynchronize) {
			GroupSynchronizerThread thread = new GroupSynchronizerThread(sess);
			thread.start();
			groupSynchronizerThreads.add(thread);
			numberOfNewlyCreatedThreads++;
			log.debug("New thread for synchronization started.");
		}

		// Get the groups with synchronization enabled
		List<Group> groups = groupsManagerImpl.getGroupsToSynchronize(sess);

		int numberOfNewlyAddedGroups = 0;
		for (Group group: groups) {
			// Get the synchronization interval for the group
			int intervalMultiplier;
			try {
				Attribute intervalAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME);
				if (intervalAttribute.getValue() != null) {
					intervalMultiplier = Integer.parseInt((String) intervalAttribute.getValue());
				} else {
					intervalMultiplier = defaultIntervalMultiplier;
					log.debug("Group {} hasn't set synchronization interval, using default {} seconds", group, intervalMultiplier);
				}
			} catch (AttributeNotExistsException e) {
				log.error("Required attribute {} isn't defined in Perun! Using default value from properties instead!", GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME);
				intervalMultiplier = defaultIntervalMultiplier;
			} catch (WrongAttributeAssignmentException e) {
				log.error("Cannot get attribute " + GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME + " for group " + group + " due to exception. Using default value from properties instead!",e);
				intervalMultiplier = defaultIntervalMultiplier;
			}

			// Multiply with 5 to get real minutes
			intervalMultiplier = intervalMultiplier*5;

			// If the minutesFromEpoch can be divided by the intervalMultiplier, then synchronize
			if ((minutesFromEpoch % intervalMultiplier) == 0) {
				if (poolOfGroupsToBeSynchronized.putJobIfAbsent(group, false)) {
					numberOfNewlyAddedGroups++;
					log.debug("Group {} was added to the pool of groups waiting for synchronization.", group);
				} else {
					log.debug("Group {} synchronzation is already running.", group);
				}
			}
		}

		// Save state of synchronization to the info log
		log.info("SynchronizeGroups method ends with these states: " +
				"'number of newly removed threads'='" + numberOfNewlyRemovedThreads + "', " +
				"'number of newly created threads'='" + numberOfNewlyCreatedThreads + "', " +
				"'number of newly added groups to the pool'='" + numberOfNewlyAddedGroups + "', " +
				"'right now synchronized groups'='" + poolOfGroupsToBeSynchronized.getRunningJobs() + "', " +
				"'right now waiting groups'='" + poolOfGroupsToBeSynchronized.getWaitingJobs() + "'.");
	}

	private class GroupSynchronizerThread extends Thread {

		// all synchronization runs under synchronizer identity.
		private final PerunPrincipal pp = new PerunPrincipal("perunSynchronizer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		private final PerunBl perunBl;
		private final PerunSession sess;
		private volatile long startTime;

		public GroupSynchronizerThread(PerunSession sess) throws InternalErrorException {
			// take only reference to perun
			this.perunBl = (PerunBl) sess.getPerun();
			this.sess = perunBl.getPerunSession(pp, new PerunClient());
			//Default settings of not running thread (waiting for another group)
			this.startTime = 0;
		}

		@Override
		public void run() {
			while (true) {
				//Set thread to default state (waiting for another group to synchronize)
				this.setThreadToDefaultState();

				//If this thread was interrupted, end it's running
				if(this.isInterrupted()) return;

				//text of exception if was thrown, null in exceptionMessage means "no exception, it's ok"
				String exceptionMessage = null;
				//text with all skipped members and reasons of this skipping
				String skippedMembersMessage = null;
				//if exception which produce fail of whole synchronization was thrown
				boolean failedDueToException = false;

				//Take another group from the pool to synchronize it
				Group group = null;
				try {
					group = poolOfGroupsToBeSynchronized.takeJob();
				} catch (InterruptedException ex) {
					log.error("Thread was interrupted when trying to take another group to synchronize from pool", ex);
					//Interrupt this thread
					this.interrupt();
					return;
				}

				try {
					// Set the start time, so we can check the timeout of the thread
					startTime = System.currentTimeMillis();

					log.debug("Synchronization thread started synchronization for group {}.", group);

					//synchronize Group and get information about skipped Members
					List<String> skippedMembers = perunBl.getGroupsManagerBl().synchronizeGroup(sess, group);

					if (!skippedMembers.isEmpty()) {
						skippedMembersMessage = "These members from extSource were skipped: { ";

						for (String skippedMember : skippedMembers) {
							if (skippedMember == null) continue;

							skippedMembersMessage += skippedMember + ", ";
						}
						skippedMembersMessage += " }";
						exceptionMessage = skippedMembersMessage;
					}
					log.debug("Synchronization thread for group {} has finished in {} ms.", group, System.currentTimeMillis() - startTime);
				} catch (WrongAttributeValueException | WrongReferenceAttributeValueException | InternalErrorException |
						WrongAttributeAssignmentException  | GroupNotExistsException |
						AttributeNotExistsException  | ExtSourceNotExistsException e) {
					failedDueToException = true;
					exceptionMessage = "Cannot synchronize group ";
					log.error(exceptionMessage + group, e);
					exceptionMessage += "due to exception: " + e.getName() + " => " + e.getMessage();
				} catch (Exception e) {
					failedDueToException = true;
					exceptionMessage = "Cannot synchronize group ";
					log.error(exceptionMessage + group, e);
					exceptionMessage += "due to unexpected exception: " + e.getClass().getName() + " => " + e.getMessage();
				} finally {
					//Save information about group synchronization, this method run in new transaction
					try {
						perunBl.getGroupsManagerBl().saveInformationAboutGroupSynchronization(sess, group, startTime, failedDueToException, exceptionMessage);
					} catch (Exception ex) {
						log.error("When synchronization group " + group + ", exception was thrown.", ex);
						log.error("Info about exception from synchronization: {}", skippedMembersMessage);
					}
					//Remove job from running jobs
					if(!poolOfGroupsToBeSynchronized.removeJob(group)) {
						log.error("Can't remove running job for object " + group + " from pool of running jobs because it is not containing it.");
					}

					log.debug("GroupSynchronizerThread finished for group: {}", group);
				}
			}
		}

		public long getStartTime() {
			return startTime;
		}

		private void setThreadToDefaultState() {
			this.startTime = 0;
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

	@Override
	public List<Group> getGroupsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR);
		if(!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
		return getGroupsManagerImpl().getGroupsByAttribute(sess, attribute);
	}

	@Override
	public List<Pair<Group, Resource>> getGroupResourcePairsByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException {
		getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		if(!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
		return getGroupsManagerImpl().getGroupResourcePairsByAttribute(sess, attribute);
	}

	@Override
	public boolean isGroupMember(PerunSession sess, Group group, Member member) throws InternalErrorException {
		return getGroupsManagerImpl().isGroupMember(sess, group, member);
	}

	@Override
	public void checkGroupExists(PerunSession sess, Group group) throws InternalErrorException, GroupNotExistsException {
		getGroupsManagerImpl().checkGroupExists(sess, group);
	}

	@Override
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

	@Override
	public List<Member> filterMembersByMembershipTypeInGroup(List<Member> members) throws InternalErrorException {
		Set<Member> filteredMembers = new HashSet<>();
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
			boolean alreadyAdded = false;

			for (Member filteredMember : filteredMembers) {
				if (filteredMember.equals(m)) {
					filteredMember.putGroupStatuses(m.getGroupStatuses());
					alreadyAdded = true;
					break;
				}
			}

			if (!alreadyAdded) {
				filteredMembers.add(m);
			}
		}

		return new ArrayList<>(filteredMembers);
	}

	@Override
	public RichGroup filterOnlyAllowedAttributes(PerunSession sess, RichGroup richGroup) throws InternalErrorException {
		if(richGroup == null) throw new InternalErrorException("RichGroup can't be null.");

		//Filtering richGroup attributes
		if(richGroup.getAttributes() != null) {
			List<Attribute> groupAttributes = richGroup.getAttributes();
			List<Attribute> allowedGroupAttributes = new ArrayList<Attribute>();
			for(Attribute groupAttr : groupAttributes) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, groupAttr, richGroup)) {
					groupAttr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttr, richGroup));
					allowedGroupAttributes.add(groupAttr);
				}
			}

			richGroup.setAttributes(allowedGroupAttributes);
		}
		return richGroup;
	}

	@Override
	public List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups) throws InternalErrorException {
		List<RichGroup> filteredRichGroups = new ArrayList<RichGroup>();
		if(richGroups == null || richGroups.isEmpty()) return filteredRichGroups;

		for(RichGroup rg : richGroups) {
			filteredRichGroups.add(this.filterOnlyAllowedAttributes(sess, rg));
		}

		return filteredRichGroups;
	}

	@Override
	public List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups, Resource resource, boolean useContext) throws InternalErrorException {

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
			String facilityadmin = ((AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) ? "FACILITYADMIN" : ""));
			String key = voadmin + voobserver + groupadmin + facilityadmin;

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
						boolean canRead = false;
						if (groupAttr.getNamespace().startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
							canRead = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, groupAttr, rg, resource);
						} else if (groupAttr.getNamespace().startsWith(AttributesManager.NS_GROUP_ATTR)) {
							canRead = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, groupAttr, rg, null);
						}
						if(canRead) {
							boolean isWritable = false;
							if (groupAttr.getNamespace().startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
								isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttr, rg, resource);
							} else if (groupAttr.getNamespace().startsWith(AttributesManager.NS_GROUP_ATTR)) {
								isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttr, rg, null);
							}
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

	@Override
	public RichGroup convertGroupToRichGroupWithAttributes(PerunSession sess, Group group) throws InternalErrorException{
		return new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group));
	}

	@Override
	public RichGroup convertGroupToRichGroupWithAttributesByName(PerunSession sess, Group group, List<String> attrNames) throws InternalErrorException{
		if (attrNames == null) return convertGroupToRichGroupWithAttributes(sess, group);
		return new RichGroup(group,this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group, attrNames));
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, List<Group> groups) throws InternalErrorException {
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group)));
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, Resource resource, List<Group> groups) throws InternalErrorException, GroupResourceMismatchException {
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group, true)));
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, List<Group> groups, List<String> attrNames) throws InternalErrorException {
		if (attrNames == null) return convertGroupsToRichGroupsWithAttributes(sess, groups);
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group, attrNames)));
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, Resource resource, List<Group> groups, List<String> attrNames) throws InternalErrorException, GroupResourceMismatchException {
		if (attrNames == null) return convertGroupsToRichGroupsWithAttributes(sess, resource, groups);
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group, attrNames, true)));
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> getRichGroupsWithAttributesAssignedToResource(PerunSession sess, Resource resource, List<String> attrNames) throws InternalErrorException {
		List<Group> assignedGroups = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		try {
			return this.convertGroupsToRichGroupsWithAttributes(sess, resource, assignedGroups, attrNames);
		} catch (GroupResourceMismatchException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames)throws InternalErrorException{
		return convertGroupsToRichGroupsWithAttributes(sess, this.getAllGroups(sess, vo), attrNames);
	}

	@Override
	public List<RichGroup> getMemberRichGroupsWithAttributesByNames(PerunSession sess, Member member, List<String> attrNames) throws InternalErrorException {
		return convertGroupsToRichGroupsWithAttributes(sess, this.getMemberGroups(sess, member), attrNames);
	}

	@Override
	public List<RichGroup> getRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames)throws InternalErrorException{
		return convertGroupsToRichGroupsWithAttributes(sess, this.getSubGroups(sess, parentGroup), attrNames);
	}

	@Override
	public List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames)throws InternalErrorException{
		return convertGroupsToRichGroupsWithAttributes(sess, this.getAllSubGroups(sess, parentGroup), attrNames);
	}

	@Override
	public RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames)throws InternalErrorException, GroupNotExistsException{
		return convertGroupToRichGroupWithAttributesByName(sess, this.getGroupById(sess, groupId), attrNames);
	}

	@Override
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
			//Log info about synchronization problems to audit log and to the perun system log
			if(failedDueToException) {
				getPerunBl().getAuditer().log(sess,new GroupSyncFailed(group));
				log.debug("{} synchronization failed because of {}", group, originalExceptionMessage);
			} else {
				getPerunBl().getAuditer().log(sess,new GroupSyncFinishedWithErrors(group));
				log.debug("{} synchronization finished with errors: {}", group, originalExceptionMessage);
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
		Map<String, RichMember> loginsOfMembers = getMapOfLoginsAndRichMembers(loginSource, actualGroupMembers, membersToRemove);

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
		Map<String, RichMember> loginsOfMembers = getMapOfLoginsAndRichMembers(loginSource, actualGroupMembers, membersToRemove);

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
	 * From membersSource extSource get attribute mergeMemberAttributes and prepare
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
	private List<String> getMemberAttributesListToBeMergedFromExtSource(ExtSource membersSource) throws InternalErrorException {
		Map<String, String> membersSourceAttributes = getPerunBl().getExtSourcesManagerBl().getAttributes(membersSource);
		List<String> mergeMemberAttributesList = new ArrayList<>();
		String mergeMemberAttributes = membersSourceAttributes.get("mergeMemberAttributes");
		if(mergeMemberAttributes != null && !mergeMemberAttributes.isEmpty()) {
			//remove all white spaces and invisible characters
			mergeMemberAttributes = mergeMemberAttributes.replaceAll("\\s", "");
			mergeMemberAttributesList = Arrays.asList(mergeMemberAttributes.split(","));
		}
		return mergeMemberAttributesList;
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
	 * @param mergeMemberAttributesList list of member attributes to be merged instead of updated
	 *
	 * @throws InternalErrorException if some internal error occurs
	 * @throws AttributeNotExistsException if some attributes not exists and for this reason can't be updated
	 * @throws WrongAttributeAssignmentException if some attribute is updated in bad way (bad assignment)
	 */
	private void updateExistingMembersWhileSynchronization(PerunSession sess, Group group, Map<Candidate, RichMember> membersToUpdate, List<String> overwriteUserAttributesList, List<String> mergeMemberAttributesList) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
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
			updateAttributes(sess, richMember, group, candidate, overwriteUserAttributesList, mergeMemberAttributesList);

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
	private void updateAttributes(PerunSession sess, RichMember richMember, Group group, Candidate candidate, List<String> overwriteUserAttributesList, List<String> mergeMemberAttributesList) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		for (String attributeName : candidate.getAttributes().keySet()) {
			// Update member attribute
			if (attributeName.startsWith(AttributesManager.NS_MEMBER_ATTR)) {
				boolean attributeFound = false;
				for (Attribute memberAttribute : richMember.getMemberAttributes()) {
					if (memberAttribute.getName().equals(attributeName)) {
						attributeFound = true;
						Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), memberAttribute.getType());
						if (subjectAttributeValue != null && !Objects.equals(memberAttribute.getValue(), subjectAttributeValue)) {
							log.trace("Group synchronization {}: value of the attribute {} for memberId {} changed. Original value {}, new value {}.",
									new Object[]{group, memberAttribute, richMember.getId(), memberAttribute.getValue(), subjectAttributeValue});
							memberAttribute.setValue(subjectAttributeValue);
							try {
								if (mergeMemberAttributesList.contains(memberAttribute.getName())) {
									getPerunBl().getAttributesManagerBl().mergeAttributeValueInNestedTransaction(sess, richMember, memberAttribute);
								} else {
									getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember, memberAttribute);
								}
							} catch (AttributeValueException e) {
								// There is a problem with attribute value, so set INVALID status for the member
								getPerunBl().getMembersManagerBl().invalidateMember(sess, richMember);
							} catch (WrongAttributeAssignmentException e) {
								throw new ConsistencyErrorException(e);
							}
						}
						// We found it, but there is no change;
						break;
					}
				}
				// Member has not set this attribute so set it now if possible
				if (!attributeFound) {
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
			} else if (attributeName.startsWith(AttributesManager.NS_USER_ATTR)) {
				boolean attributeFound = false;
				for (Attribute userAttribute : richMember.getUserAttributes()) {
					if (userAttribute.getName().equals(attributeName)) {
						attributeFound = true;
						Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), userAttribute.getType());
						if (!Objects.equals(userAttribute.getValue(), subjectAttributeValue)) {
							log.trace("Group synchronization {}: value of the attribute {} for memberId {} changed. Original value {}, new value {}.",
									new Object[]{group, userAttribute, richMember.getId(), userAttribute.getValue(), subjectAttributeValue});
							userAttribute.setValue(subjectAttributeValue);
							try {
								// Choose set or merge by extSource attribute overwriteUserAttributes (if contains this one)
								if (overwriteUserAttributesList.contains(userAttribute.getName())) {
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
				if (!attributeFound) {
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
			} else if (attributeName.equals(MembersManager.MEMBERGROUPHASHCODE_ATTRNAME)) {
				AttributeDefinition attributeDefinition = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, MembersManager.MEMBERGROUPHASHCODE_ATTRNAME);
				Attribute hashCode = new Attribute(attributeDefinition);
				hashCode.setValue(candidate.getAttributes().get(attributeName));
				if (hashCode.getValue() != null && !hashCode.getValue().equals("")) {
					// Set hash code to member-group relationship
					try {
						getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, richMember, group, hashCode);
						log.trace("Group synchronization: Setting the {} hashCode {} to member {} in group {}", new Object[]{hashCode, richMember, group});
					} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
						log.error("Group synchronization: Attribute with hash code {} can't be set to member {} in group {}", new Object[]{hashCode, richMember, group});
					}
				} else {
					//we are not supporting other attributes then member or user so skip it without error, but log it
					log.warn("Attribute {} can't be set, because it is not member or user attribute.", attributeName);
				}
			}
		}
	}

	/**
	 * Get map where key is login of member in ExtSource and value is RichMember
	 * Add RichMember to membersToRemove when RichMember doesn`t have ExtSource as loginSource
	 * @param loginSource
	 * @param actualGroupMembers
	 * @param membersToRemove
	 *
	 * @return map of logins and rich members
	 */
	private Map<String, RichMember> getMapOfLoginsAndRichMembers(ExtSource loginSource, List<RichMember> actualGroupMembers, List<RichMember> membersToRemove) {
		// Create map where key is login of member in external source and value is RichMember
		Map<String, RichMember> loginsOfMembers = new HashMap<>();
		for (RichMember actualMember: actualGroupMembers) {
			List<UserExtSource> userExtSources = actualMember.getUserExtSources();
			boolean hasRichMemberExtSource = false;
			for (UserExtSource ues: userExtSources) {
				if (ues.getExtSource().equals(loginSource)) {
					hasRichMemberExtSource = true;
					loginsOfMembers.put(ues.getLogin(), actualMember);
					break;
				}
			}
			if (!hasRichMemberExtSource) {
				membersToRemove.add(actualMember);
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
	 * @param mergeMemberAttributesList list of attributes to be merged for member if found
	 * @param skippedMembers list of not successfully synchronized members
	 *
	 * @throws InternalErrorException if some internal error occurs
	 */
	private void addMissingMembersWhileSynchronization(PerunSession sess, Group group, List<Candidate> candidatesToAdd, List<String> overwriteUserAttributesList, List<String> mergeMemberAttributesList, List<String> skippedMembers) throws InternalErrorException {
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
					updateExistingMembersWhileSynchronization(sess, group, memberMap, overwriteUserAttributesList, mergeMemberAttributesList);
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
							updateExistingMembersWhileSynchronization(sess, group, memberMap, overwriteUserAttributesList, mergeMemberAttributesList);
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
					try {
						getPerunBl().getGroupsManagerBl().addMember(sess, group, member);

					} catch (GroupNotExistsException ex) {
						// Shouldn't happen, because every group has at least Members group as a parent
						// Shouldn't happen, group should always exist
						throw new ConsistencyErrorException(ex);
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
	 */
	private void removeFormerMembersWhileSynchronization(PerunSession sess, Group group, List<RichMember> membersToRemove) throws InternalErrorException, WrongAttributeAssignmentException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//First get information if this group is authoritative group
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
									log.debug("Group synchronization {}: Member id {} disabled because synchronizer wants to remove him from last authoritativeGroup in Vo.", group, member.getId());
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
	public void addRelationMembers(PerunSession sess, Group resultGroup, List<Member> changedMembers, int sourceGroupId) throws InternalErrorException, AlreadyMemberException, WrongReferenceAttributeValueException, WrongAttributeValueException, GroupNotExistsException {
		List<Member> newMembers;

		newMembers = addIndirectMembers(sess, resultGroup, changedMembers, sourceGroupId);

		if(newMembers.isEmpty()) {
			return;
		}

		List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, resultGroup.getId());
		for (Integer groupId : relations) {
			addRelationMembers(sess, groupsManagerImpl.getGroupById(sess, groupId), newMembers, resultGroup.getId());
		}
	}

	@Override
	public void removeRelationMembers(PerunSession sess, Group resultGroup, List<Member> changedMembers, int sourceGroupId) throws WrongReferenceAttributeValueException, NotGroupMemberException, WrongAttributeValueException, InternalErrorException, GroupNotExistsException {
		List<Member> members;

		members = removeIndirectMembers(sess, resultGroup, changedMembers, sourceGroupId);

		if(members.isEmpty()){
			return;
		}

		List<Integer> relations = groupsManagerImpl.getResultGroupsIds(sess, resultGroup.getId());
		for (Integer groupId : relations) {
			removeRelationMembers(sess, groupsManagerImpl.getGroupById(sess, groupId), members, resultGroup.getId());
		}
	}

	@Override
	public Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws WrongReferenceAttributeValueException, WrongAttributeValueException, GroupNotExistsException, InternalErrorException, GroupRelationAlreadyExists, GroupRelationNotAllowed {

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
		try {
			addRelationMembers(sess, resultGroup, getGroupMembers(sess, operandGroup), operandGroup.getId());
		} catch(AlreadyMemberException ex) {
			throw new ConsistencyErrorException("AlreadyMemberException caused by DB inconsistency.",ex);
		}

		// calculate new member-group statuses
		for (Member member : getGroupMembers(sess, operandGroup)) {
			recalculateMemberGroupStatusRecursively(sess, member, operandGroup);
		}

		return resultGroup;
	}

	@Override
	public void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws InternalErrorException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, GroupNotExistsException {
		if (!groupsManagerImpl.isOneWayRelationBetweenGroups(resultGroup, operandGroup)) {
			throw new GroupRelationDoesNotExist("Union does not exist between result group " + resultGroup + " and operand group" + operandGroup + ".");
		}

		if (parentFlag || groupsManagerImpl.isRelationRemovable(sess, resultGroup, operandGroup)) {
			try {
				removeRelationMembers(sess, resultGroup, getGroupMembers(sess, operandGroup), operandGroup.getId());
			} catch (WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException("Removing relation members failed. Cause: {}", ex);
			} catch (NotGroupMemberException ex) {
				throw new ConsistencyErrorException("Database inconsistency. Cause: {}", ex);
			}
		} else {
			throw new GroupRelationCannotBeRemoved("Union between result group " + resultGroup + " and operand group" + operandGroup +
					" cannot be removed, because it's part of the hierarchical structure of the groups.");
		}

		groupsManagerImpl.removeGroupUnion(sess, resultGroup, operandGroup);

		// recalculates statuses of members in result group
		for (Member member : getGroupMembers(sess, resultGroup)) {
			recalculateMemberGroupStatusRecursively(sess, member, resultGroup);
		}
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
	 * Method sets subGroups names by their parent group
	 * Private method for moving groups
	 *
	 * @param sess perun session
	 * @param subGroups groups with same parent group
	 * @param parentGroup of subGroups
	 * @throws InternalErrorException
	 */
	private void setSubGroupsNames(PerunSession sess, List<Group> subGroups, Group parentGroup) throws InternalErrorException {
		for (Group subGroup : subGroups) {
			subGroup.setName(parentGroup.getName() + ":" + subGroup.getShortName());

			//we have to update each subGroup name in database
			getGroupsManagerImpl().updateGroupName(sess, subGroup);

			// create auditer message for every updated group
			getPerunBl().getAuditer().log(sess, new GroupUpdated(subGroup));

			setSubGroupsNames(sess, getSubGroups(sess, subGroup), subGroup);
		}
	}

	/**
	 * Method remove old relation between moving group and its parent,
	 * also create new relation between destination and moving group if destination is not null.
	 * After calling this method, proper name and parentGroupId
	 * must be set to the moving group for keeping DB consistency!
	 * Private method for moving groups.
	 *
	 * @param sess perun session
	 * @param destinationGroup
	 * @param movingGroup
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	private void processRelationsWhileMovingGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		//We have to remove old group relation, if moving group is not top level group
		if(movingGroup.getParentGroupId() != null){
			try {
				removeGroupUnion(sess, getParentGroup(sess, movingGroup), movingGroup,true );
			} catch (GroupRelationDoesNotExist e) {
				//that should never happened
				throw new InternalErrorException("Group relation does not exists between group " + movingGroup + "and its parent group.");
			} catch (GroupRelationCannotBeRemoved e) {
				//that should never happened
				throw new InternalErrorException("Group relation cannot be removed between group " + movingGroup + "and its parent group.");
			} catch (ParentGroupNotExistsException e) {
				//That should never happened
				throw new InternalErrorException("Parent group does not exists for group " + movingGroup);
			} catch (GroupNotExistsException e) {
				throw new ConsistencyErrorException("Some group does not exists while removing group union.", e);
			}
		}

		//we have to create new group relation if destination group is not null
		if (destinationGroup != null) {
			try {
				createGroupUnion(sess, destinationGroup, movingGroup, true);
			} catch (GroupRelationAlreadyExists e) {
				//that should noever happened
				throw new InternalErrorException("Group relation already exists between destination group "  + destinationGroup + " and moving group " + movingGroup + ".");
			} catch (GroupRelationNotAllowed e) {
				//that should never happened
				throw new InternalErrorException("Group relation cannot be created between destination group "  + destinationGroup + " and moving group " + movingGroup + ".");
			} catch (GroupNotExistsException e) {
				throw new ConsistencyErrorException("Some group does not exists while creating group union.", e);
			}
		}
	}

	@Override
	public void expireMemberInGroup(PerunSession sess, Member member, Group group) throws InternalErrorException {

		if (group == null) {
			throw new InternalErrorException("Group can not be null.");
		}

		if (VosManager.MEMBERS_GROUP.equals(group.getName())) {
			throw new InternalErrorException("Can not expire member in members group.");
		}

		if (member == null) {
			throw new InternalErrorException("Member to expire can not be null");
		}

		MemberGroupStatus previousStatus = getDirectMemberGroupStatus(sess, member, group);

		if (MemberGroupStatus.EXPIRED.equals(previousStatus)) {
			log.warn("Expiring member in group where is already expired. Member: {}, Group: {}", member, group);
		}

		// expire in given group
		groupsManagerImpl.setDirectGroupStatus(sess, member, group, MemberGroupStatus.EXPIRED);

		recalculateMemberGroupStatusRecursively(sess, member, group);
	}

	@Override
	public void validateMemberInGroup(PerunSession sess, Member member, Group group) throws InternalErrorException {

		if (group == null) {
			throw new InternalErrorException("Group can not be null.");
		}

		if (VosManager.MEMBERS_GROUP.equals(group.getName())) {
			throw new InternalErrorException("Can not validate member in members group.");
		}

		if (member == null) {
			throw new InternalErrorException("Member to validate can not be null");
		}

		MemberGroupStatus previousStatus = getDirectMemberGroupStatus(sess, member, group);

		if (MemberGroupStatus.VALID.equals(previousStatus)) {
			log.warn("Validating member in group where is already validated. Member: {}, Group: {}", member, group);
		}

		// validate member in given group
		groupsManagerImpl.setDirectGroupStatus(sess, member, group, MemberGroupStatus.VALID);

		recalculateMemberGroupStatusRecursively(sess, member, group);
	}


	@Override
	public MemberGroupStatus getDirectMemberGroupStatus(PerunSession session, Member member, Group group) throws InternalErrorException {
		return groupsManagerImpl.getDirectMemberGroupStatus(session, member, group);
	}

	@Override
	public MemberGroupStatus getTotalMemberGroupStatus(PerunSession session, Member member, Group group) throws InternalErrorException {
		return groupsManagerImpl.getTotalMemberGroupStatus(session, member, group);
	}

	/**
	 * Calculates the state of given member in given group and if
	 * it differs from given 'previousState' calls this method recursively
	 * for all parent groups.
	 *
	 * @param member member
	 * @param group group
	 * @throws InternalErrorException internal error
	 */
	@Override
	public void recalculateMemberGroupStatusRecursively(PerunSession sess, Member member, Group group) throws InternalErrorException {

		if (member == null) {
			throw new InternalErrorException("Member, which should be checked, can not be null.");
		}

		if (group == null) {
			throw new InternalErrorException("Group, where members status should be recalculated, can not be null.");
		}

		// skip members group where all members are valid all the time
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			return;
		}

		MemberGroupStatus newStatus = getTotalMemberGroupStatus(sess, member, group);

		boolean saveStatuses = true;
		// member has been removed from group, we need to calculate its statuses in any result groups
		// but we can not save statuses because the relations should be already removed
		if (newStatus == null) {
			saveStatuses = false;
		}

		// get all possibly affected groups and member's statuses for them
		List<Group> affectedGroups = new ArrayList<>(groupsManagerImpl.getResultGroups(sess, group.getId()));

		// if the new status is not null, update statuses received from the group to other groups
		if (saveStatuses) {
			groupsManagerImpl.setIndirectGroupStatus(sess, member, group, newStatus);

			if (newStatus.equals(MemberGroupStatus.EXPIRED)) {
				getPerunBl().getAuditer().log(sess, new MemberExpiredInGroup(member, group));
			} else if (newStatus.equals(MemberGroupStatus.VALID)) {
				getPerunBl().getAuditer().log(sess, new MemberValidatedInGroup(member, group));
			}
		}

		// check recursively all parent groups
		for (Group affectedGroup : affectedGroups) {
			recalculateMemberGroupStatusRecursively(sess, member, affectedGroup);
		}
	}

	@Override
	public boolean canExtendMembershipInGroup(PerunSession sess, Member member, Group group) throws InternalErrorException {
		Attribute membershipExpirationRulesAttribute = getMembershipExpirationRulesAttribute(sess, group);
		if (membershipExpirationRulesAttribute == null) {
			return true;
		}
		try {
			extendMembershipInGroup(sess, member, group, false);
		} catch (ExtendMembershipException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canExtendMembershipInGroupWithReason(PerunSession sess, Member member, Group group) throws InternalErrorException, ExtendMembershipException {
		Attribute membershipExpirationRulesAttribute = getMembershipExpirationRulesAttribute(sess, group);
		if (membershipExpirationRulesAttribute == null) {
			return true;
		}
		extendMembershipInGroup(sess, member, group, false);
		return true;
	}

	private void extendMembershipInGroup(PerunSession sess, Member member, Group group, boolean setValue) throws InternalErrorException, ExtendMembershipException {
		Attribute membershipExpirationRulesAttribute = getMembershipExpirationRulesAttribute(sess, group);
		if (membershipExpirationRulesAttribute == null) {
			return;
		}
		LinkedHashMap<String, String> membershipExpirationRules = (LinkedHashMap<String, String>) membershipExpirationRulesAttribute.getValue();

		Calendar calendar = Calendar.getInstance();

		// Get current membershipExpiration date
		Attribute membershipExpirationAttribute = getMemberExpiration(sess, member, group);

		checkLoaForExpiration(sess, membershipExpirationRules, membershipExpirationAttribute, member);

		String period = null;
		// Default extension
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName) != null) {
			period = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName);
		}

		String loaPeriod = getLoaPeriod(sess, member, membershipExpirationRules, membershipExpirationAttribute);

		if (loaPeriod != null) {
			period = loaPeriod;
		}

		// Do we extend for x months or for static date?
		if (period != null) {
			if (period.startsWith("+")) {
				extendForMonths(calendar, period, membershipExpirationRules, membershipExpirationAttribute, member, group);
			} else {
				// We will extend to particular date

				extendForStaticDate(calendar, period, membershipExpirationRules, membershipExpirationAttribute, member, group);
			}
		}

		// Reset hours, minutes and seconds to 0
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		if (setValue) {
			// Set new value of the membershipExpiration for the member
			membershipExpirationAttribute.setValue(BeansUtils.getDateFormatterWithoutTime().format(calendar.getTime()));
			try {
				getPerunBl().getAttributesManagerBl().setAttribute(sess, member, group, membershipExpirationAttribute);
			} catch (WrongAttributeValueException e) {
				throw new InternalErrorException("Wrong value: " + membershipExpirationAttribute.getValue(),e);
			} catch (WrongReferenceAttributeValueException | WrongAttributeAssignmentException | AttributeNotExistsException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	@Override
	public void extendMembershipInGroup(PerunSession sess, Member member, Group group) throws InternalErrorException, ExtendMembershipException {
		extendMembershipInGroup(sess, member, group, true);
	}

	private void extendForStaticDate(Calendar calendar, String period, LinkedHashMap<String, String> membershipExpirationRules, Attribute membershipExpirationAttribute, Member member, Group group) throws InternalErrorException, ExtendMembershipException {
		// Parse date
		Pattern p = Pattern.compile("([0-9]+).([0-9]+).");
		Matcher m = p.matcher(period);

		if (!m.matches()) {
			throw new InternalErrorException("Wrong format of period in Group membershipExpirationRules attribute. Period: " + period);
		}

		boolean extensionInNextYear = Utils.extendCalendarByStaticDate(calendar, m);

		// ***** GRACE PERIOD *****
		// Is there a grace period?
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName) != null) {
			String gracePeriod = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName);
			// If the extension is requested in period-gracePeriod then extend to next period

			// Get the value of the grace period
			p = Pattern.compile("([0-9]+)([dmy]?)");
			m = p.matcher(gracePeriod);
			if (m.matches()) {
				Calendar gracePeriodCalendar = Calendar.getInstance();
				Pair<Integer, Integer> fieldAmount;
				try {
					fieldAmount = Utils.extendGracePeriodCalendar(gracePeriodCalendar, m, calendar);
				} catch (InternalErrorException e) {
					throw new InternalErrorException("Wrong format of gracePeriod in group membershipExpirationRules attribute. gracePeriod: " + gracePeriod);
				}
				// Check if we are in grace period
				if (gracePeriodCalendar.before(Calendar.getInstance())) {
					// We are in grace period, so extend to the next period
					calendar.add(Calendar.YEAR, 1);
				}

				// If we do not need to set the attribute value, only check if the current member's expiration time is not in grace period
				if (membershipExpirationAttribute.getValue() != null) {
					try {
						Date currentMemberExpiration = BeansUtils.getDateFormatterWithoutTime().parse((String) membershipExpirationAttribute.getValue());
						// subtracts grace period from the currentMemberExpiration
						Calendar currentMemberExpirationCalendar = Calendar.getInstance();
						currentMemberExpirationCalendar.setTime(currentMemberExpiration);

						currentMemberExpirationCalendar.add(fieldAmount.getLeft(), -fieldAmount.getRight());

						// if today is before that time, user can extend his period
						if (currentMemberExpirationCalendar.after(Calendar.getInstance())) {
							throw new ExtendMembershipException(ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD, (String) membershipExpirationAttribute.getValue(),
										"Member " + member + " cannot extend because we are outside grace period for GROUP id " + group.getId() + ".");
						}
					} catch (ParseException e) {
						throw new InternalErrorException("Wrong format of the members group expiration: " + membershipExpirationAttribute.getValue(), e);
					}
				}
			}
		}
	}

	/**
	 * Checks given attributes and extends given calendar by given period
	 *
	 * @param calendar calendar
	 * @param period period
	 * @param membershipExpirationRules rules used in check
	 * @param membershipExpirationAttribute attributes used to check
	 * @param member member that is checked
	 * @param group group that is checked
	 * @throws InternalErrorException internal error
	 * @throws ExtendMembershipException when cannot extend
	 */
	private void extendForMonths(Calendar calendar, String period, Map<String, String> membershipExpirationRules,
								 Attribute membershipExpirationAttribute, Member member, Group group) throws InternalErrorException, ExtendMembershipException {
		if (!isMemberInGracePeriod(membershipExpirationRules, (String) membershipExpirationAttribute.getValue())) {
			throw new ExtendMembershipException(ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD, (String) membershipExpirationAttribute.getValue(),
					"Member " + member + " cannot extend because we are outside grace period for Group id " + group.getId() + ".");
		}

		try {
			Utils.extendCalendarByPeriod(calendar, period);
		} catch (InternalErrorException e) {
			throw new InternalErrorException("Wrong format of period in Group membershipExpirationRules attribute.", e);
		}
	}

	/**
	 * Checks if given member has some loa which is defined in given
	 *
	 * @param sess
	 * @param member
	 * @param membershipExpirationRules
	 * @param membershipExpirationAttribute
	 * @return
	 * @throws InternalErrorException
	 * @throws ExtendMembershipException
	 */
	private String getLoaPeriod(PerunSession sess, Member member, LinkedHashMap<String, String> membershipExpirationRules, Attribute membershipExpirationAttribute) throws InternalErrorException, ExtendMembershipException {
		// Get user LOA
		String memberLoa = getMemberLoa(sess, member);
		// Do we extend particular LoA? Attribute syntax LoA|[period][.]
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName) != null) {
			// Which period
			String[] membershipPeriodLoa = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName).split("\\|");
			String loa = membershipPeriodLoa[0];
			String periodLoa = membershipPeriodLoa[1];
			// Does the user have this LoA?
			if (loa.equals(memberLoa)) {
				if (periodLoa.endsWith(".")) {
					// If period ends with ., then we do not allow extension for users with particular LoA if they are already members
					if (membershipExpirationAttribute.getValue() != null) {
						throw new ExtendMembershipException(ExtendMembershipException.Reason.INSUFFICIENTLOAFOREXTENSION,
								"Member " + member + " doesn't have required LOA for VO id " + member.getVoId() + ".");
					}
					// remove dot from the end of the string
					return periodLoa.substring(0, periodLoa.length() - 1);
				} else {
					return periodLoa;
				}
			}
		}
		return null;
	}

	/**
	 * Checks user's loa for expiration
	 *
	 * @param sess session
	 * @param memberLoa
	 * @param membershipExpirationRules
	 * @param membershipExpirationAttribute
	 * @param member
	 * @throws ExtendMembershipException
	 * @throws InternalErrorException
	 */
	private void checkLoaForExpiration(PerunSession sess, LinkedHashMap<String, String> membershipExpirationRules,
									   Attribute membershipExpirationAttribute, Member member) throws ExtendMembershipException, InternalErrorException {

		boolean isServiceUser = isServiceUser(sess, member);

		// Get user LOA
		String memberLoa = getMemberLoa(sess, member);

		// Which LOA we won't extend?
		// This is applicable only for members who have already set expiration from the previous period
		// and are not service users
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName) != null &&
				membershipExpirationAttribute.getValue() != null &&
				!isServiceUser) {
			if (memberLoa == null) {
				// Member doesn't have LOA defined and LOA is required for extension, so do not extend membership.
				log.warn("Member {} doesn't have LOA defined, but 'doNotExtendLoa' option is set for VO id {}.", member, member.getVoId());
				throw new ExtendMembershipException(ExtendMembershipException.Reason.NOUSERLOA,
							"Member " + member + " doesn't have LOA defined, but 'doNotExtendLoa' option is set for VO id " + member.getVoId() + ".");

			}

			String[] doNotExtendLoas = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName).split(",");

			for (String doNotExtendLoa : doNotExtendLoas) {
				if (doNotExtendLoa.equals(memberLoa)) {
					// Member has LOA which is not allowed for extension
					throw new ExtendMembershipException(ExtendMembershipException.Reason.INSUFFICIENTLOAFOREXTENSION,
							"Member " + member + " doesn't have required LOA for VO id " + member.getVoId() + ".");

				}
			}
		}
	}

	/**
	 * Checks if given user of given member is service user
	 *
	 * @param sess session
	 * @param member member
	 * @return
	 * @throws InternalErrorException internal error
	 */
	private boolean isServiceUser(PerunSession sess, Member member) throws InternalErrorException {
		try {
			User user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
			return user.isServiceUser();
		} catch (UserNotExistsException ex) {
			throw new ConsistencyErrorException("User must exists for "+member+" when checking expiration rules.");
		}
	}

	/**
	 * Returns membershipExpirationAttribute for given member and group
	 * @param sess session
	 * @param member member
	 * @param group group
	 * @return membership expiration attribute
	 */
	private Attribute getMemberExpiration(PerunSession sess, Member member, Group group) throws InternalErrorException {
		try {
			return getPerunBl().getAttributesManagerBl().getAttribute(sess, member, group,	A_MG_D_MEMBERSHIP_EXPIRATION);
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Attribute: " +A_MG_D_MEMBERSHIP_EXPIRATION +
					" must be defined in order to use membershipExpirationRules");
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Returns members loa
	 *
	 * @param sess session
	 * @param member member
	 * @return member's loa
	 * @throws InternalErrorException internal error
	 */
	private String getMemberLoa(PerunSession sess, Member member) throws InternalErrorException {
		try {
			Attribute loa = getPerunBl().getAttributesManagerBl().getAttribute(sess, member, A_M_V_LOA);
			return (String) loa.getValue();
		} catch (AttributeNotExistsException e) {
			// Ignore, will be probably set further
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}
		return null;
	}

	/**
	 * Returns membership expiration rules attribute for given group
	 *
	 * @param sess session
	 * @param group group
	 * @return membership expiration attribute or null if not found
	 * @throws InternalErrorException internal error
	 */
	private Attribute getMembershipExpirationRulesAttribute(PerunSession sess, Group group) throws InternalErrorException {
		Attribute membershipExpirationRulesAttribute;

		try {
			membershipExpirationRulesAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_D_EXPIRATION_RULES);

			// If attribute was not filled, then silently exit
			if (membershipExpirationRulesAttribute.getValue() == null) return null;
		} catch (AttributeNotExistsException e) {
			// There is no attribute definition for membership expiration rules.
			return null;
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Shouldn't happen.");
		}

		return membershipExpirationRulesAttribute;
	}

	/**
	 * Return true if member is in grace period. If grace period is not set return always true.
	 * If member has not expiration date return always true.
	 *
	 * @param membershipExpirationRules
	 * @param membershipExpiration
	 * @return true if member is in grace period. Be carefull about special cases - read method description.
	 * @throws InternalErrorException
	 */
	private boolean isMemberInGracePeriod(Map<String, String> membershipExpirationRules, String membershipExpiration) throws InternalErrorException {
		// Is a grace period set?
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName) == null) {
			// If not grace period is infinite
			return true;
		}
		// does member have expiration date?
		if (membershipExpiration == null) {
			// if not grace period is infinite
			return true;
		}

		String gracePeriod = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName);

		// If the extension is requested in period-gracePeriod then extend to next period
		Pattern p = Pattern.compile("([0-9]+)([dmy]?)");
		Matcher m = p.matcher(gracePeriod);

		if (!m.matches()) {
			throw new InternalErrorException("Wrong format of gracePeriod in VO membershipExpirationRules attribute. gracePeriod: " + gracePeriod);
		}

		int amount = Integer.valueOf(m.group(1));

		int field;
		String dmyString = m.group(2);
		if (dmyString.equals("d")) {
			field = Calendar.DAY_OF_YEAR;
		} else if (dmyString.equals("m")) {
			field = Calendar.MONTH;
		} else if (dmyString.equals("y")) {
			field = Calendar.YEAR;
		} else {
			throw new InternalErrorException("Wrong format of gracePeriod in VO membershipExpirationRules attribute. gracePeriod: " + gracePeriod);
		}

		try {
			Calendar beginOfGracePeriod = Calendar.getInstance();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			beginOfGracePeriod.setTime(format.parse(membershipExpiration));
			beginOfGracePeriod.add(field, -amount);
			if (beginOfGracePeriod.before(Calendar.getInstance())) {
				return true;
			}
		} catch (ParseException e) {
			throw new InternalErrorException("Wrong format of membership expiration attribute: " + membershipExpiration, e);
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
