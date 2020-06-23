package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.TasksManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


/**
 * TasksManager
 */
@org.springframework.stereotype.Service(value = "tasksManager")
public class TasksManagerEntry implements TasksManager {
	@Autowired
	protected PerunBl perun;
	@Autowired
	private TasksManagerBl tasksManagerBl;

	@Override
	public Task getTask(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException {
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getTask");
		}
		return tasksManagerBl.getTask(perunSession, service, facility);
	}

	@Override
	public Task getTaskById(PerunSession perunSession, int id) throws PrivilegeException {
		Task task = tasksManagerBl.getTaskById(id);
		Facility facility = task.getFacility();
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getTaskResultsByTask");
		}
		return tasksManagerBl.getTaskById(perunSession, id);
	}

	@Override
	public List<Task> listAllTasks(PerunSession perunSession) {
		return tasksManagerBl.listAllTasks(perunSession);
	}

	@Override
	public List<Task> listAllTasksForFacility(PerunSession session, int facilityId) throws PrivilegeException {
		Facility facility = new Facility();
		facility.setId(facilityId);
		if (!AuthzResolver.isAuthorized(session, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(session, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(session, "listAllTasksForFacility");
		}
		return tasksManagerBl.listAllTasksForFacility(session, facilityId);
	}

	@Override
	public List<Task> listAllTasksInState(PerunSession perunSession, Task.TaskStatus state) throws PrivilegeException {
		return tasksManagerBl.listAllTasksInState(perunSession, state);
	}

	@Override
	public boolean isThereSuchTask(PerunSession session, Service service, Facility facility) throws PrivilegeException {
		if (!AuthzResolver.isAuthorized(session, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(session, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(session, "isThereSuchTask");
		}
		return tasksManagerBl.isThereSuchTask(service, facility);
	}

	@Override
	public int countTasks() {
		return tasksManagerBl.countTasks();
	}

	@Override
	public Task getTask(PerunSession perunSession,int serviceId, int facilityId) throws PrivilegeException {
		Facility facility = new Facility();
		facility.setId(facilityId);
		if (!AuthzResolver.isAuthorized(perunSession, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(perunSession, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(perunSession, "getTask");
		}
		return tasksManagerBl.getTask(perunSession, serviceId, facilityId);
	}

	@Override
	public List<TaskResult> getTaskResults() {
		return tasksManagerBl.getTaskResults();
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(PerunSession sess, int taskId) throws PrivilegeException {
		Task task = tasksManagerBl.getTaskById(taskId);
		Facility facility = task.getFacility();
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getTaskResultsByTask");
		}
		return tasksManagerBl.getTaskResultsByTask(taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTaskOnlyNewest(PerunSession session, int taskId) throws PrivilegeException {
		Task task = tasksManagerBl.getTaskById(taskId);
		Facility facility = task.getFacility();
		if (!AuthzResolver.isAuthorized(session, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(session, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(session, "getTaskResultsByTask");
		}
		return tasksManagerBl.getTaskResultsForGUIByTaskOnlyNewest(session, taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTaskAndDestination(PerunSession session, int taskId, int destinationId) throws DestinationNotExistsException, FacilityNotExistsException, PrivilegeException {
		Task task = tasksManagerBl.getTaskById(taskId);
		Facility facility = task.getFacility();
		if (!AuthzResolver.isAuthorized(session, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(session, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(session, "getTaskResultsByTask");
		}
		return tasksManagerBl.getTaskResultsForGUIByTaskAndDestination(session, taskId, destinationId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTask(PerunSession session, int taskId) throws PrivilegeException {
		Task task = tasksManagerBl.getTaskById(taskId);
		Facility facility = task.getFacility();
		if (!AuthzResolver.isAuthorized(session, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(session, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(session, "getTaskResultsByTask");
		}
		return tasksManagerBl.getTaskResultsForGUIByTask(session, taskId);
	}

	@Override
	public FacilityState getFacilityState(PerunSession session, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		if (!AuthzResolver.isAuthorized(session, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(session, Role.ENGINE) &&
				!AuthzResolver.isAuthorized(session, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(session, "getFacilityState");
		}
		return tasksManagerBl.getFacilityState(session, facility);
	}

	@Override
	public List<FacilityState> getAllFacilitiesStates(PerunSession session) throws FacilityNotExistsException {
		return tasksManagerBl.getAllFacilitiesStates(session);
	}

	@Override
	public List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException, FacilityNotExistsException {
		if (!AuthzResolver.isAuthorized(session, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(session, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(session, "getAllFacilitiesStatesForVo");
		}
		return tasksManagerBl.getAllFacilitiesStatesForVo(session, vo);
	}

	@Override
	public TaskResult getTaskResultById(int taskResultId) {
		return tasksManagerBl.getTaskResultById(taskResultId);
	}

	public List<TaskResult> getTaskResultsForDestinations(PerunSession session, List<String> destinationsNames) {
		//FIXME check privileges, probably only some monitoring system can request these data
		return tasksManagerBl.getTaskResultsForDestinations(session, destinationsNames);
	}

	@Override
	public List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException {
		if (!AuthzResolver.isAuthorized(session, Role.VOADMIN, vo) &&
				!AuthzResolver.isAuthorized(session, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(session, "getResourcesState");
		}
		return tasksManagerBl.getResourcesState(session, vo);
	}

	@Override
	public List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) throws PrivilegeException{
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility) &&
				!AuthzResolver.isAuthorized(sess, Role.PERUNOBSERVER)) {
			throw new PrivilegeException(sess, "getFacilityServicesState");
		}
		return tasksManagerBl.getFacilityServicesState(sess, facility);

	}

	@Override
	public void deleteTask(PerunSession sess, Task task) throws PrivilegeException {
		Facility facility = task.getFacility();
		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException(sess, "deleteTask");
		}
		tasksManagerBl.deleteTask(sess, task);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perun = perunBl;
	}

	public void setTasksManagerBl(TasksManagerBl tasksManagerBl) {
		this.tasksManagerBl = tasksManagerBl;
	}
}
