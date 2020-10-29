package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Sponsor;
import cz.metacentrum.perun.core.api.Sponsorship;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberWithSponsors;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SpecificUserType;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AlreadySponsorException;
import cz.metacentrum.perun.core.api.exceptions.AlreadySponsoredMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSponsoredException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSuspendedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SponsorshipDoesNotExistException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotInRoleException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class MembersManagerEntry implements MembersManager {

	final static Logger log = LoggerFactory.getLogger(MembersManagerEntry.class);

	private MembersManagerBl membersManagerBl;
	private PerunBl perunBl;

	/**
	 * Constructor.
	 */
	public MembersManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.membersManagerBl = perunBl.getMembersManagerBl();
	}

	public MembersManagerEntry() {
	}

	@Override
	public void deleteMember(PerunSession sess, Member member) throws MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteMember_Member_policy", member)) {
			throw new PrivilegeException(sess, "deleteMember");
		}


		getMembersManagerBl().deleteMember(sess, member);
	}

	@Override
	public void deleteMembers(PerunSession sess, List<Member> members) throws MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		for (Member member : members) {
			getMembersManagerBl().checkMemberExists(sess, member);
		}

		// Authorization
		for (Member member: members) {
			if (!AuthzResolver.authorizedInternal(sess, "deleteMembers_List<Member>_policy", member)) {
				throw new PrivilegeException(sess, "deleteMembers");
			}
		}

		getMembersManagerBl().deleteMembers(sess, members);
	}

	@Override
	public void deleteAllMembers(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException, MemberAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteAllMembers_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "deleteAllMembers");
		}

		getMembersManagerBl().deleteAllMembers(sess, vo);
	}

	@Override
	public Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException {
		return this.createSpecificMember(sess, vo, candidate, specificUserOwners, specificUserType, null);
	}

	@Override
	public Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(specificUserType, "specificUserType");

		Utils.notNull(candidate, "candidate");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		if (specificUserOwners.isEmpty())
			throw new InternalErrorException("List of specificUserOwners of " + candidate + " can't be empty.");

		for (User u : specificUserOwners) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, u);
		}

		if (!specificUserType.equals(SpecificUserType.SERVICE))
			throw new InternalErrorException("Only service user type is allowed.");

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		// Authorization
		if (groups != null && !groups.isEmpty()) {
			for (Group group: groups) {
				if (!AuthzResolver.authorizedInternal(sess, "createSpecificMember_Vo_Candidate_List<User>_SpecificUserType_List<Group>_policy", vo, group)) {
					throw new PrivilegeException("createSpecificMember");
				}
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "createSpecificMember_Vo_Candidate_List<User>_SpecificUserType_List<Group>_policy", vo)) {
				throw new PrivilegeException("createSpecificMember");
			}
		}

		return getMembersManagerBl().createSpecificMember(sess, vo, candidate, specificUserOwners, specificUserType, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		return this.createMember(sess, vo, candidate, new ArrayList<>());
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(candidate, "candidate");

		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		// Authorization
		if (groups != null && !groups.isEmpty()) {
			for (Group group: groups) {
				if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_Candidate_List<Group>_policy", vo, group)) {
					throw new PrivilegeException("createMember");
				}
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_Candidate_List<Group>_policy", vo)) {
				throw new PrivilegeException("createMember");
			}
		}

		return getMembersManagerBl().createMember(sess, vo, candidate, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		return this.createMember(sess, vo, extSourceName, extSourceType, login, candidate, null);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extSourceType, "extSourceType");
		Utils.notNull(login, "login");

		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		// Authorization
		if (groups != null && !groups.isEmpty()) {
			for (Group group: groups) {
				if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_String_String_String_Candidate_List<Group>_policy", vo, group)) {
					throw new PrivilegeException("createMember");
				}
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_String_String_String_Candidate_List<Group>_policy", vo)) {
				throw new PrivilegeException("createMember");
			}
		}

		return getMembersManagerBl().createMember(sess, vo, extSourceName, extSourceType, login, candidate, groups);
	}


	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		return this.createMember(sess, vo, extSourceName, extSourceType, extSourceLoa, login, candidate, null);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extSourceType, "extSourceType");
		Utils.notNull(login, "login");

		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		// Authorization
		if (groups != null && !groups.isEmpty()) {
			for (Group group: groups) {
				if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_String_String_int_String_Candidate_List<Group>_policy", vo, group)) {
					throw new PrivilegeException("createMember");
				}
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_String_String_int_String_Candidate_List<Group>_policy", vo)) {
				throw new PrivilegeException("createMember");
			}
		}

		return getMembersManagerBl().createMember(sess, vo, extSourceName, extSourceType, extSourceLoa, login, candidate, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, User user) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		return this.createMember(sess, vo, user, new ArrayList<>());
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where user " + user + " should be added.");
			}
		}

		// Authorization
		if (groups != null && !groups.isEmpty()) {
			for (Group group: groups) {
				if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_User_List<Group>_policy", vo, group, user)) {
					throw new PrivilegeException("createMember");
				}
			}
		} else {
			if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_User_List<Group>_policy", vo, user)) {
				throw new PrivilegeException("createMember");
			}
		}

		return getMembersManagerBl().createMember(sess, vo, user, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException {
		return this.createMember(sess, vo, extSource, login, new ArrayList<>());
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getExtSourcesManagerBl().checkExtSourceExists(sess, extSource);

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where user with login " + login + " from ExtSource " + extSource + " should be added.");
			}
		}

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_ExtSource_String_List<Group>_policy", Arrays.asList(vo, extSource))) {
			//also group admin of all affected groups is ok
			if (groups != null && !groups.isEmpty()) {
				for (Group group: groups) {
					if (!AuthzResolver.authorizedInternal(sess, "createMember_Vo_ExtSource_String_List<Group>_policy", group)) {
						throw new PrivilegeException(sess, "createMember - from login and extSource");
					}
				}
				//ExtSource has to be assigned to at least one of the groups
				boolean groupContainsExtSource = groups.stream()
					.map(group -> getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group))
					.anyMatch(extSources -> extSources.contains(extSource));
				if (!groupContainsExtSource) {
					throw new PrivilegeException(sess, "createMember - from login and extSource");
				}
			} else {
				throw new PrivilegeException(sess, "createMember - from login and extSource");
			}
		}

		// we run async validation
		Member member = getMembersManagerBl().createMember(sess, vo, extSource, login, groups);
		getMembersManagerBl().validateMemberAsync(sess, member);
		return member;
	}

	@Override
	public Member getMemberByUserExtSource(PerunSession sess, Vo vo, UserExtSource uea) throws VoNotExistsException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMemberByUserExtSource_Vo_UserExtSource_policy", Arrays.asList(vo, uea))) {
			throw new PrivilegeException(sess, "getMemberByUserExtSource");
		}

		return getMembersManagerBl().getMemberByUserExtSource(sess, vo, uea);
	}

	@Override
	public Member getMemberById(PerunSession sess, int id) throws MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Member member = getMembersManagerBl().getMemberById(sess, id);

		//  Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMemberById_int_policy", member)) {
			throw new PrivilegeException(sess, "getMemberById");
		}

		return member;
	}

	@Override
	public Member getMemberByUser(PerunSession sess, Vo vo, User user) throws MemberNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMemberByUser_Vo_User_policy", Arrays.asList(vo, user))) {
			throw new PrivilegeException(sess, "getMemberByUser");
		}

		return getMembersManagerBl().getMemberByUser(sess, vo, user);
	}

	@Override
	public List<Member> getMembersByUser(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMembersByUser_User_policy", user)) {
			throw new PrivilegeException(sess, "getMembersByUser");
		}

		return getMembersManagerBl().getMembersByUser(sess, user);
	}

	@Override
	public List<Member> getMembers(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMembers_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getMembers");
		}

		return getMembersManagerBl().getMembers(sess, vo);
	}

	@Override
	public List<Member> getMembers(PerunSession sess, Vo vo, Status status) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMembers_Vo_Status_policy", vo)) {
			throw new PrivilegeException(sess, "getMembers");
		}

		return getMembersManagerBl().getMembers(sess, vo, status);
	}

	@Override
	public RichMember getRichMemberById(PerunSession sess, int id) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, id);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMemberById_int_policy", member)) {
			throw new PrivilegeException(sess, "getRichMemberById");
		}

		return getPerunBl().getMembersManagerBl().getRichMember(sess, member);
	}

	@Override
	public RichMember getRichMemberWithAttributes(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMemberWithAttributes_Member_policy", member)) {
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMemberWithAttributes(sess, member));
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembersWithAttributes_Vo_List<AttributeDefinition>_policy", vo)) {
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo, attrsDef), null, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembersWithAttributes_List<String>_Group_policy", group)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, allowedStatuses, group), group, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembersWithAttributesByNames_Vo_List<String>_policy", vo)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributesByNames");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributesByNames(sess, vo, attrsNames), null, true);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getCompleteRichMembers_Vo_List<String>_policy", vo)) {
			throw new PrivilegeException(sess, "getCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, vo, attrsNames), null, true);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getCompleteRichMembers_Vo_List<String>_List<String>_policy", vo)) {
			throw new PrivilegeException(sess, "getCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, vo, attrsNames, allowedStatuses), null, true);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws PrivilegeException, ParentGroupNotExistsException, GroupNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getCompleteRichMembers_Group_List<String>_boolean_policy", group)) {
			throw new PrivilegeException(sess, "getCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, attrsNames, lookingInParentGroup), group, true);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, Resource resource, List<String> attrsNames, List<String> allowedStatuses) throws AttributeNotExistsException, GroupNotExistsException, ResourceNotExistsException, PrivilegeException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
		perunBl.getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getCompleteRichMembers_Group_Resource_List<String>_List<String>_policy", Arrays.asList(group, resource)))
			throw new PrivilegeException(sess, "getCompleteRichMembers");

		//TODO: method filterOnlyAllowedAttributes can work only with user and member attributes
		//return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, resource, attrsNames, allowedStatuses), true);
		return getMembersManagerBl().getCompleteRichMembers(sess, group, resource, attrsNames, allowedStatuses);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws PrivilegeException, ParentGroupNotExistsException, GroupNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getCompleteRichMembers_Group_List<String>_List<String>_boolean_policy", group)) {
			throw new PrivilegeException(sess, "getCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, attrsNames, allowedStatuses, lookingInParentGroup), group, true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString, boolean onlySponsored) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findCompleteRichMembers_Vo_List<String>_String_policy", vo)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, vo, attrsNames, searchString, onlySponsored), null, true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findCompleteRichMembers_Vo_List<String>_List<String>_String_policy", vo)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, vo, attrsNames, allowedStatuses, searchString), null, true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findCompleteRichMembers_List<String>_List<String>_String_policy")) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		List<RichMember> richMembers = getMembersManagerBl().findCompleteRichMembers(sess, attrsNames, allowedStatuses, searchString);

		Iterator<RichMember> richMemberIter = richMembers.iterator();
		while (richMemberIter.hasNext()) {
			RichMember richMember = richMemberIter.next();

			if (AuthzResolver.authorizedInternal(sess, "filter-findCompleteRichMembers_List<String>_List<String>_String_policy", richMember))
				continue;

			List<Resource> membersResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, richMember);
			boolean found = false;
			for (Resource resource : membersResources) {
				if (AuthzResolver.authorizedInternal(sess, "filter-findCompleteRichMembers_List<String>_List<String>_String_policy", resource)) {
					found = true;
					break;
				}
			}
			if (found) continue;

			richMemberIter.remove();
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, richMembers, null, false);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findCompleteRichMembers_Group_List<String>_String_boolean_policy", group)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, group, attrsNames, searchString, lookingInParentGroup), group, true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findCompleteRichMembers_Group_List<String>_List<String>_String_boolean_policy", group)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, group, attrsNames, allowedStatuses, searchString, lookingInParentGroup), group, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws PrivilegeException, GroupNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembersWithAttributesByNames_Group_List<String>_policy", group)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributesByNames");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributesByNames(sess, group, attrsNames), group, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembersWithAttributes_Group_List<AttributeDefinition>_policy", group)) {
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, group, attrsDef), group, true);
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembers_Group_policy", group)) {
			throw new PrivilegeException(sess, "getRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, group), group, true);
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembers_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, vo), null, true);
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembers_Vo_Status_policy", vo)) {
			throw new PrivilegeException(sess, "getRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, vo, status), null, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembersWithAttributes_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo), null, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getRichMembersWithAttributes_Vo_Status_policy", vo)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo, status), null, true);
	}

	@Override
	public int getMembersCount(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMembersCount_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getMembersCount");
		}

		return getMembersManagerBl().getMembersCount(sess, vo);
	}

	@Override
	public int getMembersCount(PerunSession sess, Vo vo, Status status) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getMembersCount_Vo_Status_policy", vo)) {
			throw new PrivilegeException(sess, "getMembersCount");
		}

		return getMembersManagerBl().getMembersCount(sess, vo, status);
	}

	@Override
	public Vo getMemberVo(PerunSession sess, Member member) throws MemberNotExistsException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().getMemberVo(sess, member);
	}

	@Override
	public List<Member> findMembersByName(PerunSession sess, String searchString) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findMembersByName_String_policy", Collections.emptyList())) {
			throw new PrivilegeException(sess, "findMembersByName");
		}

		return getMembersManagerBl().findMembersByName(sess, searchString);
	}

	@Override
	public List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findMembersByNameInVo_Vo_String_policy", vo)) {
			throw new PrivilegeException(sess, "findMembersByNameInVo");
		}

		return getMembersManagerBl().findMembersByNameInVo(sess, vo, searchString);
	}

	@Override
	public List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findMembersInVo_Vo_String_policy", vo)) {
			throw new PrivilegeException(sess, "findMembersInVo");
		}

		return getMembersManagerBl().findMembersInVo(sess, vo, searchString);
	}

	@Override
	public List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findMembersInGroup_Group_String_policy", group)) {
			throw new PrivilegeException(sess, "findMembersInGroup");
		}

		return getMembersManagerBl().findMembersInGroup(sess, group, searchString);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findRichMembersWithAttributesInGroup_Group_String_policy", group)) {
			throw new PrivilegeException(sess, "findRichMembersInGroup");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInGroup(sess, group, searchString), group, true);
	}

	@Override
	public List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString) throws PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, getPerunBl().getGroupsManagerBl().getParentGroup(sess, group));

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findMembersInParentGroup_Group_String_policy", group)) {
			throw new PrivilegeException(sess, "findMembersInParentGroup");
		}

		return getMembersManagerBl().findMembersInParentGroup(sess, group, searchString);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) throws PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findRichMembersWithAttributesInParentGroup_Group_String_policy", group)) {
			throw new PrivilegeException(sess, "findRichMembersInParentGroup");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInParentGroup(sess, group, searchString), group, true);
	}

	@Override
	public List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findRichMembersInVo_Vo_String_policy", vo)) {
			throw new PrivilegeException(sess, "findRichMembersInVo");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersInVo(sess, vo, searchString, false), null, true);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findRichMembersWithAttributesInVo_Vo_String_policy", vo)) {
			throw new PrivilegeException(sess, "findRichMembersWithAttributesInVo");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInVo(sess, vo, searchString), null, true);
	}

	@Override
	public Member setStatus(PerunSession sess, Member member, Status status) throws PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException {
		Utils.checkPerunSession(sess);

		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "setStatus_Member_Status_policy", member)) {
			throw new PrivilegeException(sess, "setStatus");
		}

		return getMembersManagerBl().setStatus(sess, member, status);
	}

	@Override
	public void suspendMemberTo(PerunSession sess, Member member, Date suspendedTo) throws MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(suspendedTo, "suspendedTo");

		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "suspendMemberTo_Member_Date_policy", member)) {
			throw new PrivilegeException(sess, "suspendMemberTo");
		}

		membersManagerBl.suspendMemberTo(sess, member, suspendedTo);
	}

	@Override
	public void unsuspendMember(PerunSession sess, Member member) throws MemberNotExistsException, MemberNotSuspendedException, PrivilegeException {
		Utils.checkPerunSession(sess);

		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "unsuspendMember_Member_policy", member)) {
			throw new PrivilegeException(sess, "unsuspendMember");
		}

		membersManagerBl.unsuspendMember(sess, member);
	}

	@Override
	public Member validateMemberAsync(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "validateMemberAsync_Member_policy", member)) {
			throw new PrivilegeException(sess, "validateMemberAsync");
		}

		return getMembersManagerBl().validateMemberAsync(sess, member);
	}

	@Override
	public void extendMembership(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "extendMembership_Member_policy", member)) {
			throw new PrivilegeException(sess, "extendMembership");
		}

		getMembersManagerBl().extendMembership(sess, member);
	}

	@Override
	public boolean canExtendMembership(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "canExtendMembership_Member_policy", member)) {
			throw new PrivilegeException(sess, "extendMembership");
		}

		return getMembersManagerBl().canExtendMembership(sess, member);
	}

	@Override
	public boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws PrivilegeException,
			MemberNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "canExtendMembershipWithReason_Member_policy", member)) {
			throw new PrivilegeException(sess, "canExtendMembershipWithReason");
		}

		return getMembersManagerBl().canExtendMembershipWithReason(sess, member);
	}

	@Override
	public boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) throws VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "canBeMember_Vo_User_String_policy", Arrays.asList(vo, user))) {
			throw new PrivilegeException(sess, "canBeMember");
		}

		return getMembersManagerBl().canBeMember(sess, vo, user, loa);
	}

	@Override
	public boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws
			VoNotExistsException, ExtendMembershipException, PrivilegeException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "canBeMemberWithReason_Vo_User_String_policy", Arrays.asList(vo, user))) {
			throw new PrivilegeException(sess, "canBeMemberWithReason");
		}

		return getMembersManagerBl().canBeMemberWithReason(sess, vo, user, loa);
	}

	@Override
	public Member getMemberByExtSourceNameAndExtLogin(PerunSession sess, Vo vo, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, MemberNotExistsException, UserNotExistsException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extLogin, "extLogin");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getMemberByExtSourceNameAndExtLogin_Vo_String_String_policy", vo)) {
			throw new PrivilegeException(sess, "getMemberByExtSourceNameAndExtLogin");
		}

		return getMembersManagerBl().getMemberByExtSourceNameAndExtLogin(sess, vo, extSourceName, extLogin);
	}

	@Override
	public Date getNewExtendMembership(PerunSession sess, Member member) throws MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().getNewExtendMembership(sess, member);
	}

	@Override
	public Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws VoNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		Utils.notNull(loa, "loa");

		return getMembersManagerBl().getNewExtendMembership(sess, vo, loa);
	}

	@Override
	public void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url, String mailAttributeUrn, String language) throws PrivilegeException, MemberNotExistsException, UserNotExistsException, AttributeNotExistsException {

		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "sendPasswordResetLinkEmail_Member_String_String_String_String_policy", member)) {
			throw new PrivilegeException(sess, "sendPasswordResetLinkEmail");
		}

		//check if attribute exists, throws AttributeNotExistsException
		Attribute mailAttribute = null;
		AttributeDefinition ad = getPerunBl().getAttributesManager().getAttributeDefinition(sess, mailAttributeUrn);


		try {
			if (ad.getEntity().equals("user")) {
				User user = perunBl.getUsersManagerBl().getUserByMember(sess, member);
				mailAttribute = getPerunBl().getAttributesManager().getAttribute(sess, user, mailAttributeUrn);
			}
			if (ad.getEntity().equals("member")) {
				mailAttribute = getPerunBl().getAttributesManager().getAttribute(sess, member, mailAttributeUrn);
			}
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}

		if (mailAttribute == null) {
			throw new InternalErrorException("MailAttribute should not be null.");
		}
		String mailAddress = mailAttribute.valueAsString();

		getMembersManagerBl().sendPasswordResetLinkEmail(sess, member, namespace, url, mailAddress, language);

	}

	@Override
	public RichMember createSponsoredMember(PerunSession session, Vo vo, String namespace, Map<String, String> name,
	                                        String password, String email, User sponsor, LocalDate validityTo)
			throws PrivilegeException, AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException,
			ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException,
			UserNotInRoleException, PasswordStrengthException, InvalidLoginException {
		Utils.checkPerunSession(session);
		Utils.notNull(vo, "vo");
		Utils.notNull(namespace, "namespace");
		if (name.get("guestName") == null) {
			Utils.notNull(name.get("firstName"), "firstName");
			Utils.notNull(name.get("lastName"), "lastName");
		}
		Utils.notNull(password, "password");

		String nameForLog = name.containsKey("guestName") ? name.get("guestName") : name.get("firstName") + " " + name.get("lastName");
		log.info("createSponsoredMember(vo={},namespace='{}',guestName='{}',sponsor={}", vo.getShortName(), namespace, nameForLog, sponsor == null ? "null" : sponsor.getId());

		if (sponsor == null) {
			//sponsor is the caller, authorization is checked in Bl
			sponsor = session.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!AuthzResolver.authorizedInternal(session, "createSponsoredMember_Vo_String_Map<String_String>_String_User_LocalDate_policy", Arrays.asList(vo, sponsor))) {
				throw new PrivilegeException(session, "createSponsoredMember");
			}
		}
		//create the sponsored member
		return membersManagerBl.getRichMemberWithAttributes(session, membersManagerBl.createSponsoredMember(session, vo, namespace, name, password, email, sponsor, validityTo, true));
	}

	@Override
	public RichMember setSponsoredMember(PerunSession session, Vo vo, User userToBeSponsored, String namespace,
	                                     String password, User sponsor, LocalDate validityTo)
		throws PrivilegeException, AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException,
		ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException,
		UserNotInRoleException, PasswordStrengthException, InvalidLoginException {

		Utils.checkPerunSession(session);
		Utils.notNull(vo, "vo");
		Utils.notNull(userToBeSponsored, "userToBeSponsored");
		Utils.notNull(namespace, "namespace");
		Utils.notNull(password, "password");

		log.debug("setSponsoredMember(vo={},namespace='{}',displayName='{}',sponsor={}", vo.getShortName(), namespace, userToBeSponsored.getFirstName() + " " + userToBeSponsored.getLastName(), sponsor == null ? "null" : sponsor.getId());

		if (sponsor == null) {
			//sponsor is the caller, authorization is checked in Bl
			sponsor = session.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!AuthzResolver.authorizedInternal(session, "setSponsoredMember_Vo_User_String_String_User_LocalDate_policy", vo, sponsor)) {
				throw new PrivilegeException(session, "setSponsoredMember");
			}
		}
		//create the sponsored member
		return membersManagerBl.getRichMember(session, membersManagerBl.setSponsoredMember(session, vo, userToBeSponsored, namespace, password, sponsor, validityTo, true));
	}

	@Override
	public Map<String, Map<String, String>> createSponsoredMembers(PerunSession session, Vo vo, String namespace, List<String> names, String email, User sponsor, LocalDate validityTo) throws PrivilegeException {
		Utils.checkPerunSession(session);
		Utils.notNull(vo, "vo");
		Utils.notNull(namespace, "namespace");
		Utils.notNull(names, "names");

		if (sponsor == null) {
			//sponsor is the caller, authorization is checked in Bl
			sponsor = session.getPerunPrincipal().getUser();
		} else {
			//Authorization
			if (!AuthzResolver.authorizedInternal(session, "createSponsoredMembers_Vo_String_List<String>_User_policy", Arrays.asList(vo, sponsor))) {
				throw new PrivilegeException(session, "createSponsoredMembers");
			}
		}

		// create sponsored members
		return membersManagerBl.createSponsoredMembers(session, vo, namespace, names, email, sponsor, validityTo, true);
	}

	@Override
	public RichMember setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor, LocalDate validityTo) throws MemberNotExistsException, AlreadySponsoredMemberException, UserNotInRoleException, PrivilegeException {
		Utils.checkPerunSession(session);
		getPerunBl().getMembersManagerBl().checkMemberExists(session, sponsoredMember);

		if (sponsor == null) {
			//sponsor is the caller
			sponsor = session.getPerunPrincipal().getUser();
		}

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "setSponsorshipForMember_Member_User_LocalDate_policy", sponsoredMember)) {
			throw new PrivilegeException(session, "setSponsorshipForMember");
		}

		//set member to be sponsored
		return membersManagerBl.getRichMember(session, membersManagerBl.setSponsorshipForMember(session, sponsoredMember, sponsor, validityTo));
	}

	@Override
	public RichMember unsetSponsorshipForMember(PerunSession session, Member sponsoredMember) throws MemberNotExistsException, MemberNotSponsoredException, PrivilegeException {
		Utils.checkPerunSession(session);
		getPerunBl().getMembersManagerBl().checkMemberExists(session, sponsoredMember);

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "unsetSponsorshipForMember_Member_policy", sponsoredMember)) {
			throw new PrivilegeException(session, "unsetSponsorshipForMember");
		}

		//unset sponsorship for member
		return membersManagerBl.getRichMember(session, membersManagerBl.unsetSponsorshipForMember(session, sponsoredMember));
	}

	@Override
	public RichMember sponsorMember(PerunSession session, Member sponsored, User sponsor, LocalDate validityTo) throws PrivilegeException, MemberNotSponsoredException, AlreadySponsorException, UserNotInRoleException {
		Utils.checkPerunSession(session);
		Utils.notNull(sponsored, "sponsored");
		Utils.notNull(sponsor, "sponsor");
		log.debug("sponsorMember(sponsored={},sponsor={}", sponsored.getId(), sponsor.getId());

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "sponsored-sponsorMember_Member_User_LocalDate_policy", sponsored) ||
		    !AuthzResolver.authorizedInternal(session, "sponsor-sponsorMember_Member_User_LocalDate_policy", sponsor)) {
			throw new PrivilegeException(session, "sponsorMember");
		}
		//create the link between sponsored and sponsoring users
		return membersManagerBl.getRichMember(session, membersManagerBl.sponsorMember(session, sponsored, sponsor, validityTo));
	}

	@Override
	public List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo, User user, List<String> attrNames) throws AttributeNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getVosManagerBl().checkVoExists(sess, vo);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);

		if (!AuthzResolver.authorizedInternal(sess, "getSponsoredMembers_Vo_User_List<String>_policy", Arrays.asList(vo, user))) {
			throw new PrivilegeException(sess, "getSponsoredMembers");
		}

		List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		for (String attrName : attrNames) {
			attributeDefinitions.add(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrName));
		}

		//Basic rich Members without attributes
		List<RichMember> richMembers = membersManagerBl.convertMembersToRichMembers(sess, membersManagerBl.getSponsoredMembers(sess, vo, user));
		//Enriched rich members with attributes by list of attributes
		richMembers = membersManagerBl.convertMembersToRichMembersWithAttributes(sess, richMembers, attributeDefinitions);
		//RichMembers with filtered attributes by rights from session
		richMembers = membersManagerBl.filterOnlyAllowedAttributes(sess, richMembers, null, true);

		return richMembers;
	}

	@Override
	public List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo, User user) throws PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(vo, "vo");
		Utils.notNull(user, "user");

		perunBl.getVosManagerBl().checkVoExists(sess, vo);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getSponsoredMembers_Vo_User_policy", Arrays.asList(vo, user))) {
			throw new PrivilegeException(sess, "getSponsoredMembers");
		}

		return membersManagerBl.convertMembersToRichMembers(sess, membersManagerBl.getSponsoredMembers(sess, vo, user));
	}

	@Override
	public List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(vo, "vo");

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getSponsoredMembers_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getSponsoredMembers");
		}

		return membersManagerBl.convertMembersToRichMembers(sess, membersManagerBl.getSponsoredMembers(sess, vo));
	}

	@Override
	public List<MemberWithSponsors> getSponsoredMembersAndTheirSponsors(PerunSession sess, Vo vo, List<String> attrNames) throws VoNotExistsException, PrivilegeException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(vo, "vo");

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		//Authorization
		if(!AuthzResolver.authorizedInternal(sess, "getSponsoredMembersAndTheirSponsors_Vo_policy", vo)) {
			throw new PrivilegeException(sess, "getSponsoredMembersAndTheirSponsors");
		}

		List<AttributeDefinition> attrsDef = new ArrayList<>();
		for (String attrName : attrNames) {
			attrsDef.add(getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, attrName));
		}

		List<RichMember> richMembers = membersManagerBl.convertMembersToRichMembersWithAttributes(sess,
			membersManagerBl.convertMembersToRichMembers(sess, membersManagerBl.getSponsoredMembers(sess, vo)),
			attrsDef);
		richMembers = membersManagerBl.filterOnlyAllowedAttributes(sess, richMembers, null, true);

		return richMembers.stream()
			.filter(member -> AuthzResolver.authorizedInternal(sess, "filter-getSponsoredMembersAndTheirSponsors_Vo_policy", member, vo))
			.map(member -> convertMemberToMemberWithSponsors(sess, member))
			.collect(Collectors.toList());
	}

	@Override
	public String extendExpirationForSponsoredMember(PerunSession sess, Member sponsoredMember, User sponsorUser) throws PrivilegeException, MemberNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(sponsoredMember, "sponsoredMember");
		Utils.notNull(sponsorUser, "sponsorUser");

		perunBl.getMembersManagerBl().checkMemberExists(sess, sponsoredMember);
		perunBl.getUsersManagerBl().checkUserExists(sess, sponsorUser);

		//Authorization
		if (!(AuthzResolver.authorizedInternal(sess, "extendExpirationForSponsoredMember_Member_User_policy", sponsoredMember))) {
			throw new PrivilegeException(sess, "extendExpirationForSponsoredMember");
		}

		return membersManagerBl.extendExpirationForSponsoredMember(sess,sponsoredMember,sponsorUser);
	}

	@Override
	public void removeSponsor(PerunSession sess, Member sponsoredMember, User sponsorToRemove) throws PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(sponsoredMember, "sponsoredMember");
		Utils.notNull(sponsorToRemove, "sponsorToRemove");
		log.info("removeSponsor(sponsoredMember={},sponsorToRemove={}", sponsoredMember.getId(), sponsorToRemove.getId());

		//Get the VO to which sponsoredMember belongs
		Vo vo = membersManagerBl.getMemberVo(sess, sponsoredMember);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "sponsored-removeSponsor_Member_User_policy", sponsoredMember) ||
		    !AuthzResolver.authorizedInternal(sess, "sponsor-removeSponsor_Member_User_policy", sponsorToRemove)) {
			throw new PrivilegeException(sess, "removeSponsor");
		}

		//Check that sponsoring user has a role SPONSOR for the VO
		if (!getPerunBl().getVosManagerBl().isUserInRoleForVo(sess, sponsorToRemove, Role.SPONSOR, vo, true)) {
			throw new PrivilegeException(sess, "user " + sponsorToRemove.getId() + " is not in role SPONSOR for VO " + vo.getId());
		}
		//remove sponsor
		membersManagerBl.removeSponsor(sess,sponsoredMember, sponsorToRemove);
	}

	@Override
	public void updateSponsorshipValidity(PerunSession sess, Member sponsoredMember, User sponsor,
	                                      LocalDate newValidity)
			throws PrivilegeException, SponsorshipDoesNotExistException, MemberNotExistsException,
			       UserNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(sponsoredMember, "sponsoredMember");
		Utils.notNull(sponsor, "sponsor");

		perunBl.getMembersManagerBl().checkMemberExists(sess, sponsoredMember);
		perunBl.getUsersManagerBl().checkUserExists(sess, sponsor);

		Vo memberVo;
		try {
			memberVo = perunBl.getVosManagerBl().getVoById(sess, sponsoredMember.getVoId());
		} catch (VoNotExistsException e) {
			throw new InternalErrorException(e);
		}

		if (!AuthzResolver.authorizedInternal(sess, "updateSponsorshipValidity_Member_User_LocalDate", memberVo,
				sponsor)) {
			throw new PrivilegeException("updateSponsorshipValidity");
		}

		membersManagerBl.updateSponsorshipValidity(sess, sponsoredMember, sponsor, newValidity);
	}

	/**
	 * Converts member to member with sponsors and sets all his sponsors.
	 *
	 * @param sess perun session
	 * @param member sponsored member
	 * @return member with sponsors
	 */
	private MemberWithSponsors convertMemberToMemberWithSponsors(PerunSession sess, RichMember member) {
		MemberWithSponsors memberWithSponsors = new MemberWithSponsors(member);

		List<Sponsor> sponsors = getPerunBl().getUsersManagerBl().getSponsors(sess, member).stream()
				.map(user -> membersManagerBl.convertUserToSponsor(sess, user, member))
				.collect(Collectors.toList());
		memberWithSponsors.setSponsors(sponsors);

		return memberWithSponsors;
	}

	/**
	 * Gets the membersManagerBl for this instance.
	 *
	 * @return The membersManagerBl.
	 */
	public MembersManagerBl getMembersManagerBl() {
		return this.membersManagerBl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	/**
	 * Sets the membersManagerBl for this instance.
	 *
	 * @param membersManagerBl The membersManagerBl.
	 */
	public void setMembersManagerBl(MembersManagerBl membersManagerBl) {
		this.membersManagerBl = membersManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}
}
