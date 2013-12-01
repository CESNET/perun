package cz.metacentrum.perun.core.bl;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichFacility;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
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
public interface FacilitiesManagerBl {

  /**
   * Initialize manager.
   */
  void initialize(PerunSession sess);
  
  /**
   * Searches for the Facility with specified id.
   *
   * @param perunSession
   * @param id
   *  
   * @return Facility with specified id
   * 
   * @throws InternalErrorException
   */
  Facility getFacilityById(PerunSession perunSession, int id) throws InternalErrorException, FacilityNotExistsException;
  
  /**
   * Searches for the Facility by its name.
   *
   * @param perunSession
   * @param name
   * @param type
   *  
   * @return Facility with specified name
   * 
   * @throws InternalErrorException
   */
  Facility getFacilityByName(PerunSession perunSession, String name, String type) throws InternalErrorException, FacilityNotExistsException;

   /**
  * Get all rich Facilities with all their owners.
  * 
  * @param perunSession
  * @return list of RichFacilities with owners
  * @throws InternalErrorException 
  */
 List<RichFacility> getRichFacilities(PerunSession perunSession) throws InternalErrorException;
  
 /**
  * Get all RichFacilities with all their owners from list of Facilities.
  * 
  * @param perunSession
  * @param facilities list of facilities
  * @return list of RichFacilities with owners
  * @throws InternalErrorException 
  */
 List<RichFacility> getRichFacilities(PerunSession perunSession, List<Facility> facilities) throws InternalErrorException; 
 
  /**
   * Searches for the Facilities by theirs destination.
   *
   * @param perunSession
   * @param destination
   * 
   * @return Facility with specified name
   * 
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   */
  List<Facility> getFacilitiesByDestination(PerunSession perunSession, String destination) throws InternalErrorException, FacilityNotExistsException;
  
  /**
   * List all facilities.
   *
   * @param perunSession
   * 
   * @return List of all Facilities within the Perun
   * 
   * @throws InternalErrorException
   */
  List<Facility> getFacilities(PerunSession perunSession) throws InternalErrorException;

  /**
   * Searches for the Facilities by theirs type.
   *
   * @param perunSession
   * @param type type of facility
   * 
   * @return Facilities with specified types
   * 
   * @throws InternalErrorException
   */
  List<Facility> getFacilitiesByType(PerunSession perunSession, String type) throws InternalErrorException;

  /**
   * Get count of facilities of specified type
   *
   * @param perunSession
   * @param type type of facility
   * 
   * @return count of facilities of specified types
   * 
   * @throws InternalErrorException
   */
  int getFacilitiesCountByType(PerunSession perunSession, String type) throws InternalErrorException;

  /**
   * Get count of all facilities.
   *
   * @param perunSession
   * 
   * @return count of all facilities
   * 
   * @throws InternalErrorException
   */
  int getFacilitiesCount(PerunSession perunSession) throws InternalErrorException;


  /**
   * Returns owners of the facility.
   * 
   * @param perunSession
   * @param facility 
   * 
   * @return owners of specified facility
   * 
   * @throws InternalErrorException
   */
  List<Owner> getOwners(PerunSession perunSession, Facility facility) throws InternalErrorException;

  /**
   * Updates owners of facility
   * 
   * @param perunSession
   * @param facility
   * @param owners
   * 
   * @throws InternalErrorException
   *
   * @deprecated Use addOwner and removeOwner instead
   */
  @Deprecated
  void setOwners(PerunSession perunSession, Facility facility, List<Owner> owners) throws InternalErrorException, OwnerNotExistsException;

  /**
   * Add owner of the facility
   * 
   * @param perunSession
   * @param facility
   * @param owner
   * 
   * @throws InternalErrorException
   * @throws OwnerAlreadyAssignedException
   */
  void addOwner(PerunSession perunSession, Facility facility, Owner owner) throws InternalErrorException, FacilityNotExistsException, OwnerAlreadyAssignedException;

