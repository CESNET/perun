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

	/**
	 * 
	 * @return
	 */
	int countTasks();

	/**
	 * Delete all TaskResults
	 *
	 * @return number of deleted TaskResults
	 */
	int deleteAllTaskResults();

	/**
	 * Delete all TaskResults older than specified number of days
	 *
	 * @param numDays Number of days to keep
	 * @return number of deleted TaskResults
	 */
	int deleteOldTaskResults(int numDays);

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
	 * Retrieve task for given service and facility (by id).
	 * 
	 * @param serviceId
	 * @param facilityId
	 * @return Task
	 */
	Task getTask(int serviceId, int facilityId);

	/**
	 * Retrieve task for given service and facility.
	 * 
	 * @param service
	 * @param facility
	 * @return Task
	 */
	Task getTask(Service service, Facility facility);

	/**
	 * Retrieve task with given id.
	 * 
	 * @param id
	 * @return Task
	 */
	Task getTaskById(int id);

	/**
	 * Get TaskResult by its ID
	 *
	 * @param taskResultId
	 * @return TaskResult
	 */
	TaskResult getTaskResultById(int taskResultId);

	/**
	 * List TaskResults
	 *
	 * @return all TaskResults
	 */
	List<TaskResult> getTaskResults();

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
	 * List newest TaskResults tied to a certain task
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTaskOnlyNewest(int taskId);

	/**
	 * Returns list of tasks results for defined destinations (string representation).
	 *
	 * @param destinationsNames
	 * @return list of tasks results
	 * @throws InternalErrorException
	 */
	List<TaskResult> getTaskResultsByDestinations(List<String> destinationsNames);

	/**
	 * Store task result into DB.
	 * 
	 * @param taskResult
	 * @return id of new task result
	 */
	int insertNewTaskResult(TaskResult taskResult);

	/**
	 * Insert new task into DB.
	 * 
	 * @param task
	 * @return id of new task
	 */
	int insertTask(Task task);

	/**
	 * Check if there is a task for given service and facility.
	 * 
	 * @param service
	 * @param facility
	 * @return boolean true if there is a task, false otherwise
	 */
	boolean isThereSuchTask(Service service, Facility facility);

	/**
	 * Retrieve all tasks from DB.
	 * 
	 * @return List of Task
	 */
	List<Task> listAllTasks();

	/**
	 * Returns all tasks associated with selected facility.
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

	/**
	 * Retrieve all tass in given state.
	 * 
	 * @param state
	 * @return List of Task
	 */
	List<Task> listAllTasksInState(Task.TaskStatus state);

	/**
	 * Retrieve all tasks not in given state.
	 * 
	 * @param state
	 * @return List of Task
	 */
	List<Task> listAllTasksNotInState(TaskStatus state);

	/**
	 * Remove task with given id.
	 * 
	 * @param id
	 */
	void removeTask(int id);

	/**
	 * Remove task for given service and facility.
	 * 
	 * @param service
	 * @param facility
	 */
	void removeTask(Service service, Facility facility);

	/**
	 * Update DB record of given task.
	 * 
	 * @param task
	 */
	void updateTask(Task task);

}
