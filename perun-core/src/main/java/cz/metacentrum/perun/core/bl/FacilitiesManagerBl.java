package cz.metacentrum.perun.core.bl;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.ContactGroup;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichFacility;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityContactNotExistsException;
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
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongPatternException;

/**
 * Facility manager can create a new facility or find an existing facility.
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 */
public interface FacilitiesManagerBl {

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
	 *
	 * @return Facility with specified name
	 *
	 * @throws InternalErrorException
	 */
	Facility getFacilityByName(PerunSession perunSession, String name) throws InternalErrorException, FacilityNotExistsException;

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
	 * Copy all owners of the source facility to the destination facility.
	 * The owners, that are in the destination facility and aren't in the source facility, are retained.
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
	 * Return all members, which are "allowed" on facility.
	 *
	 * @param sess
	 * @param facility
	 *
	 * @return list of allowed members
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getAllowedMembers(PerunSession sess, Facility facility) throws InternalErrorException;

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
	 * Returns all resources assigned to the facility with optionally VO and Service specified
	 *
	 * @param perunSession
	 * @param facility
	 * @param specificVo
	 * @param specificService
	 *
	 * @return list of resources assigned to the facility
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException;

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
	 * Updates facility.
	 *
	 * @param perunSession
	 * @param facility to update
	 *
	 * @return updated facility
	 *
	 * @throws InternalErrorException
	 */
	Facility updateFacility(PerunSession perunSession, Facility facility) throws InternalErrorException;

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
	 * Get facilities where the security team is assigned
	 *
	 * @param sess
	 * @param securityTeam
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getAssignedFacilities(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException;

	/**
	 * Returns all facilities which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess perun session
	 * @param attributeName attribute name to be searched by
	 * @param attributeValue attribute value to be searched by
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Facility> getFacilitiesByAttribute(PerunSession sess, String attributeName, String attributeValue) throws InternalErrorException, WrongAttributeAssignmentException;

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
	 * Create hosts in Perun and add them to the Facility.
	 * Names of the hosts can be generative.
	 * The pattern is string with square brackets, e.g. "local[1-3]domain". Then the content of the brackets
	 * is distributed, so the list is [local1domain, local2domain, local3domain].
	 * Multibrackets are aslo allowed. For example "a[00-01]b[90-91]c" generates [a00b90c, a00b91c, a01b90c, a01b91c].
	 *
	 * @param sess
	 * @param hosts list of strings with names of hosts, the name can by generative
	 * @param facility
	 *
	 * @return Hosts with ID's set.
	 *
	 * @throws InternalErrorException
	 * @throws HostExistsException
	 * @throws WrongPatternException when syntax of any of the hostnames is wrong
	 */
	List<Host> addHosts(PerunSession sess, Facility facility, List<String> hosts) throws InternalErrorException, HostExistsException, WrongPatternException;

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
	 * Get all hosts with this hostname (from all facilities).
	 *
	 *
	 * @param sess
	 * @param hostname
	 * @return list of hosts by hostname
	 * @throws InternalErrorException
	 */
	List<Host> getHostsByHostname(PerunSession sess, String hostname) throws InternalErrorException;

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
	 * Get list of all user administrators for supported role and given facility.
	 *
	 * If onlyDirectAdmins is true, return only direct users of the group for supported role.
	 *
	 * Supported roles: FacilityAdmin
	 *
	 * @param perunSession
	 * @param facility
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of all user administrators of the given facility for supported role
	 *
	 * @throws InternalErrorException
	 */
	List<User> getAdmins(PerunSession perunSession, Facility facility, boolean onlyDirectAdmins) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;

	/**
	 * Get list of all richUser administrators for the facility and supported role with specific attributes.
	 *
	 * Supported roles: FacilityAdmin
	 *
	 * If "onlyDirectAdmins" is "true", return only direct users of the group for supported role with specific attributes.
	 * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param perunSession
	 * @param group
	 *
	 * @param specificAttributes list of specified attributes which are needed in object richUser
	 * @param allUserAttributes if true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins if true, get only direct user administrators (if false, get both direct and indirect)
	 *
	 * @return list of RichUser administrators for the facility and supported role with attributes
	 *
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	List<RichUser> getRichAdmins(PerunSession perunSession, Facility facility, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException;

	/**
	 * Gets list of all user administrators of the Facility.
	 * If some group is administrator of the given group, all members are included in the list.
	 *
	 * @param sess
	 * @param facility
	 * @return list of Users who are admins in the facility
	 * @throws InternalErrorException
	 */
	@Deprecated
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
	@Deprecated
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
	@Deprecated
	List<RichUser> getRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get all Facility admins, which are assigned directly (not by group membership) without attributes.
	 *
	 * @param sess
	 * @param facility
	 * @return return list of RichUsers without attributes.
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdmins(PerunSession sess, Facility facility) throws InternalErrorException, UserNotExistsException;

	/**
	 * Get all Facility admins with attributes.
	 *
	 * @param sess
	 * @param facility
	 * @return list of Rich Users who are admins in the facility WITH ATTRIBUTES
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 */
	@Deprecated
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
	@Deprecated
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException;

	/**
	 * Get list of Facility administrators. which are assigned directly (not by group membership) with specific attributes.
	 * From list of specificAttributes get all Users Attributes and find those for every RichAdmin (only, other attributes are not searched)
	 *
	 * @param perunSession
	 * @param facility
	 * @param specificAttributes
	 * @return list of RichUsers with specific attributes.
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes) throws InternalErrorException;

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
	 * Returns list of Users assigned to chosen Facility.
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
	 * Copy all managers(admins) of the source facility to the destination facility.
	 * The admins, that are in the destination facility and aren't in the source facility, are retained.
	 * The common admins are replaced with admins from source facility.
	 *
	 * @param sess
	 * @param sourceFacility
	 * @param destinationFacility
	 * @throws InternalErrorException
	 */
	void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;

	/**
	 * Copy all attributes of the source facility to the destination facility.
	 * The attributes, that are in the destination facility and aren't in the source facility, are retained.
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

	// FACILITY CONTACTS METHODS

	/**
	 * Get list of contact groups for the owner.
	 *
	 * @param sess
	 * @param owner
	 * @return list of ContactGroups for the owner
	 * @throws InternalErrorException
	 */
	List<ContactGroup> getFacilityContactGroups(PerunSession sess, Owner owner) throws InternalErrorException;

	/**
	 * Get list of contact groups for the user.
	 *
	 * @param sess
	 * @param user
	 * @return list of ContactGroups for the user
	 * @throws InternalErrorException
	 */
	List<ContactGroup> getFacilityContactGroups(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get list of contact groups for the group.
	 *
	 * @param sess
	 * @param group
	 * @return list of ContactGroups for the group
	 * @throws InternalErrorException
	 */
	List<ContactGroup> getFacilityContactGroups(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get list of contact groups for the facility
	 *
	 * @param sess
	 * @param facility
	 * @return list of ContactGroups for the facility
	 * @throws InternalErrorException
	 */
	List<ContactGroup> getFacilityContactGroups(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Get contact group for the facility and the contact group name
	 *
	 * @param sess
	 * @param facility
	 * @param name
	 * @return contactGroup for the facility and the contact group name
	 * @throws InternalErrorException
	 * @throws FacilityContactNotExistsException
	 */
	ContactGroup getFacilityContactGroup(PerunSession sess, Facility facility, String name) throws InternalErrorException, FacilityContactNotExistsException;

	/**
	 * Get all exist contact group names.
	 *
	 * @param sess
	 * @return list of all contact group names
	 * @throws InternalErrorException
	 */
	List<String> getAllContactGroupNames(PerunSession sess) throws InternalErrorException;

	/**
	 * Create all contacts from list of contact groups
	 *
	 * @param sess
	 * @param contactGroupsToAdd
	 * @throws InternalErrorException
	 */
	void addFacilityContacts(PerunSession sess, List<ContactGroup> contactGroupsToAdd) throws InternalErrorException;

	/**
	 * Create all contacts from contact group
	 *
	 * @param sess
	 * @param contactGroupToAdd
	 * @throws InternalErrorException
	 */
	void addFacilityContact(PerunSession sess, ContactGroup contactGroupToAdd) throws InternalErrorException;

	/**
	 * Remove all facilities contacts assigned to this owner.
	 *
	 * @param sess
	 * @param owner
	 * @throws InternalErrorException
	 */
	void removeAllOwnerContacts(PerunSession sess, Owner owner) throws InternalErrorException;

	/**
	 * Remove all facilities contacts assigned to this user.
	 *
	 * @param sess
	 * @param user
	 * @throws InternalErrorException
	 */
	void removeAllUserContacts(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Remove all facilities contacts assigned to this group.
	 *
	 * @param sess
	 * @param group
	 * @throws InternalErrorException
	 */
	void removeAllGroupContacts(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Remove all contacts from list of contact groups
	 *
	 * @param sess
	 * @param contactGroupsToRemove
	 * @throws InternalErrorException
	 */
	void removeFacilityContacts(PerunSession sess, List<ContactGroup> contactGroupsToRemove) throws InternalErrorException;

	/**
	 * Remove all contacts from contact group
	 *
	 * @param sess
	 * @param contactGroupToRemove
	 * @throws InternalErrorException
	 */
	void removeFacilityContact(PerunSession sess, ContactGroup contactGroupToRemove) throws InternalErrorException;

	/**
	 * Return all security teams which specific facility trusts
	 *
	 * @param sess
	 * @param facility specific facility
	 * @return list of assigned security teams
	 * @throws InternalErrorException
	 */
	List<SecurityTeam> getAssignedSecurityTeams(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Assign given security team to given facility (means the facility trusts the security team)
	 *
	 * @param sess
	 * @param facility
	 * @param securityTeam
	 * @throws InternalErrorException
	 */
	void assignSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException;

	/**
	 * Remove (Unassign) given security team from given facility
	 *
	 * @param sess
	 * @param facility
	 * @param securityTeam
	 * @throws InternalErrorException
	 */
	void removeSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException;

	/**
	 * Check if facility contact for the user already exists.
	 * Throw exception info not.
	 *
	 * @param sess
	 * @param facility
	 * @param name
	 * @param user
	 * @throws InternalErrorException
	 * @throws FacilityContactNotExistsException
	 */
	void checkFacilityContactExists(PerunSession sess, Facility facility, String name, User user) throws InternalErrorException, FacilityContactNotExistsException;

	/**
	 * Check if facility contact for the group already exists.
	 * Throw exception info not.
	 *
	 * @param sess
	 * @param facility
	 * @param name
	 * @param group
	 * @throws InternalErrorException
	 * @throws FacilityContactNotExistsException
	 */
	void checkFacilityContactExists(PerunSession sess, Facility facility, String name, Group group) throws InternalErrorException, FacilityContactNotExistsException;

	/**
	 * Check if facility contact for the owner already exists.
	 * Throw exception info not.
	 *
	 * @param sess
	 * @param facility
	 * @param name
	 * @param owner
	 * @throws InternalErrorException
	 * @throws FacilityContactNotExistsException
	 */
	void checkFacilityContactExists(PerunSession sess, Facility facility, String name, Owner owner) throws InternalErrorException, FacilityContactNotExistsException;

	/**
	 * Check if security team is <b>not</b> assigned to facility.
	 * Throw exception info is.
	 *
	 * @param sess
	 * @param facility
	 * @param securityTeam
	 * @throws InternalErrorException
	 * @throws SecurityTeamAlreadyAssignedException
	 */
	void checkSecurityTeamNotAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws SecurityTeamAlreadyAssignedException, InternalErrorException;

	/**
	 * Check if security team is assigned to facility.
	 * Throw exception info not.
	 *
	 * @param sess
	 * @param facility
	 * @param securityTeam
	 * @throws InternalErrorException
	 * @throws SecurityTeamNotAssignedException
	 */
	void checkSecurityTeamAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws SecurityTeamNotAssignedException, InternalErrorException;

	/**
	 * Set ban for user on facility
	 *
	 * @param sess
	 * @param banOnFacility the ban
	 * @return ban on facility
	 * @throws InternalErrorException
	 * @throws BanAlreadyExistsException
	 *
	 */
	BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException, BanAlreadyExistsException;

	/**
	 * Get Ban for user on facility by it's id
	 *
	 * @param sess
	 * @param banId the ban id
	 * @return facility ban by it's id
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	BanOnFacility getBanById(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Get true if any ban for user and facility exists.
	 *
	 * @param sess
	 * @param userId id of user
	 * @param facilityId id of facility
	 * @return true if ban exists
	 * @throws InternalErrorException
	 */
	boolean banExists(PerunSession sess, int userId, int facilityId) throws InternalErrorException;

	/**
	 * Get true if any band defined by id exists for any user and facility.
	 *
	 * @param sess
	 * @param banId id of ban
	 * @return true if ban exists
	 * @throws InternalErrorException
	 */
	boolean banExists(PerunSession sess, int banId) throws InternalErrorException;

	/**
	 * Check if ban already exists.
	 *
	 * Throw exception if no.
	 *
	 * @param sess
	 * @param userId user id
	 * @param facilityId facility id
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void checkBanExists(PerunSession sess, int userId, int facilityId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Check if ban already exists.
	 *
	 * Throw exception if no.
	 *
	 * @param sess
	 * @param banId ban id
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void checkBanExists(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Get specific facility ban.
	 *
	 * @param sess
	 * @param userId the user id
	 * @param faclityId the facility id
	 * @return specific facility ban
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	BanOnFacility getBan(PerunSession sess, int userId, int faclityId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Get all facilities bans for user.
	 *
	 * @param sess
	 * @param userId the user id
	 * @return list of bans for user on any facility
	 * @throws InternalErrorException
	 */
	List<BanOnFacility> getBansForUser(PerunSession sess, int userId) throws InternalErrorException;

	/**
	 * Get all users bans for facility
	 *
	 * @param sess
	 * @param facilityId the facility id
	 * @return list of all users bans on facility
	 * @throws InternalErrorException
	 */
	List<BanOnFacility> getBansForFacility(PerunSession sess, int facilityId) throws InternalErrorException;

	/**
	 * Get all expired bans on any facility to now date
	 *
	 * @param sess
	 * @return list of expired bans for any facility
	 * @throws InternalErrorException
	 */
	List<BanOnFacility> getAllExpiredBansOnFacilities(PerunSession sess) throws InternalErrorException;

	/**
	 * Update description and validity timestamp of specific ban.
	 *
	 * @param sess
	 * @param banOnFacility ban to be updated
	 * @return updated ban
	 * @throws InternalErrorException
	 */
	BanOnFacility updateBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException;

	/**
	 * Remove ban by id from facilities bans.
	 *
	 * @param sess
	 * @param banId id of specific ban
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void removeBan(PerunSession sess, int banId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Remove ban by user_id and facility_id.
	 *
	 * @param sess
	 * @param userId the id of user
	 * @param facilityId the id of facility
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void removeBan(PerunSession sess, int userId, int facilityId) throws InternalErrorException, BanNotExistsException;

	/**
	 * Remove all expired bans on facilities to now date.
	 *
	 * Get all expired bans and remove them one by one with auditing process.
	 * This method is for purpose of removing expired bans using some cron tool.
	 *
	 * @param sess
	 * @throws InternalErrorException
	 */
	void removeAllExpiredBansOnFacilities(PerunSession sess) throws InternalErrorException;
}
