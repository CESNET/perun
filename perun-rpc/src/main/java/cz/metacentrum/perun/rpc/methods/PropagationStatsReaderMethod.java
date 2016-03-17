package cz.metacentrum.perun.rpc.methods;

import java.util.List;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

public enum PropagationStatsReaderMethod implements ManagerMethod {

	/*#
	 * Returns a task.
	 *
	 * @param execService int Exec service <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return Task Found task
	 */
	getTask {
		public Task call(ApiCaller ac, Deserializer parms) throws PerunException {
			ExecService execService = ac.getExecServiceById(parms.readInt("execService"));
			Facility facility = ac.getFacilityById(parms.readInt("facility"));
			return ac.getPropagationStatsReader().getTask(ac.getSession(), execService, facility);
		}
	},

	/*#
	 * Returns all tasks.
	 *
	 * @return List<Task> All tasks
	 */
	listAllTasks {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getPropagationStatsReader().listAllTasks(ac.getSession());
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
			return ac.getPropagationStatsReader().listAllTasksForFacility(ac.getSession(), parms.readInt("facility"));
		}
	},

	/*#
	 *	NOT IMPLEMENTED YET
	 */
	listAllTasksInState {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			throw new InternalErrorException("Not implemented yet!");
		}
	},

	/*#
	 *	NOT IMPLEMENTED YET
	 */
	listTasksScheduledBetweenDates {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			throw new InternalErrorException("Not implemented yet!");
		}
	},

	/*#
	 *	NOT IMPLEMENTED YET
	 */
	listTasksStartedBetweenDates {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			throw new InternalErrorException("Not implemented yet!");
		}
	},

	/*#
	 *	NOT IMPLEMENTED YET
	 */
	listTasksEndedBetweenDates {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			throw new InternalErrorException("Not implemented yet!");
		}
	},

	/*#
	 * Whether task exists.
	 *
	 * @param execService int ExecService <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @return int 1 = true; 0 = false
	 */
	isThereSuchTask {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			ExecService execService = ac.getExecServiceById(parms.readInt("execService"));
			Facility facility = ac.getFacilityById(parms.readInt("facility"));
			return ( ac.getPropagationStatsReader().isThereSuchTask(execService, facility) ? 1 : 0 );
		}
	},

	/*#
	 * Returns the count of all tasks.
	 *
	 * @return int Task count
	 */
	countTasks {
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getPropagationStatsReader().countTasks();
		}
	},

	/*#
	 * Returns all task results.
	 *
	 * @return List<TaskResult> Task results.
	 */
	getTaskResults {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getPropagationStatsReader().getTaskResults();
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
			return ac.getPropagationStatsReader().getTaskResultsByTask(parms.readInt("task"));
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
			return ac.getPropagationStatsReader().getTaskResultsForGUIByTask(ac.getSession(), parms.readInt("task"));
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
			return ac.getPropagationStatsReader().getTaskResultById(parms.readInt("taskResult"));
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
			return ac.getPropagationStatsReader().getTaskById(ac.getSession(), parms.readInt("id"));
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
			return ac.getPropagationStatsReader().getFacilityState(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
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
				return ac.getPropagationStatsReader().getAllFacilitiesStatesForVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
			} else {
				return ac.getPropagationStatsReader().getAllFacilitiesStates(ac.getSession());
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
			return ac.getPropagationStatsReader().getResourcesState(ac.getSession(), ac.getVoById(parms.readInt("voId")));
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
			return ac.getPropagationStatsReader().getTaskResultsForDestinations(ac.getSession(), parms.readList("destinations", String.class));
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
			return ac.getPropagationStatsReader().getFacilityServicesState(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Delete Task and TaskResults.
	 *
	 * @param task Task Task to delete.
	 */
	deleteTask {
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getPropagationStatsReader().deleteTask(ac.getSession(), ac.getPropagationStatsReader().getTaskById(ac.getSession(), parms.readInt("task")));
			return null;
		}
	};

}
