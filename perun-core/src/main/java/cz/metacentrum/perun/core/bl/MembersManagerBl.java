package cz.metacentrum.perun.core.bl;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichMember;
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
import cz.metacentrum.perun.core.api.exceptions.GroupOperationsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotValidYetException;
import cz.metacentrum.perun.core.api.exceptions.ParentGroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

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
	 * @throws GroupOperationsException
	 */
	void deleteMember(PerunSession sess, Member member) throws InternalErrorException, MemberAlreadyRemovedException, GroupOperationsException;

	/**
	 *  Deletes all VO members.
	 *
	 * @param sess
	 * @param vo
	 * @throws InternalErrorException
	 * @throws MemberAlreadyRemovedException
	 * @throws GroupOperationsException
	 */
	void deleteAllMembers(PerunSession sess, Vo vo) throws InternalErrorException, MemberAlreadyRemovedException, GroupOperationsException;

	/**
	 * Creates a new member from candidate which is prepared for creating specificUser
	 * In list specificUserOwners can't be specific user, only normal users and sponsored users are allowed.
	 * <strong>This method runs WITHOUT synchronization. If validation is needed, need to call concrete validateMember method (validateMemberAsync recommended).</strong>
	 *
	 * @param sess
	 * @param vo
	 * @param candidate prepared future specificUser
	 * @param specificUserOwners list of users who own specificUser (can't be empty or contain specific user)
	 * @param specificUserType type of specific user (service or sponsored)
	 * @return newly created member (of specific User)
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @throws GroupOperationsException
	 */
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @param specificUserType type of specific user (service or sponsored)
	 * @param groups list of groups where member will be added too
	 * @return newly created member (of specific User)
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @throws GroupOperationsException
	 */
	Member createSpecificMember(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	public Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, String extSourceName, String extSourceType, int extSourceLoa, String login, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMember(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMember(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, Candidate)
	 */
	Member createMember(PerunSession sess, Vo vo, SpecificUserType specificUserType, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @param SpecificUserType (Normal or service or sponsored)
	 * @param groups list of groups where member will be added too
	 * @param overwriteUserAttributes list of user attributes names which will be overwrite instead of merged
	 *
	 * @return newly created members
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 * @throws AlreadyMemberException
	 * @throws ExtendMembershipException
	 * @throws GroupOperationsException
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, Candidate)
	 */
	Member createMember(PerunSession sess, Vo vo, SpecificUserType specificUserType, Candidate candidate, List<Group> groups, List<String> overwriteUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

	/**
	 * Creates Specific Member.
	 * This method creates specific member and then validate it <strong>Synchronously</strong>
	 *
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createSpecificMember(PerunSession, Vo, Candidate, List<User>)
	 */
	Member createSpecificMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<User> serviceUserOwners, SpecificUserType specificUserType) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

	/**
	 * Creates Specific Member and add him also to all groups in list.
	 * This method creates specific member and then validate it <strong>Synchronously</strong>
	 *
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createSpecificMember(PerunSession, Vo, Candidate, List<User>)
	 */
	Member createSpecificMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<User> specificUserOwners, SpecificUserType specificUserType, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

	/**
	 * Creates member. Runs synchronously.
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

	/**
	 * Creates member. Runs synchronously. Add member also to all groups in list.
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

	/**
	 * Creates member. Runs synchronously. Add member also to all groups in list.
	 * @see cz.metacentrum.perun.core.bl.MembersManagerBl#createMember(PerunSession, Vo, boolean, Candidate)
	 */
	Member createMemberSync(PerunSession sess, Vo vo, Candidate candidate, List<Group> groups, List<String> overwriteUserAttributes) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, User user) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, User user, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	 * @throws GroupOperationsException
	 */
	Member createMember(PerunSession sess, Vo vo, ExtSource extSource, String login, List<Group> groups) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, AlreadyMemberException, ExtendMembershipException, GroupOperationsException;

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
	Member updateMember(PerunSession sess, Member member) throws InternalErrorException, WrongReferenceAttributeValueException, WrongAttributeValueException;

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
	Member getMemberByUserExtSource(PerunSession perunSession, Vo vo, UserExtSource userExtSource) throws InternalErrorException, MemberNotExistsException;

	/**
	 * Get member by one of the userExtSource.
	 *
	 * @param perunSession
	 * @param vo
	 * @param userExtSources
	 * @return member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Member getMemberByUserExtSources(PerunSession perunSession, Vo vo, List<UserExtSource> userExtSources) throws InternalErrorException, MemberNotExistsException;

	/**
	 * Returns member by his id.
	 *
	 * @param sess
	 * @param id
	 * @return member
	 * @throws InternalErrorException
	 * @throws MemberNotExistsException
	 */
	Member getMemberById(PerunSession sess, int id) throws InternalErrorException, MemberNotExistsException;

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
	Member getMemberByUser(PerunSession sess, Vo vo, User user) throws InternalErrorException, MemberNotExistsException;

	/**
	 * Returns members by his user.
	 *
	 * @param sess
	 * @param user
	 * @return member
	 * @throws InternalErrorException
	 */
	List<Member> getMembersByUser(PerunSession sess, User user) throws InternalErrorException;

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
	Member getMemberByUserId(PerunSession sess, Vo vo, int userId) throws InternalErrorException, MemberNotExistsException;

	/**
	 * Get all VO members.
	 *
	 * @param sess
	 * @param vo
	 * @return all members of the VO
	 * @throws InternalErrorException
	 */
	List<Member> getMembers(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get all VO members who have the status.
	 *
	 * @param sess
	 * @param vo
	 * @param status get only members who have this status. If status is null return all members.
	 * @return all members of the VO
	 * @throws InternalErrorException
	 */
	List<Member> getMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException;

	/**
	 * Get Member to RichMember with attributes.
	 *
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	RichMember getRichMember(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Get Member to RichMember with attributes.
	 * @param sess
	 * @param member
	 * @return
	 * @throws InternalErrorException
	 */
	RichMember getRichMemberWithAttributes(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsDef
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, List<AttributeDefinition> attrsDef) throws InternalErrorException;

	/**
	 * Get rich members for displaying on pages. Rich member object contains user, member, userExtSources, userAttributes, memberAttributes.
	 *
	 * @param sess
	 * @param group
	 * @param allowedStatuses
	 * @return list of rich members on specified page, empty list if there are no user in this group or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, List<String> allowedStatuses, Group group) throws InternalErrorException;

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
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, AttributeNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames) throws InternalErrorException, AttributeNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses) throws InternalErrorException, AttributeNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, boolean lookingInParentGroup) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException;

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
	List<RichMember> getCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, boolean lookingInParentGroup) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException;

	/**
	 * Return list of richMembers for specific vo by the searchString with attributes specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * @param sess
	 * @param vo
	 * @param attrsNames
	 * @param searchString
	 * @return list of founded richMembers with specific attributes from Vo for searchString
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, String searchString) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Return list of richMembers by the searchString with attributes specific for list of attrsNames.
	 * If attrsNames is empty or null return all attributes for specific richMembers.
	 *
	 * @param sess
	 * @param attrsNames
	 * @param searchString
	 * @return list of founded richMembers with specific attributes from Vo for searchString
	 * @throws InternalErrorException
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, String searchString) throws InternalErrorException, AttributeNotExistsException;

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
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Vo vo, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, AttributeNotExistsException;

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
	 * @throws AttributeNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, List<String> attrsNames, List<String> allowedStatuses, String searchString) throws InternalErrorException, AttributeNotExistsException;

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
	 * @throws AttributeNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, String searchString, boolean lookingInParentGroup) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException;

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
	 * @throws AttributeNotExistsException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findCompleteRichMembers(PerunSession sess, Group group, List<String> attrsNames, List<String> allowedStatuses, String searchString, boolean lookingInParentGroup) throws InternalErrorException, AttributeNotExistsException, ParentGroupNotExistsException;

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
	List<RichMember> getRichMembersWithAttributesByNames(PerunSession sess, Group group, List<String> attrsNames) throws InternalErrorException, AttributeNotExistsException;

	/**
	 * Get RichMembers with Attributes but only with selected attributes from list attrsDef.
	 *
	 * @param sess
	 * @param group
	 * @param attrsDef
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Group group, List<AttributeDefinition> attrsDef) throws InternalErrorException;

	/**
	 * Get rich members for displaying on pages. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param vo
	 * @return list of rich members on specified page, empty list if there are no user in this VO or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get rich members for displaying on pages. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param group
	 * @return list of rich members on specified page, empty list if there are no user in this Group or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get rich members who have the status, for displaying on pages. Rich member object contains user, member, userExtSources.
	 *
	 * @param sess
	 * @param vo
	 * @param status get only members who have this status. If status is null return all members.
	 * @return list of rich members on specified page, empty list if there are no user in this VO or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembers(PerunSession sess, Vo vo, Status status) throws InternalErrorException;

	/**
	 * Get rich members for displaying on pages. Rich member object contains user, member, userExtSources, userAttributes, memberAttributes.
	 *
	 * @param sess
	 * @param vo
	 * @return list of rich members on specified page, empty list if there are no user in this VO or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Get rich members who have the status, for displaying on pages. Rich member object contains user, member, userExtSources, userAttributes, memberAttributes.
	 *
	 * @param sess
	 * @param vo
	 * @param status get only members who have this status. If status is null return all members.
	 * @return list of rich members on specified page, empty list if there are no user in this VO or in this page
	 * @throws InternalErrorException
	 */
	List<RichMember> getRichMembersWithAttributes(PerunSession sess, Vo vo, Status status) throws InternalErrorException;

	/**
	 * Convert list of users' ids into the list of members.
	 *
	 * @param sess
	 * @param usersIds
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> getMembersByUsersIds(PerunSession sess, List<Integer> usersIds, Vo vo) throws InternalErrorException;

	/**
	 * Convert list of users into the list of members.
	 *
	 * @param sess
	 * @param users
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> getMembersByUsers(PerunSession sess, List<User> users, Vo vo) throws InternalErrorException;


	/**
	 * Fill the RichMember object with data from Member and corresponding User.
	 *
	 * @param sess
	 * @param members
	 * @return list of richMembers
	 * @throws InternalErrorException
	 */
	public List<RichMember> convertMembersToRichMembers(PerunSession sess, List<Member> members)  throws InternalErrorException;

	/**
	 * Fill the RichMember object with data from Member and corresponding User and user/member attributes.
	 *
	 * @param sess
	 * @param richMembers
	 * @return
	 * @throws InternalErrorException
	 */
	public List<RichMember> convertMembersToRichMembersWithAttributes(PerunSession sess, List<RichMember> richMembers)  throws InternalErrorException;

	/**
	 * Get the VO members count.
	 *
	 * @param sess
	 * @param vo
	 * @return count of VO members
	 * @throws InternalErrorException
	 */
	int getMembersCount(PerunSession sess, Vo vo) throws InternalErrorException;

	/**
	 * Returns number of Vo members with defined status.
	 *
	 * @param sess
	 * @param vo
	 * @param status
	 * @return number of members
	 * @throws InternalErrorException
	 */
	int getMembersCount(PerunSession sess, Vo vo, Status status) throws InternalErrorException;

	/**
	 * Get the member VO.
	 *
	 * @param sess
	 * @param member
	 * @return member's VO
	 * @throws InternalErrorException
	 */
	Vo getMemberVo(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Return list of members by theirs name.
	 * @param sess
	 * @param searchString
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> findMembersByName(PerunSession sess, String searchString) throws InternalErrorException;

	/**
	 * Return list of members by theirs name under defined VO.
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> findMembersByNameInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException;

	/**
	 * Return list of members by the searchString under defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group
	 * @param searchString
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> findMembersInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException;

	/**
	 * Return list of members by the searchString udner parentGroup of defined Group. Search is done in name, email and login.
	 * If the group is top-level group, searching in "members" group of vo in which the group exists.
	 *
	 * @param sess
	 * @param group this group is used to get parent group, we are searching members of the parent group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 * @throws ParentGroupNotExistsException
	 */
	List<Member> findMembersInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, ParentGroupNotExistsException;

	/**
	 * Return list of rich members with attributes by the searchString under defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributesInGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException;

	/**
	 * Return list of rich members with attributes by the searchString under parent group of defined Group. Search is done in name, email and login.
	 *
	 * @param sess
	 * @param group this group is used to get parent group, we are searching members of the parent group
	 * @param searchString
	 * @return
	 * @throws InternalErrorException
	 * @throws ParentGroupNotExistsException
	 */
	List<RichMember> findRichMembersWithAttributesInParentGroup(PerunSession sess, Group group, String searchString) throws InternalErrorException, ParentGroupNotExistsException;

	/**
	 * Return list of members by theirs name or login or email under defined VO.
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of members
	 * @throws InternalErrorException
	 */
	List<Member> findMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException;

	/**
	 * Return list of rich members by theirs name or login or email under defined VO.
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException;

	/**
	 * Return list of rich members by theirs name or login or email
	 * @param sess
	 * @param searchString
	 * @return list of rich members
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembers(PerunSession sess, String searchString) throws InternalErrorException;

	/**
	 * Return list of rich members with attributes by theirs name or login or email under defined VO.
	 * @param sess
	 * @param searchString
	 * @param vo
	 * @return list of rich members with attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributesInVo(PerunSession sess, Vo vo, String searchString) throws InternalErrorException;

	/**
	 * Return list of rich members with attributes by theirs name or login or email
	 * @param sess
	 * @param searchString
	 * @return list of rich members with attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> findRichMembersWithAttributes(PerunSession sess, String searchString) throws InternalErrorException;

	void checkMemberExists(PerunSession sess, Member member) throws InternalErrorException, MemberNotExistsException;

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
	Member setStatus(PerunSession sess, Member member, Status status) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, MemberNotValidYetException;

	/**
	 * Validate all atributes for member and set member's status to VALID.
	 * This method runs synchronously.
	 *
	 * @param sess
	 * @param member
	 * @return membet with new status set
	 *
	 * @throws InternalErrorException
	 * @throws WrongAttributeValueException
	 * @throws WrongReferenceAttributeValueException
	 */
	Member validateMember(PerunSession sess, Member member) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Validate all attributes for member and then set member's status to VALID.
	 * This method runs asynchronously. It immediately return member with <b>ORIGINAL</b> status and after asynchronous validation sucessfuly finishes
	 * it switch member's status to VALID. If validation ends with error, member keeps his status.
	 *
	 * @param sess
	 * @param member
	 * @return member with original status
	 *
	 * @throws InternalErrorException
	 */
	Member validateMemberAsync(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Set member status to invalid.
	 *
	 * @param sess
	 * @param member
	 * @return member with new status set
	 *
	 * @throws InternalErrorException
	 */
	Member invalidateMember(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Suspend member.
	 *
	 * @param sess
	 * @param member
	 * @return member with new status set
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotValidYetException
	 */
	Member suspendMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotValidYetException;

	/**
	 * Set member's status to expired.
	 *
	 * @param sess
	 * @param member
	 * @return member with new status set
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotValidYetException
	 */
	Member expireMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotValidYetException;

	/**
	 * Disable member.
	 *
	 * @param sess
	 * @param member
	 * @return member with new status set
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotValidYetException
	 */
	Member disableMember(PerunSession sess, Member member) throws InternalErrorException, MemberNotValidYetException;

	/**
	 * Retain only members with specified status.
	 *
	 * @param sess
	 * @param members
	 * @param status
	 * @return
	 *
	 * @throws InternalErrorException
	 * @throws MemberNotValidYetException
	 */
	List<Member> retainMembersWithStatus(PerunSession sess, List<Member> members, Status status) throws InternalErrorException;

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
	void extendMembership(PerunSession sess, Member member) throws InternalErrorException, ExtendMembershipException;

	/**
	 * Return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise false.
	 *
	 * @param sess
	 * @param member
	 * @return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise false
	 * @throws InternalErrorException
	 */
	boolean canExtendMembership(PerunSession sess, Member member) throws InternalErrorException;

	/**
	 * Return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise throws exception.
	 *
	 * @param sess
	 * @param member
	 * @return true if the membership can be extended or if no rules were set for the membershipExpiration, otherwise throws exception with reason
	 * @throws InternalErrorException
	 * @throws ExtendMembershipException
	 */
	boolean canExtendMembershipWithReason(PerunSession sess, Member member) throws InternalErrorException, ExtendMembershipException;

	/**
	 * Checks if the user can apply membership to the VO, it decides based on extendMembershipRules on the doNotAllowLoa key
	 * @param sess
	 * @param vo
	 * @param user
	 * @param loa
	 * @return true if user can be apply for membership to the VO
	 * @throws InternalErrorException
	 */
	boolean canBeMember(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException;

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
	boolean canBeMemberWithReason(PerunSession sess, Vo vo, User user, String loa) throws InternalErrorException, ExtendMembershipException;

	/**
	 * Returns the date to which will be extended member's expiration time.
	 *
	 * @param sess
	 * @param member
	 * @return date
	 * @throws InternalErrorException
	 * @throws ExtendMembershipException
	 */
	Date getNewExtendMembership(PerunSession sess, Member member) throws InternalErrorException, ExtendMembershipException;
	
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
  Date getNewExtendMembership(PerunSession sess, Vo vo, String loa) throws InternalErrorException, ExtendMembershipException;

	/**
	 * For richMember filter all his user and member attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richMember
	 * @return richMember with only allowed attributes
	 * @throws InternalErrorException
	 */
	RichMember filterOnlyAllowedAttributes(PerunSession sess, RichMember richMember) throws InternalErrorException;

	/**
	 * For list of richMembers filter all their user and member attributes and remove all which principal has no access to.
	 *
	 * @param sess
	 * @param richMembers
	 * @return list of richMembers with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> filterOnlyAllowedAttributes(PerunSession sess, List<RichMember> richMembers) throws InternalErrorException;

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
	 * @param useContext true or false means using context or not using context (more above in javadoc)
	 *
	 * @return list of richMembers with only allowed attributes
	 * @throws InternalErrorException
	 */
	List<RichMember> filterOnlyAllowedAttributes(PerunSession sess, List<RichMember> richMembers, boolean useContext) throws InternalErrorException;

	/**
	 * Send mail to user's preferred email address with link for non-authz password reset.
	 * Correct authz information is stored in link's URL.
	 *
	 * @param sess PerunSession
	 * @param member Member to get user to send link mail to
	 * @param namespace Namespace to reset password in (member must have login in)
	 * @param url base URL of Perun instance
	 * @throws InternalErrorException
	 */
	void sendPasswordResetLinkEmail(PerunSession sess, Member member, String namespace, String url) throws InternalErrorException;

}
