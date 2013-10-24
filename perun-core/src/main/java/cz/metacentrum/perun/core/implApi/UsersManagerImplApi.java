package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyReservedLoginException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ServiceUserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceUserOwnerAlredyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;

/**
 * UsersManager can find users.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author Zora Sebestianova
 * @version $Id$
 */
  /**
 * @author michalp
 *
 */
public interface UsersManagerImplApi {
    /**
     * Returns user by his login in external source.
     *
     * @param perunSession     
     * @param userExtSource
     * @return user by its userExtSource or throws UserNotExistsException
     * @throws InternalErrorException
     * @throws UserNotExistsException
     */
    User getUserByUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException, UserNotExistsException;
    
    /**
     * Get all the users who have given type of the ExtSource and login.
     * 
     * @param perunSession perun session
     * @param extSourceType type of the user extSource
     * @param login login of the user
     * @return all users with given parameters
     * @throws InternalErrorException
     */
    List<User> getUsersByExtSourceTypeAndLogin(PerunSession perunSession, String extSourceType, String login) throws InternalErrorException;
    
    /**
     * Returns user by its id.
     *
     * @param perunSession
     * @param id
     * @return user
     * @throws UserNotExistsException
     * @throws InternalErrorException
     */
    User getUserById(PerunSession perunSession, int id) throws InternalErrorException, UserNotExistsException;

    /**
     * Return all serviceUsers who are owned by the user
     * 
     * @param sess
     * @param user the user
     * @return list of service users who are owned by the user
     * @throws InternalErrorException 
     */
    List<User> getServiceUsersByUser(PerunSession sess, User user) throws InternalErrorException;
    
    /**
     * Return all users who owns the serviceUser
     * 
     * @param sess
     * @param serviceUser the service User
     * @return list of user who owns the serviceUser
     * @throws InternalErrorException 
     */
    List<User> getUsersByServiceUser(PerunSession sess, User serviceUser) throws InternalErrorException;
    
    /**
     * Remove serviceUser owner (the user)
     * 
     * @param sess
     * @param user the user
     * @param serviceUser the serviceUser
     * @throws InternalErrorException 
     * @throws ServiceUserOwnerAlredyRemovedException if there are 0 rows affected by deleting from DB
     */
    void removeServiceUserOwner(PerunSession sess, User user, User serviceUser) throws InternalErrorException, ServiceUserOwnerAlredyRemovedException;
    
    /**
     * Add serviceUser owner (the user)
     * 
     * @param sess
     * @param user the user
     * @param serviceUser the serviceUser
     * @throws InternalErrorException 
     */
    void addServiceUserOwner(PerunSession sess, User user, User serviceUser) throws InternalErrorException;
    
    /**
     * Return all service Users (only service users)
     * 
     * @param sess
     * @return list of all service users in perun
     * @throws InternalErrorException 
     */
    List<User> getServiceUsers(PerunSession sess) throws InternalErrorException;
    
    /**
     * Delete service user and all connection between service user and other users
     * 
     * @param sess
     * @param serviceUser the service user
     * @throws InternalErrorException 
     */
    void deleteServiceUser(PerunSession sess, User serviceUser) throws InternalErrorException, ServiceUserAlreadyRemovedException;
    
    /**
     * Returns user by VO member.
     *
     * @param perunSession 
     * @param member
     * @return user
     * @throws InternalErrorException
     */
    User getUserByMember(PerunSession perunSession, Member member) throws InternalErrorException;
    
    /**
     * Return users which have member in VO.
     * 
     * @param sess
     * @param vo
     * @return list of users
     * @throws InternalErrorException 
     */
    List<User> getUsersByVo(PerunSession sess, Vo vo) throws InternalErrorException;
    
    /**
     * Returns all users (included service users).
     * 
     * @param sess
     * @return list of all users
     * @throws InternalErrorException
     */
    List<User> getUsers(PerunSession sess) throws InternalErrorException;
    
    /**
     *  Creates the user, stores it in the DB. This method will fill user.id property.
     *
     * @param perunSession   
     * @param user user bean with filled properties
     * @return user with user.id filled
     * @throws InternalErrorException
     */
    User createUser(PerunSession perunSession, User user) throws InternalErrorException;

 
    /**
     *  Deletes user.
     *
     * @param perunSession        
     * @param user 
     * @throws InternalErrorException
     * @throws UserAlredyRemovedException
     */
    void deleteUser(PerunSession perunSession, User user) throws InternalErrorException, UserAlreadyRemovedException;
      
