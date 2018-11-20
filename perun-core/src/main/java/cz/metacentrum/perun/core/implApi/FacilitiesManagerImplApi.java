package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.ContactGroup;
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
import cz.metacentrum.perun.core.api.exceptions.FacilityContactNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotAssignedException;

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
	Facility getFacilityById(PerunSession perunSession, int id) throws InternalErrorException, FacilityNotExistsException;

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
	Facility getFacilityByName(PerunSession perunSession, String name) throws InternalErrorException, FacilityNotExistsException;


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
	 */
	void setOwners(PerunSession perunSession, Facility facility, List<Owner> owners) throws InternalErrorException;

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
	void addOwner(PerunSession perunSession, Facility facility, Owner owner) throws InternalErrorException, OwnerAlreadyAssignedException;

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
	 * Return all VO which can use this facility. (VO must have the resource which belongs to this facility).
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
	List<Resource> getAssignedResources(PerunSession perunSession, Facility facility, Vo specificVo, Service specificService) throws InternalErrorException;

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
	List<RichResource> getAssignedRichResources(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Inserts facility into DB.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 */
	Facility createFacility(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Deletes facility by id.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 * @throws FacilityAlreadyRemovedException if there are 0 rows affected by delete from DB
	 */
	void deleteFacility(PerunSession perunSession, Facility facility) throws InternalErrorException, FacilityAlreadyRemovedException;

	/**
	 * Updates facility in DB.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 */
	Facility updateFacility(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Deletes all facility owners.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 */
	void deleteFacilityOwners(PerunSession perunSession, Facility facility) throws InternalErrorException;


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
	boolean facilityExists(PerunSession perunSession, Facility facility) throws InternalErrorException;

	/**
	 * Check if facility exists in underlaying data source.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 */
	void checkFacilityExists(PerunSession perunSession, Facility facility) throws InternalErrorException, FacilityNotExistsException;

	/**
	 * Returns list of all facilities owned by the owner.
	 *
	 * @param perunSession
	 * @param owner
	 * @return list of facilities owned by the owner
	 * @throws InternalErrorException
	 */
	List<Facility> getOwnerFacilities(PerunSession perunSession, Owner owner) throws InternalErrorException;

	/**
	 * Returns all facilities which have set the attribute with the value. Searching only def and opt attributes.
	 * Large attributes are not supported.
	 *
	 * @param sess
	 * @param attribute
	 * @return
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesByAttribute(PerunSession sess, Attribute attribute) throws InternalErrorException;

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
	List<Host> getHosts(PerunSession perunSession, Facility facility) throws InternalErrorException;

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
	int getHostsCount(PerunSession perunSession, Facility facility) throws InternalErrorException;

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
	 * @throws HostAlreadyRemovedException if 0 rows affected by deleting from DB
	 */
	void removeHost(PerunSession perunSession, Host host) throws InternalErrorException, HostAlreadyRemovedException;

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
	 *
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
	 * Gets list of all user administrators of the Facility.
	 * If some group is administrator of the given group, all members are included in the list.
	 *
	 * @param sess
	 * @param facility
	 * @return list of users who are admins in the facility
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
	 * @return list of groups who are admins in the facility
	 * @throws InternalErrorException
	 */
	List<Group> getAdminGroups(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Get all facilities where the user is admin.
	 * Including facilities, where the user is a member of authorized group.
	 *
	 * @param sess
	 * @param user
	 * @return list of facilities
	 * @throws InternalErrorException
	 */
	List<Facility> getFacilitiesWhereUserIsAdmin(PerunSession sess, User user) throws InternalErrorException;

	public boolean hostExists(PerunSession sess, Host host) throws InternalErrorException;

	public void checkHostExists(PerunSession sess, Host host) throws InternalErrorException, HostNotExistsException;

	/**
	 * Return all users assigned to Facility.
	 *
	 * @param sess
	 * @param facility
	 * @return list of user
	 * @throws InternalErrorException
	 */
	List<User> getAssignedUsers(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Returns list of Users assigned with chosen Facility containing resources where service is assigned.
	 *
	 * @param sess
	 * @param facility
	 * @param service
	 * @return list of Users
	 * @throws InternalErrorException
	 */
	List<User> getAssignedUsers(PerunSession sess, Facility facility, Service service)throws InternalErrorException;

	// FACILITY CONTACTS METHODS

	/**
	 * Create new user contact for facility with contactGroupName
	 *
	 * @param sess
	 * @param facility
	 * @param contactGroupName
	 * @param user
	 * @return contactGroup
	 * @throws InternalErrorException
	 */
	ContactGroup addFacilityContact(PerunSession sess, Facility facility, String contactGroupName, User user) throws InternalErrorException;

	/**
	 * Create new owner contact for facility with contactGroupName
	 *
	 * @param sess
	 * @param facility
	 * @param contactGroupName
	 * @param owner
	 * @return contactGroup
	 * @throws InternalErrorException
	 */
	ContactGroup addFacilityContact(PerunSession sess, Facility facility, String contactGroupName, Owner owner) throws InternalErrorException;

	/**
	 * Create new group contact for facility with contactGroupName
	 *
	 * @param sess
	 * @param facility
	 * @param contactGroupName
	 * @param group
	 * @return contactGroup
	 * @throws InternalErrorException
	 */
	ContactGroup addFacilityContact(PerunSession sess, Facility facility, String contactGroupName, Group group) throws InternalErrorException;

	/**
	 * Get list of contact groups for the owner.
	 *
	 * @param sess
	 * @param owner
	 * @return list of contactGroups
	 * @throws InternalErrorException
	 */
	List<ContactGroup> getFacilityContactGroups(PerunSession sess, Owner owner) throws InternalErrorException;

	/**
	 * Get list of contact groups for the user.
	 *
	 * @param sess
	 * @param user
	 * @return list of contactGroups
	 * @throws InternalErrorException
	 */
	List<ContactGroup> getFacilityContactGroups(PerunSession sess, User user) throws InternalErrorException;

	/**
	 * Get list of contact groups for the group.
	 *
	 * @param sess
	 * @param group
	 * @return list of contactGroups
	 * @throws InternalErrorException
	 */
	List<ContactGroup> getFacilityContactGroups(PerunSession sess, Group group) throws InternalErrorException;

	/**
	 * Get list of contact groups for the facility.
	 *
	 * @param sess
	 * @param facility
	 * @return list of contactGroups
	 * @throws InternalErrorException
	 */
	List<ContactGroup> getFacilityContactGroups(PerunSession sess, Facility facility) throws InternalErrorException;

	/**
	 * Get contact group for the facility and the contact group name.
	 *
	 * @param sess
	 * @param facility
	 * @param contactGroupName
	 * @return contactGroup
	 * @throws InternalErrorException
	 * @throws FacilityContactNotExistsException if there is no such contact
	 */
	ContactGroup getFacilityContactGroup(PerunSession sess, Facility facility, String contactGroupName) throws InternalErrorException, FacilityContactNotExistsException;

	/**
	 * Get list of unique contact group names.
	 *
	 * @param sess
	 * @return
	 * @throws InternalErrorException
	 */
	List<String> getAllContactGroupNames(PerunSession sess) throws InternalErrorException;

	/**
	 * Check if contact for facility, contact name and owner exists.
	 *
	 * @param sess
	 * @param facility
	 * @param contactGroupName
	 * @param owner
	 * @throws InternalErrorException
	 * @throws FacilityContactNotExistsException if there is no such contact
	 */
	void checkFacilityContactExists(PerunSession sess, Facility facility, String contactGroupName, Owner owner) throws InternalErrorException, FacilityContactNotExistsException;

	/**
	 * Check if contact for facility, contact name and user exists.
	 *
	 * @param sess
	 * @param facility
	 * @param contactGroupName
	 * @param user
	 * @throws InternalErrorException
	 * @throws FacilityContactNotExistsException if there is no such contact
	 */
	void checkFacilityContactExists(PerunSession sess, Facility facility, String contactGroupName, User user) throws InternalErrorException, FacilityContactNotExistsException;

	/**
	 * Check if contact for facility, contact name and group exists.
	 *
	 * @param sess
	 * @param facility
	 * @param contactGroupName
	 * @param group
	 * @throws InternalErrorException
	 * @throws FacilityContactNotExistsException if there is no such contact
	 */
	void checkFacilityContactExists(PerunSession sess, Facility facility, String contactGroupName, Group group) throws InternalErrorException, FacilityContactNotExistsException;

	/**
	 * Remove owner contact for facility and contact name.
	 *
	 * @param sess
	 * @param contactGroupName
	 * @param facility
	 * @param owner
	 * @throws InternalErrorException
	 */
	void removeFacilityContact(PerunSession sess, Facility facility, String contactGroupName, Owner owner) throws InternalErrorException;

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
	 * Remove user contact for facility and contact name.
	 *
	 * @param sess
	 * @param contactGroupName
	 * @param facility
	 * @param user
	 * @throws InternalErrorException
	 */
	void removeFacilityContact(PerunSession sess, Facility facility, String contactGroupName, User user) throws InternalErrorException;

	/**
	 * Remove group contact for facility and contact name.
	 *
	 * @param sess
	 * @param contactGroupName
	 * @param facility
	 * @param group
	 * @throws InternalErrorException
	 */
	void removeFacilityContact(PerunSession sess, Facility facility, String contactGroupName, Group group) throws InternalErrorException;


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
	 * Check if security team is <b>not</b> assigned to facility.
	 * Throw exception info is.
	 *
	 * @param sess
	 * @param facility
	 * @param securityTeam
	 * @throws InternalErrorException
	 * @throws SecurityTeamAlreadyAssignedException
	 */
	void checkSecurityTeamNotAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamAlreadyAssignedException;

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
	void checkSecurityTeamAssigned(PerunSession sess, Facility facility, SecurityTeam securityTeam) throws InternalErrorException, SecurityTeamNotAssignedException;

	/**
	 * Get facilities where security team is assigned.
	 *
	 * @param sess
	 * @param securityTeam
	 * @return
	 *
	 * @throws InternalErrorException
	 */
	List<Facility> getAssignedFacilities(PerunSession sess, SecurityTeam securityTeam) throws InternalErrorException;

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
	 * Set ban for user on facility
	 * 
	 * @param sess
	 * @param banOnFacility the ban
	 * @return ban on facility
	 * @throws InternalErrorException 
	 */
	BanOnFacility setBan(PerunSession sess, BanOnFacility banOnFacility) throws InternalErrorException;

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
	 * Remove all service denials on given facility.
	 *
	 * WARNING: this method should be removed in the future if
	 * the tasks module is merged into core module.
	 *
	 * @param facilityId facility id
	 * @throws InternalErrorException when db operation fails
	 */
	void removeAllServiceDenials(int facilityId) throws InternalErrorException;
}