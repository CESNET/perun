package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
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
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSponsoredException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSuspendedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthFailedException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
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

import java.util.ArrayList;
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
	public void deleteMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "deleteMember");
		}


		getMembersManagerBl().deleteMember(sess, member);
	}

	@Override
	public void deleteMembers(PerunSession sess, List<Member> members) throws InternalErrorException, MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		for (Member member : members) {
			getMembersManagerBl().checkMemberExists(sess, member);

			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
				throw new PrivilegeException(sess, "deleteMembers");
			}
		}

		getMembersManagerBl().deleteMembers(sess, members);
	}

	@Override
	public void deleteAllMembers(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException, PrivilegeException, MemberAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "deleteAllMembers");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		getMembersManagerBl().deleteAllMembers(sess, vo);
	}

	@Override
	public Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException {
		return this.createSpecificMember(sess, vo, candidate, specificUserOwners, specificUserType, null);
	}

	@Override
	public Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(specificUserType, "specificUserType");

		//normal type is not allowed when creating specific member
		if (specificUserType.equals(SpecificUserType.NORMAL))
			throw new InternalErrorException("Type of specific user must be defined.");

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createSpecificMember (Specific User) - from candidate");
		}
		Utils.notNull(candidate, "candidate");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		if (specificUserOwners.isEmpty())
			throw new InternalErrorException("List of specificUserOwners of " + candidate + " can't be empty.");

		for (User u : specificUserOwners) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, u);
		}

		return getMembersManagerBl().createSpecificMember(sess, vo, candidate, specificUserOwners, specificUserType, groups);
	}

	@Override
	@Deprecated
	public Member createSponsoredAccount(PerunSession sess, Map<String, String> params, String namespace, ExtSource extSource, String extSourcePostfix, Vo vo, int loa) throws InternalErrorException, PrivilegeException, UserNotExistsException, ExtSourceNotExistsException, UserExtSourceNotExistsException, WrongReferenceAttributeValueException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, AlreadyMemberException, PasswordStrengthFailedException, PasswordOperationTimeoutException, WrongAttributeValueException {
		Utils.checkPerunSession(sess);
		Utils.notNull(extSource, "extSource");
		Utils.notNull(namespace, "namespace");
		Utils.notNull(vo, "vo");
		Utils.notNull(extSourcePostfix, "extSourcePostfix");

		if (!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "createSponsoredAccount");
		}

		if (params.containsKey("sponsor")) {
			String sponsorLogin = params.get("sponsor");
			User owner = getPerunBl().getUsersManager().getUserByExtSourceNameAndExtLogin(sess, extSource.getName(), sponsorLogin + extSourcePostfix);

			return getPerunBl().getMembersManagerBl().createSponsoredAccount(sess, params, namespace, extSource, extSourcePostfix, owner, vo, loa);
		} else {
			throw new InternalErrorException("sponsor cannot be null");
		}
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		return this.createMember(sess, vo, candidate, new ArrayList<>());
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from candidate");
		}

		Utils.notNull(candidate, "candidate");

		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().createMember(sess, vo, candidate, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		return this.createMember(sess, vo, extSourceName, extSourceType, login, candidate, null);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from candidate");
		}

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

		return getMembersManagerBl().createMember(sess, vo, extSourceName, extSourceType, login, candidate, groups);
	}


	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkMaxLength("TitleBefore", candidate.getTitleBefore(), 40);
		Utils.checkMaxLength("TitleAfter", candidate.getTitleAfter(), 40);

		return this.createMember(sess, vo, extSourceName, extSourceType, extSourceLoa, login, candidate, null);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from candidate");
		}

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

		return getMembersManagerBl().createMember(sess, vo, extSourceName, extSourceType, extSourceLoa, login, candidate, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		return this.createMember(sess, vo, user, new ArrayList<>());
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from user");
		}

		// if any group is not from the vo, throw an exception
		if (groups != null) {
			for (Group group : groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if (group.getVoId() != vo.getId())
					throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where user " + user + " should be added.");
			}
		}

		return getMembersManagerBl().createMember(sess, vo, user, groups);
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException {
		return this.createMember(sess, vo, extSource, login, new ArrayList<>());
	}

	@Override
	public Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException {
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

		// Authorization for vo admin and perun admin automatic
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			//also group admin of all affected groups is ok
			if (groups != null && !groups.isEmpty()) {
				boolean groupAdminOfAllGroups = true;
				boolean authorizedToExtSource = false;
				for (Group group : groups) {
					//User in session has to be GroupAdmin of all affected groups
					if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
						groupAdminOfAllGroups = false;
						break;
					}
					//User in session has to have at least one right to work with the ExtSource
					List<ExtSource> groupExtSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group);
					if (groupExtSources.contains(extSource)) authorizedToExtSource = true;
				}

				if (!groupAdminOfAllGroups || !authorizedToExtSource) {
					throw new PrivilegeException(sess, "createMember - from login and extSource -- authorized to extSource=" + authorizedToExtSource + " and groupAdmin in all groups=" + groupAdminOfAllGroups);
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
	public Member getMemberByUserExtSource(PerunSession sess, Vo vo, UserExtSource uea) throws InternalErrorException, VoNotExistsException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMemberByUserExtSource");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().getMemberByUserExtSource(sess, vo, uea);
	}

	@Override
	public Member getMemberById(PerunSession sess, int id) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Member member = getMembersManagerBl().getMemberById(sess, id);
		//  Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, member) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMemberById");
		}

		return member;
	}

	@Override
	public Member getMemberByUser(PerunSession sess, Vo vo, User user) throws InternalErrorException, MemberNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMemberByUser");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		return getMembersManagerBl().getMemberByUser(sess, vo, user);
	}

	@Override
	public List<Member> getMembersByUser(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMembersByUser");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		return getMembersManagerBl().getMembersByUser(sess, user);
	}

	@Override
	public List<Member> getMembers(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMembers");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().getMembers(sess, vo);
	}

	@Override
	public List<Member> getMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMembers");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().getMembers(sess, vo, status);
	}

	@Override
	public RichMember getRichMemberById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, id);
		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMemberById");
		}

		return getPerunBl().getMembersManagerBl().getRichMember(sess, member);
	}

	@Override
	public RichMember getRichMemberWithAttributes(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMemberWithAttributes(sess, member));
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo, attrsDef), null, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
		}

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, allowedStatuses, group), group, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributesByNames");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributesByNames(sess, vo, attrsNames), null, true);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, vo, attrsNames), null, true);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, vo, attrsNames, allowedStatuses), null, true);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, ParentGroupNotExistsException, GroupNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, attrsNames, lookingInParentGroup), group, true);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, Resource resource, List<String> attrsNames, List<String> allowedStatuses) throws InternalErrorException, AttributeNotExistsException, GroupNotExistsException, ResourceNotExistsException, PrivilegeException, GroupResourceMismatchException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
		perunBl.getResourcesManagerBl().checkResourceExists(sess, resource);

		// Authorization (only engine and PerunAdmin, because we are not able to filter member-resource and user-facility attributes properly)
		if (!AuthzResolver.isAuthorized(sess, Role.ENGINE) &&
			!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER))
			throw new PrivilegeException(sess, "getCompleteRichMembers");

		//TODO: method filterOnlyAllowedAttributes can work only with user and member attributes
		//return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, resource, attrsNames, allowedStatuses), true);
		return getMembersManagerBl().getCompleteRichMembers(sess, group, resource, attrsNames, allowedStatuses);
	}

	@Override
	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, ParentGroupNotExistsException, GroupNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, attrsNames, allowedStatuses, lookingInParentGroup), group, true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, vo, attrsNames, searchString), null, true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, vo, attrsNames, allowedStatuses, searchString), null, true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		List<RichMember> richMembers = getMembersManagerBl().findCompleteRichMembers(sess, attrsNames, allowedStatuses, searchString);

		Iterator<RichMember> richMemberIter = richMembers.iterator();
		while (richMemberIter.hasNext()) {
			RichMember richMember = richMemberIter.next();
			//Vo object which will be used only for authorization contains only id of richMember's Vo.
			Vo membersVo = new Vo(richMember.getVoId(), "", "");

			//if voadmin or voobserver or groupadmin or perunobserver has right to this member, its ok
			if (AuthzResolver.isAuthorized(sess, Role.VOADMIN, membersVo) ||
					AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, membersVo) ||
					AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, membersVo) ||
					AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) continue;

			//if not, then try facility admin rights
			List<Resource> membersResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, richMember);
			boolean found = false;
			for (Resource resource : membersResources) {
				if (AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
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
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, group, attrsNames, searchString, lookingInParentGroup), group, true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, group, attrsNames, allowedStatuses, searchString, lookingInParentGroup), group, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributesByNames");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributesByNames(sess, group, attrsNames), group, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, group, attrsDef), group, true);
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMembers");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, group), group, true);
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMembers");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, vo), null, true);
	}

	@Override
	public List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMembers");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, vo, status), null, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo), null, true);
	}

	@Override
	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo, status), null, true);
	}

	@Override
	public int getMembersCount(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMembersCount");
		}

		return getMembersManagerBl().getMembersCount(sess, vo);
	}

	@Override
	public int getMembersCount(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMembersCount");
		}

		return getMembersManagerBl().getMembersCount(sess, vo, status);
	}

	@Override
	public Vo getMemberVo(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().getMemberVo(sess, member);
	}

	@Override
	public List<Member> findMembersByName(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findMembersByName");
		}

		return getMembersManagerBl().findMembersByName(sess, searchString);
	}

	@Override
	public List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findMembersByNameInVo");
		}

		return getMembersManagerBl().findMembersByNameInVo(sess, vo, searchString);
	}

	@Override
	public List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findMembersInVo");
		}

		return getMembersManagerBl().findMembersInVo(sess, vo, searchString);
	}

	@Override
	public List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findMembersInGroup");
		}

		return getMembersManagerBl().findMembersInGroup(sess, group, searchString);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findRichMembersInGroup");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInGroup(sess, group, searchString), group, true);
	}

	@Override
	public List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, getPerunBl().getGroupsManagerBl().getParentGroup(sess, group));

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findMembersInParentGroup");
		}

		return getMembersManagerBl().findMembersInParentGroup(sess, group, searchString);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findRichMembersInParentGroup");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInParentGroup(sess, group, searchString), group, true);
	}

	@Override
	public List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findRichMembersInVo");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersInVo(sess, vo, searchString), null, true);
	}

	@Override
	public List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "findRichMembersWithAttributesInVo");
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInVo(sess, vo, searchString), null, true);
	}

	@Override
	public Member setStatus(PerunSession sess, Member member, Status status) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "setStatus");
		}

		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().setStatus(sess, member, status);
	}

	@Override
	public Member setStatus(PerunSession sess, Member member, Status status, String message) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "setStatus");
		}

		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().setStatus(sess, member, status, message);
	}

	@Override
	public void suspendMemberTo(PerunSession sess, Member member, Date suspendedTo) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(suspendedTo, "suspendedTo");

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "suspendMemberTo");
		}

		getMembersManagerBl().checkMemberExists(sess, member);

		membersManagerBl.suspendMemberTo(sess, member, suspendedTo);
	}

	@Override
	public void unsuspendMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, MemberNotSuspendedException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "unsuspendMember");
		}

		getMembersManagerBl().checkMemberExists(sess, member);
		if(member.getSuspendedTo() == null) throw new MemberNotSuspendedException(member);

		membersManagerBl.unsuspendMember(sess, member);
	}

	@Override
	public Member validateMemberAsync(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "validateMemberAsync");
		}


		return getMembersManagerBl().validateMemberAsync(sess, member);
	}

	@Override
	public void extendMembership(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "extendMembership");
		}

		getMembersManagerBl().extendMembership(sess, member);
	}

	@Override
	public boolean canExtendMembership(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "extendMembership");
		}

		return getMembersManagerBl().canExtendMembership(sess, member);
	}

	@Override
	public boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException,
			MemberNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "canExtendMembershipWithReason");
		}

		return getMembersManagerBl().canExtendMembershipWithReason(sess, member);
	}

	@Override
	public boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().canBeMember(sess, vo, user, loa);
	}

	@Override
	public boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException,
		VoNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().canBeMemberWithReason(sess, vo, user, loa);
	}

	@Override
	public Member getMemberByExtSourceNameAndExtLogin(PerunSession sess, Vo vo, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, MemberNotExistsException, UserNotExistsException, InternalErrorException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extLogin, "extLogin");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		if(!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getMemberByExtSourceNameAndExtLogin");
		}

		return getMembersManagerBl().getMemberByExtSourceNameAndExtLogin(sess, vo, extSourceName, extLogin);
	}

	@Override
	public Date getNewExtendMembership(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().getNewExtendMembership(sess, member);
	}

	@Override
	public Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws InternalErrorException, VoNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		Utils.notNull(loa, "loa");

		return getMembersManagerBl().getNewExtendMembership(sess, vo, loa);
	}

	@Override
	public void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url, String mailAttributeUrn, String language) throws InternalErrorException, PrivilegeException, MemberNotExistsException, UserNotExistsException, AttributeNotExistsException {

		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
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
	public RichMember createSponsoredMember(PerunSession session, Vo vo, String namespace, String guestName, String password, User sponsor)
			throws InternalErrorException, PrivilegeException, AlreadyMemberException,
			LoginNotExistsException, PasswordCreationFailedException,
		ExtendMembershipException,
			WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException, UserNotInRoleException {
		Utils.checkPerunSession(session);
		Utils.notNull(vo, "vo");
		Utils.notNull(namespace, "namespace");
		Utils.notNull(guestName, "guestName");
		Utils.notNull(password, "password");
		log.info("createSponsoredMember(vo={},namespace='{}',guestName='{}',sponsor={}", vo.getShortName(), namespace, guestName, sponsor == null ? "null" : sponsor.getId());

		if (sponsor == null) {
			//sponsor is the caller
			sponsor = session.getPerunPrincipal().getUser();
		} else {
			//sponsor is specified, caller must be in role REGISTRAR
			if (!AuthzResolver.isAuthorized(session, Role.REGISTRAR)) {
				throw new PrivilegeException(session, "createSponsoredMember must be called by REGISTRAR");
			}
		}
		//create the sponsored member
		return membersManagerBl.getRichMember(session, membersManagerBl.createSponsoredMember(session, vo, namespace, guestName, password, sponsor, true));
	}

	@Override
	public RichMember setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor) throws InternalErrorException, MemberNotExistsException, AlreadySponsoredMemberException, UserNotInRoleException, PrivilegeException {
		Utils.checkPerunSession(session);
		getPerunBl().getMembersManagerBl().checkMemberExists(session, sponsoredMember);

		if (sponsor == null) {
			//sponsor is the caller
			sponsor = session.getPerunPrincipal().getUser();
		}

		//only Perun Admin should has rights to do this operation
		if (!AuthzResolver.isAuthorized(session, Role.PERUNADMIN)) {
			throw new PrivilegeException(session, "Only PerunAdmin should have rights to call this method.");
		}

		//set member to be sponsored
		return membersManagerBl.getRichMember(session, membersManagerBl.setSponsorshipForMember(session, sponsoredMember, sponsor));
	}

	@Override
	public RichMember unsetSponsorshipForMember(PerunSession session, Member sponsoredMember) throws MemberNotExistsException, MemberNotSponsoredException, InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(session);
		getPerunBl().getMembersManagerBl().checkMemberExists(session, sponsoredMember);

		//only Perun Admin should has rights to do this operation
		if (!AuthzResolver.isAuthorized(session, Role.PERUNADMIN)) {
			throw new PrivilegeException(session, "Only PerunAdmin should have rights to call this method.");
		}

		//unset sponsorship for member
		return membersManagerBl.getRichMember(session, membersManagerBl.unsetSponsorshipForMember(session, sponsoredMember));
	}

	@Override
	public RichMember sponsorMember(PerunSession session, Member sponsored, User sponsor) throws InternalErrorException, PrivilegeException, MemberNotSponsoredException, AlreadySponsorException, UserNotInRoleException {
		Utils.checkPerunSession(session);
		Utils.notNull(sponsored, "sponsored");
		Utils.notNull(sponsor, "sponsor");
		log.debug("sponsorMember(sponsored={},sponsor={}", sponsored.getId(), sponsor.getId());

		//Get the VO to which the member belongs
		Vo vo = membersManagerBl.getMemberVo(session, sponsored);

		//Check if the caller is authorised to add sponsor
		if (!AuthzResolver.isAuthorized(session, Role.VOADMIN)) {
			throw new PrivilegeException(session, "sponsorMember must be called by VOADMIN");
		}
		//create the link between sponsored and sponsoring users
		return membersManagerBl.getRichMember(session, membersManagerBl.sponsorMember(session, sponsored, sponsor));
	}

	@Override
	public List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo, User user, List<String> attrNames) throws InternalErrorException, AttributeNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);
		perunBl.getVosManagerBl().checkVoExists(sess, vo);
		perunBl.getUsersManagerBl().checkUserExists(sess, user);

		if (!AuthzResolver.isAuthorized(sess, Role.REGISTRAR) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
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
	public List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo, User user) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(vo, "vo");
		Utils.notNull(user, "user");
		if (!AuthzResolver.isAuthorized(sess, Role.REGISTRAR) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getSponsoredMembers must be called by REGISTRAR or VOADMIN");
		}
		return membersManagerBl.convertMembersToRichMembers(sess, membersManagerBl.getSponsoredMembers(sess, vo, user));
	}

	@Override
	public List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(vo, "vo");
		if(!AuthzResolver.isAuthorized(sess, Role.REGISTRAR) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getSponsoredMembers");
		}
		return membersManagerBl.convertMembersToRichMembers(sess, membersManagerBl.getSponsoredMembers(sess, vo));
	}

	@Override
	public String extendExpirationForSponsoredMember(PerunSession sess, Member sponsoredMember, User sponsorUser) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(sponsoredMember, "sponsoredMember");
		Utils.notNull(sponsorUser, "sponsorUser");
		if (!(AuthzResolver.isAuthorized(sess, Role.REGISTRAR)||AuthzResolver.isAuthorized(sess, Role.VOADMIN, membersManagerBl.getMemberVo(sess,sponsoredMember)))) {
			throw new PrivilegeException(sess, "extendExpirationForSponsoredMember must be called by REGISTRAR or VOADMIN");
		}
		return membersManagerBl.extendExpirationForSponsoredMember(sess,sponsoredMember,sponsorUser);
	}

	@Override
	public void removeSponsor(PerunSession sess, Member sponsoredMember, User sponsorToRemove) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		Utils.notNull(sponsoredMember, "sponsoredMember");
		Utils.notNull(sponsorToRemove, "sponsorToRemove");
		log.info("removeSponsor(sponsoredMember={},sponsorToRemove={}", sponsoredMember.getId(), sponsorToRemove.getId());

		//Get the VO to which sponsoredMember belongs
		Vo vo = membersManagerBl.getMemberVo(sess, sponsoredMember);

		//Check if the caller is authorised to remove sponsor
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "removeSponsor must be called by VOADMIN");
		}
		//Check that sponsoring user has a role SPONSOR for the VO
		if (!getPerunBl().getVosManagerBl().isUserInRoleForVo(sess, sponsorToRemove, Role.SPONSOR, vo, true)) {
			throw new PrivilegeException(sess, "user " + sponsorToRemove.getId() + " is not in role SPONSOR for VO " + vo.getId());
		}
		//remove sponsor
		membersManagerBl.removeSponsor(sess,sponsoredMember, sponsorToRemove);
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
