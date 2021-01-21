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

	Task getTask(PerunSession perunSession, Service service, Facility facility);

	Task getTaskById(PerunSession perunSession, int id);

	List<Task> listAllTasks(PerunSession perunSession);

	/**
	 * Returns all tasks associated with selected facility
	 *
	 * @param session
	 * @param facilityId
	 *
	 * @return all tasks for facility
	 *
	 */
	List<Task> listAllTasksForFacility(PerunSession session, int facilityId);

	List<Task> listAllTasksInState(PerunSession perunSession, Task.TaskStatus state);

	boolean isThereSuchTask(Service service, Facility facility);

	int countTasks();

	Task getTask(PerunSession perunSession, int serviceId, int facilityId);

	List<TaskResult> getTaskResults();

	List<TaskResult> getTaskResultsByTask(int taskId);

	List<TaskResult> getTaskResultsForGUIByTaskOnlyNewest(PerunSession session, int taskId);

	List<TaskResult> getTaskResultsForGUIByTask(PerunSession session, int taskId);

	List<TaskResult> getTaskResultsForGUIByTaskAndDestination(PerunSession session, int taskId, int destinationId);

	TaskResult getTaskResultById(int taskResultId);

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

	// TODO - add more methods

	/**
	 * Returns task results for defined destinations (string representation).
	 *
	 * @param session
	 * @param destinationsNames
	 * @return list of tasks results
	 */
	List<TaskResult> getTaskResultsForDestinations(PerunSession session, List<String> destinationsNames);

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
	 * Delete Task and it's TaskResults. Use this method only before deleting whole Facility.
	 *
	 * @param sess PerunSession
	 * @param task Task to delete
	 * @throws InternalErrorException
	 */
	void deleteTask(PerunSession sess, Task task);

	//all new from dao

	Task getTask(Service service, Facility facility);

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

	void updateTask(Task task);

	void removeTask(int id);

	Task getTask(int serviceId, int facilityId);

	Task getTaskById(int id);

	void removeTask(Service service, Facility facility);

	/**
	 * Removes all tasks associated with given service including the associated task results
	 * 
	 * @param service
	 */
	void removeAllTasksForService(Service service);

	List<Task> listAllTasksNotInState(Task.TaskStatus state);

	/**
	 * List newest TaskResults tied to a certain task
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTaskOnlyNewest(int taskId);

	/**
	 * List newest TaskResults tied to a certain task and destination
	 *
	 * @param taskId
	 * @return
	 */
	List<TaskResult> getTaskResultsByTaskAndDestination(int taskId, int destinationId);

	/**
	 * Delete TaskResults by its ID
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
