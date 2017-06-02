package cz.metacentrum.perun.core.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.*;

/**
 * MembersManager can find members.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author Zora Sebestianova
 */
public interface MembersManager {

	// Names of the keys in membershipExpirationRules attribute
	static final String membershipGracePeriodKeyName = "gracePeriod";
	static final String membershipPeriodKeyName = "period";
	static final String membershipDoNotExtendLoaKeyName = "doNotExtendLoa";
	static final String membershipPeriodLoaKeyName = "periodLoa";
	static final String membershipDoNotAllowLoaKeyName = "doNotAllowLoa";

	/**
	 * Attribute which contains rules for membership expiration
	 */
	final static String membershipExpirationRulesAttributeName = AttributesManager.NS_VO_ATTR_DEF + ":" + "membershipExpirationRules";

	/**
	 *  Deletes only member data  appropriated by member id.
	 *
	 * @param sess
	 * @param member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 * @throws MemberAlreadyRemovedException
	 * @throws GroupOperationsException
	 */
	void deleteMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException, GroupOperationsException;

	/**
	 *  Deletes all VO members.
	 *
	 * @param sess
	 * @param vo
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws MemberAlreadyRemovedException
	 * @throws GroupOperationsException
	 */
	void deleteAllMembers(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException, PrivilegeException, MemberAlreadyRemovedException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners,SpecificUserType specificUserType, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws ExtendMembershipException
	 * @throws GroupNotExistsException
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

	/**
	 * Creates a new sponsored member in given namespace and external source.
	 * Owner of the member must be specified in params map under key "sponsor"
	 *
	 * @param sess
	 * @param params Map containing parameters about user that will be created, will be used to create Candidate,
	 *               must contain key "sponsor" with value of user login in given namespace that will be owner of created member
	 * @param namespace namespace to generate account in
	 * @param extSource external source
	 * @param extSourcePostfix login postfix if external source uses postfix after login from given namespace, e.g. "@muni.cz"
	 * @param vo VO in which user will be created
	 * @param loa
	 * @return newly created sponsored member
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws UserNotExistsException
	 * @throws ExtSourceNotExistsException
	 * @throws UserExtSourceNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws ExtendMembershipException
	 * @throws AlreadyMemberException
	 * @throws WrongAttributeValueException
	 * @throws GroupOperationsException
	 * @throws PasswordCreationFailedException
	 * @throws LoginNotExistsException
	 */
	Member createSponsoredAccount(PerunSession sess, Map<String, String> params, String namespace, ExtSource extSource, String extSourcePostfix, Vo vo, int loa) throws InternalErrorException, PrivilegeException, UserNotExistsException, ExtSourceNotExistsException, UserExtSourceNotExistsException, WrongReferenceAttributeValueException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, AlreadyMemberException, GroupOperationsException, PasswordStrengthFailedException, PasswordOperationTimeoutException, WrongAttributeValueException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException, GroupOperationsException;

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
	 * Find member of this Vo by one of his login in external source.
	 *
	 * @param perunSession
	 * @param vo
	 * @param userExtSources
	 * @return selected user or throws MemberNotExistsException in case the requested member doesn't exists in this Vo
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	Member getMemberByUserExtSources(PerunSession perunSession, Vo vo, List<UserExtSource> userExtSources) throws InternalErrorException, VoNotExistsException, MemberNotExistsException, PrivilegeException;

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
	 * @throws MemberNotExistsException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 */
	List<Member> getMembersByUser(PerunSession sess, User user) throws InternalErrorException, MemberNotExistsException, PrivilegeException, UserNotExistsException;

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
	 * @throws VoNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, VoNotExistsException, GroupNotExistsException, AttributeNotExistsException, ParentGroupNotExistsException;

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
	 * @throws VoNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, VoNotExistsException, GroupNotExistsException, AttributeNotExistsException, ParentGroupNotExistsException;

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
	 * @throws ParentGroupNotExistsException
	 * @throws WrongAttributeAssignmentException
	 * @throws ResourceNotExistsException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, Resource resource, List<String> attrsNames, List<String> allowedStatuses) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException, WrongAttributeAssignmentException, GroupNotExistsException, ResourceNotExistsException, PrivilegeException;

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
	 * @throws AttributeNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException;

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
	 * @throws AttributeNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, PrivilegeException, VoNotExistsException, AttributeNotExistsException;

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
	 * @throws VoNotExistsException
	 * @throws AttributeNotExistsException
	 * @throws MemberNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, MemberNotExistsException, PrivilegeException, VoNotExistsException, AttributeNotExistsException;

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
	 * @throws AttributeNotExistsException
	 * @throws VoNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AttributeNotExistsException, VoNotExistsException, ParentGroupNotExistsException;

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
	 * @throws AttributeNotExistsException
	 * @throws VoNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws InternalErrorException, PrivilegeException, GroupNotExistsException, AttributeNotExistsException, VoNotExistsException, ParentGroupNotExistsException;

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
	 * @throws ParentGroupNotExistsException
	 */
	List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException;

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
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException;

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
	 * @throws WrongAttributeValueException
	 * @throws PrivilegeException
	 * @throws WrongReferenceAttributeValueException
	 */
	Member validateMemberAsync(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException;

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
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, PrivilegeException, VoNotExistsException;

	/**
	 * Checks if the user can apply membership to the VO, it decides based on extendMembershipRules on the doNotAllowLoa key
	 * @param sess
	 * @param vo
	 * @param user
	 * @param loa
	 * @return true if user can be apply for membership to the VO
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws ExtendMembershipException
	 */
	boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, PrivilegeException, VoNotExistsException, ExtendMembershipException;

	/**
	 * Returns the date to which will be extended member's expiration time.
	 *
	 * @param sess
	 * @param member
	 * @return date
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException
	 * @throws ExtendMembershipException
	 */
	Date getNewExtendMembership(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException, ExtendMembershipException;

	/**
   * Returns the date to which will be extended member's expiration time.
   *
   * @param sess
   * @param vo
   * @param loa
   * @return date
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException
   * @throws ExtendMembershipException
   */
  Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws InternalErrorException, PrivilegeException, VoNotExistsException, ExtendMembershipException;

	/**
	 * Send mail to user's preferred email address with link for non-authz password reset.
	 * Correct authz information is stored in link's URL.
	 *
	 * @param sess PerunSession
	 * @param member Member to get user to send link mail to
	 * @param namespace namespace to change password in (member must have login in)
	 * @param url base URL of Perun instance
	 * @throws InternalErrorException
	 * @throws PrivilegeException If not VO admin of member
	 * @throws MemberNotExistsException If member not exists
	 */
	void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

}