  /**
   * Remove owner of the facility
   * 
   * @param perunSession
   * @param facility
   * @param owner
   * 
   * @throws InternalErrorException
   * @throws OwnerAlreadyRemovedException
   */
  void removeOwner(PerunSession perunSession, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyRemovedException;

  /**
   * Copy all owners of the source facility to the destionation facility.
   * The owners, that are in the destination facility and aren't in the source faility, are retained.
   * The common owners are replaced with owners from source facility.
   * 
   * @param sourceFacility
   * @param destinationFacility 
   * @throws InternalErrorException
   */
  public void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException;

  
  /**
   * Return all VO which can use this facility. (VO muset have the resource which belongs to this facility)
   * 
   * @param perunSession
   * @param facility
   * 
   * @return list of Vos
   * 
   * @throws InternalErrorException
   */
  List<Vo> getAllowedVos(PerunSession perunSession, Facility facility) throws InternalErrorException;

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
   */
  List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException;
  
   /**
   * Return all users who can use this facility
   * 
   * @param sess
   * @param facility
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> getAllowedUsers(PerunSession sess, Facility facility) throws InternalErrorException;
  
  /**
   * Return all users who can use this facility
   * specificVo and specificService can choose concrete users
   * if specificVo, specificService or both are null, they do not specific (all possible results are returned)
   * 
   * @param sess
   * @param facility
   * @param specificVo specific only those results which are in specific VO (with null, all results)
   * @param specificService specific only those results, which have resource with assigned specific service (if null, all results)
   * @return list of users
   * @throws InternalErrorException
   */
  List<User> getAllowedUsers(PerunSession sess, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException;
  
  /**
   * Returns all resources assigned to the facility.
   * 
   * @param perunSession
   * @param facility
   * 
   * @return list of resources assigned to the facility
   * 
   * @throws InternalErrorException
   */
  List<Resource> getAssignedResources(PerunSession perunSession, Facility facility) throws InternalErrorException;

  /**
   * Returns all rich resources assigned to the facility with VO property filled
   * 
   * @param perunSession
   * @param facility
   * 
   * @return list of resources assigned to the facility
   * 
   * @throws InternalErrorException
   */
  List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility) throws InternalErrorException;
  
  /**
   * Store the facility.
   * 
   * @param perunSession
   * @param facility
   * @return
   * @throws InternalErrorException     
   * @throws FacilityExistsException
   */
  Facility createFacility(PerunSession perunSession, Facility facility) throws InternalErrorException, FacilityExistsException;

  /**
   * Delete the facility by id.
   *
   * @param perunSession
   * @param facility
   * 
   * @throws InternalErrorException                 
   * @throws RelationExistsException there are still some resources assigned to this facility
   * @throws FacilityAlreadyRemovedException there are 0 rows affected by delete from DB
   * @throws HostAlreadyRemovedException if there is at least 1 host who was not affected by deleting from DB
   * @throws GroupAlreadyRemovedException if there is at least 1 group not affected by deleting from DB
   * @throws ResourceAlreadyRemovedException if there are at least 1 resource not affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException there are at least 1 group on resource not affected by deleting from DB
   */
  void deleteFacility(PerunSession perunSession, Facility facility) throws InternalErrorException, RelationExistsException, FacilityAlreadyRemovedException, HostAlreadyRemovedException, GroupAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

  /**
   * Returns list of all facilities owned by the owner.
   * 
   * @param perunSession
   * @param owner
   * 
   * @return list of facilities owned by the owner
   * 
   * @throws InternalErrorException
   */
  List<Facility> getOwnerFacilities(PerunSession perunSession, Owner owner) throws InternalErrorException;

  /**
   * Get facilities which are assigned to Group (via resource).
   * 
   * @param sess
   * @param group
   * @return
   * 
   * @throws InternalErrorException
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Group group) throws InternalErrorException;

  /**
   * Get facilities which have the member access on.
   * 
   * @param sess
   * @param member
   * @return
   * 
   * @throws InternalErrorException
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Member member) throws InternalErrorException;

  /**
   * Get facilities where the user is assigned.
   * 
   * @param sess
   * @param user
   * @return
   * 
   * @throws InternalErrorException
   */
  List<Facility> getAssignedFacilities(PerunSession sess, User user) throws InternalErrorException;

  /**
   * Get facilities where the user have access.
   * 
   * @param sess
   * @param user
   * @return
   * 
   * @throws InternalErrorException
   */
  List<Facility> getAllowedFacilities(PerunSession sess, User user) throws InternalErrorException;
  
  /**
   * Get facilities where the services is defined.
   * 
   * @param sess
   * @param service
   * @return
   * 
   * @throws InternalErrorException
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Service service) throws InternalErrorException;

  /**
   * Returns all facilities which have set the attribute with the value. Searching only def and opt attributes.
   * 
   * @param sess
   * @param attribute
   * @return 
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  List<Facility> getFacilitiesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException, WrongAttributeAssignmentException;

  /**
   * Returns VO which contains Facilities Administrators.
   * 
   * @param sess
   * @return VO containing facilities administrators
   * @throws InternalErrorException
   */
  Vo getFacititiesAdminsVo(PerunSession sess) throws InternalErrorException;

  /**
   * List hosts of Facility.
   * 
   * @param sess
   * @param facility
   * 
   * @return hosts
   *
   * @throws InternalErrorException
   * 
   */
  List<Host> getHosts(PerunSession sess, Facility facility) throws InternalErrorException;

  /**
   * Count hosts in the facility.
   * 
   * @param sess
   * @param facility
   * 
   * @return the number of hosts present in the Cluster.
   * 
   * @throws InternalErrorException
   */
  int getHostsCount(PerunSession sess, Facility facility) throws InternalErrorException;

  /**
   * Create hosts in Perun and add them to the Facility
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
   * @throws InternalErrorException
   * @throws HostExistsException
   */
  List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws InternalErrorException, HostExistsException; 

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
   */
  Host addHost(PerunSession perunSession, Host host, Facility facility) throws InternalErrorException; 

  /**
   * Remove hosts from the Facility.
   * 
   * @param perunSession
   * @param host
   * 
   * @throws InternalErrorException
   * @throws HostAlreadyRemovedException if 0 rows was affected by deleting from DB
   */
  void removeHost(PerunSession perunSession, Host host) throws InternalErrorException, HostAlreadyRemovedException;

  /**
   * Remove hosts from the Facility.
   * 
   * @param sess
   * @param hosts
   * @param facility
   * 
   * @throws InternalErrorException
   * @throws HostAlreadyRemovedException if there is at least 1 not removed host (not affected by removing from DB)
   */
  void removeHosts(PerunSession sess, List<Host> hosts, Facility facility) throws InternalErrorException, HostAlreadyRemovedException;

  /**
   * Get the host by its ID.
   * 
   * @param sess
   * @param id
   * @return host
   * @throws HostNotExistsException
   * @throws InternalErrorException
   */
  Host getHostById(PerunSession sess, int id) throws HostNotExistsException, InternalErrorException;
  
  /**
   * Return facility which has the host.
   * 
   * @param sess
   * @param host
   * @return facility
   * @throws InternalErrorException
   */
  Facility getFacilityForHost(PerunSession sess, Host host) throws InternalErrorException;
  
  /**
   * Return all facilities where exists host with the specific hostname
   * 
   * @param sess
   * @param hostname specific hostname
   * @return
   * @throws InternalErrorException 
   */
  List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname) throws InternalErrorException;  
  
