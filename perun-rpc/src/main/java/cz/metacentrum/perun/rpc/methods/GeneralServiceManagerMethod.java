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
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @throw ServiceAlreadyBannedException When service is already banned on facility.
	 */
	blockServiceOnFacility {
	    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
		ac.getGeneralServiceManager().blockServiceOnFacility(ac.getSession(), ac.getServiceById(parms.readInt("service")),
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
				ac.getGeneralServiceManager().blockServiceOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")),parms.readInt("destination"));
			} else {
				ac.getGeneralServiceManager().blockServiceOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")), ac.getServicesManager().getDestinationIdByName(ac.getSession(), parms.readString("destinationName"), parms.readString("destinationType")));
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
			ac.getGeneralServiceManager().blockAllServicesOnFacility(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
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
				ac.getGeneralServiceManager().blockAllServicesOnDestination(ac.getSession(), parms.readInt("destination"));
			} else {
				ac.getGeneralServiceManager().blockAllServicesOnDestination(ac.getSession(), ac.getServicesManager().getDestinationIdByName(ac.getSession(), parms.readString("destinationName"), parms.readString("destinationType")));
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
			return ac.getGeneralServiceManager().getServicesBlockedOnFacility(ac.getSession(),
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
			return ac.getGeneralServiceManager().getServicesBlockedOnDestination(ac.getSession(),parms.readInt("destination"));
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
			if (ac.getGeneralServiceManager().isServiceBlockedOnFacility(ac.getServiceById(parms.readInt("service")),
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
			if (ac.getGeneralServiceManager().isServiceBlockedOnDestination(ac.getServiceById(parms.readInt("service")),parms.readInt("destination")))
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
			ac.getGeneralServiceManager().unblockAllServicesOnFacility(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
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
				ac.getGeneralServiceManager().unblockAllServicesOnDestination(ac.getSession(), parms.readInt("destination"));
			} else if (parms.contains("destinationName")) {
				if(parms.contains("destinationType")) {
					ac.getGeneralServiceManager().unblockAllServicesOnDestination(ac.getSession(), ac.getServicesManager().getDestinationIdByName(ac.getSession(), parms.readString("destinationName"), parms.readString("destinationType")));
				} else {
					ac.getGeneralServiceManager().unblockAllServicesOnDestination(ac.getSession(), parms.readString("destinationName"));
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
			ac.getGeneralServiceManager().unblockServiceOnFacility(ac.getSession(), ac.getServiceById(parms.readInt("service")),
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
				ac.getGeneralServiceManager().unblockServiceOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")), parms.readInt("destination"));
			} else {
				ac.getGeneralServiceManager().unblockServiceOnDestination(ac.getSession(), ac.getServiceById(parms.readInt("service")), ac.getServicesManager().getDestinationIdByName(ac.getSession(), parms.readString("destinationName"), parms.readString("destinationType")));
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
				if(ac.getGeneralServiceManager().forceServicePropagation(
							ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getServiceById(parms.readInt("service")))) return 1;
				else return 0;
			} else {
				if(ac.getGeneralServiceManager().forceServicePropagation(
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
				if(ac.getGeneralServiceManager().planServicePropagation(
						ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")),
						ac.getServiceById(parms.readInt("service")))) return 1;
				else return 0;
			} else {
				if(ac.getGeneralServiceManager().planServicePropagation(
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

			return ac.getGeneralServiceManager().getFacilityAssignedServicesForGUI(ac.getSession(), ac.getFacilityById(parms.readInt("id")));

		}
	};

}
