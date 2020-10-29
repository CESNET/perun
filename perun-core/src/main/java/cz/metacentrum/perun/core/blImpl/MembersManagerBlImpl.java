package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberCreated;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberDeleted;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberDisabled;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberExpired;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberInvalidated;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberValidated;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberValidatedFailed;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.SponsoredMemberSet;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.SponsoredMemberUnset;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.SponsorshipEstablished;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.SponsorshipRemoved;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.SponsorshipValidityUpdated;
import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Sponsor;
import cz.metacentrum.perun.core.api.Sponsorship;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AlreadySponsorException;
import cz.metacentrum.perun.core.api.exceptions.AlreadySponsoredMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSponsoredException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleManagementRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SponsorshipDoesNotExistException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotInRoleException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.MembersManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.AbstractMembershipExpirationRulesModule;
import cz.metacentrum.perun.core.implApi.modules.pwdmgr.PasswordManagerModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_vo_attribute_def_def_membershipExpirationRules.VO_EXPIRATION_RULES_ATTR;
import static cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_vo_attribute_def_def_membershipExpirationRules.expireSponsoredMembers;

public class MembersManagerBlImpl implements MembersManagerBl {

	final static Logger log = LoggerFactory.getLogger(MembersManagerBlImpl.class);

	private static final String EXPIRATION = AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration";
	private static final String A_U_PREF_MAIL = AttributesManager.NS_USER_ATTR_DEF + ":preferredMail";

	private static final String NO_REPLY_EMAIL = "no-reply@muni.cz";

	private final MembersManagerImplApi membersManagerImpl;
	private PerunBl perunBl;

	/**
	 * Constructor.
	 *
	 */
	public MembersManagerBlImpl(MembersManagerImplApi membersManagerImpl) {
		this.membersManagerImpl = membersManagerImpl;
	}

