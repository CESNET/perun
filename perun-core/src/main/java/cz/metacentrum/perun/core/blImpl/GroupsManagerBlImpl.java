package cz.metacentrum.perun.core.blImpl;

import com.google.common.collect.ImmutableSet;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberAddedToGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.DirectMemberRemovedFromGroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupCreatedAsSubgroup;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupCreatedInVo;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupDeleted;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupMoved;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupStructureSyncFailed;
import cz.metacentrum.perun.audit.events.GroupManagerEvents.GroupStructureSyncFinishedWithErrors;
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
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.CandidateGroup;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.EnrichedGroup;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.RichUserExtSource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupMoveNotAllowedException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAllowedToAutoRegistrationException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotDefinedOnResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationAlreadyExists;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationCannotBeRemoved;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationDoesNotExist;
import cz.metacentrum.perun.core.api.exceptions.GroupRelationNotAllowed;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.GroupStructureSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationAlreadyRunningException;
import cz.metacentrum.perun.core.api.exceptions.GroupSynchronizationNotEnabledException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ParserException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDeletionFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.GroupsManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.SynchronizationPool;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.GroupsManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cz.metacentrum.perun.core.impl.PerunLocksUtils.lockGroupMembership;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * GroupsManager business logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class GroupsManagerBlImpl implements GroupsManagerBl {

	private final static Logger log = LoggerFactory.getLogger(GroupsManagerBlImpl.class);
	private static final String MDC_LOG_FILE_NAME = "logFileName";

	private final GroupsManagerImplApi groupsManagerImpl;
	private PerunBl perunBl;
	private final Integer maxConcurentGroupsToSynchronize;
	private final ArrayList<GroupSynchronizerThread> groupSynchronizerThreads;
	private final ArrayList<GroupStructureSynchronizerThread> groupStructureSynchronizerThreads;
	private static final String A_G_D_AUTHORITATIVE_GROUP = AttributesManager.NS_GROUP_ATTR_DEF + ":authoritativeGroup";
	private static final String A_G_D_EXPIRATION_RULES = AttributesManager.NS_GROUP_ATTR_DEF + ":groupMembershipExpirationRules";
	private static final String A_G_D_GROUP_STRUCTURE_RESOURCES = AttributesManager.NS_GROUP_ATTR_DEF + ":groupStructureResources";
	private static final String A_MG_D_MEMBERSHIP_EXPIRATION = AttributesManager.NS_MEMBER_GROUP_ATTR_DEF + ":groupMembershipExpiration";
	private static final String A_U_V_LOA = AttributesManager.NS_USER_ATTR_VIRT + ":loa";
	private static final List<Status> statusesAffectedBySynchronization = Arrays.asList(Status.DISABLED, Status.EXPIRED, Status.INVALID);

	private final Integer maxConcurrentGroupsStructuresToSynchronize;
	private final SynchronizationPool poolOfSynchronizations;

	public static final String GROUP_LOGIN = "login";
	public static final String PARENT_GROUP_LOGIN = "parentGroupLogin";
	public static final String GROUP_NAME = "groupName";
	public static final String GROUP_DESCRIPTION = "description";

	public static final Set<String> GROUP_SYNC_DEFAULT_DATA = ImmutableSet.of(
		GROUP_LOGIN,
		PARENT_GROUP_LOGIN,
		GROUP_NAME,
		GROUP_DESCRIPTION
	);

	/**
	 * Create new instance of this class.
	 *
	 */
	public GroupsManagerBlImpl(GroupsManagerImplApi groupsManagerImpl) {
		this.groupsManagerImpl = groupsManagerImpl;
		this.groupSynchronizerThreads = new ArrayList<>();
		this.groupStructureSynchronizerThreads = new ArrayList<>();
		this.poolOfSynchronizations = new SynchronizationPool();
		//set maximum concurrent groups to synchronize by property
		this.maxConcurentGroupsToSynchronize = BeansUtils.getCoreConfig().getGroupMaxConcurentGroupsToSynchronize();
		this.maxConcurrentGroupsStructuresToSynchronize = BeansUtils.getCoreConfig().getGroupMaxConcurrentGroupsStructuresToSynchronize();
	}

	@Override
	public Group createGroup(PerunSession sess, Vo vo, Group group) throws GroupExistsException {
		if (group.getParentGroupId() != null) throw new InternalErrorException("Top-level groups can't have parentGroupId set!");
		group = getGroupsManagerImpl().createGroup(sess, vo, group);
		getPerunBl().getAuditer().log(sess, new GroupCreatedInVo(group, vo));
		group.setVoId(vo.getId());

		//set creator as group admin unless he already have authz right on the group (he is VO admin or this is "members" group of VO)
		User user = sess.getPerunPrincipal().getUser();
		if(user != null) {   //user can be null in tests
			if(!sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN)
				&& !sess.getPerunPrincipal().getRoles().hasRole(Role.VOADMIN, vo)
				&& !VosManager.MEMBERS_GROUP.equals(group.getName())) {
				try {
					AuthzResolverBlImpl.setRole(sess, user, group, Role.GROUPADMIN);
				} catch (AlreadyAdminException e) {
					throw new ConsistencyErrorException("Newly created group already have an admin.", e);
				} catch (RoleCannotBeManagedException e) {
					throw new InternalErrorException(e);
				}
			}
		}

		return group;
	}

	@Override
	public Group createGroup(PerunSession sess, Group parentGroup, Group group) throws GroupExistsException, GroupRelationNotAllowed, GroupRelationAlreadyExists {
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
	public void deleteGroup(PerunSession sess, Group group, boolean forceDelete) throws RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new java.lang.IllegalArgumentException("Built-in " + group.getName() + " group cannot be deleted separately.");
		}
		this.deleteAnyGroup(sess, group, forceDelete);
	}

	@Override
	public void deleteGroups(PerunSession perunSession, List<Group> groups, boolean forceDelete) throws GroupAlreadyRemovedException, RelationExistsException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		//Use sorting by group names reverse order (first name A:B:c then A:B etc.)
		groups.sort(Collections.reverseOrder(Comparator.comparing(Group::getName)));

		for(Group group: groups) {
			this.deleteGroup(perunSession, group, forceDelete);
		}
	}

	@Override
	public void deleteMembersGroup(PerunSession sess, Vo vo) throws GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
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
	private void deleteAnyGroup(PerunSession sess, Group group, boolean forceDelete) throws RelationExistsException, GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupNotExistsException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		Vo vo = this.getVo(sess, group);

		if (getGroupsManagerImpl().getSubGroupsCount(sess, group) > 0) {
			if (!forceDelete) throw new RelationExistsException("Group group="+group+" contains subgroups");

			//get subgroups of this group
			List<Group> subGroups = getSubGroups(sess, group);

			for (Group subGroup : subGroups) {
				deleteAnyGroup(sess, subGroup, true);
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
				} catch (InvalidLoginException e) {
					throw new InternalErrorException("We are deleting reserved login from group applications, but its syntax is not allowed by namespace configuration.", e);
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
			} catch (AttributeValueException ex) {
				throw new ConsistencyErrorException("All members were removed from this group. So all member-group attribute values can be removed.", ex);
			} catch (MemberGroupMismatchException e) {
				throw new InternalErrorException("Member we tried to remove all member-group attributes doesn't come from the same VO as group", e);
			}
		}

		// remove admin roles of group
		List<Facility> facilitiesWhereGroupIsAdmin = getGroupsManagerImpl().getFacilitiesWhereGroupIsAdmin(sess, group);
		for (Facility facility : facilitiesWhereGroupIsAdmin) {
			try {
				AuthzResolverBlImpl.unsetRole(sess, group, facility, Role.FACILITYADMIN);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of facility {} due to group not admin exception {}.", group, facility, e);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
			}
		}

		List<Group> groupsWhereGroupIsAdmin = getGroupsManagerImpl().getGroupsWhereGroupIsAdmin(sess, group);
		for (Group group1 : groupsWhereGroupIsAdmin) {
			try {
				AuthzResolverBlImpl.unsetRole(sess, group, group1, Role.GROUPADMIN);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of group {} due to group not admin exception {}.", group, group1, e);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
			}
		}

		List<Resource> resourcesWhereGroupIsAdmin = getGroupsManagerImpl().getResourcesWhereGroupIsAdmin(sess, group);
		for (Resource resource : resourcesWhereGroupIsAdmin) {
			try {
				AuthzResolverBlImpl.unsetRole(sess, group, resource, Role.RESOURCEADMIN);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of resource {} due to group not admin exception {}.", group, resource, e);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
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
				AuthzResolverBlImpl.unsetRole(sess, group, securityTeam, Role.SECURITYADMIN);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of security team {} due to group not admin exception {}.", group, securityTeam, e);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
			}
		}

		List<Vo> vosWhereGroupIsAdmin = getGroupsManagerImpl().getVosWhereGroupIsAdmin(sess, group);
		for (Vo vo1 : vosWhereGroupIsAdmin) {
			try {
				AuthzResolverBlImpl.unsetRole(sess, group, vo1, Role.VOADMIN);
			} catch (GroupNotAdminException e) {
				log.warn("Can't unset group {} as admin of facility {} due to group not admin exception {}.", group, vo1, e);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
			}
		}

		//remove admins of this group
		List<Group> adminGroups = getGroupsManagerImpl().getGroupAdmins(sess, group);

		for (Group adminGroup : adminGroups) {
			try {
				AuthzResolverBlImpl.unsetRole(sess, adminGroup, group, Role.GROUPADMIN);
			} catch (GroupNotAdminException e) {
				log.warn("When trying to unsetRole GroupAdmin for group {} in the group {} the exception was thrown {}", adminGroup, group, e);
				//skip and log as warning
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
			}
		}

		List<User> adminUsers = getGroupsManagerImpl().getAdmins(sess, group);

		for (User adminUser : adminUsers) {
			try {
				AuthzResolverBlImpl.unsetRole(sess, adminUser, group, Role.GROUPADMIN);
			} catch (UserNotAdminException e) {
				log.warn("When trying to unsetRole GroupAdmin for user {} in the group {} the exception was thrown {}", adminUser, group, e);
				//skip and log as warning
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
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
	private void logTotallyRemovedMembers(PerunSession sess, Integer parentGroupId, List<Member> membersFromDeletedGroup) {
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
	public void deleteAllGroups(PerunSession sess, Vo vo) throws GroupAlreadyRemovedException, GroupAlreadyRemovedFromResourceException, GroupRelationDoesNotExist, GroupRelationCannotBeRemoved {
		// get parent groups
		List<Group> groups = getGroupsManagerImpl().getGroups(sess, vo).stream()
			.filter(group -> group.getParentGroupId() == null)
			.collect(Collectors.toList());
		for(Group group: groups) {

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
	public Group updateGroup(PerunSession sess, Group group) throws GroupExistsException {

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
	public Group updateParentGroupId(PerunSession sess, Group group) {
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
	private Map<Member, MemberGroupStatus> getMemberGroupStatusesForGroupAndForMembersFromOtherGroup(PerunSession sess, Group group, Group fromGroup) {
		Map<Member, MemberGroupStatus> groupStatuses = new HashMap<>();
		List<Member> groupMembers = getGroupMembers(sess, fromGroup);
		for (Member groupMember : groupMembers) {
			groupStatuses.put(groupMember, getTotalMemberGroupStatus(sess, groupMember, group));
		}
		return groupStatuses;
	}

	@Override
	public void moveGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws GroupMoveNotAllowedException, WrongAttributeValueException, WrongReferenceAttributeValueException {

		// check if moving group is null
		if (movingGroup == null) {
			throw new GroupMoveNotAllowedException("Moving group: cannot be null.", null, destinationGroup);
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

			//prevent existence forbidden unions between groups after moving
			//example: there are groups "A", "A:B" and "C", where "C" is included into "A" (there is a union), if we move "C" under "A:B", existing union will change to forbidden one
			//Get all unions for moving group and all of its subgroups
			Set<Group> movingGroupStructureUnions = new HashSet<>();
			movingGroupStructureUnions.addAll(getGroupUnions(sess, movingGroup, true));
			for(Group subGroup: getAllSubGroups(sess, movingGroup)) {
				movingGroupStructureUnions.addAll(getGroupUnions(sess, subGroup, true));
			}
			//remove direct relationship from moving group to it's parent group (if exists)
			//prevent wrong exception in situation like "A", "A:B", "A:C" and moving "A:C" under "A:B", which is correct
			if(movingGroup.getParentGroupId() != null) {
				try {
					Group parentGroupOfMovingGroup = getParentGroup(sess, movingGroup);
					movingGroupStructureUnions.remove(parentGroupOfMovingGroup);
				} catch (ParentGroupNotExistsException ex) {
					throw new InternalErrorException("Can't find parentGroup for " + movingGroup);
				}
			}

			//Get all group stucture of destination group (destination group and all its parent groups)
			Set<Group> destinationGroupStructure = new HashSet<>();
			destinationGroupStructure.add(destinationGroup);
			destinationGroupStructure.addAll(getParentGroups(sess, destinationGroup));

			movingGroupStructureUnions.retainAll(destinationGroupStructure);
			if(!movingGroupStructureUnions.isEmpty()) {
				throw new GroupMoveNotAllowedException("After moving of moving group: " + movingGroup + " under destination group: " + destinationGroup + " there will exist forbidden indirect relationship.", movingGroup, destinationGroup);
			}

			processRelationsWhileMovingGroup(sess, destinationGroup, movingGroup);

			// We have to set group attributes so we can update it in database
			movingGroup.setParentGroupId(destinationGroup.getId());
			String finalGroupName = destinationGroup.getName() + ":" + movingGroup.getShortName();
			movingGroup.setName(finalGroupName);

		} else {

			// check if moving group is already top level group
			if (movingGroup.getParentGroupId() == null) {
				throw new GroupMoveNotAllowedException("Moving group: " + movingGroup + " is already top level group.", movingGroup, null);
			}

			List<Group> destinationGroupSubGroups = getGroups(sess, getVo(sess, movingGroup));

			// check if there is top level group with same short name as Moving group short name
			for (Group group: destinationGroupSubGroups) {
				if(movingGroup.getShortName().equals(group.getName())){
					throw new GroupMoveNotAllowedException("There is already top level group with the same name as moving group: " + movingGroup + ".", movingGroup, null);
				}
			}

			processRelationsWhileMovingGroup(sess, null, movingGroup);

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
	public Group getGroupById(PerunSession sess, int id) throws GroupNotExistsException {
		return getGroupsManagerImpl().getGroupById(sess, id);
	}

	@Override
	public List<Group> getGroupsToSynchronize(PerunSession sess) {
		List<Group> groups = getGroupsManagerImpl().getGroupsToSynchronize(sess);
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public Group getGroupByName(PerunSession sess, Vo vo, String name) throws GroupNotExistsException {
		return getGroupsManagerImpl().getGroupByName(sess, vo, name);
	}

	@Override
	public List<Group> getGroupsByIds(PerunSession sess, List<Integer> ids) {
		return getGroupsManagerImpl().getGroupsByIds(sess, ids);
	}

	@Override
	public void addMemberToMembersGroup(PerunSession sess, Group group, Member member) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.addDirectMember(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.addAdmin and MembersManager.createMember.");
		}
	}

	@Override
	public void addMember(PerunSession sess, Group group, Member member) throws WrongReferenceAttributeValueException, AlreadyMemberException, WrongAttributeValueException, GroupNotExistsException {
		// Check if the group is NOT members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Cannot add member directly to the members group.");
		} else {
			this.addDirectMember(sess, group, member);
		}
	}

	@Override
	public boolean isDirectGroupMember(PerunSession sess, Group group, Member member) {
		return getGroupsManagerImpl().isDirectGroupMember(sess, group, member);
	}

	@Override
	public void addMember(PerunSession sess, List<Group> groups, Member member) throws WrongReferenceAttributeValueException, AlreadyMemberException, WrongAttributeValueException, GroupNotExistsException {
		groups = new ArrayList<>(groups);
		groups.sort(Collections.reverseOrder());
		for (Group group : groups) {
			// Check if the group is NOT members or administrators group
			if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
				throw new InternalErrorException("Cannot add member directly to the members group.");
			} else {
				this.addDirectMember(sess, group, member);
			}
		}
	}

	@Override
	public void addMembers(PerunSession sess, Group group,  List<Member> members) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException {
		Collections.sort(members);
		for (Member member : members) {
			// Check if the group is NOT members or administrators group
			if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
				throw new InternalErrorException("Cannot add member directly to the members group.");
			} else {
				this.addDirectMember(sess, group, member);
			}
		}
	}


	private List<Group> getParentGroups(PerunSession sess, Group group) {
		if(group == null) return new ArrayList<>();
		try {
			if (group.getParentGroupId() == null) return new ArrayList<>();
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
	protected void addDirectMember(PerunSession sess, Group group, Member member) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException {

		lockGroupMembership(group, Collections.singletonList(member));

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
	protected List<Member> addIndirectMembers(PerunSession sess, Group group, List<Member> members, int sourceGroupId) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		lockGroupMembership(group, members);

		List<Member> newMembers = new ArrayList<>();
		for (Member member : members) {
			//we want to process only newly added members
			if(!isGroupMember(sess, group, member)) newMembers.add(member);
			groupsManagerImpl.addMember(sess, group, member, MembershipType.INDIRECT, sourceGroupId);
		}

		for (Member member : newMembers) {
			setRequiredAttributes(sess, member, group);
			getPerunBl().getAuditer().log(sess, new IndirectMemberAddedToGroup(member, group));
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
	private void setRequiredAttributes(PerunSession sess, Member member, Group group) throws WrongAttributeValueException, WrongReferenceAttributeValueException {
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
	private List<Member> removeIndirectMembers(PerunSession sess, Group group, List<Member> members, int sourceGroupId) throws WrongAttributeValueException, WrongReferenceAttributeValueException, NotGroupMemberException {
		List<Member> membersToRemove = new ArrayList<>(members);

		lockGroupMembership(group, membersToRemove);

		for (Member member: membersToRemove) {
			member.setSourceGroupId(sourceGroupId);
			groupsManagerImpl.removeMember(sess, group, member);
		}

		// get list of new members
		List<Member> newMembers = this.getGroupMembers(sess, group);
		// get only removed members
		membersToRemove.removeAll(newMembers);

		for(Member removedIndirectMember: membersToRemove) {
			addMemberToGroupsFromTriggerAttribute(sess, group, removedIndirectMember);
			notifyMemberRemovalFromGroup(sess, group, removedIndirectMember);
			//remove all member-group attributes because member is not part of group any more
			try {
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, removedIndirectMember, group);
			} catch (MemberGroupMismatchException e) {
				throw new InternalErrorException("Member we tried to remove all member-group attributes is not from the same VO as Group.", e);
			}
			getPerunBl().getAuditer().log(sess, new IndirectMemberRemovedFromGroup(removedIndirectMember, group));
		}

		return membersToRemove;
	}

	@Override
	public void removeMember(PerunSession sess, Group group, Member member) throws NotGroupMemberException, GroupNotExistsException {
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
	public void removeMembers(PerunSession sess, Group group, List<Member> members) throws NotGroupMemberException, GroupNotExistsException {
		// Check if the group is NOT members or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			throw new InternalErrorException("Cannot remove member directly from the members group.");
		} else {
			Collections.sort(members);
			for (Member member : members) {
				try {
					this.removeDirectMember(sess, group, member);
				} catch (WrongAttributeValueException | WrongReferenceAttributeValueException ex){
					throw new InternalErrorException(ex);
				}
			}
		}
	}


	@Override
	public void removeMember(PerunSession sess, List<Group> groups, Member member) throws NotGroupMemberException, GroupNotExistsException {
		Collections.sort(groups, Collections.reverseOrder());
		for (Group group : groups) {
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
	}

	@Override
	public void removeMemberFromMembersOrAdministratorsGroup(PerunSession sess, Group group, Member member) throws NotGroupMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		// Check if the group IS memebers or administrators group
		if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
			this.removeDirectMember(sess, group, member);
		} else {
			throw new InternalErrorException("This method must be called only from methods VosManager.removeAdmin and MembersManager.deleteMember.");
		}
	}

	private void removeDirectMember(PerunSession sess, Group group, Member member) throws NotGroupMemberException, GroupNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {

		lockGroupMembership(group, Collections.singletonList(member));

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
			} catch (MemberGroupMismatchException e) {
				throw new InternalErrorException(e);
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

		if (!getGroupsManagerImpl().isGroupMember(sess, group, member)) {
			addMemberToGroupsFromTriggerAttribute(sess, group, member);
		}
	}

	/**
	 * Adds the member to the groups in 'groupTrigger' attribute of the 'group' argument
	 * If any error occurs, the group will be skipped and the error will be logged.
	 * If there is an error in getting the attribute, the method will log the error and return
	 *
	 * IMPORTANT: groupTrigger attribute shouldn't exist with write rights for any other role than VoAdmin, unless
	 * the right checks will be added here! Otherwise it could allow a way how to add someone to group he doesn't have
	 * any rights to.
	 *
	 * @param sess PerunSession
	 * @param group Group from which the member has been removed
	 * @param member Member to be added to groups in groupTrigger
	 */
	private void addMemberToGroupsFromTriggerAttribute(PerunSession sess, Group group, Member member) {
		ArrayList<String> groupsForAddition;
		try {
			groupsForAddition = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, AttributesManager.NS_GROUP_ATTR_DEF+":groupTrigger").valueAsList();
		} catch (InternalErrorException | WrongAttributeAssignmentException | AttributeNotExistsException | NumberFormatException e) {
			log.error("Error while getting groupTrigger attribute, Exception message: " + e.toString());
			return;
		}
		if (groupsForAddition == null) return;

		for (String groupId : groupsForAddition) {
			try {
				Group groupForAddition = getGroupById(sess, Integer.parseInt(groupId));
				if (isDirectGroupMember(sess, groupForAddition, member)) {
					reactivateMember(sess, member, groupForAddition);
				} else {
					addDirectMember(sess, groupForAddition, member);
				}
			} catch (InternalErrorException | GroupNotExistsException | MemberNotExistsException | AlreadyMemberException | WrongAttributeValueException | WrongReferenceAttributeValueException e) {
				log.error("Member could not be added to or reactivated in the group, exception message: " + e.toString());
			}
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
	private void notifyMemberRemovalFromGroup(PerunSession sess, Group group, Member member) {
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
	public List<Member> getGroupMembers(PerunSession sess, Group group) {
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupMembers(sess, group));
	}

	@Override
	public Member getGroupMemberById(PerunSession sess, Group group, int memberId) throws NotGroupMemberException {
		List<Member> members = getGroupsManagerImpl().getGroupMembersById(sess, group, memberId);
		if (members.isEmpty()) throw new NotGroupMemberException("Member with ID="+memberId+" is not member of "+group+" or doesn't exists at all.");
		List<Member> filteredMembers = this.filterMembersByMembershipTypeInGroup(members);
		if (filteredMembers.size() == 0) throw new InternalErrorException("Filtering DIRECT/INDIRECT members resulted in empty members list.");
		if (filteredMembers.size() > 1) throw new ConsistencyErrorException("Filtering DIRECT/INDIRECT members resulted with >1 members with same ID.");
		return filteredMembers.get(0);
	}

	@Override
	public List<Member> getGroupDirectMembers(PerunSession sess, Group group) {
		return groupsManagerImpl.getGroupMembersByMembership(sess, group, MembershipType.DIRECT);
	}

	@Override
	public List<Member> getGroupMembers(PerunSession sess, Group group, MemberGroupStatus statusInGroup, Status status) {
		return filterMembersByStatusInGroup(getGroupMembers(sess, group, status), statusInGroup);
	}

	@Override
	public List<Member> getActiveGroupMembers(PerunSession sess, Group group, Status status) {
		return filterMembersByStatusInGroup(getGroupMembers(sess, group, status), MemberGroupStatus.VALID);
	}

	@Override
	public List<Member> getActiveGroupMembers(PerunSession sess, Group group) {
		return filterMembersByStatusInGroup(getGroupMembers(sess, group), MemberGroupStatus.VALID);
	}

	@Override
	public List<Member> getInactiveGroupMembers(PerunSession sess, Group group, Status status) {
		return filterMembersByStatusInGroup(getGroupMembers(sess, group, status), MemberGroupStatus.EXPIRED);
	}

	@Override
	public List<Member> getInactiveGroupMembers(PerunSession sess, Group group) {
		return filterMembersByStatusInGroup(getGroupMembers(sess, group), MemberGroupStatus.EXPIRED);
	}

	@Override
	public List<Member> getGroupMembers(PerunSession sess, Group group, Status status) {
		if (status == null) {
			return this.getGroupMembers(sess, group);
		}
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupMembers(sess, group, Collections.singletonList(status), false));
	}

	@Override
	public List<User> getGroupUsers(PerunSession perunSession, Group group) {
		return new ArrayList<>(new HashSet<>(getGroupsManagerImpl().getGroupUsers(perunSession, group)));
	}

	@Override
	public List<Member> getGroupMembersExceptInvalid(PerunSession sess, Group group) {
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupMembers(sess, group, Collections.singletonList(Status.INVALID), true));
	}

	@Override
	public List<Member> getGroupMembersExceptInvalidAndDisabled(PerunSession sess, Group group) {
		return this.filterMembersByMembershipTypeInGroup(getGroupsManagerImpl().getGroupMembers(sess, group, Arrays.asList(Status.INVALID, Status.DISABLED), true));
	}

	@Override
	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group) {
		return this.getGroupRichMembers(sess, group, null);
	}

	@Override
	public List<RichMember> getGroupDirectRichMembers(PerunSession sess, Group group) {
		List<Member> directMembers = getGroupDirectMembers(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, directMembers);
	}

	@Override
	public List<RichMember> getGroupRichMembersExceptInvalid(PerunSession sess, Group group) {
		List<Member> members = this.getGroupMembersExceptInvalid(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getGroupRichMembers(PerunSession sess, Group group, Status status) {
		List<Member> members = this.getGroupMembers(sess, group, status);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group) {
		return this.getGroupRichMembersWithAttributes(sess, group, null);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributesExceptInvalid(PerunSession sess, Group group) {
		List<RichMember> richMembers = this.getGroupRichMembersExceptInvalid(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	@Override
	public List<RichMember> getGroupRichMembersWithAttributes(PerunSession sess, Group group, Status status) {
		List<RichMember> richMembers = this.getGroupRichMembers(sess, group, status);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	@Override
	public int getGroupMembersCount(PerunSession sess, Group group) {
		List<Member> members = this.getGroupMembers(sess, group);
		return members.size();
	}

	@Override
	public Map<Status, Integer> getGroupMembersCountsByVoStatus(PerunSession sess, Group group) {
		List<Member> members = this.getGroupMembers(sess, group);

		Map<Status, Integer> counts = new HashMap<>();
		for (Status status : Status.values()) {
			counts.put(status, 0);
		}
		members.forEach(member -> counts.computeIfPresent(member.getStatus(), (key, value) -> value + 1));

		return counts;
	}

	@Override
	public Map<MemberGroupStatus, Integer> getGroupMembersCountsByGroupStatus(PerunSession sess, Group group) {
		List<Member> members = this.getGroupMembers(sess, group);

		Map<MemberGroupStatus, Integer> counts = new HashMap<>();
		for (MemberGroupStatus status : MemberGroupStatus.values()) {
			counts.put(status, 0);
		}
		members.forEach(member -> counts.computeIfPresent(member.getGroupStatus(), (key, value) -> value + 1));

		return counts;
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Group group, boolean onlyDirectAdmins) {
		if(onlyDirectAdmins) {
			return getGroupsManagerImpl().getDirectAdmins(perunSession, group);
		} else {
			return getGroupsManagerImpl().getAdmins(perunSession, group);
		}
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws UserNotExistsException {
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
	public List<User> getAdmins(PerunSession sess, Group group) {
		return getGroupsManagerImpl().getAdmins(sess, group);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Group group) {
		return getGroupsManagerImpl().getDirectAdmins(sess, group);
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Group group) {
		List<Group> groups = getGroupsManagerImpl().getGroupAdmins(sess, group);
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession perunSession, Group group) {
		List<User> users = this.getAdmins(perunSession, group);
		return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdmins(PerunSession perunSession, Group group) {
		List<User> users = this.getDirectAdmins(perunSession, group);
		return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Group group) throws UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, group);
		return perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getRichAdmins(perunSession, group), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Group group, List<String> specificAttributes) {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getDirectRichAdmins(perunSession, group), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	public List<Group> getAssignedGroupsToResource(PerunSession sess, Resource resource) {
		return getAssignedGroupsToResource(sess, resource, false);
	}

	@Override
	public List<Group> getAssignedGroupsToResource(PerunSession sess, Resource resource, Member member) {
		List<Group> groups = getGroupsManagerImpl().getAssignedGroupsToResource(sess, resource, member);
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Group> getAssignedGroupsToResource(PerunSession sess, Resource resource, boolean withSubGroups) {
		List<Group> assignedGroups = getGroupsManagerImpl().getAssignedGroupsToResource(sess, resource);
		if(!withSubGroups) return assignedGroups;

		boolean done = assignedGroups.isEmpty();
		List<Group> groupsToProcess = new ArrayList<>(assignedGroups);
		while(!done) {
			List<Group> groupsToAdd = new ArrayList<>();
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

		// Sort
		Collections.sort(assignedGroups);
		return assignedGroups;
	}

	@Override
	public List<Group> getAssignedGroupsToFacility(PerunSession sess, Facility facility) {
		List<Group> assignedGroups = getGroupsManagerImpl().getAssignedGroupsToFacility(sess, facility);
		// Sort
		Collections.sort(assignedGroups);
		return assignedGroups;
	}

	@Override
	public List<Group> getAllGroups(PerunSession sess, Vo vo) {
		List<Group> groups = getGroupsManagerImpl().getAllGroups(sess, vo);

		// Sort
		Collections.sort(groups);

		return groups;
	}

	@Override
	public Map<Group, Object> getAllGroupsWithHierarchy(PerunSession sess, Vo vo) {
		Map<Group,Object> groupHierarchy = new TreeMap<>();

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
	private Map<Group, Object> getGroupsForHierarchy(PerunSession sess, Map<Group, Object> groups) {
		for (Group group: groups.keySet()) {
			List<Group> subGroups = this.getSubGroups(sess, group);

			Map<Group,Object> subGroupHierarchy = new TreeMap<>();
			for (Group subGroup: subGroups) {
				subGroupHierarchy.put(subGroup, null);
			}

			groups.put(group, this.getGroupsForHierarchy(sess, subGroupHierarchy));
		}

		return groups;
	}

	@Override
	public List<Group> getSubGroups(PerunSession sess, Group parentGroup) {
		List<Group> subGroups = getGroupsManagerImpl().getSubGroups(sess, parentGroup);

		// Sort
		Collections.sort(subGroups);

		return subGroups;
	}

	@Override
	public List<Group> getAllSubGroups(PerunSession sess, Group parentGroup) {
		Queue<Group> groupsInQueue = new ConcurrentLinkedQueue<>(getGroupsManagerImpl().getSubGroups(sess, parentGroup));
		List<Group> allSubGroups = new ArrayList<>();
		while(groupsInQueue.peek() != null) {
			groupsInQueue.addAll(getGroupsManagerImpl().getSubGroups(sess, groupsInQueue.peek()));
			allSubGroups.add(groupsInQueue.poll());
		}

		// Sort
		Collections.sort(allSubGroups);
		return allSubGroups;
	}

	@Override
	public Group getParentGroup(PerunSession sess, Group group) throws ParentGroupNotExistsException {
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
	public List<Group> getGroups(PerunSession sess, Vo vo) {
		List<Group> groups = getGroupsManagerImpl().getGroups(sess, vo);

		Collections.sort(groups);

		return groups;
	}

	@Override
	public int getGroupsCount(PerunSession sess, Vo vo) {
		return getGroupsManagerImpl().getGroupsCount(sess, vo);
	}

	@Override
	public int getGroupsCount(PerunSession sess) {
		return getGroupsManagerImpl().getGroupsCount(sess);
	}

	@Override
	public int getSubGroupsCount(PerunSession sess, Group parentGroup) {
		return getGroupsManagerImpl().getSubGroupsCount(sess, parentGroup);
	}

	@Override
	public Vo getVo(PerunSession sess, Group group) {
		int voId = getGroupsManagerImpl().getVoId(sess, group);
		try {
			return getPerunBl().getVosManagerBl().getVoById(sess, voId);
		} catch (VoNotExistsException e) {
			throw new ConsistencyErrorException("Group belongs to the non-existent VO", e);
		}
	}

	@Override
	public List<Group> getMemberGroups(PerunSession sess, Member member) {
		List<Group> groups = this.getAllMemberGroups(sess, member);
		//Remove members group
		if(!groups.isEmpty()) {
			groups.removeIf(g -> g.getName().equals(VosManager.MEMBERS_GROUP));
		}
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Group> getMemberDirectGroups(PerunSession sess, Member member) {
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
	public List<Group> getMemberGroupsByAttribute(PerunSession sess, Member member, Attribute attribute) throws WrongAttributeAssignmentException {
		List<Group> memberGroups = this.getAllMemberGroups(sess, member);
		memberGroups.retainAll(this.getGroupsByAttribute(sess, attribute));
		return memberGroups;
	}

	@Override
	public List<Group> getAllMemberGroups(PerunSession sess, Member member) {
		List<Group> groups = getGroupsManagerImpl().getAllMemberGroups(sess, member);
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Group> getGroupsWhereMemberIsActive(PerunSession sess, Member member) {
		List<Group> groupsWhereMemberIsActive = this.getAllGroupsWhereMemberIsActive(sess, member);
		groupsWhereMemberIsActive.removeIf(g -> VosManager.MEMBERS_GROUP.equals(g.getName()));
		return groupsWhereMemberIsActive;
	}

	@Override
	public List<Group> getGroupsWhereMemberIsInactive(PerunSession sess, Member member) {
		//IMPORTANT: this is the easiest way but also more time consuming - this code can be optimize if needed
		List<Group> membersGroups = this.getMemberGroups(sess, member);
		List<Group> activeMembersGroup = this.getGroupsWhereMemberIsActive(sess, member);
		//Inactive are groups where member has no active state (all-active=inactive)
		membersGroups.removeAll(activeMembersGroup);

		return membersGroups;
	}

	@Override
	public List<Group> getAllGroupsWhereMemberIsActive(PerunSession sess, Member member) {
		List<Group> groups = getGroupsManagerImpl().getAllGroupsWhereMemberIsActive(sess, member);
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Member> getParentGroupMembers(PerunSession sess, Group group) {
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
	public List<RichMember> getParentGroupRichMembers(PerunSession sess, Group group) {
		List<Member> members = this.getParentGroupMembers(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getParentGroupRichMembersWithAttributes(PerunSession sess, Group group) {
		List<RichMember> richMembers = this.getParentGroupRichMembers(sess, group);

		return getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	@Override
	public boolean isUserMemberOfGroup(PerunSession sess, User user, Group group) {
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

	@Override
	public List<String> synchronizeGroup(PerunSession sess, Group group) throws AttributeNotExistsException, WrongAttributeAssignmentException, ExtSourceNotExistsException, GroupNotExistsException {
		//needed variables for whole method
		List<String> skippedMembers = new ArrayList<>();
		ExtSource source = null;
		ExtSource membersSource = null;

		try {
			//set Logback's Mapped Diagnostic Context key for the current thread
			MDC.put(MDC_LOG_FILE_NAME, "groupsync/group_" + group.getId());

			long startTime = System.nanoTime();
			getPerunBl().getAuditer().log(sess,new GroupSyncStarted(group));
			log.debug("Group synchronization for {} has been started.", group);

			//Initialization of group extSource
			source = getGroupExtSourceForSynchronization(sess, group);

			//Initialization of groupMembers extSource (if it is set), in other case set membersSource = source
			membersSource = getGroupMembersExtSourceForSynchronization(sess, group, source);

			//Prepare info about userAttributes which need to be overwritten (not just updated) and memberAttributes which need to be merged not overwritten
			List<String> overwriteUserAttributesList = getOverwriteUserAttributesListFromExtSource(membersSource);
			List<String> mergeMemberAttributesList = getMemberAttributesListToBeMergedFromExtSource(membersSource);

			//Get info about type of synchronization (with or without update)
			boolean lightweightSynchronization = isThisLightweightSynchronization(sess, group);

			log.debug("Group synchronization {}: using configuration extSource for membership {}, extSource for members {}", group, membersSource, membersSource.getName());

			//Prepare containers for work with group members
			List<Candidate> candidatesToAdd = new ArrayList<>();
			Map<Candidate, RichMember> membersToUpdate = new HashMap<>();
			List<RichMember> membersToRemove = new ArrayList<>();

			//get all direct members of synchronized group (only direct, because we want to set direct membership with this group by synchronization)
			List<RichMember> actualGroupMembers = getPerunBl().getGroupsManagerBl().getGroupDirectRichMembers(sess, group);

			if(lightweightSynchronization) {
				categorizeMembersForLightweightSynchronization(sess, group, source, membersSource, actualGroupMembers, candidatesToAdd, membersToRemove, skippedMembers);
			} else {
				//Get subjects from extSource
				List<Map<String, String>> subjects = getSubjectsFromExtSource(sess, source, group);
				//Convert subjects to candidates
				List<Candidate> candidates = convertSubjectsToCandidates(sess, subjects, membersSource, source, actualGroupMembers, skippedMembers);

				categorizeMembersForSynchronization(sess, actualGroupMembers, candidates, candidatesToAdd, membersToUpdate, membersToRemove);
			}

			// Remove members from group who are not present in synchronized ExtSource
			boolean isAuthoritative = isAuthoritative(sess, group);
			Collections.sort(membersToRemove);
			for (RichMember memberToRemove : membersToRemove) {
				removeFormerMemberWhileSynchronization(sess, group, memberToRemove, isAuthoritative);
			}

			List<AttributeDefinition> attrDefs = new ArrayList<>();
			//Update members already presented in group
			for (Candidate candidate : membersToUpdate.keySet()) {
				RichMember memberToUpdate = membersToUpdate.get(candidate);
				//Load attrDefinitions just once for first candidate
				if (!candidate.getAttributes().isEmpty() && attrDefs.isEmpty()) {
					attrDefs = getAttributesToSynchronizeFromCandidates(sess, group, candidate);
				}
				updateExistingMemberWhileSynchronization(sess, group, candidate, memberToUpdate, overwriteUserAttributesList, mergeMemberAttributesList, attrDefs);
			}

			//Add not presented candidates to group
			Collections.sort(candidatesToAdd);
			for (Candidate candidateToAdd : candidatesToAdd) {
				addMissingMemberWhileSynchronization(sess, group, candidateToAdd, overwriteUserAttributesList, mergeMemberAttributesList, skippedMembers);
			}

			long endTime = System.nanoTime();
			getPerunBl().getAuditer().log(sess,new GroupSyncFinished(group, startTime, endTime));
			log.info("Group synchronization for {} has been finished.", group);
		} finally {
			closeExtSourcesAfterSynchronization(membersSource, source);
			MDC.remove(MDC_LOG_FILE_NAME);
		}

		return skippedMembers;
	}

	@Override
	public List<String> synchronizeGroupStructure(PerunSession sess, Group baseGroup) throws AttributeNotExistsException, WrongAttributeAssignmentException, ExtSourceNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		List<String> skippedGroups = new ArrayList<>();

		log.info("Group structure synchronization {}: started.", baseGroup);

		//get extSource for group structure
		ExtSource source = getGroupExtSourceForSynchronization(sess, baseGroup);

		//get login attribute for structure
		AttributeDefinition loginAttributeDefinition = getLoginAttributeForGroupStructure(sess, baseGroup);
		//get login prefix if exists
		String loginPrefix = getLoginPrefixForGroupStructure(sess, baseGroup);

		List<CandidateGroup> candidateGroupsToAdd = new ArrayList<>();
		Map<CandidateGroup, Group> groupsToUpdate = new HashMap<>();
		List<Group> groupsToRemove = new ArrayList<>();

		Map<String, Group> actualGroups = getAllSubGroupsWithLogins(sess, baseGroup, loginAttributeDefinition);
		List<Map<String, String>> subjectGroups = getSubjectGroupsFromExtSource(sess, source, baseGroup);

		if (isThisFlatSynchronization(sess, baseGroup)) {
			for(Map<String, String> subjectGroup : subjectGroups) {
				subjectGroup.put(PARENT_GROUP_LOGIN, null);
			}
		}

		List<CandidateGroup> candidateGroups = getPerunBl().getExtSourcesManagerBl().generateCandidateGroups(sess, subjectGroups, source, loginPrefix);

		categorizeGroupsForSynchronization(actualGroups, candidateGroups, candidateGroupsToAdd, groupsToUpdate, groupsToRemove);

		//order of operations is important here
		//removing need to go first to be able to replace groups with same name but different login
		//updating need to be last to set right order of groups again
		List<Integer> removedGroupsIds = removeFormerGroupsWhileSynchronization(sess, baseGroup, groupsToRemove, skippedGroups);
		addMissingGroupsWhileSynchronization(sess, baseGroup, candidateGroupsToAdd, loginAttributeDefinition, skippedGroups);
		updateExistingGroupsWhileSynchronization(sess, baseGroup, groupsToUpdate, removedGroupsIds, loginAttributeDefinition, skippedGroups);

		setUpSynchronizationAttributesForAllSubGroups(sess, baseGroup, source, loginAttributeDefinition, loginPrefix);

		syncResourcesForSynchronization(sess, baseGroup, loginAttributeDefinition, skippedGroups);

		log.info("Group structure synchronization {}: ended.", baseGroup);

		return skippedGroups;
	}

	/**
	 * Sync resources from groupStructureResources attribute to the group
	 * tree with the root in the given base group. If some resources are not found
	 * or the assignment failed, there is a new message added to the skippedMessages list.
	 *
	 * @param sess perun session
	 * @param baseGroup group structure sync base group
	 * @param skippedMessages list where are added messages about skipped operations
	 */
	private void syncResourcesForSynchronization(PerunSession sess, Group baseGroup, AttributeDefinition loginAttr,
	                                             List<String> skippedMessages) {
		Attribute syncedResourcesAttr;

		try {
			syncedResourcesAttr = perunBl.getAttributesManagerBl()
					.getAttribute(sess, baseGroup, A_G_D_GROUP_STRUCTURE_RESOURCES);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException("Failed to obtain the groupStructureResources attribute for structure synchronization.", e);
		}

		if (syncedResourcesAttr.getValue() == null) {
			// no resources should be synced
			return;
		}

		// load all subgroups with logins once, so it can be reused in other methods
		Map<String, Group> groupsByLogins = getAllSubGroupsWithLogins(sess, baseGroup, loginAttr);

		syncedResourcesAttr.valueAsMap().forEach((resourceId, groupLogins) ->
			syncResourceInStructure(sess, resourceId, groupLogins, baseGroup, groupsByLogins, skippedMessages));
	}

	/**
	 * Assign resource with given id to groups with logins defined in parameter 'rawGroupLogins'.
	 *
	 * If the rawGroupLogins value is empty, then the resource is assigned to the whole structure.
	 * The rawGroupLogins are in format: 'login1,login2,login3,'. If the login contains a '\' or ','
	 * characters, they must be escaped by the '\' character.
	 *
	 * @param sess perun session
	 * @param rawResourceId id of a resource which should be set
	 * @param rawGroupLogins group login separated with a comma ','
	 * @param baseGroup base group which is used if the rawGroupLogins is empty
	 * @param groupsByLogins map containing groups with by their logins
	 * @param skippedMessages a list with skipped messages, other messages are added to this list
	 */
	private void syncResourceInStructure(PerunSession sess, String rawResourceId, String rawGroupLogins,
	                                     Group baseGroup, Map<String, Group> groupsByLogins,
	                                     List<String> skippedMessages) {
		int resourceId = Integer.parseInt(rawResourceId);
		try {
			Resource resource = perunBl.getResourcesManagerBl().getResourceById(sess, resourceId);
			List<String> groupLogins;
			if (rawGroupLogins == null || rawGroupLogins.isEmpty()) {
				// if no group logins are specified, assign the resource to the whole tree
				groupLogins = Collections.singletonList(rawGroupLogins);
			} else {
				groupLogins = BeansUtils.parseEscapedListValue(rawGroupLogins);
			}
			groupLogins.forEach(login ->
					syncResourceInStructure(sess, resource, baseGroup, login, groupsByLogins, skippedMessages));
		} catch (ResourceNotExistsException e) {
			log.error("Assigning groups to a resource was skipped during group structure synchronization, because the resource wasn't found.", e);
			skippedMessages.add("Assigning groups to resource with id'" + resourceId + "' skipped because it was not found.");
		}
	}

	/**
	 * Assign given resource to a group with given login, and to all of
	 * its subgroups.
	 *
	 * If the login value is empty, then the resource is assigned to
	 * the whole structure, except for the base group.
	 *
	 * @param sess perun session
	 * @param resource a resource which should be set
	 * @param login group login
	 * @param baseGroup base group which is used if the rawGroupLogins is empty
	 * @param groupsByLogins map containing groups with by their logins
	 * @param skippedMessages a list with skipped messages, other messages are added to this list
	 */
	private void syncResourceInStructure(PerunSession sess, Resource resource, Group baseGroup, String login,
	                                Map<String, Group> groupsByLogins, List<String> skippedMessages) {
		Group rootGroup;
		boolean assigningToTheBaseGroup = login == null || login.isEmpty();

		if (assigningToTheBaseGroup) {
			rootGroup = baseGroup;
		} else {
			rootGroup = groupsByLogins.get(login);
			if (rootGroup == null) {
				skippedMessages.add("Resource with id '" + resource.getId() + "' was skipped for group with login '" +
					login + "' no group with this login was found.");
				return;
			}
		}

		List<Group> groupsToAssign = perunBl.getGroupsManagerBl().getAllSubGroups(sess, rootGroup);

		if (!assigningToTheBaseGroup) {
			groupsToAssign.add(rootGroup);
		}

		assignGroupsToResource(sess, groupsToAssign, resource);
	}

	/**
	 * Assign resource to the given groups. If some of the groups
	 * is already assigned, the group is skipped silently.
	 *
	 * @param sess perun session
	 * @param groups groups which should be assigned to the given resource
	 * @param resource resource
	 */
	private void assignGroupsToResource(PerunSession sess, Collection<Group> groups, Resource resource) {
		Set<Group> groupsToAssign = new HashSet<>(groups);
		groupsToAssign.removeAll(perunBl.getResourcesManagerBl().getAssignedGroups(sess, resource));

		try {
			perunBl.getResourcesManagerBl().assignGroupsToResource(sess, groupsToAssign, resource);
		} catch (WrongAttributeValueException | WrongReferenceAttributeValueException | GroupResourceMismatchException e) {
			log.error("Failed to assign groups during group structure synchronization. Groups {}, resource {}," +
					" exception: {}", groups, resource, e);
		}
	}

	@Override
	public void forceGroupSynchronization(PerunSession sess, Group group) throws GroupSynchronizationAlreadyRunningException, GroupSynchronizationNotEnabledException {
		//Check if the group should be synchronized (attribute synchronizationEnabled is set to 'true')
		Boolean syncEnabled = false;
		try {
			Attribute syncEnabledAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME);
			if(syncEnabledAttr.getValue() != null) syncEnabled = "true".equals(syncEnabledAttr.valueAsString());
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException ex) {
			//attribute is wrongly set in database
			throw new InternalErrorException("Can't force " + group + " because we can't find ", ex);
		}

		if(syncEnabled) {
			//Check if the group is not currently in synchronization process
			if (poolOfSynchronizations.putGroupToPoolOfWaitingGroups(group, true)) {
				log.debug("Scheduling synchronization for the group {} by force!", group);
			} else {
				throw new GroupSynchronizationAlreadyRunningException(group);
			}
		} else {
			throw new GroupSynchronizationNotEnabledException(group);
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
	public synchronized void synchronizeGroups(PerunSession sess) {
		// Get the default synchronization interval and synchronization timeout from the configuration file
		int timeout = BeansUtils.getCoreConfig().getGroupSynchronizationTimeout();
		// Get the number of miliseconds from the epoch, so we can divide it by the synchronization interval value
		long millisecondsFromEpoch = System.currentTimeMillis();
		long minutesFromEpoch = millisecondsFromEpoch/1000/60;

		LocalDateTime localDateTime = new Timestamp(millisecondsFromEpoch).toLocalDateTime();

		int numberOfNewlyRemovedThreads = 0;
		// Firstly interrupt threads after timeout, then remove all interrupted threads
		Iterator<GroupSynchronizerThread> threadIterator = groupSynchronizerThreads.iterator();
		while(threadIterator.hasNext()) {
			GroupSynchronizerThread thread = threadIterator.next();

			long threadStart = thread.getStartTime();
			long timeDiff = System.currentTimeMillis() - threadStart;

			//If thread was interrupted by anything, remove it from the pool of active threads
			if (thread.isInterrupted() || !thread.isAlive()) {
				numberOfNewlyRemovedThreads++;
				threadIterator.remove();
			} else if (threadStart != 0 && timeDiff/1000/60 > timeout) {
				//If thread start time is 0, this thread is waiting for another job, skip it
				// If the time is greater than timeout set in the configuration file (in minutes), interrupt and remove this thread from pool
				log.error("Thread was interrupted while synchronizing the group {} because of timeout!", thread.getGroup());
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
		List<Group> timeCompliantGroups = new ArrayList<>();

		int numberOfNewlyAddedGroups;
		for (Group group: groups) {

			try {
				Attribute synchronizationTimesAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess,group,GroupsManager.GROUP_SYNCHRO_TIMES_ATTRNAME);
				if (synchronizationTimesAttr.getValue() != null) {
					if (isTimeCompliantWithExactTimes(localDateTime, synchronizationTimesAttr.valueAsList())) {
						timeCompliantGroups.add(group);
					}
				} else if (isTimeCompliantWithGroupInterval(sess, group, minutesFromEpoch, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME,"Group")) {
					timeCompliantGroups.add(group);
				}
			} catch (AttributeNotExistsException e) {
				log.error("Required attribute {} isn't defined in Perun!", GroupsManager.GROUP_SYNCHRO_TIMES_ATTRNAME);
			} catch (WrongAttributeAssignmentException e) {
				log.error("Cannot get attribute " + GroupsManager.GROUP_SYNCHRO_TIMES_ATTRNAME + " for group " + group + " due to exception.", e);
			}

		}

		numberOfNewlyAddedGroups = poolOfSynchronizations.putGroupsToPoolOfWaitingGroups(timeCompliantGroups);

		// Save state of synchronization to the info log
		log.info("SynchronizeGroups method ends with these states: " +
				"'number of newly removed threads'='" + numberOfNewlyRemovedThreads + "', " +
				"'number of newly created threads'='" + numberOfNewlyCreatedThreads + "', " +
				"'number of newly added groups to the pool'='" + numberOfNewlyAddedGroups + "', " +
				"'right now synchronized groups'='" + poolOfSynchronizations.asPoolOfGroupsToBeSynchronized().getRunningJobs() + "', " +
				"'right now waiting groups'='" + poolOfSynchronizations.asPoolOfGroupsToBeSynchronized().getWaitingJobs() + "'.");
	}

	private class GroupSynchronizerThread extends Thread {

		// all synchronization runs under synchronizer identity.
		private final PerunPrincipal pp = new PerunPrincipal("perunSynchronizer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		private final PerunBl perunBl;
		private final PerunSession sess;
		private volatile long startTime;
		private Group group;

		public GroupSynchronizerThread(PerunSession sess) {
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
				try {
					group = poolOfSynchronizations.takeGroup((PerunSessionImpl)sess);
				} catch (InterruptedException ex) {
					log.error("Thread was interrupted when trying to take another group to synchronize from pool", ex);
					//Interrupt this thread
					this.interrupt();
					return;
				} catch (InternalErrorException ex) {
					log.error("Internal error exception was thrown when the thread was trying to take another group to synchronize from pool", ex);
					//Interrupt this thread
					this.interrupt();
					return;
				}

				try {
					// Set the start time, so we can check the timeout of the thread
					startTime = System.currentTimeMillis();

					try {
						// Create attribute with start of last synchronization timestamp
						Attribute startOfSynchronization = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, GroupsManager.GROUP_START_OF_LAST_SYNC_ATTRNAME));
						startOfSynchronization.setValue(BeansUtils.getDateFormatter().format(new Date(startTime)));
						((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttribute(sess, group, startOfSynchronization);
					} catch (AttributeNotExistsException ex) {
						log.error("Can't save startOfLastSynchronization, because there is missing attribute with name {}", GroupsManager.GROUP_START_OF_LAST_SYNC_ATTRNAME);
					}

					log.debug("Synchronization thread started synchronization for group {}.", group);

					//synchronize Group and get information about skipped Members
					List<String> skippedMembers = perunBl.getGroupsManagerBl().synchronizeGroup(sess, group);

					skippedMembersMessage = prepareSkippedObjectsMessage(skippedMembers, "members");
					exceptionMessage = skippedMembersMessage;

					log.debug("Synchronization thread for group {} has finished in {} ms.", group, System.currentTimeMillis() - startTime);
				} catch (InternalErrorException |
						WrongAttributeAssignmentException  | GroupNotExistsException |
						AttributeNotExistsException  | ExtSourceNotExistsException e) {
					failedDueToException = true;
					exceptionMessage = "Cannot synchronize group ";
					log.error(exceptionMessage + group, e);
					exceptionMessage += "due to exception: " + e.getClass().getSimpleName() + " => " + e.getMessage();
				} catch (Exception e) {
					failedDueToException = true;
					exceptionMessage = "Cannot synchronize group ";
					log.error(exceptionMessage + group, e);
					exceptionMessage += "due to unexpected exception: " + e.getClass().getName() + " => " + e.getMessage();
				} finally {
					//Save information about group synchronization, this method run in new transaction
					try {
						perunBl.getGroupsManagerBl().saveInformationAboutGroupSynchronizationInNewTransaction(sess, group, startTime, failedDueToException, exceptionMessage);
					} catch (Exception ex) {
						log.error("When synchronization group " + group + ", exception was thrown.", ex);
						log.error("Info about exception from synchronization: {}", skippedMembersMessage);
					}
					//Remove job from running jobs
					if(!poolOfSynchronizations.removeGroup(group)) {
						log.error("Can't remove running job for object " + group + " from pool of running jobs because it is not containing it.");
					}

					log.debug("GroupSynchronizerThread finished for group: {}", group);
				}
			}
		}

		public Group getGroup() {
			return group;
		}

		public long getStartTime() {
			return startTime;
		}

		private void setThreadToDefaultState() {
			this.startTime = 0;
		}
	}

	@Override
	public void forceGroupStructureSynchronization(PerunSession sess, Group group) throws GroupStructureSynchronizationAlreadyRunningException {
		//Adds the group on the first place to the queue of groups waiting for group structure synchronization.
		if (poolOfSynchronizations.putGroupStructureToPoolOfWaitingGroupsStructures(group, true)) {
			log.info("Scheduling synchronization for the group structure {} by force!", group);
		} else {
			throw new GroupStructureSynchronizationAlreadyRunningException(group);
		}
	}

	@Override
	public void forceAllSubGroupsSynchronization(PerunSession sess, Group group) {
		List<Group> subGroups = perunBl.getGroupsManagerBl().getAllSubGroups(sess, group);
		for(Group subGroup: subGroups) {
			try {
				forceGroupSynchronization(sess, subGroup);
			} catch (GroupSynchronizationAlreadyRunningException | GroupSynchronizationNotEnabledException ex) {
				//in bulk force this is not important, just skip it
			}
		}
	}

	@Override
	public synchronized void synchronizeGroupsStructures(PerunSession sess) {
		int numberOfNewlyRemovedThreads = processCurrentGroupStructureSynchronizationThreads();
		int numberOfNewlyCreatedThreads = createNewGroupStructureSynchronizationThreads(sess);
		int numberOfNewlyAddedGroups = addGroupsToGroupStructureSynchronizationPool(sess);

		log.info("SynchronizeGroupsStructures method ends with these states: " +
				"'number of newly removed threads'='" + numberOfNewlyRemovedThreads + "', " +
				"'number of newly created threads'='" + numberOfNewlyCreatedThreads + "', " +
				"'number of newly added groups structures to the pool'='" + numberOfNewlyAddedGroups + "', " +
				"'right now synchronized groups structures'='" + poolOfSynchronizations.asPoolOfGroupsStructuresToBeSynchronized().getRunningJobs() + "', " +
				"'right now waiting groups structures'='" + poolOfSynchronizations.asPoolOfGroupsStructuresToBeSynchronized().getWaitingJobs() + "'.");
	}

	/**
	 * Private thread class for groups structure synchronizations
	 *
	 * All group structure synchronizations runs under synchronizer identity.
	 */
	private class GroupStructureSynchronizerThread extends Thread {

		private final PerunPrincipal pp = new PerunPrincipal("perunSynchronizer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		private final PerunBl perunBl;
		private final PerunSession sess;
		private volatile long startTime;
		private Group group;

		/**
		 * Take only reference to perun
		 * Default settings of not running thread (waiting for another group)
		 *
		 * @param sess
		 * @throws InternalErrorException
		 */
		public GroupStructureSynchronizerThread(PerunSession sess) {
			this.perunBl = (PerunBl) sess.getPerun();
			this.sess = perunBl.getPerunSession(pp, new PerunClient());
			this.startTime = 0;
		}

		/**
		 * Run group structure synchronization thread
		 *
		 */
		@Override
		public void run() {
			while (!this.isInterrupted()) {
				this.setThreadToDefaultState();

				String exceptionMessage = null;
				String skippedGroupsMessage = null;
				boolean failedDueToException = false;

				try {
					group = poolOfSynchronizations.takeGroupStructure((PerunSessionImpl)sess);
				} catch (InterruptedException ex) {
					log.error("Thread was interrupted when trying to take another group structure to synchronize from pool", ex);
					this.interrupt();
					continue;
				}  catch (InternalErrorException ex) {
					log.error("Internal error exception was thrown when the thread was trying to take another group structure to synchronize from pool", ex);
					//Interrupt this thread
					this.interrupt();
					continue;
				}

				try {
					startTime = System.currentTimeMillis();

					log.debug("Synchronization thread started synchronization for group structure {}.", group);

					List<String> skippedGroups = perunBl.getGroupsManagerBl().synchronizeGroupStructure(sess, group);

					skippedGroupsMessage = prepareSkippedObjectsMessage(skippedGroups, "groups");
					exceptionMessage = skippedGroupsMessage;

					log.debug("Synchronization thread for group structure {} has finished in {} ms.", group, System.currentTimeMillis() - startTime);
				} catch (Exception e) {
					failedDueToException = true;
					exceptionMessage = "Cannot synchronize group structure ";
					log.error(exceptionMessage + group, e);
					exceptionMessage += "due to exception: " + e.getClass().getName() + " => " + e.getMessage();
				} finally {
					try {
						perunBl.getGroupsManagerBl().saveInformationAboutGroupStructureSynchronizationInNewTransaction(sess, group, failedDueToException, exceptionMessage);
					} catch (Exception ex) {
						log.error("When synchronization group structure " + group + ", exception was thrown.", ex);
						log.error("Info about exception from group structure synchronization: " + skippedGroupsMessage);
					}
					if (!poolOfSynchronizations.removeGroupStructure(group)) {
						log.error("Can't remove running job for object " + group + " from pool of running jobs because it is not containing it.");
					}

					log.debug("GroupStructureSynchronizerThread finished for group: {}", group);
				}
			}
		}

		public Group getGroup() {
			return group;
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
	List<Group> getAllAuthoritativeGroupsOfMember(PerunSession sess, Member member) throws AttributeNotExistsException {
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
	public List<Group> getGroupsByAttribute(PerunSession sess, Attribute attribute) throws WrongAttributeAssignmentException {
		getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_ATTR);
		if(!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
		List<Group> groups = getGroupsManagerImpl().getGroupsByAttribute(sess, attribute);
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Pair<Group, Resource>> getGroupResourcePairsByAttribute(PerunSession sess, Attribute attribute) throws WrongAttributeAssignmentException {
		getPerunBl().getAttributesManagerBl().checkNamespace(sess, attribute, AttributesManager.NS_GROUP_RESOURCE_ATTR);
		if(!(getPerunBl().getAttributesManagerBl().isDefAttribute(sess, attribute) || getPerunBl().getAttributesManagerBl().isOptAttribute(sess, attribute))) throw new WrongAttributeAssignmentException("This method can process only def and opt attributes");
		return getGroupsManagerImpl().getGroupResourcePairsByAttribute(sess, attribute);
	}

	@Override
	public boolean isGroupMember(PerunSession sess, Group group, Member member) {
		return getGroupsManagerImpl().isGroupMember(sess, group, member);
	}

	@Override
	public void checkGroupExists(PerunSession sess, Group group) throws GroupNotExistsException {
		getGroupsManagerImpl().checkGroupExists(sess, group);
	}

	@Override
	public List<Group> getGroupsByPerunBean(PerunSession sess, Member member) {
		List<Group> groups = new ArrayList<>(new HashSet<>(getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, member)));
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Group> getGroupsByPerunBean(PerunSession sess, Resource resource) {
		List<Group> groups = new ArrayList<>(new HashSet<>(getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource)));
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Group> getGroupsByPerunBean(PerunSession sess, User user) {
		List<Group> groups = new ArrayList<>();
		List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		for(Member memberElement: members) {
			groups.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(sess, memberElement));
		}
		groups = new ArrayList<>(new HashSet<>(groups));
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Group> getGroupsByPerunBean(PerunSession sess, Host host) {
		List<Group> groups = new ArrayList<>();
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		for(Resource resourceElement: resourcesFromFacility) {
			groups.addAll(getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resourceElement));
		}
		groups = new ArrayList<>(new HashSet<>(groups));
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Group> getGroupsByPerunBean(PerunSession sess, Facility facility) {
		List<Group> groups = new ArrayList<>();
		List<Resource> resourcesFromFacility = getPerunBl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
		for(Resource resourceElement: resourcesFromFacility) {
			groups.addAll(getPerunBl().getGroupsManagerBl().getAssignedGroupsToResource(sess, resourceElement));
		}
		groups = new ArrayList<>(new HashSet<>(groups));
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Group> getGroupsByPerunBean(PerunSession sess, Vo vo) {
		List<Group> groups = new ArrayList<>(new HashSet<>(getPerunBl().getGroupsManagerBl().getAllGroups(sess, vo)));
		// Sort
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<Member> filterMembersByMembershipTypeInGroup(List<Member> members) {
		Map<Integer, List<Member>> indirectMembersById = members.stream()
				.filter(m -> m.getMembershipType().equals(MembershipType.INDIRECT))
				.collect(groupingBy(Member::getId, toList()));

		List<Member> directMembers = members.stream()
				.filter(m -> m.getMembershipType().equals(MembershipType.DIRECT))
				.distinct()
				.map(m -> addGroupStatuses(m, indirectMembersById.get(m.getId())))
				.collect(toList());

		directMembers.forEach(directMember -> indirectMembersById.remove(directMember.getId()));

		indirectMembersById.values().stream()
				.map(this::mergeMembers)
				.forEach(directMembers::add);

		return directMembers;
	}

	/**
	 * To the given member, put all group statuses from the other given members.
	 *
	 * @param member where the group statuses are put
	 * @param members from whom are the group statuses taken
	 * @return given member with added group statuses
	 */
	private Member addGroupStatuses(Member member, Collection<Member> members) {
		if (members != null) {
			members.forEach(indirectMember -> member.putGroupStatuses(indirectMember.getGroupStatuses()));
		}
		return member;
	}

	/**
	 * Merge given members into one and their group statuses.
	 *
	 * @param members to merge
	 * @return member with merged group statuses
	 */
	private Member mergeMembers(List<Member> members) {
		return members.stream()
				.reduce(this::mergeMembers)
				.orElseThrow(() -> new InternalErrorException("Tried to merge member from empty list"));
	}

	/**
	 * Add group statuses from m2 into m1 and return it.
	 *
	 * @param m1 first member to merge
	 * @param m2 second member to merge
	 * @return member with merged group statuses
	 */
	private Member mergeMembers(Member m1, Member m2) {
		m1.putGroupStatuses(m2.getGroupStatuses());
		return m1;
	}

	@Override
	public RichGroup filterOnlyAllowedAttributes(PerunSession sess, RichGroup richGroup) {
		if(richGroup == null) throw new InternalErrorException("RichGroup can't be null.");

		//Filtering richGroup attributes
		if(richGroup.getAttributes() != null) {
			List<Attribute> groupAttributes = richGroup.getAttributes();
			List<Attribute> allowedGroupAttributes = new ArrayList<>();
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
	public List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups) {
		List<RichGroup> filteredRichGroups = new ArrayList<>();
		if(richGroups == null || richGroups.isEmpty()) return filteredRichGroups;

		for(RichGroup rg : richGroups) {
			filteredRichGroups.add(this.filterOnlyAllowedAttributes(sess, rg));
		}

		return filteredRichGroups;
	}

	@Override
	public List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups, Resource resource, boolean useContext) {
		return this.filterOnlyAllowedAttributes(sess, richGroups, null, resource, useContext);
	}

	@Override
	public List<RichGroup> filterOnlyAllowedAttributes(PerunSession sess, List<RichGroup> richGroups, Member member, Resource resource, boolean useContext) {
		//If empty, return empty list (no filtering is needed)
		List<RichGroup> filteredRichGroups = new ArrayList<>();
		if(richGroups == null || richGroups.isEmpty()) return filteredRichGroups;

		//If no context should be used - every attribute is unique in context of group (for every group test access rights for all attributes again)
		if(!useContext) return filterOnlyAllowedAttributes(sess, richGroups);

		//If context should be used - every attribute is unique in a context of users authz_roles for a group + attribute URN
		// (every attribute test only once per authz+friendlyName)
		// context+attr_name to boolean where null means - no rights at all, false means no write rights, true means read and write rights
		Map<String, Boolean> contextMap = new HashMap<>();

		for(RichGroup richGroup : richGroups) {
			String key = "";
			if (sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN)) {
				key = "VOADMINVOOBSERVERGROUPADMINFACILITYADMIN";
			} else {
				String voadmin = ((sess.getPerunPrincipal().getRoles().hasRole(Role.VOADMIN, richGroup) ? "VOADMIN" : ""));
				String voobserver = ((sess.getPerunPrincipal().getRoles().hasRole(Role.VOOBSERVER, richGroup) ? "VOOBSERVER" : ""));
				String groupadmin = ((sess.getPerunPrincipal().getRoles().hasRole(Role.GROUPADMIN, richGroup) ? "GROUPADMIN" : ""));
				String facilityadmin = ((sess.getPerunPrincipal().getRoles().hasRole(Role.FACILITYADMIN) ? "FACILITYADMIN" : ""));
				key = voadmin + voobserver + groupadmin + facilityadmin;
			}

			//Filtering group attributes
			if(richGroup.getAttributes() != null) {
				List<Attribute> groupAttributes = richGroup.getAttributes();
				List<Attribute> allowedGroupAttributes = new ArrayList<>();
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
							canRead = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, groupAttr, richGroup, resource);
						} else if (groupAttr.getNamespace().startsWith(AttributesManager.NS_GROUP_ATTR)) {
							canRead = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, groupAttr, richGroup, null);
						} else if (groupAttr.getNamespace().startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) {
							canRead = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, groupAttr, member, richGroup);
						}
						if(canRead) {
							boolean isWritable = false;
							if (groupAttr.getNamespace().startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR)) {
								isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttr, richGroup, resource);
							} else if (groupAttr.getNamespace().startsWith(AttributesManager.NS_GROUP_ATTR)) {
								isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttr, richGroup, null);
							} else if (groupAttr.getNamespace().startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) {
								isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, groupAttr, member, richGroup);
							}
							groupAttr.setWritable(isWritable);
							allowedGroupAttributes.add(groupAttr);
							contextMap.put(key + groupAttr.getName(), isWritable);
						} else {
							contextMap.put(key + groupAttr.getName(), null);
						}
					}
				}
				richGroup.setAttributes(allowedGroupAttributes);
			}
			filteredRichGroups.add(richGroup);
		}
		return filteredRichGroups;

	}

	@Override
	public EnrichedGroup filterOnlyAllowedAttributes(PerunSession sess, EnrichedGroup enrichedGroup) {
		enrichedGroup.setAttributes(AuthzResolverBlImpl
			.filterNotAllowedAttributes(sess, enrichedGroup.getGroup(), enrichedGroup.getAttributes()));
		return enrichedGroup;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public RichGroup convertGroupToRichGroupWithAttributes(PerunSession sess, Group group) {
		return new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group));
	}

	@Override
	public RichGroup convertGroupToRichGroupWithAttributesByName(PerunSession sess, Group group, List<String> attrNames) {
		if (attrNames == null) return convertGroupToRichGroupWithAttributes(sess, group);
		return new RichGroup(group,this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group, attrNames));
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, List<Group> groups) {
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group)));
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, Resource resource, List<Group> groups) throws GroupResourceMismatchException {
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group, true)));
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, List<Group> groups, List<String> attrNames) {
		if (attrNames == null) return convertGroupsToRichGroupsWithAttributes(sess, groups);
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group, attrNames)));
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, Resource resource, List<Group> groups, List<String> attrNames) throws GroupResourceMismatchException {
		if (attrNames == null) return convertGroupsToRichGroupsWithAttributes(sess, resource, groups);
		List<RichGroup> richGroups = new ArrayList<>();
		for(Group group: groups) {
			richGroups.add(new RichGroup(group, getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group, attrNames, true)));
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> convertGroupsToRichGroupsWithAttributes(PerunSession sess, Member member, Resource resource, List<Group> groups, List<String> attrNames) throws GroupResourceMismatchException, MemberGroupMismatchException {
		List<RichGroup> richGroups = new ArrayList<>();

		//filter attr names for different namespaces (we need to process them separately)
		List<String> groupAndGroupResourceAttrNames = new ArrayList<>();
		List<String> memberGroupAttrNames = new ArrayList<>();
		if(attrNames != null && !attrNames.isEmpty()) {
			groupAndGroupResourceAttrNames = attrNames.stream().filter(attrName ->
				attrName.startsWith(AttributesManager.NS_GROUP_RESOURCE_ATTR) || attrName.startsWith(AttributesManager.NS_GROUP_ATTR)).collect(Collectors.toList());
			memberGroupAttrNames = attrNames.stream().filter(attrName -> attrName.startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)).collect(Collectors.toList());
		}

		for(Group group: groups) {
			if(attrNames == null) {
				//null means - we want all possible attributes
				List<Attribute> attributes = new ArrayList<>();
				attributes.addAll(getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group, true));
				attributes.addAll(getPerunBl().getAttributesManagerBl().getAttributes(sess, member, group));
				richGroups.add(new RichGroup(group, attributes));
			} else if (attrNames.isEmpty()) {
				//empty means we don't need any attributes
				richGroups.add(new RichGroup(group, new ArrayList<>()));
			} else {
				//non-empty means - filter only these attributes if possible
				List<Attribute> attributes = new ArrayList<>();
				//if there is any group or group-resource attribute, add it
				if (!groupAndGroupResourceAttrNames.isEmpty()) attributes.addAll(getPerunBl().getAttributesManagerBl().getAttributes(sess, resource, group, groupAndGroupResourceAttrNames, true));
				//if there is any member-group attribute, add it
				if (!memberGroupAttrNames.isEmpty()) attributes.addAll(getPerunBl().getAttributesManagerBl().getAttributes(sess, member, group, memberGroupAttrNames));

				richGroups.add(new RichGroup(group, attributes));
			}
		}
		return richGroups;
	}

	@Override
	public List<RichGroup> getRichGroupsWithAttributesAssignedToResource(PerunSession sess, Resource resource, List<String> attrNames) {
		List<Group> assignedGroups = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		try {
			return this.convertGroupsToRichGroupsWithAttributes(sess, resource, assignedGroups, attrNames);
		} catch (GroupResourceMismatchException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public List<RichGroup> getRichGroupsWithAttributesAssignedToResource(PerunSession sess, Member member, Resource resource, List<String> attrNames) {
		List<Group> assignedGroups = getPerunBl().getResourcesManagerBl().getAssignedGroups(sess, resource);
		assignedGroups.retainAll(perunBl.getGroupsManagerBl().getAllMemberGroups(sess, member));
		try {
			return this.convertGroupsToRichGroupsWithAttributes(sess, member, resource, assignedGroups, attrNames);
		} catch (GroupResourceMismatchException | MemberGroupMismatchException ex) {
			throw new ConsistencyErrorException(ex);
		}
	}

	@Override
	public List<RichGroup> getAllRichGroupsWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrNames) {
		return convertGroupsToRichGroupsWithAttributes(sess, this.getAllGroups(sess, vo), attrNames);
	}

	@Override
	public List<RichGroup> getMemberRichGroupsWithAttributesByNames(PerunSession sess, Member member, List<String> attrNames) {
		List<Group> memberGroups = this.getMemberGroups(sess, member);
		List<RichGroup> richGroups = new ArrayList<>();

		if(attrNames == null) {
			//if attrNames is null, it means all possible group and member-group attributes
			for(Group group: memberGroups) {
				List<Attribute> allGroupAndMemberGroupAttributes = new ArrayList<>();
				allGroupAndMemberGroupAttributes.addAll(this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group));
				try {
					allGroupAndMemberGroupAttributes.addAll(this.getPerunBl().getAttributesManagerBl().getAttributes(sess, member, group));
				} catch (MemberGroupMismatchException e) {
					throw new InternalErrorException(e);
				}
				richGroups.add(new RichGroup(group, allGroupAndMemberGroupAttributes));
			}
		} else {
			//if attrNames is not null, it means only selected group and member-group attributes
			for (Group group : memberGroups) {
				List<Attribute> selectedGroupAndMemberGroupAttributes = new ArrayList<>();
				selectedGroupAndMemberGroupAttributes.addAll(this.getPerunBl().getAttributesManagerBl().getAttributes(sess, group, attrNames));
				try {
					selectedGroupAndMemberGroupAttributes.addAll(this.getPerunBl().getAttributesManagerBl().getAttributes(sess, member, group, attrNames));
				} catch (MemberGroupMismatchException e) {
					throw new InternalErrorException(e);
				}
				richGroups.add(new RichGroup(group, selectedGroupAndMemberGroupAttributes));
			}
		}

		return richGroups;
	}

	@Override
	public List<RichGroup> getRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) {
		return convertGroupsToRichGroupsWithAttributes(sess, this.getSubGroups(sess, parentGroup), attrNames);
	}

	@Override
	public List<RichGroup> getAllRichSubGroupsWithAttributesByNames(PerunSession sess, Group parentGroup, List<String> attrNames) {
		return convertGroupsToRichGroupsWithAttributes(sess, this.getAllSubGroups(sess, parentGroup), attrNames);
	}

	@Override
	public RichGroup getRichGroupByIdWithAttributesByNames(PerunSession sess, int groupId, List<String> attrNames)throws GroupNotExistsException{
		return convertGroupToRichGroupWithAttributesByName(sess, this.getGroupById(sess, groupId), attrNames);
	}

	@Override
	public void saveInformationAboutGroupSynchronizationInNewTransaction(PerunSession sess, Group group, long startTime, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		saveInformationAboutGroupSynchronization(sess, group, startTime, failedDueToException, exceptionMessage);
	}

	@Override
	public void saveInformationAboutGroupSynchronizationInNestedTransaction(PerunSession sess, Group group, long startTime, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		saveInformationAboutGroupSynchronization(sess, group, startTime, failedDueToException, exceptionMessage);
	}

	@Override
	public void saveInformationAboutGroupStructureSynchronizationInNewTransaction(PerunSession sess, Group group, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		saveInformationAboutGroupStructureSynchronization(sess, group, failedDueToException, exceptionMessage);
	}

	@Override
	public void saveInformationAboutGroupStructureSynchronizationInNestedTransaction(PerunSession sess, Group group, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		saveInformationAboutGroupStructureSynchronization(sess, group, failedDueToException, exceptionMessage);
	}

	@Override
	public List<Group> getGroupsWithAssignedExtSourceInVo(PerunSession sess, ExtSource source, Vo vo) {
		List<Group> groups = getGroupsManagerImpl().getGroupsWithAssignedExtSourceInVo(sess, source, vo);
		// Sort
		Collections.sort(groups);
		return groups;
	}

	//----------- PRIVATE METHODS FOR  GROUP SYNCHRONIZATION -----------

	/**
	 * Check if local time matches some of the times in synchronizationTimes list.
	 *
	 * @param localDateTime is current time
	 * @param synchronizationTimes is list of times against which will be local time compared
	 *
	 * @return True if local time matches some of the times in synchronizationTimes list. False otherwise.
	 */
	private boolean isTimeCompliantWithExactTimes(LocalDateTime localDateTime, ArrayList<String> synchronizationTimes) {

		String actualTime = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
		for (String synchronizationTime : synchronizationTimes) {
			if (actualTime.equals(synchronizationTime)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if current time matches with group time interval
	 *
	 * @param sess
	 * @param group which has set the interval attribute
	 * @param minutesFromEpoch is current time which will be compared against group interval
	 * @param attributeName of attribute which contains a interval for the group
	 * @param objectName which will be used in log messages
	 *
	 * @return True if current time matches with group time interval. False otherwise.
	 *
	 * @throws InternalErrorException
	 */
	private boolean isTimeCompliantWithGroupInterval(PerunSession sess, Group group, long minutesFromEpoch, String attributeName, String objectName) {
		int defaultIntervalMultiplier = BeansUtils.getCoreConfig().getGroupSynchronizationInterval();
		// Get the synchronization interval for the group
		int intervalMultiplier;
		try {
			Attribute intervalAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, attributeName);
			if (intervalAttribute.getValue() != null) {
				intervalMultiplier = Integer.parseInt((String) intervalAttribute.getValue());
			} else {
				intervalMultiplier = defaultIntervalMultiplier;
				log.debug(objectName + " {} hasn't set synchronization interval, using default value: {}", group, intervalMultiplier);
			}
		} catch (AttributeNotExistsException e) {
			log.error("Required attribute {} isn't defined in Perun! Using default value from properties instead!", attributeName);
			intervalMultiplier = defaultIntervalMultiplier;
		} catch (WrongAttributeAssignmentException e) {
			log.error("Cannot get attribute " + attributeName + " for " + objectName.toLowerCase() + " " + group + " due to exception. Using default value from properties instead!",e);
			intervalMultiplier = defaultIntervalMultiplier;
		}

		// Multiply with 5 to get real minutes
		intervalMultiplier = intervalMultiplier*5;

		// If the minutesFromEpoch can be divided by the intervalMultiplier, then synchronize
		return (minutesFromEpoch % intervalMultiplier) == 0;
	}

    private void saveInformationAboutGroupSynchronization(PerunSession sess, Group group, long startTime, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
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
			String attrName = AttributesManager.NS_GROUP_ATTR_DEF + ":lastSuccessSynchronizationTimestamp";
			try {
				Attribute lastSuccessSynchronizationTimestamp = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, attrName));
				lastSuccessSynchronizationTimestamp.setValue(correctTimestampString);
				attrsToSet.add(lastSuccessSynchronizationTimestamp);
			} catch (AttributeNotExistsException ex) {
				log.error("Can't save lastSuccessSynchronizationTimestamp, because there is missing attribute with name {}",attrName);
			}
			// Make string from start of synchronization in correct format
			String startOfSyncString = BeansUtils.getDateFormatter().format(new Date(startTime));
			try {
				// Create attribute with start of last success synchronization timestamp
				Attribute startOfLastSuccessSynchronization = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, GroupsManager.GROUP_START_OF_LAST_SUCCESSFUL_SYNC_ATTRNAME));
				startOfLastSuccessSynchronization.setValue(startOfSyncString);
				attrsToSet.add(startOfLastSuccessSynchronization);
			} catch (AttributeNotExistsException ex) {
				log.error("Can't save startOfLastSuccessfulSynchronization, because there is missing attribute with name {}", GroupsManager.GROUP_START_OF_LAST_SUCCESSFUL_SYNC_ATTRNAME);
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

	/**
	 * For lightweight synchronization prepare candidate to add and members to remove.
	 *
	 * Get all subjects from loginSource and try to find users in Perun by their login and this ExtSource.
	 * If found, look if this user is already in synchronized Group. If yes skip him, if not add him to candidateToAdd
	 * If not found in vo of the group, skip him.
	 *
	 * Rest of former members need to be add to membersToRemove to remove them from group.
	 *
	 * This method fill 2 member structures which get as parameters:
	 * 1. candidateToAdd - New members of the group
	 * 2. membersToRemove - Former members who are not in synchronized ExtSource now
	 *
	 * @param sess
	 * @param group
	 * @param loginSource
	 * @param memberSource
	 * @param groupMembers
	 * @param candidatesToAdd
	 * @param membersToRemove
	 * @param skippedMembers
	 */
	private void categorizeMembersForLightweightSynchronization(PerunSession sess, Group group, ExtSource loginSource, ExtSource memberSource, List<RichMember> groupMembers, List<Candidate> candidatesToAdd, List<RichMember> membersToRemove, List<String> skippedMembers) {
		//Get subjects from loginSource
		List<Map<String, String>> subjects = getSubjectsFromExtSource(sess, loginSource, group);

		//Prepare structure of userIds with richMembers to better work with actual members
		Map<Integer, RichMember> idsOfUsersInGroup = new HashMap<>();
		for(RichMember richMember: groupMembers) {
			idsOfUsersInGroup.put(richMember.getUserId(), richMember);
		}
		//try to find users by login and loginSource
		for(Map<String, String> subjectFromLoginSource : subjects) {
			if (subjectFromLoginSource == null) {
				log.error("Null value in the subjects list. Skipping.");
				continue;
			}
			String login = subjectFromLoginSource.get("login");
			// Skip subjects, which doesn't have login
			if (login == null || login.isEmpty()) {
				log.debug("Subject {} doesn't contain attribute login, skipping.", subjectFromLoginSource);
				skippedMembers.add("MemberEntry:[" + subjectFromLoginSource + "] was skipped because login is missing");
				continue;
			}

			//try to find user from perun by login and member extSource (need to use memberSource because loginSource is not saved by synchronization)
			User user = null;

			List<UserExtSource> userExtSources = new ArrayList<>();
			try {
				UserExtSource userExtSource = getPerunBl().getUsersManagerBl().getUserExtSourceByExtLogin(sess, memberSource, login);
				userExtSources.add(userExtSource);
			} catch (UserExtSourceNotExistsException e) {
				//skipping, this extSource does not exist and thus won't be in the list
			}
			Vo groupVo = getVo(sess, group);
			List<UserExtSource> additionalUserExtSources = Utils.extractAdditionalUserExtSources(sess, subjectFromLoginSource).stream().map(RichUserExtSource::asUserExtSource).collect(toList());
			userExtSources.addAll(additionalUserExtSources);
			for (UserExtSource source : userExtSources) {
				try {
					user = getPerunBl().getUsersManagerBl().getUserByUserExtSource(sess, source);
					// check if user is already member of group's vo
					if (getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user).contains(groupVo)) {
						if (idsOfUsersInGroup.containsKey(user.getId())) {
							//we can skip this one, because he is already in group, and remove him from the map
							//but first we need to also validate him if he was disabled before (invalidate and then validate)
							RichMember richMember = idsOfUsersInGroup.get(user.getId());
							if (richMember != null && Status.DISABLED.equals(richMember.getStatus())) {
								getPerunBl().getMembersManagerBl().invalidateMember(sess, richMember);
								try {
									getPerunBl().getMembersManagerBl().validateMember(sess, richMember);
								} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
									log.info("Switching member id {} into INVALID state from DISABLED, because there was problem with attributes {}.", richMember.getId(), e);
								}
							}
							idsOfUsersInGroup.remove(user.getId());
						} else {
							//he is not yet in group, so we need to create a candidate
							Candidate candidate = new Candidate(user, source);
							//for lightweight synchronization we want to skip all update of attributes
							candidate.setAttributes(new HashMap<>());
							candidatesToAdd.add(candidate);
						}
						break;
					}
				} catch(UserNotExistsException e) {
					//skip because the user from this ExtSource does not exist so we can continue
				}
			}

			// If user not found in group's vo, skip him and log it
			if (user == null) {
				log.debug("Subject {} with login {} was skipped during lightweight synchronization of group {} because he is not in vo of the group yet.", subjectFromLoginSource, login, group);
			}
		}

		//Rest of them need to be removed
		membersToRemove.addAll(idsOfUsersInGroup.values());
	}

	/**
	 * This method fill 3 member structures which get as parameters:
	 * 1. membersToUpdate - Candidates with equivalent Members from Perun for purpose of updating attributes and statuses
	 * 2. candidateToAdd - New members of the group
	 * 3. membersToRemove - Former members who are not in synchronized ExtSource now
	 *
	 * @param sess
	 * @param groupMembers current group members
	 * @param candidates to be synchronized from extSource
	 * @param membersToUpdate 1. container (more above)
	 * @param candidatesToAdd 2. container (more above)
	 * @param membersToRemove 3. container (more above)
	 *
	 */
	private void categorizeMembersForSynchronization(PerunSession sess, List<RichMember> groupMembers, List<Candidate> candidates, List<Candidate> candidatesToAdd, Map<Candidate, RichMember> membersToUpdate, List<RichMember> membersToRemove) {
		candidatesToAdd.addAll(candidates);
		membersToRemove.addAll(groupMembers);
		//mapping structure for more efficient searching
		Map<UserExtSource, RichMember> mappingStructure = this.createMappingStructure(groupMembers);

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
	private ExtSource getGroupMembersExtSourceForSynchronization(PerunSession sess, Group group, ExtSource defaultSource) throws WrongAttributeAssignmentException, AttributeNotExistsException, ExtSourceNotExistsException {
		//Prepare the groupMembersExtSource if it is set
		Attribute membersExtSourceNameAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPMEMBERSEXTSOURCE_ATTRNAME);
		ExtSource membersSource;
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
	private ExtSource getGroupExtSourceForSynchronization(PerunSession sess, Group group) throws WrongAttributeAssignmentException, AttributeNotExistsException, ExtSourceNotExistsException {
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
	private List<String> getOverwriteUserAttributesListFromExtSource(ExtSource membersSource) {
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
	 * Return login prefix for group structure if exists. In other case, return empty string.
	 *
	 * @param sess
	 * @param baseGroup base group for structure
	 * @return login prefix if exists, empty string otherwise
	 */
	private String getLoginPrefixForGroupStructure(PerunSession sess, Group baseGroup) {
		String loginPrefix = "";

		try {
			Attribute loginPrefixAttributeAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, baseGroup, GroupsManager.GROUPS_STRUCTURE_LOGIN_PREFIX_ATTRNAME);
			if(loginPrefixAttributeAttribute.getValue() != null) loginPrefix = loginPrefixAttributeAttribute.valueAsString();
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (AttributeNotExistsException ex) {
			//this is not a problem, if attribute not exists, it means there is no prefix set so we can skip it
		}

		return loginPrefix;
	}

	/**
	 * Get login attribute by login attribute name from base group. It is used for identifying groups
	 * in structure of groups.
	 *
	 * @param sess
	 * @param baseGroup base group to get login attribute for
	 *
	 * @return login attribute as attribute definition
	 *
	 * @throws InternalErrorException if some internal error happens or login attribute from extSource is null or empty
	 * @throws AttributeNotExistsException when attribute login from extSource not exists in Perun DB
	 */
	private AttributeDefinition getLoginAttributeForGroupStructure(PerunSession sess, Group baseGroup) throws AttributeNotExistsException {
		String loginAttributeName;

		try {
			Attribute loginAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, baseGroup, GroupsManager.GROUPS_STRUCTURE_LOGIN_ATTRNAME);
			loginAttributeName = loginAttribute.valueAsString();
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		if(loginAttributeName == null || loginAttributeName.isEmpty()) {
			throw new InternalErrorException("Missing login name for group structure under base group: " + baseGroup);
		}

		try {
			return getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, loginAttributeName);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("There is missing attribute login '" + loginAttributeName + "' for structure under base group:" + baseGroup, ex);
		}
	}

	/**
	 * Get map of all sub groups of defined base group with their logins as keys in map.
	 *
	 * @param sess
	 * @param baseGroup base group to get all subgroups for
	 * @param loginAttributeDefinition attribute to get login for every sub group in structure
	 * @return map of all groups with logins as keys
	 *
	 */
	private Map<String, Group> getAllSubGroupsWithLogins(PerunSession sess, Group baseGroup, AttributeDefinition loginAttributeDefinition) {
		Map<String, Group> listOfSubGroupsWithLogins = new HashMap<>();

		List<Group> groups = this.getAllSubGroups(sess, baseGroup);
		for(Group group: groups) {
			try {
				Attribute loginValue = perunBl.getAttributesManagerBl().getAttribute(sess, group, loginAttributeDefinition.getName());
				listOfSubGroupsWithLogins.put(loginValue.valueAsString(), group);
			} catch (WrongAttributeAssignmentException ex) {
				//it does mean wrong behavior of other methods
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("There is missing attribute login " + loginAttributeDefinition + " for group " + group + " in structure under base group " + baseGroup, ex);
			}
		}

		return listOfSubGroupsWithLogins;
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
	private List<String> getMemberAttributesListToBeMergedFromExtSource(ExtSource membersSource) {
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
	private boolean isThisLightweightSynchronization(PerunSession sess, Group group) throws WrongAttributeAssignmentException, AttributeNotExistsException {
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
	 *
	 * @return list of subjects
	 *
	 * @throws InternalErrorException if internal error occurs
	 */
	private List<Map<String, String>> getSubjectsFromExtSource(PerunSession sess, ExtSource source, Group group) {
		//Get all group attributes and store tham to map (info like query, time interval etc.)
		List<Attribute> groupAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, group);
		Map<String, String> groupAttributesMap = new HashMap<>();
		for (Attribute attr: groupAttributes) {
			String value = BeansUtils.attributeValueToString(attr);
			String name = attr.getName();
			groupAttributesMap.put(name, value);
		}
		//-- Get Subjects in form of map where left string is name of attribute and right string is value of attribute, every subject is one map
		List<Map<String, String>> subjects;
		try {
			subjects = ((ExtSourceSimpleApi) source).getGroupSubjects(groupAttributesMap);
			log.debug("Group synchronization {}: external group contains {} members.", group, subjects.size());
		} catch (ExtSourceUnsupportedOperationException e2) {
			throw new InternalErrorException("ExtSource " + source.getName() + " doesn't support getGroupSubjects", e2);
		}
		return subjects;
	}

	/**
	 * Convert List of subjects to list of Candidates.
	 *
	 * To getting Candidate can use 1 of 3 possible options:
	 * 1] membersSource and source are not equals => we have just login, other attributes neet to get from membersSource
	 * 2] membersSource==source and membersSource is instance of ExtSourceApi => we already have all attributes in subject
	 * 3] membersSource==source and membersSource is instance of SimplExtSourceApi => we have just login, need to read other attributes again
	 *
	 * If candidate cannot be get for some reason, add this reason to skippedMembers list and skip him.
	 *
	 * @param sess
	 * @param subjects list of subjects from ExtSource (at least login should be here)
	 * @param membersSource optional member ExtSource (if members attributes are from other source then their logins)
	 * @param source default group ExtSource
	 * @param actualGroupMembers actual members of synchronized group
	 * @param skippedMembers not successfully synchronized members are skipped and information about it should be added here
	 *
	 * @return list of successfully created candidates from subjects
	 *
	 * @throws InternalErrorException if some internal error occurs
	 */
	private List<Candidate> convertSubjectsToCandidates(PerunSession sess, List<Map<String, String>> subjects, ExtSource membersSource, ExtSource source, List<RichMember> actualGroupMembers, List<String> skippedMembers) {
		List<Candidate> candidates = new ArrayList<>();

		//mapping structure for more efficient searching of actual group members
		Map<UserExtSource, RichMember> mappingStructure = this.createMappingStructure(actualGroupMembers);

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
					candidates.add(new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, membersSource, login)));
				// 2] sources are same and we work with source which is instance of ExtSourceApi
				} else if (membersSource instanceof ExtSourceApi) {
					// we can use the data from this source without reading them again (all exists in the map of subject attributes)
					candidates.add(new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, subject, membersSource, login)));
				// 3] sources are same and we work with source which is instace of ExtSourceSimpleApi
				} else if (membersSource instanceof ExtSourceSimpleApi) {
					// we can't use the data from this source, we need to read them again (they are not in the map of subject attributes)
					candidates.add(new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, membersSource, login)));
				} else {
					// this could not happen without change in extSource API code
					throw new InternalErrorException("ExtSource is other instance than SimpleApi or Api and this is not supported!");
				}
			} catch (CandidateNotExistsException e) {
				log.warn("getGroupSubjects subjects returned login {}, but it cannot be obtained using getCandidate()", login);
				//If member can't be find in the member's extSource (we are missing other attributes) we can try find him in the group
				UserExtSource subjectUserExtSource = new UserExtSource(membersSource, login);
				//If member is in the group, we can create a simple object from him to preserve his existence in the group
				if(mappingStructure.containsKey(subjectUserExtSource)) {
					RichMember richMember = mappingStructure.get(subjectUserExtSource);
					//convert richMember to simple candidate object (to prevent wrong attribute updating)
					candidates.add(BeansUtils.convertRichMemberToCandidate(richMember, subjectUserExtSource));
					skippedMembers.add("MemberEntry:[" + richMember + "] was skipped from updating in the group, because he can't be found by login:'" + login + "' in extSource " + membersSource);
				} else {
					skippedMembers.add("MemberEntry:[" + subject + "] was skipped from adding to the group because he can't be found by login:'" + login + "' in extSource " + membersSource);
				}
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
	 * Get candidate and corresponding memberToUpdate and update his attributes, extSources, expiration and status.
	 *
	 * For Member - updateAttributes
	 * For User - updateAttributes if exists in list of overwriteUserAttributesList,
	 *            in other case just mergeAttributes.
	 *
	 * updateAttributes = store new values
	 * mergeAttributes = for List and Map add new values, do not remove old one,
	 *                   for other cases store new values (like String, Integer etc.)
	 *
	 * This method runs in separate transaction.
	 *
	 * @param sess perun session
	 * @param group to be synchronized
	 * @param candidate candidate to update by
	 * @param memberToUpdate richMember for updating in Perun by information from extSource
	 * @param overwriteUserAttributesList list of user attributes to be updated instead of merged
	 * @param mergeMemberAttributesList list of member attributes to be merged instead of updated
	 * @param attrDefs list of attribute definitions to update from candidate, if null the list is filled in process
	 *
	 * @throws AttributeNotExistsException if some attributes not exists and for this reason can't be updated
	 * @throws WrongAttributeAssignmentException if some attribute is updated in bad way (bad assignment)
	 */
	public void updateExistingMemberWhileSynchronization(PerunSession sess, Group group, Candidate candidate, RichMember memberToUpdate, List<String> overwriteUserAttributesList, List<String> mergeMemberAttributesList, List<AttributeDefinition> attrDefs) {
		//If member does not exists in this moment (somebody removed him before updating process), skip him and log it
		try {
			getPerunBl().getMembersManagerBl().checkMemberExists(sess, memberToUpdate);
		} catch (MemberNotExistsException ex) {
			//log it and skip this member
			log.debug("Someone removed member {} from group {} before updating process. Skip him.", memberToUpdate, group);
			return;
		}

		//load attrDefinitions if not received
		if (!candidate.getAttributes().isEmpty() && attrDefs.isEmpty()) {
			attrDefs = getAttributesToSynchronizeFromCandidates(sess, group, candidate);
		}

		//get RichMember with attributes
		memberToUpdate = getPerunBl().getMembersManagerBl().convertMembersToRichMembersWithAttributes(sess, Collections.singletonList(memberToUpdate), attrDefs).get(0);

		// try to find user core attributes and update user -> update name and titles
		updateUserCoreAttributes(sess, candidate, memberToUpdate, overwriteUserAttributesList);

		for (AttributeDefinition attributeDefinition : attrDefs) {
			//update member attribute
			if(attributeDefinition.getNamespace().startsWith(AttributesManager.NS_MEMBER_ATTR)) {
				updateMemberAttribute(sess, group, candidate, memberToUpdate, attributeDefinition, mergeMemberAttributesList);
			//update user attribute
			} else if(attributeDefinition.getNamespace().startsWith(AttributesManager.NS_USER_ATTR)) {
				updateUserAttribute(sess, group, candidate, memberToUpdate, attributeDefinition, overwriteUserAttributesList);
			} else {
				//we are not supporting other attributes than member or user so skip it without error, but log it
				log.warn("Attribute {} can't be set, because it is not member or user attribute.", attributeDefinition.getName());
			}
		}

		//Synchronize userExtSources (add not existing)
		addUserExtSources(sess, candidate, memberToUpdate);

		//Set correct member Status
		updateMemberStatus(sess, memberToUpdate);
	}

	/**
	 * Get all attributes to synchronize from candidate.
	 *
	 * @param sess perun session
	 * @param group group being synchronized
	 * @param candidate candidate from whom we get attributes
	 * @return list of attribute definitions
	 */
	private List<AttributeDefinition> getAttributesToSynchronizeFromCandidates(PerunSession sess, Group group, Candidate candidate) {
		List<AttributeDefinition> attrDefs = new ArrayList<>();

		if (candidate.getAttributes() == null) {
			return attrDefs;
		}

		for(String attrName : candidate.getAttributes().keySet()) {
			try {
				AttributeDefinition attrDef = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrName);
				attrDefs.add(attrDef);
			} catch (AttributeNotExistsException ex) {
				log.error("Can't synchronize attribute " + attrName + " for candidate " + candidate + " and for group " + group + "because the attribute definition does not exist.");
				//skip this attribute at all
			}
		}
		return attrDefs;
	}

	/**
	 * Update user core attributes from overwriteUserAttributesList based on values of candidate.
	 *
	 * @param sess perun session
	 * @param candidate candidate from whom we get attribute values
	 * @param memberToUpdate member to update
	 * @param overwriteUserAttributesList list of attributes to be updated
	 */
	private void updateUserCoreAttributes(PerunSession sess, Candidate candidate, RichMember memberToUpdate, List<String> overwriteUserAttributesList) {
		if (overwriteUserAttributesList != null) {
			boolean someFound = false;
			User user = memberToUpdate.getUser();
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
					throw new ConsistencyErrorException("User from perun does not exist when he should have - he was removed during sync.", e);
				}
			}
		}
	}

	/**
	 * Update value of member attribute based on value of candidate.
	 *
	 * @param sess perun session
	 * @param group group being synchronized
	 * @param candidate candidate from whom we get attribute values
	 * @param memberToUpdate member to update
	 * @param attributeDefinition attribute being updated
	 * @param mergeMemberAttributesList list of member attributes to be merged and not overwritten
	 */
	private void updateMemberAttribute(PerunSession sess, Group group, Candidate candidate, RichMember memberToUpdate, AttributeDefinition attributeDefinition, List<String> mergeMemberAttributesList) {
		for (Attribute memberAttribute: memberToUpdate.getMemberAttributes()) {
			if (memberAttribute.getName().equals(attributeDefinition.getName())) {
				Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeDefinition.getName()), memberAttribute.getType());
				if (subjectAttributeValue != null && !Objects.equals(memberAttribute.getValue(), subjectAttributeValue)) {
					log.trace("Group synchronization {}: value of the attribute {} for memberId {} changed. Original value {}, new value {}.",
						group, memberAttribute, memberToUpdate.getId(), memberAttribute.getValue(), subjectAttributeValue);
					memberAttribute.setValue(subjectAttributeValue);
					try {
						//Choose set or merge by extSource attribute mergeMemberAttributes (if contains this one)
						if (mergeMemberAttributesList != null && mergeMemberAttributesList.contains(memberAttribute.getName())) {
							getPerunBl().getAttributesManagerBl().mergeAttributeValueInNestedTransaction(sess, memberToUpdate, memberAttribute);
						} else {
							getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, memberToUpdate, memberAttribute);
						}
					} catch (AttributeValueException e) {
						// There is a problem with attribute value, so set INVALID status for the member
						getPerunBl().getMembersManagerBl().invalidateMember(sess, memberToUpdate);
					} catch	(WrongAttributeAssignmentException e) {
						throw new ConsistencyErrorException(e);
					}
				}
				//we found it, no need to continue in cycle
				break;
			}
		}
	}

	/**
	 * Update value of user attribute based on value of candidate.
	 *
	 * @param sess perun session
	 * @param group group being synchronized
	 * @param candidate candidate from whom we get attribute values
	 * @param memberToUpdate member to update
	 * @param attributeDefinition attribute being updated
	 * @param overwriteUserAttributesList list of user attributes to be overwritten and not merged
	 */
	private void updateUserAttribute(PerunSession sess, Group group, Candidate candidate, RichMember memberToUpdate, AttributeDefinition attributeDefinition, List<String> overwriteUserAttributesList) {
		for (Attribute userAttribute: memberToUpdate.getUserAttributes()) {
			if(userAttribute.getName().equals(attributeDefinition.getName())) {
				Object subjectAttributeValue = getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeDefinition.getName()), userAttribute.getType());
				if (!Objects.equals(userAttribute.getValue(), subjectAttributeValue)) {
					log.trace("Group synchronization {}: value of the attribute {} for memberId {} changed. Original value {}, new value {}.",
						group, userAttribute, memberToUpdate.getId(), userAttribute.getValue(), subjectAttributeValue);
					userAttribute.setValue(subjectAttributeValue);
					try {
						//Choose set or merge by extSource attribute overwriteUserAttributes (if contains this one)
						if(overwriteUserAttributesList != null && overwriteUserAttributesList.contains(userAttribute.getName())) {
							getPerunBl().getAttributesManagerBl().setAttributeInNestedTransaction(sess, memberToUpdate.getUser(), userAttribute);
						} else {
							getPerunBl().getAttributesManagerBl().mergeAttributeValueInNestedTransaction(sess, memberToUpdate.getUser(), userAttribute);
						}
					} catch (AttributeValueException e) {
						// There is a problem with attribute value, so set INVALID status for the member
						getPerunBl().getMembersManagerBl().invalidateMember(sess, memberToUpdate);
					} catch (WrongAttributeAssignmentException e) {
						throw new ConsistencyErrorException(e);
					}
				}
				//we found it, no need to continue in cycle
				break;
			}
		}
	}

	/**
	 * Add userExtSources to member from candidate during synchronization.
	 *
	 * @param sess perun session
	 * @param candidate candidate from whom we get the userExtSources
	 * @param memberToUpdate member to update
	 */
	private void addUserExtSources(PerunSession sess, Candidate candidate, RichMember memberToUpdate) {
		for (UserExtSource ues : candidate.getUserExtSources()) {
			if (!getPerunBl().getUsersManagerBl().userExtSourceExists(sess, ues)) {
				try {
					getPerunBl().getUsersManagerBl().addUserExtSource(sess, memberToUpdate.getUser(), ues);
				} catch (UserExtSourceExistsException e) {
					throw new ConsistencyErrorException("Adding already existing userExtSource " + ues, e);
				}
			}
		}
	}

	/**
	 * Update member status if not VALID nor SUSPENDED to EXPIRED or VALID based on membership expiration attribute.
	 *
	 * @param sess perun session
	 * @param memberToUpdate member to update
	 */
	private void updateMemberStatus(PerunSession sess, Member memberToUpdate) {
		Status memberStatus = memberToUpdate.getStatus();
		if (statusesAffectedBySynchronization.contains(memberStatus)) {
			//prepare variables with information about member's expiration
			boolean memberHasExpiration;
			boolean memberExpiredInPast = false;

			Date now = new Date();
			Date currentMembershipExpirationDate = now;
			Attribute membershipExpiration;
			try {
				membershipExpiration = getPerunBl().getAttributesManagerBl().getAttribute(sess, memberToUpdate, AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
			} catch (AttributeNotExistsException e) {
				throw new InternalErrorException("MembershipExpiration attribute doesn't exist.", e);
			} catch (WrongAttributeAssignmentException e) {
				// shouldn't happen
				throw new InternalErrorException("Member attribute is not member attribute.", e);
			}
			//Check if member has not empty expiration date
			memberHasExpiration = membershipExpiration.getValue() != null;

			if (memberHasExpiration) {
				//Check if member has expiration date in the past or not (default is false even if he doesn't have expiration at all)
				try {
					currentMembershipExpirationDate = BeansUtils.getDateFormatterWithoutTime().parse(membershipExpiration.valueAsString());
					memberExpiredInPast = currentMembershipExpirationDate.before(now);
				} catch (ParseException ex) {
					log.error("Group synchronization: memberId {} expiration String cannot be parsed, exception {}.", memberToUpdate.getId(), ex);
				}
			}

			if ((Status.DISABLED.equals(memberStatus) || Status.INVALID.equals(memberStatus)) && memberHasExpiration && memberExpiredInPast) {
				//If member has expiration in the past (should be expired now) and is in other state than expired, expire him
				try {
					//if success, this method will change status of member as side effect
					perunBl.getMembersManagerBl().expireMember(sess, memberToUpdate);
					log.info("Switching member id {} to EXPIRE state, due to expiration {}.", memberToUpdate.getId(), membershipExpiration.getValue());
					log.debug("Switching member to EXPIRE state, additional info: membership expiration date='{}', system now date='{}'", currentMembershipExpirationDate, now);
				} catch (WrongReferenceAttributeValueException | WrongAttributeValueException e) {
					log.error("Consistency error while trying to expire member id {}, exception {}", memberToUpdate.getId(), e);
				}
			} else if (!memberHasExpiration || !memberExpiredInPast) {
				//If member shouldn't be expired, validate him (don't have expiration at all or expire in the future from now)
				try {
					perunBl.getMembersManagerBl().validateMember(sess, memberToUpdate);
					log.info("Switching member id {} to VALID state, due to expiration {}.", memberToUpdate.getId(), membershipExpiration.getValue());
					log.debug("Switching member to VALID state, additional info: membership expiration date='{}', system now date='{}'", currentMembershipExpirationDate, now);
				} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
					log.error("Error during validating member id {}, exception {}", memberToUpdate.getId(), e);
				}
			}
		}
	}

	/**
	 * Get new candidate and add him to the Group.
	 *
	 * If Candidate can't be added to Group, skip him and add this information to skippedMembers list.
	 *
	 * When creating new member from Candidate, if user already exists, merge his attributes,
	 * if attribute exists in list of overwriteUserAttributesList, update it instead of merging.
	 *
	 * This method runs in separate transaction.
	 *
	 * @param sess perun session
	 * @param group to be synchronized
	 * @param candidate new member (candidate)
	 * @param overwriteUserAttributesList list of attributes to be updated for user if found
	 * @param mergeMemberAttributesList list of attributes to be merged for member if found
	 * @param skippedMembers list of not successfully synchronized members
	 */
	public void addMissingMemberWhileSynchronization(PerunSession sess, Group group, Candidate candidate, List<String> overwriteUserAttributesList, List<String> mergeMemberAttributesList, List<String> skippedMembers) {
		Member member;
		try {
			// Check if the member is already in the VO (just not in the group)
			member = getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate.getUserExtSources());

			// member exists - update attributes
			RichMember memberToUpdate = getPerunBl().getMembersManagerBl().getRichMember(sess, member);

			updateExistingMemberWhileSynchronization(sess, group, candidate, memberToUpdate, overwriteUserAttributesList, mergeMemberAttributesList, new ArrayList<>());

		} catch (MemberNotExistsException e) {
			try {
				// We have new member (candidate), so create him using synchronous createMember (and overwrite chosen user attributes)
				member = getPerunBl().getMembersManagerBl().createMemberSync(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate, null, overwriteUserAttributesList);
				log.info("Group synchronization {}: New member id {} created during synchronization.", group, member.getId());
			} catch (AlreadyMemberException e1) {
				//Probably race condition, give him another chance to fix this mess
				// Check if the member is already in the VO (just not in the group)
				try {
					member = getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, getPerunBl().getGroupsManagerBl().getVo(sess, group), candidate.getUserExtSources());
					// member exists - update attribute
					RichMember memberToUpdate = getPerunBl().getMembersManagerBl().getRichMember(sess, member);
					updateExistingMemberWhileSynchronization(sess, group, candidate, memberToUpdate, overwriteUserAttributesList, mergeMemberAttributesList, new ArrayList<>());
				} catch (Exception e2) {
					//Something is still wrong, thrown consistency exception
					throw new ConsistencyErrorException("Trying to add existing member (it is not possible to get him by userExtSource even if is also not possible to create him in DB)!");
				}
			} catch (AttributeValueException e1) {
				log.warn("Can't create member from candidate {} due to attribute value exception {}.", candidate, e1);
				skippedMembers.add("MemberEntry:[" + candidate + "] was skipped because there was problem when createing member from candidate: Exception: " + e1.getName() + " => '" + e1.getMessage() + "'");
				return;
			} catch (ExtendMembershipException e1) {
				log.warn("Can't create member from candidate {} due to membership expiration exception {}.", candidate, e1);
				skippedMembers.add("MemberEntry:[" + candidate + "] was skipped because membership expiration: Exception: " + e1.getName() + " => " + e1.getMessage() + "]");
				return;
			}
		}

		try {
			// Add the member to the group
			if (!group.getName().equals(VosManager.MEMBERS_GROUP)) {
				// Do not add members to the generic members group
				try {
					getPerunBl().getGroupsManagerBl().addMember(sess, group, member);
				} catch(GroupNotExistsException ex) {
					// Shouldn't happen, group should always exist
					throw new ConsistencyErrorException(ex);
				}
			}
			log.info("Group synchronization {}: New member id {} added.", group, member.getId());
		} catch (AlreadyMemberException e) {
			//This part is ok, it means someone add member before synchronization ends, log it and skip this member
			log.debug("Member {} was added to group {} before adding process. Skip this member.", member, group);
			return;
		} catch (AttributeValueException e) {
			// There is a problem with attribute value, so set INVALID status of the member
			getPerunBl().getMembersManagerBl().invalidateMember(sess, member);
		}

		// Try to validate member
		updateMemberStatus(sess, member);
	}

	/**
	 * Remove former member from group (if he is not listed in ExtSource).
	 *
	 * If this is membersGroup (of some Vo) try to disableMember, if not possible then delete him.
	 * If this is regular group (of some Vo) remove him and if this group is also
	 * his last authoritative group, disable or delete him also in the Vo.
	 *
	 * This method runs in separate transaction.
	 *
	 * @param sess perun session
	 * @param group to be synchronized
	 * @param memberToRemove member to be removed from Group
	 *
	 * @throws GroupNotExistsException if group does not exist
	 */
	public void removeFormerMemberWhileSynchronization(PerunSession sess, Group group, RichMember memberToRemove, boolean isAuthoritative) throws GroupNotExistsException {
		// Member is missing in the external group, so remove him from the perun group
		try {
			//members group
			if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
				// If the group is members group, the member must be disabled as a member of VO
				removeMemberFromMembersGroup(sess, group, memberToRemove);
			//not members group
			} else {
				//If this group is authoritative group, check if this is last authoritative group of this member
				//If Yes = deleteMember (from Vo), if No = only removeMember
				if (isAuthoritative) {
					removeMemberFromAuthoritativeGroup(sess, group, memberToRemove);
				} else {
					getPerunBl().getGroupsManagerBl().removeMember(sess, group, memberToRemove);
					log.info("Group synchronization {}: Member id {} removed.", group, memberToRemove.getId());
				}
			}
		} catch (NotGroupMemberException e) {
			throw new ConsistencyErrorException("Trying to remove non-existing user");
		} catch (MemberAlreadyRemovedException ex) {
			//Member was probably removed before starting of synchronization removing process, log it and skip this member
			log.debug("Member {} was removed from group {} before removing process. Skip this member.", memberToRemove, group);
		}
	}

	/**
	 * Return boolean value whether group is authoritative.
	 *
	 * @param sess perun session
	 * @param group group
	 * @return true if group is authoritative, otherwise false
	 * @throws WrongAttributeAssignmentException if there is some problem with assignment of attribute
	 */
	private boolean isAuthoritative(PerunSession sess, Group group) throws WrongAttributeAssignmentException {
		try {
			Attribute authoritativeGroupAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, A_G_D_AUTHORITATIVE_GROUP);
			if (authoritativeGroupAttr.getValue() != null) {
				return authoritativeGroupAttr.valueAsInteger() == 1;
			}
		} catch (AttributeNotExistsException ex) {
			//Means that this group is not authoritative
			log.error("Attribute {} doesn't exists.", A_G_D_AUTHORITATIVE_GROUP);
		}

		return false;
	}

	/**
	 * Remove member from authoritative group.
	 * If this is the last authoritative group of this member, delete him from vo, otherwise just disable him in vo.
	 *
	 * @param sess perun session
	 * @param group authoritative group
	 * @param memberToRemove member to remove
	 * @throws GroupNotExistsException if group does not exist
	 * @throws NotGroupMemberException if member does not exist
	 * @throws MemberAlreadyRemovedException if member was already removed
	 */
	private void removeMemberFromAuthoritativeGroup(PerunSession sess, Group group, RichMember memberToRemove) throws GroupNotExistsException, NotGroupMemberException, MemberAlreadyRemovedException {
		List<Group> memberAuthoritativeGroups = null;
		try {
			memberAuthoritativeGroups = getAllAuthoritativeGroupsOfMember(sess, memberToRemove);
		} catch (AttributeNotExistsException ex) {
			//This means that no authoritative group can exists without this attribute
			log.error("Attribute {} doesn't exists.", A_G_D_AUTHORITATIVE_GROUP);
		}

		//If list of member authoritativeGroups is not null, attribute exists
		if (memberAuthoritativeGroups != null) {
			memberAuthoritativeGroups.remove(group);
			if (memberAuthoritativeGroups.isEmpty()) {
				//First try to disable member, if is invalid, delete him from Vo
				try {
					getPerunBl().getMembersManagerBl().disableMember(sess, memberToRemove);
					log.debug("Group synchronization {}: Member id {} disabled because synchronizer wants to remove him from last authoritativeGroup in Vo.", group, memberToRemove.getId());
					getPerunBl().getGroupsManagerBl().removeMember(sess, group, memberToRemove);
					log.info("Group synchronization {}: Member id {} removed.", group, memberToRemove.getId());
				} catch (MemberNotValidYetException ex) {
					//Member is still invalid in perun. We can delete him.
					getPerunBl().getMembersManagerBl().deleteMember(sess, memberToRemove);
					log.info("Group synchronization {}: Member id {} would have been disabled but he has been deleted instead because he was invalid and synchronizer wants to remove him from last authoritativeGroup in Vo.", group, memberToRemove.getId());
				}
			} else {
				//If there is still some other authoritative group for this member, only remove him from group
				getPerunBl().getGroupsManagerBl().removeMember(sess, group, memberToRemove);
				log.info("Group synchronization {}: Member id {} removed.", group, memberToRemove.getId());
			}
			//If list of member authoritativeGroups is null, attribute not exists, only remove member from Group
		} else {
			getPerunBl().getGroupsManagerBl().removeMember(sess, group, memberToRemove);
			log.info("Group synchronization {}: Member id {} removed.", group, memberToRemove.getId());
		}
	}

	/**
	 * Remove member from members group.
	 * If member is invalid in perun, delete him, otherwise just disable him.
	 *
	 * @param sess perun session
	 * @param group members group
	 * @param memberToRemove member to remove
	 * @throws MemberAlreadyRemovedException
	 */
	private void removeMemberFromMembersGroup(PerunSession sess, Group group, RichMember memberToRemove) throws MemberAlreadyRemovedException {
		try {
			getPerunBl().getMembersManagerBl().disableMember(sess, memberToRemove);
			log.info("Group synchronization {}: Member id {} disabled.", group, memberToRemove.getId());
		} catch (MemberNotValidYetException ex) {
			//Member is still invalid in perun. We can delete him.
			getPerunBl().getMembersManagerBl().deleteMember(sess, memberToRemove);
			log.info("Group synchronization {}: Member id {} would have been disabled but he has been deleted instead because he was invalid.", group, memberToRemove.getId());
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
				log.info("Can't close membersSource connection.", e);
			}
		}
		if(source != null) {
			try {
				((ExtSourceSimpleApi) source).close();
			} catch (ExtSourceUnsupportedOperationException e) {
				// ExtSource doesn't support that functionality, so silently skip it.
			} catch (InternalErrorException e) {
				log.info("Can't close extSource connection.", e);
			}
		}
	}

	//----------- PRIVATE METHODS FOR  GROUP STRUCTURE SYNCHRONIZATION -----------

	private void saveInformationAboutGroupStructureSynchronization(PerunSession sess, Group group, boolean failedDueToException, String exceptionMessage) throws AttributeNotExistsException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException, WrongAttributeValueException {
		Date currentTimestamp = new Date();

		if (sess == null) {
			throw new InternalErrorException("Session is null when trying to save information about group structure synchronization. Group structure: " + group + ", timestamp: " + currentTimestamp + ", message: " + exceptionMessage);
		}

		if (group == null) {
			throw new InternalErrorException("Object group is null when trying to save information about group structure synchronization. Timestamp: " + currentTimestamp + ", message: " + exceptionMessage);
		}

		String correctTimestampString = BeansUtils.getDateFormatter().format(currentTimestamp);

		List<Attribute> attrsToSet = new ArrayList<>();

		exceptionMessage = processGroupStructureSynchronizationExceptionMessage(sess, group, failedDueToException, exceptionMessage);

		if (exceptionMessage == null) {
			attrsToSet.add(prepareGroupStructureSynchronizationAttribute(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lastSuccessGroupStructureSynchronizationTimestamp", correctTimestampString));
		}
		attrsToSet.add(prepareGroupStructureSynchronizationAttribute(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lastGroupStructureSynchronizationState", exceptionMessage));
		attrsToSet.add(prepareGroupStructureSynchronizationAttribute(sess, AttributesManager.NS_GROUP_ATTR_DEF + ":lastGroupStructureSynchronizationTimestamp", correctTimestampString));

		((PerunBl) sess.getPerun()).getAttributesManagerBl().setAttributes(sess, group, attrsToSet);
	}

	/**
	 * For given group, set up attributes specified in the given map.
	 * This method can only set group def attributes. Old values are
	 * replaced with the new ones.
	 *
	 * @param sess session
	 * @param group group
	 * @param additionalAttributes map with attrNames and values
	 */
	private void setUpAdditionalAttributes(PerunSession sess, Group group, Map<String, String> additionalAttributes) {
		additionalAttributes.forEach((attrName, rawValue) -> {
			if (!attrName.startsWith(AttributesManager.NS_GROUP_ATTR_DEF)) {
				throw new InternalErrorException("Cannot synchronize a non group-def attribute - " + attrName);
			}
			AttributeDefinition definition;
			try {
				definition = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrName);
			} catch (AttributeNotExistsException exception) {
				throw new InternalErrorException("Not existing attribute specified to be set for a group during group" +
						"structure synchronization: " + attrName, exception);
			}
			Object value = BeansUtils.stringToAttributeValue(rawValue, definition.getType());
			Attribute attribute = new Attribute(definition, value);
			try {
				getPerunBl().getAttributesManagerBl().setAttribute(sess, group, attribute);
			} catch (WrongAttributeValueException | WrongAttributeAssignmentException | WrongReferenceAttributeValueException e) {
				// Probably should not happen
				throw new InternalErrorException("Failed to set synced group attribute.", e);
			}
		});
	}

	/**
	 * Set up attributes, which are necessary for members synchronization,for all subgroups of given base group.
	 *
	 * Method used by group structure synchronization
	 *
	 * @param sess
	 * @param baseGroup from which are sub groups taken
	 * @param source from which members are synchronized
	 * @param loginAttributeDefinition attribute definition of group's login in the structure
	 * @param loginPrefix prefix for login in structure
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	private void setUpSynchronizationAttributesForAllSubGroups(PerunSession sess, Group baseGroup, ExtSource source, AttributeDefinition loginAttributeDefinition, String loginPrefix) throws AttributeNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Attribute baseMembersQuery = getPerunBl().getAttributesManagerBl().getAttribute(sess, baseGroup, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

		if (baseMembersQuery.getValue() == null) {
			throw new WrongAttributeValueException("Group members query attribute is not set for base group " + baseGroup + "!");
		}

		Attribute membersQueryAttribute = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, GroupsManager.GROUPMEMBERSQUERY_ATTRNAME));
		Attribute baseMemberExtsource = getPerunBl().getAttributesManagerBl().getAttribute(sess, baseGroup, GroupsManager.GROUPMEMBERSEXTSOURCE_ATTRNAME);
		Attribute lightWeightSynchronization = getPerunBl().getAttributesManagerBl().getAttribute(sess, baseGroup, GroupsManager.GROUPLIGHTWEIGHTSYNCHRONIZATION_ATTRNAME);
		Attribute synchronizationInterval = getPerunBl().getAttributesManagerBl().getAttribute(sess, baseGroup, GroupsManager.GROUPSYNCHROINTERVAL_ATTRNAME);
		Attribute extSourceNameAttr = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, GroupsManager.GROUPEXTSOURCE_ATTRNAME));
		Attribute synchroEnabled = new Attribute(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME));
		Attribute synchronizationTimes = getPerunBl().getAttributesManagerBl().getAttribute(sess, baseGroup, GroupsManager.GROUP_SYNCHRO_TIMES_ATTRNAME);

		extSourceNameAttr.setValue(source.getName());
		synchroEnabled.setValue("true");

		List<Group> groupsForMemberSynchronization = getAllSubGroups(sess, baseGroup);

		//for each group set attributes for members synchronization, synchronize them and save the result
		for (Group group: groupsForMemberSynchronization) {
			try {
				getPerunBl().getExtSourcesManagerBl().addExtSource(sess, group, source);
			} catch (ExtSourceAlreadyAssignedException e) {
				log.info("ExtSource already assigned to group: {}", group);
			}

			//we want to set login of group
			Attribute loginAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, loginAttributeDefinition.getName());
			if(loginAttribute.getValue() == null) throw new InternalErrorException("For purpose of setting attributes for " + group + " we need to have not empty group login " + loginAttribute);
			//replace question mark for login without prefix (strip prefix from it)
			membersQueryAttribute.setValue(baseMembersQuery.getValue().toString().replace("?", loginAttribute.valueAsString().replaceFirst(loginPrefix, "")));

			getPerunBl().getAttributesManagerBl().setAttributes(sess, group, Arrays.asList(baseMemberExtsource, lightWeightSynchronization, synchronizationInterval, synchroEnabled, membersQueryAttribute, synchronizationTimes, extSourceNameAttr));
		}

	}

	/**
	 * Compose message about skipped objects
	 *
	 * Method used by group structure synchronization
	 *
	 * @param skippedObjects objects which were skipped during synchronization
	 * @param objectName name of objects which were skipped
	 * @return composed message about skipped objects
	 */
	private String prepareSkippedObjectsMessage(List<String> skippedObjects, String objectName) {
		String skippedObjectsMessage = null;

		if (!skippedObjects.isEmpty()) {
			skippedObjectsMessage = "These " + objectName + " from extSource were skipped: { ";

			for (String skippedObject : skippedObjects) {
				if (skippedObject == null) continue;

				skippedObjectsMessage += skippedObjects + ", ";
			}
			skippedObjectsMessage += " }";
		}
		return skippedObjectsMessage;
	}

	/**
	 * Interrupt threads after timeout and remove all interrupted threads (group structure synchronization threads)
	 *
	 * Method used by group structure synchronization
	 *
	 * @return number of removed threads
	 */
	private int processCurrentGroupStructureSynchronizationThreads() {
		int timeoutMinutes = BeansUtils.getCoreConfig().getGroupStructureSynchronizationTimeout();

		int numberOfNewlyRemovedThreads = 0;
		Iterator<GroupStructureSynchronizerThread> threadIterator = groupStructureSynchronizerThreads.iterator();
		while(threadIterator.hasNext()) {
			GroupStructureSynchronizerThread thread = threadIterator.next();

			long threadStart = thread.getStartTime();
			long timeDiff = System.currentTimeMillis() - threadStart;

			//If thread was interrupted by anything, remove it from the pool of active threads
			if (thread.isInterrupted() || !thread.isAlive()) {
				numberOfNewlyRemovedThreads++;
				threadIterator.remove();
			} else if (threadStart != 0 && timeDiff/1000/60 > timeoutMinutes) {
				//If thread start time is 0, this thread is waiting for another job, skip it
				// If the time is greater than timeout set in the configuration file (in minutes), interrupt and remove this thread from pool
				log.error("Thread was interrupted while synchronizing the group structure {} because of timeout!", thread.getGroup());
				thread.interrupt();
				threadIterator.remove();
				numberOfNewlyRemovedThreads++;
			}
		}

		return numberOfNewlyRemovedThreads;
	}

	/**
	 * Create and start new threads for group structure synchronization
	 *
	 * Method used by group structure synchronization
	 *
	 * @param sess
	 * @return number of created threads
	 * @throws InternalErrorException
	 */
	private int createNewGroupStructureSynchronizationThreads(PerunSession sess) {
		int numberOfNewlyCreatedThreads = 0;
		while(groupStructureSynchronizerThreads.size() < maxConcurrentGroupsStructuresToSynchronize) {
			GroupStructureSynchronizerThread thread = new GroupStructureSynchronizerThread(sess);
			thread.start();
			groupStructureSynchronizerThreads.add(thread);
			numberOfNewlyCreatedThreads++;
			log.debug("New thread for group structure synchronization started.");
		}

		return numberOfNewlyCreatedThreads;
	}

	/**
	 * Add new groups to the group structure synchronization poll
	 *
	 * Method used by group structure synchronization
	 *
	 * @param sess
	 * @return number of added groups
	 * @throws InternalErrorException
	 */
	private int addGroupsToGroupStructureSynchronizationPool(PerunSession sess) {
		// Get the number of miliseconds from the epoch, so we can divide it by the synchronization interval value
		long millisecondsFromEpoch = System.currentTimeMillis();
		long minutesFromEpoch = millisecondsFromEpoch/1000/60;

		LocalDateTime localDateTime = new Timestamp(millisecondsFromEpoch).toLocalDateTime();

		List<Group> groups = groupsManagerImpl.getGroupsStructuresToSynchronize(sess);
		List<Group> timeCompliantGroups = new ArrayList<>();

		for (Group group: groups) {

			try {
				Attribute synchronizationTimesAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUP_STRUCTURE_SYNCHRO_TIMES_ATTRNAME);
				if (synchronizationTimesAttr.getValue() != null) {
					if (isTimeCompliantWithExactTimes(localDateTime, synchronizationTimesAttr.valueAsList())) {
						timeCompliantGroups.add(group);
					}
				} else if (isTimeCompliantWithGroupInterval(sess, group, minutesFromEpoch, GroupsManager.GROUP_STRUCTURE_SYNCHRO_INTERVAL_ATTRNAME,"Group structure")) {
					timeCompliantGroups.add(group);
				}
			} catch (AttributeNotExistsException e) {
				log.error("Required attribute {} isn't defined in Perun!", GroupsManager.GROUP_STRUCTURE_SYNCHRO_TIMES_ATTRNAME);
			} catch (WrongAttributeAssignmentException e) {
				log.error("Cannot get attribute " + GroupsManager.GROUP_STRUCTURE_SYNCHRO_TIMES_ATTRNAME + " for group " + group + " due to exception.", e);
			}

		}

		return poolOfSynchronizations.putGroupsStructuresToPoolOfWaitingGroupsStructures(timeCompliantGroups);
	}

	/**
	 * Checks content of exception message and base on that sets attributes, logs, etc...
	 *
	 * Method used by group structure synchronization
	 *
	 * @param sess perun session
	 * @param group under which was group structure synchronization executed
	 * @param failedDueToException boolean value if there was an exception raised during group structure synchronization
	 * @param originalExceptionMessage group structure synchronization exception message
	 * @return exceptionMessage, either modified or unmodified
	 * @throws InternalErrorException
	 */
	private String processGroupStructureSynchronizationExceptionMessage(PerunSession sess, Group group, boolean failedDueToException, String originalExceptionMessage) {
		String exceptionMessage = originalExceptionMessage;
		if (exceptionMessage != null && exceptionMessage.isEmpty()) {
			exceptionMessage = "Empty message.";
		} else if (exceptionMessage != null && exceptionMessage.length() > 1000) {
			exceptionMessage = exceptionMessage.substring(0, 1000) + " ... message is too long, other info is in perun log file. If needed, please ask perun administrators.";
		}

		if(exceptionMessage != null) {
			if(failedDueToException) {
				getPerunBl().getAuditer().log(sess, new GroupStructureSyncFailed(group));
				log.debug("{} structure synchronization failed because of {}", group, originalExceptionMessage);
			} else {
				getPerunBl().getAuditer().log(sess, new GroupStructureSyncFinishedWithErrors(group));
				log.debug("{} structure synchronization finished with errors: {}", group, originalExceptionMessage);
			}
		}

		return exceptionMessage;
	}

	/**
	 * Creates new attribute, set it value and add attribute to list of attributes
	 *
	 * Method used by group structure synchronization
	 *
	 * @param sess perun session
	 * @param attributeName name of the attribute
	 * @param attributeValue string value which will be set to the attribute
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	private Attribute prepareGroupStructureSynchronizationAttribute(PerunSession sess, String attributeName, String attributeValue) throws AttributeNotExistsException {
		Attribute attributeToProcess = new Attribute(((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, attributeName));
		attributeToProcess.setValue(attributeValue);
		return attributeToProcess;
	}

	/**
	 * This method categorize candidate groups to groups to add, update and remove
	 *
	 * Method used by group structure synchronization
	 *
	 * @param currentGroups current groups
	 * @param candidateGroups to be synchronized from extSource
	 * @param groupsToUpdate Candidate groups with equivalent Groups from Perun for purpose of updating attributes
	 * @param candidateGroupsToAdd  New groups
	 * @param groupsToRemove Former groups which are not in synchronized ExtSource now
	 */
	private void categorizeGroupsForSynchronization(Map<String, Group> currentGroups, List<CandidateGroup> candidateGroups, List<CandidateGroup> candidateGroupsToAdd, Map<CandidateGroup, Group> groupsToUpdate, List<Group> groupsToRemove) {
		candidateGroupsToAdd.addAll(candidateGroups);
		groupsToRemove.addAll(currentGroups.values());

		for(CandidateGroup candidateGroup : candidateGroups) {
			String loginOfCandidateGroup = candidateGroup.getLogin();

			if(currentGroups.containsKey(loginOfCandidateGroup)) {
				groupsToUpdate.put(candidateGroup, currentGroups.get(loginOfCandidateGroup));
				candidateGroupsToAdd.remove(candidateGroup);
				groupsToRemove.remove(currentGroups.get(loginOfCandidateGroup));
			}
		}
	}

	/**
	 * Method checks if group structure synchronization has to respect hierarchy of groups in external source.
	 *
	 * True: we don't want to synchronize group structure with hierarchy,
	 *       every group in structure will be placed under base group
	 * False: we want to synchronize group structure with hierarchy.
	 *        That means, we want to create groups and subgroups as they are in the extSource
	 *
	 * Method used by group structure synchronization
	 *
	 * @param sess
	 * @param group to be synchronized
	 * @return true if this is flatSynchronization, false if not
	 * @throws InternalErrorException if something happens while getting flatStructureSynchronization attribute
	 * @throws WrongAttributeAssignmentException if bad assignment of flatStructureSynchronization attribute
	 * @throws AttributeNotExistsException if flatStructureSynchronization attribute not exists in perun Database
	 */
	private boolean isThisFlatSynchronization(PerunSession sess, Group group) throws WrongAttributeAssignmentException, AttributeNotExistsException {
		Attribute flatSynchronizationAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUP_FLAT_SYNCHRONIZATION_ATTRNAME);
		boolean flatSynchronization = false;
		if(flatSynchronizationAttr != null && flatSynchronizationAttr.getValue() != null) {
			flatSynchronization = flatSynchronizationAttr.valueAsBoolean();
		}
		return flatSynchronization;
	}

	/**
	 * Return List of subjects, where subject is map of attribute names and attribute values.
	 * Every subject is structure for creating CandidateGroup from ExtSource.
	 *
	 * Method used by group structure synchronization
	 *
	 * @param sess
	 * @param source to get subjects from
	 * @param group under which we will be synchronizing groups
	 *
	 * @return list of subjects
	 *
	 * @throws InternalErrorException if internal error occurs
	 */
	private List<Map<String, String>> getSubjectGroupsFromExtSource(PerunSession sess, ExtSource source, Group group) {
		List<Attribute> groupAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, group);
		Map<String, String> groupAttributesMap = new HashMap<>();

		for (Attribute attr: groupAttributes) {
			String value = BeansUtils.attributeValueToString(attr);
			String name = attr.getName();
			groupAttributesMap.put(name, value);
		}

		List<Map<String, String>> subjects;
		try {
			subjects = ((ExtSourceSimpleApi) source).getSubjectGroups(groupAttributesMap);
			log.debug("Group synchronization {}: external source contains {} group.", group, subjects.size());
		} catch (ExtSourceUnsupportedOperationException e2) {
			throw new InternalErrorException("ExtSource " + source.getName() + " doesn't support getSubjectGroups", e2);
		}
		return subjects;
	}

	/**
	 * Get Map groupsToUpdate and update their parent group and description.
	 * We don't have to update short name, because if short name has changed, group will be removed and created with new name.
	 *
	 * If some problem occurs, add groupToUpdate to skippedGroups and skip it.
	 *
	 * Method is used by group structure synchronization.
	 *
	 * @param sess
	 * @param baseGroup under which will be group structure synchronized
	 * @param groupsToUpdate list of groups for updating in Perun by information from extSource
	 * @param removedGroupsIds list of ids already removed groups (these groups not exists in Perun anymore)
	 * @param loginAttributeDefinition attribute definition for login of group
	 * @param skippedGroups groups to be skipped because of any expected problem
	 *
	 * @throws InternalErrorException if some internal error occurs
	 */
	private void updateExistingGroupsWhileSynchronization(PerunSession sess, Group baseGroup, Map<CandidateGroup, Group> groupsToUpdate, List<Integer> removedGroupsIds, AttributeDefinition loginAttributeDefinition, List<String> skippedGroups) {

		for(CandidateGroup candidateGroup: groupsToUpdate.keySet()) {
			Group groupToUpdate = groupsToUpdate.get(candidateGroup);
			//If group had parent which was already removed from perun, it was moved under base group, change it's parent group id properly for updating
			if(removedGroupsIds.contains(groupToUpdate.getParentGroupId())) groupToUpdate.setParentGroupId(baseGroup.getId());

			setUpAdditionalAttributes(sess, groupToUpdate, candidateGroup.getAdditionalAttributes());

			Group newParentGroup = specifyParentForUpdatedGroup(sess, groupToUpdate, baseGroup, candidateGroup, loginAttributeDefinition.getName());

			if(newParentGroup != null) {
				try {
					moveGroup(sess, newParentGroup, groupToUpdate);
					log.trace("Group structure synchronization {}: value of the parentGroupId for groupId {} changed. Original value {}, new value {}.",
							baseGroup, groupToUpdate.getId(), groupToUpdate.getParentGroupId(), newParentGroup.getId());
				} catch (GroupMoveNotAllowedException e) {
					log.warn("Can't update group {} due to group move not allowed exception {}.", groupToUpdate, e);
					skippedGroups.add("GroupEntry:[" + groupToUpdate + "] was skipped because group move is not allowed: Exception: " + e.getName() + " => " + e.getMessage() + "]");
					continue;
				} catch (WrongAttributeValueException e) {
					log.warn("Can't update group {} from baseGroup {} due to wrong attribute value exception {}.", groupToUpdate, e);
					skippedGroups.add("GroupEntry:[" + groupToUpdate + "] was skipped because of wrong attribute value: Exception: " + e.getName() + " => " + e.getMessage() + "]");
					continue;
				} catch (WrongReferenceAttributeValueException e) {
					log.warn("Can't update group {} from baseGroup {} due to wrong reference attribute value exception {}.", groupToUpdate, e);
					skippedGroups.add("GroupEntry:[" + groupToUpdate + "] was skipped because of wrong reference attribute value: Exception: " + e.getName() + " => " + e.getMessage() + "]");
					continue;
				}
			}

			boolean changed = updateGroupDescription(sess, groupToUpdate, candidateGroup);
			if(changed) {
				log.trace("Group structure synchronization {}: value of the group description for groupId {} changed. Original value {}, new value {}.",
						baseGroup, groupToUpdate.getId(), groupToUpdate.getDescription(), candidateGroup.asGroup().getDescription());
			}

		}
	}

	/**
	 * Specify parent group for group which is updated by synchronization
	 *
	 * Method is used by group structure synchronization.
	 *
	 * @param groupToUpdate for which will be parent specified
	 * @param baseGroup under which may be new parent
	 * @param candidateGroup with potential new parent group
	 * @param loginAttributeName attribute name for login of group
	 * @return updated parent group (if null, parent group hasn't changed)
	 * @throws InternalErrorException
	 */
	private Group specifyParentForUpdatedGroup(PerunSession sess, Group groupToUpdate, Group baseGroup, CandidateGroup candidateGroup, String loginAttributeName) {
		//get current parent of updated group, if there is no group (null), it means the group is under the base group
		String oldParentGroupLogin = getOldParentGroupLogin(sess, groupToUpdate, baseGroup, loginAttributeName);

		//check if there is difference between parents of current and new group, if not, return null
		if(Objects.equals(oldParentGroupLogin, candidateGroup.getParentGroupLogin())) return null;

		//if there is difference, we need to find a new parent group
		Group newParentGroup = getNewParentGroup(sess, candidateGroup, baseGroup, loginAttributeName);

		//check if the new parent group is also under the base group somewhere (if not, set base group as new parent group)
		if(!newParentGroup.equals(baseGroup) && !getAllSubGroups(sess, baseGroup).contains(newParentGroup)) {
			newParentGroup = baseGroup;
		}

		//check situation, where group is already under base group and the destination parent group is now base group too
		if(oldParentGroupLogin == null && newParentGroup.equals(baseGroup)) {
			return null;
		}

		return newParentGroup;
	}

	/**
	 * Return parent group for the candidate group
	 *
	 * @param sess perun session
	 * @param candidateGroup candidate group
	 * @param baseGroup base group in structure
	 * @param loginAttributeName attribute name of login in the structure
	 * @return parent group of candidate group, if there is none, return base group
	 */
	private Group getNewParentGroup(PerunSession sess, CandidateGroup candidateGroup, Group baseGroup, String loginAttributeName) {
		Group newParentGroup = baseGroup;

		//if parent group login is not null, we need to find this new parent group (otherwise it is base group)
		if(candidateGroup.getParentGroupLogin() != null) {
			try {
				//we need to have vo to filter groups, base group has to be from the same vo as new parent of candidate group
				Vo baseGroupVo = getPerunBl().getVosManagerBl().getVoById(sess, baseGroup.getVoId());
				List<Group> groupsWithLogin = getPerunBl().getSearcherBl().getGroups(sess, baseGroupVo, Collections.singletonMap(loginAttributeName, candidateGroup.getParentGroupLogin()));
				//if there is missing the destination parent group, set base group as parent, there could be missing parent group in the extSource data
				if (!groupsWithLogin.isEmpty()) {
					//if there are more than one group with login, there is no way how to choose correct behavior
					if (groupsWithLogin.size() > 1)
						throw new InternalErrorException("More than 1 group has login attribute '" + loginAttributeName + "' set with the same login " + candidateGroup.getParentGroupLogin());
					//our new parent group is exactly the one with the login set
					return groupsWithLogin.get(0);
				}
			} catch (VoNotExistsException ex) {
				throw new InternalErrorException("Vo of base group: " + baseGroup + " can't be found. Unexpected situation, can't continue.");
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("Can't find attribute definition for '" + loginAttributeName + "'.");
			}
		}

		return newParentGroup;
	}

	/**
	 * Return parent group login for the group to update in the group structure.
	 *
	 * @param sess perun session
	 * @param groupToUpdate group to update
	 * @param baseGroup base group in structure
	 * @param loginAttributeName attribute name of login in the structure
	 * @return current parent group login of group to update, null if there is none (it means base group should be parent)
	 */
	private String getOldParentGroupLogin(PerunSession sess, Group groupToUpdate, Group baseGroup, String  loginAttributeName) {
		String oldParentGroupLogin = null;

		//if group to update has parent group, return it and also it's login in the structure
		if(groupToUpdate.getParentGroupId() != null && groupToUpdate.getParentGroupId() != baseGroup.getId()) {
			try {
				Group actualParentGroup = getGroupById(sess, groupToUpdate.getParentGroupId());
				Attribute parentGroupLoginAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, actualParentGroup, loginAttributeName);
				//if there is missing login attribute of parent, throw an exception
				if(parentGroupLoginAttribute.getValue() == null) throw new InternalErrorException("Can't work with parent group " + actualParentGroup + " without proper login attribute set.");
				oldParentGroupLogin = parentGroupLoginAttribute.valueAsString();
			} catch (WrongAttributeAssignmentException | GroupNotExistsException ex) {
				//group should exist and wrong assignment means problem with setting of structure login attribute
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				//parent group need to have login or we can't really work with it
				throw new InternalErrorException("Can't work with parent group of group: " + groupToUpdate + " without proper login attribute set.", ex);
			}
		}

		return oldParentGroupLogin;
	}

	/**
	 * Update group description.
	 *
	 * Method is used by group structure synchronization.
	 *
	 * @param sess
	 * @param groupWithOldDescription group with description which will be chaged
	 * @param groupWithNewDescription candidate group with description which will replace the old one
	 * @return true if description changed, false otherwise
	 * @throws InternalErrorException
	 */
	private boolean updateGroupDescription(PerunSession sess, Group groupWithOldDescription, CandidateGroup groupWithNewDescription) {
		//If the old description is not null, compare it with the newDescription and update it if they differ
		if(groupWithOldDescription.getDescription() != null) {
			if(!groupWithOldDescription.getDescription().equals(groupWithNewDescription.asGroup().getDescription())){
				groupWithOldDescription.setDescription(groupWithNewDescription.asGroup().getDescription());
				try {
					groupsManagerImpl.updateGroup(sess, groupWithOldDescription);
				} catch (GroupExistsException ex) {
					throw new InternalErrorException("Unexpected exception when trying to modify group description!");
				}
				getPerunBl().getAuditer().log(sess, new GroupUpdated(groupWithOldDescription));
				return true;
			}
		// If the new description is not null set the old description to new one
		} else if(groupWithNewDescription.asGroup().getDescription() != null){
			groupWithOldDescription.setDescription(groupWithNewDescription.asGroup().getDescription());
			try {
				groupsManagerImpl.updateGroup(sess, groupWithOldDescription);
			} catch (GroupExistsException ex) {
				throw new InternalErrorException("Unexpected exception when trying to modify group description!");
			}
			getPerunBl().getAuditer().log(sess, new GroupUpdated(groupWithOldDescription));
			return true;
		}
		return false;
	}

	/**
	 * Add missing groups under base group in Perun
	 *
	 * If some problem occurs, add candidateGroup to skippedGroups and skip it.
	 *
	 * Method is used by group structure synchronization.
	 *
	 * @param sess
	 * @param baseGroup under which we will be synchronizing groups
	 * @param candidateGroupsToAdd list of new groups (candidateGroups)
	 * @param loginAttributeDefinition attribute definition for login of group
	 * @param skippedGroups groups to be skipped because of any expected problem
	 *
	 * @throws InternalErrorException if some internal error occurs
	 */
	private void addMissingGroupsWhileSynchronization(PerunSession sess, Group baseGroup, List<CandidateGroup> candidateGroupsToAdd, AttributeDefinition loginAttributeDefinition, List<String> skippedGroups) {
		Map<CandidateGroup, Group> groupsToUpdate = new HashMap<>();

		//create all groups under base group first
		for (CandidateGroup candidateGroup: candidateGroupsToAdd) {
			try {
				//create group
				Group createdGroup = createGroup(sess, baseGroup, candidateGroup.asGroup());
				groupsToUpdate.put(candidateGroup, createdGroup);
				log.info("Group structure synchronization under base group {}: New Group id {} created during synchronization.", baseGroup, createdGroup.getId());

				//set login for group
				String login = candidateGroup.getLogin();
				if(login == null) throw new InternalErrorException("Login of candidate group " + candidateGroup + " can't be null!");
				Attribute loginAttribute = new Attribute(loginAttributeDefinition);
				loginAttribute.setValue(login);
				getPerunBl().getAttributesManagerBl().setAttribute(sess, createdGroup, loginAttribute);

			} catch (GroupExistsException e) {
				log.warn("Group {} was added to group structure {} before adding process. Skip this group.", candidateGroup, baseGroup);
				skippedGroups.add("GroupEntry:[" + candidateGroup + "] was skipped because it was added to group structure before adding process: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (GroupRelationNotAllowed e) {
				log.warn("Can't create group from candidate group {} due to group relation not allowed exception {}.", candidateGroup, e);
				skippedGroups.add("GroupEntry:[" + candidateGroup + "] was skipped because group relation was not allowed: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (GroupRelationAlreadyExists e) {
				log.warn("Can't create group from candidate group {} due to group relation already exists exception {}.", candidateGroup, e);
				skippedGroups.add("GroupEntry:[" + candidateGroup + "] was skipped because group relation already exists: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (WrongAttributeAssignmentException ex) {
				//this means wrong setting of login attribute
				throw new InternalErrorException(ex);
			} catch (WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
				throw new InternalErrorException("Group login can't be set because of wrong value!", ex);
			}
		}
		// update newly added groups cause the hierarchy could be incorrect
		//no need to send list of removed parent groups here, because it is no need to resolve it for new groups at all
		updateExistingGroupsWhileSynchronization(sess, baseGroup, groupsToUpdate, Collections.emptyList(), loginAttributeDefinition, skippedGroups);
	}

	/**
	 * remove groups which are not listed in extSource anymore
	 *
	 * If some problem occurs, add groupToRemove to skippedGroups and skip it.
	 *
	 * Method is used by group structure synchronization.
	 *
	 * @param sess
	 * @param baseGroup from which we will be removing groups
	 * @param groupsToRemove list of groups to be removed from baseGroup
	 *
	 * @return list of ids already removed groups
	 * @throws InternalErrorException if some internal error occurs
	 */
	private List<Integer> removeFormerGroupsWhileSynchronization(PerunSession sess, Group baseGroup, List<Group> groupsToRemove, List<String> skippedGroups) {
		List<Integer> removedGroups = new ArrayList<>();
		groupsToRemove.sort(reverseOrder(comparingInt(g -> g.getName().length())));

		for (Group groupToRemove: groupsToRemove) {
			try {
				groupToRemove = moveSubGroupsUnderBaseGroup(sess, groupToRemove, baseGroup);
				deleteGroup(sess, groupToRemove, true);
				removedGroups.add(groupToRemove.getId());
				log.info("Group structure synchronization {}: Group id {} removed.", baseGroup, groupToRemove.getId());
			} catch (RelationExistsException e) {
				log.warn("Can't remove group {} from baseGroup {} due to group relation exists exception {}.", groupToRemove, e);
				skippedGroups.add("GroupEntry:[" + groupToRemove + "] was skipped because group relation exists: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (GroupAlreadyRemovedException | GroupAlreadyRemovedFromResourceException e) {
				log.debug("Group {} was removed from group {} before removing process. Skip this group.", groupToRemove, baseGroup);
			} catch (GroupNotExistsException e) {
				log.warn("Can't remove group {} from baseGroup {} due to group not exists exception {}.", groupToRemove, e);
				skippedGroups.add("GroupEntry:[" + groupToRemove + "] was skipped because group does not exists: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (GroupRelationDoesNotExist e) {
				log.warn("Can't remove group {} from baseGroup {} due to group relation does not exists exception {}.", groupToRemove, e);
				skippedGroups.add("GroupEntry:[" + groupToRemove + "] was skipped because group relation does not exists: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (GroupRelationCannotBeRemoved e) {
				log.warn("Can't remove group {} from baseGroup {} due to group relation cannot be removed exception {}.", groupToRemove, e);
				skippedGroups.add("GroupEntry:[" + groupToRemove + "] was skipped because group relation cannot be removed: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (GroupMoveNotAllowedException e) {
				log.warn("Can't remove group {} from baseGroup {} due to group move not allowed exception {}.", groupToRemove, e);
				skippedGroups.add("GroupEntry:[" + groupToRemove + "] was skipped because group move is not allowed: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (WrongAttributeValueException e) {
				log.warn("Can't remove group {} from baseGroup {} due to wrong attribute value exception {}.", groupToRemove, e);
				skippedGroups.add("GroupEntry:[" + groupToRemove + "] was skipped because wrong attribute value: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			} catch (WrongReferenceAttributeValueException e) {
				log.warn("Can't remove group {} from baseGroup {} due to wrong reference attribute value exception {}.", groupToRemove, e);
				skippedGroups.add("GroupEntry:[" + groupToRemove + "] was skipped because wrong reference attribute value: Exception: " + e.getName() + " => " + e.getMessage() + "]");
			}
		}

		return removedGroups;
	}

	/**
	 * Move subGroups from processed group under destination group.
	 * At the end, get processed group from database so we have updated variable
	 *
	 * Method is used by group structure synchronization.
	 *
	 * @param sess
	 * @param processedGroup from which are subGroups moved
	 * @param destinationGroup to which are subGroups moved
	 * @return processed group without subGroups
	 * @throws InternalErrorException
	 * @throws GroupMoveNotAllowedException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws GroupNotExistsException
	 */
	private Group moveSubGroupsUnderBaseGroup(PerunSession sess, Group processedGroup, Group destinationGroup) throws GroupMoveNotAllowedException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupNotExistsException {
		List<Group> subGroups = getSubGroups(sess, processedGroup);

		for (Group subGroup: subGroups) {
			moveGroup(sess, destinationGroup, subGroup);
		}

		return getGroupById(sess, processedGroup.getId());

	}

	@Override
	public void addRelationMembers(PerunSession sess, Group resultGroup, List<Member> changedMembers, int sourceGroupId) throws AlreadyMemberException, WrongReferenceAttributeValueException, WrongAttributeValueException, GroupNotExistsException {
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
	public void removeRelationMembers(PerunSession sess, Group resultGroup, List<Member> changedMembers, int sourceGroupId) throws WrongReferenceAttributeValueException, NotGroupMemberException, WrongAttributeValueException, GroupNotExistsException {
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
	public Group createGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws WrongReferenceAttributeValueException, WrongAttributeValueException, GroupNotExistsException, GroupRelationAlreadyExists, GroupRelationNotAllowed {

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
					" and operand group " + operandGroup + " or they are in direct hierarchical structure.");
		}

		// check indirect relationships (for example "A" with "A:B:C" are in indirect relationship using "A:B")
		// looking for situation where result group is predecessor of operand group (by name) but not a parent of it (which is ok)
		if(!parentFlag && operandGroup.getName().startsWith(resultGroup.getName() + ":")) {
			throw new GroupRelationNotAllowed("There is an indirect relationship between result group " + resultGroup +
					" and operand group " + operandGroup);
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
	public void removeGroupUnion(PerunSession sess, Group resultGroup, Group operandGroup, boolean parentFlag) throws GroupRelationDoesNotExist, GroupRelationCannotBeRemoved, GroupNotExistsException {
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
	public List<Group> getGroupUnions(PerunSession session, Group group, boolean reverseDirection) {
		if (reverseDirection) {
			List<Group> resultGroups = groupsManagerImpl.getResultGroups(session, group.getId());
			// Sort
			Collections.sort(resultGroups);
			return resultGroups;
		} else {
			List<Group> operandGroups = groupsManagerImpl.getOperandGroups(session, group.getId());
			// Sort
			Collections.sort(operandGroups);
			return operandGroups;
		}
	}

	@Override
	public boolean isGroupInStructureSynchronizationTree(PerunSession session, Group group) {

		try {
			Attribute attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(session, group, GroupsManager.GROUPS_STRUCTURE_SYNCHRO_ENABLED_ATTRNAME);
			if (attrSynchronizeEnabled.getValue() != null && attrSynchronizeEnabled.getValue().equals(true)) {
				return true;
			} else {
				return isGroupSynchronizedFromExternallSource(session, group);
			}
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public boolean isGroupSynchronizedFromExternallSource(PerunSession session, Group group) {
		Attribute attrSynchronizeEnabled;
		Group parentGroup;

		if (group.getParentGroupId() == null) {
			return false;
		}
		try {
			parentGroup = getParentGroup(session, group);
		} catch (ParentGroupNotExistsException e) {
			throw new InternalErrorException(e);
		}

		try {
			attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(session, parentGroup, getPerunBl().getGroupsManager().GROUPS_STRUCTURE_SYNCHRO_ENABLED_ATTRNAME);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}

		if (attrSynchronizeEnabled.getValue() != null && attrSynchronizeEnabled.getValue().equals(true)) {
			return true;
		}
		return isGroupInStructureSynchronizationTree(session, parentGroup);
	}

	@Override
	public boolean hasGroupSynchronizedChild(PerunSession session, Group group) {
		Attribute attrSynchronizeEnabled;
		try {
			for(Group subGroup: getAllSubGroups(session, group)) {
				attrSynchronizeEnabled = getPerunBl().getAttributesManagerBl().getAttribute(session, subGroup, getPerunBl().getGroupsManager().GROUPS_STRUCTURE_SYNCHRO_ENABLED_ATTRNAME);
				if (attrSynchronizeEnabled.getValue() != null && attrSynchronizeEnabled.getValue().equals(true)) {
					return true;
				}
			}

		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}
		return false;
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
	private boolean checkGroupsCycle(PerunSession sess, int resultGroupId, int operandGroupId) {
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
	private void setSubGroupsNames(PerunSession sess, List<Group> subGroups, Group parentGroup) {
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
	private void processRelationsWhileMovingGroup(PerunSession sess, Group destinationGroup, Group movingGroup) throws WrongAttributeValueException, WrongReferenceAttributeValueException {
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
	public void expireMemberInGroup(PerunSession sess, Member member, Group group) {

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
	public void validateMemberInGroup(PerunSession sess, Member member, Group group) {

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
	public MemberGroupStatus getDirectMemberGroupStatus(PerunSession session, Member member, Group group) {
		return groupsManagerImpl.getDirectMemberGroupStatus(session, member, group);
	}

	@Override
	public MemberGroupStatus getTotalMemberGroupStatus(PerunSession session, Member member, Group group) {
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
	public void recalculateMemberGroupStatusRecursively(PerunSession sess, Member member, Group group) {

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
	public boolean canExtendMembershipInGroup(PerunSession sess, Member member, Group group) {
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
	public boolean canExtendMembershipInGroupWithReason(PerunSession sess, Member member, Group group) throws ExtendMembershipException {
		Attribute membershipExpirationRulesAttribute = getMembershipExpirationRulesAttribute(sess, group);
		if (membershipExpirationRulesAttribute == null) {
			return true;
		}
		extendMembershipInGroup(sess, member, group, false);
		return true;
	}

	private void extendMembershipInGroup(PerunSession sess, Member member, Group group, boolean setValue) throws ExtendMembershipException {
		Attribute membershipExpirationRulesAttribute = getMembershipExpirationRulesAttribute(sess, group);
		if (membershipExpirationRulesAttribute == null) {
			return;
		}
		LinkedHashMap<String, String> membershipExpirationRules = membershipExpirationRulesAttribute.valueAsMap();

		LocalDate localDate = LocalDate.now();

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
				localDate = extendForMonths(localDate, period, membershipExpirationRules, membershipExpirationAttribute, member, group);
			} else {
				// We will extend to particular date
				localDate = extendForStaticDate(localDate, period, membershipExpirationRules, membershipExpirationAttribute, member, group);
			}
		}

		if (setValue) {
			// Set new value of the membershipExpiration for the member
			membershipExpirationAttribute.setValue(localDate.toString());
			try {
				getPerunBl().getAttributesManagerBl().setAttribute(sess, member, group, membershipExpirationAttribute);
			} catch (WrongAttributeValueException e) {
				throw new InternalErrorException("Wrong value: " + membershipExpirationAttribute.getValue(),e);
			} catch (WrongReferenceAttributeValueException | WrongAttributeAssignmentException | MemberGroupMismatchException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	@Override
	public void extendMembershipInGroup(PerunSession sess, Member member, Group group) throws ExtendMembershipException {
		extendMembershipInGroup(sess, member, group, true);
	}

	/**
	 * Returns localDate extended to given date
	 *
	 * @param localDate localDate
	 * @param period period
	 * @param membershipExpirationRules rules used in check
	 * @param membershipExpirationAttribute attributes used to check
	 * @param member member that is checked
	 * @param group group that is checked
	 * @return localDate localDate extended to given date
	 * @throws InternalErrorException internal error
	 * @throws ExtendMembershipException when cannot extend
	 */
	private LocalDate extendForStaticDate(LocalDate localDate, String period, LinkedHashMap<String, String> membershipExpirationRules, Attribute membershipExpirationAttribute, Member member, Group group) throws ExtendMembershipException {
		// Parse date
		Pattern p = Pattern.compile("([0-9]+).([0-9]+).");
		Matcher m = p.matcher(period);

		if (!m.matches()) {
			throw new InternalErrorException("Wrong format of period in Group membershipExpirationRules attribute. Period: " + period);
		}

		localDate = Utils.getClosestExpirationFromStaticDate(m);

		// ***** GRACE PERIOD *****
		// Is there a grace period?
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName) != null) {
			String gracePeriod = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipGracePeriodKeyName);
			// If the extension is requested in period-gracePeriod then extend to next period

			// Get the value of the grace period
			p = Pattern.compile("([0-9]+)([dmy]?)");
			m = p.matcher(gracePeriod);
			if (m.matches()) {
				Pair<Integer, TemporalUnit> amountField = Utils.prepareGracePeriodDate(m);
				LocalDate gracePeriodDate = localDate.minus(amountField.getLeft(), amountField.getRight());

				// Check if we are in grace period
				if (gracePeriodDate.isBefore(LocalDate.now())) {
					// We are in grace period, so extend to the next period
					localDate = localDate.plusYears(1);
				}

				// If we do not need to set the attribute value, only check if the current member's expiration time is not in grace period
				if (membershipExpirationAttribute.getValue() != null) {
					LocalDate currentMemberExpirationDate = LocalDate.parse((String) membershipExpirationAttribute.getValue()).minus(amountField.getLeft(), amountField.getRight());

					// if today is before that time, user can extend his period
					if (currentMemberExpirationDate.isAfter(LocalDate.now())) {
						throw new ExtendMembershipException(ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD, (String) membershipExpirationAttribute.getValue(),
							"Member " + member + " cannot extend because we are outside grace period for GROUP id " + group.getId() + ".");
					}
				}
			}
		}
		return localDate;
	}

	/**
	 * Checks given attributes and returns localDate extended by given period
	 *
	 * @param localDate localDate
	 * @param period period
	 * @param membershipExpirationRules rules used in check
	 * @param membershipExpirationAttribute attributes used to check
	 * @param member member that is checked
	 * @param group group that is checked
	 * @return localDate localDate extended by period
	 * @throws InternalErrorException internal error
	 * @throws ExtendMembershipException when cannot extend
	 */
	private LocalDate extendForMonths(LocalDate localDate, String period, Map<String, String> membershipExpirationRules,
								 Attribute membershipExpirationAttribute, Member member, Group group) throws ExtendMembershipException {
		if (!isMemberInGracePeriod(membershipExpirationRules, (String) membershipExpirationAttribute.getValue())) {
			throw new ExtendMembershipException(ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD, (String) membershipExpirationAttribute.getValue(),
					"Member " + member + " cannot extend because we are outside grace period for Group id " + group.getId() + ".");
		}

		try {
			return Utils.extendDateByPeriod(localDate, period);
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
	private String getLoaPeriod(PerunSession sess, Member member, LinkedHashMap<String, String> membershipExpirationRules, Attribute membershipExpirationAttribute) throws ExtendMembershipException {
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
	 * @param membershipExpirationRules
	 * @param membershipExpirationAttribute
	 * @param member
	 * @throws ExtendMembershipException
	 * @throws InternalErrorException
	 */
	private void checkLoaForExpiration(PerunSession sess, LinkedHashMap<String, String> membershipExpirationRules,
									   Attribute membershipExpirationAttribute, Member member) throws ExtendMembershipException {

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
	private boolean isServiceUser(PerunSession sess, Member member) {
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
	private Attribute getMemberExpiration(PerunSession sess, Member member, Group group) {
		try {
			return getPerunBl().getAttributesManagerBl().getAttribute(sess, member, group,	A_MG_D_MEMBERSHIP_EXPIRATION);
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Attribute: " +A_MG_D_MEMBERSHIP_EXPIRATION +
					" must be defined in order to use membershipExpirationRules");
		} catch (WrongAttributeAssignmentException | MemberGroupMismatchException e) {
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
	private String getMemberLoa(PerunSession sess, Member member) {
		try {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			Attribute loa = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, A_U_V_LOA);
			return Integer.toString((Integer) loa.getValue());
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
	private Attribute getMembershipExpirationRulesAttribute(PerunSession sess, Group group) {
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
	private boolean isMemberInGracePeriod(Map<String, String> membershipExpirationRules, String membershipExpiration) {
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

		TemporalUnit gracePeriodTimeUnit;
		String dmyString = m.group(2);
		switch (dmyString) {
			case "d":
				gracePeriodTimeUnit = ChronoUnit.DAYS;
				break;
			case "m":
				gracePeriodTimeUnit = ChronoUnit.MONTHS;
				break;
			case "y":
				gracePeriodTimeUnit = ChronoUnit.YEARS;
				break;
			default:
				throw new InternalErrorException("Wrong format of gracePeriod in VO membershipExpirationRules attribute. gracePeriod: " + gracePeriod);
		}

		LocalDate beginOfGracePeriod = LocalDate.parse(membershipExpiration).minus(amount, gracePeriodTimeUnit);
		if (beginOfGracePeriod.isBefore(LocalDate.now())) {
			return true;
		}

		return false;

	}

	/**
	 * Return list of members with allowedStatus filtered from input array list membersToFilter.
	 *
	 * @param membersToFilter list of members to filter
	 * @param allowedStatus allowed status to filter by
	 * @return list of members with filtered status in group
	 * @throws InternalErrorException if allowed status is null
	 */
	private List<Member> filterMembersByStatusInGroup(List<Member> membersToFilter, MemberGroupStatus allowedStatus) {
		if (allowedStatus == null) throw new InternalErrorException("Allowed status can't be null.");
		List<Member> filteredMembers = new ArrayList<>();
		if (membersToFilter == null || membersToFilter.isEmpty()) return filteredMembers;
		for(Member member: membersToFilter) {
			if (allowedStatus.equals(member.getGroupStatus())) filteredMembers.add(member);
		}
		return filteredMembers;
	}

	/**
	 * Convert list of RichMembers to map where keys are UserExtSources and value is RichMember with such UserExtSource.
	 * This will help to find a RichMember who has specific UserExtSource.
	 *
	 * @param richMembers list of richMembers with userExtSources
	 * @return map of RichMembers mapped on UserExtSources
	 */
	private Map<UserExtSource, RichMember> createMappingStructure(List<RichMember> richMembers) {

		Map<UserExtSource, RichMember> mappingStructure = new HashMap<>();
		for (RichMember rm : richMembers) {
			for (UserExtSource ues : rm.getUserExtSources()) {
				mappingStructure.put(ues, rm);
			}
		}
		return mappingStructure;
	}

	@Override
	public List<Group> getGroupsWhereGroupIsAdmin(PerunSession perunSession, Group group) {
		return this.getGroupsManagerImpl().getGroupsWhereGroupIsAdmin(perunSession, group);
	}

	@Override
	public List<Vo> getVosWhereGroupIsAdmin(PerunSession perunSession, Group group) {
		return this.getGroupsManagerImpl().getVosWhereGroupIsAdmin(perunSession, group);
	}

	@Override
	public List<Facility> getFacilitiesWhereGroupIsAdmin(PerunSession perunSession, Group group) {
		return this.getGroupsManagerImpl().getFacilitiesWhereGroupIsAdmin(perunSession, group);
	}

	@Override
	public void reactivateMember(PerunSession sess, Member member, Group group) throws MemberNotExistsException {
		//only direct membership has own status and expiration, indirect membership get it's status and expiration from origin group
		if (!isDirectGroupMember(sess, group, member)) throw new MemberNotExistsException("Member does not belong to this group");

		validateMemberInGroup(sess, member, group);

		// Get current membershipExpiration date attribute
		Attribute membershipExpirationAttribute = getMemberExpiration(sess, member, group);
		// Set new value of the membershipExpiration for the member
		membershipExpirationAttribute.setValue(null);

		try {
			getPerunBl().getAttributesManagerBl().setAttribute(sess, member, group, membershipExpirationAttribute);
		} catch (WrongAttributeValueException e) {
			throw new InternalErrorException("Wrong value: " + membershipExpirationAttribute.getValue(),e);
		} catch (WrongReferenceAttributeValueException | WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		} catch (MemberGroupMismatchException e) {
			throw new ConsistencyErrorException(e);
		}

		try {
			extendMembershipInGroup(sess, member, group);
		} catch (ExtendMembershipException ex) {
			// This exception should not be thrown for null membershipExpiration attribute
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Group> getGroupsForAutoRegistration(PerunSession sess, Vo vo) {
		return this.getGroupsManagerImpl().getGroupsForAutoRegistration(sess, vo);
	}

	@Override
	public void deleteGroupsFromAutoRegistration(PerunSession sess, List<Group> groups) {
		for (Group group : groups) {
			this.getGroupsManagerImpl().deleteGroupFromAutoRegistration(sess, group);
		}
	}

	@Override
	public void addGroupsToAutoRegistration(PerunSession sess, List<Group> groups) throws GroupNotAllowedToAutoRegistrationException {
		for (Group group : groups) {
			if (group.getName().equals(VosManager.MEMBERS_GROUP)) {
				throw new GroupNotAllowedToAutoRegistrationException("Members group cannot be added to auto registration.", group);
			}

			try {
				Attribute syncEnabledAttr = getPerunBl().getAttributesManagerBl().getAttribute(sess, group, GroupsManager.GROUPSYNCHROENABLED_ATTRNAME);
				if ("true".equals(syncEnabledAttr.valueAsString()) || isGroupInStructureSynchronizationTree(sess, group)) {
					throw new GroupNotAllowedToAutoRegistrationException("Group with synchronization cannot be added to auto registration.", group);
				}
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
				// if attribute does not exist we can skip it
			}

			this.getGroupsManagerImpl().addGroupToAutoRegistration(sess, group);
		}
	}

	@Override
	public List<List<Group>> getIndirectMembershipPaths(PerunSession sess, Member member, Group group) throws MemberNotExistsException, GroupNotExistsException {
		if (!isGroupMember(sess, group, member))
			throw new MemberNotExistsException("Member does not belong to this group");

		return stepIntoGroup(sess, member, group);
	}

	/**
	 * Step into group, if it matches the source, start reconstructing the path, otherwise step into all
	 * subgroups where member is recognised and glue current group to path when returning. Included groups
	 * are cut off after first included group. Duplicates avoided.
	 */
	private List<List<Group>> stepIntoGroup(PerunSession sess, Member member, Group currentGroup) {
		List<List<Group>> pathsFromCurrentGroup = new ArrayList<>();

		// reached source group, create empty list to which current group will be added at the end of this method call
		if (isDirectGroupMember(sess, currentGroup, member)) {
			pathsFromCurrentGroup.add(new ArrayList<Group>());
		}

		// subgroups and included groups
		List<Group> relatedGroups = getGroupUnions(sess, currentGroup, false);

		// step into every group in which the member is recognised
		for (Group group : relatedGroups) {
			if (isGroupMember(sess, group, member)) {

				// cut paths via included groups
				if (group.getParentGroupId() == null ||group.getParentGroupId() != currentGroup.getId()) {
					List<Group> cutPath = new ArrayList<>();
					cutPath.add(group);
					pathsFromCurrentGroup.add(cutPath);
					continue;
				}

				List<List<Group>> subPaths = stepIntoGroup(sess, member, group);
				pathsFromCurrentGroup.addAll(subPaths);
			}
		}

		// glue current group to all paths
		List<List<Group>> resultPaths = new ArrayList<>();
		for (List<Group> path : pathsFromCurrentGroup) {
			path.add(0, currentGroup);
			resultPaths.add(path);
		}

		return resultPaths;
	}

	@Override
	public boolean isGroupForAutoRegistration(PerunSession sess, Group group) {
		return this.getGroupsManagerImpl().isGroupForAutoRegistration(sess, group);
	}

	@Override
	public EnrichedGroup convertToEnrichedGroup(PerunSession sess, Group group, List<String> attrNames) {
		List<Attribute> attributes;
		if (attrNames == null) {
			attributes = perunBl.getAttributesManagerBl().getAttributes(sess, group);
		} else {
			attributes = perunBl.getAttributesManagerBl().getAttributes(sess, group, attrNames);
		}
		return new EnrichedGroup(group, attributes);
	}
}
