package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;

import java.util.List;
import java.util.Map;

/**
 * <p>VOs manager can create, delete, update and find VO.</p>
 * <p/>
 * <p>You must get an instance of VosManager from Perun:</p>
 * <pre>
 *    PerunSession ps;
 *    //...
 *    VosManager vm = ps.getPerun().getVosManager();
 * </pre>
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @see PerunSession
 */
public interface VosManager {

	String MEMBERS_GROUP = "members";
	String MEMBERS_GROUP_DESCRIPTION = "Group containing VO members";

	/**
	 * Get list of Vos by Access Righs:
	 * If User is:
	 * - PERUNADMIN : get all Vos
	 * - VoAdmin : Vo where user is Admin
	 * - GroupAdmin: Vo where user is GroupAdmin
	 *
	 * @param perunSession
	 * @return List of VOs or empty ArrayList<Vo>
	 *
	 * @throws RelationExistsException
	 * @throws InternalErrorException
	 */
	List<Vo> getVos(PerunSession perunSession) throws PrivilegeException;

	/**
	 * Get list of EnrichedVos of all the VOs the user has access to
	 *
	 * @param perunSession
	 * @return List of EnrichedVos or empty list
	 * @throws PrivilegeException
	 */
	List<EnrichedVo> getEnrichedVos(PerunSession perunSession) throws PrivilegeException;

	/**
	 * Get list of Vos without any privilege.
	 *
	 * @param perunSession
	 * @return List of VOs or empty ArrayList<Vo>
	 *
	 * @throws RelationExistsException
	 * @throws InternalErrorException
	 */
	List<Vo> getAllVos(PerunSession perunSession) throws PrivilegeException;

	/**
	 * Delete VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void deleteVo(PerunSession perunSession, Vo vo) throws VoNotExistsException, PrivilegeException;

	/**
	 * Delete VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param forceDelete force the deletion of the VO, regardless there are any existing entities associated with the VO (they will be deleted)
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void deleteVo(PerunSession perunSession, Vo vo, boolean forceDelete) throws VoNotExistsException, PrivilegeException;


	/**
	 * Create new VO.
	 *
	 * @param perunSession
	 * @param vo vo object with prefilled voShortName and voName
	 * @return newly created VO
	 * @throws VoExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Vo createVo(PerunSession perunSession, Vo vo) throws VoExistsException, PrivilegeException;

	/**
	 * Updates VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @return returns updated VO
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	Vo updateVo(PerunSession perunSession, Vo vo) throws VoNotExistsException, PrivilegeException;

	/**
	 * Find existing VO by short name (short name is unique).
	 *
	 * @param perunSession
	 * @param shortName short name of VO which you find (for example "KZCU")
	 * @return VO with requested shortName or throws VoNotExistsException if the VO with specified shortName doesn't exist
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	Vo getVoByShortName(PerunSession perunSession, String shortName) throws VoNotExistsException, PrivilegeException;

	/**
	 * Finds existing VO by id.
	 *
	 * @param perunSession
	 * @param id
	 * @return VO with requested id or throws VoNotExistsException if the VO with specified id doesn't exist
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	Vo getVoById(PerunSession perunSession, int id) throws VoNotExistsException, PrivilegeException;

	/**
	 * Finds existing vo by and id and returns corresponding EnrichedVo
	 * @param perunSession
	 * @param id
	 * @return EnrichedVO object of requested VO, which contains its member and parent VOs
	 * @throws VoNotExistsException
	 */
	EnrichedVo getEnrichedVoById(PerunSession perunSession, int id) throws VoNotExistsException, PrivilegeException;

	/**
	 * Finds existing VOs by ids.
	 *
	 * @param perunSession
	 * @param ids
	 * @return VOs with requested ids
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<Vo> getVosByIds(PerunSession perunSession, List<Integer> ids) throws PrivilegeException;

	/**
	 * Finds users, who can join the Vo.
	 *
	 * @param perunSession
	 * @param vo
	 * @param searchString depends on the extSource of the VO, could by part of the name, email or something like that.
	 * @param maxNumOfResults limit the maximum number of returned entries
	 * @return list of candidates who match the searchString
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 */
	List<Candidate> findCandidates(PerunSession perunSession, Vo vo, String searchString, int maxNumOfResults) throws VoNotExistsException, PrivilegeException;

	/**
	 * Finds users, who can join the Vo.
	 *
	 * @param perunSession
	 * @param vo vo to be used
	 * @param searchString depends on the extSource of the VO, could by part of the name, email or something like that.
	 * @return list of candidates who match the searchString
	 * @throws InternalErrorException
	 * @throws VoNotExistsException
	 * @throws PrivilegeException
	 */
	List<Candidate> findCandidates(PerunSession perunSession, Vo vo, String searchString) throws VoNotExistsException, PrivilegeException;

