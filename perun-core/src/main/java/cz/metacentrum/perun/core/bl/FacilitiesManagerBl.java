package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.EnrichedFacility;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichFacility;
import cz.metacentrum.perun.core.api.RichGroup;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.BanAlreadyExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsentHubExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupAlreadyRemovedFromResourceException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
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
	Facility getFacilityById(PerunSession perunSession, int id) throws FacilityNotExistsException;

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
	Facility getFacilityByName(PerunSession perunSession, String name) throws FacilityNotExistsException;

	/**
	 * Searches for the Facilities with specified ids.
	 *
	 * @param perunSession
	 * @param ids
	 *
	 * @return list of Facilities with specified ids
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByIds(PerunSession perunSession, List<Integer> ids);

	/**
	 * Get all rich Facilities with all their owners.
	 *
	 * @param perunSession
	 * @return list of RichFacilities with owners
	 * @throws InternalErrorException
	 */
	List<RichFacility> getRichFacilities(PerunSession perunSession);

	/**
	 * Get all RichFacilities with all their owners from list of Facilities.
	 *
	 * @param perunSession
	 * @param facilities list of facilities
	 * @return list of RichFacilities with owners
	 * @throws InternalErrorException
	 */
	List<RichFacility> getRichFacilities(PerunSession perunSession, List<Facility> facilities);

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
	List<Facility> getFacilitiesByDestination(PerunSession perunSession, String destination) throws FacilityNotExistsException;

	/**
	 * List all facilities.
	 *
	 * @param perunSession
	 *
	 * @return List of all Facilities within the Perun
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilities(PerunSession perunSession);

	/**
	 * Get count of all facilities.
	 *
	 * @param perunSession
	 *
	 * @return count of all facilities
	 *
	 * @throws InternalErrorException
	 */
	int getFacilitiesCount(PerunSession perunSession);

	/**
	 * Gets all enriched facilities.
	 *
	 * @param perunSession
	 *
	 * @return List of all EnrichedFacilities
	 *
	 * @throws InternalErrorException
	 */
	List<EnrichedFacility> getEnrichedFacilities(PerunSession perunSession);

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
	List<Owner> getOwners(PerunSession perunSession, Facility facility);

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
	void setOwners(PerunSession perunSession, Facility facility, List<Owner> owners);

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
	void addOwner(PerunSession perunSession, Facility facility, Owner owner) throws OwnerAlreadyAssignedException;

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
	void removeOwner(PerunSession perunSession, Facility facility, Owner owner) throws OwnerAlreadyRemovedException;

	/**
	 * Copy all owners of the source facility to the destination facility.
	 * The owners, that are in the destination facility and aren't in the source facility, are retained.
	 * The common owners are replaced with owners from source facility.
	 *
	 * @param sourceFacility
	 * @param destinationFacility
	 * @throws InternalErrorException
	 */
	void copyOwners(PerunSession sess, Facility sourceFacility, Facility destinationFacility);


	/**
	 * Return all VO which can use this facility. (VO must have the resource which belongs to this facility)
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return list of Vos
	 *
	 * @throws InternalErrorException
	 */
	List<Vo> getAllowedVos(PerunSession perunSession, Facility facility);

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
	List<Group> getAllowedGroups(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService);

	/**
	 * Get all RichGroups which can use this facility (Groups must be assigned to Resource which belongs to this facility)
	 * specificVo and specificService can choose concrete groups
	 * if specificVo, specificService or both are null, they do not specific (all possible results are returned)
	 * We also retrieve attributes specified by attrNames for each returned RichGroup.
	 *
	 * @param facility        searching for this facility
	 * @param specificVo      specific only those results which are in specific VO (with null, all results)
	 * @param specificService specific only those results, which have resource with assigned specific service (if null, all results)
	 * @param attrNames       with each returned RichGroup we get also attributes specified by this list
	 * @return list of allowed groups
	 * @throws InternalErrorException when implementation fails
	 */
	List<RichGroup> getAllowedRichGroupsWithAttributes(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService, List<String> attrNames);

	/**
	 * Return all users who can use this facility
	 *
	 * @param sess
	 * @param facility
	 * @return list of users
	 */
	List<User> getAllowedUsers(PerunSession sess, Facility facility);

	/**
	 * Return all users, which are associated with facility through any member/resource.
	 * Does not require ACTIVE group-resource status.
	 *
	 * @param sess
	 * @param facility
	 * @return list of associated users
	 */
	List<User> getAssociatedUsers(PerunSession sess, Facility facility);

	/**
	 * Return all users who can use this facility
	 * You can specify VO or Service you are interested in to filter resulting users (they must be members of VO and from Resource with assigned Service).
	 * if specificVo, specificService or both are null, they do not specific (all possible results are returned)
	 *
	 * @param sess
	 * @param facility
	 * @param specificVo specific only those results which are in specific VO (with null, all results)
	 * @param specificService specific only those results, which have resource with assigned specific service (if null, all results)
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getAllowedUsers(PerunSession sess, Facility facility, Vo specificVo, Service specificService);

	/**
	 * Return all users who can use this facility and who are not expired in any of groups associated with any resource
	 * You can specify VO or Service you are interested in to filter resulting users (they must be members of VO and from Resource with assigned Service).
	 * if specificVo, specificService or both are null, they do not specific (all possible results are returned)
	 *
	 * @param sess
	 * @param facility
	 * @param specificVo specific only those results which are in specific VO (with null, all results)
	 * @param specificService specific only those results, which have resource with assigned specific service (if null, all results)
	 * @return list of users
	 * @throws InternalErrorException
	 */
	List<User> getAllowedUsersNotExpiredInGroups(PerunSession sess, Facility facility, Vo specificVo, Service specificService);

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
	List<Member> getAllowedMembers(PerunSession sess, Facility facility);

	/**
	 * Return all members, which are "allowed" on facility through any resource assigned to the given service disregarding
	 * their possible expired status in a group. All members include all group statuses, through which they can
	 * be filtered if necessary.
	 *
	 * @param sess
	 * @param facility
	 * @param service
	 *
	 * @return list of allowed members
	 */
	List<Member> getAllowedMembers(PerunSession sess, Facility facility, Service service);

	/**
	 * Return all members, which are "allowed" on facility through any resource assigned to the given service
	 * and have ACTIVE status in a group.
	 *
	 * @param sess
	 * @param facility
	 * @param service
	 *
	 * @return list of allowed members
	 */
	List<Member> getAllowedMembersNotExpiredInGroups(PerunSession sess, Facility facility, Service service);

	/**
	 * Return all members, which are associated with the facility and belong to given user.
	 * Does not require ACTIVE group-resource status or any specific member status.
	 *
	 * @param sess
	 * @param facility
	 * @param user
	 *
	 * @return list of associated members
	 *
	 * @throws InternalErrorException
	 */
	List<Member> getAssociatedMembers(PerunSession sess, Facility facility, User user);

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
	List<Resource> getAssignedResources(PerunSession perunSession, Facility facility);

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
	List<Resource> getAssignedResources(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService);

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
	List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility);

	/**
	 * Returns all rich resources assigned to the facility and service with VO property filled
	 *
	 * @param perunSession
	 * @param facility
	 * @param service
	 *
	 * @return list of resources assigned to the facility and service
	 *
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility, Service service);

	/**
	 * Store the facility.
	 *
	 * @param perunSession
	 * @param facility
	 * @return
	 * @throws InternalErrorException
	 * @throws FacilityExistsException
	 * @throws ConsentHubExistsException
	 */
	Facility createFacility(PerunSession perunSession, Facility facility) throws FacilityExistsException, ConsentHubExistsException;

	/**
	 * Delete the facility by id.
	 *
	 * @param perunSession
	 * @param facility
	 * @param force
	 *
	 * @throws InternalErrorException
	 * @throws RelationExistsException there are still some resources assigned to this facility
	 * @throws FacilityAlreadyRemovedException there are 0 rows affected by delete from DB
	 * @throws HostAlreadyRemovedException if there is at least 1 host who was not affected by deleting from DB
	 * @throws ResourceAlreadyRemovedException if there are at least 1 resource not affected by deleting from DB
	 * @throws GroupAlreadyRemovedFromResourceException there are at least 1 group on resource not affected by deleting from DB
	 */
	void deleteFacility(PerunSession perunSession, Facility facility, Boolean force) throws RelationExistsException, FacilityAlreadyRemovedException, HostAlreadyRemovedException, ResourceAlreadyRemovedException, GroupAlreadyRemovedFromResourceException;

	/**
	 * Updates facility.
	 *
	 * @param perunSession
	 * @param facility to update
	 *
	 * @return updated facility
	 *
	 * @throws InternalErrorException
	 * @throws FacilityExistsException
	 */
	Facility updateFacility(PerunSession perunSession, Facility facility) throws FacilityExistsException;

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
	List<Facility> getOwnerFacilities(PerunSession perunSession, Owner owner);

	/**
	 * Get facilities which are assigned to Group (via resource).
	 *
	 * @param sess
	 * @param group
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getAssignedFacilities(PerunSession sess, Group group);

	/**
	 * Get facilities which have the member access on.
	 *
	 * @param sess
	 * @param member
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getAssignedFacilities(PerunSession sess, Member member);

	/**
	 * Get facilities where the user is assigned.
	 *
	 * @param sess
	 * @param user
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getAssignedFacilities(PerunSession sess, User user);

	/**
	 * Get facilities where the user is allowed.
	 *
	 * @param sess
	 * @param user
	 * @return List of allowed facilities of the user.
	 */
	List<Facility> getAllowedFacilities(PerunSession sess, User user);

	/**
	 * Get facilities where member is allowed.
	 *
	 * @param sess
	 * @param member
	 * @return List of allowed facilities of the member.
	 */
	List<Facility> getAllowedFacilities(PerunSession sess, Member member);

	/**
	 * Get facilities where the services is defined.
	 *
	 * @param sess
	 * @param service
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getAssignedFacilities(PerunSession sess, Service service);

	/**
	 * Get facilities where the security team is assigned
	 *
	 * @param sess
	 * @param securityTeam
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getAssignedFacilities(PerunSession sess, SecurityTeam securityTeam);

	/**
	 * Returns all facilities that have set the attribute 'attributeName' with the value 'attributeValue'.
	 * Searching only def and opt attributes. Large attributes are not supported.
	 *
	 * @param sess perun session
	 * @param attributeName attribute name to be searched by
	 * @param attributeValue attribute value to be searched by
	 * @return
	 * @throws InternalErrorException
	 * @throws WrongAttributeAssignmentException
	 */
	List<Facility> getFacilitiesByAttribute(PerunSession sess, String attributeName, String attributeValue) throws WrongAttributeAssignmentException;

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
	List<Host> getHosts(PerunSession sess, Facility facility);

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
	int getHostsCount(PerunSession sess, Facility facility);

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
	 * @throws HostExistsException
	 */
	List<Host> addHosts(PerunSession sess, List<Host> hosts, Facility facility) throws HostExistsException;

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
	 * @throws HostExistsException
	 * @throws WrongPatternException when syntax of any of the hostnames is wrong
	 */
	List<Host> addHosts(PerunSession sess, Facility facility, List<String> hosts) throws HostExistsException, WrongPatternException;

	/**
	 * Adds host to the Facility.
	 *
	 * @param perunSession
	 * @param host
	 * @param facility
	 *
	 * return host
	 */
	Host addHost(PerunSession perunSession, Host host, Facility facility);

	/**
	 * Remove hosts from the Facility.
	 *
	 * @param perunSession
	 * @param host
	 * @param facility
	 *
	 * @throws InternalErrorException
	 * @throws HostAlreadyRemovedException if 0 rows was affected by deleting from DB
	 */
	void removeHost(PerunSession perunSession, Host host, Facility facility) throws HostAlreadyRemovedException;

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
	void removeHosts(PerunSession sess, List<Host> hosts, Facility facility) throws HostAlreadyRemovedException;

	/**
	 * Get the host by its ID.
	 *
	 * @param sess
	 * @param id
	 * @return host
	 * @throws HostNotExistsException
	 * @throws InternalErrorException
	 */
	Host getHostById(PerunSession sess, int id) throws HostNotExistsException;

	/**
	 * Get all hosts with this hostname (from all facilities).
	 *
	 *
	 * @param sess
	 * @param hostname
	 * @return list of hosts by hostname
	 * @throws InternalErrorException
	 */
	List<Host> getHostsByHostname(PerunSession sess, String hostname);

	/**
	 * Return facility which has the host.
	 *
	 * @param sess
	 * @param host
	 * @return facility
	 * @throws InternalErrorException
	 */
	Facility getFacilityForHost(PerunSession sess, Host host);

	/**
	 * Return all facilities where exists host with the specific hostname
	 *
	 * @param sess
	 * @param hostname specific hostname
	 * @return
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByHostName(PerunSession sess, String hostname);

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
	 * Gets list of all user administrators of the Facility.
	 * If some group is administrator of the given group, all VALID members are included in the list.
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
	List<User> getAdmins(PerunSession perunSession, Facility facility, boolean onlyDirectAdmins);

	/**
	 * Gets list of all richUser administrators of the Facility.
	 * If some group is administrator of the given group, all VALID members are included in the list.
	 *
	 * Supported roles: FacilityAdmin
	 *
	 * If "onlyDirectAdmins" is "true", return only direct users of the group for supported role with specific attributes.
	 * If "allUserAttributes" is "true", do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param perunSession
	 * @param facility
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
	List<RichUser> getRichAdmins(PerunSession perunSession, Facility facility, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws UserNotExistsException;

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
	List<User> getAdmins(PerunSession sess, Facility facility);

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
	List<User> getDirectAdmins(PerunSession perunSession, Facility facility);

	/**
	 * Gets list of all group administrators of the Facility.
	 *
	 * @param sess
	 * @param facility
	 * @return list of Groups that are admins in the facility
	 * @throws InternalErrorException
	 */
	List<Group> getAdminGroups(PerunSession sess, Facility facility);

	/**
	 * Get all Facility admins without attributes.
	 *
	 * @param sess
	 * @param facility
	 * @return return list of RichUsers without attributes.
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getRichAdmins(PerunSession sess, Facility facility);

	/**
	 * Get all Facility admins, which are assigned directly (not by group membership) without attributes.
	 *
	 * @param sess
	 * @param facility
	 * @return return list of RichUsers without attributes.
	 * @throws InternalErrorException
	 */
	@Deprecated
	List<RichUser> getDirectRichAdmins(PerunSession sess, Facility facility);

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
	List<RichUser> getRichAdminsWithAttributes(PerunSession sess, Facility facility) throws UserNotExistsException;

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
	List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes);

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
	List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Facility facility, List<String> specificAttributes);

	/**
	 * Get all facilities where the user is admin.
	 * Including facilities, where the user is a VALID member of authorized group.
	 *
	 * @param sess
	 * @param user
	 * @return list of Facilities, where the user is an admin.
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user);

	/**
	 * Returns list of facilities connected with a group
	 *
	 * @param sess
	 * @param group
	 * @return list of facilities connected with group
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByPerunBean(PerunSession sess, Group group);

	/**
	 * Returns list of facilities connected with a member
	 *
	 * @param sess
	 * @param member
	 * @return list of facilities connected with member
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByPerunBean(PerunSession sess, Member member);

	/**
	 * Returns list of facilities connected with a resource
	 *
	 * @param sess
	 * @param resource
	 * @return list of facilities connected with resource
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByPerunBean(PerunSession sess, Resource resource);

	/**
	 * Returns list of facilities connected with a user
	 *
	 * @param sess
	 * @param user
	 * @return list of facilities connected with user
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByPerunBean(PerunSession sess, User user);

	/**
	 * Returns list of facilities connected with a host
	 *
	 * @param sess
	 * @param host
	 * @return list of facilities connected with host
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByPerunBean(PerunSession sess, Host host);

	/**
	 * Returns list of facilities connected with a vo
	 *
	 * @param sess
	 * @param vo
	 * @return list of facilities connected with vo
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByPerunBean(PerunSession sess, Vo vo);

	void checkFacilityExists(PerunSession sess, Facility facility) throws FacilityNotExistsException;

	void checkHostExists(PerunSession sess, Host host) throws HostNotExistsException;

	/**
	 * Returns list of Users assigned to chosen Facility.
	 *
	 * @param sess
	 * @param facility
	 * @return list of Users
	 * @throws InternalErrorException
	 */

	List<User> getAssignedUsers(PerunSession sess, Facility facility);

	/**
	 * Returns list of Users assigned with chosen Facility containing resources where service is assigned.
	 *
	 * @param sess
	 * @param facility
	 * @param service
	 * @return list of Users
	 * @throws InternalErrorException
	 */

	List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service);

	/**
	 * Copy all managers(admins) of the source facility to the destination facility.
	 * The admins, that are in the destination facility and aren't in the source facility, are retained.
	 * The common admins are also retained in destination facility.
	 *
	 * @param sess
	 * @param sourceFacility
	 * @param destinationFacility
	 * @throws InternalErrorException
	 */
	void copyManagers(PerunSession sess, Facility sourceFacility, Facility destinationFacility);

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
	void copyAttributes(PerunSession sess, Facility sourceFacility, Facility destinationFacility) throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

	/**
	 * Return all security teams which specific facility trusts
	 *
	 * @param sess
	 * @param facility specific facility
	 * @return list of assigned security teams
	 * @throws InternalErrorException
	 */
	List<SecurityTeam> getAssignedSecurityTeams(PerunSession sess, Facility facility);

	/**
	 * Assign given security team to given facility (means the facility trusts the security team)
	 *
	 * @param sess
	 * @param facility
	 * @param securityTeam
	 * @throws InternalErrorException
	 */
	void assignSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam);

	/**
	 * Remove (Unassign) given security team from given facility
	 *
	 * @param sess
	 * @param facility
	 * @param securityTeam
	 * @throws InternalErrorException
	 */
	void removeSecurityTeam(PerunSession sess, Facility facility, SecurityTeam securityTeam);

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
	void checkSecurityTeamNotAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws SecurityTeamAlreadyAssignedException;

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
	void checkSecurityTeamAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws SecurityTeamNotAssignedException;

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
	BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility) throws BanAlreadyExistsException;

	/**
	 * Get Ban for user on facility by it's id
	 *
	 * @param sess
	 * @param banId the ban id
	 * @return facility ban by it's id
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	BanOnFacility getBanById(PerunSession sess, int banId) throws BanNotExistsException;

	/**
	 * Get true if any ban for user and facility exists.
	 *
	 * @param sess
	 * @param userId id of user
	 * @param facilityId id of facility
	 * @return true if ban exists
	 * @throws InternalErrorException
	 */
	boolean banExists(PerunSession sess, int userId, int facilityId);

	/**
	 * Get true if any band defined by id exists for any user and facility.
	 *
	 * @param sess
	 * @param banId id of ban
	 * @return true if ban exists
	 * @throws InternalErrorException
	 */
	boolean banExists(PerunSession sess, int banId);

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
	void checkBanExists(PerunSession sess, int userId, int facilityId) throws BanNotExistsException;

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
	void checkBanExists(PerunSession sess, int banId) throws BanNotExistsException;

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
	BanOnFacility getBan(PerunSession sess, int userId, int faclityId) throws BanNotExistsException;

	/**
	 * Get all facilities bans for user.
	 *
	 * @param sess
	 * @param userId the user id
	 * @return list of bans for user on any facility
	 * @throws InternalErrorException
	 */
	List<BanOnFacility> getBansForUser(PerunSession sess, int userId);

	/**
	 * Get all users bans for facility
	 *
	 * @param sess
	 * @param facilityId the facility id
	 * @return list of all users bans on facility
	 * @throws InternalErrorException
	 */
	List<BanOnFacility> getBansForFacility(PerunSession sess, int facilityId);

	/**
	 * Get all expired bans on any facility to now date
	 *
	 * @param sess
	 * @return list of expired bans for any facility
	 * @throws InternalErrorException
	 */
	List<BanOnFacility> getAllExpiredBansOnFacilities(PerunSession sess);

	/**
	 * Update description and validity timestamp of specific ban.
	 *
	 * @param sess
	 * @param banOnFacility ban to be updated
	 * @return updated ban
	 * @throws InternalErrorException
	 */
	BanOnFacility updateBan(PerunSession sess, BanOnFacility banOnFacility);

	/**
	 * Remove ban by id from facilities bans.
	 *
	 * @param sess
	 * @param banId id of specific ban
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void removeBan(PerunSession sess, int banId) throws BanNotExistsException;

	/**
	 * Remove ban by user_id and facility_id.
	 *
	 * @param sess
	 * @param userId the id of user
	 * @param facilityId the id of facility
	 * @throws InternalErrorException
	 * @throws BanNotExistsException
	 */
	void removeBan(PerunSession sess, int userId, int facilityId) throws BanNotExistsException;

	/**
	 * Remove all expired bans on facilities to now date.
	 *
	 * Get all expired bans and remove them one by one with auditing process.
	 * This method is for purpose of removing expired bans using some cron tool.
	 *
	 * @param sess
	 * @throws InternalErrorException
	 */
	void removeAllExpiredBansOnFacilities(PerunSession sess);
}
