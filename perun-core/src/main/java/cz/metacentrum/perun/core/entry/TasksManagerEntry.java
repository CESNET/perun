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
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
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
	public int countTasks() {
		return tasksManagerBl.countTasks();
	}

	@Override
	public void deleteTask(PerunSession sess, Task task) throws PrivilegeException {
		Utils.notNull(sess, "sess");
		Facility facility = task.getFacility();

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteTask_Task_policy", facility)) {
			throw new PrivilegeException(sess, "deleteTask");
		}

		tasksManagerBl.deleteTask(sess, task);
	}

	@Override
	public void deleteTaskResultById(PerunSession sess, int taskResultId) throws PrivilegeException {
		TaskResult result = tasksManagerBl.getTaskResultById(sess, taskResultId);
		Task task = tasksManagerBl.getTaskById(sess, result.getTaskId());
		Facility facility = task.getFacility();
		if (!AuthzResolver.authorizedInternal(sess, "deleteTaskResultById_int_policy", facility)) {
			throw new PrivilegeException(sess, "deleteTaskResults");
		}
		tasksManagerBl.deleteTaskResultById(sess, result.getId());
	}

	@Override
	public void deleteTaskResults(PerunSession sess, Task task, Destination destination) throws PrivilegeException {
		Facility facility = task.getFacility();
		if (!AuthzResolver.authorizedInternal(sess, "deleteTaskResults_Task_Destination_policy", facility)) {
			throw new PrivilegeException(sess, "deleteTaskResults");
		}
		tasksManagerBl.deleteTaskResults(sess, task.getId(), destination.getId());
	}

	@Override
	public List<FacilityState> getAllFacilitiesStates(PerunSession session) throws FacilityNotExistsException {
		Utils.notNull(session, "session");

		return tasksManagerBl.getAllFacilitiesStates(session);
	}

	@Override
	public List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException, FacilityNotExistsException {
		Utils.notNull(session, "session");
		perun.getVosManagerBl().checkVoExists(session, vo);

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "getAllFacilitiesStatesForVo_Vo_policy", vo)) {
			throw new PrivilegeException(session, "getAllFacilitiesStatesForVo");
		}

		return tasksManagerBl.getAllFacilitiesStatesForVo(session, vo);
	}

	@Override
	public List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.notNull(sess, "sess");
		perun.getFacilitiesManagerBl().checkFacilityExists(sess, facility);

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getFacilityServicesState_Facility_policy", facility)) {
			throw new PrivilegeException(sess, "getFacilityServicesState");
		}

		return tasksManagerBl.getFacilityServicesState(sess, facility);

	}

	@Override
	public FacilityState getFacilityState(PerunSession session, Facility facility) throws PrivilegeException, FacilityNotExistsException {
		Utils.notNull(session, "session");
		perun.getFacilitiesManagerBl().checkFacilityExists(session, facility);

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "getFacilityState_Facility_policy", facility)) {
			throw new PrivilegeException(session, "getFacilityState");
		}

		return tasksManagerBl.getFacilityState(session, facility);
	}

	@Override
	public List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException {
		Utils.notNull(session, "session");
		perun.getVosManagerBl().checkVoExists(session, vo);

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "getResourcesState_Vo_policy", vo)) {
			throw new PrivilegeException(session, "getResourcesState");
		}

		return tasksManagerBl.getResourcesState(session, vo);
	}

	@Override
	public Task getTask(PerunSession perunSession, Service service, Facility facility) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException {
		Utils.notNull(perunSession, "perunSession");
		perun.getFacilitiesManagerBl().checkFacilityExists(perunSession, facility);
		perun.getServicesManagerBl().checkServiceExists(perunSession, service);

		//Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getTask_Service_Facility_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(perunSession, "getTask");
		}

		return tasksManagerBl.getTask(perunSession, service, facility);
	}

	@Override
	public Task getTaskById(PerunSession perunSession, int id) throws PrivilegeException {
		Utils.notNull(perunSession, "perunSession");
		Task task = tasksManagerBl.getTaskById(perunSession, id);
		Facility facility = task.getFacility();

		//Authorization
		if (!AuthzResolver.authorizedInternal(perunSession, "getTaskById_int_policy", facility)) {
			throw new PrivilegeException(perunSession, "getTaskResultsByTask");
		}

		return tasksManagerBl.getTaskById(perunSession, id);
	}

	@Override
	public TaskResult getTaskResultById(PerunSession perunSession, int taskResultId) {
		return tasksManagerBl.getTaskResultById(perunSession, taskResultId);
	} 

	@Override
	public List<TaskResult> getTaskResults(PerunSession perunSession) {
		return tasksManagerBl.getTaskResults(perunSession);
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(PerunSession sess, int taskId) throws PrivilegeException {
		Utils.notNull(sess, "sess");
		Task task = tasksManagerBl.getTaskById(sess, taskId);
		Facility facility = task.getFacility();

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getTaskResultsByTask_int_policy", facility)) {
			throw new PrivilegeException(sess, "getTaskResultsByTask");
		}

		return tasksManagerBl.getTaskResultsByTask(sess, taskId);
	}

	public List<TaskResult> getTaskResultsByDestinations(PerunSession session, List<String> destinationsNames) {
		Utils.notNull(session, "session");

		//FIXME check privileges, probably only some monitoring system can request these data
		return tasksManagerBl.getTaskResultsByDestinations(session, destinationsNames);
	}

	@Override
	public List<TaskResult> getTaskResultsByTaskAndDestination(PerunSession session, int taskId, int destinationId) throws DestinationNotExistsException, FacilityNotExistsException, PrivilegeException {
		Utils.notNull(session, "session");
		Task task = tasksManagerBl.getTaskById(session, taskId);
		Facility facility = task.getFacility();

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "getTaskResultsByTaskAndDestination_int_int_policy", facility)) {
			throw new PrivilegeException(session, "getTaskResultsByTask");
		}

		return tasksManagerBl.getTaskResultsByTaskAndDestination(session, taskId, destinationId);
	}

	@Override
	public List<TaskResult> getTaskResultsByTaskOnlyNewest(PerunSession session, int taskId) throws PrivilegeException {
		Utils.notNull(session, "session");
		Task task = tasksManagerBl.getTaskById(session, taskId);
		Facility facility = task.getFacility();

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "getTaskResultsByTaskOnlyNewest_int_policy", facility)) {
			throw new PrivilegeException(session, "getTaskResultsByTask");
		}

		return tasksManagerBl.getTaskResultsByTaskOnlyNewest(session, taskId);
	}

	@Override
	public boolean isThereSuchTask(PerunSession session, Service service, Facility facility) throws PrivilegeException, FacilityNotExistsException, ServiceNotExistsException {
		Utils.notNull(session, "session");
		perun.getFacilitiesManagerBl().checkFacilityExists(session, facility);
		perun.getServicesManagerBl().checkServiceExists(session, service);

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "isThereSuchTask_Service_Facility_policy", Arrays.asList(service, facility))) {
			throw new PrivilegeException(session, "isThereSuchTask");
		}

		return tasksManagerBl.isThereSuchTask(session, service, facility);
	}

	@Override
	public List<Task> listAllTasks(PerunSession perunSession) {
		return tasksManagerBl.listAllTasks(perunSession);
	}

	@Override
	public List<Task> listAllTasksForFacility(PerunSession session, int facilityId) throws PrivilegeException {
		Utils.notNull(session, "session");
		Facility facility = new Facility();
		facility.setId(facilityId);

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "listAllTasksForFacility_int_policy", facility)) {
			throw new PrivilegeException(session, "listAllTasksForFacility");
		}

		return tasksManagerBl.listAllTasksForFacility(session, facilityId);
	}

	@Override
	public List<Task> listAllTasksInState(PerunSession perunSession, Task.TaskStatus state) throws PrivilegeException {
		Utils.notNull(perunSession, "perunSession");
		return tasksManagerBl.listAllTasksInState(perunSession, state);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perun = perunBl;
	}

	public void setTasksManagerBl(TasksManagerBl tasksManagerBl) {
		this.tasksManagerBl = tasksManagerBl;
	}
}
