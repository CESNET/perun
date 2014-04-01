package cz.metacentrum.perun.rpc.methods;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.controller.model.FacilityState;
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
	 * @param execService int Exec service ID
	 * @param facility int Facility ID
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
	 * @param facility int Facility ID
	 * @return List<Tasks> Tasks
	 */
	listAllRichTasksForFacility {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getPropagationStatsReader().listAllTasksForFacility(ac.getSession(), parms.readInt("facility"));
		}
	},

	/*#
	 * Returns RichTasks for a service.
	 *
	 * @param service int Service ID
	 * @param facility int Facility ID
	 * @return List<Tasks> Tasks
	 */
	getServiceRichTasks {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {

			List<ExecService> list = ac.getGeneralServiceManager().listExecServices(ac.getSession(), parms.readInt("service"));
			List<Task> tasks = new ArrayList<Task>();

			for (ExecService exec : list) {
				Task task = ac.getPropagationStatsReader().getTask(ac.getSession(), exec.getId(), parms.readInt("facility"));
				if (task != null) {
					Task rtask = (Task) task;
					rtask.setExecService(exec);
					tasks.add(rtask);
				}
			}
			return tasks;
		}
	},

	listAllTasksInState {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			throw new InternalErrorException("Not implemented yet!");
		}
	},

	listTasksScheduledBetweenDates {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			throw new InternalErrorException("Not implemented yet!");
		}
	},

	listTasksStartedBetweenDates {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			throw new InternalErrorException("Not implemented yet!");
		}
	},

	listTasksEndedBetweenDates {
		public List<Task> call(ApiCaller ac, Deserializer parms) throws PerunException {
			throw new InternalErrorException("Not implemented yet!");
		}
	},

	/*#
	 * Whether task exists.
	 *
	 * @param execService int ExecService ID
	 * @param facility int Facility ID
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
	 * @param task int Task ID
	 * @return List<TaskResult> Results
	 */
	getTaskResultsByTask {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getPropagationStatsReader().getTaskResultsByTask(parms.readInt("task"));
		}
	},

	/*#
	 * Return list of TaskResults by a Task.
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
	 * Returns TaskResult by its ID.
	 *
	 * @param taskResult int TaskResult ID
	 * @return TaskResult Result
	 */
	getTaskResultById {
		public TaskResult call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getPropagationStatsReader().getTaskResultById(parms.readInt("taskResult"));
		}
	},

	/*#
	 * Returns Task by its ID.
	 *
	 * @param id int Task ID
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
	 * @param facility int Facility ID
	 * @return FacilityState Facility state
	 */
	getFacilityState {
		public FacilityState call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getPropagationStatsReader().getFacilityState(ac.getSession(), ac.getFacilityById(parms.readInt("facility")));
		}
	},

	/*#
	 * Return propagation status of all facilities in Perun.
	 * @return List<FacilityState> Propagation status
	 */
	/*#
	 * Return propagation status of all facilities related to VO resources.
	 *
	 * @param vo int VO ID
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
	 * Returns task results for defined destinations.
	 *
	 * @param destinations List<String> Destinations
	 * @return List<TaskResult> Results.
	 */
	getTaskResultsForDestinations {
		public List<TaskResult> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getPropagationStatsReader().getTaskResultsForDestinations(ac.getSession(), parms.readList("destinations", String.class));
		}
	};
}
