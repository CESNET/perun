package cz.metacentrum.perun.controller.service;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;

import java.util.List;

/**
 *
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
public interface PropagationStatsReader {

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

	List<Task> listAllTasksInState(PerunSession perunSession, TaskStatus state);

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
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 */
	FacilityState getFacilityState(PerunSession session, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException;

	/**
	 * Return propagation status of all facilities in Perun
	 *
	 * @param session PerunSession
	 * @return all facilities propagation statuses
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 */
	List<FacilityState> getAllFacilitiesStates(PerunSession session) throws InternalErrorException, PrivilegeException, FacilityNotExistsException;

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
	List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, FacilityNotExistsException;

	// TODO - add more methods

	/**
	 * Returns task results for defined destinations (string representation).
	 *
	 * @param session
	 * @param destinationsNames
	 * @return list of tasks results
	 */
	List<TaskResult> getTaskResultsForDestinations(PerunSession session, List<String> destinationsNames) throws InternalErrorException;

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
	List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException, InternalErrorException;

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
	List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException;

	/**
	 * Delete Task and it's TaskResults. Use this method only before deleting whole Facility.
	 *
	 * @param sess PerunSession
	 * @param task Task to delete
	 * @throws InternalErrorException
	 * @throws PrivilegeException
	 */
	void deleteTask(PerunSession sess, Task task) throws InternalErrorException, PrivilegeException;

}
