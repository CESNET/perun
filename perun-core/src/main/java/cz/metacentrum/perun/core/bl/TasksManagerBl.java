package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

import java.util.List;

/**
 * TasksManagerBl
 */
public interface TasksManagerBl {

	/**
	 * Get number of tasks in DB.
	 * 
	 * @return int number of tasks
	 */
	int countTasks();

	/**
	 * Delete all TaskResults
	 *
	 * @return number of deleted TaskResults
	 */
	int deleteAllTaskResults(PerunSession sess);

	/**
	 * Delete all TaskResults older than specified number of days
	 *
	 * @param numDays Number of days to keep
	 * @return number of deleted TaskResults
	 */
	int deleteOldTaskResults(PerunSession sess, int numDays);

	/**
	 * Delete Task and it's TaskResults. Use this method only before deleting whole Facility.
	 *
	 * @param sess PerunSession
	 * @param task Task to delete
	 * @throws InternalErrorException
	 */
	void deleteTask(PerunSession sess, Task task);

	/**
	 * Delete TaskResults by its ID
	 *
	 * @param taskResultId ID of TaskResult to delete
	 */
	void deleteTaskResultById(PerunSession sess, int taskResultId);

	/**
	 * Delete all TaskResults for the particular Task
	 *
	 * @param taskId ID of Task to delete TaskResults
	 * @return number of deleted TaskResults
	 */
	int deleteTaskResults(PerunSession sess, int taskId);

	/**
	 * Delete all TaskResults for the particular Task and Destination.
	 *
	 * @param taskId ID of Task to delete TaskResults
	 * @param destinationId ID of Destination to delete TaskResults
	 * @return number of deleted TaskResults
	 */
	int deleteTaskResults(PerunSession sess, int taskId, int destinationId);

	/**
	 * Return propagation status of all facilities in Perun
	 *
	 * @param session PerunSession
	 * @return all facilities propagation statuses
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 */
	List<FacilityState> getAllFacilitiesStates(PerunSession session) throws FacilityNotExistsException;

	/**
	 * Return propagation status of all facilities related to VO resources
	 *
	 * @param session PerunSession
	 * @return all facilities propagation statuses
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 * @throws VoNotExistsException
	 */
	List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws VoNotExistsException, FacilityNotExistsException;

	/**
	 * Returns list of ServiceStates for given facility. It lists states for all services, which are currently
	 * assigned to the facility or has any Task related to this facility.
	 *
	 * So results are returned even when there was no previous propagation of such service or service is no longer assigned.
	 *
	 * @param sess
	 * @param facility
	 * @return list of ServiceStates
	 * @throws InternalErrorException
	 */
	List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility);

	/**
	 * Return propagation status of facility
	 *
	 * @param session
	 * @param facility
	 * @return propagation status of facility
	 *
	 * @throws FacilityNotExistsException
	 * @throws InternalErrorException
	 */
	FacilityState getFacilityState(PerunSession session, Facility facility) throws FacilityNotExistsException;

	/**
	 * Returns list of ResourceStates for VO.
	 *
	 * @param session PerunSession
	 * @param vo VirtualOrganization
	 * @return list of ResourceStates
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 */
	List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws VoNotExistsException;

	/**
	 * Find propagation task for given service and facility.
	 * 
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return Task
	 */
	Task getTask(PerunSession perunSession, Service service, Facility facility);

	// TODO - add more methods

	/**
	 * Retrieve task given its id.
	 * 
	 * @param perunSession
	 * @param id
	 * @return Task
	 */
	Task getTaskById(PerunSession perunSession, int id);

	/**
	 * Retrieve all task results for given task (by task id)
	 * 
	 * @param sess
	 * @param taskResultId
	 * @return TaskResult
	 */
	TaskResult getTaskResultById(PerunSession sess, int taskResultId);

	/**
	 * Retrieve all task results from DB.
	 * 
	 * @param sess
	 * @return List of TaskResult
	 */
	List<TaskResult> getTaskResults(PerunSession sess);

	//all new from dao

	/**
	 * Retrieve all tasks results for given task 
	 * 
	 * @param sess
	 * @param taskId
	 * @return List of TaskResult
	 */
	List<TaskResult> getTaskResultsByTask(PerunSession sess, int taskId);

	/**
	 * List newest TaskResults tied to a certain task and destination
	 *
	 * @param taskId
	 * @return List of TaskResult
	 */
	List<TaskResult> getTaskResultsByTaskAndDestination(PerunSession sess, int taskId, int destinationId);

	/**
	 * List newest TaskResults tied to a certain task
	 *
	 * @param taskId
	 * @return List of TaskResult
	 */
	List<TaskResult> getTaskResultsByTaskOnlyNewest(PerunSession sess, int taskId);

	/**
	 * Returns task results for defined destinations (string representation).
	 *
	 * @param session
	 * @param destinationsNames
	 * @return list of tasks results
	 */
	List<TaskResult> getTaskResultsByDestinations(PerunSession session, List<String> destinationsNames);

	/**
	 * Insert TaskResult into DB.
	 * 
	 * @param sess
	 * @param taskResult
	 * @return int id of the new task result
	 */
	int insertNewTaskResult(PerunSession sess, TaskResult taskResult);

	/**
	 * Insert Task into DB.
	 * 
	 * @param sess
	 * @param task
	 * @return int id of the inserted task
	 */
	int insertTask(PerunSession sess, Task task);

	/**
	 * Check if there is a task for given service and facility.
	 * 
	 * @param sess
	 * @param service
	 * @param facility
	 * @return boolean true if there is a task, false otherwise
	 */
	boolean isThereSuchTask(PerunSession sess, Service service, Facility facility);

	/**
	 * Retrieve all tasks from DB.
	 * 
	 * @param perunSession
	 * @return List of Task
	 */
	List<Task> listAllTasks(PerunSession perunSession);

	/**
	 * Returns all tasks associated with selected facility.
	 *
	 * @param session
	 * @param facilityId
	 *
	 * @return all tasks for facility
	 *
	 */
	List<Task> listAllTasksForFacility(PerunSession session, int facilityId);

	/**
	 * Returns all tasks associated with given service.
	 * 
	 * @param serviceId
	 * @return tasks for service
	 */
	List<Task> listAllTasksForService(PerunSession sess, int serviceId);

	/**
	 * Retrieve all tasks in given state.
	 * 
	 * @param perunSession
	 * @param state
	 * @return List of Task
	 */
	List<Task> listAllTasksInState(PerunSession perunSession, Task.TaskStatus state);

	/**
	 * Retrieve all tasks that are not in given state.
	 * 
	 * @param sess
	 * @param state
	 * @return List of Task
	 */
	List<Task> listAllTasksNotInState(PerunSession sess,  Task.TaskStatus state);

	/**
	 * Removes all tasks associated with given service including the associated task results
	 * 
	 * @param service
	 */
	void removeAllTasksForService(PerunSession sess, Service service);

	/**
	 * Remove task with given id.
	 * 
	 * @param sess
	 * @param id
	 */
	void removeTask(PerunSession sess, int id);

	/**
	 * Remove task for given service and facility.
	 * 
	 * @param sess
	 * @param service
	 * @param facility
	 */
	void removeTask(PerunSession sess, Service service, Facility facility);

	/**
	 * Update DB record for given task.
	 * 
	 * @param sess
	 * @param task
	 */
	void updateTask(PerunSession sess, Task task);

}
