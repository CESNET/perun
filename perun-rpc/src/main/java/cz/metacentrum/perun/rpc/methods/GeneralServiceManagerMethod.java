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
	 * @param service int Service ID
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
   * Returns a service with specific ID.
   *
   * @param id int Service ID
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
   * @return List<ExecService> Exec services
   */
  /*#
   * Returns exec services.
   *
   * @param service int Service ID
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
   * Returns an exec service with specific ID.
   *
   * @param id int Service ID
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
   * @param owner int Owner ID
   * @return int Int
   */
  insertExecService {
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGeneralServiceManager().insertExecService(ac.getSession(),
                                                      parms.read("execService", ExecService.class),
                                                      ac.getOwnerById(parms.readInt("owner")));
    }
  },

  /*#
   * Updates an exec service.
   *
   * @param execService ExecService JSON object
   */
  updateExecService {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().updateExecService(ac.getSession(), parms.read("execService", ExecService.class));
      return null;
    }
  },

  /*#
   * Deletes an exec service.
   *
   * @param execService int ExecService ID
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
   * @param service int Service ID
   * @param facility int Facility ID
   */
  banExecServiceOnFacility {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().banExecServiceOnFacility(ac.getExecServiceById(parms.readInt("service")),
                                                             ac.getFacilityById(parms.readInt("facility")));
      return null;
    }
  },

  /*#
   * Bans exec service on a destination.
   *
   * @param execService int Service ID
   * @param destination int Destination ID
   */
  banExecServiceOnDestination {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().banExecServiceOnDestination(ac.getExecServiceById(parms.readInt("execService")),parms.readInt("destination"));
      return null;
    }
  },

  /*#
   * Returns list of denials for a facility.
   *
   * @param facility int Facility ID
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
   * @param destination int Destination ID
   * @return List<ExecService> Exec services
   */
  listDenialsForDestination {
    public List<ExecService> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGeneralServiceManager().listDenialsForDestination(ac.getSession(),parms.readInt("destination"));
    }
  },

  isExecServiceDeniedOnFacility {
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (ac.getGeneralServiceManager().isExecServiceDeniedOnFacility(ac.getExecServiceById(parms.readInt("execService")),
                                                                         ac.getFacilityById(parms.readInt("facility"))))
        return 1;
      else return 0;
    }
  },

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

  isExecServiceDeniedOnDestination {
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (ac.getGeneralServiceManager().isExecServiceDeniedOnDestination(ac.getExecServiceById(parms.readInt("execService")),parms.readInt("destination")))
        return 1;
      else return 0;
    }
  },

  freeAllDenialsOnFacility {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().freeAllDenialsOnFacility(ac.getFacilityById(parms.readInt("facility")));
      return null;
    }
  },

  freeAllDenialsOnDestination {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().freeAllDenialsOnDestination(parms.readInt("destination"));
      return null;
    }
  },

  freeDenialOfExecServiceOnFacility {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().freeDenialOfExecServiceOnFacility(ac.getExecServiceById(parms.readInt("execService")),
                                                                      ac.getFacilityById(parms.readInt("facility")));
      return null;
    }
  },

  freeDenialOfExecServiceOnDestination {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().freeDenialOfExecServiceOnDestination(ac.getExecServiceById(parms.readInt("execService")),parms.readInt("destination"));
      return null;
    }
  },

  createDependency {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().createDependency(ac.getExecServiceById(parms.readInt("execService")),
                                                     ac.getExecServiceById(parms.readInt("dependantExecService")));
      return null;
    }
  },

  removeDependency {
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().removeDependency(ac.getExecServiceById(parms.readInt("execService")),
                                                     ac.getExecServiceById(parms.readInt("dependantExecService")));
      return null;
    }
  },

  isThereDependency {
    public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
      if(ac.getGeneralServiceManager().isThereDependency(ac.getExecServiceById(parms.readInt("execService")),
                                                             ac.getExecServiceById(parms.readInt("dependantExecService"))))
        return 1;
      else return 0;
    }
  },

  listExecServicesDependingOn {
    public List<ExecService> call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.getGeneralServiceManager().listExecServicesDependingOn(ac.getSession(),
                                                                ac.getExecServiceById(parms.readInt("dependantExecService")));
      return null;
    }
  },

  listExecServicesThisExecServiceDependsOn {
    public List<ExecService> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getGeneralServiceManager().listExecServicesThisExecServiceDependsOn(
                                      ac.getSession(),
                                      ac.getExecServiceById(parms.readInt("dependantExecService")));
    }
  },

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

   getFacilityAssignedServicesForGUI {
	   public List<ServiceForGUI> call(ApiCaller ac, Deserializer parms) throws PerunException {

		   return ac.getGeneralServiceManager().getFacilityAssignedServicesForGUI(ac.getSession(), ac.getFacilityById(parms.readInt("id")));

	   }
   };

}