   /**
     *  Updates users data in DB.
     *
     * @param perunSession       
     * @param user
     * @return updated user
     * @throws InternalErrorException
     */
    User updateUser(PerunSession perunSession, User user) throws InternalErrorException;
    
    /**
     *  Updates user;s userExtSource in DB.
     *
     * @param perunSession       
     * @param userExtSource
     * @return updated user
     * @throws InternalErrorException
     */
    UserExtSource updateUserExtSource(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException;
    
    /**
     *  Updates user's userExtSource last access time in DB.
     *
     * @param perunSession
     * @param userExtSource
     * @return updated userExtSource
     * @throws InternalErrorException
     */
    void updateUserExtSourceLastAccess(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException;
    
    /**
     * Gets list of all user external sources ids of the user.
     * 
     * @param perunSession       
     * @param user
     * @return list of user's external sources ids
     * @throws InternalErrorException
     */
    List<Integer> getUserExtSourcesIds(PerunSession perunSession, User user) throws InternalErrorException;

    /**
     * Get the user ext source by its id.
     * 
     * @param sess
     * @param id
     * @return user external source for the id
     * @throws InternalErrorException
     * @throws UserExtSourceNotExistsException
     */
    UserExtSource getUserExtSourceById(PerunSession sess, int id) throws InternalErrorException, UserExtSourceNotExistsException;
    
    /**
     * Adds user's external sources.
     * 
     * @param perunSession       
     * @param user
     * @param userExtSource
     * @return	user external source with userExtSource.id filled
     * @throws InternalErrorException
     */
    UserExtSource addUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource) throws InternalErrorException;
    
    /**
     * Removes user's external sources.
     * 
     * @param perunSession       
     * @param user
     * @param userExtSource
     * @throws InternalErrorException
     * @throws UserExtSourceAlreadyRemovedException if there are 0 rows affected by deleting from DB
     */
    void removeUserExtSource(PerunSession perunSession, User user, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceAlreadyRemovedException;
    
    /**
     *  Removes all user's external sources.
     *
     * @param perunSession        
     * @param user
     * @throws InternalErrorException
     */
    void removeAllUserExtSources(PerunSession perunSession, User user) throws InternalErrorException;
      
    /**
     * Gets user's external source by the user's external login and external source.
     * 
     * @param perunSession
     * @param source
     * @param extLogin
     * @return user external source object
     * @throws InternalErrorException
     * @throws UserExtSourceNotExistsException
     */
    UserExtSource getUserExtSourceByExtLogin(PerunSession perunSession, ExtSource source, String extLogin) throws InternalErrorException, UserExtSourceNotExistsException;

    /**
     * Return true if login in specified namespace is already reserved, false if not.
     * 
     * @param sess
     * @param namespace namespace for login
     * @param login login to check
     * @return true if login exist, false if not exist
     * @throws InternalErrorException 
     */
    boolean isLoginReserved(PerunSession sess, String namespace, String login) throws InternalErrorException;
    
    /**
     * Check if login in specified namespace exists.
     * 
     * @param sess
     * @param namespace namespace for login
     * @param login login to check
     * @throws InternalErrorException
     * @throws AlreadyReservedLoginException throw this exception if login already exist in table of reserved logins 
     */
    void checkReservedLogins(PerunSession sess, String namespace, String login) throws InternalErrorException, AlreadyReservedLoginException;
    
    /**
     * Check if user exists in underlaying data source.
     * 
     * @param perunSession 
     * @param user user to check
     * @return true if user exists in underlaying data source, false othewise
     * @throws InternalErrorException
     */
    boolean userExists(PerunSession perunSession, User user) throws InternalErrorException;

    /**
     * Check if user exists in underlaying data source.
     * 
     * @param perunSession 
     * @param user
     * @throws InternalErrorException
     * @throws UserNotExistsException
     */
    void checkUserExists(PerunSession perunSession, User user) throws InternalErrorException, UserNotExistsException;
    
    /**
     * Check if userExtSource exists in underlaying data source.
     * 
     * @param perunSession 
     * @param userExtSource userExtSource to check
     * @return true if userExtSource exists in underlaying data source, false othewise
     * @throws InternalErrorException
     */
    boolean userExtSourceExists(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException;

    /**
     * Check if userExtSource exists in underlaying data source.
     * 
     * @param perunSession 
     * @param userExtSource
     * @throws InternalErrorException
     * @throws UserExtSourceNotExistsException
     */
    void checkUserExtSourceExists(PerunSession perunSession, UserExtSource userExtSource) throws InternalErrorException, UserExtSourceNotExistsException;

    /**
     * Returns list of VOs, where the user is an Administrator.
     * 
     * @param perunSession
     * @param user
     * @return list of VOs, where the user is an Administrator.
     * @throws InternalErrorException
     */
    List<Vo> getVosWhereUserIsAdmin(PerunSession perunSession, User user) throws InternalErrorException;
    
    /**
     * Returns list of Groups, where the user is an Administrator.
     * 
     * @param perunSession
     * @param user
     * @return list of Groups, where the user is an Administrator.
     * @throws InternalErrorException
     */
    List<Group> getGroupsWhereUserIsAdmin(PerunSession perunSession, User user) throws InternalErrorException;
    
    /**
     * Returns list of Vos' ids, where the user is member.
     * 
     * @param sess
     * @param user
     * @return list of Vos, where the user is member
     * @throws InternalErrorException
     */
    List<Vo> getVosWhereUserIsMember(PerunSession sess, User user) throws InternalErrorException;
    
    /**
     * Returns list of users who matches the searchString, searching name, email and logins.
     * 
     * @param sess
     * @param searchString
     * @return list of users
     * @throws InternalErrorException
     */
    List<User> findUsers(PerunSession sess, String searchString) throws InternalErrorException;
    
    /**
     * Returns list of users who matches the searchString
     * 
     * @param sess
     * @param searchString
     * @return list of users
     * @throws InternalErrorException
     */
    List<User> findUsersByName(PerunSession sess, String searchString) throws InternalErrorException;
    
    /**
     * Returns list of users who matches the fields.
     * 
     * @param sess
     * @param titleBefore
     * @param firstName
     * @param middleName
     * @param lastName
     * @param titleAfter
     * @return list of users
     * @throws InternalErrorException
     */
    List<User> findUsersByName(PerunSession sess, String titleBefore, String firstName, String middleName, String lastName, String titleAfter) throws InternalErrorException;

    /**
     * Returns all users who have set the attribute with the value. Searching only def and opt attributes.
     * 
     * @param sess
     * @param attribute
     * @return list of users
     * @throws InternalErrorException
     */
    List<User> getUsersByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException;
    
    /**
     * Returns all users who have the attribute with the value. attributeValue is not converted to the attribute type, it is always type of String.
     * 
     * @param sess
     * @param attributeDefintion
     * @param attributeValue
     * @return list of users
     * @throws InternalErrorException
     */
    List<User> getUsersByAttributeValue(PerunSession sess, AttributeDefinition attributeDefintion, String attributeValue) throws InternalErrorException;
    
    /**
     * Batch method which returns users by theirs ids.
     * 
     * @param sess
     * @param usersIds
     * @return
     * @throws InternalErrorException
     */
    List<User> getUsersByIds(PerunSession sess, List<Integer> usersIds) throws InternalErrorException;
    
    /**
     * Returns all users who are not member of any VO.
     * 
     * @param sess
     * @return list of users who are not member of any VO
     * @throws InternalErrorException
     */
    List<User> getUsersWithoutVoAssigned(PerunSession sess) throws InternalErrorException;
    
    /**
     * Adds PERUNADMIN role to the user.
     * 
     * @param sess
     * @param user
     * @throws InternalErrorException
     */
    void makeUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException;
    
    /**
     * Returns true if the user is PERUNADMIN.
     * 
     * @param sess
     * @param user
     * @return true if the user is PERUNADMIN, false otherwise.
     * @throws InternalErrorException
     */
    boolean isUserPerunAdmin(PerunSession sess, User user) throws InternalErrorException;
    
    /**
     * Removes all authorships of user when user is deleted from DB
     * (author records on all his publications).
     * 
     * @param sess
     * @param user
     * @throws InternalErrorException thrown when runtime exception
     */
    void removeAllAuthorships(PerunSession sess, User user) throws InternalErrorException;
    
    /**
     * Return list of all reserved logins for specific user
     * (pair is namespace and login)
     * 
     * @param user for which get reserved logins
     * @return list of pairs namespace and login
     */
    public List<Pair<String, String>> getUsersReservedLogins(User user);
    
    /**
     * Delete all reserved logins for specific user
     * (pair is namespace and login)
     * 
     * @param user for which get delete reserved logins
     */
    public void deleteUsersReservedLogins(User user);
    
    /**
     * Get All RichUsers without UserExtSources and without virtual attributes.
     * 
     * @param sess
     * @return list of richUsers
     * @throws InternalErrorException 
     */
    List<Pair<User, Attribute>> getAllRichUsersWithAllNonVirutalAttributes(PerunSession sess) throws InternalErrorException;
}