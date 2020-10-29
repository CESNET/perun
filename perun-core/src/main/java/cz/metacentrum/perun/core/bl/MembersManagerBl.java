package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Sponsor;
import cz.metacentrum.perun.core.api.Sponsorship;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
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
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSponsoredException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PasswordCreationFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.SponsorshipDoesNotExistException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotInRoleException;
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
public interface MembersManagerBl {

	/**
	 *  Deletes only member data  appropriated by member id.
	 *
	 * @param sess
	 * @param member
	 * @throws InternalErrorException
	 * @throws MemberAlreadyRemovedException
	 */
	void deleteMember(PerunSession sess, Member member) throws MemberAlreadyRemovedException;

	/**
	 * Delete given members. It is possible to delete members from multiple vos.
	 *
	 * @param sess session
	 * @param members members that will be deleted
	 * @throws InternalErrorException internal error
	 * @throws MemberAlreadyRemovedException if already removed
	 */
	void deleteMembers(PerunSession sess, List<Member> members) throws MemberAlreadyRemovedException;

	/**
	 *  Deletes all VO members.
	 *
	 * @param sess
	 * @param vo
	 * @throws InternalErrorException
	 * @throws MemberAlreadyRemovedException
	 */
	void deleteAllMembers(PerunSession sess, Vo vo) throws MemberAlreadyRemovedException;