  /**
   * Check if host exists in the facility.
   * 
   * @param sess
   * @param facility
   * @param host
   * @return true if exists, false otherwise
   * @throws InternalErrorException
   */
  
  /**
   * Adds user administrator to the Facility.
   * 
   * @param sess
   * @param facility
   * @param user
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws AlreadyAdminException 
   */
  void addAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, AlreadyAdminException;
 
   /**
   * Adds group administrator to the Facility.
   * 
   * @param sess
   * @param facility
   * @param group that will become a Facility administrator
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws AlreadyAdminException 
   */
  void addAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, AlreadyAdminException;
 
  /**
   * Removes a user administrator from the Facility.
   * 
   * @param sess
   * @param facility
   * @param user
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws UserNotAdminException
   */
  void removeAdmin(PerunSession sess, Facility facility, User user) throws InternalErrorException, UserNotAdminException;

   /**
   * Removes a group administrator from the Facility.
   * 
   * @param sess
   * @param facility
   * @param group group that will lose a Facility administrator role
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws GroupNotAdminException
   */
  void removeAdmin(PerunSession sess, Facility facility, Group group) throws InternalErrorException, GroupNotAdminException;

  /**
   * Gets list of all user administrators of the Facility.
   * If some group is administrator of the given group, all members are included in the list.
   * 
   * @param sess
   * @param facility
   * @return list of Users who are admins in the facility
   * @throws InternalErrorException
   */
  List<User> getAdmins(PerunSession sess, Facility facility) throws InternalErrorException;

