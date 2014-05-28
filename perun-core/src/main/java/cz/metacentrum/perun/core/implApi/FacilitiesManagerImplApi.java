package cz.metacentrum.perun.core.implApi;

import java.util.List;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.FacilityAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.HostAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;

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
	 * Returns owners' id of the facility.
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return owners' id of specified facility
	 *
	 * @throws InternalErrorException
	 */
	List<Integer> getOwnersIds(PerunSession perunSession, Facility facility) throws InternalErrorException;

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
	 * Return all VO' id which can use this facility. (VO must have the resource which belongs to this facility).
	 *
	 * @param perunSession
	 * @param facility
	 *
	 * @return list of Vos' id
	 *
	 * @throws InternalErrorException
	 */
	List<Integer> getAllowedVosIds(PerunSession perunSession, Facility facility) throws InternalErrorException;

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
}
