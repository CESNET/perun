package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import java.util.List;

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
public interface VosManagerImplApi {

  /**
   * Adds new relationship between vo and a member vo.
   *
   * @param sess     session
   * @param vo       vo
   * @param memberVo new member of the vo
   * @throws RelationExistsException if member vo is already member of the vo
   */
  void addMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationExistsException;

  /**
   * Check if vo exists in underlaying data source.
   *
   * @param perunSession
   * @param vo
   * @throws InternalErrorException
   * @throws VoNotExistsException
   */
  void checkVoExists(PerunSession perunSession, Vo vo) throws VoNotExistsException;

  /**
   * Creates empty application form definition for VO when VO is created
   *
   * @param sess
   * @param vo
   * @throws InternalErrorException
   */
  void createApplicationForm(PerunSession sess, Vo vo);

  /**
   * Create new VO.
   *
   * @param perunSession
   * @param vo
   * @return newly created VO
   * @throws VoExistsException
   * @throws InternalErrorException
   */
  Vo createVo(PerunSession perunSession, Vo vo) throws VoExistsException;

  /**
   * Delete VO.
   *
   * @param perunSession
   * @param vo
   * @return deleted VO
   * @throws InternalErrorException
   */
  Vo deleteVo(PerunSession perunSession, Vo vo);

  /**
   * Get list of group administrators of the given VO for specific role.
   *
   * @param sess
   * @param vo
   * @return List of groups, who are administrators of the Vo with specific role. Returns empty list if there is no such
   * authorized group.
   * @throws InternalErrorException
   */
  List<Group> getAdminGroups(PerunSession sess, Vo vo, String role);

  /**
   * Get list of group administrators of the given VO.
   *
   * @param sess
   * @param vo
   * @return List of groups, who are administrators of the Vo. Returns empty list if there is no VO group admin.
   * @throws InternalErrorException
   */
  @Deprecated
  List<Group> getAdminGroups(PerunSession sess, Vo vo);

  /**
   * Get list of user administrators of specific vo for specific role. If some group is administrator of the VO, all
   * VALID members are included in the list.
   *
   * @param sess
   * @param vo
   * @param role
   * @return List of users who are administrators of the vo with specific role. Empty list if there is no such
   * administrator
   * @throws InternalErrorException
   */
  List<User> getAdmins(PerunSession sess, Vo vo, String role);

  /**
   * Get list of Vo administrators. If some group is administrator of the VO, all members are included in the list.
   *
   * @param sess
   * @param vo
   * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.
   * @throws InternalErrorException
   */
  @Deprecated
  List<User> getAdmins(PerunSession sess, Vo vo);

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
   * Get ban for given member or NULL if there is no ban.
   *
   * @param sess     session
   * @param memberId member id
   * @return ban object or NULL
   */
  BanOnVo getBanForMember(PerunSession sess, int memberId);

  /**
   * Gets all rich bans for given user.
   *
   * @param sess   session
   * @param userId user id
   * @return
   */
  List<BanOnVo> getBansForUser(PerunSession sess, int userId);

  /**
   * Get list of all bans for vo with given id.
   *
   * @param sess session
   * @param voId vo id
   * @return list of bans for given vo
   */
  List<BanOnVo> getBansForVo(PerunSession sess, int voId);

  /**
   * Get list of direct user administrators of specific vo for specific role. 'Direct' means, there aren't included
   * users, who are members of group administrators, in the returned list.
   *
   * @param sess
   * @param vo
   * @param role
   * @return List of direct users who are administrators of the vo with specific role. Empty list if there is no such
   * administrator
   * @throws InternalErrorException
   */
  List<User> getDirectAdmins(PerunSession sess, Vo vo, String role);

  /**
   * Gets list of direct user administrators of the VO. 'Direct' means, there aren't included users, who are members of
   * group administrators, in the returned list.
   *
   * @param perunSession
   * @param vo
   * @throws InternalErrorException
   */
  @Deprecated
  List<User> getDirectAdmins(PerunSession perunSession, Vo vo);

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
   * Return list of IDs of all applications, which belongs to VO.
   *
   * @param sess
   * @param vo
   * @return list of all vo applications ids
   * @throws InternalErrorException
   */
  List<Integer> getVoApplicationIds(PerunSession sess, Vo vo);

  /**
   * Finds existing VO by id.
   *
   * @param perunSession
   * @param id           id of the VO you are looking for
   * @return found VO
   * @throws VoNotExistsException
   * @throws InternalErrorException
   */
  Vo getVoById(PerunSession perunSession, int id) throws VoNotExistsException;

  /**
   * Find existing VO by short name (short name is unique).
   *
   * @param perunSession
   * @param shortName
   * @return vo
   * @throws VoNotExistsException
   * @throws InternalErrorException
   */
  Vo getVoByShortName(PerunSession perunSession, String shortName) throws VoNotExistsException;

  /**
   * Get list of all Vos.
   *
   * @param perunSession
   * @return List of VOs
   * @throws InternalErrorException
   */
  List<Vo> getVos(PerunSession perunSession);

  /**
   * Gets VOs by their ids. Silently skips non-existing VOs.
   *
   * @param perunSession
   * @param ids
   * @return List of VOs with specified ids
   * @throws InternalErrorException
   */
  List<Vo> getVosByIds(PerunSession perunSession, List<Integer> ids);

  /**
   * Get count of all vos.
   *
   * @param perunSession
   * @return count of all vos
   * @throws InternalErrorException
   */
  int getVosCount(PerunSession perunSession);

  /**
   * Returns true, if there is a vo with given id which has application form with the EMBEDDED_GROUP_APPLICATION item in
   * it.
   *
   * @param sess session
   * @param voId vo id
   * @return true, if there is a vo with given id which has application form with the EMBEDDED_GROUP_APPLICATION item in
   * it, false otherwise
   */
  boolean hasEmbeddedGroupsItemInForm(PerunSession sess, int voId);

  /**
   * Information if there is a ban for member with given id.
   *
   * @param sess     session
   * @param memberId member id
   * @return true, if member with given id is banned, false otherwise
   */
  boolean isMemberBanned(PerunSession sess, int memberId);

  /**
   * Removes ban with given id.
   *
   * @param sess  session
   * @param banId ban id
   * @throws BanNotExistsException if there is no ban with given id
   */
  void removeBan(PerunSession sess, int banId) throws BanNotExistsException;

  /**
   * Removes member vo from given vo.
   *
   * @param sess     session
   * @param vo       vo
   * @param memberVo vo to be removed
   * @throws RelationNotExistsException if member vo is not a member of the vo
   */
  void removeMemberVo(PerunSession sess, Vo vo, Vo memberVo) throws RelationNotExistsException;

  /**
   * Set given ban.
   *
   * @param sess    session
   * @param banOnVo ban information, memberId, voId, validity and description are needed
   * @return created ban object
   */
  BanOnVo setBan(PerunSession sess, BanOnVo banOnVo);

  /**
   * Update ban information. Only description and validity are updated.
   *
   * @param sess    session
   * @param banOnVo updated ban
   * @return updated ban object
   */
  BanOnVo updateBan(PerunSession sess, BanOnVo banOnVo);

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
   * Check if vo exists in underlaying data source.
   *
   * @param perunSession
   * @param vo
   * @return true if vo exists in underlaying data source, false otherwise
   * @throws InternalErrorException
   */
  boolean voExists(PerunSession perunSession, Vo vo);

}
