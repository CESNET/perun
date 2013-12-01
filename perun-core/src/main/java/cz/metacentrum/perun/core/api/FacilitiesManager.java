package cz.metacentrum.perun.core.api;

import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;

/**
 * Facility manager can create a new facility or find an existing facility.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 * @author 
 * @version $Id$
 */
public interface FacilitiesManager {

  public final static String CLUSTERTYPE = "cluster";
  public final static String VIRTUALCLUSTERTYPE = "vcluster";
  public final static String HOSTTYPE = "host";
  public final static String VIRTUALHOSTTYPE = "vhost";
  public final static String GENERALTYPE = "general";
  public final static String STORAGE = "storage";
  
  public final static String FACADMINVO = "facadmins";
  public final static String FACADMINVONAME = "Facilities Administrators";
  
  /**
   * Searches for the Facility with specified id.
   *
   * @param perunSession
   * @param id
   *  
   * @return Facility with specified id
   * 
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  Facility getFacilityById(PerunSession perunSession, int id) throws InternalErrorException, FacilityNotExistsException, PrivilegeException;

 /**
  * Searches the Facility by its name.
  *
  * @param perunSession
  * @param name
  * @param type
  *  
  * @return Facility with specified name
  * 
  * @throws FacilityNotExistsException
  * @throws InternalErrorException
  * @throws PrivilegeException
  */
 Facility getFacilityByName(PerunSession perunSession, String name, String type) throws InternalErrorException, FacilityNotExistsException, PrivilegeException;

 /**
  * Get all possible rich Facilities with all their owners.
  * For PerunAdmin get All richFacilities.
  * For FacilityAdmin get only richFacilities under his administration.
  * 
  * @param perunSession
  * @return list of RichFacilities with owners
  * @throws InternalErrorException
  * @throws PrivilegeException 
  */
 List<RichFacility> getRichFacilities(PerunSession perunSession) throws InternalErrorException, PrivilegeException;
 
 /**
  * Searches for the Facilities by theirs destination.
  *
  * @param perunSession
  * @param destination
  * 
  * @return Facilities with specified name
  * 
  * @throws FacilityNotExistsException  //FIXME proc tato vyjimka?
  * @throws InternalErrorException
  * @throws PrivilegeException
  */
 List<Facility> getFacilitiesByDestination(PerunSession perunSession, String destination) throws InternalErrorException, FacilityNotExistsException, PrivilegeException;

 /**
  * Searches for the Facilities by theirs type.
  *
  * @param perunSession
  * @param type type of facility
  * 
  * @return Facilities with specified types
  * 
  * @throws InternalErrorException
  * @throws PrivilegeException
  */
 List<Facility> getFacilitiesByType(PerunSession perunSession, String type) throws InternalErrorException, PrivilegeException;

 /**
  * Get count of facilities of specified type
  *
  * @param perunSession
  * @param type type of facility
  * 
  * @return count of facilities of specified types
  * 
  * @throws InternalErrorException
  * @throws PrivilegeException
  */
 int getFacilitiesCountByType(PerunSession perunSession, String type) throws InternalErrorException, PrivilegeException;

 /**
  * Get count of all facilities.
  *
  * @param perunSession
  * 
  * @return count of all facilities
  * 
  * @throws InternalErrorException
  * @throws PrivilegeException
  */
 int getFacilitiesCount(PerunSession perunSession) throws InternalErrorException, PrivilegeException;
 
  /**
   * List facilities by ACCESS RIGHTS:
   * If User is:
   * - PERUNADMIN : all facilities
   * - FACILITYADMIN : only facilities where user is facility admin
   *
   * @param perunSession
   * 
   * @return List of all Facilities within the Perun
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<Facility> getFacilities(PerunSession perunSession) throws InternalErrorException, PrivilegeException;

  /**
   * Returns owners of the facility.
   * 
   * @param perunSession
   * @param facility 
   * 
   * @return owners of specified facility
   * 
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<Owner> getOwners(PerunSession perunSession, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;

  /**
   * Updates owners of facility
   * 
   * @param perunSession
   * @param facility
   * @param owners
   * 
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws OwnerNotExistsException
   *
   * @deprecated Use addOwner and removeOwner instead
   */
  @Deprecated
  void setOwners(PerunSession perunSession, Facility facility, List<Owner> owners) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, OwnerNotExistsException;

  /**
   * Add owner of the facility
   * 
   * @param perunSession
   * @param facility
   * @param owner
   * 
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws OwnerNotExistsException
   * @throws OwnerAlreadyAssignedException
   */
  void addOwner(PerunSession perunSession, Facility facility, Owner owner) throws InternalErrorException, PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyAssignedException;

