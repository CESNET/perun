package cz.metacentrum.perun.core.entry;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
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

	public void deleteMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		getMembersManagerBl().checkMemberExists(sess, member);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, member)) {
			throw new PrivilegeException(sess, "deleteMember");
		}


		getMembersManagerBl().deleteMember(sess, member);
	}

	public void deleteAllMembers(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException, PrivilegeException, MemberAlreadyRemovedException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "deleteAllMembers");
		}

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		getMembersManagerBl().deleteAllMembers(sess, vo);
	}

	public Member createServiceMember(PerunSession sess, Vo vo, Candidate candidate, List<User> serviceUserOwners) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createServiceMember (Service User) - from candidate");
		}
		Utils.notNull(candidate, "candidate");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);
		if(serviceUserOwners.isEmpty()) throw new InternalErrorException("List of serviceUserOwners of " + candidate + " can't be empty.");

		for(User u: serviceUserOwners) {
			getPerunBl().getUsersManagerBl().checkUserExists(sess, u);
		}

		return getMembersManagerBl().createServiceMember(sess, vo, candidate, serviceUserOwners);
	}

	public Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from candidate");
		}

		Utils.notNull(candidate, "candiate");
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().createMember(sess, vo, candidate);
	}

	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extSourceType, "extSourceType");
		Utils.notNull(login, "login");

		return getMembersManagerBl().createMember(sess, vo, extSourceName, extSourceType, login, candidate);
	}

	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException {
		Utils.checkPerunSession(sess);

		//TODO Authorization

		Utils.notNull(extSourceName, "extSourceName");
		Utils.notNull(extSourceType, "extSourceType");
		Utils.notNull(login, "login");

		return getMembersManagerBl().createMember(sess, vo, extSourceName, extSourceType, extSourceLoa, login, candidate);
	}

	public Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyMemberException, WrongAttributeValueException, WrongReferenceAttributeValueException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException {
		Utils.checkPerunSession(sess);
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
			throw new PrivilegeException(sess, "createMember - from user");
		}

		getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		return getMembersManagerBl().createMember(sess, vo, user);
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
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo, attrsDef));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributesByNames(sess, vo, attrsNames));
	}

	public List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException {
		Utils.checkPerunSession(sess);

		perunBl.getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.TOPGROUPCREATOR, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, vo)){
			throw new PrivilegeException(sess, "getCompleteRichMembers");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, vo, attrsNames));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, vo, attrsNames, allowedStatuses));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, attrsNames, lookingInParentGroup));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getCompleteRichMembers(sess, group, attrsNames, allowedStatuses, lookingInParentGroup));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, vo, attrsNames, searchString));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, vo, attrsNames, allowedStatuses, searchString));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, group, attrsNames, searchString, lookingInParentGroup));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findCompleteRichMembers(sess, group, attrsNames, allowedStatuses, searchString, lookingInParentGroup));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributesByNames(sess, group, attrsNames));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, group, attrsDef));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, group));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, vo));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembers(sess, vo, status));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().getRichMembersWithAttributes(sess, vo, status));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInGroup(sess, group, searchString));
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

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInParentGroup(sess, group, searchString));
	}

	public List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "findRichMembersInVo");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersInVo(sess, vo, searchString));
	}

	public List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException {
		Utils.checkPerunSession(sess);

		getPerunBl().getVosManagerBl().checkVoExists(sess, vo);

		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(sess, Role.VOOBSERVER, vo)) {
			throw new PrivilegeException(sess, "findRichMembersWithAttributesInVo");
				}

		return getPerunBl().getMembersManagerBl().filterOnlyAllowedAttributes(sess, getMembersManagerBl().findRichMembersWithAttributesInVo(sess, vo, searchString));
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
