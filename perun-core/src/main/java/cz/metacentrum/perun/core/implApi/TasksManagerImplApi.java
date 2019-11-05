package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

import java.util.List;

/**
 * TasksManagerImplApi
 */
public interface TasksManagerImplApi {

	Task getTask(Service service, Facility facility);

	int insertTask(Task task, int engineID);

	List<Task> listAllTasks();

	List<Pair<Task, Integer>> listAllTasksAndClients();

	/**
	 * Returns all tasks associated with selected facility
	 *
	 * @param facilityId
	 * @return tasks for facility
	 */
	List<Task> listAllTasksForFacility(int facilityId);

	List<Task> listAllTasksInState(Task.TaskStatus state);

	void updateTask(Task task);

	void removeTask(int id);

	int countTasks();

	Task getTask(int serviceId, int facilityId);

	int scheduleNewTask(Task task, int engineID);

	Task getTask(Service service, Facility facility, int engineID);

	Task getTask(int serviceId, int facilityId, int engineID);

	Task getTaskById(int id);

	Task getTaskById(int id, int engineID);

	List<Task> listAllTasks(int engineID);

	List<Task> listAllTasksInState(Task.TaskStatus state, int engineID);

	void updateTask(Task task, int engineID);

	void updateTaskEngine(Task task, int engineID) throws InternalErrorException;

	boolean isThereSuchTask(Service service, Facility facility, int engineID);

	boolean isThereSuchTask(Service service, Facility facility);

	void removeTask(Service service, Facility facility, int engineID);

	void removeTask(Service service, Facility facility);

	void removeTask(int id, int engineID);

	int countTasks(int engineID);

	List<Task> listAllTasksNotInState(Task.TaskStatus state, int engineID);

	/**
	 * List TaskResults
	 *
	 * @return all TaskResults
	 */
	List<TaskResult> getTaskResults();

	/**
	 * List newest TaskResults tied to a certain task
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTaskOnlyNewest(int taskId);

	/**
	 * List TaskResults tied to a certain task
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTask(int taskId);

	/**
	 * List newest TaskResults tied to a certain task and destination
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTaskAndDestination(int taskId, int destinationId);

	/**
	 * Get TaskResult by its ID
	 *
	 * @param taskResultId
	 * @return
	 */
	TaskResult getTaskResultById(int taskResultId);

	/**
	 * Clear all results tied to a particular Task
	 *
	 * @param taskId
	 * @return number of deleted TaskResults
	 */
	int clearByTask(int taskId);

	/**
	 * Clear all results
	 *
	 * @return number of deleted TaskResults
	 */
	int clearAll();

	int insertNewTaskResult(TaskResult taskResult, int engineID) throws InternalErrorException;

	List<TaskResult> getTaskResults(int engineID);

	TaskResult getTaskResultById(int taskResultId, int engineID);

	int clearByTask(int taskId, int engineID);

	int clearAll(int engineID);

	int clearOld(int engineID, int numDays);

	List<TaskResult> getTaskResultsByTask(int taskId, int engineID);

	/**
	 * Returns list of tasks results for defined destinations (string representation).
	 *
	 * @param destinationsNames
	 * @return list of tasks results
	 * @throws InternalErrorException
	 */
	List<TaskResult> getTaskResultsForDestinations(List<String> destinationsNames) throws InternalErrorException;

}