	/**
	 * Creates a new member from candidate which is prepared for creating specificUser
	 * In list specificUserOwners can't be specific user, only normal users and sponsored users are allowed.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate prepared future specificUser
	 * @param specificUserOwners list of users who own specificUser (can't be empty or contain specific user)
	 * @param specificUserType type of specific user (service)
	 * @return newly created member (of specific User)
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 */
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

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
	 * @param specificUserOwners list of users who own specificUser (can't be empty or contain specific user)
	 * @param specificUserType type of specific user (service)
	 * @param groups list of groups where member will be added too
	 * @return newly created member (of specific User)
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 */
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates a new member and sets all member's attributes from the candidate.
	 * It can be called in synchronous or asynchronous mode
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
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws WrongReferenceAttributeValueException, WrongAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates a new member and sets all member's attributes from the candidate.
	 * It can be called in synchronous or asynchronous mode
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
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws WrongReferenceAttributeValueException, WrongAttributeValueException, AlreadyMemberException, ExtendMembershipException;

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
	 * @throws ExtendMembershipException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

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
	 * @throws ExtendMembershipException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

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
	 * @throws ExtendMembershipException
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

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
	 * @throws ExtendMembershipException
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates a new member from candidate returned by the method VosManager.findCandidates which fills Candidate.userExtSource.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate
	 * @param specificUserType (Normal or service or sponsored)
	 *
	 * @return newly created members
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, Candidate)
	 */
	Member createMember(PerunSession sess, Vo vo, SpecificUserType specificUserType, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

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
	 * @param specificUserType (Normal or service or sponsored)
	 * @param groups list of groups where member will be added too
	 * @param overwriteUserAttributes list of user attributes names which will be overwrite instead of merged
	 *
	 * @return newly created members
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, Candidate)
	 */
	Member createMember(PerunSession sess, Vo vo, SpecificUserType specificUserType, Candidate candidate, List<Group> groups, List<String> overwriteUserAttributes) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates Specific Member.
	 * This method creates specific member and then validate it <strong>Synchronously</strong>
	 *
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createSpecificMember(PerunSession, Vo, Candidate, List<User>)
	 */
	Member createSpecificMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<User> serviceUserOwners, SpecificUserType specificUserType) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates Specific Member and add him also to all groups in list.
	 * This method creates specific member and then validate it <strong>Synchronously</strong>
	 *
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createSpecificMember(PerunSession, Vo, Candidate, List<User>)
	 */
	Member createSpecificMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Transform non-sponsored member to sponsored one with defined sponsor
	 *
	 * @param session perun session
	 * @param sponsoredMember member who will be set as sponsored one
	 * @param sponsor new sponsor of this member
	 * @param validityTo the last day when the sponsorship is active
	 *
	 * @return sponsored member
	 *
	 * @throws AlreadySponsoredMemberException if member was already flagged as sponsored
	 * @throws UserNotInRoleException if sponsor has not right role in the member's VO
	 * @throws InternalErrorException if something unexpected happened
	 */
	Member setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor, LocalDate validityTo) throws AlreadySponsoredMemberException, UserNotInRoleException;

	/**
	 * Transform non-sponsored member to sponsored one with defined sponsor
	 *
	 * @param session perun session
	 * @param sponsoredMember member who will be set as sponsored one
	 * @param sponsor new sponsor of this member
	 *
	 * @return sponsored member
	 *
	 * @throws AlreadySponsoredMemberException if member was already flagged as sponsored
	 * @throws UserNotInRoleException if sponsor has not right role in the member's VO
	 * @throws InternalErrorException if something unexpected happened
	 */
	Member setSponsorshipForMember(PerunSession session, Member sponsoredMember, User sponsor) throws AlreadySponsoredMemberException, UserNotInRoleException;

	/**
	 * Transform sponsored member to non-sponsored one. Delete all his sponsors.
	 *
	 * @param session perun session
	 * @param sponsoredMember member which who be unset from sponsoring
	 *
	 * @return non-sponsored member
	 *
	 * @throws MemberNotSponsoredException If member was not set as sponsored before calling this method.
	 * @throws InternalErrorException if something unexpected happend
	 */
	Member unsetSponsorshipForMember(PerunSession session, Member sponsoredMember) throws MemberNotSponsoredException;

	/**
	 * Creates member. Runs synchronously.
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates member. Runs synchronously. Add member also to all groups in list.
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates member. Runs synchronously. Add member also to all groups in list.
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups, List<String> overwriteUserAttributes) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates a new member from user.
	 * <strong>This method runs asynchronously</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @return newly created member
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 */
	Member createMember(PerunSession sess, Vo vo, User user) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Creates a new member from user.
	 *
	 * Also add this member to groups in list.
	 *
	 * <strong>This method runs asynchronously</strong>
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
	 * @throws ExtendMembershipException
	 */
	Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

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
	 * @param groups list of groups where member will be added too
	 * @return newly created member
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 */
	Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException;

	/**
	 * Update member in underlaying data source. Member is find by id. Other java attributes are updated.
	 *
	 * @param sess
	 * @param member member who have set new java attributes.
	 * @return updated member
	 *
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	Member updateMember(PerunSession sess, Member member) throws WrongReferenceAttributeValueException, WrongAttributeValueException;

	/**
	 * Find member of this Vo by his login in external source
	 *
	 * @param perunSession
	 * @param vo
	 * @param userExtSource
	 * @return selected user or throws  in case the requested member doesn't exists in this Vo
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Member getMemberByUserExtSource(PerunSession perunSession, Vo vo, UserExtSource userExtSource) throws MemberNotExistsException;

	/**
	 * Get member by its external sources. If the given sources do not belong to a single member
	 * and exception is thrown.
	 *
	 * @param perunSession session
	 * @param vo vo
	 * @param userExtSources ues
	 * @return member
	 * @throws InternalErrorException internal error
	 * @throws MemberNotExistsException member does not exist
	 */
	Member getMemberByUserExtSources(PerunSession perunSession, Vo vo, List<UserExtSource> userExtSources) throws MemberNotExistsException;

	/**
	 * Returns member by his id.
	 *
	 * @param sess
	 * @param id
	 * @return member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Member getMemberById(PerunSession sess, int id) throws MemberNotExistsException;

	/**
	 * Returns member by his user and vo.
	 *
	 * @param sess
	 * @param vo
	 * @param user
	 * @return member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Member getMemberByUser(PerunSession sess, Vo vo, User user) throws MemberNotExistsException;

	/**
	 * Return all VO Members of the User.
	 *
	 * @param sess
	 * @param user
	 * @return List of Members
	 * @throws InternalErrorException
	 */
	List<Member> getMembersByUser(PerunSession sess, User user);

	/**
	 * Return all VO Members of the User, which have specified Status in their VO.
	 *
	 * @param sess
	 * @param user
	 * @param status
	 * @return List of Members
	 * @throws InternalErrorException
	 */
	List<Member> getMembersByUserWithStatus(PerunSession sess, User user, Status status);

	/**
	 * Returns member by his userId.
	 *
	 * @param sess
	 * @param vo
	 * @param userId
	 * @return member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Member getMemberByUserId(PerunSession sess, Vo vo, int userId) throws MemberNotExistsException;

	/**
	 * Get all VO members.
	 *
	 * @param sess
	 * @param vo
	 * @return all members of the VO
	 * @throws InternalErrorException
	 */
	List<Member> getMembers(PerunSession sess, Vo vo);

	/**
	 * Get all VO members who have the status.
	 *
	 * @param sess
	 * @param vo
	 * @param status get only members who have this status. If status is null return all members.
	 * @return all members of the VO
	 * @throws InternalErrorException
	 */
	List<Member> getMembers(PerunSession sess, Vo vo, Status status);

	/**
	 * Get Member to RichMember with attributes.
	 *
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	RichMember getRichMember(PerunSession sess, Member member);

	/**
	 * Get Member to RichMember with attributes.
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	RichMember getRichMemberWithAttributes(PerunSession sess, Member member);

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsDef
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef);

	/**
	 * Get rich members for displaying on pages. Rich member object contains user, member, userExtSources, userAttributes, memberAttributes.
	 *
	 * @param sess
	 * @param group
	 * @param allowedStatuses
	 * @return list of rich members on specified page, empty list if there are no user in this group or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group);

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsNames for vo.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames list of attrNames for selected attributes
	 * @return list of RichMembers
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws AttributeNotExistsException;

	/**
	 * Get all RichMembers with attributes specific for list of attrsNames from the vo.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames
	 * @return list of richMembers with specific attributes from Vo
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws AttributeNotExistsException;

	/**
	 * Get all RichMembers with attributes specific for list of attrsNames from the vo and have only
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
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws AttributeNotExistsException;

	/**
	 * Get all RichMembers with attributes specific for list of attrsNames from the group.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * If lookingInParentGroup is true, get all these richMembers only for parentGroup of this group.
	 * If this group is top level group, so get richMembers from members group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsNames
	 * @param lookingInParentGroup
	 * @return list of richMembers with specific attributes from group
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws AttributeNotExistsException, ParentGroupNotExistsException;

	/**
	 * Get all RichMembers with attributes specific for list of attrsNames from the group and have only
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
	 * @throws AttributeNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws AttributeNotExistsException, ParentGroupNotExistsException;

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
	 * @throws GroupResourceMismatchException
	 */
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, Resource resource, List<String> attrsNames, List<String> allowedStatuses) throws AttributeNotExistsException, GroupResourceMismatchException;

	/**
	 * Return list of richMembers for specific vo by the searchString with attributes specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames
	 * @param searchString
	 * @param onlySponsored return only sponsored members
	 * @return list of founded richMembers with specific attributes from Vo for searchString
	 * @throws InternalErrorException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString, boolean onlySponsored);

	/**
	 * Return list of richMembers by the searchString with attributes specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * @param sess
	 * @param attrsNames
	 * @param searchString
	 * @return list of founded richMembers with specific attributes from Vo for searchString
	 * @throws InternalErrorException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, String searchString);

	/**
	 * Return list of richMembers for specific vo by the searchString with attributes specific for list of attrsNames
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
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString);

	/**
	 * Return list of richMembers by the searchString with attributes specific for list of attrsNames
	 * and who have only status which is contain in list of statuses.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 * If listOfStatuses is empty or null, return all possible statuses.
	 *
	 * @param sess
	 * @param attrsNames
	 * @param allowedStatuses
	 * @param searchString
	 * @return list of founded richMembers with specific attributes by searchString with allowed statuses
	 * @throws InternalErrorException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString);

	/**
	 * Return list of richMembers for specific group by the searchString with attributes specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.

	 * If lookingInParentGroup is true, find all these richMembers only for parentGroup of this group.
	 * If this group is top level group, so find richMembers from members group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsNames
	 * @param lookingInParentGroup
	 * @param searchString
	 * @return list of founded richMembers with specific attributes from Group for searchString
	 * @throws InternalErrorException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws ParentGroupNotExistsException;

	/**
	 * Return list of richMembers for specific group by the searchString with attributes specific for list of attrsNames
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
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws ParentGroupNotExistsException;

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for group.
	 *
	 * @param sess
	 * @param group
	 * @param attrsNames list of attrNames for selected attributes
	 * @return list of RichMembers
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws AttributeNotExistsException;

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef for group.
	 * Get also user-facility (as user attribute in rich member) and member-resource (as member attributes in rich member)
	 * attributes by resource.
	 *
	 * @param sess
	 * @param group
	 * @param resource
	 * @param attrsNames list of attrNames for selected attributes
	 * @return list of RichMembers
	 * @throws AttributeNotExistsException
	 * @throws InternalErrorException
	 * @throws GroupResourceMismatchException
	 */
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, Resource resource, List<String> attrsNames) throws AttributeNotExistsException, GroupResourceMismatchException;


	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef.
	 *
	 * @param sess
	 * @param group
	 * @param attrsDef
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef);

	/**
	 * Get rich members for displaying on pages. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param vo
	 * @return list of rich members on specified page, empty list if there are no user in this VO or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Vo vo);

	/**
	 * Get rich members for displaying on pages. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param group
	 * @return list of rich members on specified page, empty list if there are no user in this Group or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Group group);

	/**
	 * Get rich members who have the status, for displaying on pages. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param vo
	 * @param status get only members who have this status. If status is null return all members.
	 * @return list of rich members on specified page, empty list if there are no user in this VO or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status);

	/**
	 * Get rich members for displaying on pages. Rich member object contains user, member, userExtSources, userAttributes, memberAttributes.
	 *
	 * @param sess
	 * @param vo
	 * @return list of rich members on specified page, empty list if there are no user in this VO or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo);

	/**
	 * Get rich members who have the status, for displaying on pages. Rich member object contains user, member, userExtSources, userAttributes, memberAttributes.
	 *
	 * @param sess
	 * @param vo
	 * @param status get only members who have this status. If status is null return all members.
	 * @return list of rich members on specified page, empty list if there are no user in this VO or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status);

	/**
	 * Convert list of users' ids into the list of members.
	 *
	 * @param sess
	 * @param usersIds
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> getMembersByUsersIds(PerunSession sess, List<Integer> usersIds, Vo vo);

	/**
	 * Convert list of users into the list of members.
	 *
	 * @param sess
	 * @param users
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> getMembersByUsers(PerunSession sess, List<User> users, Vo vo);


	/**
	 * Fill the RichMember object with data from Member and corresponding User.
	 *
	 * @param sess
	 * @param members
	 * @return list of richMembers
	 * @throws InternalErrorException
	 */
	List<RichMember> convertMembersToRichMembers(PerunSession sess, List<Member> members);

	/**
	 * Fill the RichMember object with data from Member and corresponding User and user/member attributes.
	 *
	 * @param sess
	 * @param richMembers
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers);

	/**
	 * Fill the RichMember object with data from Member and corresponding User and user/member attributes defined by list of attribute definition.
	 *
	 * @param sess
	 * @param richMembers
	 * @param attrsDef
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers, List<AttributeDefinition> attrsDef);

	/**
	 * Convert given User to the Sponsor object. For the sponsor object, there is loaded information
	 * about the sponsorship. Also, if the given user is a RichUser, all of its attributes and userExtSources
	 * are also set to the sponsor object.
	 *
	 * @param sess session
	 * @param user a User or a RichUser object
	 * @param sponsoredMember member, to which the sponsorship information is loaded
	 * @return Sponsor object created from given user object with addition info about sponsorship for the given member.
	 */
	Sponsor convertUserToSponsor(PerunSession sess, User user, Member sponsoredMember);

	/**
	 * Fill the RichMember object with data from Member and corresponding User, user/member, user-facility and member-resource attributes defined by list of attribute definition.
	 *
	 * @param sess
	 * @param richMembers
	 * @param resource
	 * @param attrsDef
	 * @return
	 * @throws InternalErrorException
	 * @throws MemberResourceMismatchException
	 */
	List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers, Resource resource, List<AttributeDefinition> attrsDef) throws MemberResourceMismatchException;

	/**
	 * Get the VO members count.
	 *
	 * @param sess
	 * @param vo
	 * @return count of VO members
	 * @throws InternalErrorException
	 */
	int getMembersCount(PerunSession sess, Vo vo);

	/**
	 * Returns number of Vo members with defined status.
	 *
	 * @param sess
	 * @param vo
	 * @param status
	 * @return number of members
	 * @throws InternalErrorException
	 */
	int getMembersCount(PerunSession sess, Vo vo, Status status);

	/**
	 * Get the member VO.
	 *
	 * @param sess
	 * @param member
	 * @return member's VO
	 * @throws InternalErrorException
	 */
	Vo getMemberVo(PerunSession sess, Member member);

	/**
	 * Return list of members by theirs name.
	 * @param sess
	 * @param searchString
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> findMembersByName(PerunSession sess, String searchString);

	/**
	 * Return list of members by theirs name under defined VO.
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString);

	/**
	 * Return list of members by the searchString under defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group
	 * @param searchString
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString);

	/**
	 * Return list of members by the searchString udner parentGroup of defined Group. Search is done in name, email and login.
	 * If the group is top-level group, searching in "members" group of vo in which the group exists.
	 *
	 * @param sess
	 * @param group this group is used to get parent group, we are searching members of the parent group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 */
	List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString);

	/**
	 * Return list of rich members with certain attributes by the searchString under defined Group. Search is done in name, email and login.
	 * @param sess session
	 * @param group group
	 * @param searchString search string
	 * @param attrsNames list of attributes that should be found
	 * @return list of rich members with certain attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString, List<String> attrsNames);

	/**
	 * Return list of rich members with attributes by the searchString under defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString);

	/**
	 * Return list of rich members with attributes by the searchString under parent group of defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group this group is used to get parent group, we are searching members of the parent group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString);

	/**
	 * Return list of members by theirs name or login or email under defined VO.
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString);

	/**
	 * Return list of rich members by theirs name or login or email under defined VO.
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @param onlySponsored return only sponsored members
	 * @return list of rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString, boolean onlySponsored);

	/**
	 * Return list of rich members by theirs name or login or email
	 * @param sess
	 * @param searchString
	 * @param onlySponsored return only sponsored members
	 * @return list of rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembers(PerunSession sess, String searchString, boolean onlySponsored);

	/**
	 * Return list of rich members with certain attributes by theirs name or login or email defined VO.
	 * @param sess session
	 * @param vo vo
	 * @param searchString search string
	 * @param attrsNames list of attribute names that should be found
	 * @param onlySponsored return only sponsored members
	 * @return list of rich members with certain attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString, List<String> attrsNames, boolean onlySponsored);

	/**
	 * Return list of rich members with attributes by theirs name or login or email under defined VO.
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of rich members with attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString);

	/**
	 * Return list of rich members with certain attributes by theirs name or login or email.
	 * @param sess session
	 * @param searchString search string
	 * @param attrsNames list of attribute names that should be found
	 * @return list of rich members with certain attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributes(PerunSession sess, String searchString, List<String> attrsNames);

	/**
	 * Return list of rich members with attributes by theirs name or login or email
	 * @param sess
	 * @param searchString
	 * @return list of rich members with attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributes(PerunSession sess, String searchString);

	void checkMemberExists(PerunSession sess, Member member) throws MemberNotExistsException;

	/**
	 * Set date to which will be member suspended in his VO.
	 *
	 * For almost unlimited time please use time in the far future.
	 *
	 * @param sess
	 * @param member member who will be suspended
	 * @param suspendedTo date to which will be member suspended (after this date, he will not be affected by suspension any more)
	 * @throws InternalErrorException
	 */
	void suspendMemberTo(PerunSession sess, Member member, Date suspendedTo);

	/**
	 * Remove suspend state from Member - remove date to which member should be considered as suspended in the VO.
	 *
	 * WARNING: this method will always succeed if member exists, because it will set date for suspension to null
	 *
	 * @param sess
	 * @param member member for which the suspend state will be removed
	 * @throws InternalErrorException
	 */
	void unsuspendMember(PerunSession sess, Member member);

	/**
	 * Return false if member has status INVALID or DISABLED. True in other cases.
	 *
	 * @param sess
	 * @param member the member
	 * @return false if member has INVALID or DISABLED status, true in other cases
	 * @throws InternalErrorException
	 */
	boolean isMemberAllowed(PerunSession sess, Member member);

	/**
	 *  Set status of the member to specified status.
	 *
	 * @param sess
	 * @param member
	 * @param status new status
	 * @return member with status set
	 *
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws MemberNotValidYetException
	 * @throws WrongAttributeValueException
	 */
	Member setStatus(PerunSession sess, Member member, Status status) throws WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException;

	/**
	 * Validate all atributes for member and set member's status to VALID.
	 * This method runs synchronously.
	 *
	 * Method runs in nested transaction.
	 * As side effect, on success will change status of the object member.
	 *
	 * @param sess
	 * @param member
	 * @return membet with new status set
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	Member validateMember(PerunSession sess, Member member) throws WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Validate all attributes for member and then set member's status to VALID.
	 * This method runs asynchronously. It immediately return member with <b>ORIGINAL</b> status and after asynchronous validation sucessfuly finishes
	 * it switch member's status to VALID. If validation ends with error, member keeps his status.
	 *
	 * @param sess
	 * @param member
	 * @return member with original status
	 *
	 */
	Member validateMemberAsync(PerunSession sess, Member member);

	/**
	 * Set member status to invalid.
	 *
	 * As side effect it will change status of the object member.
	 *
	 * @param sess
	 * @param member
	 * @return member with new status set
	 *
	 * @throws InternalErrorException
	 */
	Member invalidateMember(PerunSession sess, Member member);

	/**
	 * Set member's status to expired.
	 * All attributes are validated if was in INVALID or DISABLED state before.
	 * If validation ends with error, member keeps his old status.
	 *
	 * Method runs in nested transaction.
	 * As side effect, on success will change status of the object member.
	 *
	 * @param sess
	 * @param member
	 * @return member with new status set
	 *
	 * @throws InternalErrorException
	 * @throws WrongReferenceAttributeValueException
	 * @throws WrongAttributeValueException
	 */
	Member expireMember(PerunSession sess, Member member) throws WrongReferenceAttributeValueException, WrongAttributeValueException;

	/**
	 * Disable member.
	 *
	 * As side effect, on success will change status of the object member.
	 *
	 * @param sess
	 * @param member
	 * @return member with new status set
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotValidYetException
	 */
	Member disableMember(PerunSession sess, Member member) throws MemberNotValidYetException;

	/**
	 * Retain only members with specified status.
	 *
	 * @param sess
	 * @param members
	 * @param status
	 * @return
	 *
	 * @throws MemberNotValidYetException
	 */
	List<Member> retainMembersWithStatus(PerunSession sess, List<Member> members, Status status);

	/**
	 * Return true if member have specified status.
	 *
	 * @param sess
	 * @param member
	 * @param status
	 * @return true if member have the specified status
	 *         false otherwise
	 */
	boolean haveStatus(PerunSession sess, Member member, Status status);

	/**
	 * Extend member membership using membershipExpirationRules attribute defined at VO.
	 *
	 * @param sess
	 * @param member
	 * @throws InternalErrorException
	 * @throws ExtendMembershipException
	 */
	void extendMembership(PerunSession sess, Member member) throws ExtendMembershipException;

	/**
	 * Return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise false.
	 *
	 * @param sess
	 * @param member
	 * @return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise false
	 * @throws InternalErrorException
	 */
	boolean canExtendMembership(PerunSession sess, Member member);

	/**
	 * Return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise throws exception.
	 *
	 * @param sess
	 * @param member
	 * @return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise throws exception with reason
	 * @throws InternalErrorException
	 * @throws ExtendMembershipException
	 */
	boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws ExtendMembershipException;

	/**
	 * Checks if the user can apply membership to the VO, it decides based on extendMembershipRules on the doNotAllowLoa key
	 * @param sess
	 * @param vo
	 * @param user
	 * @param loa
	 * @return true if user can be apply for membership to the VO
	 * @throws InternalErrorException
	 */
	boolean canBeMember(PerunSession sess, Vo vo, User user, String loa);

	/**
	 * Checks if the user can apply membership to the VO, it decides based on extendMembershipRules on the doNotAllowLoa key
	 * @param sess
	 * @param vo
	 * @param user
	 * @param loa
	 * @return true if user can be apply for membership to the VO, exception with reason otherwise
	 * @throws InternalErrorException
	 * @throws ExtendMembershipException
	 */
	boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws ExtendMembershipException;

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
	 * @throws InternalErrorException
	 */
	Member getMemberByExtSourceNameAndExtLogin(PerunSession sess, Vo vo, String extSourceName, String extLogin) throws ExtSourceNotExistsException, UserExtSourceNotExistsException, MemberNotExistsException, UserNotExistsException;

	/**
	 * Returns the date to which will be extended member's expiration time.
	 *
	 * @param sess
	 * @param member
	 * @return date
	 * @throws InternalErrorException
	 */
	Date getNewExtendMembership(PerunSession sess, Member member);

	/**
   * Returns the date to which will be extended potential member of the VO.
   *
   * @param sess
   * @param vo
   * @param loa
   * @return date
   * @throws InternalErrorException
   * @throws ExtendMembershipException
   */
  Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws ExtendMembershipException;

	/**
	 * For richMember filter all his user and member attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richMember
	 * @return richMember with only allowed attributes
	 * @throws InternalErrorException
	 */
	RichMember filterOnlyAllowedAttributes(PerunSession sess, RichMember richMember);

	/**
	 * For list of richMembers filter all their user and member attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richMembers
	 * @return list of richMembers with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> filterOnlyAllowedAttributes(PerunSession sess, List<RichMember> richMembers);

	/**
	 * For list of richMembers filter all their user and member attributes and remove all which principal has no access to.
	 *
	 * Context means that voId for all members is same (rules can be same for all members in list)
	 *
	 * if useContext is true: every attribute is unique in context of friendlyName, which means more attributes for more members have same
	 * rules if friendly name is same for all of them (better performance, worse authorization check)
	 * if useContext is false: every attribute is unique in context of member, which means every attribute for more members need to be check separately,
	 * because for example members can be from different vos (better authorization check, worse performance)
	 *
	 * @param sess
	 * @param richMembers list of richMembers for which attributes need to be filtered
	 * @param group
	 * @param useContext true or false means using context or not using context (more above in javadoc)
	 *
	 * @return list of richMembers with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> filterOnlyAllowedAttributes(PerunSession sess, List<RichMember> richMembers, Group group, boolean useContext);

	/**
	 * Send mail to user's preferred email address with link for non-authz password reset.
	 * Correct authz information is stored in link's URL.
	 *
	 * @param sess PerunSession
	 * @param member Member to get user to send link mail to
	 * @param namespace Namespace to reset password in (member must have login in)
	 * @param url base URL of Perun instance
	 * @param mailAddress mail address where email will be sent
	 * @param language language of the message
	 * @throws InternalErrorException
	 */
	void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url, String mailAddress, String language);

	/**
	 * Creates a new sponsored member.
	 *
	 * @param session perun session
	 * @param vo virtual organization
	 * @param namespace used for selecting external system in which guest user account will be created
	 * @param name a map containing the full name or its parts (mandatory: firstName, lastName; optionally: titleBefore, titleAfter)
	 * @param password password
	 * @param email (optional) preferred email that will be set to the created user. If no email
	 *              is provided, "no-reply@muni.cz" is used.
	 * @param sponsor sponsoring user
	 * @param asyncValidation
	 * @return created member
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws LoginNotExistsException
	 * @throws PasswordCreationFailedException
	 * @throws ExtendMembershipException
	 * @throws WrongAttributeValueException
	 * @throws ExtSourceNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws UserNotInRoleException if the member is not in required role
	 */
	Member createSponsoredMember(PerunSession session, Vo vo, String namespace, Map<String, String> name, String password, String email, User sponsor, boolean asyncValidation) throws AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException, UserNotInRoleException, PasswordStrengthException, InvalidLoginException;

	/**
	 * Creates a new sponsored member.
	 *
	 * @param session perun session
	 * @param vo virtual organization
	 * @param namespace used for selecting external system in which guest user account will be created
	 * @param name a map containing the full name or its parts (mandatory: firstName, lastName; optionally: titleBefore, titleAfter)
	 * @param password password
	 * @param email (optional) preferred email that will be set to the created user. If no email
	 *              is provided, "no-reply@muni.cz" is used.
	 * @param sponsor sponsoring user
	 * @param validityTo last day when the sponsorship is active (null means the sponsorship will last forever)
	 * @param asyncValidation
	 * @return created member
	 * @throws InternalErrorException
	 * @throws AlreadyMemberException
	 * @throws LoginNotExistsException
	 * @throws PasswordCreationFailedException
	 * @throws ExtendMembershipException
	 * @throws WrongAttributeValueException
	 * @throws ExtSourceNotExistsException
	 * @throws WrongReferenceAttributeValueException
	 * @throws UserNotInRoleException if the member is not in required role
	 */
	Member createSponsoredMember(PerunSession session, Vo vo, String namespace, Map<String, String> name, String password, String email, User sponsor, LocalDate validityTo, boolean asyncValidation) throws AlreadyMemberException, LoginNotExistsException, PasswordCreationFailedException, ExtendMembershipException, WrongAttributeValueException, ExtSourceNotExistsException, WrongReferenceAttributeValueException, UserNotInRoleException, PasswordStrengthException, InvalidLoginException;

	/**
	 * Creates a sponsored membership for the given user.
	 *
	 * @param session perun session
	 * @param vo virtual organization
	 * @param userToBeSponsored user, that will be sponsored by sponsor
	 * @param namespace used for selecting external system in which guest user account will be created
	 * @param password password
	 * @param sponsor sponsoring user
	 * @param asyncValidation
	 * @return sponsored member
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @throws UserNotInRoleException
	 * @throws PasswordStrengthException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws LoginNotExistsException
	 * @throws PasswordCreationFailedException
	 * @throws InvalidLoginException
	 * @throws ExtSourceNotExistsException
	 */
	Member setSponsoredMember(PerunSession session, Vo vo, User userToBeSponsored, String namespace, String password, User sponsor, LocalDate validityTo, boolean asyncValidation) throws AlreadyMemberException, ExtendMembershipException, UserNotInRoleException, PasswordStrengthException, WrongAttributeValueException, WrongReferenceAttributeValueException, LoginNotExistsException, PasswordCreationFailedException, InvalidLoginException, ExtSourceNotExistsException;

	/**
	 * Creates a sponsored membership for the given user.
	 *
	 * @param session perun session
	 * @param vo virtual organization
	 * @param userToBeSponsored user, that will be sponsored by sponsor
	 * @param namespace used for selecting external system in which guest user account will be created
	 * @param password password
	 * @param sponsor sponsoring user
	 * @param asyncValidation
	 * @return sponsored member
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @throws UserNotInRoleException
	 * @throws PasswordStrengthException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws LoginNotExistsException
	 * @throws PasswordCreationFailedException
	 * @throws InvalidLoginException
	 * @throws ExtSourceNotExistsException
	 */
	Member setSponsoredMember(PerunSession session, Vo vo, User userToBeSponsored, String namespace, String password, User sponsor, boolean asyncValidation) throws AlreadyMemberException, ExtendMembershipException, UserNotInRoleException, PasswordStrengthException, WrongAttributeValueException, WrongReferenceAttributeValueException, LoginNotExistsException, PasswordCreationFailedException, InvalidLoginException, ExtSourceNotExistsException;

	/**
	 * Creates new sponsored members.
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
	 * @param vo virtual organization to created sponsored members in
	 * @param namespace used for selecting external system in which guest user account will be created
	 * @param names names of members to create, single name should have the format {firstName};{lastName} to be
	 *              parsed well
	 * @param email (optional) preferred email that will be set to the created user. If no email
	 *              is provided, "no-reply@muni.cz" is used.
	 * @param sponsor sponsoring user
	 * @param asyncValidation switch for easier testing
	 * @return map of names to map of status, login and password
	 */
	Map<String, Map<String, String>> createSponsoredMembers(PerunSession session, Vo vo, String namespace, List<String> names, String email, User sponsor, LocalDate validityTo, boolean asyncValidation);

	/**
	 * Links sponsored member and sponsoring user.
	 * @param session perun session
	 * @param sponsoredMember member which is sponsored
	 * @param sponsor sponsoring user
	 * @return member
	 * @throws InternalErrorException
	 * @throws MemberNotSponsoredException
	 * @throws AlreadySponsorException
	 * @throws UserNotInRoleException
	 */
	Member sponsorMember(PerunSession session, Member sponsoredMember, User sponsor) throws MemberNotSponsoredException, AlreadySponsorException, UserNotInRoleException;

	/**
	 * Links sponsored member and sponsoring user.
	 *
	 * @param session perun session
	 * @param sponsoredMember member which is sponsored
	 * @param sponsor sponsoring user
	 * @param validityTo last day when the sponsorship is active (null means the sponsorship will last forever)
	 *
	 * @return member
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotSponsoredException
	 * @throws AlreadySponsorException
	 * @throws UserNotInRoleException
	 */
	Member sponsorMember(PerunSession session, Member sponsoredMember, User sponsor, LocalDate validityTo) throws MemberNotSponsoredException, AlreadySponsorException, UserNotInRoleException;

	/**
	 * For the given member and user returns their sponsorship relation object. If there is no
	 * such relation, the SponsorshipDoesNotExistException is thrown.
	 *
	 * @param sess session
	 * @param sponsoredMember sponsored member
	 * @param sponsor sponsor
	 * @return Sponsorship object
	 * @throws SponsorshipDoesNotExistException if there is no sponsorship relation between the given member and user
	 */
	Sponsorship getSponsorship(PerunSession sess, Member sponsoredMember, User sponsor) throws SponsorshipDoesNotExistException;
	/**
	 * Gets list of members that are sponsored by the user in the vo.
	 *
	 * @param sess perun session
	 * @param vo virtual organization
	 * @param user user sponsoring members
	 * @return list of members sponsored by the user in VO
	 * @throws InternalErrorException if given parameters are invalid
	 */
	List<Member> getSponsoredMembers(PerunSession sess, Vo vo, User user);

	/**
	 * Gets list of members that are sponsored by the user in all vos.
	 *
	 * @param sess perun session
	 * @param user user sponsoring members
	 * @return list of members sponsored by the user in VO
	 * @throws InternalErrorException if given parameters are invalid
	 */
	List<Member> getSponsoredMembers(PerunSession sess, User user);

	/**
	 * Gets list of sponsored members of a VO.
	 * @param sess session
	 * @param vo virtual organization from which are the sponsored members chosen
	 * @throws InternalErrorException if given parameters are invalid
	 * @return list of members from given vo who are sponsored
	 */
	List<Member> getSponsoredMembers(PerunSession sess, Vo vo);

	/**
	 * Removes a sponsor.
	 * @param sess perun session
	 * @param sponsoredMember member which is sponsored
	 * @param sponsor sponsoring user
	 * @throws InternalErrorException if given parameters are invalid
	 */
	void removeSponsor(PerunSession sess, Member sponsoredMember, User sponsor);

	/**
	 * Extends expiration date. Sponsored members cannot apply for membership extension, this method allows a sponsor to extend it.
	 *
	 * @param sess perun session
	 * @param sponsoredMember member which is sponsored
	 * @param sponsorUser sponsoring user or null for the caller
	 * @return new expiration date
	 * @throws InternalErrorException
	 */
	String extendExpirationForSponsoredMember(PerunSession sess, Member sponsoredMember, User sponsorUser);

	/**
	 * Returns unified result of MemberGroupStatus for specified member and resource.
	 *
	 * If member is VALID in at least one group assigned to the resource, result is VALID.
	 * If member is not VALID in any of groups assigned to the resource, result is EXPIRED.
	 * If member is not assigned to the resource at all, result is NULL.
	 *
	 * MemberGroupStatus is never related to the members status in a VO as a whole!
	 *
	 * @param sess PerunSession
	 * @param member Member to get unified MemberGroupStatus
	 * @param resource Resource to get unified MemberGroupStatus
	 * @return MemberGroupStatus for member unified through all his groups assigned to the resource.
	 */
	MemberGroupStatus getUnifiedMemberGroupStatus(PerunSession sess, Member member, Resource resource);

	/**
	 * Returns unified result of MemberGroupStatus for specified user and facility.
	 *
	 * If user is VALID in at least one group assigned to at least one resource on facility, result is VALID.
	 * If user is not VALID in any of groups assigned to any of resources, result is EXPIRED.
	 * If user is not assigned to the resource at all, result is NULL.
	 *
	 * MemberGroupStatus is never related to the members status in any VO!
	 *
	 * @param sess PerunSession
	 * @param user User to get unified MemberGroupStatus
	 * @param facility Facility to get unified MemberGroupStatus
	 * @return MemberGroupStatus for user unified throught all his groups assigned to any of resources of facility.
	 */
	MemberGroupStatus getUnifiedMemberGroupStatus(PerunSession sess, User user, Facility facility);

	/**
	 * Return list of members VO by specific string.
	 * All searches are case insensitive.
	 * Looking for searchString in member mail, user preferredMail, logins, name and IDs (user and member).
	 * If parameter onlySponsored is true, it will return only sponsored members by searchString.
	 * If vo is null, looking for any members in whole Perun. If vo is not null, looking only in specific VO.<
	 *
	 * @param sess
	 * @param vo for which searching will be filtered, if null there is no filter for vo
	 * @param searchString it will be looking for this search string in the specific parameters in DB
	 * @param onlySponsored it will return only sponsored members in vo
	 * @return all members from specific VO by specific string
	 * @throws InternalErrorException
	 */
	List<Member> findMembers(PerunSession sess, Vo vo, String searchString, boolean onlySponsored);

	/**
	 * Update the sponsorship of given member for given sponsor.
	 *
	 * @param sess session
	 * @param sponsoredMember sponsored member
	 * @param sponsor sponsor
	 * @param newValidity new validity, can be set to null never expire
	 * @throws SponsorshipDoesNotExistException if the given user is not sponsor of the given member
	 */
	void updateSponsorshipValidity(PerunSession sess, Member sponsoredMember, User sponsor, LocalDate newValidity) throws SponsorshipDoesNotExistException;

	/**
	 * Returns sponsorship, which have validityTo in range [from, to).
	 * (from is inclusive, to is exclusive).
	 *
	 * @param sess session
	 * @param from lower validityTo bound (inclusive), use LocalDate.MIN if you don't want to specify the lower bound
	 * @param to upper validityTo bound (exclusive), use LocalDate.MAX, if you don't want to specify the upper bound
	 * @return list of sponsorships which have validityTo set in the given range
	 */
	List<Sponsorship> getSponsorshipsExpiringInRange(PerunSession sess, LocalDate from, LocalDate to);
}
