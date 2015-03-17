package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum FacilitiesManagerMethod implements ManagerMethod {

	/*#
	 * Searches for the Facility with specified id.
	 *
	 * @param id int Facility ID
	 * @return Facility Found facility
	 */
	getFacilityById {

		@Override
		public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilityById(parms.readInt("id"));
		}
	},

	/*#
	 * Searches the Facility by its name.
	 *
	 * @param name String Facility name
	 * @return Facility Found facility
	 */
	getFacilityByName {

		@Override
		public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilityByName(parms.readString("name"));
		}
	},

	/*#
	 * Lists all users assigned to facility containing resources where service is assigned.
	 *
	 * @param service int Service ID
	 * @param facility int Facility ID
	 * @return List<User> assigned users
	 */
	/*#
	 * Lists all users assigned to facility.
	 *
	 * @param facility int Facility ID
	 * @return List<User> assigned users
	 */
	getAssignedUsers {

		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("service"))
				return ac.getFacilitiesManager().getAssignedUsers(ac.getSession(),ac.getFacilityById(parms.readInt("facility")),ac.getServiceById(parms.readInt("service")));
			else
				return ac.getFacilitiesManager().getAssignedUsers(ac.getSession(),ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Gets all possible rich facilities with all their owners.
	 *
	 * @return List<RichFacility> rich facilities
	 */
	getRichFacilities {

		@Override
		public List<RichFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getRichFacilities(ac.getSession());
		}
	},

	/*#
	 * Searches for the Facilities by theirs destination.
	 *
	 * @param destination String Destination
	 * @return Facility Found facility
	 */
	getFacilitiesByDestination {

		@Override
		public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getFacilitiesByDestination(ac.getSession(),
					parms.readString("destination"));
		}
	},

	/*#
	 * List all facilities.
	 *
	 * @return List<Facility> All facilities
	 */
	getFacilities {

		@Override
		public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getFacilities(ac.getSession());
		}
	},

	/*#
	 * Gets count of all facilities.
	 * @return int Facilities count
	 */
	getFacilitiesCount {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getFacilitiesCount(ac.getSession());
		}
	},

	/*#
	 * Returns owners of a facility.
	 *
	 * @param facility int Facility ID
	 * @return List<Owner> Facility owners
	 */
	getOwners {

		@Override
		public List<Owner> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getOwners(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Add owner of a facility.
	 *
	 * @param facility int Facility ID
	 * @param owner int Owner ID
	 */
	addOwner {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getFacilitiesManager().addOwner(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					ac.getOwnerById(parms.readInt("owner")));
			return null;
		}
	},

	/*#
	 * Remove owner of a facility.
	 *
	 * @param facility int Facility ID
	 * @param owner int Owner ID
	 */
	removeOwner {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getFacilitiesManager().removeOwner(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					ac.getOwnerById(parms.readInt("owner")));
			return null;
		}
	},

	/*#
	 * Return all VO which can use a facility. (VO must have the resource which belongs to this facility.)
	 *
	 * @param facility int Facility ID
	 * @return List<Vo> List of VOs
	 */
	getAllowedVos {

		@Override
		public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getAllowedVos(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Get all assigned groups on Facility.
	 *
	 * @param facility int Facility ID
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by VO.
	 *
	 * @param facility int Facility ID
	 * @param vo int Vo ID to filter groups by
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by Service.
	 *
	 * @param facility int Facility ID
	 * @param service int Service ID to filter groups by
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by VO and Service.
	 *
	 * @param facility int Facility ID
	 * @param vo int Vo ID to filter groups by
	 * @param service int Service ID to filter groups by
	 * @return List<Group> assigned groups
	 */
	getAllowedGroups {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
			Facility facility = ac.getFacilityById(parms.readInt("facility"));
			Service service = null;
			Vo vo = null;
			if (parms.contains("vo")) {
				vo = ac.getVoById(parms.readInt("vo"));
			}
			if (parms.contains("service")) {
				service = ac.getServiceById(parms.readInt("service"));
			}
			return ac.getFacilitiesManager().getAllowedGroups(ac.getSession(),facility, vo, service);
		}
	},

	/*#
	 * Returns all resources assigned to a facility.
	 *
	 * @param facility int Facility ID
	 * @return List<Resource> Resources
	 */
	getAssignedResources {

		@Override
		public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getAssignedResources(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},


	/*#
	 * Returns all rich resources assigned to a facility with VO property filled.
	 * @param facility int Facility ID
	 * @return List<RichResource> Resources
	 */
	getAssignedRichResources {

		@Override
		public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getAssignedRichResources(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Creates a facility.
	 * @param facility Facility JSON object
	 * @return Facility Created Facility object
	 */
	createFacility {

		@Override
		public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().createFacility(ac.getSession(),
					parms.read("facility", Facility.class));
		}
	},

	/*#
	 * Deletes a facility.
	 * @param facility int Facility ID
	 */
	deleteFacility {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getFacilitiesManager().deleteFacility(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
			return null;
		}
	},

	/*#
	 * Update a facility (facility name)
	 *
	 * @param facility Facility JSON object
	 * @return Facility updated Facility object
	 */
	updateFacility {

		@Override
		public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().updateFacility(ac.getSession(),
					parms.read("facility", Facility.class));
		}
	},

	/*#
	 * Returns list of all facilities owned by the owner.
	 * @param owner int Owner ID
	 * @return List<Facility> Owner's facilities
	 */
	getOwnerFacilities {

		@Override
		public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getOwnerFacilities(ac.getSession(),
					ac.getOwnerById(parms.readInt("owner")));
		}
	},

	/*#
	 * Lists hosts of a Facility.
	 * @param facility int Facility ID
	 * @return List<Host> Hosts
	 */
	getHosts {
		@Override
		public List<Host> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getHosts(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Returns a host by its ID.
	 * @param id int Host ID
	 * @return Host Host object
	 */
	getHostById {
		@Override
		public Host call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getHostById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Returns hosts by hostname. (from all facilities)
	 * @param hostname String hostname of hosts
	 * @return List<Host> all hosts with this hostname, empty arrayList if none exists
	 */
	getHostsByHostname {
		@Override
		public List<Host> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getHostsByHostname(ac.getSession(), parms.readString("hostname"));
		}
	},

	/*#
	 * Return facility which has the host.
	 * @param host int Host ID
	 * @return Facility Facility object
	 */
	getFacilityForHost {
		@Override
		public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getFacilityForHost(ac.getSession(),
					ac.getHostById(parms.readInt("host")));
		}
	},

	/*#
	 * Count hosts of Facility.
	 * @param facility int Facility ID
	 * @return int Hosts count
	 */
	getHostsCount {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getHostsCount(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Adds hosts to the Facility.
	 *
	 * @param hostnames List<String> Host names
	 * @param facility int Facility ID
	 * @return List<Host> Hosts with ID's set.
	 */
	addHosts {
		@Override
		public List<Host> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			Facility facility = ac.getFacilityById(parms.readInt("facility"));

			List<String> hostnames = parms.readList("hostnames", String.class);

			return ac.getFacilitiesManager().addHosts(ac.getSession(), facility, hostnames);
		}
	},

	/*#
	 * Remove hosts from a Facility.
	 * @param hosts List<Integer> List of Host IDs
	 * @param facility int Facility ID
	 */
	removeHosts {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			Facility facility = ac.getFacilityById(parms.readInt("facility"));

			//TODO: optimalizovat?
			int[] ids = parms.readArrayOfInts("hosts");
			List<Host> hosts = new ArrayList<Host>(ids.length);
			for (int i : ids) {
				hosts.add(ac.getHostById(i));
			}

			ac.getFacilitiesManager().removeHosts(ac.getSession(),
					hosts,
					facility);
			return null;
		}
	},

	/*#
	 * Adds host to a Facility.
	 * @param hostname String Hostname
	 * @param facility int Facility ID
	 * @return Host Host with ID set.
	 */
	addHost {
		@Override
		public Host call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			Facility facility = ac.getFacilityById(parms.readInt("facility"));

			String hostname = parms.readString("hostname");
			Host host = new Host();
			host.setHostname(hostname);

			return ac.getFacilitiesManager().addHost(ac.getSession(), host, facility);
		}
	},

	/*#
	 * Removes a host.
	 * @param host int Host ID
	 */
	removeHost {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			int id = parms.readInt("host");

			Host host = ac.getFacilitiesManager().getHostById(ac.getSession(), id);

			ac.getFacilitiesManager().removeHost(ac.getSession(), host);
			return null;
		}
	},

	/*#
	 * Get facilities where the service is defined..
	 *
	 * @param service int Service ID
	 * @return List<Facility> Assigned facilities
	 */
	/*#
	 * Get facilities which are assigned to a Group (via resource).
	 *
	 * @param group int Group ID
	 * @return List<Facility> Assigned facilities
	 */
	/*#
	 * Get facilities which have the member access on.
	 *
	 * @param member int Member ID
	 * @return List<Facility> Assigned facilities
	 */
	/*#
	 * Get facilities which have the user access on.
	 *
	 * @param user int User ID
	 * @return List<Facility> Assigned facilities
	 */
	getAssignedFacilities {

		@Override
		public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("service")) {
				return ac.getFacilitiesManager().getAssignedFacilities(ac.getSession(),
						ac.getServiceById(parms.readInt("service")));
			} else if (parms.contains("group")) {
				return ac.getFacilitiesManager().getAssignedFacilities(ac.getSession(),
						ac.getGroupById(parms.readInt("group")));
			} else if (parms.contains("member")) {
				return ac.getFacilitiesManager().getAssignedFacilities(ac.getSession(),
						ac.getMemberById(parms.readInt("member")));
			} else if (parms.contains("user")) {
				return ac.getFacilitiesManager().getAssignedFacilities(ac.getSession(),
						ac.getUserById(parms.readInt("user")));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "service or group or member of user");
			}
		}
	},

	/*#
	 * Adds a Facility admin.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 */
	/*#
	 *  Adds a group administrator to the Facility.
	 *
	 *  @param facility int Facility ID
	 *  @param authorizedGroup int Group ID
	 */
	addAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getFacilitiesManager().addAdmin(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getFacilitiesManager().addAdmin(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")),
						ac.getGroupById(parms.readInt("authorizedGroup")));
			}
			return null;
		}
	},

	/*#
	 * Removes a Facility admin.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 */
	/*#
	 *  Removes a group administrator of the Facility.
	 *
	 *  @param facility int Facility ID
	 *  @param authorizedGroup int Group ID
	 */
	removeAdmin {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getFacilitiesManager().removeAdmin(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getFacilitiesManager().removeAdmin(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")),
						ac.getGroupById(parms.readInt("authorizedGroup")));
			}
			return null;
		}
	},

	/*#
	 * Get list of all facility administrators for supported role and given facility.
	 *
	 * If onlyDirectAdmins is == 1, return only direct admins of the group for supported role.
	 *
	 * Supported roles: FacilityAdmin
	 *
	 * @param facility int Facility ID
	 * @param onlyDirectAdmins int if == 1, get only direct facility administrators (if != 1, get both direct and indirect)
	 *
	 * @return List<User> list of all facility administrators of the given facility for supported role
	 */
	/*#
	 * Get all Facility admins.
	 *
	 * !!! DEPRECATED version !!!
	 *
	 * @param facility int Facility ID
	 * @return List<User> List of Users who are admins in the facility.
	 */
	getAdmins {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if(parms.contains("onlyDirectAdmins")) {
				return ac.getFacilitiesManager().getAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readInt("onlyDirectAdmins") ==1);
			} else {
				return ac.getFacilitiesManager().getAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
			}
		}
	},

	/*#
	 * Get all Facility direct admins.
	 *
	 * !!! DEPRECATED version !!!
	 *
	 * @param facility int Facility ID
	 * @return List<User> list of admins of the facility
	 */
	@Deprecated
	getDirectAdmins {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getDirectAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Get all Facility group admins.
	 *
	 * @param facility int Facility ID
	 * @return List<Group> admins
	 */
	getAdminGroups {

		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getAdminGroups(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Get list of all richUser administrators for the facility and supported role with specific attributes.
	 *
	 * Supported roles: FacilityAdmin
	 *
	 * If "onlyDirectAdmins" is == 1, return only direct admins of the facility for supported role with specific attributes.
	 * If "allUserAttributes" is == 1, do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param facility int Facility ID
	 * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
	 * @param allUserAttributes int if == 1, get all possible user attributes and ignore list of specificAttributes (if != 1, get only specific attributes)
	 * @param onlyDirectAdmins int if == 1, get only direct facility administrators (if != 1, get both direct and indirect)
	 *
	 * @return List<RichUser> list of RichUser administrators for the facility and supported role with attributes
	 */
	/*#
    * Get all Facility admins as RichUsers
    *
	* !!! DEPRECATED version !!!
	*
    * @param facility int Facility ID
    * @return List<RichUser> admins
    */
	getRichAdmins {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if(parms.contains("onlyDirectAdmins")) {
				return ac.getFacilitiesManager().getRichAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readList("specificAttributes", String.class),
					parms.readInt("allUserAttributes") == 1,
					parms.readInt("onlyDirectAdmins") == 1);
			} else {
				return ac.getFacilitiesManager().getRichAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
			}
		}
	},

	/*#
	* Get all Facility admins as RichUsers with all their non-null user attributes
	*
	* !!! DEPRECATED version !!!
	*
	* @param facility int Facility ID
	* @return List<RichUser> admins with attributes
	*/
	@Deprecated
	getRichAdminsWithAttributes {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getRichAdminsWithAttributes(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	* Get all Facility admins as RichUsers with specific attributes (from user namespace)
	*
	* !!! DEPRECATED version !!!
	*
	* @param facility int Facility ID
	* @param specificAttributes List<String> list of attributes URNs
	* @return List<RichUser> admins with attributes
	*/
	@Deprecated
	getRichAdminsWithSpecificAttributes {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getRichAdminsWithSpecificAttributes(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readList("specificAttributes", String.class));
		}
	},

	/*#
	* Get all Facility admins, which are assigned directly,
	* as RichUsers with specific attributes (from user namespace)
	*
	* !!! DEPRECATED version !!!
	*
	* @param facility int Facility ID
	* @param specificAttributes List<String> list of attributes URNs
	* @return List<RichUser> direct admins with attributes
	*/
	@Deprecated
	getDirectRichAdminsWithSpecificAttributes {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getDirectRichAdminsWithSpecificAttributes(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readList("specificAttributes", String.class));
		}
	},

	/*#
	 * Returns list of Facilities, where the user is an Administrator.
	 *
	 * @param user int User ID
	 * @return List<Facility> Found Facilities
	 */
	getFacilitiesWhereUserIsAdmin {

		@Override
		public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getFacilitiesWhereUserIsAdmin(ac.getSession(),
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Return all facilities where exists host with the specific hostname
	 *
	 * @param hostname String specific hostname
	 * @return List<Facility> Found Facilities
	 */
	getFacilitiesByHostName {

		@Override
		public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getFacilitiesByHostName(ac.getSession(),
					parms.readString("hostname"));
		}
	},

	/*#
	 * Return all users which can use this facility
	 *
	 * @param facility int Facility ID
	 * @param vo int VO ID, if provided, filter out users who aren't in specific VO
	 * @param service int Service ID, if provided, filter out users who aren't allowed to use the service on the facility
	 * @return List<User> list of allowed users
	 */
	getAllowedUsers {
		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("vo")) {
				if(parms.contains("service")) {
					return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getVoById(parms.readInt("vo")),
							ac.getServiceById(parms.readInt("service")));
				} else {
					return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getVoById(parms.readInt("vo")),
							null);
				}
			} else if(parms.contains("service")) {
				return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")),
						null,
						ac.getServiceById(parms.readInt("service")));
			} else {
				return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")));
			}
		}
	},

	/*#
	 * Copy owners from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacility int facility ID
	 * @param destFacility int facility ID
	 */
	copyOwners {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getFacilitiesManager().copyOwners(ac.getSession(),
					ac.getFacilityById(parms.readInt("srcFacility")),
					ac.getFacilityById(parms.readInt("destFacility")));

			return null;

		}
	},

	/*#
	 * Copy managers from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacility int facility ID
	 * @param destFacility int facility ID
	 */
	copyManagers {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getFacilitiesManager().copyManagers(ac.getSession(),
					ac.getFacilityById(parms.readInt("srcFacility")),
					ac.getFacilityById(parms.readInt("destFacility")));

			return null;

		}
	},

	/*#
	 * Copy attributes (settings) from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacility int facility ID
	 * @param destFacility int facility ID
	 */
	copyAttributes {

		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getFacilitiesManager().copyAttributes(ac.getSession(),
					ac.getFacilityById(parms.readInt("srcFacility")),
					ac.getFacilityById(parms.readInt("destFacility")));

			return null;

		}
	};
}