  /**
   * Remove owner of the facility
   * 
   * @param perunSession
   * @param facility
   * @param owner
   * 
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws OwnerNotExistsException
   * @throws OwnerAlreadyRemovedException
   */
  void removeOwner(PerunSession perunSession, Facility facility, Owner owner) throws InternalErrorException, PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyRemovedException;

  /**
   * Copy all owners of the source facility to the destionation facility.
   * The owners, that are in the destination facility and aren't in the source faility, are retained.
   * The common owners are replaced with owners from source facility.
   * 
   * @param sourceFacility 
   * @param destinationFacility 
   * @throws InternalErrorException
   * @throws FacilityNotExistsException
   * @throws PrivilegeException
   */
  public void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException;
  
  /**
   * Return all VO which can use this facility. (VO muset have the resource which belongs to this facility)
   * 
   * @param perunSession
   * @param facility
   * 
   * @return list of Vos
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws FacilityNotExistsException
   */
  List<Vo> getAllowedVos(PerunSession perunSession, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;

  /**
   * Get all Groups which can use this facility (Groups must be assigned to resource which belongs to this facility)
   * specificVo and specificService can choose concrete groups
   * if specificVo, specificService or both are null, they do not specific (all possible results are returned)
   * 
   * @param perunSession
   * @param facility searching for this facility
   * @param specificVo specific only those results which are in specific VO (with null, all results)
   * @param specificService specific only those results, which have resource with assigned specific service (if null, all results)
   * @return list of allowed groups
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws FacilityNotExistsException if facility not exist, return this exception
   * @throws ServiceNotExistsException if service is not null and not exist
   * @throws VoNotExistsException if vo is not null and not exist
   */
  List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException;
  
  /**
   * Return all users who can use this facility
   * 
   * @param perunSession
   * @param facility
   * 
   * @return list of users
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws FacilityNotExistsException
   */
  List<User> getAllowedUsers(PerunSession perunSession, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;

  
  /**
   * Return all users who can use this facility
   * specificVo and specificService can choose concrete users
   * if specificVo, specificService or both are null, they do not specific (all possible results are returned)
   * 
   * @param perunSession
   * @param facility
   * @param specificVo specific only those results which are in specific VO (with null, all results)
   * @param specificService specific only those results, which have resource with assigned specific service (if null, all results)
   * 
   * @return list of users
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws FacilityNotExistsException
   * @throws ServiceNotExistsException if service is not null and not exist
   * @throws VoNotExistsException if vo is not null and not exist
   */
  List<User> getAllowedUsers(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException;

  /**
   * Returns all resources assigned to the facility.
   * 
   * @param perunSession
   * @param facility
   * 
   * @return list of resources assigned to the facility
   * 
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<Resource> getAssignedResources(PerunSession perunSession, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;

  /**
   * Returns all rich resources assigned to the facility with VO property filled
   * 
   * @param perunSession
   * @param facility
   * 
   * @return list of rich resources assigned to the facility
   * 
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;
  
  /**
   * Store the facility.
   * 
   * @param perunSession
   * @param facility
   * @return
   * @throws InternalErrorException     
   * @throws FacilityExistsException
   * @throws PrivilegeException
   */
  Facility createFacility(PerunSession perunSession, Facility facility) throws InternalErrorException, PrivilegeException, FacilityExistsException;

  /**
   * Delete the facility by id.
   *
   * @param perunSession
   * @param facility
   * 
   * @throws InternalErrorException                 
   * @throws FacilityNotExistsException
   * @throws PrivilegeException
   * @throws RelationExistsException
   * @throws FacilityAlreadyRemovedException if 0 rows affected by delete from DB
   * @throws HostAlreadyRemovedException if there is at least 1 hosts not affected by deleting from DB
   * @throws GroupAlreadyRemovedException if there is at least 1 group not affected by deleting from DB
   * @throws ResourceAlreadyRemovedException if there is at least 1 resource not affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on any resource affected by removing from DB
   */
  void deleteFacility(PerunSession perunSession, Facility facility) throws InternalErrorException, RelationExistsException, FacilityNotExistsException, PrivilegeException, FacilityAlreadyRemovedException, HostAlreadyRemovedException, GroupAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

  /**
   * Returns list of all facilities owned by the owner.
   * 
   * @param perunSession
   * @param owner
   * 
   * @return list of facilities owned by the owner
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException 
   * @throws OwnerNotExistsException 
   */
  List<Facility> getOwnerFacilities(PerunSession perunSession, Owner owner) throws InternalErrorException, OwnerNotExistsException, PrivilegeException;

  /**
   * Get facilities which are assigned to Group (via resource).
   * 
   * @param sess
   * @param group
   * @return
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotExistsException
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Group group) throws InternalErrorException, PrivilegeException, GroupNotExistsException;

  /**
   * Get facilities which have the member access on.
   * 
   * @param sess
   * @param member
   * @return
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws MemberNotExistsException
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException;

  /**
   * Get facilities which have the user access on.
   * 
   * @param sess
   * @param group
   * @return
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<Facility> getAssignedFacilities(PerunSession sess, User user) throws InternalErrorException, PrivilegeException, UserNotExistsException;
  
  /**
   * Get facilities where the service is defined.
   * 
   * @param sess
   * @param service
   * @return
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ServiceNotExistsException
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Service service) throws InternalErrorException, PrivilegeException, ServiceNotExistsException;

  /**
   * List hosts of Facility.
   *
   * @param sess
   * @param facility
   *
   * @return hosts
   *
   * @throws FacilityNotExistsException
   * @throws PrivilegeException
   * @throws InternalErrorException
   *
   */
  List<Host> getHosts(PerunSession sess, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException;

  /**
   * Count hosts of Facility.
   *
   * @param sess
   * @param facility
   *
   * @return the number of hosts present in the facility
   *
   * @throws FacilityNotExistsException
   * @throws PrivilegeException
   * @throws InternalErrorException
   */
  int getHostsCount(PerunSession sess, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException;

  /**
   * Adds hosts to the Facility
   * Note: If you wish to add more hosts, it is recommended to
   * prepare a List<Host> of them so as there can be only one
   * database call.
   *
   * @param sess
   * @param hosts ID of any host doesn't need to be filled. Hosts will be created.
   * @param facility
   *
   * @return Hosts with ID's set.
   *
   * @throws FacilityNotExistsException
   * @throws PrivilegeException
   * @throws InternalErrorException
   * @throws HostExistsException
   */
  List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException, HostExistsException;

  /**
   * Remove hosts from the Facility.
   *
   * @param sess
   * @param hosts
   * @param facility
   *
   * @throws FacilityNotExistsException
   * @throws PrivilegeException
   * @throws InternalErrorException
   * @throws HostAlreadyRemovedException if there is at least 1 host not affected by deleting from DB
   */
  void removeHosts(PerunSession sess, List<Host> hosts, Facility facility) throws FacilityNotExistsException, InternalErrorException, PrivilegeException, HostAlreadyRemovedException;
  
  /**
   * Adds host to the Facility. 
   * 
   * @param perunSession
   * @param host
   * @param facility
   * 
   * return host
   * 
   * @throws InternalErrorException
   * @throws FacilityNotExistsException
   * @throws PrivilegeException    
   */
  Host addHost(PerunSession perunSession, Host host, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException; 

  /**
   * Remove hosts from the Facility.
   * 
   * @param perunSession
   * @param host
   * @param facility
   * 
   * @throws InternalErrorException
   * @throws HostNotExistsException
   * @throws PrivilegeException
   * @throws HostAlreadyRemovedException if there are 0 rows affected by deleting from DB
   */
  void removeHost(PerunSession perunSession, Host host) throws InternalErrorException, HostNotExistsException, PrivilegeException, HostAlreadyRemovedException;

  /**
   * Get the host by its ID.
   * 
   * @param sess
   * @param id
   * @return host
   * @throws HostNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  Host getHostById(PerunSession sess, int id) throws HostNotExistsException, InternalErrorException, PrivilegeException;
  
  /**
   * Return facility which has the host.
   * 
   * @param sess
   * @param host
   * @return facility
   * @throws HostNotExistsException
   * @throws InternalErrorException
   */
  Facility getFacilityForHost(PerunSession sess, Host host) throws InternalErrorException, PrivilegeException, HostNotExistsException;
  
  /**
   * Return all facilities where exists host with the specific hostname
   * 
   * @param sess
   * @param hostname specific hostname
   * @return
   * @throws InternalErrorException
   * @throws PrivilegeException 
   */
  List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) throws InternalErrorException, PrivilegeException;
  
  /**
   * Adds user administrator to the Facility.
   * 
   * @param sess
   * @param facility
   * @param user
   * @throws InternalErrorException
   * @throws FacilityNotExistsException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws AlreadyAdminException 
   */
  void addAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, FacilityNotExistsException, UserNotExistsException, PrivilegeException, AlreadyAdminException;
  
   /**
   * Adds group administrator to the Facility.
   * 
   * @param sess
   * @param facility
   * @param group that will become a Facility administrator
   * @throws InternalErrorException
   * @throws FacilityNotExistsException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   * @throws AlreadyAdminException 
   */
  void addAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, FacilityNotExistsException, GroupNotExistsException, PrivilegeException, AlreadyAdminException;
  
  /**
   * Removes a user administrator from the Facility.
   * 
   * @param sess
   * @param facility
   * @param user
   * @throws InternalErrorException
   * @throws FacilityNotExistsException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws UserNotAdminException
   */
  void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, FacilityNotExistsException, UserNotExistsException, PrivilegeException, UserNotAdminException;
 
  /**
   * Removes a group administrator from the Facility.
   * 
   * @param sess
   * @param facility
   * @param group group that will lose a Facility administrator role
   * @throws InternalErrorException
   * @throws FacilityNotExistsException
   * @throws GroupNotExistsException
   * @throws PrivilegeException
   * @throws GroupNotAdminException
   */
  void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, FacilityNotExistsException, GroupNotExistsException, PrivilegeException, GroupNotAdminException;
 
  /**
   * Gets list of all user administrators of the Facility.
   * If some group is administrator of the given group, all members are included in the list.
   * 
   * @param sess
   * @param facility
   * @return list of Users who are admins in the facility.
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<User> getAdmins(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;
  
  /** 
   * Gets list of direct user administrators of the Facility.
   * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
   * 
   * @param perunSession
   * @param facility
   * 
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws FacilityNotExistsException
   * @throws InternalErrorRuntimeException
   */
  List<User> getDirectAdmins(PerunSession perunSession, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;

  /**
   * Gets list of all group administrators of the Facility.
   * 
   * @param sess
   * @param facility
   * @return list of Group that are admins in the facility.
   * @throws InternalErrorException
   * @throws PrivilegeException
   */
  List<Group> getAdminGroups(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;
  
  /**
   * Get all Facility admins without attributes.
   * 
   * @param sess
   * @param facility
   * @return return list of RichUsers without attributes.
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException 
   */
  List<RichUser> getRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException, FacilityNotExistsException, PrivilegeException;
  
  /**
   * Get all Facility admins with attributes.
   * 
   * @param sess
   * @param facility
   * @return list of RichUsers who are admins in the facility WITH ATTRIBUTES.
   * @throws InternalErrorException
   * @throws UserNotExistsException
   * @throws PrivilegeException
   * @throws FacilityNotExistsException 
   */
  List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException, PrivilegeException, FacilityNotExistsException;
          
  /**
   * Get list of Facility administrators with specific attributes.
   * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
   * 
   * @param perunSession
   * @param facility
   * @param specificAttributes
   * @return list of RichUsers with specific attributes.
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws VoNotExistsException 
   */
  List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;
    
  /**
   * Returns list of Facilities, where the user is an admin.
   * 
   * @param sess
   * @param user
   * @return list of Facilities, where the user is an admin.
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException;
  
  /**
   * Returns list of Users, assigned with choosen Facility.
   * 
   * @param sess 
   * @param facility 
   * 
   * @return list of users
   * 
   * @throws PrivilegeException
   * @throws InternalErrorException
   */
  List<User> getAssignedUsers(PerunSession sess, Facility facility) throws PrivilegeException, InternalErrorException;
          
  /**
   * Returns list of Users assigned with chosen Facility containing resources where service is assigned.
   * 
   * @param sess 
   * @param facility 
   * @param service 
   * 
   * @return list of users
   * 
   * @throws PrivilegeException
   * @throws InternalErrorException
   */
  
  List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service) throws PrivilegeException, InternalErrorException;
  
  /**
   * Copy all managers(admins) of the source facility to the destionation facility.
    * The admins, that are in the destination facility and aren't in the source faility, are retained.
    * The common admins are replaced with admins from source facility.
   * 
   * @param sess
   * @param sourceFacility
   * @param destinationFacility
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws FacilityNotExistsException 
   */
  void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;
  
 /**
  * Copy all attributes of the source facility to the destionation facility.
  * The attributes, that are in the destination facility and aren't in the source faility, are retained.
  * The common attributes are replaced with attributes from source facility.
  * 
  * @param sess
  * @param sourceFacility
  * @param destinationFacility
  * @throws InternalErrorException
  * @throws PrivilegeException
  * @throws FacilityNotExistsException
  * @throws WrongAttributeAssignmentException if there is no facility attribute
  * @throws WrongAttributeValueException if the attribute value is illegal
  * @throws WrongReferenceAttributeValueException if the attribute value is illegal
  */
  public void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;
}