	@Override
	public void deleteMember(PerunSession sess, Member member) throws MemberAlreadyRemovedException {
		Vo vo = this.getMemberVo(sess, member);

		// Remove member from all groups
		List<Group> memberGroups = getPerunBl().getGroupsManagerBl().getMemberDirectGroups(sess, member);
		for (Group group: memberGroups) {
			// Member must be removed from the members group using separate method
			if(group.getName().equals(VosManager.MEMBERS_GROUP)) continue;

			try {
				getPerunBl().getGroupsManagerBl().removeMember(sess, group, member);
			} catch (NotGroupMemberException e) {
				throw new ConsistencyErrorException("getMemberGroups return group where the member is not member", e);
			} catch (GroupNotExistsException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		// Remove member from the VO members group
		try {
			Group g = getPerunBl().getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
			try {
				getPerunBl().getGroupsManagerBl().removeMemberFromMembersOrAdministratorsGroup(sess, g, member);
			} catch (NotGroupMemberException e) {
				throw new ConsistencyErrorException("Member is not in the \"members\" group." + member + "  " + g, e);
			} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
				throw new InternalErrorException(e);
			}
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException(e);
		}

		// Remove member's  attributes (namespaces: member and resource-member)
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, member);
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
			for(Resource resource : resources) {
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, member, resource);
			}
		} catch(AttributeValueException ex) {
			throw new ConsistencyErrorException("Member is removed from all groups. There are no required attribute for this member. Member's attributes can be removed without problem.", ex);
		} catch (MemberResourceMismatchException ex) {
			throw new InternalErrorException(ex);
		}

		removeAllMemberBans(sess, member);

		//Remove possible links to member's sponsors
		membersManagerImpl.deleteSponsorLinks(sess, member);

		// Remove member from the DB
		getMembersManagerImpl().deleteMember(sess, member);
		getPerunBl().getAuditer().log(sess, new MemberDeleted(member));
	}

	@Override
	public void deleteMembers(PerunSession sess, List<Member> members) throws MemberAlreadyRemovedException {
		Collections.sort(members);

		for (Member member : members) {
			deleteMember(sess, member);
		}
	}

	@Override
	public void deleteAllMembers(PerunSession sess, Vo vo) throws MemberAlreadyRemovedException {
		for (Member m: this.getMembers(sess, vo)) {
			this.deleteMember(sess, m);
		}
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, User user) throws AlreadyMemberException, ExtendMembershipException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		return this.createMember(sess, vo, user, null);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws AlreadyMemberException, ExtendMembershipException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		try {
			Member member = getMemberByUser(sess, vo, user);
			throw new AlreadyMemberException(member);
		} catch(MemberNotExistsException IGNORE) {
		}
		Member member = getMembersManagerImpl().createMember(sess, vo, user);
		getPerunBl().getAuditer().log(sess, new MemberCreated(member));

		// Set the initial membershipExpiration

		// Get user LOA
		String memberLoa = null;
		try {
			Attribute loa = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_VIRT + ":loa");
			memberLoa = Integer.toString((Integer) loa.getValue());;
		} catch (AttributeNotExistsException e) {
			// User has no loa defined - if required by VO, it will be stopped in checking method later
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// check if user can be member - service members are not checked for LoA
		this.canBeMemberInternal(sess, vo, user, memberLoa, true);

		// Set initial membership expiration
		this.extendMembership(sess, member);

		insertToMemberGroup(sess, member, vo);

		// add member also to all groups in list
		if(groups != null && !groups.isEmpty()) {
			for(Group group: groups) {
				try {
					perunBl.getGroupsManagerBl().addMember(sess, group, member);
				} catch (GroupNotExistsException e) {
					throw new ConsistencyErrorException(e);
				}
			}
		}

		// Set default membership expiration

		return member;
	}

	@Override
	public Member createServiceMember(PerunSession sess, Vo vo, Candidate candidate, List<User> owners) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		return this.createServiceMember(sess, vo, candidate, owners, null);
	}

	@Override
	public Member createServiceMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		candidate.setFirstName("(Service)");

		//Set organization only if user in sessione exists (in tests there is no user in session)
		if(sess.getPerunPrincipal().getUser() != null) {
			String userOrganization = AttributesManager.NS_USER_ATTR_DEF + ":organization";
			String memberOrganization = AttributesManager.NS_MEMBER_ATTR_DEF + ":organization";

			Map<String, String> candidateAttributes =  new HashMap<>();
			if(candidate.getAttributes() != null) candidateAttributes.putAll(candidate.getAttributes());

			if(candidateAttributes.get(memberOrganization) == null) {
				Attribute actorUserOrganization;
				String actorUserOrganizationValue;
				try {
					actorUserOrganization = perunBl.getAttributesManagerBl().getAttribute(sess, sess.getPerunPrincipal().getUser(), userOrganization);
					actorUserOrganizationValue = (String) actorUserOrganization.getValue();
				} catch (WrongAttributeAssignmentException | AttributeNotExistsException ex) {
					throw new InternalErrorException(ex);
				}

				if(actorUserOrganizationValue != null) {
					candidateAttributes.put(memberOrganization, actorUserOrganizationValue);
					candidate.setAttributes(candidateAttributes);
				}
			}
		}

		//create member for service user from candidate
		Member member = createMember(sess, vo, SpecificUserType.SERVICE, candidate, groups, null);

		//set specific user owners or sponsors
		User specificUser = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
		for(User u: specificUserOwners) {
			try {
				getPerunBl().getUsersManagerBl().addSpecificUserOwner(sess, u, specificUser);
			} catch (RelationExistsException ex) {
				throw new InternalErrorException(ex);
			}
		}
		return member;
	}

	@Override
	public Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		return this.createMemberSync(sess, vo, candidate, null);
	}

	@Override
	public Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups, List<String> overwriteUserAttributes) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		Member member = createMember(sess, vo, SpecificUserType.NORMAL, candidate, groups, overwriteUserAttributes);

		//Validate synchronously
		try {
			member = getPerunBl().getMembersManagerBl().validateMember(sess, member);
		} catch (AttributeValueException ex) {
			log.info("Member can't be validated. He stays in invalid state. Cause: " + ex);
		}

		return member;
	}

	@Override
	public Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		return this.createMemberSync(sess, vo, candidate, groups, null);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		return createMember(sess, vo, candidate, null);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		return createMember(sess, vo, SpecificUserType.NORMAL, candidate, groups, null);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, SpecificUserType specificUserType, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
			return this.createMember(sess, vo, specificUserType, candidate, null, new ArrayList<>());
	}

	//MAIN METHOD
	@Override
	public Member createMember(PerunSession sess, Vo vo, SpecificUserType specificUserType, Candidate candidate, List<Group> groups, List<String> overwriteUserAttributes) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		log.debug("Creating member for VO {} from candidate {}", vo, candidate);
		// Get the user
		User user = null;
		if (candidate.getUserExtSources() != null) {
			for (UserExtSource ues: candidate.getUserExtSources()) {
				// Check if the extSource exists
				ExtSource tmpExtSource = getPerunBl().getExtSourcesManagerBl().checkOrCreateExtSource(sess, ues.getExtSource().getName(),
						ues.getExtSource().getType());
				// Set the extSource ID
				ues.getExtSource().setId(tmpExtSource.getId());
				try {
					// Try to find the user by userExtSource
					user = getPerunBl().getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, ues.getExtSource().getName(), ues.getLogin());
				} catch (UserExtSourceNotExistsException e) {
					// This is OK, non-existent userExtSource will be assigned later
				} catch (UserNotExistsException | ExtSourceNotExistsException e) {
					// Ignore, we are only checking if the user exists
				}
			}
		}

		// If user hasn't been found, then create him
		if (user == null) {
			user = new User();
			user.setFirstName(candidate.getFirstName());
			user.setLastName(candidate.getLastName());
			user.setMiddleName(candidate.getMiddleName());
			user.setTitleAfter(candidate.getTitleAfter());
			user.setTitleBefore(candidate.getTitleBefore());
			if(specificUserType.equals(SpecificUserType.SERVICE)) user.setServiceUser(true);
			if(specificUserType.equals(SpecificUserType.SPONSORED)) user.setSponsoredUser(true);
			// Store the user, this must be done in separate transaction
			user = getPerunBl().getUsersManagerBl().createUser(sess, user);

			log.debug("createMember: new user: {}", user);
		}

		// Assign missing userExtSource and update LoA
		if (candidate.getUserExtSources() != null) {
			for (UserExtSource userExtSource : candidate.getUserExtSources()) {
				try {
					UserExtSource currentUserExtSource = getPerunBl().getUsersManagerBl().getUserExtSourceByExtLogin(sess, userExtSource.getExtSource(), userExtSource.getLogin());
					// Update LoA
					currentUserExtSource.setLoa(userExtSource.getLoa());
					getPerunBl().getUsersManagerBl().updateUserExtSource(sess, currentUserExtSource);
				} catch (UserExtSourceNotExistsException e) {
					// Create userExtSource
					try {
						getPerunBl().getUsersManagerBl().addUserExtSource(sess, user, userExtSource);
					} catch (UserExtSourceExistsException e1) {
						throw new ConsistencyErrorException("Adding userExtSource which already exists: " + userExtSource);
					}
				} catch (UserExtSourceExistsException e1) {
					throw new ConsistencyErrorException("Updating login of userExtSource to value which already exists: " + userExtSource);
				}
			}
		}

		try {
			Member member = getMemberByUser(sess, vo, user);
			throw new AlreadyMemberException(member);
		} catch(MemberNotExistsException IGNORE) {
		}

		// Create the member
		Member member = getMembersManagerImpl().createMember(sess, vo, user);
		getPerunBl().getAuditer().log(sess,  new MemberCreated(member));
		// Create the member's attributes
		List<Attribute> membersAttributes = new ArrayList<>();
		List<Attribute> usersAttributesToMerge = new ArrayList<>();
		List<Attribute> usersAttributesToModify = new ArrayList<>();
		if (candidate.getAttributes() != null) {
			for (String attributeName: candidate.getAttributes().keySet()) {
				AttributeDefinition attributeDefinition;
				try {
					attributeDefinition = getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attributeName);
				} catch(AttributeNotExistsException ex) {
					throw new InternalErrorException(ex);
				}
				Attribute attribute = new Attribute(attributeDefinition);
				attribute.setValue(getPerunBl().getAttributesManagerBl().stringToAttributeValue(candidate.getAttributes().get(attributeName), attribute.getType()));
				if (getPerunBl().getAttributesManagerBl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR_DEF) ||
						getPerunBl().getAttributesManagerBl().isFromNamespace(sess, attribute, AttributesManager.NS_MEMBER_ATTR_OPT)) {
					// This is member's attribute
					membersAttributes.add(attribute);
				} else if (getPerunBl().getAttributesManagerBl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR_DEF) ||
						getPerunBl().getAttributesManagerBl().isFromNamespace(sess, attribute, AttributesManager.NS_USER_ATTR_OPT)) {
					if(overwriteUserAttributes != null && !overwriteUserAttributes.isEmpty() && overwriteUserAttributes.contains(attribute.getName())) {
						usersAttributesToModify.add(attribute);
					} else {
						usersAttributesToMerge.add(attribute);
					}
				}
			}
		}

		// Store the attributes
		try {
			// If empty, skip setting or merging empty arrays of attributes at all
			if(!membersAttributes.isEmpty()) getPerunBl().getAttributesManagerBl().setAttributes(sess, member, membersAttributes);
			if(!usersAttributesToMerge.isEmpty()) getPerunBl().getAttributesManagerBl().mergeAttributesValues(sess, user, usersAttributesToMerge);
			if(!usersAttributesToModify.isEmpty()) getPerunBl().getAttributesManagerBl().setAttributes(sess, user, usersAttributesToModify);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// Set the initial membershipExpiration

		// Get user LOA
		String memberLoa = null;
		try {
			Attribute loa = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_VIRT + ":loa");
			memberLoa = Integer.toString((Integer) loa.getValue());
		} catch (AttributeNotExistsException e) {
			// user has no loa defined - if required by VO, it will be stopped in checking method later
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// Check if user can be member
		this.canBeMemberInternal(sess, vo, user, memberLoa, true);

		// set initial membership expiration
		this.extendMembership(sess, member);

		insertToMemberGroup(sess, member, vo);

		// Add member also to all groups in list
		if(groups != null && !groups.isEmpty()) {
			for(Group group: groups) {
				try {
					perunBl.getGroupsManagerBl().addMember(sess, group, member);
				} catch (GroupNotExistsException e) {
					throw new ConsistencyErrorException(e);
				}
			}
		}

		return member;
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int loa, String login, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		return this.createMember(sess, vo, extSourceName, extSourceType, loa, login, candidate, null);
	}

	/*
	 * This method with support of LoA finally has to call this.createMember(PerunSession sess, Vo vo, UserExtSource userExtSource)
	 * @see cz.metacentrum.perun.core.api.MembersManager#createMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Vo, java.lang.String, java.lang.String, java.lang.String, cz.metacentrum.perun.core.api.Candidate)
	 */
	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int loa, String login, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {

		// Create ExtSource object
		ExtSource extSource = new ExtSource();
		extSource.setName(extSourceName);
		extSource.setType(extSourceType);

		// Create UserExtSource object
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setLogin(login);
		userExtSource.setExtSource(extSource);
		userExtSource.setLoa(loa);

		// Set all above data to the candidate's userExtSource
		candidate.setUserExtSource(userExtSource);

		return this.createMember(sess, vo, candidate, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		return this.createMember(sess, vo, extSourceName, extSourceType, login, candidate, null);
	}

	/*
	 * This method finally has to call this.createMember(PerunSession sess, Vo vo, UserExtSource userExtSource)
	 * @see cz.metacentrum.perun.core.api.MembersManager#createMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Vo, java.lang.String, java.lang.String, java.lang.String, cz.metacentrum.perun.core.api.Candidate)
	 */
	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {

		// Create ExtSource object
		ExtSource extSource = new ExtSource();
		extSource.setName(extSourceName);
		extSource.setType(extSourceType);

		// Create UserExtSource object
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setLogin(login);
		userExtSource.setExtSource(extSource);

		// Set all above data to the candidate's userExtSource
		candidate.setUserExtSource(userExtSource);

		return this.createMember(sess, vo, candidate, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException {
		//First of all get candidate from extSource directly
		Candidate candidate = null;
		try {
			if (extSource instanceof ExtSourceApi) {
				//get first subject, then create candidate
				Map<String, String> subject = ((ExtSourceSimpleApi) extSource).getSubjectByLogin(login);
				candidate = (getPerunBl().getExtSourcesManagerBl().getCandidate(sess, subject, extSource, login));
			} else if (extSource instanceof ExtSourceSimpleApi) {
				// get candidates from external source by login
				candidate = (getPerunBl().getExtSourcesManagerBl().getCandidate(sess, extSource, login));
			}
		} catch (CandidateNotExistsException | SubjectNotExistsException ex) {
			throw new InternalErrorException("Can't find candidate for login " + login + " in extSource " + extSource, ex);
		} catch (ExtSourceUnsupportedOperationException ex) {
			throw new InternalErrorException("Some operation is not allowed for extSource " + extSource, ex);
		}

		return this.createMember(sess, vo, candidate, groups);
	}

	@Override
	public Member updateMember(PerunSession sess, Member member) throws WrongAttributeValueException, WrongReferenceAttributeValueException {
		Member storedMember;
		try {
			storedMember = getMemberById(sess, member.getId());
		} catch(MemberNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		if(storedMember.getUserId() != member.getUserId()) throw new InternalErrorException("Can't change userId in object member");
		if(!storedMember.getStatus().equals(member.getStatus())) {
			try {
				member = setStatus(sess, storedMember, member.getStatus());
			} catch(MemberNotValidYetException ex) {
				throw new WrongAttributeValueException(ex);
			}
		}
		return member;
	}

	@Override
	public Member getMemberByUserExtSource(PerunSession sess, Vo vo, UserExtSource uea) throws MemberNotExistsException {
		return getMembersManagerImpl().getMemberByUserExtSource(sess, vo, uea);
	}

	@Override
	public Member getMemberByUserExtSources(PerunSession sess, Vo vo, List<UserExtSource> ueas) throws MemberNotExistsException {
		if (ueas == null) {
			throw new InternalErrorException("Given userExtSources are null.");
		}
		if (ueas.isEmpty()) {
			throw new InternalErrorException("Given userExtSources are empty.");
		}

		Set<Member> foundMembers = new HashSet<>();

		for (UserExtSource ues: ueas) {
			try {
				foundMembers.add(getMembersManagerImpl().getMemberByUserExtSource(sess, vo, ues));
			} catch (MemberNotExistsException e) {
				// ignore
			}
		}

		if (foundMembers.isEmpty()) {
			throw new MemberNotExistsException("Member with userExtSources " + ueas + " doesn't exists.");
		}

		if (foundMembers.size() > 1) {
			throw new InternalErrorException("Given userExtSources do not belong to the same member.");
		} else {
			return (Member)foundMembers.toArray()[0];
		}
	}

	@Override
	public Member getMemberById(PerunSession sess, int id) throws MemberNotExistsException {
		return getMembersManagerImpl().getMemberById(sess, id);
	}

	@Override
	public Member getMemberByUser(PerunSession sess, Vo vo, User user) throws MemberNotExistsException {
		return getMembersManagerImpl().getMemberByUserId(sess, vo, user.getId());
	}

	@Override
	public Member getMemberByUserId(PerunSession sess, Vo vo, int userId) throws MemberNotExistsException {
		return getMembersManagerImpl().getMemberByUserId(sess, vo, userId);
	}

	@Override
	public List<Member> getMembersByUser(PerunSession sess, User user) {
		return getMembersManagerImpl().getMembersByUser(sess, user);
	}

	@Override
	public List<Member> getMembersByUserWithStatus(PerunSession sess, User user, Status status) {
		return getMembersManagerImpl().getMembersByUserWithStatus(sess, user, status);
	}

	@Override
	public List<Member> getMembers(PerunSession sess, Vo vo) {
		try {
			Group g = getPerunBl().getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
			return getPerunBl().getGroupsManagerBl().getGroupMembers(sess, g);
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getMembers(PerunSession sess, Vo vo, Status status) {
		try {
			Group g = getPerunBl().getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
			return getPerunBl().getGroupsManagerBl().getGroupMembers(sess, g, status);
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public RichMember getRichMember(PerunSession sess, Member member) {
		List<Member> members = new ArrayList<>();
		members.add(member);
		return this.convertMembersToRichMembers(sess, members).get(0);
	}

	@Override
	public RichMember getRichMemberWithAttributes(PerunSession sess, Member member) {
		List<Member> members = new ArrayList<>();
		members.add(member);
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<RichMember> richMembersWithAttributes =  this.convertMembersToRichMembersWithAttributes(sess, richMembers);
		return richMembersWithAttributes.get(0);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef) {
		List<Member> members = new ArrayList<>(perunBl.getMembersManagerBl().getMembers(sess, vo));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		return this.convertMembersToRichMembersWithAttributes(sess, richMembers, attrsDef);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws AttributeNotExistsException {
		List<Member> members = new ArrayList<>(perunBl.getMembersManagerBl().getMembers(sess, vo));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<AttributeDefinition> attrsDef = new ArrayList<>();
		for(String atrrName: attrsNames) {
			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, atrrName);
			attrsDef.add(attrDef);
		}
		return this.convertMembersToRichMembersWithAttributes(sess, richMembers, attrsDef);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws AttributeNotExistsException {
		if(attrsNames == null || attrsNames.isEmpty()) {
			return this.getRichMembersWithAttributes(sess, vo);
		} else {
			return this.getRichMembersWithAttributesByNames(sess, vo, attrsNames);
		}
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws AttributeNotExistsException {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.getCompleteRichMembers(sess, vo, attrsNames), allowedStatuses);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, Resource resource, List<String> attrsNames, List<String> allowedStatuses) throws AttributeNotExistsException, GroupResourceMismatchException {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.getRichMembersWithAttributesByNames(sess, group, resource, attrsNames), allowedStatuses);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws AttributeNotExistsException, ParentGroupNotExistsException {
		if(lookingInParentGroup) group = getPerunBl().getGroupsManagerBl().getParentGroup(sess, group);

		if(attrsNames == null || attrsNames.isEmpty()) {
			return this.convertMembersToRichMembersWithAttributes(sess, getRichMembers(sess, group));
		} else {
			return this.getRichMembersWithAttributesByNames(sess, group, attrsNames);
		}
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws AttributeNotExistsException, ParentGroupNotExistsException {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.getCompleteRichMembers(sess, group, attrsNames, lookingInParentGroup), allowedStatuses);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString, boolean onlySponsored) {
		return this.findRichMembersWithAttributesInVo(sess, vo, searchString, attrsNames, onlySponsored);
	}


	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, String searchString) {
		return this.findRichMembersWithAttributes(sess, searchString, attrsNames);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.findCompleteRichMembers(sess, vo, attrsNames, searchString, false), allowedStatuses);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.findCompleteRichMembers(sess, attrsNames, searchString), allowedStatuses);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws ParentGroupNotExistsException {
		if(lookingInParentGroup) group = getPerunBl().getGroupsManagerBl().getParentGroup(sess, group);
		return this.findRichMembersWithAttributesInGroup(sess, group, searchString, attrsNames);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws ParentGroupNotExistsException {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.findCompleteRichMembers(sess, group, attrsNames, searchString, lookingInParentGroup), allowedStatuses);
	}

	/**
	 * Return list of RichMembers with allowed statuses contains in list of allowedStatuses.
	 * If allowedStatuses is empty or null, get richMembers with all statuses.
	 *
	 * @param sess
	 * @param richMembers
	 * @param allowedStatuses
	 * @return list of allowed richMembers
	 */
	private List<RichMember> getOnlyRichMembersWithAllowedStatuses(PerunSession sess, List<RichMember> richMembers, List<String> allowedStatuses) {
		List<RichMember> allowedRichMembers = new ArrayList<>();
		if(richMembers == null || richMembers.isEmpty()) return allowedRichMembers;
		if(allowedStatuses == null || allowedStatuses.isEmpty()) return richMembers;

		//Covert statuses to objects Status
		List<Status> statuses = new ArrayList<>();
		for(String status: allowedStatuses) {
			statuses.add(Status.valueOf(status));
		}

		for(RichMember rm: richMembers) {
			if(statuses.contains(rm.getStatus())) allowedRichMembers.add(rm);
		}

		return allowedRichMembers;
	}

	@Override
	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, Resource resource, List<String> attrsNames) throws AttributeNotExistsException, GroupResourceMismatchException {
		getPerunBl().getAttributesManagerBl().checkGroupIsFromTheSameVoLikeResource(sess, group, resource);
		List<Member> members = new ArrayList<>(perunBl.getGroupsManagerBl().getGroupMembers(sess, group));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<AttributeDefinition> attrsDef = new ArrayList<>();
		for(String atrrName: attrsNames) {
			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, atrrName);
			attrsDef.add(attrDef);
		}
		List<RichMember> richMembersWithAttributes;
		try {
			richMembersWithAttributes = this.convertMembersToRichMembersWithAttributes(sess, group, resource, richMembers, attrsDef);
		} catch (MemberResourceMismatchException | MemberGroupMismatchException ex) {
			throw new ConsistencyErrorException(ex);
		}
		return richMembersWithAttributes;
	}

	@Override
	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws AttributeNotExistsException {
		List<Member> members = new ArrayList<>(perunBl.getGroupsManagerBl().getGroupMembers(sess, group));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<AttributeDefinition> attrsDef = new ArrayList<>();
		for(String atrrName: attrsNames) {
			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, atrrName);
			attrsDef.add(attrDef);
		}
		List<RichMember> richMembersWithAttributes = null;
		try {
			richMembersWithAttributes = this.convertMembersToRichMembersWithAttributes(sess, group, richMembers, attrsDef);
		} catch (MemberGroupMismatchException e) {
			throw new InternalErrorException(e);
		}
		return richMembersWithAttributes;
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef) {
		List<Member> members = new ArrayList<>(perunBl.getGroupsManagerBl().getGroupMembers(sess, group));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<RichMember> richMembersWithAttributes = null;
		try {
			richMembersWithAttributes = this.convertMembersToRichMembersWithAttributes(sess, group, richMembers, attrsDef);
		} catch (MemberGroupMismatchException e) {
			throw new InternalErrorException(e);
		}
		return richMembersWithAttributes;
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Vo vo) {
		List<Member> members = this.getMembers(sess, vo);
		return this.convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Group group) {
		List<Member> members = new ArrayList<>(perunBl.getGroupsManagerBl().getGroupMembers(sess, group));
		return this.convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status) {
		List<Member> members = this.getMembers(sess, vo, status);
		return this.convertMembersToRichMembers(sess, members);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo) {
		List<RichMember> richMembers = this.getRichMembers(sess, vo);
		return this.convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status) {
		List<RichMember> richMembers = this.getRichMembers(sess, vo, status);
		return this.convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group) {
		List<RichMember> richMembers = this.getRichMembers(sess, group);
		return getOnlyRichMembersWithAllowedStatuses(sess, this.convertMembersToRichMembersWithAttributes(sess, richMembers), allowedStatuses);
	}

	/**
	 * Converts members to rich members.
	 * Rich member object contains user, member, userExtSources, userAttributes, memberAttributes.
	 * The method returns list of rich members with user and userExtSources filled. UserAttributes and memberAttributes are set to null.
	 *
	 * @param sess
	 * @param members
	 * @return list of rich members, empty list if empty list of members is passed
	 * @throws InternalErrorException
	 */
	@Override
	public List<RichMember> convertMembersToRichMembers(PerunSession sess, List<Member> members) {
		List<RichMember> richMembers = new ArrayList<>();

		for (Member member: members) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

			RichMember richMember = new RichMember(user, member, userExtSources);
			richMembers.add(richMember);
		}

		return richMembers;
	}

	/**
	 * Adds userAttributes and memberAttributes to rich members.
	 * The method returns list of rich members with userAttributes and memberAttributes filled.
	 *
	 * @param sess
	 * @param richMembers
	 * @return list of rich members with userAttributes and memberAttributes filled
	 * @throws InternalErrorException
	 */
	@Override
	public List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers) {
		for (RichMember richMember: richMembers) {
			List<Attribute> userAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember.getUser());
			List<Attribute> memberAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember);

			richMember.setUserAttributes(userAttributes);
			richMember.setMemberAttributes(memberAttributes);
		}

		return richMembers;
	}

	/**
	 * Adds userAttributes and memberAttributes to rich members.
	 * Specifically adds attributes that are associated with the members and the resource. Attributes are also limited by the list of attributes definitions.
	 * Adds member and member-resource attributes to memberAttributes and user and user-facility attributes to userAttributes.
	 * The method returns list of rich members with userAttributes and memberAttributes filled.
	 *
	 * @param sess
	 * @param richMembers
	 * @param resource
	 * @param attrsDef
	 * @return list of rich members with userAttributes and memberAttributes filled
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	@Override
	public List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers, Resource resource, List<AttributeDefinition> attrsDef) throws MemberResourceMismatchException {
		List<String> attrNames = new ArrayList<>();
		for(AttributeDefinition attributeDefinition: attrsDef) {
			attrNames.add(attributeDefinition.getName());
		}

		for (RichMember richMember: richMembers) {
			List<Attribute> userAttributes = new ArrayList<>();
			List<Attribute> memberAttributes = new ArrayList<>();

			List<Attribute> attributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember, resource, attrNames, true);

			for(Attribute attribute: attributes) {
				if(attribute.getName().startsWith(AttributesManager.NS_USER_ATTR)) userAttributes.add(attribute);
				else if(attribute.getName().startsWith(AttributesManager.NS_USER_FACILITY_ATTR)) userAttributes.add(attribute);
				else if(attribute.getName().startsWith(AttributesManager.NS_MEMBER_ATTR)) memberAttributes.add(attribute);
				else if(attribute.getName().startsWith(AttributesManager.NS_MEMBER_RESOURCE_ATTR)) memberAttributes.add(attribute);
				else {
					throw new InternalErrorException(attribute + " is not from user or member namespace (member-resource, user-facility included)!");
				}
			}

			richMember.setUserAttributes(userAttributes);
			richMember.setMemberAttributes(memberAttributes);
		}

		return richMembers;
	}

	/**
	 * Adds userAttributes and memberAttributes to rich members.
	 * Specifically adds attributes that are associated with the members, the group and the resource. Attributes are also limited by the list of attributes definitions.
	 * Adds member, member-group and member-resource attributes to memberAttributes and user and user-facility attributes to userAttributes.
	 * The method returns list of rich members with userAttributes and memberAttributes filled.
	 *
	 * @param sess
	 * @param group
	 * @param resource
	 * @param richMembers
	 * @param attrsDef
	 * @return list of rich members with userAttributes and memberAttributes filled
	 * @throws InternalErrorException
	 * @throws GroupResourceMismatchException
	 * @throws MemberResourceMismatchException
	 */
	public List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, Group group, Resource resource, List<RichMember> richMembers, List<AttributeDefinition> attrsDef) throws MemberResourceMismatchException, GroupResourceMismatchException, MemberGroupMismatchException {
		List<String> attrNames = new ArrayList<>();
		for(AttributeDefinition attributeDefinition: attrsDef) {
			attrNames.add(attributeDefinition.getName());
		}

		for (RichMember richMember: richMembers) {
			List<Attribute> userAttributes = new ArrayList<>();
			List<Attribute> memberAttributes = new ArrayList<>();

			List<Attribute> attributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, group, richMember, resource, attrNames, true);

			for(Attribute attribute: attributes) {
				if(attribute.getName().startsWith(AttributesManager.NS_USER_ATTR)) userAttributes.add(attribute);
				else if(attribute.getName().startsWith(AttributesManager.NS_USER_FACILITY_ATTR)) userAttributes.add(attribute);
				else if(attribute.getName().startsWith(AttributesManager.NS_MEMBER_ATTR)) memberAttributes.add(attribute);
				else if(attribute.getName().startsWith(AttributesManager.NS_MEMBER_RESOURCE_ATTR)) memberAttributes.add(attribute);
				else if(attribute.getName().startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) memberAttributes.add(attribute);
				else {
					throw new InternalErrorException(attribute + " is not from user or member namespace (member-resource, user-facility, member-group included)!");
				}
			}

			richMember.setUserAttributes(userAttributes);
			richMember.setMemberAttributes(memberAttributes);
		}

		return richMembers;
	}

	/**
	 * Adds userAttributes and memberAttributes to rich members.
	 * Attributes are limited by the list of attributes definitions.
	 * The method returns list of rich members with userAttributes and memberAttributes filled.
	 *
	 * @param sess
	 * @param richMembers
	 * @param attrsDef
	 * @return list of rich members with userAttributes and memberAttributes filled
	 * @throws InternalErrorException
	 */
	@Override
	public List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers, List<AttributeDefinition> attrsDef) {
		List<AttributeDefinition> usersAttributesDef = new ArrayList<>();
		List<AttributeDefinition> membersAttributesDef = new ArrayList<>();

		for(AttributeDefinition attrd: attrsDef) {
			if(attrd.getName().startsWith(AttributesManager.NS_USER_ATTR)) usersAttributesDef.add(attrd);
			else if(attrd.getName().startsWith(AttributesManager.NS_MEMBER_ATTR)) membersAttributesDef.add(attrd);
		}

		for (RichMember richMember: richMembers) {

			List<String> userAttrNames = new ArrayList<>();
			for(AttributeDefinition ad: usersAttributesDef) {
				userAttrNames.add(ad.getName());
			}
			List<Attribute> userAttributes = new ArrayList<>(getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember.getUser(), userAttrNames));

			List<String> memberAttrNames = new ArrayList<>();
			for(AttributeDefinition ad: membersAttributesDef) {
				memberAttrNames.add(ad.getName());
			}
			List<Attribute> memberAttributes = new ArrayList<>(getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember, memberAttrNames));

			richMember.setUserAttributes(userAttributes);
			richMember.setMemberAttributes(memberAttributes);
		}

		return richMembers;
	}

	@Override
	public Sponsor convertUserToSponsor(PerunSession sess, User user, Member sponsoredMember) {
		Sponsor sponsor = new Sponsor(user);
		Sponsorship sponsorship;
		try {
			sponsorship = perunBl.getMembersManagerBl().getSponsorship(sess, sponsoredMember, user);
		} catch (SponsorshipDoesNotExistException e) {
			throw new InternalErrorException(e);
		}
		sponsor.setActive(sponsorship.isActive());
		sponsor.setValidityTo(sponsorship.getValidityTo());
		if (user instanceof RichUser) {
			sponsor.setUserAttributes(((RichUser) user).getUserAttributes());
			sponsor.setUserExtSources(((RichUser) user).getUserExtSources());
		}
		return sponsor;
	}

	/**
	 * Adds userAttributes and memberAttributes to rich members.
	 * Specifically adds attributes that are associated with the members and the group. Attributes are also limited by the list of attributes definitions.
	 * Adds member and member-group attributes to memberAttributes and user attributes to userAttributes.
	 * The method returns list of rich members with userAttributes and memberAttributes filled.
	 *
	 * @param sess
	 * @param group
	 * @param richMembers
	 * @param attrsDef
	 * @return list of rich members with userAttributes and memberAttributes filled
	 * @throws InternalErrorException
	 */
	public List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, Group group, List<RichMember> richMembers, List<AttributeDefinition> attrsDef) throws MemberGroupMismatchException {
		List<AttributeDefinition> usersAttributesDef = new ArrayList<>();
		List<AttributeDefinition> membersAttributesDef = new ArrayList<>();
		List<AttributeDefinition> memberGroupAttributesDef = new ArrayList<>();

		for(AttributeDefinition attrd: attrsDef) {
			if(attrd.getName().startsWith(AttributesManager.NS_USER_ATTR)) usersAttributesDef.add(attrd);
			else if(attrd.getName().startsWith(AttributesManager.NS_MEMBER_ATTR)) membersAttributesDef.add(attrd);
			else if(attrd.getName().startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) memberGroupAttributesDef.add(attrd);
		}

		for (RichMember richMember: richMembers) {

			List<String> userAttrNames = new ArrayList<>();
			for(AttributeDefinition ad: usersAttributesDef) {
				userAttrNames.add(ad.getName());
			}
			List<Attribute> userAttributes = new ArrayList<>(getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember.getUser(), userAttrNames));

			List<String> memberAttrNames = new ArrayList<>();
			for(AttributeDefinition ad: membersAttributesDef) {
				memberAttrNames.add(ad.getName());
			}
			List<Attribute> memberAttributes = new ArrayList<>(getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember, memberAttrNames));

			//add group-member attributes
			List<String> groupAttrNames = new ArrayList<>();
			for(AttributeDefinition ad: memberGroupAttributesDef) {
				groupAttrNames.add(ad.getName());
			}
			memberAttributes.addAll(getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember, group, groupAttrNames));

			richMember.setUserAttributes(userAttributes);
			richMember.setMemberAttributes(memberAttributes);
		}

		return richMembers;
	}

	@Override
	public int getMembersCount(PerunSession sess, Vo vo) {
		return getMembersManagerImpl().getMembersCount(sess, vo);
	}

	@Override
	public int getMembersCount(PerunSession sess, Vo vo, Status status) {
		return getMembersManagerImpl().getMembersCount(sess, vo, status);
	}

	@Override
	public Vo getMemberVo(PerunSession sess, Member member) {
		try {
			return getPerunBl().getVosManagerBl().getVoById(sess, getMembersManagerImpl().getMemberVoId(sess, member));
		} catch (VoNotExistsException e1) {
			throw new ConsistencyErrorException("Member is under nonexistent VO", e1);
		}
	}

	@Override
	public List<Member> findMembersByName(PerunSession sess, String searchString) {

		List<User> users = getPerunBl().getUsersManagerBl().findUsersByName(sess, searchString);

		List<Member> members = new ArrayList<>();
		for (User user: users) {
			members.addAll(getMembersManagerImpl().getMembersByUser(sess, user));
		}

		return members;
	}

	@Override
	public List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString) {

		List<User> users = getPerunBl().getUsersManagerBl().findUsersByName(sess, searchString);

		List<Member> members = new ArrayList<>();
		for (User user: users) {
			try {
				members.add(getMembersManagerImpl().getMemberByUserId(sess, vo, user.getId()));
			} catch (MemberNotExistsException e) {
				// User is not member of this VO
			}
		}

		return this.setAllMembersSameType(members, MembershipType.DIRECT);
	}

	@Override
	public List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString) {

		List<User> users = getPerunBl().getUsersManagerBl().findUsers(sess, searchString);

		List<Member> members = new ArrayList<>();
		for (User user: users) {
			try {
				members.add(getMembersManagerImpl().getMemberByUserId(sess, vo, user.getId()));
			} catch (MemberNotExistsException e) {
				// User is not member of this VO
			}
		}

		return this.setAllMembersSameType(members, MembershipType.DIRECT);
	}

	@Override
	public List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) {

		List<User> users = getPerunBl().getUsersManagerBl().findUsers(sess, searchString);
		List<Member> allGroupMembers = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		List<Member> allFoundMembers = new ArrayList<>();
		for(User user: users){
			allFoundMembers.addAll(getMembersByUser(sess, user));
		}
		allGroupMembers.retainAll(allFoundMembers);
		return allGroupMembers;
	}

	@Override
	public List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString) {

		List<User> users = getPerunBl().getUsersManagerBl().findUsers(sess, searchString);
		List<Member> allGroupMembers;
		if(group.getParentGroupId() == null) {
			Vo vo;
			try {
				vo = getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId());
			} catch (VoNotExistsException ex) {
				throw new ConsistencyErrorException("Vo for group " + group + " does not exist.");
			}
			allGroupMembers = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
		} else {
			allGroupMembers = getPerunBl().getGroupsManagerBl().getParentGroupMembers(sess, group);
		}

		List<Member> allFoundMembers = new ArrayList<>();
		for(User user: users){
			allFoundMembers.addAll(getMembersByUser(sess, user));
		}
		allGroupMembers.retainAll(allFoundMembers);
		return allGroupMembers;
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString, List<String> attrsNames) {
		List<Member> members = findMembersInGroup(sess, group, searchString);
		List<AttributeDefinition> attrsDefs = new ArrayList<>();
		for (String attrsName : attrsNames) {
			try {
				attrsDefs.add(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrsName));
			} catch (AttributeNotExistsException e) {
				//pass
			}
		}
		return convertMembersToRichMembersWithAttributes(sess, convertMembersToRichMembers(sess, members), attrsDefs);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString) {

		List<Member> members = findMembersInGroup(sess, group, searchString);
		return this.convertMembersToRichMembersWithAttributes(sess, this.convertMembersToRichMembers(sess, members));
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) {

		List<Member> members = findMembersInParentGroup(sess, group, searchString);
		return this.convertMembersToRichMembersWithAttributes(sess, this.convertMembersToRichMembers(sess, members));
	}


	@Override
	public List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString, boolean onlySposnored) {

		List<Member> members = this.findMembers(sess, vo, searchString, onlySposnored);

		return this.convertMembersToRichMembers(sess, this.setAllMembersSameType(members, MembershipType.DIRECT));
	}

	@Override
	public List<RichMember> findRichMembers(PerunSession sess, String searchString, boolean onlySponsored) {

		List<Member> members = this.findMembers(sess, null, searchString, onlySponsored);

		return this.convertMembersToRichMembers(sess, this.setAllMembersSameType(members, MembershipType.DIRECT));
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString, List<String> attrsNames, boolean onlySponsored) {
		List<RichMember> list = findRichMembersInVo(sess, vo, searchString, onlySponsored);
		List<AttributeDefinition> attrsDefs = new ArrayList<>();
		for (String attrsName : attrsNames) {
			try {
				attrsDefs.add(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrsName));
			} catch (AttributeNotExistsException e) {
				//pass
			}
		}
		return convertMembersToRichMembersWithAttributes(sess, list, attrsDefs);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) {

		List<RichMember> list = findRichMembersInVo(sess, vo, searchString, false);
		return convertMembersToRichMembersWithAttributes(sess, list);

	}

	@Override
	public List<RichMember> findRichMembersWithAttributes(PerunSession sess, String searchString, List<String> attrsNames) {
		List<RichMember> list = findRichMembers(sess, searchString, false);
		List<AttributeDefinition> attrsDefs = new ArrayList<>();
		for (String attrsName : attrsNames) {
			try {
				attrsDefs.add(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrsName));
			} catch (AttributeNotExistsException e) {
				//pass
			}
		}
		return convertMembersToRichMembersWithAttributes(sess, list, attrsDefs);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributes(PerunSession sess, String searchString) {
		List<RichMember> list = findRichMembers(sess, searchString, false);
		return convertMembersToRichMembersWithAttributes(sess, list);
	}

	@Override
	public void checkMemberExists(PerunSession sess, Member member) throws MemberNotExistsException {
		getMembersManagerImpl().checkMemberExists(sess, member);
	}

	@Override
	public void suspendMemberTo(PerunSession sess, Member member, Date suspendedTo) {
		BanOnVo ban = new BanOnVo();
		ban.setMemberId(member.getId());
		ban.setVoId(member.getVoId());
		ban.setValidityTo(suspendedTo);

		try {
			perunBl.getVosManagerBl().setBan(sess, ban);
		} catch (MemberNotExistsException e) {
			// shouldn't happen, we expect that the given member exists
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void unsuspendMember(PerunSession sess, Member member) {
		try {
			perunBl.getVosManagerBl().removeBanForMember(sess, member.getId());
		} catch (BanNotExistsException e) {
			log.warn("Member not unsuspended because he was not suspended.");
		}
	}

	@Override
	public boolean isMemberAllowed(PerunSession sess, Member member) {
		if (member == null) throw new InternalErrorException("Member can't be null.");
		return !this.haveStatus(sess, member, Status.INVALID) && !this.haveStatus(sess, member, Status.DISABLED);
	}

	@Override
	public Member setStatus(PerunSession sess, Member member, Status status) throws WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException {
		switch(status) {
			case VALID:
				return validateMember(sess, member);
			//break;
			case INVALID:
				return invalidateMember(sess, member);
			//break;
			case EXPIRED:
				return expireMember(sess, member);
			//break;
			case DISABLED:
				return disableMember(sess, member);
			//break;
			default:
				throw new InternalErrorException("Unknown status:" + status);
		}
	}

	@Override
	public Member validateMember(PerunSession sess, Member member) throws WrongAttributeValueException, WrongReferenceAttributeValueException {
		//this method run in nested transaction
		if(this.haveStatus(sess, member, Status.VALID)) {
			log.debug("Trying to validate member who is already valid. " + member);
			return member;
		}

		Status oldStatus = member.getStatus();
		getMembersManagerImpl().setStatus(sess, member, Status.VALID);
		member.setStatus(Status.VALID);
		getPerunBl().getAuditer().log(sess, new MemberValidated(member));
		if(oldStatus.equals(Status.INVALID) || oldStatus.equals(Status.DISABLED)) {
			try {
				getPerunBl().getAttributesManagerBl().doTheMagic(sess, member);
			} catch (Exception ex) {
				//return old status to object to prevent incorrect result in higher methods
				member.setStatus(oldStatus);
				throw ex;
			}
		}

		return member;
	}

	@Override
	public Member validateMemberAsync(final PerunSession sess, final Member member) {
		new Thread(() -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Status oldStatus = Status.getStatus(member.getStatus().getCode());

			try {
				((PerunSessionImpl) sess).getPerunBl().getMembersManagerBl().validateMember(sess, member);
			} catch(Exception ex) {
				log.info("validateMemberAsync failed.", ex);
				getPerunBl().getAuditer().log(sess, new MemberValidatedFailed(member, oldStatus));
				log.info("Validation of {} failed. He stays in {} state.", member, oldStatus);
			}
		}, "validateMemberAsync").start();
		return member;
	}

	@Override
	public Member invalidateMember(PerunSession sess, Member member) {
		if(this.haveStatus(sess, member, Status.INVALID)) {
			log.debug("Trying to invalidate member who is already invalid. " + member);
			return member;
		}

		getMembersManagerImpl().setStatus(sess, member, Status.INVALID);
		member.setStatus(Status.INVALID);
		getPerunBl().getAuditer().log(sess, new MemberInvalidated(member));
		return member;
	}

	@Override
	public Member expireMember(PerunSession sess, Member member) throws WrongReferenceAttributeValueException, WrongAttributeValueException {
		//this method run in nested transaction
		if(this.haveStatus(sess, member, Status.EXPIRED)) {
			log.debug("Trying to set member expired but he's already expired. " + member);
			return member;
		}

		Status oldStatus = member.getStatus();
		getMembersManagerImpl().setStatus(sess, member, Status.EXPIRED);
		member.setStatus(Status.EXPIRED);
		getPerunBl().getAuditer().log(sess, new MemberExpired(member));

		//We need to check validity of attributes first (expired member has to have valid attributes)
		if(oldStatus.equals(Status.INVALID) || oldStatus.equals(Status.DISABLED)) {
			try {
				getPerunBl().getAttributesManagerBl().doTheMagic(sess, member);
			} catch (Exception ex) {
				//return old status to object to prevent incorrect result in higher methods
				member.setStatus(oldStatus);
				throw ex;
			}
		}

		return member;
	}

	@Override
	public Member disableMember(PerunSession sess, Member member) throws MemberNotValidYetException {
		if(this.haveStatus(sess, member, Status.DISABLED)) {
			log.debug("Trying to disable member who is already disabled. " + member);
			return member;
		}

		if(this.haveStatus(sess, member, Status.INVALID)) throw new MemberNotValidYetException(member);
		getMembersManagerImpl().setStatus(sess, member, Status.DISABLED);
		member.setStatus(Status.DISABLED);
		getPerunBl().getAuditer().log(sess, new MemberDisabled(member));
		return member;
	}

	public void insertToMemberGroup(PerunSession sess, Member member, Vo vo) throws AlreadyMemberException {
		// Insert member into the members group
		try {
			getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
			Group g = getPerunBl().getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
			getPerunBl().getGroupsManagerBl().addMemberToMembersGroup(sess, g, member);
		} catch (GroupNotExistsException | VoNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeValueException | WrongReferenceAttributeValueException e) {
			throw new ConsistencyErrorException(e); //Member is not valid, so he couldn't have truly required atributes, neither he couldn't have influence on user attributes
		}
	}

	@Override
	public List<Member> retainMembersWithStatus(PerunSession sess, List<Member> members, Status status) {
		members.removeIf(member -> !haveStatus(sess, member, status));
		return members;
	}

	@Override
	public List<Member> getMembersByUsersIds(PerunSession sess, List<Integer> usersIds, Vo vo) {
		return getMembersManagerImpl().getMembersByUsersIds(sess, usersIds, vo);
	}

	@Override
	public List<Member> getMembersByUsers(PerunSession sess, List<User> users, Vo vo) {
		return getMembersManagerImpl().getMembersByUsers(sess, users, vo);
	}

	@Override
	public boolean haveStatus(PerunSession sess, Member member, Status status) {
		return member.getStatus().equals(status);
	}

	@Override
	public void extendMembership(PerunSession sess, Member member) throws ExtendMembershipException {
		this.manageMembershipExpiration(sess, member, true, true);
	}

	@Override
	public boolean canExtendMembership(PerunSession sess, Member member) {
		try {
			Pair<Boolean, Date> ret = this.manageMembershipExpiration(sess, member, false, false);
			return ret.getLeft();
		} catch (ExtendMembershipException e) {
			return false;
		}
	}

	@Override
	public boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws ExtendMembershipException {
		Pair<Boolean, Date> ret = this.manageMembershipExpiration(sess, member, false, true);
		return ret.getLeft();
	}

	@Override
	public Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws ExtendMembershipException {
	  // Check if the VO has set membershipExpirationRules attribute
    LinkedHashMap<String, String> membershipExpirationRules;

    Attribute membershipExpirationRulesAttribute;
    try {
      membershipExpirationRulesAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, vo, MembersManager.membershipExpirationRulesAttributeName);
      membershipExpirationRules = membershipExpirationRulesAttribute.valueAsMap();
      // If attribute was not filled, then silently exit with null
      if (membershipExpirationRules == null) return null;
    } catch (AttributeNotExistsException e) {
      // No rules set, so leave it as it is
      return null;
    } catch (WrongAttributeAssignmentException e) {
      throw new InternalErrorException("Shouldn't happen.");
    }

    // Which LOA we won't extend? This is applicable only for members who have already set expiration from the previous period
    if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName) != null) {
      String[] doNotExtendLoas = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName).split(",");

      for (String doNotExtendLoa : doNotExtendLoas) {
        if (doNotExtendLoa.equals(loa)) {
          // LOA provided is not allowed for extension
          throw new ExtendMembershipException(ExtendMembershipException.Reason.INSUFFICIENTLOA,
                "Provided LoA " + loa + " doesn't have required level for VO id " + vo.getId() + ".");
        }
      }
    }

    LocalDate localDate = LocalDate.now();

    String period = null;
    // Default extension
    if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName) != null) {
      period = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName);
    }

    // Do we extend particular LoA? Attribute syntax LoA|[period][.]
    if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName) != null) {
      // Which period
      String[] membershipPeriodLoa = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodLoaKeyName).split("\\|");
      String membershipLoa = membershipPeriodLoa[0];
      String periodLoa = membershipPeriodLoa[1];
      // Does the user have this LoA?
      if (membershipLoa.equals(loa)) {
        period = periodLoa;
      }
    }

    // Do we extend for x months or for static date?
    if (period != null) {
      if (period.startsWith("+")) {
		  try {
			  localDate = Utils.extendDateByPeriod(localDate, period);
		  } catch (InternalErrorException e) {
		  	throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute.", e);
		  }
      } else {
        // We will extend to particular date

        // Parse date
        Pattern p = Pattern.compile("([0-9]+).([0-9]+).");
        Matcher m = p.matcher(period);
		  if (!m.matches()) {
			  throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute. Period: " + period);
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
				LocalDate gracePeriodDate;
				try {
					Pair<Integer, TemporalUnit> fieldAmount = Utils.prepareGracePeriodDate(m);
					gracePeriodDate = localDate.minus(fieldAmount.getLeft(), fieldAmount.getRight());
				} catch (InternalErrorException e) {
					throw new InternalErrorException("Wrong format of gracePeriod in VO membershipExpirationRules attribute. gracePeriod: " + gracePeriod);
				}

              // Check if we are in grace period
              if (gracePeriodDate.isBefore(LocalDate.now())) {
                // We are in grace period, so extend to the next period
                localDate = localDate.plusYears(1);
              }
            }
          }
      }
    }

        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	@Override
	public Date getNewExtendMembership(PerunSession sess, Member member) {
		try {
			Pair<Boolean, Date> ret = this.manageMembershipExpiration(sess, member, false, false);
			if (ret.getLeft()) {
				return ret.getRight();
			}
		} catch (ExtendMembershipException e) {}

		return null;
	}

	/* Check if the user can apply for VO membership
	*/
	@Override
	public boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws ExtendMembershipException {
		return this.canBeMemberInternal(sess, vo, user, loa, true);
	}

	@Override
	public Member getMemberByExtSourceNameAndExtLogin(PerunSession sess, Vo vo, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, MemberNotExistsException, UserNotExistsException {
		User user = getPerunBl().getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, extSourceName, extLogin);
		return getPerunBl().getMembersManagerBl().getMemberByUser(sess, vo, user);
	}

	/* Check if the user can apply for VO membership
	*/
	@Override
	public boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) {
		try {
			return this.canBeMemberInternal(sess, vo, user, loa, false);
		} catch (ExtendMembershipException e) {
			return false;
		}
	}

	@Override
	public RichMember filterOnlyAllowedAttributes(PerunSession sess, RichMember richMember) {
		if(richMember == null) throw new InternalErrorException("RichMember can't be null.");
		if(richMember.getUser() == null) throw new InternalErrorException("User cant be null in RichMember.");
		//Filtering members attributes
		if(richMember.getMemberAttributes() != null) {
			richMember.setMemberAttributes(AuthzResolverBlImpl
				.filterNotAllowedAttributes(sess, richMember, richMember.getMemberAttributes()));
		}
		//Filtering users attributes
		if(richMember.getUserAttributes() != null) {
			richMember.setUserAttributes(AuthzResolverBlImpl
				.filterNotAllowedAttributes(sess, richMember.getUser(), richMember.getUserAttributes()));
		}
		return richMember;
	}

	@Override
	public List<RichMember> filterOnlyAllowedAttributes(PerunSession sess, List<RichMember> richMembers) {
		List<RichMember> filteredRichMembers = new ArrayList<>();
		if(richMembers == null || richMembers.isEmpty()) return filteredRichMembers;

		for(RichMember rm: richMembers) {
			filteredRichMembers.add(this.filterOnlyAllowedAttributes(sess, rm));
		}

		return filteredRichMembers;
	}

	@Override
	public List<RichMember> filterOnlyAllowedAttributes(PerunSession sess, List<RichMember> richMembers, Group group, boolean useContext) {
		//If no context should be used - every attribute is unique in context of member (for every member test access rights for all attributes again)
		if(!useContext) return filterOnlyAllowedAttributes(sess, richMembers);

		//If context should be used - every attribute is unique in context of friendlyName (every attribute test only once per friendlyName)
		List<RichMember> filteredRichMembers = new ArrayList<>();
		if(richMembers == null || richMembers.isEmpty()) return filteredRichMembers;

		// attr_name to boolean where null means - no rights at all, false means no write rights, true means read and write rights
		Map<String, Boolean> contextMap = new HashMap<>();
		// voId is there the context
		Integer voId = null;
		for(RichMember rm: richMembers) {
			//set or test voId for testing of context
			if(voId == null) {
				voId = rm.getVoId();
			} else {
				if(rm.getVoId() != voId) throw new InternalErrorException("Method using filtering by context, but some members are not from the same Vo!");
			}

			//Filtering members attributes
			if(rm.getMemberAttributes() != null) {
				List<Attribute> memberAttributes = rm.getMemberAttributes();
				List<Attribute> allowedMemberAttributes = new ArrayList<>();
				for(Attribute membAttr: memberAttributes) {
					//if there is record in contextMap, use it
					if(contextMap.containsKey(membAttr.getFriendlyName())) {
						Boolean isWritable = contextMap.get(membAttr.getFriendlyName());
						if(isWritable != null) {
							membAttr.setWritable(isWritable);
							allowedMemberAttributes.add(membAttr);
						}
					//if not, get information about authz rights and set record to contextMap
					} else {
						boolean canRead = false;
						if (membAttr.getNamespace().startsWith(AttributesManager.NS_MEMBER_GROUP_ATTR)) {
							canRead = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, membAttr, rm, group);
						} else if (membAttr.getNamespace().startsWith(AttributesManager.NS_MEMBER_ATTR)) {
							canRead = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, membAttr, rm);
						}
						if(canRead) {
							boolean isWritable = false;
							if (membAttr.getNamespace().startsWith(AttributesManager.NS_MEMBER_RESOURCE_ATTR)) {
								isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, membAttr, rm, group);
							} else if (membAttr.getNamespace().startsWith(AttributesManager.NS_MEMBER_ATTR)) {
								isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, membAttr, rm);
							}
							membAttr.setWritable(isWritable);
							allowedMemberAttributes.add(membAttr);
							contextMap.put(membAttr.getFriendlyName(), isWritable);
						} else {
							contextMap.put(membAttr.getFriendlyName(), null);
						}
					}
				}
				rm.setMemberAttributes(allowedMemberAttributes);
			}
			//Filtering users attributes
			if(rm.getUserAttributes() != null) {
				List<Attribute> userAttributes = rm.getUserAttributes();
				List<Attribute> allowedUserAttributes = new ArrayList<>();
				for(Attribute userAttr: userAttributes) {
					//if there is record in contextMap, use it
					if(contextMap.containsKey(userAttr.getFriendlyName())) {
						Boolean isWritable = contextMap.get(userAttr.getFriendlyName());
						if(isWritable != null) {
							userAttr.setWritable(isWritable);
							allowedUserAttributes.add(userAttr);
						}
					//if not, get information about authz rights and set record to contextMap
					} else {
						if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, userAttr, rm.getUser())) {
							boolean isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, userAttr, rm.getUser());
							userAttr.setWritable(isWritable);
							allowedUserAttributes.add(userAttr);
							contextMap.put(userAttr.getFriendlyName(), isWritable);
						} else {
							contextMap.put(userAttr.getFriendlyName(), null);
						}
					}
				rm.setUserAttributes(allowedUserAttributes);
				}
			}
			filteredRichMembers.add(rm);
		}
		return filteredRichMembers;
	}

	/**
	 * More info on https://wiki.metacentrum.cz/wiki/VO_managers%27s_manual
	 *
	 * Check if the user can apply for VO membership. VO restrictions doesn't apply to service users.
	 *
	 * @param sess session
	 * @param vo VO to apply for
	 * @param user User applying for membership
	 * @param loa level of assurance provided by user's external identity
	 * @param throwExceptions TRUE = throw exceptions / FALSE = return false when user can't be member of VO
	 * @return True if user can become member of VO / false or exception otherwise.
	 *
	 * @throws ExtendMembershipException When user can't be member of VO and throwExceptions is set to true
	 * @throws InternalErrorException
	*/
	protected boolean canBeMemberInternal(PerunSession sess, Vo vo, User user, String loa, boolean throwExceptions) throws ExtendMembershipException {

		if (user != null && user.isServiceUser()) return true;

		// Check if the VO has set membershipExpirationRules attribute
		LinkedHashMap<String, String> membershipExpirationRules;

		Attribute membershipExpirationRulesAttribute;
		try {
			membershipExpirationRulesAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, vo, MembersManager.membershipExpirationRulesAttributeName);
			membershipExpirationRules = membershipExpirationRulesAttribute.valueAsMap();
			// If attribute was not filled, then silently exit
			if (membershipExpirationRules == null) return true;
		} catch (AttributeNotExistsException e) {
			// No rules set, so leave it as it is
			return true;
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Shouldn't happen.");
		}

		// Which LOA we won't allow?
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipDoNotAllowLoaKeyName) != null) {
			if (loa == null) {
				// User doesn't have LOA defined and LOA is required for getting in, so do not allow membership.
				log.warn("User {} doesn't have LOA defined, but 'doNotAllowLoa' option is set for VO {}.", user, vo);
				if (throwExceptions) {
					throw new ExtendMembershipException(ExtendMembershipException.Reason.NOUSERLOA,
							"User " + user + " doesn't have LOA defined, but 'doNotExtendLoa' option is set for VO id " + vo.getId() + ".");
				} else {
					return false;
				}
			}

			String[] doNotAllowLoas = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipDoNotAllowLoaKeyName).split(",");

			for (String doNotAllowLoa : doNotAllowLoas) {
				if (doNotAllowLoa.equals(loa)) {
					// User has LOA which is not allowed for getting in
					if (throwExceptions) {
						throw new ExtendMembershipException(ExtendMembershipException.Reason.INSUFFICIENTLOA,
								"User " + user + " doesn't have required LOA for VO id " + vo.getId() + ".");
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * More info on https://wiki.metacentrum.cz/wiki/VO_managers%27s_manual
	 *
	 * If setAttributeValue is true, then store the membership expiration date into the attribute, otherwise
	 * return object pair containing true/false if the member can be extended and date specifying exact date of the new expiration
	 *
	 * @param sess session
	 * @param member member to check / set membership expiration
	 * @param setAttributeValue TRUE = set new membership expiration date / FALSE = do NOT set new expiration date (just calculate it)
	 * @param throwExceptions TRUE = throw exception / FALSE = return false when member can't extend membership
	 * @return Pair with result in left side (can / can't extend membership) and Date in right side telling new membership expiration date
	 *
	 * @throws InternalErrorException
	 * @throws ExtendMembershipException When member can't extend membership and throwException is set to true.
	 */
	protected Pair<Boolean, Date> manageMembershipExpiration(PerunSession sess, Member member, boolean setAttributeValue, boolean throwExceptions) throws ExtendMembershipException {
		// Check if the VO has set membershipExpirationRules attribute
		LinkedHashMap<String, String> membershipExpirationRules;

		Vo vo;
		Attribute membershipExpirationRulesAttribute;
		try {
			vo = getPerunBl().getVosManagerBl().getVoById(sess, member.getVoId());
			membershipExpirationRulesAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, vo, MembersManager.membershipExpirationRulesAttributeName);
			membershipExpirationRules = membershipExpirationRulesAttribute.valueAsMap();
			// If attribute was not filled, then silently exit
			if (membershipExpirationRules == null) return new Pair<>(true, null);
		} catch (VoNotExistsException e) {
			throw new ConsistencyErrorException("Member " + member + " of non-existing VO id=" + member.getVoId());
		} catch (AttributeNotExistsException e) {
			// There is no attribute definition for membership expiration rules.
			return new Pair<>(true, null);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Shouldn't happen.");
		}

		// Get user LOA
		String memberLoa = null;
		try {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			Attribute loa = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_VIRT + ":loa");
			memberLoa = Integer.toString((Integer) loa.getValue());
		} catch (AttributeNotExistsException e) {
			// Ignore, will be probably set further
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// Get current membershipExpiration date
		Attribute membershipExpirationAttribute;
		try {
			membershipExpirationAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, member,
					EXPIRATION);
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Attribute: " + AttributesManager.NS_MEMBER_ATTR_DEF +
						":membershipExpiration" + " must be defined in order to use membershipExpirationRules");
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		boolean isServiceUser;
		try {
			User user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
			isServiceUser = user.isServiceUser();
		} catch (UserNotExistsException ex) {
			throw new ConsistencyErrorException("User must exists for "+member+" when checking expiration rules.");
		}

		// Which LOA we won't extend?
		// This is applicable only for members who have already set expiration from the previous period
		// and are not service users
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName) != null &&
				membershipExpirationAttribute.getValue() != null &&
				!isServiceUser) {
			if (memberLoa == null) {
				// Member doesn't have LOA defined and LOA is required for extension, so do not extend membership.
				log.warn("Member {} doesn't have LOA defined, but 'doNotExtendLoa' option is set for VO id {}.", member, member.getVoId());
				if (throwExceptions) {
					throw new ExtendMembershipException(ExtendMembershipException.Reason.NOUSERLOA,
							"Member " + member + " doesn't have LOA defined, but 'doNotExtendLoa' option is set for VO id " + member.getVoId() + ".");
				} else {
					return new Pair<>(false, null);
				}
			}

			String[] doNotExtendLoas = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipDoNotExtendLoaKeyName).split(",");

			for (String doNotExtendLoa : doNotExtendLoas) {
				if (doNotExtendLoa.equals(memberLoa)) {
					// Member has LOA which is not allowed for extension
					if (throwExceptions) {
						throw new ExtendMembershipException(ExtendMembershipException.Reason.INSUFFICIENTLOAFOREXTENSION,
								"Member " + member + " doesn't have required LOA for VO id " + member.getVoId() + ".");
					} else {
						return new Pair<>(false, null);
					}
				}
			}
		}

		LocalDate localDate = LocalDate.now();

		// Does the user have expired membership, if yes, then for canExtendMembership return true
		if (!setAttributeValue && membershipExpirationAttribute.getValue() != null) {
			LocalDate currentMemberExpiration = LocalDate.parse((String) membershipExpirationAttribute.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);

			if (localDate.isAfter(currentMemberExpiration)) {
				return new Pair<>(true, null);
			}
		}

		String period = null;
		// Default extension
		if (membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName) != null) {
			period = membershipExpirationRules.get(AbstractMembershipExpirationRulesModule.membershipPeriodKeyName);
		}

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
						if (throwExceptions) {
							throw new ExtendMembershipException(ExtendMembershipException.Reason.INSUFFICIENTLOAFOREXTENSION,
									"Member " + member + " doesn't have required LOA for VO id " + member.getVoId() + ".");
						} else {
							return new Pair<>(false, null);
						}
					}
					// remove dot from the end of the string
					period = periodLoa.substring(0, periodLoa.length() - 1);
				} else {
					period = periodLoa;
				}
			}
		}

		// Do we extend for x months or for static date?
		if (period != null) {
			if (period.startsWith("+")) {
				if (!isMemberInGracePeriod(membershipExpirationRules, (String) membershipExpirationAttribute.getValue())) {
					if (throwExceptions) {
						throw new ExtendMembershipException(ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD, (String) membershipExpirationAttribute.getValue(),
								"Member " + member + " cannot extend because we are outside grace period for VO id " + member.getVoId() + ".");
					} else {
						return new Pair<>(false, null);
					}
				}

				// extend calendar by given period
				try {
					localDate = Utils.extendDateByPeriod(localDate, period);
				} catch (InternalErrorException e) {
					throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute.", e);
				}
			} else {
				// We will extend to particular date

				// Parse date
				Pattern p = Pattern.compile("([0-9]+).([0-9]+).");
				Matcher m = p.matcher(period);
				if (!m.matches()) {
					throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute. Period: " + period);
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
						Pair<Integer, TemporalUnit> fieldAmount;
						fieldAmount = Utils.prepareGracePeriodDate(m);
						LocalDate gracePeriodDate = localDate.minus(fieldAmount.getLeft(), fieldAmount.getRight());
						// Check if we are in grace period
						if (gracePeriodDate.isBefore(LocalDate.now())) {
							// We are in grace period, so extend to the next period
							localDate = localDate.plusYears(1);
						}

						// If we do not need to set the attribute value, only check if the current member's expiration time is not in grace period
						if (!setAttributeValue && membershipExpirationAttribute.getValue() != null) {
							LocalDate currentMemberExpiration = LocalDate.parse((String) membershipExpirationAttribute.getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
							currentMemberExpiration = currentMemberExpiration.minus(fieldAmount.getLeft(), fieldAmount.getRight());
							// if today is before that time, user can extend his period
							if (currentMemberExpiration.isAfter(LocalDate.now())) {
								if (throwExceptions) {
									throw new ExtendMembershipException(ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD, (String) membershipExpirationAttribute.getValue(),
											"Member " + member + " cannot extend because we are outside grace period for VO id " + member.getVoId() + ".");
								} else {
									return new Pair<>(false, null);
								}
							}
						}
					}
				}
			}

			// Set new value of the membershipExpiration for the member
			if (setAttributeValue) {
				membershipExpirationAttribute.setValue(localDate.toString());
				try {
					getPerunBl().getAttributesManagerBl().setAttribute(sess, member, membershipExpirationAttribute);
				} catch (WrongAttributeValueException e) {
					throw new InternalErrorException("Wrong value: " + membershipExpirationAttribute.getValue(),e);
				} catch (WrongReferenceAttributeValueException | WrongAttributeAssignmentException e) {
					throw new InternalErrorException(e);
				}
			}
		}
		return new Pair<>(true, Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
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

		LocalDate beginOfGracePeriod = LocalDate.parse(membershipExpiration, DateTimeFormatter.ISO_LOCAL_DATE);
		beginOfGracePeriod = beginOfGracePeriod.minus(amount, gracePeriodTimeUnit);

		if (beginOfGracePeriod.isBefore(LocalDate.now())) {
			return true;
		}

		return false;

	}

	@Override
	public void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url,
										   String mailAddress, String language) {

		User user = perunBl.getUsersManagerBl().getUserByMember(sess, member);

		List<Attribute> logins = perunBl.getAttributesManagerBl().getLogins(sess, user);
		boolean found = false;
		for (Attribute a : logins) {
			if (a.getFriendlyNameParameter().equals(namespace)) found = true;
		}
		if (!found)
			throw new InternalErrorException(user.toString() + " doesn't have login in namespace: " + namespace);

		String subject;
		try {
			Attribute subjectTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, language,
					AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":nonAuthzPwdResetMailSubject:" + namespace);
			subject = (String) subjectTemplateAttribute.getValue();
			if (subject == null) {
				subjectTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, "en",
						AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":nonAuthzPwdResetMailSubject:" + namespace);
				subject = (String) subjectTemplateAttribute.getValue();
			}
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		String message;
		try {
			Attribute messageTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, language,
					AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":nonAuthzPwdResetMailTemplate:" + namespace);
			message = (String) messageTemplateAttribute.getValue();
			if (message == null) {
				messageTemplateAttribute = perunBl.getAttributesManagerBl().getAttribute(sess, "en",
						AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":nonAuthzPwdResetMailTemplate:" + namespace);
				message = (String) messageTemplateAttribute.getValue();
			}
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		int id = getMembersManagerImpl().storePasswordResetRequest(sess, user, namespace, mailAddress);
		Utils.sendPasswordResetEmail(user, mailAddress, namespace, url, id, message, subject);

	}

	@Override
	public Member setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor) throws AlreadySponsoredMemberException, UserNotInRoleException {
		return setSponsorshipForMember(session, sponsoredMember, sponsor, null);
	}

	@Override
	public Member setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor, LocalDate validityTo) throws AlreadySponsoredMemberException, UserNotInRoleException {
		if(sponsoredMember.isSponsored()) {
			throw new AlreadySponsoredMemberException(sponsoredMember + " is already sponsored member!");
		}

		//Test if Vo exists and sponsor has the right role in it
		Vo membersVo;
		try {
			membersVo = getPerunBl().getVosManagerBl().getVoById(session, sponsoredMember.getVoId());
		} catch (VoNotExistsException ex) {
			throw new ConsistencyErrorException("Vo for " + sponsoredMember + " not exists!");
		}
		if (!getPerunBl().getVosManagerBl().isUserInRoleForVo(session, sponsor, Role.SPONSOR, membersVo, true)) {
			throw new UserNotInRoleException("User " + sponsor.getId() + " is not in role SPONSOR for VO " + membersVo.getId());
		}

		//set member to be sponsored
		sponsoredMember = getMembersManagerImpl().setSponsorshipForMember(session, sponsoredMember, sponsor, validityTo);
		getPerunBl().getAuditer().log(session, new SponsoredMemberSet(sponsoredMember));
		getPerunBl().getAuditer().log(session, new SponsorshipEstablished(sponsoredMember, sponsor, validityTo));

		return sponsoredMember;
	}

	@Override
	public Member unsetSponsorshipForMember(PerunSession session, Member sponsoredMember) throws MemberNotSponsoredException {
		if(!sponsoredMember.isSponsored()) {
			throw new MemberNotSponsoredException(sponsoredMember + " is not sponsored member!");
		}

		//set member to be sponsored
		List<User> sponsors = getPerunBl().getUsersManagerBl().getSponsors(session, sponsoredMember);
		sponsoredMember = getMembersManagerImpl().unsetSponsorshipForMember(session, sponsoredMember);
		getPerunBl().getAuditer().log(session, new SponsoredMemberUnset(sponsoredMember));
		for(User sponsor: sponsors) {
			getPerunBl().getAuditer().log(session, new SponsorshipRemoved(sponsoredMember, sponsor));
		}

		return sponsoredMember;
	}


	@Override
	public Member createSponsoredMember(PerunSession session, Vo vo, String namespace, Map<String, String> name, String password, String email, User sponsor, boolean asyncValidation) throws AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException, UserNotInRoleException, InvalidLoginException {
		return createSponsoredMember(session, vo, namespace, name, password, email, sponsor, null, asyncValidation);
	}

	@Override
	public Member createSponsoredMember(PerunSession session, Vo vo, String namespace, Map<String, String> name,
		String password, String email, User sponsor, LocalDate validityTo, boolean asyncValidation) throws AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException, UserNotInRoleException, InvalidLoginException {

		if (email == null) {
			email = NO_REPLY_EMAIL;
		}
		if (!Utils.emailPattern.matcher(email).matches()) {
			throw new InternalErrorException("Email has an invalid format: " + email);
		}
		//create new user
		User user;
		if (name.containsKey("guestName")) {
			user = Utils.parseUserFromCommonName(name.get("guestName"), true);
		} else {
			user = Utils.createUserFromNameMap(name, true);
		}

		User sponsoredUser = getPerunBl().getUsersManagerBl().createUser(session, user);

		if (email != null) {
			try {
				Attribute mailAttr = getPerunBl().getAttributesManagerBl().getAttribute(session, user, A_U_PREF_MAIL);

				mailAttr.setValue(email);

				getPerunBl().getAttributesManagerBl().setAttribute(session, user, mailAttr);
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
				throw new InternalErrorException(e);
			}
		}

		return setSponsoredMember(session, vo, sponsoredUser, namespace, password, sponsor, validityTo, asyncValidation);
	}

	@Override
	public Member setSponsoredMember(PerunSession session, Vo vo, User userToBeSponsored, String namespace, String password, User sponsor, boolean asyncValidation) throws AlreadyMemberException, ExtendMembershipException, UserNotInRoleException, WrongAttributeValueException, WrongReferenceAttributeValueException, LoginNotExistsException, PasswordCreationFailedException, InvalidLoginException, ExtSourceNotExistsException {
		return setSponsoredMember(session, vo, userToBeSponsored, namespace, password, sponsor, null, asyncValidation);
	}

	@Override
	public Member setSponsoredMember(PerunSession session, Vo vo, User userToBeSponsored, String namespace,
	                                 String password, User sponsor, LocalDate validityTo, boolean asyncValidation) throws AlreadyMemberException, ExtendMembershipException, UserNotInRoleException, WrongAttributeValueException, WrongReferenceAttributeValueException, LoginNotExistsException, PasswordCreationFailedException, InvalidLoginException, ExtSourceNotExistsException {
		//check that sponsoring user has role SPONSOR for the VO
		if (!getPerunBl().getVosManagerBl().isUserInRoleForVo(session, sponsor, Role.SPONSOR, vo, true)) {
			try {
				if (!AuthzResolver.authorizedToManageRole(session, vo, Role.SPONSOR)) {
					throw new UserNotInRoleException("user " + sponsor.getId() + " is not in role SPONSOR for VO " + vo.getId());
				}
				// if the principal is Authorized to set the Sponsor role, set it
				AuthzResolverBlImpl.setRole(session, sponsor, vo, Role.SPONSOR);
			} catch (AlreadyAdminException | RoleCannotBeManagedException | RoleManagementRulesNotExistsException e) {
				throw new InternalErrorException(e);
			}
		}

		String loginAttributeName = PasswordManagerModule.LOGIN_PREFIX + namespace;
		String attributeValue = getAttributeValueAsString (session, userToBeSponsored, loginAttributeName);

		//create the user account in external system if does not exist yet
		String login = createUserAccountInExternalSystem (session, userToBeSponsored, namespace, attributeValue, loginAttributeName, password);

		//create the member in Perun
		Member sponsoredMember = getMembersManagerImpl().createSponsoredMember(session, vo, userToBeSponsored, sponsor, validityTo);
		getPerunBl().getAuditer().log(session, new MemberCreated(sponsoredMember));
		getPerunBl().getAuditer().log(session, new SponsoredMemberSet(sponsoredMember));
		getPerunBl().getAuditer().log(session, new SponsorshipEstablished(sponsoredMember, sponsor, validityTo));
		extendMembership(session, sponsoredMember);
		insertToMemberGroup(session, sponsoredMember, vo);

		if (asyncValidation) {
			validateMemberAsync(session, sponsoredMember);
		} else {
			//for unit tests
			validateMember(session, sponsoredMember);
		}
		getPerunBl().getUsersManagerBl().validatePasswordAndSetExtSources(session, userToBeSponsored, login, namespace);

		return sponsoredMember;
	}

	@Override
	public Map<String, Map<String, String>> createSponsoredMembers(PerunSession sess, Vo vo, String namespace, List<String> names, String email, User sponsor, LocalDate validityTo, boolean asyncValidation) {
		Map<String, Map<String, String>> result = new HashMap<>();
		PasswordManagerModule module = getPerunBl().getUsersManagerBl().getPasswordManagerModule(sess, namespace);

		for (String name : names) {
			Map<String, String> mapName = new HashMap<>();
			if (name.contains(";")) {
				String[] split = name.split(";", 2);
				mapName.put("firstName", split[0]);
				mapName.put("lastName", split[1]);
			} else {
				mapName.put("guestName", name);
			}

			String password = module.generateRandomPassword(sess, null);

			// create sponsored member
			User user;
			try {
				user = perunBl.getUsersManagerBl().getUserByMember(sess,
						createSponsoredMember(sess, vo, namespace, mapName, password, email, sponsor, validityTo, asyncValidation));
				// get login to return
				String login = perunBl.getAttributesManagerBl().getAttribute(sess, user, PasswordManagerModule.LOGIN_PREFIX + namespace).valueAsString();
				Map<String, String> statusWithLogin = new HashMap<>();
				statusWithLogin.put("status", "OK");
				statusWithLogin.put("login", login);
				statusWithLogin.put("password", password);
				result.put(name, statusWithLogin);
			} catch (Exception e) {
				Map<String, String> status = new HashMap<>();
				status.put("status", e.getMessage());
				result.put(name, status);
			}
		}

		return result;
	}

	/**
	 * Try to get attribute from attribute manager
	 *
	 * @param session perun session
	 * @param userToBeSponsored user, that will be sponsored by sponsor
	 * @param loginAttributeName login attribute name
	 * @return attributeValue
	 */
	private String getAttributeValueAsString (PerunSession session, User userToBeSponsored, String loginAttributeName) {
		String attributeValue;
		try {
			attributeValue = perunBl.getAttributesManagerBl().getAttribute(session, userToBeSponsored, loginAttributeName).valueAsString();
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
			throw new InternalErrorException(e);
		}
		return attributeValue;
	}

	private void setLoginToSponsoredUser(PerunSession sess, User sponsoredUser, String loginAttributeName, String login) {
		try {
			Attribute a = getPerunBl().getAttributesManagerBl().getAttribute(sess, sponsoredUser, loginAttributeName);
			a.setValue(login);
			getPerunBl().getAttributesManagerBl().setAttribute(sess,sponsoredUser,a);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException |WrongAttributeValueException | WrongReferenceAttributeValueException e) {
			throw new InternalErrorException("cannot set attribute "+loginAttributeName+" for user "+sponsoredUser.getId(),e);
		}
	}

	@Override
	public Member sponsorMember(PerunSession session, Member sponsoredMember, User sponsor) throws MemberNotSponsoredException, AlreadySponsorException, UserNotInRoleException {
		return sponsorMember(session, sponsoredMember, sponsor, null);
	}

	@Override
	public Member sponsorMember(PerunSession session, Member sponsoredMember, User sponsor, LocalDate validityTo) throws MemberNotSponsoredException, AlreadySponsorException, UserNotInRoleException {
		//check that sponsoring user has role SPONSOR for the VO
		Vo vo = getMemberVo(session, sponsoredMember);
		if (!getPerunBl().getVosManagerBl().isUserInRoleForVo(session, sponsor, Role.SPONSOR, vo, true)) {
			throw new UserNotInRoleException("user " + sponsor.getId() + " is not in role SPONSOR for VO " + vo.getId());
		}
		if(!sponsoredMember.isSponsored()) {
			throw new MemberNotSponsoredException("member "+sponsoredMember.getId()+" is not marked as sponsored");
		}

		// check whether the user is already sponsor
		List<User> sponsors = getPerunBl().getUsersManagerBl().getSponsors(session, sponsoredMember);
		if(sponsors.stream().map(PerunBean::getId).anyMatch(id -> id==sponsor.getId())) {
			throw new AlreadySponsorException("member "+sponsoredMember.getId()+" is already sponsored by user "+sponsor.getId());
		}

		// add the sponsor
		getMembersManagerImpl().addSponsor(session, sponsoredMember, sponsor, validityTo);

		//remove expiration and validate member
		try {
			AttributeDefinition expiration = getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, EXPIRATION);
			getPerunBl().getAttributesManagerBl().removeAttribute(session, sponsoredMember, expiration);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException| WrongAttributeValueException | WrongReferenceAttributeValueException ex) {
			throw new InternalErrorException("cannot remove expiration date for sponsored member " + sponsoredMember.getId(), ex);
		}
		try {
			validateMember(session, sponsoredMember);
		} catch (WrongReferenceAttributeValueException | WrongAttributeValueException ex) {
			throw new InternalErrorException("cannot validate sponsored member " + sponsoredMember.getId(), ex);
		}

		getPerunBl().getAuditer().log(session, new SponsorshipEstablished(sponsoredMember, sponsor, validityTo));

		return sponsoredMember;
	}

	@Override
	public Sponsorship getSponsorship(PerunSession sess, Member sponsoredMember, User sponsor) throws SponsorshipDoesNotExistException {
		return getMembersManagerImpl().getSponsorship(sess, sponsoredMember, sponsor);
	}

	@Override
	public List<Member> getSponsoredMembers(PerunSession sess, Vo vo, User user) {
		return getMembersManagerImpl().getSponsoredMembers(sess, vo, user);
	}

	@Override
	public List<Member> getSponsoredMembers(PerunSession sess, User user) {
		return getMembersManagerImpl().getSponsoredMembers(sess, user);
	}

	@Override
	public List<Member> getSponsoredMembers(PerunSession sess, Vo vo) {
		return getMembersManagerImpl().getSponsoredMembers(sess, vo);
	}

	@Override
	public void removeSponsor(PerunSession sess, Member sponsoredMember, User sponsorToRemove) {
		getMembersManagerImpl().removeSponsor(sess,sponsoredMember, sponsorToRemove);
		getPerunBl().getAuditer().log(sess, new SponsorshipRemoved(sponsoredMember, sponsorToRemove));
		//check if the user was the last sponsor
		Vo vo = getMemberVo(sess, sponsoredMember);
		boolean hasSponsor = false;
		for (User sponsor : getPerunBl().getUsersManagerBl().getSponsors(sess, sponsoredMember)) {
			if(getPerunBl().getVosManagerBl().isUserInRoleForVo(sess, sponsor, Role.SPONSOR, vo, true)) {
				hasSponsor = true;
				break;
			}
		}
		if(!hasSponsor) {
			processMemberAfterRemovingLastSponsor(sess, sponsoredMember, vo);
		}
	}

	@Override
	public String extendExpirationForSponsoredMember(PerunSession sess, Member sponsoredMember, User sponsorUser) {
		List<User> sponsors = getPerunBl().getUsersManagerBl().getSponsors(sess, sponsoredMember);
		if(!sponsors.contains(sponsorUser)) {
			throw new IllegalArgumentException("user "+sponsorUser.getId()+" is not sponsor of member "+sponsoredMember.getId());
		}
		if(!sponsoredMember.isSponsored()) {
			throw new IllegalArgumentException("member "+sponsoredMember.getId()+" is not marked as sponsored");
		}
		//TODO VO-specific rules
		Date newExpiration = Date.from( Instant.now().plus( 1, ChronoUnit.YEARS));
		String expirationString = BeansUtils.getDateFormatterWithoutTime().format(newExpiration);
		//set the new expiration
		try {
			Attribute expiration = getPerunBl().getAttributesManagerBl().getAttribute(sess, sponsoredMember, EXPIRATION);
			expiration.setValue(expirationString);
			getPerunBl().getAttributesManagerBl().setAttribute(sess,sponsoredMember,expiration);
		} catch (WrongAttributeAssignmentException | AttributeNotExistsException|WrongAttributeValueException | WrongReferenceAttributeValueException e) {
			throw new InternalErrorException("cannot set expiration date to today for sponsored member "+sponsoredMember.getId(),e);
		}
		return expirationString;
	}

	@Override
	public MemberGroupStatus getUnifiedMemberGroupStatus(PerunSession sess, Member member, Resource resource) {
		return getMembersManagerImpl().getUnifiedMemberGroupStatus(sess, member, resource);
	}

	@Override
	public MemberGroupStatus getUnifiedMemberGroupStatus(PerunSession sess, User user, Facility facility) {
		return getMembersManagerImpl().getUnifiedMemberGroupStatus(sess, user, facility);
	}

	@Override
	public List<Member> findMembers(PerunSession sess, Vo vo, String searchString, boolean onlySponsored) {
		return getMembersManagerImpl().findMembers(sess, vo, searchString, onlySponsored);
	}

	@Override
	public void updateSponsorshipValidity(PerunSession sess, Member sponsoredMember, User sponsor,
	                                      LocalDate newValidity) throws SponsorshipDoesNotExistException {
		membersManagerImpl.updateSponsorshipValidity(sess, sponsoredMember, sponsor, newValidity);
		perunBl.getAuditer().log(sess, new SponsorshipValidityUpdated(sponsoredMember, sponsor, newValidity));
	}

	@Override
	public List<Sponsorship> getSponsorshipsExpiringInRange(PerunSession sess, LocalDate from, LocalDate to) {
		Utils.notNull(from, "from");
		Utils.notNull(to, "to");
		return membersManagerImpl.getSponsorshipsExpiringInRange(sess, from, to);
	}

	/**
	 * For given member, remove all of his bans on all entities.
	 *
	 * @param sess session
	 * @param member member
	 */
	private void removeAllMemberBans(PerunSession sess, Member member) {
		List<BanOnResource> bansOnResource = getPerunBl().getResourcesManagerBl().getBansForMember(sess, member.getId());
		for(BanOnResource banOnResource : bansOnResource) {
			try {
				getPerunBl().getResourcesManagerBl().removeBan(sess, banOnResource.getId());
			} catch (BanNotExistsException ex) {
				//it is ok, we just want to remove it anyway
			}
		}

		try {
			perunBl.getVosManagerBl().removeBanForMember(sess, member.getId());
		} catch (BanNotExistsException e) {
			// it is ok, we just want to remove it if it exists
		}
	}

	/**
	 * Take list of members and set them all the same type.
	 *
	 * @param members
	 * @param type
	 * @return list of members with the same type
	 */
	private List<Member> setAllMembersSameType(List<Member> members, MembershipType type) {
		if(members == null) return new ArrayList<>();
		for(Member m: members) {
			m.setMembershipType(type);
		}
		return members;
	}

	/**
	 * Gets the membersManagerImpl.
	 *
	 * @return The membersManagerImpl.
	 */
	public MembersManagerImplApi getMembersManagerImpl() {
		return this.membersManagerImpl;
	}

	/**
	 * Gets the perunBl.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	/**
	 * Creates a user account in external system if does not exist yet
	 *
	 * @param session perun session
	 * @param userToBeSponsored user, that will be sponsored by sponsor
	 * @param namespace namespace
	 * @param attributeValue attribute value
	 * @param loginAttributeName login attribute name
	 * @param password password
	 * @return login for given user
	 */
	private String createUserAccountInExternalSystem (PerunSession session, User userToBeSponsored, String namespace, String attributeValue, String loginAttributeName, String password) {
		String login;
		if (attributeValue == null || attributeValue.isEmpty()) {
			Map<String, String> p = new HashMap<>();
			p.put(PasswordManagerModule.TITLE_BEFORE_KEY, userToBeSponsored.getTitleBefore());
			p.put(PasswordManagerModule.FIRST_NAME_KEY, userToBeSponsored.getFirstName());
			p.put(PasswordManagerModule.LAST_NAME_KEY, userToBeSponsored.getLastName());
			p.put(PasswordManagerModule.TITLE_AFTER_KEY, userToBeSponsored.getTitleAfter());
			p.put(PasswordManagerModule.PASSWORD_KEY, password);
			Map<String, String> r;
			try {
				r = getPerunBl().getUsersManagerBl().generateAccount(session, namespace, p);
			} catch (PasswordStrengthException e) {
				throw new InternalErrorException("The password fails strength check required by the namespace.");
			}
			login = r.get(loginAttributeName);
			setLoginToSponsoredUser(session, userToBeSponsored, loginAttributeName, login);
		} else {
			login = attributeValue;
		}
		return login;
	}

	/**
	 * Execute process which expires member and removes his sponsorship.
	 *
	 * @param sess perun session
	 * @param sponsoredMember who will be processed
	 * @param vo of sponsored member
	 */
	private void processMemberAfterRemovingLastSponsor(PerunSession sess, Member sponsoredMember, Vo vo) {

		//Check whether the member should be expired according to vo's expiration rules.
		boolean shouldBeExpired = true;
		try {
			Attribute expirationRules = getPerunBl().getAttributesManagerBl().getAttribute(sess, vo, VO_EXPIRATION_RULES_ATTR);
			Map<String, String> rulesMap = expirationRules.valueAsMap();
			if (rulesMap != null && rulesMap.get(expireSponsoredMembers) != null)
				shouldBeExpired = Boolean.parseBoolean(rulesMap.get(expireSponsoredMembers));
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException e) {
			//When some error occurs we use default value, which means that the member should be expired.
		}
		if (shouldBeExpired) {
			//Set member's expiration to today and set status to expired.
			try {
				Attribute expiration = getPerunBl().getAttributesManagerBl().getAttribute(sess, sponsoredMember, EXPIRATION);
				expiration.setValue(BeansUtils.getDateFormatterWithoutTime().format(new Date()));
				getPerunBl().getAttributesManagerBl().setAttribute(sess,sponsoredMember,expiration);
			} catch (WrongAttributeAssignmentException | AttributeNotExistsException| WrongAttributeValueException | WrongReferenceAttributeValueException e) {
				throw new InternalErrorException("cannot set expiration date to today for sponsored member "+sponsoredMember.getId(),e);
			}
			try {
				expireMember(sess, sponsoredMember);
			} catch (WrongReferenceAttributeValueException | WrongAttributeValueException ex) {
				throw new InternalErrorException("cannot expire member "+sponsoredMember.getId(),ex);
			}
		}

		//Remove member's sponsorship.
		try {
			unsetSponsorshipForMember(sess, sponsoredMember);
		} catch (MemberNotSponsoredException e) {
			//Should not happen
			throw new InternalErrorException(e);
		}
	}
}