  /** 
   * Gets list of direct user administrators of the Facility.
   * 'Direct' means, there aren't included users, who are members of group administrators, in the returned list.
   * 
   * @param perunSession
   * @param facility
   * 
   * @throws InternalErrorException
   */
  List<User> getDirectAdmins(PerunSession perunSession, Facility facility) throws InternalErrorException;

  /**
   * Gets list of all group administrators of the Facility.
   * 
   * @param sess
   * @param facility
   * @return list of Groups that are admins in the facility
   * @throws InternalErrorException
   */
  List<Group> getAdminGroups(PerunSession sess, Facility facility) throws InternalErrorException;

  /**
   * Get all Facility admins without attributes.
   * 
   * @param sess
   * @param facility
   * @return return list of RichUsers without attributes.
   * @throws InternalErrorException
   * @throws UserNotExistsException 
   */
  List<RichUser> getRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException;
  
  /**
   * Get all Facility admins with attributes.
   * 
   * @param sess
   * @param facility
   * @return list of Rich Users who are admins in the facility WITH ATTRIBUTES
   * @throws InternalErrorException
   * @throws UserNotExistsException 
   */
  List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException;
 
  /**
   * Get list of Facility administrators with specific attributes.
   * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
   * 
   * @param perunSession
   * @param facility
   * @param specificAttributes
   * @return list of RichUsers with specific attributes.
   * @throws InternalErrorException
   */
  List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException;
      
  
  /**
   * Returns list of Facilities, where the user is an admin.
   * 
   * @param sess
   * @param user
   * @return list of Facilities, where the user is an admin.
   * @throws InternalErrorException
   */
  List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException;
 
  /**
   * !!! Not Complete yet, need to implement all perunBeans !!!
   * 
   * Get perunBean and try to find all connected Facilities
   * 
   * @param sess
   * @param perunBean
   * @return list of facilities connected with perunBeans
   * @throws InternalErrorException 
   */
  List<Facility> getFacilitiesByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException;
  
  void checkFacilityExists(PerunSession sess, Facility facility) throws InternalErrorException, FacilityNotExistsException;
  
  void checkHostExists(PerunSession sess, Host host) throws InternalErrorException, HostNotExistsException;
  
  /**
   * Returns list of Users assigned to choosen Facility.
   * 
   * @param sess
   * @param facility
   * @return list of Users
   * @throws InternalErrorException 
   */
  
  public List<User> getAssignedUsers(PerunSession sess, Facility facility) throws InternalErrorException;
  
  /**
   * Returns list of Users assigned with chosen Facility containing resources where service is assigned.
   * 
   * @param sess
   * @param facility
   * @param service 
   * @return list of Users
   * @throws InternalErrorException 
   */
  
  public List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service) throws InternalErrorException;
  
   /**
   * Copy all managers(admins) of the source facility to the destionation facility.
   * The admins, that are in the destination facility and aren't in the source faility, are retained.
   * The common admins are replaced with admins from source facility.
   * 
   * @param sess
   * @param sourceFacility
   * @param destinationFacility
   * @throws InternalErrorException 
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
   * @throws WrongAttributeAssignmentException if there is no facility attribute
   * @throws WrongAttributeValueException if the attribute value is illegal
   * @throws WrongReferenceAttributeValueException if the attribute value is illegal
   */
  public void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

}
