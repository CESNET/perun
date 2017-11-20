package cz.metacentrum.perun.core.bl;

import java.util.List;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.*;

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
public interface VosManagerBl {

	/**
	 * Get list of all Vos.
	 *
	 * @param perunSession
	 * @throws InternalErrorException
	 * @return List of VOs or empty ArrayList<Vo>
	 */
	List<Vo> getVos(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Delete VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @throws InternalErrorException
	 * @throws RelationExistsException
	 */
	void deleteVo(PerunSession perunSession, Vo vo) throws InternalErrorException, RelationExistsException;

	/**
	 * Delete VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param forceDelete force the deletion of the VO, regardless there are any existing entities associated with the VO (they will be deleted)
	 * @throws InternalErrorException
	 * @throws RelationExistsException exception is thrown when forceDelete is false and there are some entities associated with the VO
	 */
	void deleteVo(PerunSession perunSession, Vo vo, boolean forceDelete) throws InternalErrorException, RelationExistsException;


	/**
	 * Create new VO.
	 *
	 * @param perunSession
	 * @param vo vo object with prefilled voShortName and voName
	 * @return newly created VO
	 * @throws VoExistsException
	 * @throws InternalErrorException
	 */
	Vo createVo(PerunSession perunSession, Vo vo) throws VoExistsException, InternalErrorException;

	/**
	 * Updates VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @return returns updated VO
	 * @throws InternalErrorException
	 */
	Vo updateVo(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Find existing VO by short name (short name is unique).
	 *
	 * @param perunSession
	 * @param shortName short name of VO which you find (for example "KZCU")
	 * @return VO with requested shortName or throws  if the VO with specified shortName doesn't exist
	 * @throws InternalErrorException
	 */
	Vo getVoByShortName(PerunSession perunSession, String shortName) throws InternalErrorException, VoNotExistsException;

	/**
	 * Finds existing VO by id.
	 *
	 * @param perunSession
	 * @param id
	 * @return VO with requested id or throws  if the VO with specified id doesn't exist
	 * @throws InternalErrorException
	 */
	Vo getVoById(PerunSession perunSession, int id) throws InternalErrorException, VoNotExistsException;

	/**
	 * Finds users, who can join the Vo.
	 *
	 * @param perunSession
	 * @param vo
	 * @param searchString depends on the extSource of the VO, could by part of the name, email or something like that.
	 * @param maxNumOfResults limit the maximum number of returned entries
	 * @return list of candidates who match the searchString
	 * @throws InternalErrorException
	 */
	List<Candidate> findCandidates(PerunSession perunSession, Vo vo, String searchString, int maxNumOfResults) throws InternalErrorException;

	/**
	 * Finds users, who can join the Vo.
	 *
	 * @param perunSession
	 * @param vo vo to be used
	 * @param searchString depends on the extSource of the VO, could by part of the name, email or something like that.
	 * @return list of candidates who match the searchString
	 * @throws InternalErrorException
	 */
	List<Candidate> findCandidates(PerunSession perunSession, Vo vo, String searchString) throws InternalErrorException;

	/**
	 * Finds users, who can join the group in Vo.
	 *
	 * @param sess
	 * @param group group to be used
	 * @param searchString depends on the extSource of the Group, could by part of the name, email or something like that.
	 * @return list of candidates who match the searchString
	 * @throws InternalErrorException
	 */
	List<Candidate> findCandidates(PerunSession sess, Group group, String searchString) throws InternalErrorException;

	/**
	 * Finds MemberCandidates who can join the Vo.
	 *
	 * @param sess session
	 * @param vo vo to be used
	 * @param attrNames name of attributes to be searched
	 * @param searchString depends on the extSource of the Vo, could by part of the name, email or something like that.
	 * @return list of memberCandidates who match the searchString
	 * @throws InternalErrorException internal error
	 */
	List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, List<String> attrNames, String searchString) throws InternalErrorException;

	/**
	 * Finds MemberCandidates who can join the Group. If the given vo is not null, it searches only
	 * users who belong to this Vo.
	 *
	 * @param sess session
	 * @param vo vo if is null, users are searched in whole perun, otherwise only members of this vo are used.
	 * @param group group to be used
	 * @param attrNames name of attributes to be searched
	 * @param searchString depends on the extSource of the Vo, could by part of the name, email or something like that.
	 * @param extSources extSources used to find candidates
	 * @return list of memberCandidates who match the searchString
	 * @throws InternalErrorException internal error
	 */
	List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, Group group, List<String> attrNames, String searchString, List<ExtSource> extSources) throws InternalErrorException;

