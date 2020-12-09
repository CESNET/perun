package cz.metacentrum.perun.rpc.methods;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServiceAttributes;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum ServicesManagerMethod implements ManagerMethod {

	/*#
	 * Bans service on a facility.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @throw ServiceAlreadyBannedException When service is already banned on facility.
	 */
	blockServiceOnFacility {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

			ac.getServicesManager().blockServiceOnFacility(ac.getSession(), ac.getServiceById(parms.readInt("service")),
				ac.getFacilityById(parms.readInt("facility")));
			return null;
		}
	},

	/*#
	 * Bans Service on a destination.
	 *
	 * @param service int Service <code>id</code>
	 * @param destination int Destination <code>id</code>
	 */
	/*#
	 * Bans Service on a destination.
	 *
	 * @param service int Service <code>id</code>
	 * @param destinationName String Destination name (like hostnames)
	 * @param destinationType String Destination type (like host, user@host, user@host:port, email, service-specific, ...)
	 */
	blockServiceOnDestination {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {

			if (parms.contains("destination")) {
				ac.getServicesManager().blockServiceOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")),parms.readInt("destination"));
			} else {
				ac.getServicesManager().blockServiceOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")), ac.getServicesManager().getDestinationIdByName(ac.getSession(), parms.readString("destinationName"), parms.readString("destinationType")));
			}
			return null;
		}
	},

	/*#
	 * Block all services currently assigned on this facility.
	 * Newly assigned services are still allowed for propagation.
	 *
	 * @param facility int Facility <code>id</code>
	 */
	blockAllServicesOnFacility {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getServicesManager().blockAllServicesOnFacility(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
			return null;
		}
	},

	/*#
	 * Block all services currently assigned on this destination.
	 * Newly assigned services are still allowed for propagation.
	 *
	 * @param destination int Destination <code>id</code>
	 */
	/*#
	 * Block all services currently assigned on this destination.
	 * Newly assigned services are still allowed for propagation.
	 *
	 * @param destinationName String Destination name (like hostnames)
	 * @param destinationType String Destination type (like host, user@host, user@host:port, email, service-specific, ...)
	 */
	blockAllServicesOnDestination {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("destination")) {
				ac.getServicesManager().blockAllServicesOnDestination(ac.getSession(), parms.readInt("destination"));
			} else {
				ac.getServicesManager().blockAllServicesOnDestination(ac.getSession(), ac.getServicesManager().getDestinationIdByName(ac.getSession(), parms.readString("destinationName"), parms.readString("destinationType")));
			}
			return null;
		}
	},

	/*#
	 * Returns list of denials for a facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<Service> Services
	 */
	getServicesBlockedOnFacility {
		public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServicesBlockedOnFacility(ac.getSession(),
				ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Returns list of denials for a destination.
	 *
	 * @param destination int Destination <code>id</code>
	 * @return List<Service> Services
	 */
	getServicesBlockedOnDestination {
		public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServicesBlockedOnDestination(ac.getSession(),parms.readInt("destination"));
		}
	},

	/*#
	 * Is this Service denied on the facility?
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @exampleResponse 1
	 * @return int 1 = true - the Service is denied on the facility, 0 = false - the Service is NOT denied on the facility
	 */
	isServiceBlockedOnFacility {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (ac.getServicesManager().isServiceBlockedOnFacility(ac.getSession(), ac.getServiceById(parms.readInt("service")),
				ac.getFacilityById(parms.readInt("facility"))))
				return 1;
			else return 0;
		}
	},

	/*#
	 * Is this Service denied on the destination?
	 *
	 * @param service int Service <code>id</code>
	 * @param destination int Destination <code>id</code>
	 * @exampleResponse 1
	 * @return int 1 = true - the Service is denied on the destination, 0 = false - the Service is NOT denied on the destination
	 */
	isServiceBlockedOnDestination {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (ac.getServicesManager().isServiceBlockedOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")),parms.readInt("destination")))
				return 1;
			else return 0;
		}
	},

	/*#
	 * Erase all the possible denials on this facility.
	 *
	 * @param facility int Facility <code>id</code>
	 */
	unblockAllServicesOnFacility {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getServicesManager().unblockAllServicesOnFacility(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
			return null;
		}
	},

	/*#
	 * Erase all the possible denials on this destination.
	 *
	 * @param destination int Destination <code>id</code>
	 */
	/*#
	 * Erase all the possible denials on this destination.
	 *
	 * @param destinationName String Destination name (like hostnames)
	 * @param destinationType String Destination type (like host, user@host, user@host:port, email, service-specific, ...)
	 */
	unblockAllServicesOnDestination {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("destination")) {
				ac.getServicesManager().unblockAllServicesOnDestination(ac.getSession(), parms.readInt("destination"));
			} else if (parms.contains("destinationName")) {
				if(parms.contains("destinationType")) {
					ac.getServicesManager().unblockAllServicesOnDestination(ac.getSession(), ac.getServicesManager().getDestinationIdByName(ac.getSession(), parms.readString("destinationName"), parms.readString("destinationType")));
				} else {
					ac.getServicesManager().unblockAllServicesOnDestination(ac.getSession(), parms.readString("destinationName"));
				}
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "destination (id) or destinationName (text)");
			}
			return null;
		}
	},

	/*#
	 * Free the denial of the Service on this facility. If the Service was banned
	 * on this facility, it will be freed. In case the Service was not banned on
	 * this facility, nothing will happen.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 */
	unblockServiceOnFacility {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getServicesManager().unblockServiceOnFacility(ac.getSession(), ac.getServiceById(parms.readInt("service")),
				ac.getFacilityById(parms.readInt("facility")));
			return null;
		}
	},

	/*#
	 * Free the denial of the Service on this destination. If the Service was banned on
	 * this destination, it will be freed. In case the Service was not banned on this
	 * destination, nothing will happen.
	 *
	 * @param service int Service <code>id</code>
	 * @param destination int Destination <code>id</code>
	 */
	/*#
	 * Free the denial of the Service on this destination. If the Service was banned on
	 * this destination, it will be freed. In case the Service was not banned on this
	 * destination, nothing will happen.
	 *
	 * @param service int Service <code>id</code>
	 * @param destinationName String Destination name (like hostnames)
	 * @param destinationType String Destination type (like host, user@host, user@host:port, email, service-specific, ...)
	 */
	unblockServiceOnDestination {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("destination")) {
				ac.getServicesManager().unblockServiceOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")), parms.readInt("destination"));
			} else {
				ac.getServicesManager().unblockServiceOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")), ac.getServicesManager().getDestinationIdByName(ac.getSession(), parms.readString("destinationName"), parms.readString("destinationType")));
			}
			return null;
		}
	},

	/*#
	 * Forces service propagation on defined facility.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return int 1 = true if it is possible, 0 = false if not
	 */
	/*#
	 * Forces service propagation on defined facility.
	 *
	 * @param service int Service <code>id</code>
	 * @return int 1 = true if it is possible, 0 = false if not
	 */
	forceServicePropagation {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("facility")) {
				if(ac.getServicesManager().forceServicePropagation(
					ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					ac.getServiceById(parms.readInt("service")))) return 1;
				else return 0;
			} else {
				if(ac.getServicesManager().forceServicePropagation(
					ac.getSession(),
					ac.getServiceById(parms.readInt("service")))) return 1;
				else return 0;
			}
		}
	},

	/*#
	 * Plans service propagation on defined facility.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return int 1 = true if it is possible, 0 = false if not
	 */
	/*#
	 * Plans service propagation on defined facility.
	 *
	 * @param service int Service <code>id</code>
	 * @return int 1 = true if it is possible, 0 = false if not
	 */
	planServicePropagation {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("facility")) {
				if(ac.getServicesManager().planServicePropagation(
					ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					ac.getServiceById(parms.readInt("service")))) return 1;
				else return 0;
			} else {
				if(ac.getServicesManager().planServicePropagation(
					ac.getSession(),
					ac.getServiceById(parms.readInt("service")))) return 1;
				else return 0;
			}
		}
	},

	/*#
	 * Return list of ServiceForGUI assigned on facility, (Service with "allowedOnFacility" property filled).
	 * 1 - allowed / 0 - service is service is denied.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<ServiceForGUI> list of assigned services with allowed property
	 */
	getFacilityAssignedServicesForGUI {
		public List<ServiceForGUI> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getServicesManager().getFacilityAssignedServicesForGUI(ac.getSession(), ac.getFacilityById(parms.readInt("id")));

		}
	},

	/*#
	 * Creates a new service.
	 *
	 * Service object must contain name. Parameters desctiption, script, delay, recurrence, enabled are optional. Other parameters ignored.
	 *
	 * @param service Service JSON object
	 * @return Service Created Service
	 * @exampleParam service { "name" : "New Service"}
	 */
	/*#
	 * Creates a new service.
	 *
	 * @param name String name
	 * @param description String description
	 * @param script String script which should be constructed like ./service_name (where anything else than [a-z,A-Z] is converted to _)
	 * @return Service Created Service
	 * @exampleParam name "New Service"
	 * @exampleParam description "The new description with information"
	 * @exampleParam script "./service_name"
	 */
	createService {

		@Override
		public Service call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("service")) {
				return ac.getServicesManager().createService(ac.getSession(),
						parms.read("service", Service.class));
			} else if (parms.contains("name") && parms.contains("description") && parms.contains("script")) {
				String name = parms.readString("name");
				String description = parms.readString("description");
				Service service = new Service(0, name, description);
				service.setScript(parms.readString("script"));
				return ac.getServicesManager().createService(ac.getSession(), service);
			} else {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER);
			}
		}
	},

	/*#
	 * Deletes a service.
	 *
	 * @param service int Service <code>id</code>
	 */
	deleteService {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().deleteService(ac.getSession(),
					ac.getServiceById(parms.readInt("service")));
			return null;
		}
	},

	/*#
	 * Updates a service.
	 *
	 * @param service Service JSON object
	 */
	updateService {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().updateService(ac.getSession(),
					parms.read("service", Service.class));
			return null;
		}
	},

	/*#
	 * Returns a service by its <code>id</code>.
	 *
	 * @param id int Service <code>id</code>
	 * @return Service Found Service
	 */
	getServiceById {

		@Override
		public Service call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServiceById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Returns a service by its name.
	 *
	 * @param name String Service Name
	 * @return Service Found Service
	 */
	getServiceByName {

		@Override
		public Service call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServiceByName(ac.getSession(),
					parms.readString("name"));
		}
	},

	/*#
	 * Returns all services.
	 *
	 * @return List<Service> All services
	 */
	getServices {

		@Override
		public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServices(ac.getSession());
		}
	},

	/*#
	 * Get all services with given attribute.
	 *
	 * @param attributeDefinition int attributeDefinition <code>id</code>
	 * @return all services with given attribute
	 */
	getServicesByAttributeDefinition {

		@Override
		public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServicesByAttributeDefinition(ac.getSession(),
					ac.getAttributeDefinitionById(parms.readInt("attributeDefinition")));
		}
	},

	/*#
	 * Generates the list of attributes per each member associated with the resource.
	 *
	 * @deprecated use getHashedDataWithGroups
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>. You will get attributes for this facility, resources associated with it and members assigned to the resources.
	 * @param filterExpiredMembers if true the method does not take members expired in groups into account
	 * @return List<ServiceAttributes> Attributes in special structure. Facility is in the root, facility children are resources. And resource children are members.
	 <pre>
	 Facility
	 +---Attrs
	 +---ChildNodes
	 +------Resource
	 |      +---Attrs
	 |      +---ChildNodes
	 |             +------Member
	 |             |        +-------Attrs
	 |             +------Member
	 |             |        +-------Attrs
	 |             +...
	 |
	 +------Resource
	 |      +---Attrs
	 |      +---ChildNodes
	 .             +------Member
	 .             |        +-------Attrs
	 .             +------Member
	 |        +-------Attrs
	 +...
	 </pre>
	 */
	/*#
	 * Generates the list of attributes per each member associated with the resource.
	 *
	 * @deprecated use getHashedDataWithGroups
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>. You will get attributes for this facility, resources associated with it and members assigned to the resources.
	 * @return List<ServiceAttributes> Attributes in special structure. Facility is in the root, facility children are resources. And resource children are members.
	 <pre>
	 Facility
	 +---Attrs
	 +---ChildNodes
	 +------Resource
	 |      +---Attrs
	 |      +---ChildNodes
	 |             +------Member
	 |             |        +-------Attrs
	 |             +------Member
	 |             |        +-------Attrs
	 |             +...
	 |
	 +------Resource
	 |      +---Attrs
	 |      +---ChildNodes
	 .             +------Member
	 .             |        +-------Attrs
	 .             +------Member
	 |        +-------Attrs
	 +...
	 </pre>
	 */
	getHierarchicalData {

		@Override
		public ServiceAttributes call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("filterExpiredMembers")) {
				return ac.getServicesManager().getHierarchicalData(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readBoolean("filterExpiredMembers"));
			} else {
				return ac.getServicesManager().getHierarchicalData(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")),
					false);
			}
		}
	},

	/*#
	 * Generates hashed hierarchical data structure for given service and facility.
	 *
	 * attributes: {...hashes...}
	 * hierarchy: {
	 *   "1": {    ** facility id **
	 *     members: {    ** all members on the facility **
	 *        "4" : 5,    ** member id : user id **
	 *        "6" : 7,    ** member id : user id **
	 *       ...
	 *     }
	 *     children: [
	 *       "2": {    ** resource id **
	 *         children: [],
	 *         voId: 99,
	 *         members: {    ** all members on the resource with id 2 **
	 *           "4" : 5    ** member id : user id **
	 *         }
	 *       },
	 *       "3": {
	 *         ...
	 *       }
	 *     ]
	 *   }
	 * }
	 *
	 * @param service Integer service
	 * @param facility Integer facility
	 * @param filterExpiredMembers Boolean if the generator should filter expired members
	 * @return HashedGenData generated hashed data structure
	 * @throw FacilityNotExistsException if there is no such facility
	 * @throw ServiceNotExistsException if there is no such service
	 * @throw PrivilegeException insufficient permissions
	 */
	/*#
	 * Generates hashed hierarchical data structure for given service and facility.
	 *
	 * attributes: {...hashes...}
	 * hierarchy: {
	 *   "1": {    ** facility id **
	 *     members: {    ** all members on the facility **
	 *        "4" : 5,    ** member id : user id **
	 *        "6" : 7,    ** member id : user id **
	 *       ...
	 *     }
	 *     children: [
	 *       "2": {    ** resource id **
	 *         children: [],
	 *         voId: 99,
	 *         members: {    ** all members on the resource with id 2 **
	 *           "4" : 5    ** member id : user id **
	 *         }
	 *       },
	 *       "3": {
	 *         ...
	 *       }
	 *     ]
	 *   }
	 * }
	 *
	 * @param service Integer service
	 * @param facility Integer facility
	 * @return HashedGenData generated hashed data structure
	 * @throw FacilityNotExistsException if there is no such facility
	 * @throw ServiceNotExistsException if there is no such service
	 * @throw PrivilegeException insufficient permissions
	 */
	getHashedHierarchicalData {
		@Override
		public HashedGenData call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("filterExpiredMembers")) {
				return ac.getServicesManager().getHashedHierarchicalData(ac.getSession(),
						ac.getServiceById(parms.readInt("service")),
						ac.getFacilityById(parms.readInt("facility")),
						parms.readBoolean("filterExpiredMembers"));
			} else {
				return ac.getServicesManager().getHashedHierarchicalData(ac.getSession(),
						ac.getServiceById(parms.readInt("service")),
						ac.getFacilityById(parms.readInt("facility")),
						false);
			}
		}
	},

	/*#
	 * Generates hashed data with group structure for given service and resource.
	 *
	 * Generates data in format:
	 *
	 * attributes: {...hashes...}
	 * hierarchy: {
	 *   "1": {    ** facility id **
	 *     members: {    ** all members on the facility **
	 *        "4" : 5,    ** member id : user id **
	 *        "6" : 7,    ** member id : user id **
	 *       ...
	 *     }
	 *     children: [
	 *       "2": {    ** resource id **
	 *         voId: 99,
	 *         children: [
	 *           "89": {    ** group id **
	 *              "children": {},
	 *              "members": {
	 *                  "91328": 57986,
	 *                  "91330": 60838
	 *              }
	 *           }
	 *         ],
	 *         "members": {    ** all members on the resource with id 2 **
	 *             "91328": 57986,
	 *             "91330": 60838
	 *         }
	 *       },
	 *       "3": {
	 *         ...
	 *       }
	 *     ]
	 *   }
	 * }
	 *
	 * @param service Integer service
	 * @param facility Integer facility
	 * @param filterExpiredMembers Boolean if the generator should filter expired members
	 * @return HashedGenData generated hashed data structure
	 * @throw FacilityNotExistsException if there is no such facility
	 * @throw ServiceNotExistsException if there is no such service
	 * @throw PrivilegeException insufficient permissions
	 */
	/*#
	 * Generates hashed data with group structure for given service and resource.
	 *
	 *  Generates data in format:
	 *
	 * attributes: {...hashes...}
	 * hierarchy: {
	 *   "1": {    ** facility id **
	 *     members: {    ** all members on the facility **
	 *        "4" : 5,    ** member id : user id **
	 *        "6" : 7,    ** member id : user id **
	 *       ...
	 *     }
	 *     children: [
	 *       "2": {    ** resource id **
	 *         voId: 99,
	 *         children: [
	 *           "89": {    ** group id **
	 *              "children": {},
	 *              "members": {
	 *                  "91328": 57986,
	 *                  "91330": 60838
	 *              }
	 *           }
	 *         ],
	 *         "members": {    ** all members on the resource with id 2 **
	 *             "91328": 57986,
	 *             "91330": 60838
	 *         }
	 *       },
	 *       "3": {
	 *         ...
	 *       }
	 *     ]
	 *   }
	 * }
	 *
	 * @param service Integer service
	 * @param facility Integer facility
	 * @return HashedGenData generated hashed data structure
	 * @throw FacilityNotExistsException if there is no such facility
	 * @throw ServiceNotExistsException if there is no such service
	 * @throw PrivilegeException insufficient permissions
	 */
	getHashedDataWithGroups {
		@Override
		public HashedGenData call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("filterExpiredMembers")) {
				return ac.getServicesManager().getHashedDataWithGroups(ac.getSession(),
						ac.getServiceById(parms.readInt("service")),
						ac.getFacilityById(parms.readInt("facility")),
						parms.readBoolean("filterExpiredMembers"));
			} else {
				return ac.getServicesManager().getHashedDataWithGroups(ac.getSession(),
						ac.getServiceById(parms.readInt("service")),
						ac.getFacilityById(parms.readInt("facility")),
						false);
			}
		}
	},

	/*#
	 * Generates the list of attributes per each user and per each resource. Never return member or member-resource attribute.
	 *
	 * @deprecated use getHashedHierarchicalData
	 * @param service int Service <code>id</code>. You will get attributes required by this service
	 * @param facility int Facility <code>id</code>. You will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @param filterExpiredMembers if true the method does not take members expired in groups into account
	 * @return ServiceAttributes Attributes in special structure. The facility is in the root. Facility first children is abstract node which contains no attributes and it's children are all resources. Facility second child is abstract node with no attribute and it's children are all users.
	 <pre>
	 Facility
	 +---Attrs
	 +---ChildNodes
	 +------()
	 |      +---ChildNodes
	 |             +------Resource
	 |             |        +-------Attrs
	 |             +------Resource
	 |             |        +-------Attrs
	 |             +...
	 |
	 +------()
	 +---ChildNodes
	 +------User
	 |        +-------Attrs (do NOT return member, member-resource attributes)
	 +------User
	 |        +-------Attrs (do NOT return member, member-resource attributes)
	 +...
	 </pre>

	 *
	 */
	/*#
	 * Generates the list of attributes per each user and per each resource. Never return member or member-resource attribute.
	 *
	 * @deprecated use getHashedDataWithGroups
	 * @param service int Service <code>id</code>. You will get attributes required by this service
	 * @param facility int Facility <code>id</code>. You will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @return ServiceAttributes Attributes in special structure. The facility is in the root. Facility first children is abstract node which contains no attributes and it's children are all resources. Facility second child is abstract node with no attribute and it's children are all users.
	 <pre>
	 Facility
	 +---Attrs
	 +---ChildNodes
	 +------()
	 |      +---ChildNodes
	 |             +------Resource
	 |             |        +-------Attrs
	 |             +------Resource
	 |             |        +-------Attrs
	 |             +...
	 |
	 +------()
	 +---ChildNodes
	 +------User
	 |        +-------Attrs (do NOT return member, member-resource attributes)
	 +------User
	 |        +-------Attrs (do NOT return member, member-resource attributes)
	 +...
	 </pre>
	 */
	getFlatData {

		@Override
		public ServiceAttributes call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("filterExpiredMembers")) {
				return ac.getServicesManager().getFlatData(ac.getSession(),
						ac.getServiceById(parms.readInt("service")),
						ac.getFacilityById(parms.readInt("facility")),
					parms.readBoolean("filterExpiredMembers"));
			} else {
				return ac.getServicesManager().getFlatData(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")),
					false);
			}
		}
	},

	/*#
	 * Generates the list of attributes per each member associated with the resources and groups.
	 *
	 * @deprecated use getHashedDataWithGroups
	 * @param service int Service <code>id</code>. You will get attributes reuqired by this service
	 * @param facility int Facility <code>id</code>. You will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @param filterExpiredMembers if true the method does not take members expired in groups into account
	 * @return ServiceAttributes Attributes in special structure. Facility is in the root, facility children are resources.
	 *         Resource first chil is abstract structure which children are groups.
	 *         Resource  second chi is abstract structure which children are members.
	 *         Group first chil is abstract structure which children are groups.
	 *         Group second chi is abstract structure which children are members.
	 <pre>
	 Facility
	 +---Attrs                       ...................................................
	 +---ChildNodes                  |                                                 .
	 +------Resource                 |                                                 .
	 |       +---Attrs               |                                                 .
	 |       +---ChildNodes          |                                                 .
	 |              +------()        V                                                 .
	 |              |       +------Group                                               .
	 |              |       |        +-------Attrs                                     .
	 |              |       |        +-------ChildNodes                                .
	 |              |       |                   +-------()                             .
	 |              |       |                   |        +---ChildNodes                .
	 |              |       |                   |               +------- GROUP (same structure as any other group)
	 |              |       |                   |               +------- GROUP (same structure as any other group)
	 |              |       |                   |               +...
	 |              |       |                   +-------()
	 |              |       |                            +---ChildNodes
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +...
	 |              |       |
	 |              |       +------Group
	 |              |       |        +-------Attrs
	 |              |       |        +-------ChildNodes
	 |              |       |                   +-------()
	 |              |       |                   |        +---ChildNodes
	 |              |       |                   |               +------- GROUP (same structure as any other group)
	 |              |       |                   |               +------- GROUP (same structure as any other group)
	 |              |       |                   |               +...
	 |              |       |                   +-------()
	 |              |       |                            +---ChildNodes
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +...
	 |              |       |
	 |              |       +...
	 |              |
	 |              +------()
	 |                      +------Member
	 |                      |         +----Attrs
	 |                      |
	 |                      +------Member
	 |                      |         +----Attrs
	 |                      +...
	 |
	 +------Resource
	 |       +---Attrs
	 |       +---ChildNodes
	 |              +------()
	 |              |       +...
	 |              |       +...
	 |              |
	 |              +------()
	 |                      +...
	 .                      +...
	 .
	 .
	 </pre>
	 */
	/*#
	 * Generates the list of attributes per each member associated with the resources and groups.
	 *
	 * @deprecated use getHashedDataWithGroups
	 * @param service int Service <code>id</code>. You will get attributes reuqired by this service
	 * @param facility int Facility <code>id</code>. You will get attributes for this facility, resources associated with it and members assigned to the resources
	 * @return ServiceAttributes Attributes in special structure. Facility is in the root, facility children are resources.
	 *         Resource first child is abstract structure which children are groups.
	 *         Resource  second child is abstract structure which children are members.
	 *         Group first child is empty structure (services expect members to be second child, here used to be subgroups).
	 *         Group second child is abstract structure which children are members.
	 <pre>
	 Facility
	 +---Attrs                       ...................................................
	 +---ChildNodes                  |                                                 .
	 +------Resource                 |                                                 .
	 |       +---Attrs               |                                                 .
	 |       +---ChildNodes          |                                                 .
	 |              +------()        V                                                 .
	 |              |       +------Group                                               .
	 |              |       |        +-------Attrs                                     .
	 |              |       |        +-------ChildNodes                                .
	 |              |       |                   +-------()                             .
	 |              |       |                   +-------()
	 |              |       |                            +---ChildNodes
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +...
	 |              |       |
	 |              |       +------Group
	 |              |       |        +-------Attrs
	 |              |       |        +-------ChildNodes
	 |              |       |                   +-------()
	 |              |       |                   +-------()
	 |              |       |                            +---ChildNodes
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +------Member
	 |              |       |                                   |        +----Attrs
	 |              |       |                                   +...
	 |              |       |
	 |              |       +...
	 |              |
	 |              +------()
	 |                      +------Member
	 |                      |         +----Attrs
	 |                      |
	 |                      +------Member
	 |                      |         +----Attrs
	 |                      +...
	 |
	 +------Resource
	 |       +---Attrs
	 |       +---ChildNodes
	 |              +------()
	 |              |       +...
	 |              |       +...
	 |              |
	 |              +------()
	 |                      +...
	 .                      +...
	 .
	 .
	 </pre>
	 */
	getDataWithGroups {

		@Override
		public ServiceAttributes call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("filterExpiredMembers")) {
				return ac.getServicesManager().getDataWithGroups(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readBoolean("filterExpiredMembers"));
			} else {
				return ac.getServicesManager().getDataWithGroups(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")),
					false);
			}
		}
	},

	/*#
	 * Generates the list of attributes per each member associated with the resources and groups in vos.
	 *
	 * @deprecated use getHashedDataWithGroups
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, vos associated with this facility by resources, resources associated with it and members assigned to the resources
	 * @param filterExpiredMembers if true the method does not take members expired in groups into account
	 * @return attributes in special structure.
	 *        Facility is in the root, facility children are vos.
	 *        Vo first child is abstract structure which children are resources.
	 *        Resource first child is abstract structure which children are groups.
	 *        Resource  second chi is abstract structure which children are members.
	 *        Group first child is abstract structure which children are groups.
	 *        Group second chi is abstract structure which children are members.
	 <pre>
	 Facility
	 +---Attrs
	 +---ChildNodes
	        +-----Vo
	        |      +---Attrs
	        |      +---ChildNodes
	        |             +-------Resource
	        |             |       +---Attrs               |-------------------------------------------------.
	        |             |       +---ChildNodes          |                                                 .
	        |             |              +------()        V                                                 .
	        |             |              |       +------Group                                               .
	        |             |              |       |        +-------Attrs                                     .
	        |             |              |       |        +-------ChildNodes                                .
	        |             |              |       |                   +-------()                             .
	        |             |              |       |                   |        +---ChildNodes                .
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +...
	        |             |              |       |                   +-------()
	        |             |              |       |                            +---ChildNodes
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +...
	        |             |              |       |
	        |             |              |       +------Group
	        |             |              |       |        +-------Attrs
	        |             |              |       |        +-------ChildNodes
	        |             |              |       |                   +-------()
	        |             |              |       |                   |        +---ChildNodes
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +...
	        |             |              |       |                   +-------()
	        |             |              |       |                            +---ChildNodes
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +...
	        |             |              |       |
	        |             |              |       +...
	        |             |              |
	        |             |              +------()
	        |             |                      +------Member
	        |             |                      |         +----Attrs
	        |             |                      |
	        |             |                      +------Member
	        |             |                      |         +----Attrs
	        |             |                      +...
	        |             |
	        |             +------Resource
	        |             |       +---Attrs
	        |             |       +---ChildNodes
	        |             |              +------()
	        |             |              |       +...
	        |             |              |       +...
	        |             |              |
	        |             |              +------()
	        |             |                      +...
	        +-----Vo ....
	</pre>
	 */
	/*#
	 * Generates the list of attributes per each member associated with the resources and groups in vos.
	 *
	 * @deprecated use getHashedDataWithGroups
	 * @param service attributes required by this service you will get
	 * @param facility you will get attributes for this facility, vos associated with this facility by resources, resources associated with it and members assigned to the resources
	 * @return attributes in special structure.
	 *        Facility is in the root, facility children are vos.
	 *        Vo first child is abstract structure which children are resources.
	 *        Resource first child is abstract structure which children are groups.
	 *        Resource  second chi is abstract structure which children are members.
	 *        Group first child is abstract structure which children are groups.
	 *        Group second chi is abstract structure which children are members.
	 <pre>
	 Facility
	 +---Attrs
	 +---ChildNodes
	        +-----Vo
	        |      +---Attrs
	        |      +---ChildNodes
	        |             +-------Resource
	        |             |       +---Attrs               |-------------------------------------------------.
	        |             |       +---ChildNodes          |                                                 .
	        |             |              +------()        V                                                 .
	        |             |              |       +------Group                                               .
	        |             |              |       |        +-------Attrs                                     .
	        |             |              |       |        +-------ChildNodes                                .
	        |             |              |       |                   +-------()                             .
	        |             |              |       |                   |        +---ChildNodes                .
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +...
	        |             |              |       |                   +-------()
	        |             |              |       |                            +---ChildNodes
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +...
	        |             |              |       |
	        |             |              |       +------Group
	        |             |              |       |        +-------Attrs
	        |             |              |       |        +-------ChildNodes
	        |             |              |       |                   +-------()
	        |             |              |       |                   |        +---ChildNodes
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +------- GROUP (same structure as any other group)
	        |             |              |       |                   |               +...
	        |             |              |       |                   +-------()
	        |             |              |       |                            +---ChildNodes
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +------Member
	        |             |              |       |                                   |        +----Attrs
	        |             |              |       |                                   +...
	        |             |              |       |
	        |             |              |       +...
	        |             |              |
	        |             |              +------()
	        |             |                      +------Member
	        |             |                      |         +----Attrs
	        |             |                      |
	        |             |                      +------Member
	        |             |                      |         +----Attrs
	        |             |                      +...
	        |             |
	        |             +------Resource
	        |             |       +---Attrs
	        |             |       +---ChildNodes
	        |             |              +------()
	        |             |              |       +...
	        |             |              |       +...
	        |             |              |
	        |             |              +------()
	        |             |                      +...
	        +-----Vo ....
	</pre>
	 */
	getDataWithVos {
		@Override
		public ServiceAttributes call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("filterExpiredMembers")) {
				return ac.getServicesManager().getDataWithVos(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")),
					parms.readBoolean("filterExpiredMembers"));
			} else {
				return ac.getServicesManager().getDataWithVos(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")),
					false);
			}
		}
	},

	/*#
	 * Returns packages.
	 *
	 * @return List<ServicesPackage> Packages.
	 */
	getServicesPackages {

		@Override
		public List<ServicesPackage> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServicesPackages(ac.getSession());
		}
	},

	/*#
	 * Gets package by <code>id</code>.
	 *
	 * @param servicesPackageId int ServicesPackage <code>id</code>.
	 * @return ServicesPackage Found ServicesPackage
	 */
	getServicesPackageById {

		@Override
		public ServicesPackage call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServicesPackageById(ac.getSession(), parms.readInt("servicesPackageId"));
		}
	},

	/*#
	 * Gets package by name.
	 *
	 * @param name String ServicesPackage name.
	 * @return ServicesPackage Found ServicesPackage
	 */
	getServicesPackageByName {

		@Override
		public ServicesPackage call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServicesPackageByName(ac.getSession(),
					parms.readString("name"));
		}
	},

	/*#
	 * Creates a new services package.
	 *
	 * @param servicesPackage ServicesPackage JSON object.
	 * @return ServicesPackage Created ServicesPackage
	 */
	/*#
	 * Creates a new services package.
	 *
	 * @param name String name
	 * @param description String description
	 * @return ServicesPackage Created ServicesPackage
	 */
	createServicesPackage {

		@Override
		public ServicesPackage call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if (parms.contains("servicesPackage")) {
				return ac.getServicesManager().createServicesPackage(ac.getSession(),
						parms.read("servicesPackage", ServicesPackage.class));
			} else if (parms.contains("name") && parms.contains("description")) {
				String name = parms.readString("name");
				String description = parms.readString("description");
				ServicesPackage servicesPackage = new ServicesPackage(0, name, description);
				return ac.getServicesManager().createServicesPackage(ac.getSession(), servicesPackage);
			} else {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER);
			}
		}
	},

	/*#
	 * Deletes a services package.
	 *
	 * @param servicesPackage int ServicesPackage <code>id</code>
	 */
	deleteServicesPackage {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().deleteServicesPackage(ac.getSession(),
					ac.getServicesPackageById(parms.readInt("servicesPackage")));
			return null;
		}
	},

	/*#
	 * Updates a service package.
	 *
	 * @param servicesPackage ServicesPackage JSON object.
	 */
	updateServicesPackage {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().updateServicesPackage(ac.getSession(),
					parms.read("servicesPackage", ServicesPackage.class));
			return null;
		}
	},

	/*#
	 * Adds a Service to a Services Package.
	 *
	 * @param servicesPackage int Services package <code>id</code> to which the service supposed to be added
	 * @param service int Service <code>id</code> to be added to the services package
	 */
	addServiceToServicesPackage {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().addServiceToServicesPackage(ac.getSession(),
					ac.getServicesPackageById(parms.readInt("servicesPackage")),
					ac.getServiceById(parms.readInt("service")));
			return null;
		}
	},

	/*#
	 * Removes a Service from a Services Package.
	 *
	 * @param servicesPackage int Services package <code>id</code> from which the service supposed to be removed
	 * @param service int Service <code>id</code> that will be removed from the services package
	 */
	removeServiceFromServicesPackage {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().removeServiceFromServicesPackage(ac.getSession(),
					ac.getServicesPackageById(parms.readInt("servicesPackage")),
					ac.getServiceById(parms.readInt("service")));
			return null;
		}
	},

	/*#
	 * Lists services stored in a package.
	 *
	 * @param servicesPackage int ServicesPackage <code>id</code>
	 * @return List<Service> List of services
	 */
	getServicesFromServicesPackage {

		@Override
		public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getServicesFromServicesPackage(ac.getSession(),
					ac.getServicesPackageById(parms.readInt("servicesPackage")));
		}
	},

	/*#
	 * Mark the attribute as required for the service. Required attributes are requisite for Service to run.
	 * If you add attribute which has a default attribute then this default attribute will be automatically add too.
	 *
	 * @param service int Service <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	addRequiredAttribute {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().addRequiredAttribute(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getAttributeDefinitionById(parms.readInt("attribute")));
			return null;
		}
	},

	/*#
	 * Batch version of addRequiredAttribute.
	 *
	 * @param service int Service <code>id</code>
	 * @param attributes int[] Attribute IDs
	 */
	addRequiredAttributes {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			int[] ids = parms.readArrayOfInts("attributes");
			List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>(ids.length);

			for (int i : ids) {
				attributes.add(ac.getAttributeDefinitionById(i));
			}

			ac.getServicesManager().addRequiredAttributes(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					attributes);
			return null;
		}
	},

	/*#
	 * Remove required attribute from service.
	 *
	 * @param service int Service <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	removeRequiredAttribute {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().removeRequiredAttribute(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getAttributeDefinitionById(parms.readInt("attribute")));
			return null;
		}
	},

	/*#
	 * Remove required attributes from service.
	 *
	 * @param service int Service <code>id</code>
	 * @param attributes int[] Attribute IDs
	 */
	removeRequiredAttributes {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			int[] ids = parms.readArrayOfInts("attributes");
			List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>(ids.length);

			for (int i : ids) {
				attributes.add(ac.getAttributeDefinitionById(i));
			}

			ac.getServicesManager().removeRequiredAttributes(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					attributes);
			return null;
		}
	},

	/*#
	 * Remove all required attributes from service.
	 *
	 * @param service int Service <code>id</code>
	 */
	removeAllRequiredAttributes {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().removeAllRequiredAttributes(ac.getSession(),
					ac.getServiceById(parms.readInt("service")));
			return null;
		}
	},

	/*#
	 * Returns a destination by its <code>id</code>.
	 *
	 * @param id int Destination <code>id</code>
	 * @return Destination Found Destination
	 */
	getDestinationById {

		@Override
		public Destination call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getDestinationById(ac.getSession(),
					parms.readInt("id"));
		}
	},

	/*#
	 * Returns list of all destinations defined for the service and facility.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return List<Destination> Found Destinations
	 */
	/*#
	 * Get list of all destinations.
	 *
	 * @return List<Destination> List of all destinations for session
	 */
	getDestinations {

		@Override
		public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				return ac.getServicesManager().getDestinations(ac.getSession(),
						ac.getServiceById(parms.readInt("service")),
						ac.getFacilityById(parms.readInt("facility")));
			} else {
				return ac.getServicesManager().getDestinations(ac.getSession());
			}
		}
	},

	/*#
	 * Returns all rich destinations defined for a facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<RichDestination> Found RichDestinations
	 */
	/*#
	 * Returns all rich destinations defined for a service.
	 *
	 * @param service int Service <code>id</code>
	 * @return List<RichDestination> Found RichDestinations
	 */
	getAllRichDestinations {
		public List<RichDestination> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				return ac.getServicesManager().getAllRichDestinations(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")));
			}else{
				return ac.getServicesManager().getAllRichDestinations(ac.getSession(),
						ac.getServiceById(parms.readInt("service")));
			}
		}
	},

	/*#
	 * Returns list of all rich destinations defined for the service and facility.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return List<RichDestination> Found RichDestination
	 */
	getRichDestinations {
		public List<RichDestination> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getRichDestinations(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					ac.getServiceById(parms.readInt("service")));
		}
	},

	/*#
	 * Adds an destination for a facility and service. If destination doesn't exist it will be created.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @param destination String Destination
	 * @param type String Destination type (host,user@host,user@host:port,url,mail,service-specific)
	 * @return Destination Created destination.
	 */
	/*#
	 * Adds an destination for a facility and list of services. If destination doesn't exist it will be created.
	 *
	 * @param services List<Service> Services
	 * @param facility int Facility <code>id</code>
	 * @param destination String Destination
	 * @param type String Destination type (host,user@host,user@host:port,url,mail,service-specific)
	 * @return Destination Created destination.
	 */
	/*#
	 * Adds an destination for a facility and service. If destination doesn't exist it will be created.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @param destination String Destination
	 * @param type String Destination type (host,user@host,user@host:port,url,mail,service-specific)
	 * @param propagationType String propagation type (PARALLEL, DUMMY - doesn't send data)
	 * @return Destination Created destination.
	 */
	/*#
	 * Adds an destination for a facility and list of services. If destination doesn't exist it will be created.
	 *
	 * @param services List<Service> Services
	 * @param facility int Facility <code>id</code>
	 * @param destination String Destination
	 * @param type String Destination type (host,user@host,user@host:port,url,mail,service-specific)
	 * @param propagationType String propagation type (PARALLEL, DUMMY - doesn't send data)
	 * @return Destination Created destination.
	 */
	addDestination {

		@Override
		public Destination call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Destination destination;

			if(parms.contains("propagationType")) {
				destination = ac.getDestination(parms.readString("destination"), parms.readString("type"),
						parms.readString("propagationType"));
			} else {
				destination = ac.getDestination(parms.readString("destination"), parms.readString("type"));
			}
			if(parms.contains("services")) {
				return ac.getServicesManager().addDestination(ac.getSession(),
						parms.readList("services", Service.class),
						ac.getFacilityById(parms.readInt("facility")),
						destination);
			} else {
				return ac.getServicesManager().addDestination(ac.getSession(),
						ac.getServiceById(parms.readInt("service")),
						ac.getFacilityById(parms.readInt("facility")),
						destination);
			}


		}
	},

	/*#
	 * Adds destination for all services defined on the facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param destination String Destination
	 * @param type String String Destination type (host,user@host,user@host:port,url,mail,service-specific)
	 * @return List<Destinations> Added destinations
	 */
	/*#
	 * Adds destination for all services defined on the facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param destination String Destination
	 * @param type String Destination type (host,user@host,user@host:port,url,mail,service-specific)
	 * @param propagationType String propagation type (PARALLEL, DUMMY - doesn't send data)
	 * @return List<Destinations> Added destinations
	 */
	addDestinationsForAllServicesOnFacility {

		@Override
		public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			Destination destination;

			if(parms.contains("propagationType")) {
				destination = ac.getDestination(parms.readString("destination"), parms.readString("type"),
						parms.readString("propagationType"));
			} else {
				destination = ac.getDestination(parms.readString("destination"), parms.readString("type"));
			}
			return ac.getServicesManager().addDestinationsForAllServicesOnFacility(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")),
					destination);
		}
	},

	/*#
	 * Add services destinations for all services currently available on facility
	 * (assigned to all facility's resources). Destinations names are taken from
	 * all facility's host hostnames.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return List<Destinations> Added destinations
	 */
	/*#
	 * Add services destinations for list of services. Destinations names are taken from
	 * all facility's host hostnames.
	 *
	 * @param services List<Service> Services
	 * @param facility int Facility <code>id</code>
	 * @return List<Destinations> Added destinations
	 */
	/*#
	 * Add services destinations for one service. Destinations names are taken from
	 * all facility's host hostnames.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<Destinations> Added destinations
	 */
	addDestinationsDefinedByHostsOnFacility {

		@Override
		public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			if(parms.contains("service")) {
				return ac.getServicesManager().addDestinationsDefinedByHostsOnFacility(ac.getSession(),
						ac.getServiceById(parms.readInt("service")),
						ac.getFacilityById(parms.readInt("facility")));
			} else if (parms.contains("services")) {
				return ac.getServicesManager().addDestinationsDefinedByHostsOnFacility(ac.getSession(),
						parms.readList("services", Service.class),
						ac.getFacilityById(parms.readInt("facility")));
			} else {
				return ac.getServicesManager().addDestinationsDefinedByHostsOnFacility(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")));
			}

		}
	},

	/*#
	 * Removes an destination from a facility and service.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @param destination String Destination
	 * @param type String Type
	 */
	removeDestination {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().removeDestination(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")),
					ac.getDestination(parms.readString("destination"), parms.readString("type")));

			return null;
		}
	},

	/*#
	 * Removes all destinations from a facility and service.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 */
	removeAllDestinations {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();

			ac.getServicesManager().removeAllDestinations(ac.getSession(),
					ac.getServiceById(parms.readInt("service")),
					ac.getFacilityById(parms.readInt("facility")));

			return null;
		}
	},

	/*#
	 * List all destinations for all facilities which are joined by resources to the VO.
	 *
	 * @param vo int VO <code>id</code>
	 * @return List<Destination> Found destinations
	 */
	getFacilitiesDestinations {
		@Override
		public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getServicesManager().getFacilitiesDestinations(ac.getSession(),
					ac.getVoById(parms.readInt("vo")));
		}
	},

	/*#
	 * Gets count of all destinations.

	 * @return int destinations count
	 */
	getDestinationsCount {

		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getDestinationsCount(ac.getSession());
		}
	},

	/*#
	 * List all services associated with the facility (via resource).
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<Service> Found services
	 */
	getAssignedServices {

		@Override
		public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getServicesManager().getAssignedServices(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Lists resources assigned to service.
	 *
	 * @param service int Service <code>id</code>
	 * @return List<Resource> List of resources
	 */
	getAssignedResources {

		@Override
		public List<Resource> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getServicesManager().getAssignedResources(ac.getSession(),
					ac.getServiceById(parms.readInt("service")));
		}
	};
}
