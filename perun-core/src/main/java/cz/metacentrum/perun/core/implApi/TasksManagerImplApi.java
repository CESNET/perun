package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;

import java.util.List;

/**
 * TasksManagerImplApi
 */
public interface TasksManagerImplApi {

	int insertTask(Task task);

	List<Task> listAllTasks();

	/**
	 * Returns all tasks associated with selected facility
	 *
	 * @param facilityId
	 * @return tasks for facility
	 */
	List<Task> listAllTasksForFacility(int facilityId);

	/**
	 * Returns all tasks associated with given service
	 * 
	 * @param serviceId
	 * @return tasks for service
	 */
	List<Task> listAllTasksForService(int serviceId);

	List<Task> listAllTasksInState(Task.TaskStatus state);

	List<Task> listAllTasksNotInState(TaskStatus state);

	void updateTask(Task task);

	void removeTask(int id);

	int countTasks();

	Task getTask(int serviceId, int facilityId);

	Task getTask(Service service, Facility facility);

	Task getTaskById(int id);

	boolean isThereSuchTask(Service service, Facility facility);

	void removeTask(Service service, Facility facility);

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
	 * Delete TaskResult by its ID
	 *
	 * @param taskResultId ID of TaskResult to delete
	 */
	void deleteTaskResultById(int taskResultId);

	/**
	 * Delete all TaskResults for the particular Task
	 *
	 * @param taskId ID of Task to delete TaskResults
	 * @return number of deleted TaskResults
	 */
	int deleteTaskResults(int taskId);

	/**
	 * Delete all TaskResults for the particular Task and Destination.
	 *
	 * @param taskId ID of Task to delete TaskResults
	 * @param destinationId ID of Destination to delete TaskResults
	 * @return number of deleted TaskResults
	 */
	int deleteTaskResults(int taskId, int destinationId);

	/**
	 * Delete all TaskResults older than specified number of days
	 *
	 * @param numDays Number of days to keep
	 * @return number of deleted TaskResults
	 */
	int deleteOldTaskResults(int numDays);

	/**
	 * Delete all TaskResults
	 *
	 * @return number of deleted TaskResults
	 */
	int deleteAllTaskResults();

	int insertNewTaskResult(TaskResult taskResult);

	/**
	 * Returns list of tasks results for defined destinations (string representation).
	 *
	 * @param destinationsNames
	 * @return list of tasks results
	 * @throws InternalErrorException
	 */
	List<TaskResult> getTaskResultsForDestinations(List<String> destinationsNames);

}
