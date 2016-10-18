package cz.metacentrum.perun.core.entry;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.GroupOperationsException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthFailedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Slavek Licehammer glory@ics.muni.cz
 */
public class MembersManagerEntry implements MembersManager {

	final static Logger log = LoggerFactory.getLogger(MembersManagerEntry.class);

	private MembersManagerBl membersManagerBl;
	private PerunBl perunBl;

	/**
	 * Constructor.
	 *
	 */
	public MembersManagerEntry(PerunBl perunBl) {
		this.perunBl = perunBl;
		this.membersManagerBl = perunBl.getMembersManagerBl();
	}

	public MembersManagerEntry() {
	}

	public void deleteMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException, GroupOperationsException {
		Utils.checkPerunSession(sess);

		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "deleteMember");
		}


		getMembersManagerBl().deleteMember(sess, member);
	}

	public void deleteAllMembers(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException, PrivilegeException, MemberAlreadyRemovedException, GroupOperationsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "deleteAllMembers");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		getMembersManagerBl().deleteAllMembers(sess, vo);
	}

	public Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		return this.createSpecificMember(sess, vo, candidate, specificUserOwners, specificUserType, null);
	}

	public Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		Utils.checkPerunSession(sess);
		Utils.notNull(specificUserType, "specificUserType");

		//normal type is not allowed when creating specific member
		if(specificUserType.equals(SpecificUserType.NORMAL)) throw new InternalErrorException("Type of specific user must be defined.");

		// if any group is not from the vo, throw an exception
		if(groups != null) {
			for(Group group: groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if(group.getVoId() != vo.getId()) throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createSpecificMember (Specific User) - from candidate");
		}
		Utils.notNull(candidate, "candidate");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		if(specificUserOwners.isEmpty()) throw new InternalErrorException("List of specificUserOwners of " + candidate + " can't be empty.");

		for(User u: specificUserOwners) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, u);
		}

		return getMembersManagerBl().createSpecificMember(sess, vo, candidate, specificUserOwners, specificUserType, groups);
	}

	public Member createSponsoredAccount(PerunSession sess, Map<String, String> params, String namespace, ExtSource extSource, String extSourcePostfix, Vo vo, int loa) throws InternalErrorException, PrivilegeException, UserNotExistsException, ExtSourceNotExistsException, UserExtSourceNotExistsException, WrongReferenceAttributeValueException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, AlreadyMemberException, GroupOperationsException, PasswordStrengthFailedException, PasswordOperationTimeoutException, WrongAttributeValueException {
		Utils.checkPerunSession(sess);
		Utils.notNull(extSource, "extSource");
		Utils.notNull(namespace, "namespace");
		Utils.notNull(vo, "vo");
		Utils.notNull(extSourcePostfix, "extSourcePostfix");

		if (!AuthzResolver.isAuthorized(sess, Role.REGISTRAR)) {
			throw new PrivilegeException(sess, "createSponsoredAccount");
		}

		if(params.containsKey("sponsor")) {
			String sponsorLogin = params.get("sponsor");
			User owner = getPerunBl().getUsersManager().getUserByExtSourceNameAndExtLogin(sess, extSource.getName(), sponsorLogin + extSourcePostfix);

			return getPerunBl().getMembersManagerBl().createSponsoredAccount(sess, params, namespace, extSource, extSourcePostfix, owner, vo, loa);
		} else {
			throw new InternalErrorException("sponsor cannot be null");
		}
	}

	public Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		return this.createMember(sess, vo, candidate, new ArrayList<Group>());
	}

	public Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		Utils.checkPerunSession(sess);

		// if any group is not from the vo, throw an exception
		if(groups != null) {
			for(Group group: groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if(group.getVoId() != vo.getId()) throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from candidate");
		}

		Utils.notNull(candidate, "candidate");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().createMember(sess, vo, candidate, groups);
	}

	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		return this.createMember(sess, vo, extSourceName, extSourceType, login, candidate, null);
	}

	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from candidate");
		}

		// if any group is not from the vo, throw an exception
		if(groups != null) {
			for(Group group: groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if(group.getVoId() != vo.getId()) throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extSourceType, "extSourceType");
		Utils.notNull(login, "login");

		return getMembersManagerBl().createMember(sess, vo, extSourceName, extSourceType, login, candidate, groups);
	}


	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		return this.createMember(sess, vo, extSourceName, extSourceType, extSourceLoa, login, candidate, null);
	}

	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from candidate");
		}

		// if any group is not from the vo, throw an exception
		if(groups != null) {
			for(Group group: groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if(group.getVoId() != vo.getId()) throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where candidate " + candidate + " should be added.");
			}
		}

		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extSourceType, "extSourceType");
		Utils.notNull(login, "login");

		return getMembersManagerBl().createMember(sess, vo, extSourceName, extSourceType, extSourceLoa, login, candidate, groups);
	}

	public Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		return this.createMember(sess, vo, user, new ArrayList<Group>());
	}

	public Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from user");
		}

		// if any group is not from the vo, throw an exception
		if(groups != null) {
			for(Group group: groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if(group.getVoId() != vo.getId()) throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where user " + user + " should be added.");
			}
		}

		return getMembersManagerBl().createMember(sess, vo, user, groups);
	}

	public Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException, GroupOperationsException {
		return this.createMember(sess, vo, extSource, login, new ArrayList<Group>());
	}

	public Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException, GroupOperationsException {
		Utils.checkPerunSession(sess);
		
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getExtSourcesManagerBl().checkExtSourceExists(sess, extSource);

		// if any group is not from the vo, throw an exception
		if(groups != null) {
			for(Group group: groups) {
				perunBl.getGroupsManagerBl().checkGroupExists(sess, group);
				if(group.getVoId() != vo.getId()) throw new InternalErrorException("Group " + group + " is not from the vo " + vo + " where user with login " + login + " from ExtSource " + extSource + " should be added.");
			}
		}

		// Authorization for vo admin and perun admin automatic
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			//also group admin of all affected groups is ok
			if(groups != null && !groups.isEmpty()) {
				boolean groupAdminOfAllGroups = true;
				boolean authorizedToExtSource = false;
				for(Group group: groups) {
					//User in session has to be GroupAdmin of all affected groups
					if(!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
						groupAdminOfAllGroups = false;
						break;
					}
					//User in session has to have at least one right to work with the ExtSource
					List<ExtSource> groupExtSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group);
					if(groupExtSources.contains(extSource)) authorizedToExtSource = true;
				}

				if(!groupAdminOfAllGroups || !authorizedToExtSource) {
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

	public Member getMemberByUserExtSource(PerunSession sess, Vo vo, UserExtSource uea) throws InternalErrorException, VoNotExistsException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "getMemberByUserExtSource");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().getMemberByUserExtSource(sess, vo, uea);
	}

	public Member getMemberByUserExtSources(PerunSession sess, Vo vo, List<UserExtSource> ueas) throws InternalErrorException, MemberNotExistsException, VoNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER)) {
			throw new PrivilegeException(sess, "getMemberByUserExtSources");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().getMemberByUserExtSources(sess, vo, ueas);
	}

	public Member getMemberById(PerunSession sess, int id) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
		Utils.checkPerunSession(sess);

		Member member = getMembersManagerBl().getMemberById(sess, id);
		//  Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, member) &&
				!AuthzResolver.isAuthorized(sess, Role.RPC)) {
			throw new PrivilegeException(sess, "getMemberById");
				}

		return member;
	}

	public Member getMemberByUser(PerunSession sess, Vo vo, User user) throws InternalErrorException, MemberNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getMemberByUser");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		return getMembersManagerBl().getMemberByUser(sess, vo, user);
	}

	public List<Member> getMembersByUser(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF, user)) {
			throw new PrivilegeException(sess, "getMembersByUser");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

		return getMembersManagerBl().getMembersByUser(sess, user);
	}

	public List<Member> getMembers(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getMembers");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().getMembers(sess, vo);
	}

	public List<Member> getMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getMembers");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().getMembers(sess, vo, status);
	}

	public RichMember getRichMemberById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		Member member = getPerunBl().getMembersManagerBl().getMemberById(sess, id);
		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getRichMemberById");
				}

		return getPerunBl().getMembersManagerBl().getRichMember(sess, member);
	}

	public RichMember getRichMemberWithAttributes(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getMembersManagerBl().checkMemberExists(sess, member);

		Vo vo = getPerunBl().getMembersManagerBl().getMemberVo(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMemberWithAttributes(sess, member));
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)){
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo, attrsDef), true);
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
				}

		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, allowedStatuses, group), true);
	}

	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)){
			throw new PrivilegeException(sess, "getRichMembersWithAttributesByNames");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributesByNames(sess, vo, attrsNames), true);
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)){
			throw new PrivilegeException(sess, "getCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, vo, attrsNames), true);
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)){
			throw new PrivilegeException(sess, "getCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, vo, attrsNames, allowedStatuses), true);
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, ParentGroupNotExistsException, GroupNotExistsException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)){
			throw new PrivilegeException(sess, "getCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, attrsNames, lookingInParentGroup), true);
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, ParentGroupNotExistsException, GroupNotExistsException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)){
			throw new PrivilegeException(sess, "getCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, attrsNames, allowedStatuses, lookingInParentGroup), true);
	}

	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, vo, attrsNames, searchString), true);
	}

	public List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, vo, attrsNames, allowedStatuses, searchString), true);
	}

	@Override
	public List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, MemberNotExistsException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN) &&
				!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
		}

		List<RichMember> richMembers = getMembersManagerBl().findCompleteRichMembers(sess, attrsNames, allowedStatuses, searchString);

		Iterator<RichMember> richMemberIter = richMembers.iterator();
		while(richMemberIter.hasNext()) {
			RichMember richMember = richMemberIter.next();
			
			//if voadmin or voobserver or groupadmin has right to this member, its ok
			if(AuthzResolver.isAuthorized(sess, Role.VOADMIN, richMember) ||
				AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, richMember) ||
				AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, richMember)) continue;

			//if not, then try facility admin rights
			List<Resource> membersResources = getPerunBl().getResourcesManagerBl().getAssignedResources(sess, richMember);
			boolean found = false;
			for(Resource resource: membersResources) {
				if(AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, resource)) {
					found = true;
					break;
				}
			}
			if(found) continue;

			richMemberIter.remove();
		}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, richMembers, false);
	}

	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, group, attrsNames, searchString, lookingInParentGroup), true);
	}

	public List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "findCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, group, attrsNames, allowedStatuses, searchString, lookingInParentGroup), true);
	}

	public List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)){
			throw new PrivilegeException(sess, "getRichMembersWithAttributesByNames");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributesByNames(sess, group, attrsNames), true);
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)){
			throw new PrivilegeException(sess, "getRichMemberWithAttributes");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, group, attrsDef), true);
	}

	public List<RichMember> getRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)){
			throw new PrivilegeException(sess, "getRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, group), true);
	}

	public List<RichMember> getRichMembers(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getMembers");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, vo), true);
	}

	public List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getRichMembers");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, vo, status), true);
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo), true);
	}

	public List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getRichMembersWithAttributes");
				}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo, status), true);
	}

	public int getMembersCount(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getMembersCount");
				}

		return getMembersManagerBl().getMembersCount(sess, vo);
	}

	public int getMembersCount(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)) {
			throw new PrivilegeException(sess, "getMembersCount");
				}

		return getMembersManagerBl().getMembersCount(sess, vo, status);
	}

	public Vo getMemberVo(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().getMemberVo(sess, member);
	}

	public List<Member> findMembersByName(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException(sess, "findMembersByName");
		}

		return getMembersManagerBl().findMembersByName(sess, searchString);
	}

	public List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "findMembersByNameInVo");
		}

		return getMembersManagerBl().findMembersByNameInVo(sess, vo, searchString);
	}

	public List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "findMembersInVo");
		}

		return getMembersManagerBl().findMembersInVo(sess, vo, searchString);
	}

	public List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "findMembersInGroup");
				}

		return getMembersManagerBl().findMembersInGroup(sess, group, searchString);
	}

	public List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException{
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "findRichMembersInGroup");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInGroup(sess, group, searchString), true);
	}

	public List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException{
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess,getPerunBl().getGroupsManagerBl().getParentGroup(sess, group));

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "findMembersInParentGroup");
				}

		return getMembersManagerBl().findMembersInParentGroup(sess, group, searchString);
	}

	public List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException{
		Utils.checkPerunSession(sess);
		getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, group) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
			throw new PrivilegeException(sess, "findRichMembersInParentGroup");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInParentGroup(sess, group, searchString), true);
	}

	public List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "findRichMembersInVo");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersInVo(sess, vo, searchString), true);
	}

	public List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "findRichMembersWithAttributesInVo");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInVo(sess, vo, searchString), true);
	}

	public Member setStatus(PerunSession sess, Member member, Status status) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "setStatus");
		}

		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().setStatus(sess, member, status);
	}

	public Member validateMemberAsync(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "validateMemberAsync");
				}


		return getMembersManagerBl().validateMemberAsync(sess, member);
	}

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

	public boolean canExtendMembership(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
				!AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
			throw new PrivilegeException(sess, "extendMembership");
				}

		return getMembersManagerBl().canExtendMembership(sess, member);
	}

	public boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException,
				 MemberNotExistsException, ExtendMembershipException {
					 Utils.checkPerunSession(sess);
					 getMembersManagerBl().checkMemberExists(sess, member);

					 // Authorization
					 if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member) &&
							 !AuthzResolver.isAuthorized(sess, Role.SELF, member)) {
						 throw new PrivilegeException(sess, "canExtendMembershipWithReason");
							 }

					 return getMembersManagerBl().canExtendMembershipWithReason(sess, member);
	}

	public boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().canBeMember(sess, vo, user, loa);
	}

	public boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, PrivilegeException,
				 VoNotExistsException, ExtendMembershipException {
					 Utils.checkPerunSession(sess);
					 getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

					 return getMembersManagerBl().canBeMemberWithReason(sess, vo, user, loa);
	}

	public Date getNewExtendMembership(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		return getMembersManagerBl().getNewExtendMembership(sess, member);
	}
	
	public Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws InternalErrorException, PrivilegeException, VoNotExistsException, ExtendMembershipException {
    Utils.checkPerunSession(sess);
    getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
    Utils.notNull(loa, "loa");

    return getMembersManagerBl().getNewExtendMembership(sess, vo, loa);
  }

	public void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url) throws InternalErrorException, PrivilegeException, MemberNotExistsException {

		Utils.checkPerunSession(sess);
		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "sendPasswordResetLinkEmail");
		}

		getMembersManagerBl().sendPasswordResetLinkEmail(sess, member, namespace, url);

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
	public void setPerunBl(PerunBl perunBl)
	{
		this.perunBl = perunBl;
	}

	/**
	 * Sets the membersManagerBl for this instance.
	 *
	 * @param membersManagerBl The membersManagerBl.
	 */
	public void setMembersManagerBl(MembersManagerBl membersManagerBl)
	{
		this.membersManagerBl = membersManagerBl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}
}
