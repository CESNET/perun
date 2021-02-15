package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;

import java.util.List;

/**
 * TasksManager
 */
public interface TasksManager {

	/**
	 * Get number of Tasks in DB.
	 * 
	 * @return Number of tasks.
	 */
	int countTasks();

	/**
	 * Delete Task and it's TaskResults. Use this method only before deleting whole Facility.
	 *
	 * @param sess PerunSession
	 * @param task Task to delete
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void deleteTask(PerunSession sess, Task task) throws PrivilegeException;

	/**
	 * Delete TaskResult by its ID
	 *
	 * @param sess PerunSession
	 * @param taskResultId Id of TaskResults to be deleted
	 * @throws PrivilegeException
	 */
	void deleteTaskResultById(PerunSession sess, int taskResultId) throws PrivilegeException;

	/**
	 * Delete all TaskResults related to specified Task and Destination
	 *
	 * @param sess PerunSession
	 * @param task Task to have TaskResults deleted
	 * @param destination Destination to have TasksResults deleted
	 * @throws PrivilegeException
	 */
	void deleteTaskResults(PerunSession sess, Task task, Destination destination) throws PrivilegeException;

	/**
	 * Return propagation status of all facilities in Perun
	 *
	 * @param session PerunSession
	 * @return all facilities propagation statuses
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 */
	List<FacilityState> getAllFacilitiesStates(PerunSession session) throws FacilityNotExistsException;

	/**
	 * Return propagation status of all facilities related to VO resources
	 *
	 * @param session PerunSession
	 * @return all facilities propagation statuses
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 * @throws VoNotExistsException
	 */
	List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException, FacilityNotExistsException;

	/**
	 * Returns list of ServiceStates for given facility. It lists states for all services, which are currently
	 * assigned to the facility or has any Task related to this facility.
	 *
	 * So results are returned even when there was no previous propagation of such service or service is no longer assigned.
	 *
	 * @param sess PerunSession
	 * @param facility
	 * @return list of ServiceStates
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException;

	/**
	 * Return propagation status of facility
	 *
	 * @param session
	 * @param facility
	 * @return propagation status of facility
	 *
	 * @throws FacilityNotExistsException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	FacilityState getFacilityState(PerunSession session, Facility facility) throws PrivilegeException, FacilityNotExistsException;

	/**
	 * Returns list of ResourceStates for VO.
	 *
	 * @param session PerunSession
	 * @param vo VirtualOrganization
	 * @return list of ResourceStates
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 */
	List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException;

	/**
	 * Find Task for given Service and Facility.
	 * 
	 * @param perunSession
	 * @param service
	 * @param facility
	 * @return
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 * @throws ServiceNotExistsException
	 */
	Task getTask(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException;

	/**
	 * Retrieve Task given its id.
	 * 
	 * @param perunSession
	 * @param id
	 * @return Task with given id
	 * @throws PrivilegeException
	 */
	Task getTaskById(PerunSession perunSession, int id) throws PrivilegeException;

	/**
	 * Get TaskResult given its id.
	 * 
	 * @param session
	 * @param readInt
	 * @return TaskResult
	 */
	TaskResult getTaskResultById(PerunSession session, int readInt);

	/**
	 * Get all TaskResults.
	 * 
	 * @param perunSession
	 * @return List of TaskResult
	 */
	List<TaskResult> getTaskResults(PerunSession perunSession);

	/**
	 * Get all TaskResult's for given Task 
	 * 
	 * @param sess
	 * @param taskId
	 * @return List of TaskResult
	 * @throws PrivilegeException
	 */
	List<TaskResult> getTaskResultsByTask(PerunSession sess, int taskId) throws PrivilegeException;

	/**
	 * Returns task results for defined destinations (string representation).
	 *
	 * @param session
	 * @param destinationsNames
	 * @return list of tasks results
	 */
	List<TaskResult> getTaskResultsByDestinations(PerunSession session, List<String> destinationsNames);

	// TODO - add more methods

	/**
	 * Find all task results for given task and destination.
	 * 
	 * @param session
	 * @param taskId
	 * @param destinationId
	 * @return List of TaskResult
	 * @throws DestinationNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws PrivilegeException
	 */
	List<TaskResult> getTaskResultsByTaskAndDestination(PerunSession session, int taskId, int destinationId) throws DestinationNotExistsException, FacilityNotExistsException, PrivilegeException;

	/**
	 * Retrieve the newest task results for given task.
	 * 
	 * @param session
	 * @param taskId
	 * @return List of TaskResult
	 * @throws PrivilegeException
	 */
	List<TaskResult> getTaskResultsByTaskOnlyNewest(PerunSession session, int taskId) throws PrivilegeException;

	/**
	 * Check if if there is a task for given service and facility.
	 * 
	 * @param session
	 * @param service
	 * @param facility
	 * @return true if task exists, false otherwise
	 * @throws PrivilegeException
	 * @throws FacilityNotExistsException
	 * @throws ServiceNotExistsException
	 */
	boolean isThereSuchTask(PerunSession session, Service service, Facility facility) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException;

	/**
	 * Retrieve all task results.
	 * 
	 * @param perunSession
	 * @return List of TaskResult
	 * @throws PrivilegeException
	 */
	List<Task> listAllTasks(PerunSession perunSession) throws PrivilegeException;

	/**
	 * Returns all tasks associated with selected facility
	 *
	 * @param session
	 * @param facilityId
	 *
	 * @return all tasks for facility
	 *
	 */
	List<Task> listAllTasksForFacility(PerunSession session, int facilityId) throws PrivilegeException;

	/**
	 * Retrieve all tasks in given state
	 * 
	 * @param perunSession
	 * @param state
	 * @return List of Task
	 * @throws PrivilegeException
	 */
	List<Task> listAllTasksInState(PerunSession perunSession, Task.TaskStatus state) throws PrivilegeException;


}
