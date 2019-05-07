package cz.metacentrum.perun.core.api;

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
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * MembersManager can find members.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author Zora Sebestianova
 */
public interface MembersManager {

	/**
	 * Attribute which contains rules for membership expiration
	 */
	String membershipExpirationRulesAttributeName = AttributesManager.NS_VO_ATTR_DEF + ":" + "membershipExpirationRules";

	/**
	 *  Deletes only member data  appropriated by member id.
	 *
	 * @param sess
	 * @param member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 * @throws MemberAlreadyRemovedException
	 */
	void deleteMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException;

	/**
	 *  Deletes all VO members.
	 *
	 * @param sess
	 * @param vo
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws MemberAlreadyRemovedException
	 */
	void deleteAllMembers(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException, PrivilegeException, MemberAlreadyRemovedException;

	/**
	 * Creates a new member from candidate which is prepared for creating specific User
	 * In list specificUserOwners can't be specific user, only normal users and sponsored users are allowed.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate prepared future specificUser
	 * @param specificUserOwners list of users who own specificUser (can't be empty or contain specificUser)
	 * @param specificUserType type of specific user (service or sponsored)
	 * @return newly created member (of specificUser)
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new member from candidate which is prepared for creating specificUser
	 * In list specificUserOwners can't be specific user, only normal users and sponsored users are allowed.
	 *
	 * Also add this member to groups in list.
	 *
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate prepared future specificUser
	 * @param specificUserOwners list of users who own specificUser (can't be empty or contain specificUser)
	 * @param specificUserType type of specific user (service or sponsored)
	 * @param groups list of groups where member will be added too
	 * @return newly created member (of specific User)
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners,SpecificUserType specificUserType, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new member and sets all member's attributes from the candidate.
	 * Also stores the associated user if doesn't exist. This method is used by the registrar.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate
	 * @param extSourceName name of the extSource
	 * @param extSourceType type of the extSource (e.g. cz.metacentrum.perun.core.impl.ExtSourceIdp)
	 * @param login user's login within extSource
	 * @return newly created member, who has set all his/her attributes
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new member and sets all member's attributes from the candidate.
	 * Also stores the associated user if doesn't exist. This method is used by the registrar.
	 *
	 * Also add this member to groups in list.
	 *
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate
	 * @param extSourceName name of the extSource
	 * @param extSourceType type of the extSource (e.g. cz.metacentrum.perun.core.impl.ExtSourceIdp)
	 * @param login user's login within extSource
	 * @param groups list of groups where member will be added too
	 * @return newly created member, who has set all his/her attributes
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new member and sets all member's attributes from the candidate.
	 * Also stores the associated user if doesn't exist. This method is used by the registrar.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate
	 * @param extSourceName name of the extSource
	 * @param extSourceType type of the extSource (e.g. cz.metacentrum.perun.core.impl.ExtSourceIdp)
	 * @param extSourceLoa level of assurance
	 * @param login user's login within extSource
	 * @return newly created member, who has set all his/her attributes
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new member and sets all member's attributes from the candidate.
	 * Also stores the associated user if doesn't exist. This method is used by the registrar.
	 *
	 * Also add this member to groups in list.
	 *
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate
	 * @param extSourceName name of the extSource
	 * @param extSourceType type of the extSource (e.g. cz.metacentrum.perun.core.impl.ExtSourceIdp)
	 * @param extSourceLoa level of assurance
	 * @param login user's login within extSource
	 * @param groups list of groups where member will be added too
	 * @return newly created member, who has set all his/her attributes
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new sponsored member in given namespace and external source.
	 * Owner of the member must be specified in params map under key "sponsor"
	 *
	 * @param sess session
	 * @param params Map containing parameters about user that will be created, will be used to create Candidate,
	 *               must contain key "sponsor" with value of user login in given namespace that will be owner of created member
	 * @param namespace namespace to generate account in
	 * @param extSource external source
	 * @param extSourcePostfix login postfix if external source uses postfix after login from given namespace, e.g. "@muni.cz"
	 * @param vo VO in which user will be created
	 * @param loa level of assurance
	 * @return newly created sponsored member
	 *
	 * @deprecated replaced by {@link #createSponsoredMember(PerunSession, Vo, String, String, String, User)}
	 */
	@Deprecated
	Member createSponsoredAccount(PerunSession sess, Map<String, String> params, String namespace, ExtSource extSource, String extSourcePostfix, Vo vo, int loa) throws InternalErrorException, PrivilegeException, UserNotExistsException, ExtSourceNotExistsException, UserExtSourceNotExistsException, WrongReferenceAttributeValueException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, AlreadyMemberException, PasswordStrengthFailedException, PasswordOperationTimeoutException, WrongAttributeValueException;

	/**
	 * Creates a new member from candidate returned by the method VosManager.findCandidates which fills Candidate.userExtSource.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate
	 * @return newly created members
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new member from candidate returned by the method VosManager.findCandidates which fills Candidate.userExtSource.
	 *
	 * Also add this member to groups in list.
	 *
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate
	 * @param groups list of groups where member will be added too
	 * @return newly created members
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new member from user.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @return newly created member
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws VoNotExistsException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Creates a new member from user.
	 *
	 * Also add this member to groups in list.
	 *
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @param groups list of groups where member will be added too
	 * @return newly created member
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws VoNotExistsException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Create new member from user by login and ExtSource.
	 *
	 * <strong>This method runs asynchronously</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param extSource
	 * @param login
	 * @return newly created member
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @throws VoNotExistsException
	 * @throws ExtSourceNotExistsException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException;

	/**
	 * Create new member from user by login and ExtSource.
	 *
	 * Also add this member to groups in list.
	 *
	 * <strong>This method runs asynchronously</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param extSource
	 * @param login
	 * @param groups
	 * @return newly created member
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @throws VoNotExistsException
	 * @throws ExtSourceNotExistsException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException;

	/**
	 * Find member of this Vo by his login in external source
	 *
	 * @param perunSession
	 * @param vo
	 * @param userExtSource
	 * @return selected user or throws MemberNotExistsException in case the requested member doesn't exists in this Vo
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	Member getMemberByUserExtSource(PerunSession perunSession, Vo vo, UserExtSource userExtSource) throws InternalErrorException, VoNotExistsException, MemberNotExistsException, PrivilegeException;

	/**
	 * Returns member by his id.
	 *
	 * @param sess
	 * @param id
	 * @return member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 */
	Member getMemberById(PerunSession sess, int id) throws InternalErrorException, MemberNotExistsException, PrivilegeException;

	/**
	 * Returns member by his user and vo.
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @return member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws VoNotExistsException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 */
	Member getMemberByUser(PerunSession sess, Vo vo, User user) throws InternalErrorException, MemberNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException;

	/**
	 * Returns members by his user.
	 *
	 * @param sess
	 * @param user
	 * @return member
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 */
	List<Member> getMembersByUser(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException;

	/**
	 * Get all VO members.
	 *
	 * @param sess
	 * @param vo
	 * @return all members of the VO
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<Member> getMembers(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get all VO members who have the status.
	 *
	 * @param sess
	 * @param vo
	 * @param status get only members who have this status
	 * @return all members of the VO
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<Member> getMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get richMember without attribute by id of member
	 *
	 * @param sess
	 * @param id of member
	 * @return richMember without attributes (only with user)
	 * @throws InternalErrorException
	 * @throws PrivilegeException if user has no rights to get this richMember
	 * @throws MemberNotExistsException if member not exists
	 */
	RichMember getRichMemberById(PerunSession sess, int id) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

	/**
	 * Get Member to RichMember with attributes.
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException
	 */
	RichMember getRichMemberWithAttributes(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for vo.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsDef list of attrDefs only for selected attributes
	 * @return list of richmembers
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get all rich members with allowed statuses from specific group. Rich member object contains user, member, userExtSources and member/user attributes.
	 *
	 * @param sess
	 * @param group to get richMembers from
	 * @param allowedStatuses only allowed statuses
	 * @return list of rich members with all member/user attributes, empty list if there are no members in group
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for vo.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames list of attrNames for selected attributes
	 * @return list of richmembers
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException;

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsNames list of attrNames for selected attributes
	 * @return list of richmembers
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws AttributeNotExistsException
	 * @throws GroupNotExistsException
	 */
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AttributeNotExistsException;

	/**
	 * Get all RichMembers with attrs specific for list of attrsNames from the vo.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames
	 * @return list of richMembers with specific attributes from Vo
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException;

	/**
	 * Get all RichMembers with attrs specific for list of attrsNames from the vo and have only
	 * status which is contain in list of statuses.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 * If listOfStatuses is empty or null, return all possible statuses.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames
	 * @param allowedStatuses
	 * @return list of richMembers with specific attributes from Vo
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException;

	/**
	 * Get all RichMembers with attrs specific for list of attrsNames from the group.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * If lookingInParentGroup is true, get all these richMembers only for parentGroup of this group.
	 * If this group is top level group, so get richMembers from members group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsNames
	 * @param lookingInParentGroup
	 * @return list of richMembers with specific attributes from Group
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AttributeNotExistsException, ParentGroupNotExistsException;

	/**
	 * Get all RichMembers with attrs specific for list of attrsNames from the group and have only
	 * status which is contain in list of statuses.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 * If listOfStatuses is empty or null, return all possible statuses.
	 *
	 * If lookingInParentGroup is true, get all these richMembers only for parentGroup of this group.
	 * If this group is top level group, so get richMembers from members group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsNames
	 * @param allowedStatuses
	 * @param lookingInParentGroup
	 * @return list of richMembers with specific attributes from group
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AttributeNotExistsException, ParentGroupNotExistsException;

	/**
	 * Get all RichMembers with attributes specific for list of attrNames.
	 * Attributes are defined by member (user) and resource (facility) objects.
	 * It returns also user-facility (in userAttributes of RichMember) and
	 * member-resource (in memberAttributes of RichMember) attributes.
	 * Members are defined by group and are filtered by list of allowed statuses.
	 *
	 * @param sess
	 * @param group
	 * @param resource
	 * @param attrsNames
	 * @param allowedStatuses
	 * @return list of richMembers with specific attributes
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 * @throws GroupResourceMismatchException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, Resource resource, List<String> attrsNames, List<String> allowedStatuses) throws InternalErrorException, AttributeNotExistsException, GroupNotExistsException, ResourceNotExistsException, PrivilegeException, GroupResourceMismatchException;

	/**
	 * Return list of richMembers for specific vo by the searchString with attrs specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames
	 * @param searchString
	 * @return list of founded richMembers with specific attributes from Vo for searchString
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Return list of richMembers for specific vo by the searchString with attrs specific for list of attrsNames
	 * and who have only status which is contain in list of statuses.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 * If listOfStatuses is empty or null, return all possible statuses.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames
	 * @param allowedStatuses
	 * @param searchString
	 * @return list of founded richMembers with specific attributes from Vo for searchString with allowed statuses
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Return list of richMembers from Perun by searchString with attrs specific for list of attrsNames
	 * and who have only status which is contain in list of statuses.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 * If listOfStatuses is empty or null, return all possible statuses.
	 *
	 * @param sess
	 * @param attrsNames
	 * @param allowedStatuses
	 * @param searchString
	 *
	 * @return list of founded richMembers with specific attributes by searchString with allowed statuses
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, PrivilegeException;

	/**
	 * Return list of richMembers for specific group by the searchString with attrs specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * If lookingInParentGroup is true, find all these richMembers only for parentGroup of this group.
	 * If this group is top level group, so find richMembers from members group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsNames
	 * @param searchString
	 * @param lookingInParentGroup
	 * @return list of founded richMembers with specific attributes from Group for searchString
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException;

	/**
	 * Return list of richMembers for specific group by the searchString with attrs specific for list of attrsNames
	 * and who have only status which is contain in list of statuses.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 * If listOfStatuses is empty or null, return all possible statuses.
	 *
	 * If lookingInParentGroup is true, find all these richMembers only for parentGroup of this group.
	 * If this group is top level group, so find richMembers from members group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsNames
	 * @param allowedStatuses
	 * @param searchString
	 * @param lookingInParentGroup
	 * @return list of founded richMembers with specific attributes from Group for searchString
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException;

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsDef
	 * @return
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

	/**
	 * Get all rich members of VO. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param vo
	 * @return list of rich members, empty list if there are no members in VO
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get all rich members of Group. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param group
	 * @return list of rich members, empty list if there are no members in Group
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

	/**
	 * Get all rich members of VO with specified status. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param vo
	 * @param status get only members who have this status
	 * @return list of rich members, empty list if there are no members in VO with specified status
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get all rich members of VO. Rich member object contains user, member, userExtSources and member/user attributes.
	 *
	 * @param sess
	 * @param vo
	 * @return list of rich members with all member/user attributes, empty list if there are no members in VO
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get all rich members of VO with specified status. Rich member object contains user, member, userExtSources and member/user attributes.
	 *
	 * @param sess
	 * @param vo
	 * @param status
	 * @return list of rich members with all member/user attributes, empty list if there are no members in VO with specified status
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get the VO members count.
	 *
	 * @param sess
	 * @param vo
	 * @return count of VO members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	int getMembersCount(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get the VO members count with defined status.
	 *
	 * @param sess
	 * @param vo
	 * @param status
	 * @return count of VO members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	int getMembersCount(PerunSession sess, Vo vo, Status status) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Get the member VO.
	 *
	 * @param sess
	 * @param member
	 * @return member's VO
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Vo getMemberVo(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException;

	/**
	 * Return list of members by the searchString
	 *
	 * @param sess
	 * @param searchString
	 * @return list of members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Member> findMembersByName(PerunSession sess, String searchString) throws InternalErrorException, PrivilegeException;

	/**
	 * Return list of members by the searchString under defined VO.
	 *
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Return list of members by the searchString under defined VO. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Return list of members by the searchString under defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group
	 * @param searchString
	 * @return list of members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 */
	List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

	/**
	 * Return list of members by the searchString udner parentGroup of defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group this group is used to get parent group, we are searching members of the parent group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException;

	/**
	 * Return list of rich members with attributes by the searchString under defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group this group is used to get parent group, we are searching members of the parent group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException is thrown if group or parent group of this group not exists.
	 */
	List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

	/**
	 * Return list of rich with attributes members by the searchString under parent group of defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws GroupNotExistsException is thrown if group or parent group of this group not exists.
	 */
	List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

	/**
	 * Return list of rich members by the searchString under defined VO. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of rich members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Return list of rich members with attributes by the searchString under defined VO. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of rich members with attributes
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 *  Set status of the member to specified status.
	 *
	 * @param sess
	 * @param member
	 * @param status new status
	 * @return member with status set
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws MemberNotValidYetException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws PrivilegeException
	 */
	Member setStatus(PerunSession sess, Member member, Status status) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException;

	/**
	 *  Set status of the member to specified status.
	 *
	 * @param sess
	 * @param member
	 * @param status new status
	 * @param message message with reason for suspension
	 * @return member with status set
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws MemberNotValidYetException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws PrivilegeException
	 */
	Member setStatus(PerunSession sess, Member member, Status status, String message) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException;

	/**
	 * Validate all attributes for member and set member's status to VALID.
	 * This method runs asynchronously. It immediately return member with <b>ORIGINAL</b> status and after asynchronous validation successfully
	 * finishes it switch member's status to VALID. If validation ends with error, member keeps his status.
	 *
	 * @param sess
	 * @param member
	 * @return member with new status set
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 */
	Member validateMemberAsync(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

	/**
	 * Extend member membership using membershipExpirationRules attribute defined at VO.
	 *
	 * @param sess
	 * @param member
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException
	 * @throws ExtendMembershipException
	 */
	void extendMembership(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ExtendMembershipException;

	/**
	 * Return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise false.
	 *
	 * @param sess
	 * @param member
	 * @return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise false.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException
	 */
	boolean canExtendMembership(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

	/**
	 * Return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise throws exception.
	 *
	 * @param sess
	 * @param member
	 * @return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise false
	 * @throws InternalErrorException
	 * @throws ExtendMembershipException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException
	 */
	boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws InternalErrorException, ExtendMembershipException, PrivilegeException, MemberNotExistsException;

	/**
	 * Checks if the user can apply membership to the VO, it decides based on extendMembershipRules on the doNotAllowLoa key
	 * @param sess
	 * @param vo
	 * @param user
	 * @param loa
	 * @return true if user can be apply for membership to the VO
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 */
	boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, VoNotExistsException;

	/**
	 * Checks if the user can apply membership to the VO, it decides based on extendMembershipRules on the doNotAllowLoa key
	 * @param sess
	 * @param vo
	 * @param user
	 * @param loa
	 * @return true if user can be apply for membership to the VO
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws ExtendMembershipException
	 */
	boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, VoNotExistsException, ExtendMembershipException;

	/**
	 * Get member by extSourceName, extSourceLogin and Vo
	 *
	 * @param sess
	 * @param extSourceName name of extSource
	 * @param extLogin login of user in extSource
	 * @param vo Vo where we are looking for member
	 * @return member
	 * @throws ExtSourceNotExistsException
	 * @throws UserExtSourceNotExistsException
	 * @throws MemberNotExistsException
	 * @throws UserNotExistsException
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Member getMemberByExtSourceNameAndExtLogin(PerunSession sess, Vo vo, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, MemberNotExistsException, UserNotExistsException, InternalErrorException, VoNotExistsException, PrivilegeException;

	/**
	 * Returns the date to which will be extended member's expiration time.
	 *
	 * @param sess
	 * @param member
	 * @return date
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Date getNewExtendMembership(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException;

	/**
   * Returns the date to which will be extended member's expiration time.
   *
   * @param sess
   * @param vo
   * @param loa
   * @return date
   * @throws InternalErrorException
   * @throws VoNotExistsException
   * @throws ExtendMembershipException
   */
  Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws InternalErrorException, VoNotExistsException, ExtendMembershipException;

	/**
	 * Send mail to user's preferred email address with link for non-authz password reset.
	 * Correct authz information is stored in link's URL.
	 *
	 * @param sess PerunSession
	 * @param member Member to get user to send link mail to
	 * @param namespace namespace to change password in (member must have login in it)
	 * @param url base URL of Perun instance
	 * @param mailAttributeUrn urn of the attribute with stored mail
	 * @param language language of the message
	 * @throws InternalErrorException
	 * @throws PrivilegeException If not VO admin of member
	 * @throws MemberNotExistsException If member not exists
	 */
	void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url, String mailAttributeUrn, String language) throws InternalErrorException, PrivilegeException, MemberNotExistsException, UserNotExistsException, AttributeNotExistsException;

	/**
	 * Creates a new sponsored Member and its User.
	 * @param session actor
	 * @param vo virtual organization  for the member
	 * @param namespace namespace for selecting password module
	 * @param guestName a designation, usually a full name
	 * @param password  password
	 * @param sponsor sponsoring user or null for the caller
	 * @return new Member in the Vo
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 * @throws AlreadyMemberException
	 * @throws LoginNotExistsException
	 * @throws PasswordCreationFailedException
	 * @throws ExtendMembershipException
	 * @throws WrongAttributeValueException
	 * @throws ExtSourceNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws UserNotInRoleException
	 */
	RichMember createSponsoredMember(PerunSession session, Vo vo, String namespace, String guestName, String password, User sponsor) throws InternalErrorException, PrivilegeException, AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException, UserNotInRoleException;

	/**
	 * Transform non-sponsored member to sponsored one with defined sponsor
	 *
	 * @param session perun session
	 * @param sponsoredMember member who will be set as sponsored one
	 * @param sponsor new sponsor of this member
	 *
	 * @return sponsored member
	 *
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws MemberNotExistsException if member with defined id not exists in system Perun
	 * @throws AlreadySponsoredMemberException if member is already sponsored
	 * @throws UserNotInRoleException if sponsor hasn't right role in the same vo
	 * @throws PrivilegeException if not PerunAdmin
	 */
	RichMember setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor) throws InternalErrorException, MemberNotExistsException, AlreadySponsoredMemberException, UserNotInRoleException, PrivilegeException;

	/**
	 * Transform sponsored member to non-sponsored one. Delete all his sponsors.
	 *
	 * @param session perun session
	 * @param sponsoredMember member who will be unset from sponsoring
	 *
	 * @return non-sponsored member
	 *
	 * @throws MemberNotExistsException if member with defined id not exists in system Perun
	 * @throws MemberNotSponsoredException if member is not sponsored yet
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not PerunAdmin
	 */
	RichMember unsetSponsorshipForMember(PerunSession session, Member sponsoredMember) throws MemberNotExistsException, MemberNotSponsoredException, InternalErrorException, PrivilegeException;

	/**
	 * Assigns a new sponsor to an existing member.
	 * @param session actor
	 * @param sponsored existing member that needs sponsoring
	 * @param sponsor sponsoring user or null for the caller
	 * @return existing Member
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotSponsoredException
	 * @throws AlreadySponsorException
	 * @throws UserNotInRoleException
	 */
	RichMember sponsorMember(PerunSession session, Member sponsored, User sponsor) throws InternalErrorException, PrivilegeException, MemberNotSponsoredException, AlreadySponsorException, UserNotInRoleException;

	/**
	 * Get all sponsored RichMembers with attributes by list of attribute names for specific User and Vo.
	 *
	 * @param sess
	 * @param vo to specify Member for User
	 * @param user to specify Member for User
	 * @param attrNames list of attrNames - if empty, return richMembers without attributes
	 * @return list of sponsored rich members with attributes from the list
	 *
	 * @throws InternalErrorException if any internal error has occurred
	 * @throws AttributeNotExistsException if any attributeDefinition can't be found by one of attribute names
	 * @throws VoNotExistsException if Vo not exists in Perun
	 * @throws UserNotExistsException if User not exists in Perun
	 * @throws PrivilegeException if user in session is not allowed to call this method
	 */
	List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo, User user, List<String> attrNames) throws InternalErrorException, AttributeNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException;

	/**
	 * Gets list of members of a VO sponsored by the given user.
	 * @param sess actor
	 * @param vo virtual organization from which are the sponsored members chosen
	 * @param user user of system
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 * @return list of members from given VO who are sponsored by the given user.
	 */
	List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo, User user) throws InternalErrorException, PrivilegeException;

	/**
	 * Gets list of sponsored members of a VO.
	 * @param sess actor
	 * @param vo virtual organization from which are the sponsored members chosen
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 * @return list of members from given VO who are sponsored
	 */
	List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo) throws InternalErrorException, PrivilegeException;

	/**
	 * Extends expiration date. Sponsored members cannot apply for membership extension, this method allows a sponsor to extend it.
	 * @param session actor
	 * @param sponsored existing member that is sponsored
	 * @param sponsor sponsoring user or null for the caller
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 * @return new expiration date
	 */
	String extendExpirationForSponsoredMember(PerunSession session, Member sponsored, User sponsor) throws InternalErrorException, PrivilegeException;

	/**
	 * Removes the sponsor.
	 *
	 * @param sess actor
	 * @param sponsoredMember existing member that is sponsored
	 * @param sponsorToRemove sponsoring user for removal
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 */
	void removeSponsor(PerunSession sess, Member sponsoredMember, User sponsorToRemove) throws InternalErrorException, PrivilegeException;

}
