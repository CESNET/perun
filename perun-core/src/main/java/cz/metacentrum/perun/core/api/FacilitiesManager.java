package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeSetException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import java.util.List;

/**
 * Facility manager can create a new facility or find an existing facility.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 */
public interface FacilitiesManager {

  /**
   * Adds user administrator to the Facility.
   */
  void addAdmin(PerunSession sess, Facility facility, User user)
          throws FacilityNotExistsException, UserNotExistsException, PrivilegeException, AlreadyAdminException,
          RoleCannotBeManagedException, RoleCannotBeSetException;

  /**
   * Adds group administrator to the Facility.
   *
   * @param group that will become a Facility administrator
   */
  void addAdmin(PerunSession sess, Facility facility, Group group)
      throws FacilityNotExistsException, GroupNotExistsException, PrivilegeException, AlreadyAdminException,
      RoleCannotBeManagedException, RoleCannotBeSetException;

  /**
   * Adds host to the Facility. Adds host only if host and destination with the same name doesn't exist or if privilege
   * requirements are met.
   *
   * @return host
   */
  Host addHost(PerunSession perunSession, Host host, Facility facility)
      throws FacilityNotExistsException, PrivilegeException;

  /**
   * Adds hosts to the Facility. Adds hosts only if host and destination with the same name doesn't exist or if
   * privilege requirements are met. Note: If you wish to add more hosts, it is recommended to prepare a List<Host> of
   * them so as there can be only one database call.
   *
   * @param hosts ID of any host doesn't need to be filled. Hosts will be created.
   * @return Hosts with ID's set.
   */
  List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility)
      throws FacilityNotExistsException, PrivilegeException, HostExistsException;

  /**
   * Create hosts in Perun and add them to the Facility. Adds hosts only if host and destination with the same name
   * doesn't exist or if privilege requirements are met. Names of the hosts can be generative. The pattern is string
   * with square brackets, e.g. "local[1-3]domain". Then the content of the brackets is distributed, so the list is
   * [local1domain, local2domain, local3domain]. Multibrackets are aslo allowed. For example "a[00-01]b[90-91]c"
   * generates [a00b90c, a00b91c, a01b90c, a01b91c].
   *
   * @param hosts list of strings with names of hosts, the name can by generative
   * @return Hosts with ID's set.
   * @throws WrongPatternException when syntax of any of the hostnames is wrong
   */
  List<Host> addHosts(PerunSession sess, Facility facility, List<String> hosts)
      throws FacilityNotExistsException, PrivilegeException, HostExistsException, WrongPatternException;

  /**
   * Add owner of the facility
   */
  @Deprecated
  void addOwner(PerunSession perunSession, Facility facility, Owner owner)
      throws PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyAssignedException;

  /**
   * Add owners of the facility
   *
   * @param sess     perun session
   * @param facility facility
   * @param owners   list of owners to be added
   * @throws PrivilegeException            when the principal does not have sufficient rights
   * @throws OwnerNotExistsException       when owner does not exist
   * @throws FacilityNotExistsException    when facility does not exist
   * @throws OwnerAlreadyAssignedException when owner was already assigned to the facility
   */
  @Deprecated
  void addOwners(PerunSession sess, Facility facility, List<Owner> owners)
      throws PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyAssignedException;

  /**
   * Copy all attributes of the source facility to the destination facility. The attributes, that are in the destination
   * facility and aren't in the source facility, are retained. The common attributes are replaced with attributes from
   * source facility.
   *
   * @throws WrongAttributeAssignmentException     if there is no facility attribute
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongReferenceAttributeValueException if the attribute value is illegal
   */
  void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility)
      throws PrivilegeException, FacilityNotExistsException, WrongAttributeAssignmentException,
      WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Copy all managers(admins) of the source facility to the destination facility. The admins, that are in the
   * destination facility and aren't in the source facility, are retained. The common admins are also retained in
   * destination facility.
   */
  void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Copy all owners of the source facility to the destination facility. The owners, that are in the destination
   * facility and aren't in the source facility, are retained. The common owners are replaced with owners from source
   * facility.
   */
  @Deprecated
  void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility)
      throws FacilityNotExistsException, PrivilegeException;

  /**
   * Store the facility.
   */
  Facility createFacility(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityExistsException, ConsentHubExistsException;

  /**
   * Delete the facility by id.
   *
   * @throws FacilityAlreadyRemovedException          if 0 rows affected by delete from DB
   * @throws HostAlreadyRemovedException              if there is at least 1 hosts not affected by deleting from DB
   * @throws ResourceAlreadyRemovedException          if there is at least 1 resource not affected by deleting from DB
   * @throws GroupAlreadyRemovedFromResourceException if there is at least 1 group on any resource affected by removing
   *                                                  from DB
   */
  void deleteFacility(PerunSession perunSession, Facility facility, Boolean force)
      throws RelationExistsException, FacilityNotExistsException, PrivilegeException, FacilityAlreadyRemovedException,
      HostAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

  /**
   * Gets list of all group administrators of the Facility.
   *
   * @return list of Group that are admins in the facility.
   */
  @Deprecated
  List<Group> getAdminGroups(PerunSession sess, Facility facility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Gets list of all user administrators of the Facility. If some group is administrator of the given group, all VALID
   * members are included in the list.
   * <p>
   * If onlyDirectAdmins is true, return only direct users of the group for supported role.
   * <p>
   * Supported roles: FacilityAdmin
   *
   * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
   * @return list of all user administrators of the given facility for supported role
   */
  @Deprecated
  List<User> getAdmins(PerunSession perunSession, Facility facility, boolean onlyDirectAdmins)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Gets list of all user administrators of the Facility. If some group is administrator of the given group, all
   * members are included in the list.
   *
   * @return list of Users who are admins in the facility.
   */
  @Deprecated
  List<User> getAdmins(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException;

  /**
   * Get all Groups which can use this facility (Groups must be assigned to resource which belongs to this facility)
   * specificVo and specificService can choose concrete groups if specificVo, specificService or both are null, they do
   * not specific (all possible results are returned)
   *
   * @param facility        searching for this facility
   * @param specificVo      specific only those results which are in specific VO (with null, all results)
   * @param specificService specific only those results, which have resource with assigned specific service (if null,
   *                        all results)
   * @return list of allowed groups
   * @throws FacilityNotExistsException if facility not exist, return this exception
   * @throws ServiceNotExistsException  if service is not null and not exist
   * @throws VoNotExistsException       if vo is not null and not exist
   */
  List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService)
      throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException;

  /**
   * Get all RichGroups which can use this facility (Groups must be assigned to Resource which belongs to this facility)
   * specificVo and specificService can choose concrete groups if specificVo, specificService or both are null, they do
   * not specific (all possible results are returned) We also retrieve attributes specified by attrNames for each
   * returned RichGroup.
   *
   * @param facility        searching for this facility
   * @param specificVo      specific only those results which are in specific VO (with null, all results)
   * @param specificService specific only those results, which have resource with assigned specific service (if null,
   *                        all results)
   * @param attrNames       with each returned RichGroup we get also attributes specified by this list
   * @return list of allowed groups
   * @throws FacilityNotExistsException if facility not exist, return this exception
   * @throws ServiceNotExistsException  if service is not null and not exist
   * @throws VoNotExistsException       if vo is not null and not exist
   */
  List<RichGroup> getAllowedRichGroupsWithAttributes(PerunSession perunSession, Facility facility, Vo specificVo,
                                                     Service specificService, List<String> attrNames)
      throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException;

  /**
   * Return all users who can use this facility
   *
   * @return list of users
   */
  List<User> getAllowedUsers(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Return all users who can use this facility specificVo and specificService can choose concrete users if specificVo,
   * specificService or both are null, they do not specific (all possible results are returned)
   *
   * @param specificVo      specific only those results which are in specific VO (with null, all results)
   * @param specificService specific only those results, which have resource with assigned specific service (if null,
   *                        all results)
   * @return list of users
   * @throws ServiceNotExistsException if service is not null and not exist
   * @throws VoNotExistsException      if vo is not null and not exist
   */
  List<User> getAllowedUsers(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService)
      throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException, VoNotExistsException;

  /**
   * Return all VO which can use this facility. (VO muset have the resource which belongs to this facility)
   *
   * @return list of Vos
   */
  List<Vo> getAllowedVos(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Get facilities which are assigned to Group (via resource).
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Group group)
      throws PrivilegeException, GroupNotExistsException;

  /**
   * Get facilities which have the member access on.
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Member member)
      throws PrivilegeException, MemberNotExistsException;

  /**
   * Get facilities which have the user access on.
   */
  List<Facility> getAssignedFacilities(PerunSession sess, User user) throws PrivilegeException, UserNotExistsException;

  /**
   * Get facilities where the service is defined.
   */
  List<Facility> getAssignedFacilities(PerunSession sess, Service service)
      throws PrivilegeException, ServiceNotExistsException;

  /**
   * Returns all resources assigned to the facility.
   *
   * @return list of resources assigned to the facility
   */
  List<Resource> getAssignedResources(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Returns resources with specific service assigned to the facility.
   *
   * @param perunSession perun session
   * @param facility     facility
   * @param service      specific only those results, which have resource with assigned specific service
   * @return list of resources assigned to the facility by specific service
   * @throws FacilityNotExistsException if facility does not exist
   * @throws ServiceNotExistsException  if service does not exist
   */
  List<Resource> getAssignedResourcesByAssignedService(PerunSession perunSession, Facility facility, Service service)
      throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException;

  /**
   * Returns all rich resources assigned to the facility with VO property filled
   *
   * @return list of rich resources assigned to the facility
   */
  List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Returns all rich resources assigned to the facility and service with VO property filled
   *
   * @return list of rich resources assigned to the facility and service
   */
  List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility, Service service)
      throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException;

  /**
   * Returns list of Users, assigned with chosen Facility.
   *
   * @return list of users
   */
  List<User> getAssignedUsers(PerunSession sess, Facility facility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Returns list of Users assigned with chosen Facility containing resources where service is assigned.
   *
   * @return list of users
   */
  List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service)
      throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException;

  /**
   * Get ban by userId and facilityId.
   *
   * @param userId    the id of user
   * @param faclityId the id of facility
   * @return specific ban for user on facility
   */
  BanOnFacility getBan(PerunSession sess, int userId, int faclityId)
      throws BanNotExistsException, PrivilegeException, UserNotExistsException, FacilityNotExistsException;

  /**
   * Get Ban for user on facility by it's id
   *
   * @param banId the id of ban
   * @return facility ban by it's id
   */
  BanOnFacility getBanById(PerunSession sess, int banId) throws BanNotExistsException, PrivilegeException;

  /**
   * Get all bans for users on the facility
   *
   * @param facilityId the id of facility
   * @return list of bans for all users on the facility
   */
  List<BanOnFacility> getBansForFacility(PerunSession sess, int facilityId)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Get all bans for user on any facility.
   *
   * @param userId the id of user
   * @return list of bans for user on any facility
   */
  List<BanOnFacility> getBansForUser(PerunSession sess, int userId) throws UserNotExistsException;

  /**
   * Gets list of direct user administrators of the Facility. 'Direct' means, there aren't included users, who are
   * members of group administrators, in the returned list.
   */
  @Deprecated
  List<User> getDirectAdmins(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Get list of Facility administrators, which are assigned directly (not by group membership), with specific
   * attributes. From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only,
   * other attributes are not searched)
   *
   * @return list of RichUsers with specific attributes.
   */
  @Deprecated
  List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility,
                                                           List<String> specificAttributes)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Get all enriched bans for users on the facility.
   *
   * @param sess
   * @param facilityId id of the facility
   * @param attrNames  list of attribute names, returns all user attributes if null or empty
   * @return list of enriched bans on facility
   * @throws PrivilegeException
   * @throws FacilityNotExistsException
   */
  List<EnrichedBanOnFacility> getEnrichedBansForFacility(PerunSession sess, int facilityId, List<String> attrNames)
      throws PrivilegeException, FacilityNotExistsException, AttributeNotExistsException;

  /**
   * Get all enriched bans on facility for user.
   *
   * @param sess
   * @param userId    id of user
   * @param attrNames list of attributes names, returns all user/member attributes if null or empty
   * @return list of user's enriched bans on assigned facilities
   * @throws PrivilegeException
   * @throws UserNotExistsException
   */
  List<EnrichedBanOnFacility> getEnrichedBansForUser(PerunSession sess, int userId, List<String> attrNames)
      throws PrivilegeException, UserNotExistsException, AttributeNotExistsException;

  /**
   * Gets all enriched facilities user has access rights to. If User is: - PERUNADMIN : all facilities - FACILITYADMIN :
   * only facilities where user is facility admin - FACILITYOBSERVER: only facilities where user is facility observer
   *
   * @param perunSession
   * @return List of all EnrichedFacilities
   * @throws PrivilegeException insufficient permissions
   */
  List<EnrichedFacility> getEnrichedFacilities(PerunSession perunSession) throws PrivilegeException;

  /**
   * Return all EnrichedHosts of given facility. That is host with attributes given by attrNames.
   *
   * @param sess      perun session
   * @param facility  facility
   * @param attrNames attribute names
   * @return list of enriched hosts
   * @throws AttributeNotExistsException if some attribute does not exist
   * @throws FacilityNotExistsException  if facility does not exist
   * @throws PrivilegeException          if user has insufficient permissions
   */
  List<EnrichedHost> getEnrichedHosts(PerunSession sess, Facility facility, List<String> attrNames)
      throws AttributeNotExistsException, FacilityNotExistsException, PrivilegeException;

  /**
   * List facilities by ACCESS RIGHTS: If User is: - PERUNADMIN : all facilities - FACILITYADMIN : only facilities where
   * user is facility admin
   *
   * @return List of all Facilities within the Perun
   */
  List<Facility> getFacilities(PerunSession perunSession) throws PrivilegeException;

  /**
   * Returns all facilities that have set the attribute 'attributeName' with the value 'attributeValue'. Searching only
   * def and opt attributes. Large attributes are not supported.
   *
   * @param attributeName  name of the attribute
   * @param attributeValue value of the attribute
   * @return list of facilities
   */
  List<Facility> getFacilitiesByAttribute(PerunSession sess, String attributeName, String attributeValue)
      throws PrivilegeException, AttributeNotExistsException, WrongAttributeAssignmentException;

  /**
   * Searches (partially!) for facilities with the attribute 'searchAttributeName' and its value 'searchAttributeValue'.
   * Found Facilities are returned along with attributes listed in 'attrNames'.
   *
   * @param sess
   * @param searchAttributeName  name of the attribute to search by
   * @param searchAttributeValue value to search for
   * @param attrNames            names of attributes to return with facilities
   * @return list of facilities with attributes
   * @throws PrivilegeException
   * @throws AttributeNotExistsException when the attribute to search by does not exist
   */
  List<FacilityWithAttributes> getFacilitiesByAttributeWithAttributes(PerunSession sess, String searchAttributeName,
                                                                      String searchAttributeValue,
                                                                      List<String> attrNames)
      throws PrivilegeException, AttributeNotExistsException;

  /**
   * Searches for the Facilities by theirs destination.
   *
   * @return Facilities with specified name
   * @throws FacilityNotExistsException //FIXME proc tato vyjimka?
   */
  List<Facility> getFacilitiesByDestination(PerunSession perunSession, String destination)
      throws FacilityNotExistsException, PrivilegeException;

  /**
   * Return all facilities where exists host with the specific hostname
   *
   * @param hostname specific hostname
   */
  List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname);

  /**
   * Searches for the Facilities with specified ids.
   *
   * @param perunSession
   * @param ids
   * @return list of Facilities with specified ids
   * @throws PrivilegeException
   */
  List<Facility> getFacilitiesByIds(PerunSession perunSession, List<Integer> ids) throws PrivilegeException;

  /**
   * Get count of all facilities.
   *
   * @return count of all facilities
   */
  int getFacilitiesCount(PerunSession perunSession);

  /**
   * Get all facilities where the user is admin. Including facilities, where the user is a VALID member of authorized
   * group.
   *
   * @return list of Facilities, where the user is an admin.
   */
  List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user)
      throws UserNotExistsException, PrivilegeException;

  /**
   * Searches for the Facility with specified id.
   *
   * @return Facility with specified id
   */
  Facility getFacilityById(PerunSession perunSession, int id) throws FacilityNotExistsException, PrivilegeException;

  /**
   * Searches the Facility by its name.
   *
   * @return Facility with specified name
   */
  Facility getFacilityByName(PerunSession perunSession, String name)
      throws FacilityNotExistsException, PrivilegeException;

  /**
   * Return facility which has the host.
   *
   * @return facility
   */
  Facility getFacilityForHost(PerunSession sess, Host host) throws PrivilegeException, HostNotExistsException;

  /**
   * Get the host by its ID.
   *
   * @return host
   */
  Host getHostById(PerunSession sess, int id) throws HostNotExistsException, PrivilegeException;

  /**
   * List hosts of Facility.
   *
   * @return hosts
   */
  List<Host> getHosts(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException;

  /**
   * Get all hosts with this hostname (from all facilities).
   * <p>
   * Facility Admin get only those which are from his facilities.
   *
   * @return list of hosts by hostname
   */
  List<Host> getHostsByHostname(PerunSession sess, String hostname) throws PrivilegeException;

  /**
   * Count hosts of Facility.
   *
   * @return the number of hosts present in the facility
   */
  int getHostsCount(PerunSession sess, Facility facility) throws FacilityNotExistsException, PrivilegeException;

  /**
   * Returns list of all facilities owned by the owner.
   *
   * @return list of facilities owned by the owner
   */
  List<Facility> getOwnerFacilities(PerunSession perunSession, Owner owner)
      throws OwnerNotExistsException, PrivilegeException;

  /**
   * Returns owners of the facility.
   *
   * @return owners of specified facility
   */
  @Deprecated
  List<Owner> getOwners(PerunSession perunSession, Facility facility)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Gets list of all richUser administrators of the Facility. If some group is administrator of the given group, all
   * VALID members are included in the list.
   * <p>
   * Supported roles: FacilityAdmin
   * <p>
   * If "onlyDirectAdmins" is "true", return only direct users of the group for supported role with specific attributes.
   * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser .
   * Ignoring list of specific attributes.
   *
   * @param specificAttributes list of specified attributes which are needed in object richUser
   * @param allUserAttributes  if true, get all possible user attributes and ignore list of specificAttributes (if
   *                           false, get only specific attributes)
   * @param onlyDirectAdmins   if true, get only direct user administrators (if false, get both direct and indirect)
   * @return list of RichUser administrators for the facility and supported role with attributes
   */
  @Deprecated
  List<RichUser> getRichAdmins(PerunSession perunSession, Facility facility, List<String> specificAttributes,
                               boolean allUserAttributes, boolean onlyDirectAdmins)
      throws UserNotExistsException, PrivilegeException, FacilityNotExistsException;

  /**
   * Get all Facility admins without attributes.
   *
   * @return return list of RichUsers without attributes.
   */
  @Deprecated
  List<RichUser> getRichAdmins(PerunSession sess, Facility facility)
      throws FacilityNotExistsException, PrivilegeException;

  /**
   * Get all Facility admins with attributes.
   *
   * @return list of RichUsers who are admins in the facility WITH ATTRIBUTES.
   */
  @Deprecated
  List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility)
      throws UserNotExistsException, PrivilegeException, FacilityNotExistsException;

  /**
   * Get list of Facility administrators with specific attributes. From list of specificAttributes get all Users
   * Attributes and find those for every RichAdmin (only, other attributes are not searched)
   *
   * @return list of RichUsers with specific attributes.
   */
  @Deprecated
  List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility,
                                                     List<String> specificAttributes)
      throws PrivilegeException, FacilityNotExistsException;

  /**
   * Get all possible rich Facilities with all their owners. For PerunAdmin get All richFacilities. For FacilityAdmin
   * get only richFacilities under his administration.
   *
   * @return list of RichFacilities with owners
   */
  List<RichFacility> getRichFacilities(PerunSession perunSession) throws PrivilegeException;

  /**
   * Removes a user administrator from the Facility.
   */
  void removeAdmin(PerunSession sess, Facility facility, User user)
      throws FacilityNotExistsException, UserNotExistsException, PrivilegeException, UserNotAdminException,
      RoleCannotBeManagedException;

  /**
   * Removes a group administrator from the Facility.
   *
   * @param group group that will lose a Facility administrator role
   */
  void removeAdmin(PerunSession sess, Facility facility, Group group)
      throws FacilityNotExistsException, GroupNotExistsException, PrivilegeException, GroupNotAdminException,
      RoleCannotBeManagedException;

  /**
   * Remove existing ban by it's id.
   *
   * @param banId the id of ban
   */
  void removeBan(PerunSession sess, int banId)
      throws BanNotExistsException, PrivilegeException, FacilityNotExistsException;

  /**
   * Remove existing ban by id of user and facility.
   *
   * @param userId     the id of user
   * @param facilityId the id of facility
   */
  void removeBan(PerunSession sess, int userId, int facilityId)
      throws BanNotExistsException, PrivilegeException, FacilityNotExistsException;

  /**
   * Remove hosts from the Facility.
   *
   * @throws HostAlreadyRemovedException if there are 0 rows affected by deleting from DB
   */
  void removeHost(PerunSession perunSession, Host host)
      throws HostNotExistsException, PrivilegeException, HostAlreadyRemovedException;

  /**
   * Remove host from the Facility based on hostname. If there is ambiguity, method throws exception and no host is
   * removed.
   *
   * @throws HostAlreadyRemovedException if there are 0 rows affected by deleting from DB
   * @throws HostNotExistsException      if there is ambiguity in host to remove
   */
  void removeHostByHostname(PerunSession perunSession, String hostname)
      throws InternalErrorException, HostNotExistsException, HostAlreadyRemovedException, PrivilegeException;

  /**
   * Remove hosts from the Facility.
   *
   * @throws HostAlreadyRemovedException if there is at least 1 host not affected by deleting from DB
   */
  void removeHosts(PerunSession sess, List<Host> hosts, Facility facility)
      throws FacilityNotExistsException, PrivilegeException, HostAlreadyRemovedException;

  /**
   * Remove owner of the facility
   */
  @Deprecated
  void removeOwner(PerunSession perunSession, Facility facility, Owner owner)
      throws PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyRemovedException;

  /**
   * Remove owners of the facility
   *
   * @param sess     perun session
   * @param facility facility
   * @param owners   list of owners to be removed
   * @throws PrivilegeException           when the principal does not have sufficient rights
   * @throws OwnerNotExistsException      when owner does not exist
   * @throws FacilityNotExistsException   when facility does not exist
   * @throws OwnerAlreadyRemovedException when owner was already removed from the facility
   */
  @Deprecated
  void removeOwners(PerunSession sess, Facility facility, List<Owner> owners)
      throws PrivilegeException, OwnerNotExistsException, FacilityNotExistsException, OwnerAlreadyRemovedException;

  /**
   * Set ban for user on facility.
   *
   * @param banOnFacility the ban
   * @return ban on facility
   */
  BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility)
      throws PrivilegeException, BanAlreadyExistsException, UserNotExistsException, FacilityNotExistsException;

  /**
   * Updates owners of facility
   *
   * @deprecated Use addOwner and removeOwner instead
   */
  @Deprecated
  void setOwners(PerunSession perunSession, Facility facility, List<Owner> owners)
      throws PrivilegeException, FacilityNotExistsException, OwnerNotExistsException;

  /**
   * Update existing ban (description and validation timestamp)
   *
   * @param banOnFacility the existing ban
   * @return updated ban
   */
  BanOnFacility updateBan(PerunSession sess, BanOnFacility banOnFacility)
      throws PrivilegeException, FacilityNotExistsException, UserNotExistsException, BanNotExistsException;

  /**
   * Updates facility.
   *
   * @param facility to update
   * @return updated facility
   * @throws FacilityExistsException
   * @throws FacilityNotExistsException
   * @throws InternalErrorException
   * @throws PrivilegeException
   * @throws ConsentHubExistsException
   */
  Facility updateFacility(PerunSession perunSession, Facility facility)
      throws FacilityNotExistsException, FacilityExistsException, PrivilegeException, ConsentHubExistsException;
}
