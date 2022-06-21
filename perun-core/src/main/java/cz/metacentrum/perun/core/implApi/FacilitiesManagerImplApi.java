package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotAssignedException;

import java.util.List;

/**
 * Facility manager can create a new facility or find an existing facility.
 *
 * <p>You must get an instance of FacilityManager from PerunSession:</p>
 * <pre>
 *    PerunSession perunSession = ...;
 *    FacilityManager gm = perunSession.getPerun().getFacilityManager();
 * </pre>
 *
 * @author Michal Prochazka
 * @author Slavek Licehammer
 */
public interface FacilitiesManagerImplApi {

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
	 * @throws FacilityNotExistsException
	 * @throws InternalErrorException
	 */
	Facility getFacilityByName(PerunSession perunSession, String name) throws FacilityNotExistsException;

	/**
	 * Gets facilities by their ids. Silently skips non-existing facilities.
	 *
	 * @param perunSession
	 * @param ids
	 *
	 * @return list of facilities with specified ids
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByIds(PerunSession perunSession, List<Integer> ids);

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
	 */
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
	 * Return all VO which can use this facility. (VO must have the resource which belongs to this facility).
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
	 * Return all members, which are "allowed" on facility through any resource disregarding
	 * their possible expired status in a group. All members include all group statuses, through which they can
	 * be filtered if necessary.
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
	 * Return all users, which are "allowed" on facility through any member/resource.
	 *
	 * @param sess
	 * @param facility
	 * @return list of allowed users
	 */
	List<User> getAllowedUsers(PerunSession sess, Facility facility);

	/**
	 * Return all users, which are associated with facility through any member/resource.
	 * Does not require ACTIVE group-resource status.
	 *
	 * @param sess
	 * @param facility
	 * @return list of allowed users
	 */
	List<User> getAssociatedUsers(PerunSession sess, Facility facility);

	/**
	 * Return all allowed facilities of the user.
	 * It means all facilities, where is assigned through some resource and member is allowed on such resource.
	 *
	 * @param sess
	 * @param user
	 * @return List of allowed facilities of the user.
	 */
	List<Facility> getAllowedFacilities(PerunSession sess, User user);

	/**
	 * Return all allowed facilities of the member.
	 * It means all facilities, where is assigned through some resource and member is allowed.
	 *
	 * @param sess
	 * @param member
	 * @return List of allowed facilities of the member.
	 */
	List<Facility> getAllowedFacilities(PerunSession sess, Member member);

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
	 * Returns all resources assigned to the facility with optionally VO and Service specified.
	 *
	 * @param perunSession
	 * @param facility
	 * @param specificVo
	 * @param specificService
	 *
	 * @return list of resources assigned to the facility with optionally filter for VO and Service.
	 *
	 * @throws InternalErrorException
	 */
	List<Resource> getAssignedResources(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService);

	/**
	 * Returns all rich resources assigned to the facility.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return list of rich resources assigned to the facility
	 *
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility);

	/**
	 * Returns all rich resources assigned to the facility and service.
	 *
	 * @param perunSession
	 * @param facility
	 * @param service
	 *
	 * @return list of rich resources assigned to the facility and service
	 *
	 * @throws InternalErrorException
	 */
	List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility, Service service);

	/**
	 * Inserts facility into DB.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 */
	Facility createFacility(PerunSession perunSession, Facility facility);

	/**
	 * Deletes facility by id.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 * @throws FacilityAlreadyRemovedException if there are 0 rows affected by delete from DB
	 */
	void deleteFacility(PerunSession perunSession, Facility facility) throws FacilityAlreadyRemovedException;

	/**
	 * Updates facility in DB.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 * @throws FacilityExistsException if the name of facility has been already used for different Facility
	 */
	Facility updateFacility(PerunSession perunSession, Facility facility) throws FacilityExistsException;

	/**
	 * Deletes all facility owners.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 */
	void deleteFacilityOwners(PerunSession perunSession, Facility facility);


	/**
	 * Check if facility exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return true if facility exists in underlaying data source, false otherwise
	 *
	 * @throws InternalErrorException
	 */
	boolean facilityExists(PerunSession perunSession, Facility facility);

	/**
	 * Check if facility exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 */
	void checkFacilityExists(PerunSession perunSession, Facility facility) throws FacilityNotExistsException;

	/**
	 * Returns list of all facilities owned by the owner.
	 *
	 * @param perunSession
	 * @param owner
	 * @return list of facilities owned by the owner
	 * @throws InternalErrorException
	 */
	List<Facility> getOwnerFacilities(PerunSession perunSession, Owner owner);

	/**
	 * Returns all facilities which have set the attribute with the value. Searching only def and opt attributes.
	 *
	 * @param sess
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByAttribute(PerunSession sess, Attribute attribute);

	/**
	 * List hosts from facility.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return hosts Hosts' id from the Cluster
	 *
	 * @throws InternalErrorException
	 */
	List<Host> getHosts(PerunSession perunSession, Facility facility);

	/**
	 * Count hosts in the facility.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return int The number of hosts present in the Cluster.
	 *
	 * @throws InternalErrorException
	 */
	int getHostsCount(PerunSession perunSession, Facility facility);

	/**
	 * Adds host to the Facility.
	 *
	 * @param perunSession
	 * @param host
	 * @param facility
	 *
	 * return host
	 *
	 */
	Host addHost(PerunSession perunSession, Host host, Facility facility);

	/**
	 * Remove hosts from the Facility.
	 *
	 * @param perunSession
	 * @param host
	 *
	 * @throws InternalErrorException
	 * @throws HostAlreadyRemovedException if 0 rows affected by deleting from DB
	 */
	void removeHost(PerunSession perunSession, Host host) throws HostAlreadyRemovedException;

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
	 *
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
	 * Gets list of all user administrators of the Facility.
	 * If some group is administrator of the given group, all VALID members are included in the list.
	 *
	 * @param sess
	 * @param facility
	 * @return list of users who are admins in the facility
	 * @throws InternalErrorException
	 */
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
	List<User> getDirectAdmins(PerunSession perunSession, Facility facility);

	/**
	 * Gets list of all group administrators of the Facility.
	 *
	 * @param sess
	 * @param facility
	 * @return list of groups who are admins in the facility
	 * @throws InternalErrorException
	 */
	List<Group> getAdminGroups(PerunSession sess, Facility facility);

	/**
	 * Get all facilities where the user is admin.
	 * Including facilities, where the user is a VALID member of authorized group.
	 *
	 * @param sess
	 * @param user
	 * @return list of facilities
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user);

	boolean hostExists(PerunSession sess, Host host);

	void checkHostExists(PerunSession sess, Host host) throws HostNotExistsException;

	/**
	 * Return all users assigned to Facility.
	 *
	 * @param sess
	 * @param facility
	 * @return list of user
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
	 * Get facilities where security team is assigned.
	 *
	 * @param sess
	 * @param securityTeam
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getAssignedFacilities(PerunSession sess, SecurityTeam securityTeam);

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
	 * Set ban for user on facility
	 *
	 * @param sess
	 * @param banOnFacility the ban
	 * @return ban on facility
	 * @throws InternalErrorException
	 */
	BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility);

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
	 * Remove all service denials on given facility.
	 *
	 * WARNING: this method should be removed in the future if
	 * the tasks module is merged into core module.
	 *
	 * @param facilityId facility id
	 * @throws InternalErrorException when db operation fails
	 */
	void removeAllServiceDenials(int facilityId);
}
