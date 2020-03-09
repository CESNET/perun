package cz.metacentrum.perun.rpc.methods;

import java.util.List;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

/**
 * TasksManagerMethod
 */
public enum TasksManagerMethod implements ManagerMethod {

	/*#
	 * Returns a task.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return Task Found task
	 */
	getTask {
		public Task call(ApiCaller ac, Deserializer parms) throws PerunException {
			Service service = ac.getServiceById(parms.readInt("service"));
			Facility facility = ac.getFacilityById(parms.readInt("facility"));
			return ac.getTasksManager().getTask(ac.getSession(), service, facility);
		}
	},

	/*#
	 * Returns all tasks.
	 *
	 * @return List<Task> All tasks
	 */
	listAllTasks {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().listAllTasks(ac.getSession());
		}
	},

	/*#
	 * Returns all tasks associated with selected facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<Tasks> Tasks
	 */
	listAllTasksForFacility {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().listAllTasksForFacility(ac.getSession(), parms.readInt("facility"));
		}
	},

	/*#
	 * Whether task exists.
	 *
	 * @param service int Service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return int 1 = true; 0 = false
	 */
	isThereSuchTask {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			Service service = ac.getServiceById(parms.readInt("service"));
			Facility facility = ac.getFacilityById(parms.readInt("facility"));
			return ( ac.getTasksManager().isThereSuchTask(ac.getSession(), service, facility) ? 1 : 0 );
		}
	},

	/*#
	 * Returns the count of all tasks.
	 *
	 * @return int Task count
	 */
	countTasks {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().countTasks();
		}
	},

	/*#
	 * Returns all task results.
	 *
	 * @return List<TaskResult> Task results.
	 */
	getTaskResults {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getTaskResults();
		}
	},

	/*#
	 * Return list of TaskResults by a Task.
	 *
	 * @param task int Task <code>id</code>
	 * @return List<TaskResult> Results
	 */
	getTaskResultsByTask {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getTaskResultsByTask(ac.getSession(), parms.readInt("task"));
		}
	},

	/*#
	 * Return list of only newest TaskResults by a Task for GUI.
	 *
	 * @param task int Task
	 * @return List<TaskResult> Results
	 */
	getTaskResultsForGUIByTaskOnlyNewest {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getTaskResultsForGUIByTaskOnlyNewest(ac.getSession(), parms.readInt("task"));
		}
	},

	/*#
	 * Return list of TaskResults by a Task and destination for GUI.
	 *
	 * @param task int Task
	 * @param destination int Destination <code>id</code>
	 * @return List<TaskResult> Results
	 */
	getTaskResultsForGUIByTaskAndDestination {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getTaskResultsForGUIByTaskAndDestination(ac.getSession(), parms.readInt("task"), parms.readInt("destination"));
		}
	},

	/*#
	 * Return list of TaskResults by a Task for GUI.
	 *
	 * @param task int Task
	 * @return List<TaskResult> Results
	 */
	getTaskResultsForGUIByTask {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getTaskResultsForGUIByTask(ac.getSession(), parms.readInt("task"));
		}
	},

	/*#
	 * Returns TaskResult by its <code>id</code>.
	 *
	 * @param taskResult int TaskResult <code>id</code>
	 * @return TaskResult Result
	 */
	getTaskResultById {
		public TaskResult call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getTaskResultById(parms.readInt("taskResult"));
		}
	},

	/*#
	 * Returns Task by its <code>id</code>.
	 *
	 * @param id int Task <code>id</code>
	 * @return Task Task
	 */

	getTaskById {
		public Task call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getTaskById(ac.getSession(), parms.readInt("id"));
		}
	},

	/*#
	 * Return propagation status of facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return FacilityState Facility state
	 */
	getFacilityState {
		public FacilityState call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getFacilityState(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Return propagation status of all facilities in Perun.
	 *
	 * @return List<FacilityState> Propagation status
	 */
	/*#
	 * Return propagation status of all facilities related to VO resources.
	 *
	 * @param vo int VO <code>id</code>
	 * @return List<FacilityState> Propagation status
	 */
	getAllFacilitiesStates {
		public List<FacilityState> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("vo")) {
				return ac.getTasksManager().getAllFacilitiesStatesForVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			} else {
				return ac.getTasksManager().getAllFacilitiesStates(ac.getSession());
			}
		}
	},

	/*#
	 * Return propagation status of all resources related to VO.
	 *
	 * @param voId int VO <code>id</code>
	 * @return List<ResourceState> Propagation status
	 */
	getAllResourcesState {
		public List<ResourceState> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getResourcesState(ac.getSession(), ac.getVoById(parms.readInt("voId")));
		}
	},

	/*#
	 * Returns task results for defined destinations.
	 *
	 * @param destinations List<String> Destinations
	 * @return List<TaskResult> Results.
	 */
	getTaskResultsForDestinations {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getTaskResultsForDestinations(ac.getSession(), parms.readList("destinations", String.class));
		}
	},

	/*#
	 * Returns service states for defined facility.
	 *
	 * @param Facility int <code>id</code> of facility
	 * @return List<ServiceState> serviceStates.
	 */
	getFacilityServicesState {
		public List<ServiceState> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getTasksManager().getFacilityServicesState(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Delete Task and TaskResults.
	 *
	 * @param task int Task to delete.
	 */
	deleteTask {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getTasksManager().deleteTask(ac.getSession(), ac.getTasksManager().getTaskById(ac.getSession(), parms.readInt("task")));
			return null;
		}
	};

}
