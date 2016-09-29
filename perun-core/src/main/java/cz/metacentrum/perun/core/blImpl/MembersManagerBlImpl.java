package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.ActionType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.metacentrum.perun.core.api.exceptions.GroupOperationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.MembershipType;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.NotMemberOfParentGroupException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.MembersManagerImplApi;

public class MembersManagerBlImpl implements MembersManagerBl {

	final static Logger log = LoggerFactory.getLogger(MembersManagerBlImpl.class);

	private MembersManagerImplApi membersManagerImpl;
	private PerunBl perunBl;

	/**
	 * Constructor.
	 *
	 */
	public MembersManagerBlImpl(MembersManagerImplApi membersManagerImpl) {
		this.membersManagerImpl = membersManagerImpl;
	}

	public void deleteMember(PerunSession sess, Member member) throws InternalErrorException, MemberAlreadyRemovedException, GroupOperationsException {
		Vo vo = this.getMemberVo(sess, member);

		User user;
		try {
			user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
		} catch (UserNotExistsException e1) {
			throw new ConsistencyErrorException("Removing member who doesn't have corresponding user.", e1);
		}

		List<Facility> allowedFacilities = getPerunBl().getFacilitiesManagerBl().getAllowedFacilities(sess, user);

		Map<Facility, List<Attribute>> requiredAttributesBeforeMemberRemove = new HashMap<Facility, List<Attribute>>();

		for(Facility facility : allowedFacilities) {
			// Get actually required attributes, they will be later compared with list of required attributes when the member will be removed from all resources in this VO
			requiredAttributesBeforeMemberRemove.put(facility, getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, facility, user));
		}

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
			}
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException(e);
		}

		// Remove member's  attributes (namespaces: member and resource-member)
		try {
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, member);
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
			for(Resource resource : resources) {
				getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource, member);
			}
		} catch(AttributeValueException ex) {
			throw new ConsistencyErrorException("Member is removed from all groups. There are no required attribute for this member. Member's attributes can be removed without problem.", ex);
		} catch(WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		// Remove user-facility attributes which are no longer required
		for(Facility facility : allowedFacilities) {
			List<Attribute> requiredAttributes = requiredAttributesBeforeMemberRemove.get(facility);
			//remove currently required attributes from requiredAttributesBeforeMemberRemove
			requiredAttributes.removeAll(getPerunBl().getAttributesManagerBl().getRequiredAttributes(sess, facility, user));
			//remove attributes which are no longer required
			try {
				getPerunBl().getAttributesManagerBl().removeAttributes(sess, facility, user, requiredAttributes);
			} catch(AttributeValueException | WrongAttributeAssignmentException ex) {
				throw new ConsistencyErrorException(ex);
			}
		}

		//Remove all members bans
		List<BanOnResource> bansOnResource = getPerunBl().getResourcesManagerBl().getBansForMember(sess, member.getId());
		for(BanOnResource banOnResource : bansOnResource) {
			try {
				getPerunBl().getResourcesManagerBl().removeBan(sess, banOnResource.getId());
			} catch (BanNotExistsException ex) {
				//it is ok, we just want to remove it anyway
			}
		}
		
		/* TODO this can be used for future optimization. If the user is not asigned to the facility anymore all user-facility attributes (for this facility) can be safely removed.
			 for (Facility facility: facilitiesBeforeMemberRemove) {
		// Remove user-facility attributes
		try {
		getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, facility, user);
		log.debug("Removing user-facility attributes for facility {}", facility);
		} catch (AttributeValueException e) {
		throw new ConsistencyErrorException("Member is removed from all resources. There are no required attribute for this member. User-facility attributes can be removed without problem.", e);
		}
			 }
			 */

		// Remove member from the DB
		getMembersManagerImpl().deleteMember(sess, member);
		getPerunBl().getAuditer().log(sess, "{} deleted.", member);
	}

	public void deleteAllMembers(PerunSession sess, Vo vo) throws InternalErrorException, MemberAlreadyRemovedException, GroupOperationsException {
		for (Member m: this.getMembers(sess, vo)) {
			this.deleteMember(sess, m);
		}
	}

	public Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyMemberException, ExtendMembershipException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupOperationsException {
		return this.createMember(sess, vo, user, null);
	}

	public Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws InternalErrorException, AlreadyMemberException, ExtendMembershipException, WrongAttributeValueException, WrongReferenceAttributeValueException, GroupOperationsException {
		try {
			Member member = getMemberByUser(sess, vo, user);
			throw new AlreadyMemberException(member);
		} catch(MemberNotExistsException IGNORE) {
		}
		Member member = getMembersManagerImpl().createMember(sess, vo, user);
		getPerunBl().getAuditer().log(sess, "{} created.", member);

		// Set the initial membershipExpiration

		// Get user LOA
		String memberLoa = null;
		try {
			Attribute loa = getPerunBl().getAttributesManagerBl().getAttribute(sess, member, AttributesManager.NS_MEMBER_ATTR_VIRT + ":loa");
			memberLoa = (String) loa.getValue();
		} catch (AttributeNotExistsException e) {
			// user has no loa defined - if required by VO, it will be stopped in checking method later
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// check if user can be member - service members are not checked for LoA
		this.canBeMemberInternal(sess, vo, user, memberLoa, true);

		// set initial membership expiration
		this.extendMembership(sess, member);

		insertToMemberGroup(sess, member, vo);

		// add member also to all groups in list
		if(groups != null && !groups.isEmpty()) {
			for(Group group: groups) {
				try {
					perunBl.getGroupsManagerBl().addMember(sess, group, member);
				} catch (NotMemberOfParentGroupException ex) {
					throw new InternalErrorException("Member " + member + " can't be add to the group " + group + " because he is not member of it's parent group.", ex);
				} catch (GroupNotExistsException e) {
					throw new ConsistencyErrorException(e);
				}
			}
		}

		// Set default membership expiration

		return member;
	}

	public Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		return this.createSpecificMember(sess, vo, candidate, specificUserOwners, specificUserType, null);
	}

	public Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		if(specificUserType.equals(SpecificUserType.SERVICE)) candidate.setFirstName("(Service)");

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
		Member member = createMember(sess, vo, specificUserType, candidate, groups, null);

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

	public Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		return this.createMemberSync(sess, vo, candidate, null);
	}

	public Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups, List<String> overwriteUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		Member member = createMember(sess, vo, SpecificUserType.NORMAL, candidate, groups, overwriteUserAttributes);

		//Validate synchronously
		try {
			member = getPerunBl().getMembersManagerBl().validateMember(sess, member);
		} catch (AttributeValueException ex) {
			log.info("Member can't be validated. He stays in invalid state. Cause: " + ex);
		}

		return member;
	}

	public Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		return this.createMemberSync(sess, vo, candidate, groups, null);
	}
	
	public Member createSpecificMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		return this.createSpecificMemberSync(sess, vo, candidate, specificUserOwners, specificUserType, null);
	}

	public Member createSpecificMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {

		Member member = createSpecificMember(sess, vo, candidate, specificUserOwners, specificUserType, groups);

		//Validate synchronously
		try {
			member = validateMember(sess, member);
		} catch (AttributeValueException ex) {
			log.info("Specific Member can't be validated. He stays in invalid state. Cause: " + ex);
		}

		return member;
	}

	public Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		return createMember(sess, vo, candidate, null);
	}

	public Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		return createMember(sess, vo, SpecificUserType.NORMAL, candidate, groups, null);
	}

	public Member createMember(PerunSession sess, Vo vo, SpecificUserType specificUserType, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
			return this.createMember(sess, vo, specificUserType, candidate, null, new ArrayList<String>());
	}

	//MAIN METHOD
	public Member createMember(PerunSession sess, Vo vo, SpecificUserType specificUserType, Candidate candidate, List<Group> groups, List<String> overwriteUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
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
				} catch (UserNotExistsException e) {
					// Ignore, we are only checking if the user exists
				} catch (ExtSourceNotExistsException e) {
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
			if(specificUserType.equals(specificUserType.SERVICE)) user.setServiceUser(true);
			if(specificUserType.equals(specificUserType.SPONSORED)) user.setSponsoredUser(true);
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
		getPerunBl().getAuditer().log(sess, "{} created.", member);

		// Create the member's attributes
		List<Attribute> membersAttributes = new ArrayList<Attribute>();
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
			//if empty, skip setting or merging empty arrays of attributes at all
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
			Attribute loa = getPerunBl().getAttributesManagerBl().getAttribute(sess, member, AttributesManager.NS_MEMBER_ATTR_VIRT + ":loa");
			memberLoa = (String) loa.getValue();
		} catch (AttributeNotExistsException e) {
			// user has no loa defined - if required by VO, it will be stopped in checking method later
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// check if user can be member
		this.canBeMemberInternal(sess, vo, user, memberLoa, true);

		// set initial membership expiration
		this.extendMembership(sess, member);

		insertToMemberGroup(sess, member, vo);

		// add member also to all groups in list
		if(groups != null && !groups.isEmpty()) {
			for(Group group: groups) {
				try {
					perunBl.getGroupsManagerBl().addMember(sess, group, member);
				} catch (NotMemberOfParentGroupException ex) {
					throw new InternalErrorException("Member " + member + " can't be add to the group " + group + " because he is not member of it's parent group.", ex);
				} catch (GroupNotExistsException e) {
					throw new ConsistencyErrorException(e);
				}
			}
		}

		return member;
	}

	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int loa, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		return this.createMember(sess, vo, extSourceName, extSourceType, loa, login, candidate, null);
	}

	/*
	 * This method with support of LoA finally has to call this.createMember(PerunSession sess, Vo vo, UserExtSource userExtSource)
	 * @see cz.metacentrum.perun.core.api.MembersManager#createMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Vo, java.lang.String, java.lang.String, java.lang.String, cz.metacentrum.perun.core.api.Candidate)
	 */
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int loa, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {

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

	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
		return this.createMember(sess, vo, extSourceName, extSourceType, login, candidate, null);
	}

	/*
	 * This method finally has to call this.createMember(PerunSession sess, Vo vo, UserExtSource userExtSource)
	 * @see cz.metacentrum.perun.core.api.MembersManager#createMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Vo, java.lang.String, java.lang.String, java.lang.String, cz.metacentrum.perun.core.api.Candidate)
	 */
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {

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

	public Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException {
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
		} catch (ExtSourceNotExistsException ex) {
			throw new InternalErrorException("ExtSource " + extSource + " not exists.");
		}

		return this.createMember(sess, vo, candidate, groups);
	}

	public Member updateMember(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
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

	public Member getMemberByUserExtSource(PerunSession sess, Vo vo, UserExtSource uea) throws InternalErrorException, MemberNotExistsException {
		return getMembersManagerImpl().getMemberByUserExtSource(sess, vo, uea);
	}

	public Member getMemberByUserExtSources(PerunSession sess, Vo vo, List<UserExtSource> ueas) throws InternalErrorException, MemberNotExistsException {
		for (UserExtSource ues: ueas) {
			try {
				return getMembersManagerImpl().getMemberByUserExtSource(sess, vo, ues);
			} catch (MemberNotExistsException e) {
				// Ignore
			}
		}
		throw new MemberNotExistsException("Member with userExtSources " + ueas + " doesn't exists.");
	}

	public Member getMemberById(PerunSession sess, int id) throws InternalErrorException, MemberNotExistsException {
		return getMembersManagerImpl().getMemberById(sess, id);
	}

	public Member getMemberByUser(PerunSession sess, Vo vo, User user) throws InternalErrorException, MemberNotExistsException {
		return getMembersManagerImpl().getMemberByUserId(sess, vo, user.getId());
	}

	public Member getMemberByUserId(PerunSession sess, Vo vo, int userId) throws InternalErrorException, MemberNotExistsException {
		return getMembersManagerImpl().getMemberByUserId(sess, vo, userId);
	}

	public List<Member> getMembersByUser(PerunSession sess, User user) throws InternalErrorException {
		return getMembersManagerImpl().getMembersByUser(sess, user);
	}

	public List<Member> getMembers(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			Group g = getPerunBl().getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
			return getPerunBl().getGroupsManagerBl().getGroupMembers(sess, g);
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Member> getMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException {
		try {
			Group g = getPerunBl().getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
			return getPerunBl().getGroupsManagerBl().getGroupMembers(sess, g, status);
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	public RichMember getRichMember(PerunSession sess, Member member) throws InternalErrorException {
		List<Member> members = new ArrayList<Member>();
		members.add(member);
		return this.convertMembersToRichMembers(sess, members).get(0);
	}

	public RichMember getRichMemberWithAttributes(PerunSession sess, Member member) throws InternalErrorException {
		List<Member> members = new ArrayList<Member>();
		members.add(member);
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<RichMember> richMembersWithAttributes =  this.convertMembersToRichMembersWithAttributes(sess, richMembers);
		return richMembersWithAttributes.get(0);
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef) throws InternalErrorException {
		List<Member> members = new ArrayList<Member>();
		members.addAll(perunBl.getMembersManagerBl().getMembers(sess, vo));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<RichMember> richMembersWithAttributes = this.convertMembersToRichMembersWithAttributes(sess, richMembers, attrsDef);
		return richMembersWithAttributes;
	}

	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, AttributeNotExistsException {
		List<Member> members = new ArrayList<Member>();
		members.addAll(perunBl.getMembersManagerBl().getMembers(sess, vo));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<AttributeDefinition> attrsDef = new ArrayList<AttributeDefinition>();
		for(String atrrName: attrsNames) {
			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, atrrName);
			attrsDef.add(attrDef);
		}
		List<RichMember> richMembersWithAttributes = this.convertMembersToRichMembersWithAttributes(sess, richMembers, attrsDef);
		return richMembersWithAttributes;
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, AttributeNotExistsException {
		if(attrsNames == null || attrsNames.isEmpty()) {
			return this.getRichMembersWithAttributes(sess, vo);
		} else {
			return this.getRichMembersWithAttributesByNames(sess, vo, attrsNames);
		}
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws InternalErrorException, AttributeNotExistsException {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.getCompleteRichMembers(sess, vo, attrsNames), allowedStatuses);
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException {
		if(lookingInParentGroup) group = getPerunBl().getGroupsManagerBl().getParentGroup(sess, group);

		if(attrsNames == null || attrsNames.isEmpty()) {
			return this.convertMembersToRichMembersWithAttributes(sess, getRichMembers(sess, group));
		} else {
			return this.getRichMembersWithAttributesByNames(sess, group, attrsNames);
		}
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.getCompleteRichMembers(sess, group, attrsNames, lookingInParentGroup), allowedStatuses);
	}

	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString) throws InternalErrorException, AttributeNotExistsException {
		List<RichMember> richMembersWithAttributesFromVo = this.findRichMembersWithAttributesInVo(sess, vo, searchString);
		return this.getRichMembersOnlyWithSpecificAttrNames(sess, richMembersWithAttributesFromVo, attrsNames);
	}


	public List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, String searchString) throws InternalErrorException, AttributeNotExistsException {
		List<RichMember> richMembersWithAttributes = this.findRichMembersWithAttributes(sess, searchString);
		return this.getRichMembersOnlyWithSpecificAttrNames(sess, richMembersWithAttributes, attrsNames);
	}

	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, AttributeNotExistsException {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.findCompleteRichMembers(sess, vo, attrsNames, searchString), allowedStatuses);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, AttributeNotExistsException {
		return getOnlyRichMembersWithAllowedStatuses(sess, this.findCompleteRichMembers(sess, attrsNames, searchString), allowedStatuses);
	}

	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException {
		if(lookingInParentGroup) group = getPerunBl().getGroupsManagerBl().getParentGroup(sess, group);
		List<RichMember> richMembersWithAttributesFromGroup = this.findRichMembersWithAttributesInGroup(sess, group, searchString);
		return this.getRichMembersOnlyWithSpecificAttrNames(sess, richMembersWithAttributesFromGroup, attrsNames);
	}

	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException {
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
	 * @throws InternalErrorException
	 */
	private List<RichMember> getOnlyRichMembersWithAllowedStatuses(PerunSession sess, List<RichMember> richMembers, List<String> allowedStatuses) throws InternalErrorException {
		List<RichMember> allowedRichMembers = new ArrayList<RichMember>();
		if(richMembers == null || richMembers.isEmpty()) return allowedRichMembers;
		if(allowedStatuses == null || allowedStatuses.isEmpty()) return richMembers;

		//Covert statuses to objects Status
		List<Status> statuses = new ArrayList<Status>();
		for(String status: allowedStatuses) {
			statuses.add(Status.valueOf(status));
		}

		for(RichMember rm: richMembers) {
			if(statuses.contains(rm.getStatus())) allowedRichMembers.add(rm);
		}

		return allowedRichMembers;
	}

	/**
	 * From list of richMembers with attributes get all these richMembers only with specificied attributes by attrsNames.
	 * If attrsNames is empty or null, return back all richMembers with all already defined attributes.
	 *
	 * @param sess
	 * @param richMembersWithAttributes
	 * @param attrsNames
	 * @return list of RichMembers with already specified attributes.
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	private List<RichMember> getRichMembersOnlyWithSpecificAttrNames(PerunSession sess, List<RichMember> richMembersWithAttributes, List<String> attrsNames) throws InternalErrorException, AttributeNotExistsException {
		if(richMembersWithAttributes == null || richMembersWithAttributes.isEmpty()) return new ArrayList<RichMember>();
		if(attrsNames == null || attrsNames.isEmpty()) return richMembersWithAttributes;
		for(RichMember rm: richMembersWithAttributes) {
			for(Iterator<Attribute> userAttributeIter = rm.getUserAttributes().iterator(); userAttributeIter.hasNext();) {
				Attribute attr = userAttributeIter.next();
				if(!attrsNames.contains(attr.getName())) userAttributeIter.remove();
			}
			for(Iterator<Attribute> memberAttributeIter = rm.getMemberAttributes().iterator(); memberAttributeIter.hasNext();) {
				Attribute attr = memberAttributeIter.next();
				if(!attrsNames.contains(attr.getName())) memberAttributeIter.remove();
			}
		}
		return richMembersWithAttributes;
	}

	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws InternalErrorException, AttributeNotExistsException {
		List<Member> members = new ArrayList<Member>();
		members.addAll(perunBl.getGroupsManagerBl().getGroupMembers(sess, group));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<AttributeDefinition> attrsDef = new ArrayList<AttributeDefinition>();
		for(String atrrName: attrsNames) {
			AttributeDefinition attrDef = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, atrrName);
			attrsDef.add(attrDef);
		}
		List<RichMember> richMembersWithAttributes = this.convertMembersToRichMembersWithAttributes(sess, richMembers, attrsDef);
		return richMembersWithAttributes;
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef) throws InternalErrorException {
		List<Member> members = new ArrayList<Member>();
		members.addAll(perunBl.getGroupsManagerBl().getGroupMembers(sess, group));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		List<RichMember> richMembersWithAttributes = this.convertMembersToRichMembersWithAttributes(sess, richMembers, attrsDef);
		return richMembersWithAttributes;
	}

	public List<RichMember> getRichMembers(PerunSession sess, Vo vo) throws InternalErrorException {
		List<Member> members = this.getMembers(sess, vo);
		return this.convertMembersToRichMembers(sess, members);
	}

	public List<RichMember> getRichMembers(PerunSession sess, Group group) throws InternalErrorException {
		List<Member> members = new ArrayList<Member>();
		members.addAll(perunBl.getGroupsManagerBl().getGroupMembers(sess, group));
		List<RichMember> richMembers = this.convertMembersToRichMembers(sess, members);
		return richMembers;
	}

	public List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException {
		List<Member> members = this.getMembers(sess, vo, status);
		return this.convertMembersToRichMembers(sess, members);
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo) throws InternalErrorException {
		List<RichMember> richMembers = this.getRichMembers(sess, vo);
		return this.convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status) throws InternalErrorException {
		List<RichMember> richMembers = this.getRichMembers(sess, vo, status);
		return this.convertMembersToRichMembersWithAttributes(sess, richMembers);
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group) throws InternalErrorException {
		List<RichMember> richMembers = this.getRichMembers(sess, group);
		return getOnlyRichMembersWithAllowedStatuses(sess, this.convertMembersToRichMembersWithAttributes(sess, richMembers), allowedStatuses);
	}


	public List<RichMember> convertMembersToRichMembers(PerunSession sess, List<Member> members) throws InternalErrorException {
		List<RichMember> richMembers = new ArrayList<RichMember>();

		for (Member member: members) {
			User user = getPerunBl().getUsersManagerBl().getUserByMember(sess, member);
			List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);

			RichMember richMember = new RichMember(user, member, userExtSources);
			richMembers.add(richMember);
		}

		return richMembers;
	}

	public List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers)  throws InternalErrorException {
		for (RichMember richMember: richMembers) {
			List<Attribute> userAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember.getUser());
			List<Attribute> memberAttributes = getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember);

			richMember.setUserAttributes(userAttributes);
			richMember.setMemberAttributes(memberAttributes);
		}

		return richMembers;
	}

	public List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers, List<AttributeDefinition> attrsDef)  throws InternalErrorException {
		List<AttributeDefinition> usersAttributesDef = new ArrayList<AttributeDefinition>();
		List<AttributeDefinition> membersAttributesDef = new ArrayList<AttributeDefinition>();

		for(AttributeDefinition attrd: attrsDef) {
			if(attrd.getName().startsWith(AttributesManager.NS_USER_ATTR)) usersAttributesDef.add(attrd);
			else if(attrd.getName().startsWith(AttributesManager.NS_MEMBER_ATTR)) membersAttributesDef.add(attrd);
		}

		for (RichMember richMember: richMembers) {
			List<Attribute> userAttributes = new ArrayList<Attribute>();
			List<Attribute> memberAttributes = new ArrayList<Attribute>();

			List<String> userAttrNames = new ArrayList<String>();
			for(AttributeDefinition ad: usersAttributesDef) {
				userAttrNames.add(ad.getName());
			}
			userAttributes.addAll(getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember.getUser(), userAttrNames));

			List<String> memberAttrNames = new ArrayList<String>();
			for(AttributeDefinition ad: membersAttributesDef) {
				memberAttrNames.add(ad.getName());
			}
			memberAttributes.addAll(getPerunBl().getAttributesManagerBl().getAttributes(sess, richMember, memberAttrNames));

			richMember.setUserAttributes(userAttributes);
			richMember.setMemberAttributes(memberAttributes);
		}

		return richMembers;
	}

	public int getMembersCount(PerunSession sess, Vo vo) throws InternalErrorException {
		return getMembersManagerImpl().getMembersCount(sess, vo);
	}

	public int getMembersCount(PerunSession sess, Vo vo, Status status) throws InternalErrorException {
		return getMembersManagerImpl().getMembersCount(sess, vo, status);
	}

	public Vo getMemberVo(PerunSession sess, Member member) throws InternalErrorException {
		try {
			return getPerunBl().getVosManagerBl().getVoById(sess, getMembersManagerImpl().getMemberVoId(sess, member));
		} catch (VoNotExistsException e1) {
			throw new ConsistencyErrorException("Member is under nonexistent VO", e1);
		}
	}

	public List<Member> findMembersByName(PerunSession sess, String searchString) throws InternalErrorException {

		List<User> users = getPerunBl().getUsersManagerBl().findUsersByName(sess, searchString);

		List<Member> members = new ArrayList<Member>();
		for (User user: users) {
			members.addAll(getMembersManagerImpl().getMembersByUser(sess, user));
		}

		return members;
	}

	public List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException {

		List<User> users = getPerunBl().getUsersManagerBl().findUsersByName(sess, searchString);

		List<Member> members = new ArrayList<Member>();
		for (User user: users) {
			try {
				members.add(getMembersManagerImpl().getMemberByUserId(sess, vo, user.getId()));
			} catch (MemberNotExistsException e) {
				// User is not member of this VO
			}
		}

		return this.setAllMembersSameType(members, MembershipType.DIRECT);
	}

	public List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException {

		List<User> users = getPerunBl().getUsersManagerBl().findUsers(sess, searchString);

		List<Member> members = new ArrayList<Member>();
		for (User user: users) {
			try {
				members.add(getMembersManagerImpl().getMemberByUserId(sess, vo, user.getId()));
			} catch (MemberNotExistsException e) {
				// User is not member of this VO
			}
		}

		return this.setAllMembersSameType(members, MembershipType.DIRECT);
	}

	public List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException{

		List<User> users = getPerunBl().getUsersManagerBl().findUsers(sess, searchString);
		List<Member> allGroupMembers = getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group);
		List<Member> allFoundMembers = new ArrayList<Member>();
		for(User user: users){
			allFoundMembers.addAll(getMembersByUser(sess, user));
		}
		allGroupMembers.retainAll(allFoundMembers);
		return allGroupMembers;
	}

	public List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, ParentGroupNotExistsException{

		List<User> users = getPerunBl().getUsersManagerBl().findUsers(sess, searchString);
		List<Member> allGroupMembers = new ArrayList<Member>();
		if(group.getParentGroupId() == null) {
			Vo vo = null;
			try {
				vo = getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId());
			} catch (VoNotExistsException ex) {
				throw new ConsistencyErrorException(group + " is not in " + vo);
			}
			allGroupMembers = getPerunBl().getMembersManagerBl().getMembers(sess, vo);
		} else {
			allGroupMembers = getPerunBl().getGroupsManagerBl().getParentGroupMembers(sess, group);
		}

		List<Member> allFoundMembers = new ArrayList<Member>();
		for(User user: users){
			allFoundMembers.addAll(getMembersByUser(sess, user));
		}
		allGroupMembers.retainAll(allFoundMembers);
		return allGroupMembers;
	}

	public List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException{

		List<Member> members = findMembersInGroup(sess, group, searchString);
		return this.convertMembersToRichMembersWithAttributes(sess, this.convertMembersToRichMembers(sess, members));
	}

	public List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, ParentGroupNotExistsException{

		List<Member> members = findMembersInParentGroup(sess, group, searchString);
		return this.convertMembersToRichMembersWithAttributes(sess, this.convertMembersToRichMembers(sess, members));
	}


	public List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException {

		List<User> users = getPerunBl().getUsersManagerBl().findUsers(sess, searchString);

		List<Member> members = new ArrayList<Member>();
		for (User user: users) {
			try {
				members.add(getMembersManagerImpl().getMemberByUserId(sess, vo, user.getId()));
			} catch (MemberNotExistsException e) {
				// User is not member of this VO
			}
		}

		return this.convertMembersToRichMembers(sess, this.setAllMembersSameType(members, MembershipType.DIRECT));
	}

	@Override
	public List<RichMember> findRichMembers(PerunSession sess, String searchString) throws InternalErrorException {

		List<User> users = getPerunBl().getUsersManagerBl().findUsers(sess, searchString);

		List<Member> members = new ArrayList<Member>();
		for (User user: users) {
			members.addAll(getMembersByUser(sess, user));
		}

		return this.convertMembersToRichMembers(sess, this.setAllMembersSameType(members, MembershipType.DIRECT));
	}

	public List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException {

		List<RichMember> list = findRichMembersInVo(sess, vo, searchString);
		return convertMembersToRichMembersWithAttributes(sess, list);

	}

	@Override
	public List<RichMember> findRichMembersWithAttributes(PerunSession sess, String searchString) throws InternalErrorException {
		List<RichMember> list = findRichMembers(sess, searchString);
		return convertMembersToRichMembersWithAttributes(sess, list);
	}

	public void checkMemberExists(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException {
		getMembersManagerImpl().checkMemberExists(sess, member);
	}

	public Member setStatus(PerunSession sess, Member member, Status status) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException {
		switch(status) {
			case VALID:
				return validateMember(sess, member);
				//break;
			case INVALID:
				return invalidateMember(sess, member);
				//break;
			case SUSPENDED:
				return suspendMember(sess, member);
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

	public Member validateMember(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		if(this.haveStatus(sess, member, Status.VALID)) {
			log.debug("Trying to validate member who is already valid. " + member);
			return member;
		}

		Status oldStatus = member.getStatus();
		getMembersManagerImpl().setStatus(sess, member, Status.VALID);
		member.setStatus(Status.VALID);
		getPerunBl().getAuditer().log(sess, "{} validated.", member);
		if(oldStatus.equals(Status.INVALID)) {
			try {
				getPerunBl().getAttributesManagerBl().doTheMagic(sess, member);
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			}
		}

		return member;
	}

	public Member validateMemberAsync(final PerunSession sess, final Member member) throws InternalErrorException {
		new Thread(new Runnable() {
			public void run() {
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
					log.info("validateMemberAsync failed. Cause: {}", ex);
					try {
						getPerunBl().getAuditer().log(sess, "Validation of {} failed. He stays in {} state.", member, oldStatus);
						log.info("Validation of {} failed. He stays in {} state.", member, oldStatus);
					} catch(InternalErrorException internalError) {
						log.error("Store message to auditer failed. message: Validation of {} failed. He stays in {} state. cause: {}", new Object[] {member, oldStatus, internalError});
					}
				}
			}
		}, "validateMemberAsync").start();
		return member;
	}

	public Member invalidateMember(PerunSession sess, Member member) throws InternalErrorException {
		if(this.haveStatus(sess, member, Status.INVALID)) {
			log.debug("Trying to invalidate member who is already invalid. " + member);
			return member;
		}

		getMembersManagerImpl().setStatus(sess, member, Status.INVALID);
		member.setStatus(Status.INVALID);
		getPerunBl().getAuditer().log(sess, "{} invalidated.", member);
		return member;
	}

	public Member suspendMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotValidYetException {
		if(this.haveStatus(sess, member, Status.SUSPENDED)) {
			log.warn("Trying to suspend member who is already suspended. Suspend operation will be procesed anyway (to be shure)." + member);
		}

		if(this.haveStatus(sess, member, Status.INVALID)) throw new MemberNotValidYetException(member);
		getMembersManagerImpl().setStatus(sess, member, Status.SUSPENDED);
		member.setStatus(Status.SUSPENDED);
		getPerunBl().getAuditer().log(sess, "{} suspended #{}.", member, Auditer.engineForceKeyword);
		return member;
	}

	public Member expireMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotValidYetException {
		if(this.haveStatus(sess, member, Status.EXPIRED)) {
			log.debug("Trying to set member expired but he's already expired. " + member);
			return member;
		}

		if(this.haveStatus(sess, member, Status.INVALID)) throw new MemberNotValidYetException(member);
		getMembersManagerImpl().setStatus(sess, member, Status.EXPIRED);
		member.setStatus(Status.EXPIRED);
		getPerunBl().getAuditer().log(sess, "{} expired.", member);
		return member;
	}

	public Member disableMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotValidYetException {
		if(this.haveStatus(sess, member, Status.DISABLED)) {
			log.debug("Trying to disable member who is already disabled. " + member);
			return member;
		}

		if(this.haveStatus(sess, member, Status.INVALID)) throw new MemberNotValidYetException(member);
		getMembersManagerImpl().setStatus(sess, member, Status.DISABLED);
		member.setStatus(Status.DISABLED);
		getPerunBl().getAuditer().log(sess, "{} disabled.", member);
		return member;
	}

	public void insertToMemberGroup(PerunSession sess, Member member, Vo vo) throws InternalErrorException, AlreadyMemberException, GroupOperationsException {
		// Insert member into the members group
		try {
			getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
			Group g = getPerunBl().getGroupsManagerBl().getGroupByName(sess, vo, VosManager.MEMBERS_GROUP);
			getPerunBl().getGroupsManagerBl().addMemberToMembersGroup(sess, g, member);
		} catch (NotMemberOfParentGroupException ex) {
			//members group is top level -> this should not happen
			throw new ConsistencyErrorException(ex);
		} catch (GroupNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (VoNotExistsException e) {
			throw new InternalErrorException(e);
		} catch (WrongAttributeValueException e) {
			throw new ConsistencyErrorException(e); //Member is not valid, so he couldn't have truly required atributes, neither he couldn't have influence on user attributes
		} catch (WrongReferenceAttributeValueException e) {
			throw new ConsistencyErrorException(e); //Member is not valid, so he couldn't have truly required atributes, neither he couldn't have influence on user attributes
		}
	}

	public List<Member> retainMembersWithStatus(PerunSession sess, List<Member> members, Status status) throws InternalErrorException {
		Iterator<Member> iterator =  members.iterator();
		while(iterator.hasNext()) {
			Member member = iterator.next();
			if(!haveStatus(sess, member, status)) iterator.remove();
		}
		return members;
	}

	public List<Member> getMembersByUsersIds(PerunSession sess, List<Integer> usersIds, Vo vo) throws InternalErrorException {
		return getMembersManagerImpl().getMembersByUsersIds(sess, usersIds, vo);
	}

	public List<Member> getMembersByUsers(PerunSession sess, List<User> users, Vo vo) throws InternalErrorException {
		return getMembersManagerImpl().getMembersByUsers(sess, users, vo);
	}

	public boolean haveStatus(PerunSession sess, Member member, Status status) {
		return member.getStatus().equals(status);
	}

	public void extendMembership(PerunSession sess, Member member) throws InternalErrorException, ExtendMembershipException {
		this.manageMembershipExpiration(sess, member, true, true);
	}

	public boolean canExtendMembership(PerunSession sess, Member member) throws InternalErrorException {
		try {
			Pair<Boolean, Date> ret = this.manageMembershipExpiration(sess, member, false, false);
			return ret.getLeft();
		} catch (ExtendMembershipException e) {
			return false;
		}
	}

	public boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws InternalErrorException, ExtendMembershipException {
		Pair<Boolean, Date> ret = this.manageMembershipExpiration(sess, member, false, true);
		return ret.getLeft();
	}

	public Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws InternalErrorException, ExtendMembershipException {
	  // Check if the VO has set membershipExpirationRules attribute
    LinkedHashMap<String, String> membershipExpirationRules;

    Attribute membershipExpirationRulesAttribute = null;
    try {
      membershipExpirationRulesAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, vo, MembersManager.membershipExpirationRulesAttributeName);
      membershipExpirationRules = (LinkedHashMap<String, String>) membershipExpirationRulesAttribute.getValue();
      // If attribute was not filled, then silently exit with null
      if (membershipExpirationRules == null) return null;
    } catch (AttributeNotExistsException e) {
      // No rules set, so leave it as it is
      return null;
    } catch (WrongAttributeAssignmentException e) {
      throw new InternalErrorException("Shouldn't happen.");
    }

    // Which LOA we won't extend? This is applicable only for members who have already set expiration from the previous period
    if (membershipExpirationRules.get(MembersManager.membershipDoNotExtendLoaKeyName) != null) {
      String[] doNotExtendLoas = membershipExpirationRules.get(MembersManager.membershipDoNotExtendLoaKeyName).split(",");

      for (String doNotExtendLoa : doNotExtendLoas) {
        if (doNotExtendLoa.equals(loa)) {
          // LOA provided is not allowed for extension
          throw new ExtendMembershipException(ExtendMembershipException.Reason.INSUFFICIENTLOA,
                "Provided LoA " + loa + " doesn't have required level for VO id " + vo.getId() + ".");
        }
      }
    }

    Calendar calendar = Calendar.getInstance();

    String period = null;
    // Default extension
    if (membershipExpirationRules.get(MembersManager.membershipPeriodKeyName) != null) {
      period = membershipExpirationRules.get(MembersManager.membershipPeriodKeyName);
    }

    // Do we extend particular LoA? Attribute syntax LoA|[period][.]
    if (membershipExpirationRules.get(MembersManager.membershipPeriodLoaKeyName) != null) {
      // Which period
      String[] membershipPeriodLoa = membershipExpirationRules.get(MembersManager.membershipPeriodLoaKeyName).split("\\|");
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
        // By default do not add nothing
        int amount = 0;
        int field;

        // We will add days/months/years
        Pattern p = Pattern.compile("\\+([0-9]+)([dmy]?)");
        Matcher m = p.matcher(period);
        if (m.matches()) {
          String countString = m.group(1);
          amount = Integer.valueOf(countString);

          String dmyString = m.group(2);
          if (dmyString.equals("d")) {
            field = Calendar.DAY_OF_YEAR;
          } else if (dmyString.equals("m")) {
            field = Calendar.MONTH;
          } else if (dmyString.equals("y")) {
            field = Calendar.YEAR;
          } else {
            throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute. Period: " + period);
          }
        } else {
          throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute. Period: " + period);
        }

        // Add days/months/years
        calendar.add(field, amount);
      } else {
        // We will extend to particular date

        // Parse date
        Pattern p = Pattern.compile("([0-9]+).([0-9]+).");
        Matcher m = p.matcher(period);
        if (m.matches()) {
          int day = Integer.valueOf(m.group(1));
          int month = Integer.valueOf(m.group(2));

          // Get current year
          int year = calendar.get(Calendar.YEAR);

          // We must detect if the extension date is in current year or in a next year
          boolean extensionInNextYear;
          Calendar extensionCalendar = Calendar.getInstance();
          extensionCalendar.set(year, month-1, day);
          Calendar today = Calendar.getInstance();
          if (extensionCalendar.before(today)) {
            // Extension date is in a next year
            extensionInNextYear = true;
          } else {
            // Extension is in the current year
            extensionInNextYear = false;
          }

          // Set the date to which the membershi should be extended, can be changed if there was grace period, see next part of the code
          calendar.set(year, month-1, day); // month is 0-based
          if (extensionInNextYear) {
            calendar.add(Calendar.YEAR, 1);
          }

          // ***** GRACE PERIOD *****
          // Is there a grace period?
          if (membershipExpirationRules.get(MembersManager.membershipGracePeriodKeyName) != null) {
            String gracePeriod = membershipExpirationRules.get(MembersManager.membershipGracePeriodKeyName);
            // If the extension is requested in period-gracePeriod then extend to next period

            // Get the value of the grace period
            p = Pattern.compile("([0-9]+)([dmy]?)");
            m = p.matcher(gracePeriod);
            if (m.matches()) {
              String countString = m.group(1);
              int amount = Integer.valueOf(countString);

              // Set the gracePeriodCalendar to the extension date
              Calendar gracePeriodCalendar = Calendar.getInstance();
              gracePeriodCalendar.set(year, month-1, day);
              if (extensionInNextYear) {
                gracePeriodCalendar.add(Calendar.YEAR, 1);
              }

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
              // subtracts period definition, e.g. 3m
              gracePeriodCalendar.add(field, -amount);

              // Check if we are in grace period
              if (gracePeriodCalendar.before(Calendar.getInstance())) {
                // We are in grace period, so extend to the next period
                calendar.add(Calendar.YEAR, 1);
              }
            }
          }
        } else {
          throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute. Period: " + period);
        }
      }

      // Reset hours, minutes and seconds to 0
      calendar.set(Calendar.HOUR, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
    }
    
    return calendar.getTime();
	}

	public Date getNewExtendMembership(PerunSession sess, Member member) throws InternalErrorException {
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
	public boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, ExtendMembershipException {
		return this.canBeMemberInternal(sess, vo, user, loa, true);
	}

	/* Check if the user can apply for VO membership
	*/
	public boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException {
		try {
			return this.canBeMemberInternal(sess, vo, user, loa, false);
		} catch (ExtendMembershipException e) {
			return false;
		}
	}

	public RichMember filterOnlyAllowedAttributes(PerunSession sess, RichMember richMember) throws InternalErrorException {
		if(richMember == null) throw new InternalErrorException("RichMember can't be null.");
		if(richMember.getUser() == null) throw new InternalErrorException("User cant be null in RichMember.");
		//Filtering members attributes
		if(richMember.getMemberAttributes() != null) {
			List<Attribute> memberAttributes = richMember.getMemberAttributes();
			List<Attribute> allowedMemberAttributes = new ArrayList<Attribute>();
			for(Attribute membAttr: memberAttributes) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, membAttr, richMember, null)) {
					membAttr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, membAttr, richMember, null));
					allowedMemberAttributes.add(membAttr);
				}
			}
			richMember.setMemberAttributes(allowedMemberAttributes);
		}
		//Filtering users attributes
		if(richMember.getUserAttributes() != null) {
			List<Attribute> userAttributes = richMember.getUserAttributes();
			List<Attribute> allowedUserAttributes = new ArrayList<Attribute>();
			for(Attribute userAttr: userAttributes) {
				if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, userAttr, richMember.getUser(), null)) {
					userAttr.setWritable(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, userAttr, richMember.getUser(), null));
					allowedUserAttributes.add(userAttr);
				}
			}
			richMember.setUserAttributes(allowedUserAttributes);
		}
		return richMember;
	}

	public List<RichMember> filterOnlyAllowedAttributes(PerunSession sess, List<RichMember> richMembers) throws InternalErrorException {
		List<RichMember> filteredRichMembers = new ArrayList<RichMember>();
		if(richMembers == null || richMembers.isEmpty()) return filteredRichMembers;

		for(RichMember rm: richMembers) {
			filteredRichMembers.add(this.filterOnlyAllowedAttributes(sess, rm));
		}

		return filteredRichMembers;
	}

	public List<RichMember> filterOnlyAllowedAttributes(PerunSession sess, List<RichMember> richMembers, boolean useContext) throws InternalErrorException {
		//If no context should be used - every attribute is unique in context of member (for every member test access rights for all attributes again)
		if(!useContext) return filterOnlyAllowedAttributes(sess, richMembers);

		//If context should be used - every attribute is unique in context of friendlyName (every attribute test only once per friendlyName)
		List<RichMember> filteredRichMembers = new ArrayList<RichMember>();
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
				List<Attribute> allowedMemberAttributes = new ArrayList<Attribute>();
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
						if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, membAttr, rm, null)) {
							boolean isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, membAttr, rm, null);
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
				List<Attribute> allowedUserAttributes = new ArrayList<Attribute>();
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
						if(AuthzResolver.isAuthorizedForAttribute(sess, ActionType.READ, userAttr, rm.getUser(), null)) {
							boolean isWritable = AuthzResolver.isAuthorizedForAttribute(sess, ActionType.WRITE, userAttr, rm.getUser(), null);
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
	protected boolean canBeMemberInternal(PerunSession sess, Vo vo, User user, String loa, boolean throwExceptions) throws InternalErrorException, ExtendMembershipException {

		if (user != null && user.isServiceUser()) return true;

		// Check if the VO has set membershipExpirationRules attribute
		LinkedHashMap<String, String> membershipExpirationRules;

		Attribute membershipExpirationRulesAttribute = null;
		try {
			membershipExpirationRulesAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, vo, MembersManager.membershipExpirationRulesAttributeName);
			membershipExpirationRules = (LinkedHashMap<String, String>) membershipExpirationRulesAttribute.getValue();
			// If attribute was not filled, then silently exit
			if (membershipExpirationRules == null) return true;
		} catch (AttributeNotExistsException e) {
			// No rules set, so leave it as it is
			return true;
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Shouldn't happen.");
		}

		// Which LOA we won't allow?
		if (membershipExpirationRules.get(MembersManager.membershipDoNotAllowLoaKeyName) != null) {
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

			String[] doNotAllowLoas = membershipExpirationRules.get(MembersManager.membershipDoNotAllowLoaKeyName).split(",");

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
	protected Pair<Boolean, Date> manageMembershipExpiration(PerunSession sess, Member member, boolean setAttributeValue, boolean throwExceptions) throws InternalErrorException, ExtendMembershipException {
		// Check if the VO has set membershipExpirationRules attribute
		LinkedHashMap<String, String> membershipExpirationRules;

		Vo vo;
		Attribute membershipExpirationRulesAttribute = null;
		try {
			vo = getPerunBl().getVosManagerBl().getVoById(sess, member.getVoId());
			membershipExpirationRulesAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, vo, MembersManager.membershipExpirationRulesAttributeName);
			membershipExpirationRules = (LinkedHashMap<String, String>) membershipExpirationRulesAttribute.getValue();
			// If attribute was not filled, then silently exit
			if (membershipExpirationRules == null) return new Pair<Boolean, Date>(true, null);
		} catch (VoNotExistsException e) {
			throw new ConsistencyErrorException("Member " + member + " of non-existing VO id=" + member.getVoId());
		} catch (AttributeNotExistsException e) {
			// There is no attribute definition for membership expiration rules.
			return new Pair<Boolean, Date>(true, null);
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException("Shouldn't happen.");
		}

		// Get user LOA
		String memberLoa = null;
		try {
			Attribute loa = getPerunBl().getAttributesManagerBl().getAttribute(sess, member, AttributesManager.NS_MEMBER_ATTR_VIRT + ":loa");
			memberLoa = (String) loa.getValue();
		} catch (AttributeNotExistsException e) {
			// Ignore, will be probably set further
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		// Get current membershipExpiration date
		Attribute membershipExpirationAttribute = null;
		try {
			membershipExpirationAttribute = getPerunBl().getAttributesManagerBl().getAttribute(sess, member,
					AttributesManager.NS_MEMBER_ATTR_DEF + ":membershipExpiration");
		} catch (AttributeNotExistsException e) {
			throw new ConsistencyErrorException("Attribute: " + AttributesManager.NS_MEMBER_ATTR_DEF +
						":membershipExpiration" + " must be defined in order to use membershipExpirationRules");
		} catch (WrongAttributeAssignmentException e) {
			throw new InternalErrorException(e);
		}

		boolean isServiceUser = false;
		try {
			User user = getPerunBl().getUsersManagerBl().getUserById(sess, member.getUserId());
			isServiceUser = user.isServiceUser();
		} catch (UserNotExistsException ex) {
			throw new ConsistencyErrorException("User must exists for "+member+" when checking expiration rules.");
		}

		// Which LOA we won't extend?
		// This is applicable only for members who have already set expiration from the previous period
		// and are not service users
		if (membershipExpirationRules.get(MembersManager.membershipDoNotExtendLoaKeyName) != null &&
				membershipExpirationAttribute.getValue() != null &&
				!isServiceUser) {
			if (memberLoa == null) {
				// Member doesn't have LOA defined and LOA is required for extension, so do not extend membership.
				log.warn("Member {} doesn't have LOA defined, but 'doNotExtendLoa' option is set for VO id {}.", member, member.getVoId());
				if (throwExceptions) {
					throw new ExtendMembershipException(ExtendMembershipException.Reason.NOUSERLOA,
							"Member " + member + " doesn't have LOA defined, but 'doNotExtendLoa' option is set for VO id " + member.getVoId() + ".");
				} else {
					return new Pair<Boolean, Date>(false, null);
				}
			}

			String[] doNotExtendLoas = membershipExpirationRules.get(MembersManager.membershipDoNotExtendLoaKeyName).split(",");

			for (String doNotExtendLoa : doNotExtendLoas) {
				if (doNotExtendLoa.equals(memberLoa)) {
					// Member has LOA which is not allowed for extension
					if (throwExceptions) {
						throw new ExtendMembershipException(ExtendMembershipException.Reason.INSUFFICIENTLOAFOREXTENSION,
								"Member " + member + " doesn't have required LOA for VO id " + member.getVoId() + ".");
					} else {
						return new Pair<Boolean, Date>(false, null);
					}
				}
			}
		}

		Calendar calendar = Calendar.getInstance();

		// Does the user have expired membership, if yes, then for canExtendMembership return true
		if (!setAttributeValue && membershipExpirationAttribute.getValue() != null) {
			try {
				Date currentMemberExpiration = BeansUtils.getDateFormatterWithoutTime().parse((String) membershipExpirationAttribute.getValue());

				Calendar currentMemberExpirationCalendar = Calendar.getInstance();
				currentMemberExpirationCalendar.setTime(currentMemberExpiration);

				if (calendar.after(currentMemberExpirationCalendar)) {
					return new Pair<Boolean, Date>(true, null);
				}
			} catch (ParseException e) {
				throw new InternalErrorException("Wrong format of the membersExpiration: " + membershipExpirationAttribute.getValue(), e);
			}
		}

		String period = null;
		// Default extension
		if (membershipExpirationRules.get(MembersManager.membershipPeriodKeyName) != null) {
			period = membershipExpirationRules.get(MembersManager.membershipPeriodKeyName);
		}

		// Do we extend particular LoA? Attribute syntax LoA|[period][.]
		if (membershipExpirationRules.get(MembersManager.membershipPeriodLoaKeyName) != null) {
			// Which period
			String[] membershipPeriodLoa = membershipExpirationRules.get(MembersManager.membershipPeriodLoaKeyName).split("\\|");
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
							return new Pair<Boolean, Date>(false, null);
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
						return new Pair<Boolean, Date>(false, null);
					}
				}
				// By default do not add nothing
				int amount = 0;
				int field;

				// We will add days/months/years
				Pattern p = Pattern.compile("\\+([0-9]+)([dmy]?)");
				Matcher m = p.matcher(period);
				if (m.matches()) {
					String countString = m.group(1);
					amount = Integer.valueOf(countString);

					String dmyString = m.group(2);
					if (dmyString.equals("d")) {
						field = Calendar.DAY_OF_YEAR;
					} else if (dmyString.equals("m")) {
						field = Calendar.MONTH;
					} else if (dmyString.equals("y")) {
						field = Calendar.YEAR;
					} else {
						throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute. Period: " + period);
					}
				} else {
					throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute. Period: " + period);
				}

				// Add days/months/years
				calendar.add(field, amount);
			} else {
				// We will extend to particular date

				// Parse date
				Pattern p = Pattern.compile("([0-9]+).([0-9]+).");
				Matcher m = p.matcher(period);
				if (m.matches()) {
					int day = Integer.valueOf(m.group(1));
					int month = Integer.valueOf(m.group(2));

					// Get current year
					int year = calendar.get(Calendar.YEAR);

					// We must detect if the extension date is in current year or in a next year
					boolean extensionInNextYear;
					Calendar extensionCalendar = Calendar.getInstance();
					extensionCalendar.set(year, month-1, day);
					Calendar today = Calendar.getInstance();
					if (extensionCalendar.before(today)) {
						// Extension date is in a next year
						extensionInNextYear = true;
					} else {
						// Extension is in the current year
						extensionInNextYear = false;
					}

					// Set the date to which the membershi should be extended, can be changed if there was grace period, see next part of the code
					calendar.set(year, month-1, day); // month is 0-based
					if (extensionInNextYear) {
						calendar.add(Calendar.YEAR, 1);
					}

					// ***** GRACE PERIOD *****
					// Is there a grace period?
					if (membershipExpirationRules.get(MembersManager.membershipGracePeriodKeyName) != null) {
						String gracePeriod = membershipExpirationRules.get(MembersManager.membershipGracePeriodKeyName);
						// If the extension is requested in period-gracePeriod then extend to next period

						// Get the value of the grace period
						p = Pattern.compile("([0-9]+)([dmy]?)");
						m = p.matcher(gracePeriod);
						if (m.matches()) {
							String countString = m.group(1);
							int amount = Integer.valueOf(countString);

							// Set the gracePeriodCalendar to the extension date
							Calendar gracePeriodCalendar = Calendar.getInstance();
							gracePeriodCalendar.set(year, month-1, day);
							if (extensionInNextYear) {
								gracePeriodCalendar.add(Calendar.YEAR, 1);
							}

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
							// subtracts period definition, e.g. 3m
							gracePeriodCalendar.add(field, -amount);

							// Check if we are in grace period
							if (gracePeriodCalendar.before(Calendar.getInstance())) {
								// We are in grace period, so extend to the next period
								calendar.add(Calendar.YEAR, 1);
							}

							// If we do not need to set the attribute value, only check if the current member's expiration time is not in grace period
							if (!setAttributeValue && membershipExpirationAttribute.getValue() != null) {
								try {
									Date currentMemberExpiration = BeansUtils.getDateFormatterWithoutTime().parse((String) membershipExpirationAttribute.getValue());
									// subtracts grace period from the currentMemberExpiration
									Calendar currentMemberExpirationCalendar = Calendar.getInstance();
									currentMemberExpirationCalendar.setTime(currentMemberExpiration);

									currentMemberExpirationCalendar.add(field, -amount);

									// if today is before that time, user can extend his period
									if (currentMemberExpirationCalendar.after(Calendar.getInstance())) {
										if (throwExceptions) {
											throw new ExtendMembershipException(ExtendMembershipException.Reason.OUTSIDEEXTENSIONPERIOD, (String) membershipExpirationAttribute.getValue(),
													"Member " + member + " cannot extend because we are outside grace period for VO id " + member.getVoId() + ".");
										} else {
											return new Pair<Boolean, Date>(false, null);
										}
									}
								} catch (ParseException e) {
									throw new InternalErrorException("Wrong format of the membersExpiration: " + membershipExpirationAttribute.getValue(), e);
								}
							}
						}
					}
				} else {
					throw new InternalErrorException("Wrong format of period in VO membershipExpirationRules attribute. Period: " + period);
				}
			}

			// Reset hours, minutes and seconds to 0
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			// Set new value of the membershipExpiration for the member
			if (setAttributeValue) {
				membershipExpirationAttribute.setValue(BeansUtils.getDateFormatterWithoutTime().format(calendar.getTime()));
				try {
					getPerunBl().getAttributesManagerBl().setAttribute(sess, member, membershipExpirationAttribute);
				} catch (WrongAttributeValueException e) {
					throw new InternalErrorException("Wrong value: " + membershipExpirationAttribute.getValue(),e);
				} catch (WrongReferenceAttributeValueException e) {
					throw new InternalErrorException(e);
				} catch (WrongAttributeAssignmentException e) {
					throw new InternalErrorException(e);
				}
			}
		}
		return new Pair<Boolean, Date>(true, calendar.getTime());
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
		if (membershipExpirationRules.get(MembersManager.membershipGracePeriodKeyName) == null) {
			// If not grace period is infinite
			return true;
		}
		// does member have expiration date?
		if (membershipExpiration == null) {
			// if not grace period is infinite
			return true;
		}

		String gracePeriod = membershipExpirationRules.get(MembersManager.membershipGracePeriodKeyName);

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

	public void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url) throws InternalErrorException {

		User user = perunBl.getUsersManagerBl().getUserByMember(sess, member);

		List<Attribute> logins = perunBl.getAttributesManagerBl().getLogins(sess, user);
		boolean found = false;
		for (Attribute a : logins) {
			if (a.getFriendlyNameParameter().equals(namespace)) found = true;
		}
		if (!found) throw new InternalErrorException(user.toString()+" doesn't have login in namespace: "+namespace);

		String email = "";
		try {
			Attribute a = perunBl.getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_DEF+":preferredMail");
			if (a != null && a.getValue() != null) {
				email = (String)a.getValue();
			}
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException(ex);
		}

		int id = getMembersManagerImpl().storePasswordResetRequest(sess, user, namespace);
		Utils.sendPasswordResetEmail(user, email, namespace, url, id);

	}

	/**
	 * Take list of members and set them all the same type.
	 *
	 * @param members
	 * @param type
	 * @return list of members with the same type
	 * @throws InternalErrorException
	 */
	private List<Member> setAllMembersSameType(List<Member> members, MembershipType type) throws InternalErrorException {
		if(members == null) return new ArrayList<Member>();
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


}
