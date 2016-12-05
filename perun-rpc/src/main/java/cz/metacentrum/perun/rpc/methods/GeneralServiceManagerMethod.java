package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.controller.model.ServiceForGUI;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.*;

import java.util.List;

import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.taskslib.model.ExecService;

public enum GeneralServiceManagerMethod implements ManagerMethod {

	/*#
	 * Deletes a service.
	 *
	 * @param service int Service <code>id</code>
	 */
	deleteService {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			Service service = ac.getServiceById(parms.readInt("service"));
			ac.getGeneralServiceManager().deleteService(ac.getSession(), service);
			return null;
		}
	},

	/*#
	 * Returns all services.
	 *
	 * @return List<Service> All services
	 */
	listServices {
		public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGeneralServiceManager().listServices(ac.getSession());
		}
	},

	/*#
	 * Returns a service with specific <code>id</code>.
	 *
	 * @param id int Service <code>id</code>
	 * @return Service Found service object
	 */
	getService {
		public Service call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGeneralServiceManager().getService(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Returns exec services.
	 *
	 * @param service int Service <code>id</code>
	 * @return List<ExecService> Exec services
	 */
	/*#
	 * Returns exec services.
	 *
	 * @return List<ExecService> Exec services
	 */
	listExecServices {
		public List<ExecService> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("service")) {
				return ac.getGeneralServiceManager().listExecServices(ac.getSession(), parms.readInt("service"));
			} else {
				return ac.getGeneralServiceManager().listExecServices(ac.getSession());
			}
		}
	},

	/*#
	 * Returns count of exec services.
	 * @return int Count of exec services
	 */
	countExecServices {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGeneralServiceManager().countExecServices();
		}
	},

	/*#
	 * Returns an exec service with specific <code>id</code>.
	 *
	 * @param id int Service <code>id</code>
	 * @return ExecService Found exec service object
	 */
	getExecService {
		public ExecService call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGeneralServiceManager().getExecService(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Inserts an exec service.
	 *
	 * @param execService ExecService JSON object
	 * @return int new ExecService <code>id</code>
	 */
	insertExecService {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGeneralServiceManager().insertExecService(ac.getSession(),
					parms.read("execService", ExecService.class));
		}
	},

	/*#
	 * Updates an exec service.
	 *
	 * @param execService ExecService JSON object
	 * @return ExecService updated ExecService
	 */
	updateExecService {
		public ExecService call(ApiCaller ac, Deserializer parms) throws PerunException {
			ExecService es = parms.read("execService", ExecService.class);
			ac.getGeneralServiceManager().updateExecService(ac.getSession(), es);
			return ac.getGeneralServiceManager().getExecService(ac.getSession(), es.getId());
		}
	},

	/*#
	 * Deletes an exec service.
	 *
	 * @param execService int ExecService <code>id</code>
	 */
	deleteExecService {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().deleteExecService(ac.getExecServiceById(parms.readInt("execService")));
			return null;
		}
	},

	/*#
	 * Bans exec service on a facility.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @throw ServiceAlreadyBannedException When service is already banned on facility.
	 */
	banExecServiceOnFacility {
	    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
		ac.getGeneralServiceManager().banExecServiceOnFacility(ac.getSession(), ac.getExecServiceById(parms.readInt("service")),
		ac.getFacilityById(parms.readInt("facility")));
		return null;
	    }
	},

	/*#
	 * Bans exec service on a destination.
	 *
	 * @param execService int Service <code>id</code>
	 * @param destination int Destination <code>id</code>
	 */
	banExecServiceOnDestination {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().banExecServiceOnDestination(ac.getSession(), ac.getExecServiceById(parms.readInt("execService")),parms.readInt("destination"));
			return null;
		}
	},

	/*#
	 * Returns list of denials for a facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<ExecService> Exec services
	 */
	listDenialsForFacility {
		public List<ExecService> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGeneralServiceManager().listDenialsForFacility(ac.getSession(),
					ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Returns list of denials for a destination.
	 *
	 * @param destination int Destination <code>id</code>
	 * @return List<ExecService> Exec services
	 */
	listDenialsForDestination {
		public List<ExecService> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGeneralServiceManager().listDenialsForDestination(ac.getSession(),parms.readInt("destination"));
		}
	},

	/*#
	 * Is this execService denied on the facility?
	 *
	 * @param execService int ExecService <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @exampleResponse 1
	 * @return int 1 = true - the execService is denied on the facility, 0 = false - the execService in NOT denied on the facility
	 */
	isExecServiceDeniedOnFacility {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (ac.getGeneralServiceManager().isExecServiceDeniedOnFacility(ac.getExecServiceById(parms.readInt("execService")),
						ac.getFacilityById(parms.readInt("facility"))))
				return 1;
			else return 0;
		}
	},

	/*#
	 * Is this service denied on the facility?
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @exampleResponse 1
	 * @return int 1 = true - the service is denied on the facility, 0 = false - the service in NOT denied on the facility
	 */
	isServiceDeniedOnFacility {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {

			List<ExecService> list = ac.getGeneralServiceManager().listExecServices(ac.getSession(), parms.readInt("service"));
			Integer answer = 0;
			for (ExecService exec : list) {
				if (ac.getGeneralServiceManager().isExecServiceDeniedOnFacility(ac.getExecServiceById(exec.getId()), ac.getFacilityById(parms.readInt("facility")))) {
					answer = 1;
				}
			}
			return answer;
		}
	},

	/*#
	 * Is this execService denied on the destination?
	 *
	 * @param execService int ExecService <code>id</code>
	 * @param destination int Destination <code>id</code>
	 * @exampleResponse 1
	 * @return int 1 = true - the execService is denied on the destination, 0 = false - the execService in NOT denied on the destination
	 */
	isExecServiceDeniedOnDestination {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (ac.getGeneralServiceManager().isExecServiceDeniedOnDestination(ac.getExecServiceById(parms.readInt("execService")),parms.readInt("destination")))
				return 1;
			else return 0;
		}
	},

	/*#
	 * Erase all the possible denials on this facility.
	 *
	 * @param facility int Facility <code>id</code>
	 */
	freeAllDenialsOnFacility {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().freeAllDenialsOnFacility(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
			return null;
		}
	},

	/*#
	 * Erase all the possible denials on this destination.
	 *
	 * @param destination int Destination <code>id</code>
	 */
	freeAllDenialsOnDestination {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().freeAllDenialsOnDestination(ac.getSession(), parms.readInt("destination"));
			return null;
		}
	},

	/*#
	 * Free the denial of the execService on this facility. If the execService was banned
	 * on this facility, it will be freed. In case the execService was not banned on
	 * this facility, nothing will happen.
	 *
	 * @param execService int ExecService <code>id</code>
	 * @param facility int Facility <code>id</code>
	 */
	freeDenialOfExecServiceOnFacility {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().freeDenialOfExecServiceOnFacility(ac.getSession(), ac.getExecServiceById(parms.readInt("execService")),
					ac.getFacilityById(parms.readInt("facility")));
			return null;
		}
	},

	/*#
	 * Free the denial of the execService on this destination. If the execService was banned on
	 * this destination, it will be freed. In case the execService was not banned on this
	 * destination, nothing will happen.
	 *
	 * @param execService int ExecService <code>id</code>
	 * @param destination int Destination <code>id</code>
	 */
	freeDenialOfExecServiceOnDestination {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().freeDenialOfExecServiceOnDestination(ac.getSession(), ac.getExecServiceById(parms.readInt("execService")),parms.readInt("destination"));
			return null;
		}
	},

	/*#
	 * Creates a dependency of one ExecService on other.
	 * The execService can not be executed if any of the execServices it depends on is in an unstable (not terminal) state.
	 *
	 * @param execService int ExecService <code>id</code> to create dependency for
	 * @param dependantExecService int ExecService <code>id</code> to depend on
	 */
	createDependency {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().createDependency(ac.getExecServiceById(parms.readInt("execService")),
					ac.getExecServiceById(parms.readInt("dependantExecService")));
			return null;
		}
	},

	/*#
	 * Removes a dependency.
	 *
	 * @param dependantExecService int DependantExecService <code>id</code>
	 * @param execService int ExecService <code>id</code>
	 */
	removeDependency {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().removeDependency(ac.getExecServiceById(parms.readInt("execService")),
					ac.getExecServiceById(parms.readInt("dependantExecService")));
			return null;
		}
	},

	/*#
	 * Checks whether one execService depends on the other.
	 *
	 * @param dependantExecService int DependantExecService <code>id</code>
	 * @param execService int ExecService <code>id</code>
	 * @exampleResponse 1
	 * @return int 1 = true - yes, there is such a dependency, 0 = false - no, there is not such a dependency
	 */
	isThereDependency {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(ac.getGeneralServiceManager().isThereDependency(ac.getExecServiceById(parms.readInt("execService")),
						ac.getExecServiceById(parms.readInt("dependantExecService"))))
				return 1;
			else return 0;
		}
	},

	/*#
	 * List execServices depending on the given execService
	 *
	 * @param execService int ExecService <code>id</code>
	 * @return List<ExecService> A list of execServices that are depending on the given execService.
	 */
	listExecServicesDependingOn {
		public List<ExecService> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getGeneralServiceManager().listExecServicesDependingOn(ac.getSession(),
					ac.getExecServiceById(parms.readInt("dependantExecService")));
			return null;
		}
	},

	/*#
	 * List execServices this execService depends on
	 *
	 * @param dependantExecService int dependantExecService <code>id</code>
	 * @return List<ExecService> A list of execServices this execService depends on.
	 */
	listExecServicesThisExecServiceDependsOn {
		public List<ExecService> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getGeneralServiceManager().listExecServicesThisExecServiceDependsOn(
					ac.getSession(),
					ac.getExecServiceById(parms.readInt("dependantExecService")));
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
	 * 1 - allowed / 0 - one of service exec services is denied on this facility (=> service is denied).
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
