package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Group;
import java.util.List;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

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
 * @version $Id: c50827739426cd0f4735f59e98597a376f23d591 $
 * @see PerunSession
 */
public interface VosManagerImplApi {

  /**
   * Get list of all Vos.
   *
   * @param perunSession
   * @throws InternalErrorException
   * @return List of VOs
   */
  List<Vo> getVos(PerunSession perunSession) throws InternalErrorException;

  /**
   * Delete VO.
   *
   * @param perunSession
   * @param vo
   * @throws InternalErrorException
   */
  void deleteVo(PerunSession perunSession, Vo vo) throws InternalErrorException;


  /**
   * Create new VO.
   *
   * @param perunSession
   * @param vo
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
   * @param shortName
   * @return vo
   * @throws VoNotExistsException
   * @throws InternalErrorException
   */
  Vo getVoByShortName(PerunSession perunSession, String shortName) throws VoNotExistsException, InternalErrorException;

  /**
   * Finds existing VO by id.
   *
   * @param perunSession
   * @param id id of the VO you are looking for
   * @return found VO
   * @throws VoNotExistsException
   * @throws InternalErrorException
   */
  Vo getVoById(PerunSession perunSession, int id) throws VoNotExistsException, InternalErrorException;
  
  /**
   * Get list of Vo administrators.
   * If some group is administrator of the VO, all members are included in the list.
   *
   * @param sess
   * @param vo
   * @return List of users, who are administrators of the Vo. Returns empty list if there is no VO admin.   
   * @throws InternalErrorException
   */
  List<User> getAdmins(PerunSession sess, Vo vo) throws InternalErrorException;
  
 /** 
   * Gets list of direct user administrators of the VO.
   * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
   * 
   * @param perunSession
   * @param vo
   * 
   * @throws InternalErrorException
   */
  List<User> getDirectAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException;

  /**
     * Get list of group administrators of the given VO.
   *
   * @param sess
   * @param vo
   * @return List of groups, who are administrators of the Vo. Returns empty list if there is no VO group admin.   
   * @throws InternalErrorException
   */
  List<Group> getAdminGroups(PerunSession sess, Vo vo) throws InternalErrorException;

  /**
   * Add a user administrator to the VO.
   * 
   * @param sess
   * @param vo
   * @param user
   * @throws InternalErrorException
   * @throws AlreadyAdminException
   */
  void addAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException;
  
  /**
   * Add a group administrator to the VO.
   * 
   * @param sess
   * @param vo
   * @param group
   * @throws InternalErrorException
   * @throws AlreadyAdminException
   */
  void addAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException;
    
  /**
   * Removes a user administrator from the VO.
   * 
   * @param sess
   * @param vo
   * @param user
   * @throws InternalErrorException
   * @throws UserNotAdminException
   */
  void removeAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException;
  
    
  /**
   * Removes a group administrator from the VO.
   * 
   * @param sess
   * @param vo
   * @param group
   * @throws InternalErrorException
   * @throws GroupNotAdminException
   */
  void removeAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException;
  
  /**
   * Check if vo exists in underlaying data source.
   * 
   * @param perunSession
   * @param vo
   * @return true if vo exists in underlaying data source, false othewise
   *
   * @throws InternalErrorException
   */
  boolean voExists(PerunSession perunSession, Vo vo) throws InternalErrorException;

  /**
   * Check if vo exists in underlaying data source.
   * 
   * @param perunSession
   * @param vo
   * 
   * @throws InternalErrorException
   * @throws VoNotExistsException
   */
  void checkVoExists(PerunSession perunSession, Vo vo) throws InternalErrorException, VoNotExistsException;
 
  /**
   * Return list of IDs of all applications, which belongs to VO.
   * 
   * @param sess
   * @param vo
   * @return list of all vo applications ids
   */
  public List<Integer> getVoApplicationIds(PerunSession sess, Vo vo);
  
  /**
   * Return list of all reserved logins for specific application
   * (pair is namespace and login)
   * 
   * @param appId from which application get reserved logins
   * @return list of pairs namespace and login
   */
  public List<Pair<String, String>> getApplicationReservedLogins(Integer appId);
  
  /**
   * Delete all VO login reservations
   * 
   * Reserved logins must be removed from external systems 
   * (e.g. KDC) BEFORE calling this method via deletePassword() in
   * UsersManager.
   * 
   * @param sess
   * @param vo VO to delete all login reservations for
   */
  public void deleteVoReservedLogins(PerunSession sess, Vo vo);
  
  /**
   * Creates empty application form definition for VO when 
   * VO is created
   * 
   * @param sess
   * @param vo
   * @throws InternalErrorException
   */
  public void createApplicationForm(PerunSession sess, Vo vo) throws InternalErrorException;
  
}