package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.*;

import java.util.List;

import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum GeneralServiceManagerMethod implements ManagerMethod {

	/*#
	 * Bans service on a facility.
	 *
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
	 * @param service int Service <code>id</code>
	 * @param destination int Destination <code>id</code>
	 */
	/*#
	 * Bans Service on a destination.
	 *
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
	 * @param destination int Destination <code>id</code>
	 */
	/*#
	 * Block all services currently assigned on this destination.
	 * Newly assigned services are still allowed for propagation.
	 *
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
	 * @param destination int Destination <code>id</code>
	 */
	/*#
	 * Erase all the possible denials on this destination.
	 *
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
	 * @param service int Service <code>id</code>
	 * @param destination int Destination <code>id</code>
	 */
	/*#
	 * Free the denial of the Service on this destination. If the Service was banned on
	 * this destination, it will be freed. In case the Service was not banned on this
	 * destination, nothing will happen.
	 *
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return int 1 = true if it is possible, 0 = false if not
	 */
	/*#
	 * Forces service propagation on defined facility.
	 *
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return int 1 = true if it is possible, 0 = false if not
	 */
	/*#
	 * Plans service propagation on defined facility.
	 *
	 * @deprecated Method was moved to ServicesManager
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
	 * @deprecated Method was moved to ServicesManager
	 * @param facility int Facility <code>id</code>
	 * @return List<ServiceForGUI> list of assigned services with allowed property
	 */
	getFacilityAssignedServicesForGUI {
		public List<ServiceForGUI> call(ApiCaller ac, Deserializer parms) throws PerunException {

			return ac.getServicesManager().getFacilityAssignedServicesForGUI(ac.getSession(), ac.getFacilityById(parms.readInt("id")));

		}
	};

}