	/**
	 * Add a user administrator to the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param user user who will became an VO administrator
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession perunSession, Vo vo, User user) throws InternalErrorException, AlreadyAdminException;


	/**
	 * Add a group administrator to the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param group group who will become a VO administrator
	 * @throws InternalErrorException
	 * @throws AlreadyAdminException
	 */
	void addAdmin(PerunSession perunSession, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException;

	/**
	 * Removes a user administrator from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param user user who will lose an VO administrator role
	 * @throws InternalErrorException
	 * @throws UserNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, Vo vo, User user) throws InternalErrorException, UserNotAdminException;

	/**
	 * Removes a group administrator from the VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @param group group who will lose a VO administrator role
	 * @throws InternalErrorException
	 * @throws GroupNotAdminException
	 */
	void removeAdmin(PerunSession perunSession, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException;

	/**
	 * Get list of all user administrators for supported role and specific vo.
	 *
	 * If onlyDirectAdmins is true, return only direct users of the group for supported role.
	 *
	 * Supported roles: VoObserver, TopGroupCreator, VoAdmin
	 *
	 * @param perunSession
	 * @param vo
	 * @param role supported role
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of all user administrators of the given vo for supported role
	 *
	 * @throws InternalErrorException
	 */
	List<User> getAdmins(PerunSession perunSession, Vo vo, Role role, boolean onlyDirectAdmins) throws InternalErrorException;

	/**
	 * Get list of all richUser administrators for the vo and supported role with specific attributes.
	 *
	 * Supported roles: VoObserver, TopGroupCreator, VoAdmin
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
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, Role role, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get list of group administrators of the given VO.
	 *
	 * Supported roles: VoObserver, TopGroupCreator, VoAdmin
	 *
	 * @param perunSession
	 * @param vo
	 * @param role
	 *
	 * @return List of groups, who are administrators of the Vo with supported role. Returns empty list if there is no VO group admin.
	 *
	 * @throws InternalErrorException
	 */
	List<Group> getAdminGroups(PerunSession perunSession, Vo vo, Role role) throws InternalErrorException;

	/**
	 * Get list of Vo administrators.
	 * If some group is administrator of the VO, all members are included in the list.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<User> getAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Gets list of direct user administrators of the VO.
	 * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
	 *
	 * @param perunSession
	 * @param vo
	 *
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<User> getDirectAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException;

	/**
	 * Get list of group administrators of the given VO.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of groups, who are administrators of the Vo. Returns empty list if there is no VO group admin.
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<Group> getAdminGroups(PerunSession perunSession, Vo vo) throws InternalErrorException;


	/**
	 * Get list of Vo administrators like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	@Deprecated
	List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get list of Vo administrators directly assigned to VO like RichUsers without attributes.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get list of Vo administrators like RichUsers with attributes.
	 *
	 * @param perunSession
	 * @param vo
	 * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	@Deprecated
	List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Vo vo) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get list of Vo administrators with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param vo
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws UserNotExistsException
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get list of Vo administrators, which are directly assigned (not by group membership) with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param vo
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws UserNotExistsException
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException;

	/**
	 * !!! Not Complete yet, need to implement all perunBeans !!!
	 *
	 * Get perunBean and try to find all connected Vos
	 *
	 * @param sess
	 * @param perunBean
	 * @return list of vos connected with perunBeans
	 * @throws InternalErrorException
	 */
	List<Vo> getVosByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException, VoNotExistsException;

	void checkVoExists(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException;

	/**
	 * Get count of all vos.
	 *
	 * @param perunSession
	 *
	 * @return count of all vos
	 *
	 * @throws InternalErrorException
	 */
	int getVosCount(PerunSession perunSession) throws InternalErrorException;

	/**
	 * Check whether a user is in a role for a given VO, possibly checking also user's groups.
	 * @param session session
	 * @param user user
	 * @param role role
	 * @param vo  virtual organization
	 * @param checkGroups check also groups of the user whether they have the role
	 * @return true if user is directly in role for the vo, or if "checkGroups" flag is set and at least one of the groups is in the role
	 * @throws InternalErrorException exception
	 */
	boolean isUserInRoleForVo(PerunSession session, User user, Role role, Vo vo, boolean checkGroups) throws InternalErrorException;

	/**
	 * Handles a user that lost a role.
	 *
	 * @param sess perun session
	 * @param user user
	 * @param vo virtual organization
	 * @param role role of user in VO
	 * @throws InternalErrorException
	 */
	void handleUserLostVoRole(PerunSession sess, User user, Vo vo, Role role) throws InternalErrorException;

	/**
	 * Handles a group that lost a role.
	 *
	 * @param sess perun session
	 * @param group group
	 * @param vo virtual organization
	 * @param role role of group in VO
	 * @throws InternalErrorException
	 */
	void handleGroupLostVoRole(PerunSession sess, Group group, Vo vo, Role role) throws InternalErrorException;
}
