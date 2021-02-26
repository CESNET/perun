package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.SecurityTeam;
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
			return ac.getFacilitiesManager().getFacilityById(ac.getSession(), parms.readInt("id"));
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
			return ac.getFacilitiesManager().getFacilityByName(ac.getSession(), parms.readString("name"));
		}
	},

	/*#
	 * Returns facilities by their IDs.
	 *
	 * @param ids List<Integer> list of facilities IDs
	 * @return List<Facility> facilities with specified IDs
	 */
	getFacilitiesByIds {

		@Override
		public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getFacilitiesByIds(ac.getSession(), parms.readList("ids", Integer.class));
		}
	},

	/*#
	 * Lists all users assigned to facility containing resources where service is assigned.
	 *
	 * @param service int Service <code>id</code>
	 * @param facilityName String Facility name
	 * @return List<User> assigned users
	 */
	/*#
	 * Lists all users assigned to facility.
	 *
	 * @param facilityName String Facility name
	 * @return List<User> assigned users
	 */
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
				return ac.getFacilitiesManager().getAssignedUsers(ac.getSession(), getFacility(ac, parms),ac.getServiceById(parms.readInt("service")));
			else
				return ac.getFacilitiesManager().getAssignedUsers(ac.getSession(), getFacility(ac, parms));
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
	 * Returns all facilities that have set the attribute 'attributeName' with the value 'attributeValue'.
	 * Searching only def and opt attributes.
	 *
	 * @param attributeName String
	 * @param attributeValue String
	 * @return List<Facility> facilities with the specified attribute
	 */
	getFacilitiesByAttribute {

		@Override
		public List<Facility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getFacilitiesByAttribute(ac.getSession(),
					parms.readString("attributeName"), parms.readString("attributeValue"));
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
	 * Gets all enriched facilities user has access rights to.
	 * If User is:
	 * - PERUNADMIN : all facilities
	 * - FACILITYADMIN : only facilities where user is facility admin
	 * - FACILITYOBSERVER: only facilities where user is facility observer
	 *
	 * @return List<EnrichedFacility> All enriched facilities
	 */
	getEnrichedFacilities {

		@Override
		public List<EnrichedFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getEnrichedFacilities(ac.getSession());
		}
	},

	/*#
	 * Returns owners of a facility.
	 *
	 * @param facilityName String Facility name
	 * @return List<Owner> Facility owners
	 */
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
					getFacility(ac, parms));
		}
	},

	/*#
	 * Add owner of a facility.
	 *
	 * @param facilityName String Facility name
	 * @param ownerName String Owner name
	 */
	/*#
	 * Add owner of a facility.
	 *
	 * @param facilityName String Facility name
	 * @param owner int Owner <code>id</code>
	 */
	/*#
	 * Add owner of a facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param ownerName String Owner name
	 */
	/*#
	 * Add owner of a facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param owner int Owner <code>id</code>
	 */
	addOwner {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			Owner owner;
			if (parms.contains("ownerName")) owner = ac.getOwnerByName(parms.readString("ownerName"));
			else owner = ac.getOwnerById(parms.readInt("owner"));

			ac.getFacilitiesManager().addOwner(ac.getSession(),
					getFacility(ac, parms),
					owner);
			return null;
		}
	},

	/*#
	 * Remove owner of a facility.
	 *
	 * @param facilityName String Facility name
	 * @param ownerName String Owner name
	 */
	/*#
	 * Remove owner of a facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param ownerName String Owner name
	 */
	/*#
	 * Remove owner of a facility.
	 *
	 * @param facilityName String Facility name
	 * @param owner int Owner <code>id</code>
	 */
	/*#
	 * Remove owner of a facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param owner int Owner <code>id</code>
	 */
	removeOwner {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			Owner owner;
			if (parms.contains("ownerName")) owner = ac.getOwnerByName(parms.readString("ownerName"));
			else owner = ac.getOwnerById(parms.readInt("owner"));

			ac.getFacilitiesManager().removeOwner(ac.getSession(),
					getFacility(ac, parms),
					owner);
			return null;
		}
	},

	/*#
	 * Return all VO which can use a facility. (VO must have the resource which belongs to this facility.)
	 *
	 * @param facilityName String Facility name
	 * @return List<Vo> List of VOs
	 */
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
					getFacility(ac, parms));
		}
	},

	/*#
	 * Get all assigned groups on Facility.
	 *
	 * @param facilityName String Facility name
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by VO.
	 *
	 * @param facilityName String Facility name
	 * @param vo int Vo <code>id</code> to filter groups by
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by Service.
	 *
	 * @param facilityName String Facility name
	 * @param service int Service <code>id</code> to filter groups by
	 * @return List<Group> assigned groups
	 */
	/*#
	 * Get all assigned groups on Facility filtered by VO and Service.
	 *
	 * @param facilityName String Facility name
	 * @param vo int Vo <code>id</code> to filter groups by
	 * @param service int Service <code>id</code> to filter groups by
	 * @return List<Group> assigned groups
	 */
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
			Facility facility = getFacility(ac, parms);
			Service service = null;
			Vo vo = null;
			if (parms.contains("vo")) {
				vo = ac.getVoById(parms.readInt("vo"));
			}
			if (parms.contains("service")) {
				service = ac.getServiceById(parms.readInt("service"));
			}
			return ac.getFacilitiesManager().getAllowedGroups(ac.getSession(), facility, vo, service);
		}
	},

	/*#
	 * Get all assigned RichGroups on Facility with specified set of attributes.
	 *
	 * @param facilityName String Facility name
	 * @param attrNames List<String> Attribute names
	 * @return List<RichGroup> assigned groups
	 * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" , "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
	 */
	/*#
	 * Get all assigned RichGroups on Facility filtered by VO with specified set of attributes.
	 *
	 * @param facilityName String Facility name
	 * @param vo int Vo <code>id</code> to filter groups by
	 * @param attrNames List<String> Attribute names
	 * @return List<RichGroup> assigned groups
	 * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" , "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
	 */
	/*#
	 * Get all assigned RichGroups on Facility filtered by Service with specified set of attributes.
	 *
	 * @param facilityName String Facility name
	 * @param service int Service <code>id</code> to filter groups by
	 * @param attrNames List<String> Attribute names
	 * @return List<RichGroup> assigned groups
	 * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" , "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
	 */
	/*#
	 * Get all assigned RichGroups on Facility filtered by VO and Service with specified set of attributes.
	 *
	 * @param facilityName String Facility name
	 * @param vo int Vo <code>id</code> to filter groups by
	 * @param service int Service <code>id</code> to filter groups by
	 * @param attrNames List<String> Attribute names
	 * @return List<RichGroup> assigned groups
	 * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" , "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
	 */
	/*#
	 * Get all assigned RichGroups on Facility with specified set of attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @return List<RichGroup> assigned groups
	 * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" , "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
	 */
	/*#
	 * Get all assigned RichGroups on Facility filtered by VO with specified set of attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param vo int Vo <code>id</code> to filter groups by
	 * @param attrNames List<String> Attribute names
	 * @return List<RichGroup> assigned groups
	 * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" , "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
	 */
	/*#
	 * Get all assigned RichGroups on Facility filtered by Service with specified set of attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param service int Service <code>id</code> to filter groups by
	 * @param attrNames List<String> Attribute names
	 * @return List<RichGroup> assigned groups
	 * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" , "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
	 */
	/*#
	 * Get all assigned RichGroups on Facility filtered by VO and Service with specified set of attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param vo int Vo <code>id</code> to filter groups by
	 * @param service int Service <code>id</code> to filter groups by
	 * @param attrNames List<String> Attribute names
	 * @return List<RichGroup> assigned groups
	 * @exampleParam attrNames [ "urn:perun:group:attribute-def:core:name" , "urn:perun:group:attribute-def:def:synchronizationEnabled" ]
	 */
	getAllowedRichGroupsWithAttributes {

		@Override
		public List<RichGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {
			Facility facility = getFacility(ac, parms);

			Service service = null;
			Vo vo = null;
			if (parms.contains("vo")) {
				vo = ac.getVoById(parms.readInt("vo"));
			}
			if (parms.contains("service")) {
				service = ac.getServiceById(parms.readInt("service"));
			}
			return ac.getFacilitiesManager().getAllowedRichGroupsWithAttributes(ac.getSession(), facility, vo, service, parms.readList("attrNames", String.class));
		}
	},

	/*#
	 * Returns all resources assigned to a facility.
	 *
	 * @param facilityName String Facility name
	 * @return List<Resource> Resources
	 */
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
					getFacility(ac, parms));
		}
	},

	/*#
	 * Returns resources with specific service assigned to the facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param service int Service <code>id</code>
	 * @return List<Resource> Resources
	 */
	getAssignedResourcesByAssignedService {

		@Override
		public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getAssignedResourcesByAssignedService(ac.getSession(),
				ac.getFacilityById(parms.readInt("facility")), ac.getServiceById(parms.readInt("service")));
		}
	},

	/*#
	 * Returns all rich resources assigned to a facility with VO property filled.
	 * @param facilityName String Facility name
	 * @return List<RichResource> Resources
	 */
	/*#
	 * Returns all rich resources assigned to a facility with VO property filled.
	 * @param facility int Facility <code>id</code>
	 * @return List<RichResource> Resources
	 */
	getAssignedRichResources {

		@Override
		public List<RichResource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getAssignedRichResources(ac.getSession(),
					getFacility(ac, parms));
		}
	},

	/*#
	 * Creates a facility. Caller is automatically set as facility manager.
	 * Facility Object must contain name which can contain only a-Z0-9.-_ and space characters.
	 * Parameter description is optional.
	 * Other parameters are ignored.
	 * @param facility Facility JSON object
	 * @return Facility Created Facility object
	 * @exampleParam facility { "name" : "the best-facility_7" }
	 */
	/*#
	 * Creates a facility. Caller is automatically set as facility manager.
	 * @param name String name of a facility - can contain only a-Z0-9.-_ and space characters.
	 * @param description String description of a facility
	 * @return Facility Created Facility object
	 * @exampleParam name "the best-facility_7"
	 * @exampleParam description "A description with information."
	 */
	createFacility {

		@Override
		public Facility call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				return ac.getFacilitiesManager().createFacility(ac.getSession(),
						parms.read("facility", Facility.class));
			} else if (parms.contains("name") && parms.contains("description")) {
				String name = parms.readString("name");
				String description = parms.readString("description");
				Facility facility = new Facility(0, name, description);
				return ac.getFacilitiesManager().createFacility(ac.getSession(), facility);
			} else {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER);
			}
		}
	},

	/*#
	 * Deletes a facility.
	 * @param facilityName String Facility name
	 */
	/*#
	 * Deletes a facility.
	 * @param facilityName String Facility name
	 * @param force Boolean if true deletes all constrains of facility before deleting facility
	 */
	/*#
	 * Deletes a facility.
	 * @param facility int Facility <code>id</code>
	 */
	/*#
	 * Deletes a facility.
	 * @param facility int Facility <code>id</code>
	 * @param force Boolean if true deletes all constrains of facility before deleting facility
	 */
	deleteFacility {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("force")){
				ac.getFacilitiesManager().deleteFacility(ac.getSession(),
					getFacility(ac, parms),
					parms.readBoolean("force"));
				return null;
			} else {
				ac.getFacilitiesManager().deleteFacility(ac.getSession(),
					getFacility(ac, parms),
					false);
				return null;
			}
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
	 * @param facilityName String Facility name
	 * @return List<Host> Hosts
	 */
	/*#
	 * Lists hosts of a Facility.
	 * @param facility int Facility <code>id</code>
	 * @return List<Host> Hosts
	 */
	getHosts {
		@Override
		public List<Host> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getHosts(ac.getSession(),
					getFacility(ac, parms));
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
	 * Return all enriched hosts of given facility. That is host with all its attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @return List<EnrichedHosts> enrichedHosts
	 */
	getEnrichedHosts {
		@Override
		public List<EnrichedHost> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getEnrichedHosts(ac.getSession(),
				ac.getFacilityById(parms.readInt("facility")),
				parms.readList("attrNames", String.class));
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
	 * @param facilityName String Facility name
	 * @return int Hosts count
	 */
	/*#
	 * Count hosts of Facility.
	 * @param facility int Facility <code>id</code>
	 * @return int Hosts count
	 */
	getHostsCount {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getHostsCount(ac.getSession(),
					getFacility(ac, parms));
		}
	},

	/*#
	 * Adds hosts to the Facility.
	 *
	 * @param hostnames List<String> Host names
	 * @param facilityName String Facility name
	 * @return List<Host> Hosts with <code>id</code>'s set.
	 * @throw InvalidHostnameException When host has invalid hostname.
	 */
	/*#
	 * Adds hosts to the Facility.
	 *
	 * @param hostnames List<String> Host names
	 * @param facility int Facility <code>id</code>
	 * @return List<Host> Hosts with <code>id</code>'s set.
	 * @throw InvalidHostnameException When host has invalid hostname.
	 */
	addHosts {
		@Override
		public List<Host> call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Facility facility = getFacility(ac, parms);

			List<String> hostnames = parms.readList("hostnames", String.class);

			return ac.getFacilitiesManager().addHosts(ac.getSession(), facility, hostnames);
		}
	},

	/*#
	 * Remove hosts from a Facility.
	 * @param hosts List<Integer> List of Host IDs
	 * @param facilityName String Facility name
	 */
	/*#
	 * Remove hosts from a Facility.
	 * @param hosts List<Integer> List of Host IDs
	 * @param facility int Facility <code>id</code>
	 */
	removeHosts {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Facility facility = getFacility(ac, parms);

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
	 * @param facilityName String Facility name
	 * @return Host Host with <code>id</code> set.
	 * @throw InvalidHostnameException When host has invalid hostname.
	 */
	/*#
	 * Adds host to a Facility.
	 * @param hostname String Hostname
	 * @param facility int Facility <code>id</code>
	 * @return Host Host with <code>id</code> set.
	 * @throw InvalidHostnameException When host has invalid hostname.
	 */
	addHost {
		@Override
		public Host call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Facility facility = getFacility(ac, parms);

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
			parms.stateChangingCheck();

			int id = parms.readInt("host");

			Host host = ac.getFacilitiesManager().getHostById(ac.getSession(), id);

			ac.getFacilitiesManager().removeHost(ac.getSession(), host);
			return null;
		}
	},

	/*#
	 * Remove host from the Facility based on hostname. If there is ambiguity, method throws exception and no host is removed.
	 *
	 * @param hostname String hostname
	 * @throw HostNotExistsException When host doesn't exist or is not unique by name
	 */
	removeHostByHostname {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getFacilitiesManager().removeHostByHostname(ac.getSession(), parms.readString("hostname"));
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
	 * @param facilityName String Facility name
	 * @param user int User <code>id</code>
	 */
	/*#
	 *  Adds a group administrator to the Facility.
	 *
	 *  @param facilityName String Facility name
	 *  @param authorizedGroup int Group <code>id</code>
	 */
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
			parms.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getFacilitiesManager().addAdmin(ac.getSession(),
						getFacility(ac, parms),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getFacilitiesManager().addAdmin(ac.getSession(),
						getFacility(ac, parms),
						ac.getGroupById(parms.readInt("authorizedGroup")));
			}
			return null;
		}
	},

	/*#
	 * Removes a Facility admin.
	 *
	 * @param facilityName String Facility name
	 * @param user int User <code>id</code>
	 */
	/*#
	 *  Removes a group administrator of the Facility.
	 *
	 *  @param facilityName String Facility name
	 *  @param authorizedGroup int Group <code>id</code>
	 */
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
			parms.stateChangingCheck();
			if (parms.contains("user")) {
				ac.getFacilitiesManager().removeAdmin(ac.getSession(),
						getFacility(ac, parms),
						ac.getUserById(parms.readInt("user")));
			} else {
				ac.getFacilitiesManager().removeAdmin(ac.getSession(),
						getFacility(ac, parms),
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
	 * @param facilityName String Facility name
	 * @param onlyDirectAdmins boolean if true, get only direct facility administrators (if false, get both direct and indirect)
	 *
	 * @return List<User> list of all facility administrators of the given facility for supported role
	 */
	/*#
	 * Get all Facility admins.
	 *
	 * @deprecated
	 * @param facilityName String Facility name
	 * @return List<User> List of Users who are admins in the facility.
	 */
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
						getFacility(ac, parms),
						parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getFacilitiesManager().getAdmins(ac.getSession(),
						getFacility(ac, parms));
			}
		}
	},

	/*#
	 * Get all Facility direct admins.
	 *
	 * @deprecated
	 * @param facilityName String Facility name
	 * @return List<User> list of admins of the facility
	 */
	getDirectAdmins {

		@Override
		public List<User> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getDirectAdmins(ac.getSession(),
					getFacility(ac, parms));
		}
	},

	/*#
	 * Get all Facility group admins.
	 *
	 * @param facilityName String Facility name
	 * @return List<Group> admins
	 */
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
					getFacility(ac, parms));
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
	 * @param facilityName String Facility name
	 * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
	 * @param allUserAttributes boolean if == true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins boolean if == true, get only direct facility administrators (if false, get both direct and indirect)
	 *
	 * @return List<RichUser> list of RichUser administrators for the facility and supported role with attributes
	 */
	/*#
	 * Get all Facility admins as RichUsers
	 *
	 * @deprecated
	 * @param facilityName String Facility name
	 * @return List<RichUser> admins
	 */
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
	 * @param allUserAttributes boolean if == true, get all possible user attributes and ignore list of specificAttributes (if false, get only specific attributes)
	 * @param onlyDirectAdmins boolean if == true, get only direct facility administrators (if false, get both direct and indirect)
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
						getFacility(ac, parms),
						parms.readList("specificAttributes", String.class),
						parms.readBoolean("allUserAttributes"),
						parms.readBoolean("onlyDirectAdmins"));
			} else {
				return ac.getFacilitiesManager().getRichAdmins(ac.getSession(),
						getFacility(ac, parms));
			}
		}
	},

	/*#
	 * Get all Facility admins as RichUsers with all their non-null user attributes
	 *
	 * @deprecated
	 * @param facilityName String Facility name
	 * @return List<RichUser> admins with attributes
	 */
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
					getFacility(ac, parms));
		}
	},

	/*#
	 * Get all Facility admins as RichUsers with specific attributes (from user namespace)
	 *
	 * @deprecated
	 * @param facilityName String Facility name
	 * @param specificAttributes List<String> list of attributes URNs
	 * @return List<RichUser> admins with attributes
	 */
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
					getFacility(ac, parms),
					parms.readList("specificAttributes", String.class));
		}
	},

	/*#
	 * Get all Facility admins, which are assigned directly,
	 * as RichUsers with specific attributes (from user namespace)
	 *
	 * @deprecated
	 * @param facilityName String Facility name
	 * @param specificAttributes List<String> list of attributes URNs
	 * @return List<RichUser> direct admins with attributes
	 */
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
					getFacility(ac, parms),
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
	 * @param facilityName String Facility name
	 * @param vo int VO <code>id</code>, if provided, filter out users who aren't in specific VO
	 * @param service int Service <code>id</code>, if provided, filter out users who aren't allowed to use the service on the facility
	 * @return List<User> list of allowed users
	 */
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
							getFacility(ac, parms),
							ac.getVoById(parms.readInt("vo")),
							ac.getServiceById(parms.readInt("service")));
				} else {
					return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(),
							getFacility(ac, parms),
							ac.getVoById(parms.readInt("vo")),
							null);
				}
			} else if(parms.contains("service")) {
				return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(),
						getFacility(ac, parms),
						null,
						ac.getServiceById(parms.readInt("service")));
			} else {
				return ac.getFacilitiesManager().getAllowedUsers(ac.getSession(),
						getFacility(ac, parms));
			}
		}
	},

	/*#
	 * Copy owners from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacilityName String facility name
	 * @param destFacilityName String facility name
	 */
	/*#
	 * Copy owners from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacility int facility <code>id</code>
	 * @param destFacilityName String facility name
	 */
	/*#
	 * Copy owners from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacilityName String facility name
	 * @param destFacility int facility <code>id</code>
	 */
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
			parms.stateChangingCheck();

			Facility srcFacility;
			if (parms.contains("srcFacilityName")) srcFacility = ac.getFacilityByName(parms.readString("srcFacilityName"));
			else srcFacility = ac.getFacilityById(parms.readInt("srcFacility"));

			Facility destFacility;
			if (parms.contains("destFacilityName")) destFacility = ac.getFacilityByName(parms.readString("destFacilityName"));
			else destFacility = ac.getFacilityById(parms.readInt("destFacility"));

			ac.getFacilitiesManager().copyOwners(ac.getSession(),
					srcFacility,
					destFacility);

			return null;

		}
	},

	/*#
	 * Copy managers from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacilityName String facility name
	 * @param destFacilityName String facility name
	 */
	/*#
	 * Copy managers from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacility int facility <code>id</code>
	 * @param destFacilityName String facility name
	 */
	/*#
	 * Copy managers from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacilityName String facility name
	 * @param destFacility int facility <code>id</code>
	 */
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
			parms.stateChangingCheck();

			Facility srcFacility;
			if (parms.contains("srcFacilityName")) srcFacility = ac.getFacilityByName(parms.readString("srcFacilityName"));
			else srcFacility = ac.getFacilityById(parms.readInt("srcFacility"));

			Facility destFacility;
			if (parms.contains("destFacilityName")) destFacility = ac.getFacilityByName(parms.readString("destFacilityName"));
			else destFacility = ac.getFacilityById(parms.readInt("destFacility"));

			ac.getFacilitiesManager().copyManagers(ac.getSession(),
					srcFacility,
					destFacility);

			return null;

		}
	},

	/*#
	 * Copy attributes (settings) from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacilityName String facility name
	 * @param destFacilityName String facility name
	 */
	/*#
	 * Copy attributes (settings) from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacility int facility <code>id</code>
	 * @param destFacilityName String facility name
	 */
	/*#
	 * Copy attributes (settings) from source facility to destination facility.
	 * You must be facility manager of both.
	 *
	 * @param srcFacilityName String facility name
	 * @param destFacility int facility <code>id</code>
	 */
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
			parms.stateChangingCheck();

			Facility srcFacility;
			if (parms.contains("srcFacilityName")) srcFacility = ac.getFacilityByName(parms.readString("srcFacilityName"));
			else srcFacility = ac.getFacilityById(parms.readInt("srcFacility"));

			Facility destFacility;
			if (parms.contains("destFacilityName")) destFacility = ac.getFacilityByName(parms.readString("destFacilityName"));
			else destFacility = ac.getFacilityById(parms.readInt("destFacility"));

			ac.getFacilitiesManager().copyAttributes(ac.getSession(),
					srcFacility,
					destFacility);

			return null;

		}
	},

	/*#
	 * Get list of contact groups for the Owner.
	 *
	 * @param owner int Owner <code>id</code>
	 * @return List<ContactGroup> list of assigned contact groups
	 */
	/*#
	 * Get list of contact groups for the User.
	 *
	 * @param user int User <code>id</code>
	 * @return List<ContactGroup> list of assigned contact groups
	 */
	/*#
	 * Get list of contact groups for the Group.
	 *
	 * @param group int Group <code>id</code>
	 * @return List<ContactGroup> list of assigned contact groups
	 */
	/*#
	 * Get list of contact groups for the Facility.
	 *
	 * @param facilityName String Facility name
	 * @return List<ContactGroup> list of assigned contact groups
	 */
	/*#
	 * Get list of contact groups for the Facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<ContactGroup> list of assigned contact groups
	 */
	getFacilityContactGroups {
		@Override
		public List<ContactGroup> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("owner")) {
				return ac.getFacilitiesManager().getFacilityContactGroups(ac.getSession(),
						ac.getOwnerById(parms.readInt("owner")));
			} else if(parms.contains("user")) {
				return ac.getFacilitiesManager().getFacilityContactGroups(ac.getSession(),
						ac.getUserById(parms.readInt("user")));
			} else if(parms.contains("group")) {
				return ac.getFacilitiesManager().getFacilityContactGroups(ac.getSession(),
						ac.getGroupById(parms.readInt("group")));
			} else if(parms.contains("facility")) {
				return ac.getFacilitiesManager().getFacilityContactGroups(ac.getSession(),
						getFacility(ac, parms));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "owner or user or group or facility");
			}
		}
	},

	/*#
	 * Get contact group for the facility and the name.
	 *
	 * @param facilityName String Facility name
	 * @param name String name of the contact group
	 * @return ContactGroup contactGroup for the facility and the name
	 */
	/*#
	 * Get contact group for the facility and the name.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param name String name of the contact group
	 * @return ContactGroup contactGroup for the facility and the name
	 */
	getFacilityContactGroup {
		@Override
		public ContactGroup call(ApiCaller ac, Deserializer parms) throws PerunException {
			if((parms.contains("facility") || parms.contains("facilityName")) && parms.contains("name")) {
				return ac.getFacilitiesManager().getFacilityContactGroup(ac.getSession(),
						getFacility(ac, parms), parms.readString("name"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility and name or facilityName and name");
			}
		}
	},

	/*#
	 * Get all exist contact group names.
	 *
	 * @return List<String> list of contact group names
	 */
	getAllContactGroupNames {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getAllContactGroupNames(ac.getSession());
		}
	},

	/*#
	 * Add all contacts in list of facilities contact groups
	 *
	 * @param contactGroupsToAdd List<ContactGroup> list of contact groups to add
	 */
	addFacilityContacts {
		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getFacilitiesManager().addFacilityContacts(ac.getSession(),
					parms.readList("contactGroupsToAdd", ContactGroup.class));

			return null;
		}
	},

	/*#
	 * Add all contacts in the contact group
	 *
	 * @param contactGroupToAdd ContactGroup contact group to add
	 */
	addFacilityContact {
		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getFacilitiesManager().addFacilityContact(ac.getSession(),
					parms.read("contactGroupToAdd", ContactGroup.class));

			return null;
		}
	},

	/*#
	 * Remove all contacts in list of facilities contact groups
	 *
	 * @param contactGroupsToRemove List<ContactGroup> list of contact groups to remove
	 */
	removeFacilityContacts {
		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getFacilitiesManager().removeFacilityContacts(ac.getSession(),
					parms.readList("contactGroupsToRemove", ContactGroup.class));

			return null;
		}
	},

	/*#
	 * Remove all contacts in the contact group
	 *
	 * @param contactGroupToRemove ContactGroup contact group to remove
	 */
	removeFacilityContact {
		@Override
		public Object call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getFacilitiesManager().removeFacilityContact(ac.getSession(),
					parms.read("contactGroupToRemove", ContactGroup.class));

			return null;
		}
	},

	/*#
	 * Return assigned security teams for specific facility
	 *
	 * @param facilityName String Facility name
	 * @return List<SecurityTeam> assigned security teams fot given facility
	 * @throw FacilityNotExistsException When Facility with given name doesn't exists.
	 */
	/*#
	 * Return assigned security teams for specific facility
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<SecurityTeam> assigned security teams fot given facility
	 * @throw FacilityNotExistsException When Facility with given <code>id</code> doesn't exists.
	 */
	getAssignedSecurityTeams {
		@Override
		public List<SecurityTeam> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getFacilitiesManager().getAssignedSecurityTeams(ac.getSession(), getFacility(ac, parms));
		}
	},

	/*#
	 * Assign given security team to given facility (means the facility trusts the security team)
	 *
	 * @param facilityName String Facility name
	 * @param securityTeam int SecurityTeam <code>id</code>
	 * @throw SecurityTeamAlreadyAssignedException When SecurityTeam with given <code>id</code> is already assigned.
	 * @throw SecurityTeamNotExistsException When SecurityTeam with given <code>id</code> doesn't exists.
	 * @throw FacilityNotExistsException When Facility with given name doesn't exists.
	 */
	/*#
	 * Assign given security team to given facility (means the facility trusts the security team)
	 *
	 * @param facility int Facility <code>id</code>
	 * @param securityTeam int SecurityTeam <code>id</code>
	 * @throw SecurityTeamAlreadyAssignedException When SecurityTeam with given <code>id</code> is already assigned.
	 * @throw SecurityTeamNotExistsException When SecurityTeam with given <code>id</code> doesn't exists.
	 * @throw FacilityNotExistsException When Facility with given <code>id</code> doesn't exists.
	 */
	assignSecurityTeam {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getFacilitiesManager().assignSecurityTeam(ac.getSession(), getFacility(ac, parms),
					ac.getSecurityTeamById(parms.readInt("securityTeam")));
			return null;
		}
	},

	/*#
	 * Remove (Unassign) given security team from given facility
	 *
	 * @param facilityName String Facility name
	 * @param securityTeam int SecurityTeam <code>id</code>
	 * @throw SecurityTeamNotAssignedException When SecurityTeam with given <code>id</code> is not assigned.
	 * @throw SecurityTeamNotExistsException When SecurityTeam with given <code>id</code> doesn't exists.
	 * @throw FacilityNotExistsException When Facility with given name doesn't exists.
	 */
	/*#
	 * Remove (Unassign) given security team from given facility
	 *
	 * @param facility int Facility <code>id</code>
	 * @param securityTeam int SecurityTeam <code>id</code>
	 * @throw SecurityTeamNotAssignedException When SecurityTeam with given <code>id</code> is not assigned.
	 * @throw SecurityTeamNotExistsException When SecurityTeam with given <code>id</code> doesn't exists.
	 * @throw FacilityNotExistsException When Facility with given <code>id</code> doesn't exists.
	 */
	removeSecurityTeam {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getFacilitiesManager().removeSecurityTeam(ac.getSession(), getFacility(ac, parms),
					ac.getSecurityTeamById(parms.readInt("securityTeam")));
			return null;
		}
	},

	/*#
	 *  Set ban for user on facility.
	 *
	 * @param banOnFacility BanOnFacility JSON object
	 * @return BanOnFacility Created banOnFacility
	 */
	setBan {

		@Override
		public BanOnFacility call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			return ac.getFacilitiesManager().setBan(ac.getSession(),
					parms.read("banOnFacility", BanOnFacility.class));

		}
	},

	/*#
	 *  Get Ban for user on facility by it's id.
	 *
	 * @param banId int BanOnFacility <code>id</code>
	 * @return BanOnFacility banOnFacility
	 */
	getBanById {

		@Override
		public BanOnFacility call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getBanById(ac.getSession(),
					parms.readInt("banId"));

		}
	},

	/*#
	 *  Get ban by userId and facilityId.
	 *
	 * @param userId int User <code>id</code>
	 * @param facilityId int Facility <code>id</code>
	 * @return BanOnFacility banOnFacility
	 */
	getBan {

		@Override
		public BanOnFacility call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getBan(ac.getSession(),
					parms.readInt("userId"), parms.readInt("facilityId"));

		}
	},

	/*#
	 * Get all bans for user on any facility.
	 *
	 * @param userId int User <code>id</code>
	 * @return List<BanOnFacility> userBansOnFacilities
	 */
	getBansForUser {

		@Override
		public List<BanOnFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getBansForUser(ac.getSession(),
					parms.readInt("userId"));

		}
	},

	/*#
	 * Get all bans for user on the facility.
	 *
	 * @param facilityId int Facility <code>id</code>
	 * @return List<BanOnFacility> usersBansOnFacility
	 */
	getBansForFacility {

		@Override
		public List<BanOnFacility> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getFacilitiesManager().getBansForFacility(ac.getSession(),
					parms.readInt("facilityId"));

		}
	},

	/*#
	 * Update existing ban (description, validation timestamp)
	 *
	 * @param banOnFacility BanOnFacility JSON object
	 * @return BanOnFacility updated banOnFacility
	 */
	updateBan {

		@Override
		public BanOnFacility call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			return ac.getFacilitiesManager().updateBan(ac.getSession(),
					parms.read("banOnFacility", BanOnFacility.class));

		}
	},

	/*#
	 * Remove specific ban by it's id.
	 *
	 * @param banId int BanOnFacility <code>id</code>
	 */
	/*#
	 * Remove specific ban by userId and facilityId.
	 *
	 * @param userId int User <code>id</code>
	 * @param facilityId int Facility <code>id</code>
	 */
	removeBan {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if(parms.contains("banId")) {
				ac.getFacilitiesManager().removeBan(ac.getSession(),
					parms.readInt("banId"));
			} else {
				ac.getFacilitiesManager().removeBan(ac.getSession(),
					parms.readInt("userId"), parms.readInt("facilityId"));
			}
			return null;
		}
	};
	
	private static Facility getFacility(ApiCaller ac, Deserializer parms) throws PerunException {
		if (parms.contains("facilityName")) return ac.getFacilityByName(parms.readString("facilityName"));
		else return ac.getFacilityById(parms.readInt("facility"));
	}
}
