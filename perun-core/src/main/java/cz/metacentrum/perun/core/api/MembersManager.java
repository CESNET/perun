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
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.InvalidSponsoredUserDataException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSponsoredException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSuspendedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.NamespaceRulesNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordResetMailNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.SponsorshipDoesNotExistException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotInRoleException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

import java.time.LocalDate;
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
	void deleteMember(PerunSession sess, Member member) throws MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException;

	/**
	 * Delete given members. It is possible to delete members from multiple vos.
	 *
	 * @param sess session
	 * @param members members that will be deleted
	 * @throws InternalErrorException internal error
	 * @throws MemberNotExistsException if any member doesn't exist
	 * @throws PrivilegeException insufficient permissions
	 * @throws MemberAlreadyRemovedException if already removed
	 */
	void deleteMembers(PerunSession sess, List<Member> members) throws MemberNotExistsException, PrivilegeException, MemberAlreadyRemovedException;

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
	void deleteAllMembers(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException, MemberAlreadyRemovedException;

	/**
	 * Creates a new member from candidate which is prepared for creating specific User
	 * In list specificUserOwners can't be specific user, only normal users and sponsored users are allowed.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate prepared future specificUser
	 * @param specificUserOwners list of users who own specificUser (can't be empty or contain specificUser)
	 * @param specificUserType type of specific user (service)
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
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException;

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
	 * @param specificUserType type of specific user (service)
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
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners,SpecificUserType specificUserType, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, UserNotExistsException, ExtendMembershipException, GroupNotExistsException;

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
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

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
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

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
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

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
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

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
	Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

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
	Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

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
	Member createMember(PerunSession sess, Vo vo, User user) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

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
	Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, VoNotExistsException, UserNotExistsException, PrivilegeException, ExtendMembershipException, GroupNotExistsException;

	/**
	 * Create new member from user by login and ExtSource.
	 *
	 * <strong>This method validates member asynchronously</strong>
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
	Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException;

	/**
	 * Create new member from user by login and ExtSource.
	 *
	 * Also add this member to groups in list.
	 *
	 * <strong>This method validates member asynchronously</strong>
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
	Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, VoNotExistsException, ExtSourceNotExistsException, PrivilegeException, GroupNotExistsException;

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
	Member getMemberByUserExtSource(PerunSession perunSession, Vo vo, UserExtSource userExtSource) throws VoNotExistsException, MemberNotExistsException, PrivilegeException;

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
	Member getMemberById(PerunSession sess, int id) throws MemberNotExistsException, PrivilegeException;

	/**
	 * Returns members by their ids.
	 *
	 * @param perunSession
	 * @param ids
	 * @return list of members with specified ids
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Member> getMembersByIds(PerunSession perunSession, List<Integer> ids) throws PrivilegeException;

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
	Member getMemberByUser(PerunSession sess, Vo vo, User user) throws MemberNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException;

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
	List<Member> getMembersByUser(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException;

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
	List<Member> getMembers(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

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
	List<Member> getMembers(PerunSession sess, Vo vo, Status status) throws PrivilegeException, VoNotExistsException;

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
	RichMember getRichMemberById(PerunSession sess, int id) throws PrivilegeException, MemberNotExistsException;

	/**
	 * Get Member to RichMember with attributes.
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException
	 */
	RichMember getRichMemberWithAttributes(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

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
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef) throws PrivilegeException, VoNotExistsException;

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
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws PrivilegeException, VoNotExistsException, AttributeNotExistsException;

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
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws PrivilegeException, GroupNotExistsException, AttributeNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws PrivilegeException, VoNotExistsException, AttributeNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws PrivilegeException, VoNotExistsException, AttributeNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws PrivilegeException, GroupNotExistsException, AttributeNotExistsException, ParentGroupNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws PrivilegeException, GroupNotExistsException, AttributeNotExistsException, ParentGroupNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, Resource resource, List<String> attrsNames, List<String> allowedStatuses) throws AttributeNotExistsException, GroupNotExistsException, ResourceNotExistsException, PrivilegeException, GroupResourceMismatchException;

	/**
	 * Return list of richMembers for specific vo by the searchString with attrs specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames
	 * @param searchString
	 * @param onlySponsored return only sponsored members
	 * @return list of founded richMembers with specific attributes from Vo for searchString
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString, boolean onlySponsored) throws PrivilegeException, VoNotExistsException;

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
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws PrivilegeException, VoNotExistsException;

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
	List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws PrivilegeException;

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
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException;

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
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException;

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
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> getRichMembers(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

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
	List<RichMember> getRichMembers(PerunSession sess, Group group) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status) throws PrivilegeException, VoNotExistsException;

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
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

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
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status) throws PrivilegeException, VoNotExistsException;

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
	int getMembersCount(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

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
	int getMembersCount(PerunSession sess, Vo vo, Status status) throws PrivilegeException, VoNotExistsException;

	/**
	 * Get the member VO.
	 *
	 * @param sess
	 * @param member
	 * @return member's VO
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Vo getMemberVo(PerunSession sess, Member member) throws MemberNotExistsException;

	/**
	 * Return list of members by the searchString
	 *
	 * @param sess
	 * @param searchString
	 * @return list of members
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Member> findMembersByName(PerunSession sess, String searchString) throws PrivilegeException;

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
	List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString) throws PrivilegeException, VoNotExistsException;

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
	List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString) throws PrivilegeException, VoNotExistsException;

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
	List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) throws PrivilegeException, GroupNotExistsException;

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
	List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString) throws PrivilegeException, GroupNotExistsException, ParentGroupNotExistsException;

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
	List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) throws PrivilegeException, GroupNotExistsException;

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
	List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString) throws PrivilegeException, VoNotExistsException;

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
	List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) throws PrivilegeException, VoNotExistsException;

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
	Member setStatus(PerunSession sess, Member member, Status status) throws PrivilegeException, MemberNotExistsException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException;

	/**
	 * Set date to which will be member suspended in his VO.
	 *
	 * For almost unlimited time please use time in the far future.
	 *
	 * @param sess
	 * @param member member who will be suspended
	 * @param suspendedTo date to which will be member suspended (after this date, he will not be affected by suspension any more)
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotExistsException if member not exists in Perun
	 */
	void suspendMemberTo(PerunSession sess, Member member, Date suspendedTo) throws MemberNotExistsException, PrivilegeException;

	/**
	 * Remove suspend state from Member - remove date to which member should be considered as suspended in the VO.
	 *
	 * WARNING: this will remove the date even if it is in the past (so member is no longer considered as suspended)
	 *
	 * @param sess
	 * @param member member for which the suspend state will be removed
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 * @throws MemberNotSuspendedException if member has not set date to which should be considered as suspended
	 * @throws PrivilegeException
	 */
	void unsuspendMember(PerunSession sess, Member member) throws MemberNotExistsException, MemberNotSuspendedException, PrivilegeException;


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
	Member validateMemberAsync(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

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
	void extendMembership(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException, ExtendMembershipException;

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
	boolean canExtendMembership(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

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
	boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws ExtendMembershipException, PrivilegeException, MemberNotExistsException;

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
	boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) throws VoNotExistsException, PrivilegeException;

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
	boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws VoNotExistsException, ExtendMembershipException, PrivilegeException;

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
	Member getMemberByExtSourceNameAndExtLogin(PerunSession sess, Vo vo, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, MemberNotExistsException, UserNotExistsException, VoNotExistsException, PrivilegeException;

	/**
	 * Returns the date to which will be extended member's expiration time.
	 *
	 * @param sess
	 * @param member
	 * @return date
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Date getNewExtendMembership(PerunSession sess, Member member) throws MemberNotExistsException;

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
  Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws VoNotExistsException, ExtendMembershipException;

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
	 * @throws PasswordResetMailNotExistsException If the attribute with stored mail is not filled.
	 */
	void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url, String mailAttributeUrn, String language) throws PrivilegeException, MemberNotExistsException, UserNotExistsException, AttributeNotExistsException, PasswordResetMailNotExistsException;

	/**
	 * Send mail to user's preferred email address with link for non-authz account activation.
	 * Correct authz information is stored in link's URL.
	 *
	 * @param sess PerunSession
	 * @param member Member to get user to send link mail to
	 * @param namespace namespace to activate account in (member must have login in it)
	 * @param url base URL of Perun instance
	 * @param mailAttributeUrn urn of the attribute with stored mail
	 * @param language language of the message
	 * @throws InternalErrorException
	 * @throws PrivilegeException If not VO admin of member
	 * @throws MemberNotExistsException If member not exists
	 * @throws PasswordResetMailNotExistsException If the attribute with stored mail is not filled.
	 */
	void sendAccountActivationLinkEmail(PerunSession sess, Member member, String namespace, String url, String mailAttributeUrn, String language) throws PrivilegeException, MemberNotExistsException, UserNotExistsException, AttributeNotExistsException, PasswordResetMailNotExistsException;

	/**
	 * Creates a new sponsored Member and its User.
	 * @param session actor
	 * @param vo virtual organization  for the member
	 * @param data about the user that should be created, required fields depend on the
	 *        provided namespace. However, it has to contain either `guestName`, or `firstName` and `lastName`.
	 *        Also, if you want to create an external account, specify the `namespace` field.
	 * @param sponsor sponsoring user or null for the caller
	 * @param validityTo last day when the sponsorship is active (null means the sponsorship will last forever)
	 * @param sendActivationLink if true link for manual activation of account will be send to the email
	 *                           be careful when using with empty (no-reply) email
	 * @param url base URL of Perun Instance
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
	 * @throws AlreadySponsorException
	 */
	RichMember createSponsoredMember(PerunSession session, SponsoredUserData data, Vo vo, User sponsor, LocalDate validityTo, boolean sendActivationLink, String url) throws PrivilegeException, AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException, UserNotInRoleException, PasswordStrengthException, InvalidLoginException, AlreadySponsorException, InvalidSponsoredUserDataException, NamespaceRulesNotExistsException;

	/**
	 * Creates a sponsored membership for the given user.
	 *
	 * @param session actor
	 * @param vo virtual organization for the member
	 * @param userToBeSponsored user, that will be sponsored by sponsor
	 * @param namespace namespace for selecting password module
	 * @param password password
	 * @param login login
	 * @param sponsor sponsoring user or null for the caller
	 * @param validityTo last day when the sponsorship is active (null means the sponsorship will last forever)
	 *
	 * @return sponsored member
	 *
	 * @throws PrivilegeException
	 * @throws AlreadyMemberException
	 * @throws LoginNotExistsException
	 * @throws PasswordCreationFailedException
	 * @throws ExtendMembershipException
	 * @throws WrongAttributeValueException
	 * @throws ExtSourceNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws UserNotInRoleException
	 * @throws PasswordStrengthException
	 * @throws InvalidLoginException
	 * @throws AlreadySponsorException
	 */
	RichMember setSponsoredMember(PerunSession session, Vo vo, User userToBeSponsored, String namespace, String password, String login, User sponsor, LocalDate validityTo) throws PrivilegeException, AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException, UserNotInRoleException, PasswordStrengthException, InvalidLoginException, AlreadySponsorException, InvalidSponsoredUserDataException, NamespaceRulesNotExistsException;

	/**
	 * Creates new sponsored members using input from CSV file.
	 *
	 * Since there may be error while creating some of the members and we cannot simply rollback the transaction and
	 * start over, exceptions during member creation are not thrown and the returned map has this structure:
	 *
	 * name -> {"status" -> "OK" or "Error...", "login" -> login, "password" -> password}
	 *
	 * Keys are names given to this method and values are maps containing keys "status", "login" and "password".
	 * "status" has as its value either "OK" or message of exception which was thrown during creation of the member.
	 * "login" contains login (e.g. učo) if status is OK, "password" contains password if status is OK.
	 *
	 * @param sess perun session
	 * @param vo virtual organization to created sponsored members in
	 * @param namespace used for selecting external system in which guest user account will be created
	 * @param data csv file values separated by semicolon ';' characters
	 * @param header header to the given csv data, it should represent columns for the given data.
	 *               Required values are - firstname, lastname, urn:perun:user:attribute-def:def:preferredMail
	 *               Optional values are - urn:perun:user:attribute-def:def:note
	 *               The order of the items doesn't matter.
	 * @param sponsor sponsoring user
	 * @param sendActivationLink if true link for manual activation of every created sponsored member account will be send
	 *                           to email which was set for him, be careful when using no-reply emails
	 * @param url base URL of Perun Instance
	 * @param groups groups, to which will be the created users assigned
	 * @return map of names to map of status, login and password
	 * @throws PrivilegeException insufficient permissions
	 */
	Map<String, Map<String, String>> createSponsoredMembersFromCSV(PerunSession sess, Vo vo, String namespace,
	                                                               List<String> data, String header, User sponsor,
	                                                               LocalDate validityTo, boolean sendActivationLink,
																   String url, List<Group> groups) throws PrivilegeException;

	/**
	 * Creates new sponsored Members (with random generated passwords).
	 *
	 * Since there may be error while creating some of the members and we cannot simply rollback the transaction and start over,
	 * exceptions during member creation are not thrown and the returned map has this structure:
	 *
	 * name -> {"status" -> "OK" or "Error...", "login" -> login, "password" -> password}
	 *
	 * Keys are names given to this method and values are maps containing keys "status", "login" and "password".
	 * "status" has as its value either "OK" or message of exception which was thrown during creation of the member.
	 * "login" contains login (e.g. učo) if status is OK, "password" contains password if status is OK.
	 *
	 * @param session perun session
	 * @param vo vo for members
	 * @param namespace namespace for selecting password module
	 * @param names names of members to create, single name should have the format {firstName};{lastName} to be
	 *              parsed well
	 * @param email (optional) preferred email that will be set to the created user. If no email
	 *              is provided, "no-reply@muni.cz" is used.
	 * @param sponsor sponsoring user or null for the caller
	 * @param validityTo last day when the sponsorship is active (null means the sponsorship will last forever)
	 * @param sendActivationLink if true link for manual activation of every created sponsored member account will be send
	 *                           to the email, be careful when using with empty (no-reply) email
	 * @param url base URL of Perun Instance
	 * @return map of names to map of status, login and password
	 * @throws PrivilegeException
	 */
	Map<String, Map<String, String>> createSponsoredMembers(PerunSession session, Vo vo, String namespace, List<String> names, String email, User sponsor, LocalDate validityTo, boolean sendActivationLink, String url) throws PrivilegeException;

	/**
	 * Transform non-sponsored member to sponsored one with defined sponsor
	 *
	 * @param session perun session
	 * @param sponsoredMember member who will be set as sponsored one
	 * @param sponsor new sponsor of this member
	 * @param validityTo last day when the sponsorship is active (null means the sponsorship will last forever)
	 *
	 * @return sponsored member
	 *
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws MemberNotExistsException if member with defined id not exists in system Perun
	 * @throws AlreadySponsoredMemberException if member is already sponsored
	 * @throws UserNotInRoleException if sponsor hasn't right role in the same vo
	 * @throws AlreadySponsorException sponsoredMember is already sponsored by User and his sponsorship is still active
	 * @throws PrivilegeException if not PerunAdmin
	 */
	RichMember setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor, LocalDate validityTo) throws MemberNotExistsException, AlreadySponsoredMemberException, UserNotInRoleException, AlreadySponsorException, PrivilegeException;

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
	RichMember unsetSponsorshipForMember(PerunSession session, Member sponsoredMember) throws MemberNotExistsException, MemberNotSponsoredException, PrivilegeException;

	/**
	 * Assigns a new sponsor to an existing member.
	 * @param session actor
	 * @param sponsored existing member that needs sponsoring
	 * @param sponsor sponsoring user or null for the caller
	 * @param validityTo last day when the sponsorship is active (null means the sponsorship will last forever)
	 * @return existing Member
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws MemberNotSponsoredException
	 * @throws AlreadySponsorException
	 * @throws UserNotInRoleException
	 */
	RichMember sponsorMember(PerunSession session, Member sponsored, User sponsor, LocalDate validityTo) throws PrivilegeException, MemberNotSponsoredException, AlreadySponsorException, UserNotInRoleException;

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
	List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo, User user, List<String> attrNames) throws AttributeNotExistsException, PrivilegeException, VoNotExistsException, UserNotExistsException;

	/**
	 * Gets list of members of a VO sponsored by the given user.
	 * @param sess actor
	 * @param vo virtual organization from which are the sponsored members chosen
	 * @param user user of system
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 * @return list of members from given VO who are sponsored by the given user.
	 */
	List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo, User user) throws PrivilegeException, VoNotExistsException, UserNotExistsException;

	/**
	 * Gets list of sponsored members of a VO.
	 * @param sess actor
	 * @param vo virtual organization from which are the sponsored members chosen
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 * @return list of members from given VO who are sponsored
	 */
	List<RichMember> getSponsoredMembers(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

	/**
	 * Gets list of all sponsored members of a VO.
	 *
	 * @param sess perun session
	 * @param vo virtual organization from which are the sponsored members chosen
	 * @throws PrivilegeException insufficient permissions
	 * @return list of members from given VO who are sponsored
	 */
	List<RichMember> getAllSponsoredMembers(PerunSession sess, Vo vo) throws PrivilegeException, VoNotExistsException;

	/**
	 * Gets list of sponsored members with sponsors.
	 *
	 * @param sess session
	 * @param vo virtual organization from which are the sponsored members chosen
	 * @param attrNames list of attrNames for selected attributes
	 * @throws VoNotExistsException if given VO does not exist
	 * @throws PrivilegeException if not VOADMIN, VOOBSERVER, PERUNOBSERVER or SPONSOR
	 * @return list of members with sponsors
	 */
	List<MemberWithSponsors> getSponsoredMembersAndTheirSponsors(PerunSession sess, Vo vo, List<String> attrNames) throws VoNotExistsException, PrivilegeException, AttributeNotExistsException;

	/**
	 * Gets list of VO's all sponsored members with sponsors.
	 *
	 * @param sess session
	 * @param vo virtual organization from which are the sponsored members chosen
	 * @param attrNames list of attrNames for selected attributes
	 * @throws VoNotExistsException if given VO does not exist
	 * @throws PrivilegeException if not VOADMIN, VOOBSERVER, PERUNOBSERVER or SPONSOR
	 * @return list of members with sponsors
	 */
	List<MemberWithSponsors> getAllSponsoredMembersAndTheirSponsors(PerunSession sess, Vo vo, List<String> attrNames) throws VoNotExistsException, PrivilegeException, AttributeNotExistsException;

	/**
	 * Extends expiration date. Sponsored members cannot apply for membership extension, this method allows a sponsor to extend it.
	 * @param session actor
	 * @param sponsored existing member that is sponsored
	 * @param sponsor sponsoring user or null for the caller
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 * @return new expiration date
	 */
	String extendExpirationForSponsoredMember(PerunSession session, Member sponsored, User sponsor) throws PrivilegeException, MemberNotExistsException, UserNotExistsException;

	/**
	 * Removes the sponsor.
	 *
	 * @param sess actor
	 * @param sponsoredMember existing member that is sponsored
	 * @param sponsorToRemove sponsoring user for removal
	 * @throws InternalErrorException if given parameters are invalid
	 * @throws PrivilegeException if not REGISTRAR or VOADMIN
	 */
	void removeSponsor(PerunSession sess, Member sponsoredMember, User sponsorToRemove) throws PrivilegeException;

	/**
	 * Get page of members from the given vo, with the given attributes.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param query query with page information
	 * @param attrNames attribute names
	 * @return page of requested rich members
	 * @throws VoNotExistsException if there is no such vo
	 * @throws PrivilegeException insufficient permission
	 */
	Paginated<RichMember> getMembersPage(PerunSession sess, Vo vo, MembersPageQuery query, List<String> attrNames) throws VoNotExistsException, PrivilegeException;

	/**
	 * Update the sponsorship of given member for given sponsor.
	 *
	 * @param sess session
	 * @param sponsoredMember sponsored member
	 * @param sponsor sponsor
	 * @param newValidity new validity, can be set to null never expire
	 * @throws PrivilegeException insufficient permissions
	 * @throws SponsorshipDoesNotExistException if the given user is not sponsor of the given member
	 * @throws MemberNotExistsException if there is no such member
	 * @throws UserNotExistsException if there is no such user
	 */
	void updateSponsorshipValidity(PerunSession sess, Member sponsoredMember, User sponsor, LocalDate newValidity) throws PrivilegeException, SponsorshipDoesNotExistException, MemberNotExistsException, UserNotExistsException;

	/**
	 * Moves membership in VO from source user to target user - moves the source user's
	 * memberships in non-synchronized groups, member related attributes, bans and
	 * sponsorships in the VO. Removes the source user's member object.
	 *
	 * @param sess session
	 * @param vo the VO in which the membership should be moved
	 * @param sourceUser the user to move membership from
	 * @param targetUser the user to move membership to
	 * @throws UserNotExistsException if there is no such user
	 * @throws VoNotExistsException if there is no such VO
	 * @throws PrivilegeException insufficient permissions
	 * @throws MemberNotExistsException when sourceUser is not member of the VO
	 * @throws AlreadyMemberException when targetUser is already member of the VO
	 * @throws ExtendMembershipException when targetUser doesn't have required LOA for the VO
	 */
	void moveMembership(PerunSession sess, Vo vo, User sourceUser, User targetUser) throws UserNotExistsException, VoNotExistsException, PrivilegeException, ExtendMembershipException, MemberNotExistsException, AlreadyMemberException;

	/**
	 * Return all loaded namespaces rules.
	 *
	 * @return all namespaces rules
	 */
	List<NamespaceRules> getAllNamespacesRules();

	/**
	 * Returns rich members by their IDs with specific attributes.
	 *
	 * @param sess Perun session
	 * @param ids List<Integer> list of members IDs
	 * @param attrsNames List<String> Attribute names. If the list is null or empty, no attributes will be provided.
	 * @return List<RichMember> RichMembers with specified IDs and attributes
	 */
	List<RichMember> getRichMembersByIds(PerunSession sess, List<Integer> ids, List<String> attrsNames) throws PrivilegeException, AttributeNotExistsException;
}
