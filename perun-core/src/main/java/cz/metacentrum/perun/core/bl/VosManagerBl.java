package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.EnrichedBanOnVo;
import cz.metacentrum.perun.core.api.EnrichedVo;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
   * @return List of VOs or empty ArrayList<Vo>
   * @throws InternalErrorException
   */
  List<Vo> getVos(PerunSession perunSession);

  /**
   * Get list of all EnrichedVos
   *
   * @param perunSession
   * @return List of EnrichedVos or empty list
   */
  List<EnrichedVo> getEnrichedVos(PerunSession perunSession);

  /**
   * Delete VO.
   *
   * @param perunSession
   * @param vo
   * @throws InternalErrorException
   */
  void deleteVo(PerunSession perunSession, Vo vo);

  /**
   * Delete VO.
   *
   * @param perunSession
   * @param vo
   * @param forceDelete  force the deletion of the VO, regardless there are any existing entities associated with the VO (they will be deleted)
   * @throws InternalErrorException
   */
  void deleteVo(PerunSession perunSession, Vo vo, boolean forceDelete);


  /**
   * Create new VO.
   *
   * @param perunSession
   * @param vo           vo object with prefilled voShortName and voName
   * @return newly created VO
   * @throws VoExistsException
   * @throws InternalErrorException
   */
  Vo createVo(PerunSession perunSession, Vo vo) throws VoExistsException;

  /**
   * Updates VO.
   *
   * @param perunSession
   * @param vo
   * @return returns updated VO
   * @throws InternalErrorException
   */
  Vo updateVo(PerunSession perunSession, Vo vo);

  /**
   * Find existing VO by short name (short name is unique).
   *
   * @param perunSession
   * @param shortName    short name of VO which you find (for example "KZCU")
   * @return VO with requested shortName or throws  if the VO with specified shortName doesn't exist
   * @throws InternalErrorException
   */
  Vo getVoByShortName(PerunSession perunSession, String shortName) throws VoNotExistsException;

  /**
   * Finds existing VO by id.
   *
   * @param perunSession
   * @param id
   * @return VO with requested id or throws  if the VO with specified id doesn't exist
   * @throws InternalErrorException
   */
  Vo getVoById(PerunSession perunSession, int id) throws VoNotExistsException;

  /**
   * Finds existing vo by and id and returns corresponding EnrichedVo
   *
   * @param perunSession
   * @param id
   * @return EnrichedVO object of requested VO, which contains its member and parent VOs
   * @throws VoNotExistsException
   */
  EnrichedVo getEnrichedVoById(PerunSession perunSession, int id) throws VoNotExistsException;

  /**
   * Finds existing VOs by ids.
   *
   * @param perunSession
   * @param ids
   * @return VOs with requested ids
   * @throws InternalErrorException
   */
  List<Vo> getVosByIds(PerunSession perunSession, List<Integer> ids);

  /**
   * Finds users, who can join the Vo.
   *
   * @param perunSession
   * @param vo
   * @param searchString    depends on the extSource of the VO, could by part of the name, email or something like that.
   * @param maxNumOfResults limit the maximum number of returned entries
   * @return list of candidates who match the searchString
   * @throws InternalErrorException
   */
  List<Candidate> findCandidates(PerunSession perunSession, Vo vo, String searchString, int maxNumOfResults);

  /**
   * Finds users, who can join the Vo.
   *
   * @param perunSession
   * @param vo           vo to be used
   * @param searchString depends on the extSource of the VO, could by part of the name, email or something like that.
   * @return list of candidates who match the searchString
   * @throws InternalErrorException
   */
  List<Candidate> findCandidates(PerunSession perunSession, Vo vo, String searchString);

  /**
   * Finds users, who can join the group in Vo.
   *
   * @param sess
   * @param group        group to be used
   * @param searchString depends on the extSource of the Group, could by part of the name, email or something like that.
   * @return list of candidates who match the searchString
   * @throws InternalErrorException
   */
  List<Candidate> findCandidates(PerunSession sess, Group group, String searchString);

  /**
   * Finds MemberCandidates who can join the Vo.
   *
   * @param sess         session
   * @param vo           vo to be used
   * @param attrNames    name of attributes to be searched
   * @param searchString depends on the extSource of the Vo, could by part of the name, email or something like that.
   * @return list of memberCandidates who match the searchString
   * @throws InternalErrorException internal error
   */
  List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, List<String> attrNames, String searchString);

  /**
   * Finds MemberCandidates who can join the Group. If the given vo is not null, it searches only
   * users who belong to this Vo or who have ues in any of given extSources.
   *
   * @param sess         session
   * @param vo           vo if vo is null, users are searched in whole perun, otherwise users are searched in members of given vo and in users with ues in any of given extSources
   * @param group        group to be used
   * @param attrNames    name of attributes to be searched
   * @param searchString depends on the extSource of the Vo, could by part of the name, email or something like that.
   * @param extSources   extSources used to find candidates and possibly users
   * @return list of memberCandidates who match the searchString
   */
  List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, Group group, List<String> attrNames,
                                              String searchString, List<ExtSource> extSources);

  /**
   * Get list of user administrators of specific vo for specific role.
   * If some group is administrator of the VO, all VALID members are included in the list.
   * <p>
   * If onlyDirectAdmins is true, return only direct users of the group for supported role.
   * <p>
   * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN
   *
   * @param perunSession
   * @param vo
   * @param role             supported role
   * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
   * @return list of all user administrators of the given vo for supported role
   * @throws InternalErrorException
   */
  List<User> getAdmins(PerunSession perunSession, Vo vo, String role, boolean onlyDirectAdmins);

  /**
   * Get list of richUser administrators of specific vo for specific role.
   * If some group is administrator of the VO, all VALID members are included in the list.
   * <p>
   * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN
   * <p>
   * If "onlyDirectAdmins" is "true", return only direct users of the vo for supported role with specific attributes.
   * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
   *
   * @param perunSession
   * @param vo
   * @param specificAttributes list of specified attributes which are needed in object richUser
   * @param allUserAttributes  if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
   * @param onlyDirectAdmins   if true, get only direct user administrators (if false, get both direct and indirect)
   * @return list of RichUser administrators for the vo and supported role with attributes
   * @throws InternalErrorException
   * @throws UserNotExistsException
   */
  List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, String role, List<String> specificAttributes,
                               boolean allUserAttributes, boolean onlyDirectAdmins) throws UserNotExistsException;

  /**
   * Get list of group administrators of the given VO.
   * <p>
   * Supported roles: VOOBSERVER, TOPGROUPCREATOR, VOADMIN
   *
   * @param perunSession
   * @param vo
   * @param role
   * @return List of groups, who are administrators of the Vo with supported role. Returns empty list if there is no VO group admin.
   * @throws InternalErrorException
   */
  List<Group> getAdminGroups(PerunSession perunSession, Vo vo, String role);

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
  List<User> getAdmins(PerunSession perunSession, Vo vo);

  /**
   * Gets list of direct user administrators of the VO.
   * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
   *
   * @param perunSession
   * @param vo
   * @throws InternalErrorException
   */
  @Deprecated
  List<User> getDirectAdmins(PerunSession perunSession, Vo vo);

  /**
   * Get list of group administrators of the given VO.
   *
   * @param perunSession
   * @param vo
   * @return List of groups, who are administrators of the Vo. Returns empty list if there is no VO group admin.
   * @throws InternalErrorException
   */
  @Deprecated
  List<Group> getAdminGroups(PerunSession perunSession, Vo vo);


  /**
   * Get list of Vo administrators like RichUsers without attributes.
   *
   * @param perunSession
   * @param vo
   * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
   * @throws InternalErrorException
   */
  @Deprecated
  List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo);

  /**
   * Get list of Vo administrators directly assigned to VO like RichUsers without attributes.
   *
   * @param perunSession
   * @param vo
   * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
   * @throws InternalErrorException
   */
  @Deprecated
  List<RichUser> getDirectRichAdmins(PerunSession perunSession, Vo vo);

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
  List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Vo vo) throws UserNotExistsException;

  /**
   * Get list of Vo administrators with specific attributes.
   * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
   *
   * @param perunSession
   * @param vo
   * @param specificAttributes
   * @return list of RichUsers with specific attributes.
   * @throws InternalErrorException
   */
  @Deprecated
  List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes);

  /**
   * Get list of Vo administrators, which are directly assigned (not by group membership) with specific attributes.
   * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
   *
   * @param perunSession
   * @param vo
   * @param specificAttributes
   * @return list of RichUsers with specific attributes.
   * @throws InternalErrorException
   */
  @Deprecated
  List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo,
                                                           List<String> specificAttributes);

  /**
   * Returns list of vos connected with a group
   *
   * @param sess
   * @param group
   * @return list of vos connected with group
   * @throws InternalErrorException
   */
  List<Vo> getVosByPerunBean(PerunSession sess, Group group) throws VoNotExistsException;

  /**
   * Returns list of vos connected with a member
   *
   * @param sess
   * @param member
   * @return list of vos connected with member
   * @throws InternalErrorException
   */
  List<Vo> getVosByPerunBean(PerunSession sess, Member member);

  /**
   * Returns list of vos connected with a resource
   *
   * @param sess
   * @param resource
   * @return list of vos connected with resource
   * @throws InternalErrorException
   */
  List<Vo> getVosByPerunBean(PerunSession sess, Resource resource) throws VoNotExistsException;

  /**
   * Returns list of vos connected with a user
   *
   * @param sess
   * @param user
   * @return list of vos connected with user
   * @throws InternalErrorException
   */
  List<Vo> getVosByPerunBean(PerunSession sess, User user);

  /**
   * Returns list of vos connected with a host
   *
   * @param sess
   * @param host
   * @return list of vos connected with host
   * @throws InternalErrorException
   */
  List<Vo> getVosByPerunBean(PerunSession sess, Host host);

  /**
   * Returns list of vos connected with a facility
   *
   * @param sess
   * @param facility
   * @return list of vos connected with facility
   * @throws InternalErrorException
   */
  List<Vo> getVosByPerunBean(PerunSession sess, Facility facility);

  void checkVoExists(PerunSession sess, Vo vo) throws VoNotExistsException;

  /**
   * Get count of all vos.
   *
   * @param perunSession
   * @return count of all vos
   * @throws InternalErrorException
   */
  int getVosCount(PerunSession perunSession);

  /**
   * Returns number of vo members by their status.
   *
   * @param sess perun session
   * @param vo   vo of members
   * @return map of status in vo to number of vo members with the status
   */
  Map<Status, Integer> getVoMembersCountsByStatus(PerunSession sess, Vo vo);

  /**
   * Check whether a user is in a role for a given VO, possibly checking also user's groups.
   *
   * @param session     session
   * @param user        user
   * @param role        role
   * @param vo          virtual organization
   * @param checkGroups check also groups of the user whether they have the role
   * @return true if user is directly in role for the vo, or if "checkGroups" flag is set and at least one of the groups is in the role
   * @throws InternalErrorException exception
   */
  boolean isUserInRoleForVo(PerunSession session, User user, String role, Vo vo, boolean checkGroups);

  /**
   * Handles a user that lost a role.
   *
   * @param sess perun session
   * @param user user
   * @param vo   virtual organization
   * @param role role of user in VO
   * @throws InternalErrorException
   */
  void handleUserLostVoRole(PerunSession sess, User user, Vo vo, String role);

  /**
   * Handles a group that lost a role.
   *
   * @param sess  perun session
   * @param group group
   * @param vo    virtual organization
   * @param role  role of group in VO
   * @throws InternalErrorException
   */
  void handleGroupLostVoRole(PerunSession sess, Group group, Vo vo, String role);

  /**
   * Set given ban.
   *
   * @param sess    session
   * @param banOnVo ban information, memberId, voId, validity and description are needed
   * @return created ban object
   * @throws MemberNotExistsException
   * @throws BanAlreadyExistsException
   */
  BanOnVo setBan(PerunSession sess, BanOnVo banOnVo) throws MemberNotExistsException, BanAlreadyExistsException;

  /**
   * Get ban by its id.
   *
   * @param sess  session
   * @param banId ban id
   * @return ban object
   * @throws BanNotExistsException if ban with given id is not found
   */
  BanOnVo getBanById(PerunSession sess, int banId) throws BanNotExistsException;

  /**
   * Get ban for given member, if it exists.
   *
   * @param sess     session
   * @param memberId member id
   * @return ban object, or null if there is no ban for given member
   */
  Optional<BanOnVo> getBanForMember(PerunSession sess, int memberId);

  /**
   * Get list of all bans for vo with given id.
   *
   * @param sess session
   * @param voId vo id
   * @return list of bans for given vo
   */
  List<BanOnVo> getBansForVo(PerunSession sess, int voId);

  /**
   * Update ban information. Only description and validity are updated.
   *
   * @param sess    session
   * @param banOnVo updated ban
   * @return updated ban object
   */
  BanOnVo updateBan(PerunSession sess, BanOnVo banOnVo);

  /**
   * Removes ban with given id.
   *
   * @param sess  session
   * @param banId ban id
   * @throws BanNotExistsException if there is no ban with given id
   */
  void removeBan(PerunSession sess, int banId) throws BanNotExistsException;

  /**
   * Removes ban for member with given id.
   *
   * @param sess     session
   * @param memberId member id
   * @throws BanNotExistsException if there is no ban for member with given id
   */
  void removeBanForMember(PerunSession sess, int memberId) throws BanNotExistsException;

  /**
   * Information if there is a ban for member with given id.
   *
   * @param sess     session
   * @param memberId member id
   * @return true, if member with given id is banned, false otherwise
   */
  boolean isMemberBanned(PerunSession sess, int memberId);

  /**
   * For the given vo, creates sponsored members for each sponsored user who is a member
   * of the given vo. Original sponsors of the users will be set to the sponsored members.
   *
   * @param sess session
   * @param vo   vo where members will be converted
   */
  void convertSponsoredUsers(PerunSession sess, Vo vo);

  /**
   * For the given vo, creates sponsored members for each sponsored user who is a member
   * of the given vo. The sponsored members will be sponsored by the given user, not by its
   * original sponsors.
   *
   * @param sess       session
   * @param vo         vo where members will be converted
   * @param newSponsor user, who will be set as a sponsor to the sponsored members
   */
  void convertSponsoredUsersWithNewSponsor(PerunSession sess, Vo vo, User newSponsor);

  /**
   * Returns true, if the given vo uses EMBEDDED_GROUP_APPLICATION item in its form.
   *
   * @param sess session
   * @param vo   vo
   * @return true, if the given vo uses EMBEDDED_GROUP_APPLICATION item in its form, false otherwise.
   */
  boolean usesEmbeddedGroupRegistrations(PerunSession sess, Vo vo);

  /**
   * Adds new relationship between vo and a member vo.
   * If user is member in both vos, updates memberOrganizations list attribute.
   * If user is member only in member vo, creates member in parent vo and sets memberOrganizations list attribute.
   *
   * @param sess     session
   * @param vo       vo
   * @param memberVo new member of the vo
   * @throws RelationExistsException if member vo is already member of the vo
   */
  void addMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationExistsException;

  /**
   * Removes member vo from given vo.
   * Updates memberOrganizations list attribute for those members of parent vo who came from the member vo.
   *
   * @param sess     session
   * @param vo       vo
   * @param memberVo vo to be removed
   * @throws RelationNotExistsException if member vo is not a member of the vo
   */
  void removeMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationNotExistsException;

  /**
   * Gets all member organizations of the given vo.
   *
   * @param sess session
   * @param voId vo id
   * @return list of member vos
   */
  List<Vo> getMemberVos(PerunSession sess, int voId);

  /**
   * Gets all organizations where given vo is direct member.
   *
   * @param sess       session
   * @param memberVoId member vo id
   * @return list of direct parent vos
   */
  List<Vo> getParentVos(PerunSession sess, int memberVoId);

  /**
   * Gets all bans for given user
   *
   * @param sess   session
   * @param userId id of user
   * @return list of bans for given user
   */
  List<BanOnVo> getBansForUser(PerunSession sess, int userId);

  /**
   * Get all Enriched Bans for given VO and attribute names
   *
   * @param sess      sesion
   * @param vo        VO
   * @param attrNames List of attributes, returns all attributes if null or empty
   * @return list of enriched bans
   * @throws VoNotExistsException if vo not exists
   */
  List<EnrichedBanOnVo> getEnrichedBansForVo(PerunSession sess, Vo vo, List<String> attrNames)
      throws AttributeNotExistsException;

  /**
   * Get all Enriched Bans for given User
   *
   * @param sess      sesion
   * @param userId    User ID
   * @param attrNames List of attributes, returns all attributes if null or empty
   * @return List of Enriched Bans
   * @throws UserNotExistsException if user not exists
   */
  List<EnrichedBanOnVo> getEnrichedBansForUser(PerunSession sess, int userId, List<String> attrNames)
      throws AttributeNotExistsException;
}
