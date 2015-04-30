package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum FacilitiesManagerMethod implements ManagerMethod {

	/*#
	 * Searches for the Facility with specified id.
	 *
	 * @param id int Facility <code>id</code>
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
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return List<User> assigned users
	 */
	/*#
	 * Lists all users assigned to facility.
	 *
	 * @param facility int Facility <code>id</code>
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
	 * @param facility int Facility <code>id</code>
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
	 * @param facility int Facility <code>id</code>
	 * @param owner int Owner <code>id</code>
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
	 * @param facility int Facility <code>id</code>
	 * @param owner int Owner <code>id</code>
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
	 * @param facility int Facility <code>id</code>
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
	 * @param facility int Facility <code>id</code>
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by VO.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param vo int Vo <code>id</code> to filter groups by
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by Service.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param service int Service <code>id</code> to filter groups by
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by VO and Service.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param vo int Vo <code>id</code> to filter groups by
	 * @param service int Service <code>id</code> to filter groups by
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
	 * @param facility int Facility <code>id</code>
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
	 * @param facility int Facility <code>id</code>
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
	 * @param facility int Facility <code>id</code>
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
	 * @param owner int Owner <code>id</code>
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
	 * @param facility int Facility <code>id</code>
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
	 * Returns a host by its <code>id</code>.
	 * @param id int Host <code>id</code>
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
	 * @param host int Host <code>id</code>
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
	 * @param facility int Facility <code>id</code>
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
	 * @param facility int Facility <code>id</code>
	 * @return List<Host> Hosts with <code>id</code>'s set.
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
	 * @param facility int Facility <code>id</code>
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
	 * @param facility int Facility <code>id</code>
	 * @return Host Host with <code>id</code> set.
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
	 * @param host int Host <code>id</code>
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
	 * @param service int Service <code>id</code>
	 * @return List<Facility> Assigned facilities
	 */
	/*#
	 * Get facilities which are assigned to a Group (via resource).
	 *
	 * @param group int Group <code>id</code>
	 * @return List<Facility> Assigned facilities
	 */
	/*#
	 * Get facilities which have the member access on.
	 *
	 * @param member int Member <code>id</code>
	 * @return List<Facility> Assigned facilities
	 */
	/*#
	 * Get facilities which have the user access on.
	 *
	 * @param user int User <code>id</code>
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
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 */
	/*#
	 *  Adds a group administrator to the Facility.
	 *
	 *  @param facility int Facility <code>id</code>
	 *  @param authorizedGroup int Group <code>id</code>
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
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 */
	/*#
	 *  Removes a group administrator of the Facility.
	 *
	 *  @param facility int Facility <code>id</code>
	 *  @param authorizedGroup int Group <code>id</code>
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
	 * If onlyDirectAdmins is == true, return only direct admins of the group for supported role.
	 *
	 * Supported roles: FacilityAdmin
	 *
	 * @param facility int Facility <code>id</code>
	 * @param onlyDirectAdmins boolean if true, get only direct facility administrators (if false, get both direct and indirect)
	 *
	 * @return List<User> list of all facility administrators of the given facility for supported role
	 */
	/*#
	 * Get all Facility admins.
	 *
	 * @deprecated
	 * @param facility int Facility <code>id</code>
	 * @return List<User> List of Users who are admins in the facility.
	 */
	getAdmins {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if(parms.contains("onlyDirectAdmins")) {
				return ac.getFacilitiesManager().getAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getFacilitiesManager().getAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
			}
		}
	},

	/*#
	 * Get all Facility direct admins.
	 *
	 * @deprecated
	 * @param facility int Facility <code>id</code>
	 * @return List<User> list of admins of the facility
	 */
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
	 * @param facility int Facility <code>id</code>
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
	 * If "onlyDirectAdmins" is true, return only direct admins of the facility for supported role with specific attributes.
	 * If "allUserAttributes" is true, do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
	 * @param allUserAttributes int if == true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins int if == true, get only direct facility administrators (if false, get both direct and indirect)
	 *
	 * @return List<RichUser> list of RichUser administrators for the facility and supported role with attributes
	 */
	/*#
    * Get all Facility admins as RichUsers
	*
	* @deprecated
    * @param facility int Facility <code>id</code>
    * @return List<RichUser> admins
    */
	getRichAdmins {

		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

			if(parms.contains("onlyDirectAdmins")) {
				return ac.getFacilitiesManager().getRichAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readList("specificAttributes", String.class),
					parms.readBoolean("allUserAttributes"),
					parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getFacilitiesManager().getRichAdmins(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
			}
		}
	},

	/*#
	* Get all Facility admins as RichUsers with all their non-null user attributes
	*
	* @deprecated
	* @param facility int Facility <code>id</code>
	* @return List<RichUser> admins with attributes
	*/
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
	* @deprecated
	* @param facility int Facility <code>id</code>
	* @param specificAttributes List<String> list of attributes URNs
	* @return List<RichUser> admins with attributes
	*/
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
	* @deprecated
	* @param facility int Facility <code>id</code>
	* @param specificAttributes List<String> list of attributes URNs
	* @return List<RichUser> direct admins with attributes
	*/
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
	 * @param user int User <code>id</code>
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
	 * @param facility int Facility <code>id</code>
	 * @param vo int VO <code>id</code>, if provided, filter out users who aren't in specific VO
	 * @param service int Service <code>id</code>, if provided, filter out users who aren't allowed to use the service on the facility
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
	 * @param srcFacility int facility <code>id</code>
	 * @param destFacility int facility <code>id</code>
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
	 * @param srcFacility int facility <code>id</code>
	 * @param destFacility int facility <code>id</code>
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
	 * @param srcFacility int facility <code>id</code>
	 * @param destFacility int facility <code>id</code>
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
