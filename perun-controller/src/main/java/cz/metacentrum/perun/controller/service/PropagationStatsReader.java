package cz.metacentrum.perun.controller.service;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;

/**
 *
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
public interface PropagationStatsReader {

	Task getTask(PerunSession perunSession, ExecService execService, Facility facility) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	Task getTaskById(PerunSession perunSession, int id) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	List<Task> listAllTasks(PerunSession perunSession) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	/**
	 * Returns all tasks associated with selected facility
	 *
	 * @param session
	 * @param facilityId
	 *
	 * @return all tasks for facility
	 *
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws FacilityNotExistsException
	 */
	List<Task> listAllTasksForFacility(PerunSession session, int facilityId) throws ServiceNotExistsException, PrivilegeException, InternalErrorException, FacilityNotExistsException;

	List<Task> listAllTasksInState(PerunSession perunSession, TaskStatus state) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	List<Task> listTasksScheduledBetweenDates(PerunSession perunSession, Date olderThen, Date youngerThen) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	List<Task> listTasksStartedBetweenDates(PerunSession perunSession, Date olderThen, Date youngerThen) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	List<Task> listTasksEndedBetweenDates(PerunSession perunSession, Date olderThen, Date youngerThen) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	boolean isThereSuchTask(ExecService execService, Facility facility);

	int countTasks();

	Task getTask(PerunSession perunSession, int execServiceId, int facilityId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException;

	List<TaskResult> getTaskResults();

	List<TaskResult> getTaskResultsByTask(int taskId);

	List<TaskResult> getTaskResultsForGUIByTask(PerunSession session, int taskId) throws DestinationNotExistsException, PrivilegeException, InternalErrorException;

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
	 * @throws UserNotExistsException
	 * @throws FacilityNotExistsException
	 */
	List<FacilityState> getAllFacilitiesStates(PerunSession session) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, UserNotExistsException;

	/**
	 * Return propagation status of all facilities related to VO resources
	 *
	 * @param session PerunSession
	 * @return all facilities propagation statuses
	 * @throws PrivilegeException
	 * @throws InternalErrorException
	 * @throws UserNotExistsException
	 * @throws FacilityNotExistsException
	 * @throws VoNotExistsException
	 */
	List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, FacilityNotExistsException, UserNotExistsException;

	// TODO - add more methods

	/**
	 * Returns task results for defined destinations (string representation).
	 *
	 * @param session
	 * @param destinationsNames
	 * @return list of tasks results
	 */
	List<TaskResult> getTaskResultsForDestinations(PerunSession session, List<String> destinationsNames) throws InternalErrorException, PrivilegeException;

	/**
	 * Returns list of ResourceStates for VO with tasks which have ExecServiceType SEND.
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
	 * @throws ServiceNotExistsException
	 * @throws PrivilegeException
	 */	
	List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) throws InternalErrorException, ServiceNotExistsException, PrivilegeException;

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