	/**
	 * Finds users, who can join the group in Vo.
	 *
	 * @param sess
	 * @param group group to be used
	 * @param searchString depends on the extSource of the Group, could by part of the name, email or something like that.
	 * @return list of candidates who match the searchString
	 * @throws InternalErrorException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 */
	List<Candidate> findCandidates(PerunSession sess, Group group, String searchString) throws GroupNotExistsException, PrivilegeException;

	/**
	 * Finds MemberCandidates who can join the Vo.
	 *
	 * @param sess session
	 * @param vo vo to be used
	 * @param attrNames names of attributes that will be found
	 * @param searchString depends on the extSource of the Group, could by part of the name, email or something like that.
	 * @return list of MemberCandidates for given vo.
	 * @throws InternalErrorException internal error
	 * @throws VoNotExistsException when vo does not exist
	 * @throws PrivilegeException privilege exception
	 */
	List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, List<String> attrNames, String searchString) throws VoNotExistsException, PrivilegeException;

	/**
	 * Finds MemberCandidates who can join the Group.
	 *
	 * @param sess session
	 * @param group group to be used
	 * @param attrNames names of attributes that will be found
	 * @param searchString depends on the extSource of the Group, could by part of the name, email or something like that.
	 * @return list of MemberCandidates for given vo.
	 * @throws InternalErrorException internal error
	 * @throws GroupNotExistsException when group does not exist
	 * @throws PrivilegeException privilege exception
	 */
	List<MemberCandidate> getCompleteCandidates(PerunSession sess, Group group, List<String> attrNames, String searchString) throws GroupNotExistsException, PrivilegeException;


	/**
	 * Add a user administrator to the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param user user who will became an VO administrator
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws AlreadyAdminException
	 * @throws VoNotExistsException
	 */
	void addAdmin(PerunSession perunSession, Vo vo, User user) throws PrivilegeException, AlreadyAdminException, VoNotExistsException, UserNotExistsException, RoleCannotBeManagedException;

	/**
	 * Add a group administrator to the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param group that will become a VO administrator
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws AlreadyAdminException
	 * @throws VoNotExistsException
	 * @throws GroupNotExistsException
	 */
	void addAdmin(PerunSession perunSession, Vo vo, Group group) throws PrivilegeException, AlreadyAdminException, VoNotExistsException, GroupNotExistsException, RoleCannotBeManagedException;


	/**
	 * Removes a user administrator from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param user user who will lose an VO administrator role
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, Vo vo, User user) throws PrivilegeException, VoNotExistsException, UserNotAdminException, UserNotExistsException, RoleCannotBeManagedException;

	/**
	 * Removes a group administrator from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param group group that will lose a VO administrator role
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws GroupNotAdminException
	 * @throws GroupNotExistsException
	 */
	void removeAdmin(PerunSession perunSession, Vo vo, Group group) throws PrivilegeException, VoNotExistsException, GroupNotAdminException, GroupNotExistsException, RoleCannotBeManagedException;

	/**
	 * Get list of user administrators of specific vo for specific role.
	 * If some group is administrator of the VO, all VALID members are included in the list.
	 *
	 * If onlyDirectAdmins is true, return only direct users of the group for supported role.
	 *
	 * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN
	 *
	 * @param perunSession
	 * @param vo
	 * @param role supported role
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of all user administrators of the given vo for supported role
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RoleNotSupportedException
	 * @throws VoNotExistsException
	 */
	List<User> getAdmins(PerunSession perunSession, Vo vo, String role, boolean onlyDirectAdmins) throws PrivilegeException, VoNotExistsException, RoleNotSupportedException;

	/**
	 * Get list of richUser administrators of specific vo for specific role.
	 * If some group is administrator of the VO, all VALID members are included in the list.
	 *
	 * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN, SPONSOR
	 *
	 * If "onlyDirectAdmins" is "true", return only direct users of the vo for supported role with specific attributes.
	 * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @param specificAttributes list of specified attributes which are needed in object richUser
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of RichUser administrators for the vo and supported role with attributes
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws RoleNotSupportedException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, String role, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws PrivilegeException, VoNotExistsException, RoleNotSupportedException, UserNotExistsException;

	/**
	 * Get list of group administrators of the given VO.
	 *
	 * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN, SPONSOR
	 *
	 * @param perunSession
	 * @param vo
	 * @param role
	 *
	 * @return List of groups, who are administrators of the Vo with supported role. Returns empty list if there is no VO group admin.
	 *
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws RoleNotSupportedException
	 */
	List<Group> getAdminGroups(PerunSession perunSession, Vo vo, String role) throws PrivilegeException, VoNotExistsException, RoleNotSupportedException;

	/**
	 * Get list of Vo administrators.
	 * If some group is administrator of the VO, all members are included in the list.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	@Deprecated
	List<User> getAdmins(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException;

	/**
	 * Gets list of direct user administrators of the VO.
	 * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	@Deprecated
	List<User> getDirectAdmins(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException;

	/**
	 * Get list of group administrators of the given VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of groups, who are administrators of the Vo. Returns empty list if there is no VO group admin.
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	@Deprecated
	List<Group> getAdminGroups(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException;


	/**
	 * Get list of Vo administrators, which are directly assigned (not by group membership) with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param vo
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws PrivilegeException, VoNotExistsException;

	/**
	 * Get list of Vo administrators with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param vo
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 */
	@Deprecated
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws PrivilegeException, VoNotExistsException;

	/**
	 * Get list of Vo administrators like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	@Deprecated
	List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException;

	/**
	 * Get list of Vo administrators like RichUsers with attributes.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 */
	@Deprecated
	List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Vo vo) throws PrivilegeException, VoNotExistsException, UserNotExistsException;

	/**
	 * Get count of all vos.
	 *
	 * @param sess PerunSession
	 *
	 * @throws InternalErrorException
	 * @return count of all vos
	 */
	int getVosCount(PerunSession sess);

	/**
	 * Returns number of vo members by their status.
	 *
	 * @param sess perun session
	 * @param vo vo of members
	 * @return map of status in vo to number of vo members with the status
	 * @throws VoNotExistsException if vo does not exist
	 * @throws PrivilegeException insufficient permissions
	 */
	Map<Status, Integer> getVoMembersCountsByStatus(PerunSession sess, Vo vo) throws VoNotExistsException, PrivilegeException;

	/**
	 * Adds role SPONSOR for user in a VO.
	 *
	 * @param sess perun session
	 * @param vo virtual organization
	 * @param user specific user in VO
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 * @throws VoNotExistsException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 */
	void addSponsorRole(PerunSession sess, Vo vo, User user) throws AlreadyAdminException, VoNotExistsException, UserNotExistsException, PrivilegeException, RoleCannotBeManagedException;

	/**
	 * Adds role SPONSOR for group in a VO.
	 *
	 * @param sess perun session
	 * @param vo virtual organization
	 * @param group specific group in VO
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 * @throws VoNotExistsException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 */
	void addSponsorRole(PerunSession sess, Vo vo, Group group) throws AlreadyAdminException, VoNotExistsException, GroupNotExistsException, PrivilegeException, RoleCannotBeManagedException;

	/**
	 * Removes role SPONSOR from user in a VO.
	 * @param sess perun session
	 * @param vo virtual organization
	 * @param user user in VO for removal of sponsor role
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 * @throws VoNotExistsException
	 * @throws UserNotExistsException
	 * @throws PrivilegeException
	 */
	void removeSponsorRole(PerunSession sess, Vo vo, User user) throws UserNotAdminException, VoNotExistsException, UserNotExistsException, PrivilegeException, RoleCannotBeManagedException;

	/**
	 * Removes role SPONSOR from group in a VO.
	 * @param sess perun session
	 * @param vo virtual organization
	 * @param group group in VO for removal of sponsor role
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 * @throws VoNotExistsException
	 * @throws GroupNotExistsException
	 * @throws PrivilegeException
	 */
	void removeSponsorRole(PerunSession sess, Vo vo, Group group) throws GroupNotAdminException, VoNotExistsException, GroupNotExistsException, PrivilegeException, RoleCannotBeManagedException;

	/**
	 * Set ban for member on his vo.
	 *
	 * @param sess session
	 * @param ban ban information
	 * @return created ban object
	 * @throws PrivilegeException insufficient permissions
	 * @throws MemberNotExistsException if there is no member with specified id
	 * @throws BanAlreadyExistsException
	 */
	BanOnVo setBan(PerunSession sess, BanOnVo ban) throws PrivilegeException, MemberNotExistsException, BanAlreadyExistsException;

	/**
	 * Remove vo ban with given id.
	 *
	 * @param sess session
	 * @param banId if of vo ban
	 * @throws PrivilegeException insufficient permissions
	 * @throws BanNotExistsException if there is no ban with specified id
	 */
	void removeBan(PerunSession sess, int banId) throws PrivilegeException, BanNotExistsException;

	/**
	 * Remove vo ban for given member.
	 *
	 * @param sess session
	 * @param member member
	 * @throws PrivilegeException insufficient permissions
	 * @throws BanNotExistsException if there is no ban for member with given id
	 * @throws MemberNotExistsException if there is no such member
	 */
	void removeBanForMember(PerunSession sess, Member member) throws PrivilegeException, BanNotExistsException, MemberNotExistsException;

	/**
	 * Get vo ban with given id.
	 *
	 * @param sess session
	 * @param banId ban id
	 * @return found ban
	 * @throws BanNotExistsException if there is no such ban
	 * @throws PrivilegeException insufficient permissions
	 */
	BanOnVo getBanById(PerunSession sess, int banId) throws BanNotExistsException, PrivilegeException;

	/**
	 * Get ban for given member, or null if he is not banned.
	 *
	 * @param sess session
	 * @param member member
	 * @return found ban or null if the member is not banned
	 * @throws PrivilegeException insufficient permissions
	 * @throws MemberNotExistsException if there is no such member
	 */
	BanOnVo getBanForMember(PerunSession sess, Member member) throws PrivilegeException, MemberNotExistsException;

	/**
	 * Get list of all bans for vo with given id.
	 *
	 * @param sess session
	 * @param voId vo id
	 * @return vo bans for given vo
	 * @throws PrivilegeException insufficient permissions
	 * @throws VoNotExistsException if there is no vo with given id
	 */
	List<BanOnVo> getBansForVo(PerunSession sess, int voId) throws PrivilegeException, VoNotExistsException;

	/**
	 * Update existing ban (description, validation timestamp)
	 *
	 * @param sess
	 * @param banOnVo the specific ban
	 * @return updated ban
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 * @throws BanNotExistsException
	 * @throws VoNotExistsException
	 */
	BanOnVo updateBan(PerunSession sess, BanOnVo banOnVo) throws PrivilegeException, BanNotExistsException, VoNotExistsException;

	/**
	 * For the given vo, creates sponsored members for each sponsored user who is a member
	 * of the given vo. Original sponsors of the users will be set to the sponsored members.
	 *
	 * @param sess session
	 * @param vo vo where members will be converted
	 */
	void convertSponsoredUsers(PerunSession sess, Vo vo) throws PrivilegeException;


	/**
	 * For the given vo, creates sponsored members for each sponsored user who is a member
	 * of the given vo. The sponsored members will be sponsored by the given user, not by its
	 * original sponsors.
	 *
	 * @param sess session
	 * @param vo vo where members will be converted
	 * @param newSponsor user, who will be set as a sponsor to the sponsored members
	 */
	void convertSponsoredUsersWithNewSponsor(PerunSession sess, Vo vo, User newSponsor) throws PrivilegeException;

	/**
	 * Adds new relationship between vo and a member vo.
	 * If user is member in both vos, updates memberOrganizations list attribute.
	 * If user is member only in member vo, creates member in parent vo and sets memberOrganizations list attribute.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param memberVo new member of the vo
	 * @throws RelationExistsException if member vo is already member of the vo
	 * @throws PrivilegeException if not authorized
	 * @throws VoNotExistsException if any of the vos don't exist
	 */
	void addMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationExistsException, PrivilegeException, VoNotExistsException;

	/**
	 * Removes member vo from given vo.
	 * Updates memberOrganizations list attribute for those members of parent vo who came from the member vo.
	 *
	 * @param sess session
	 * @param vo vo
	 * @param memberVo member vo to be removed
	 * @throws RelationNotExistsException if member vo is not a member of the vo
	 * @throws PrivilegeException if not authorized
	 * @throws VoNotExistsException if any of the vos don't exist
	 */
	void removeMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationNotExistsException, PrivilegeException, VoNotExistsException;

	/**
	 * Gets all member organizations of the given vo.
	 *
	 * @param sess session
	 * @param voId vo id
	 * @return list of member vos
	 * @throws VoNotExistsException if given vo does not exist
	 * @throws PrivilegeException if not authorized
	 */
	List<Vo> getMemberVos(PerunSession sess, int voId) throws VoNotExistsException, PrivilegeException;

	/**
	 * Gets all organizations where given vo is direct member.
	 *
	 * @param sess session
	 * @param memberVoId member vo id
	 * @return list of direct parent vos
	 * @throws VoNotExistsException if given member vo does not exist
	 * @throws PrivilegeException if not authorized
	 */
	List<Vo> getParentVos(PerunSession sess, int memberVoId) throws VoNotExistsException, PrivilegeException;
}
