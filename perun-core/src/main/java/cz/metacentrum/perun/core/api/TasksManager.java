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

	Task getTask(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException;

	Task getTaskById(PerunSession perunSession, int id) throws PrivilegeException;

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

	List<Task> listAllTasksInState(PerunSession perunSession, Task.TaskStatus state) throws PrivilegeException;

	boolean isThereSuchTask(PerunSession session, Service service, Facility facility) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException;

	int countTasks();

	Task getTask(PerunSession perunSession, int serviceId, int facilityId) throws PrivilegeException;

	List<TaskResult> getTaskResults();

	List<TaskResult> getTaskResultsByTask(PerunSession sess, int taskId) throws PrivilegeException;

	List<TaskResult> getTaskResultsForGUIByTaskOnlyNewest(PerunSession session, int taskId) throws PrivilegeException;

	List<TaskResult> getTaskResultsForGUIByTask(PerunSession session, int taskId) throws PrivilegeException;

	List<TaskResult> getTaskResultsForGUIByTaskAndDestination(PerunSession session, int taskId, int destinationId) throws DestinationNotExistsException, FacilityNotExistsException, PrivilegeException;

	TaskResult getTaskResultById(int taskResultId);

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
	 * @throws PrivilegeException
	 * @throws VoNotExistsException
	 * @throws InternalErrorException
	 */
	List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException;

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
	 * @throws PrivilegeException
	 */
	List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException;

	/**
	 * Delete Task and it's TaskResults. Use this method only before deleting whole Facility.
	 *
	 * @param sess PerunSession
	 * @param task Task to delete
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void deleteTask(PerunSession sess, Task task) throws PrivilegeException;

}
