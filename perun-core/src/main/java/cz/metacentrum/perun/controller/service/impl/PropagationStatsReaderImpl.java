package cz.metacentrum.perun.controller.service.impl;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.FacilityState.FacilityPropagationState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.controller.service.PropagationStatsReader;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import cz.metacentrum.perun.taskslib.service.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Michal Karm Babacek
 *         JavaDoc coming soon...
 *
 */
@org.springframework.stereotype.Service(value = "propagationStatsReader")
public class PropagationStatsReaderImpl implements PropagationStatsReader {

	@Autowired
	private TaskDao taskDao;
	@Autowired
	private TaskResultDao taskResultDao;
	@Autowired
	private GeneralServiceManager generalServiceManager;
	@Autowired
	protected PerunBl perun;
	@Autowired
	private TaskManager taskManager;

	@Override
	public Task getTask(PerunSession perunSession, Service service, Facility facility) {
		return taskDao.getTask(service, facility);
	}

	@Override
	public Task getTaskById(PerunSession perunSession, int id) {
		return taskDao.getTaskById(id);
	}

	@Override
	public List<Task> listAllTasks(PerunSession perunSession) {
		return taskDao.listAllTasks();
	}

	@Override
	public List<Task> listAllTasksForFacility(PerunSession session, int facilityId) {
		return taskDao.listAllTasksForFacility(facilityId);
	}

	@Override
	public List<Task> listAllTasksInState(PerunSession perunSession,TaskStatus state) {
		return taskDao.listAllTasksInState(state);
	}

	@Override
	public boolean isThereSuchTask(Service service, Facility facility) {
		return taskDao.isThereSuchTask(service, facility);
	}

	@Override
	public int countTasks() {
		return taskDao.countTasks();
	}

	@Override
	public Task getTask(PerunSession perunSession,int serviceId, int facilityId) {
		return taskDao.getTask(serviceId, facilityId);
	}

	@Override
	public List<TaskResult> getTaskResults() {
		return taskResultDao.getTaskResults();
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId) {
		return taskResultDao.getTaskResultsByTask(taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTaskOnlyNewest(PerunSession session, int taskId) {
		return taskResultDao.getTaskResultsByTaskOnlyNewest(taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTaskAndDestination(PerunSession session, int taskId, int destinationId) {
		return taskResultDao.getTaskResultsByTaskAndDestination(taskId, destinationId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTask(PerunSession session, int taskId) {
		return taskResultDao.getTaskResultsByTask(taskId);
	}

	@Override
	public FacilityState getFacilityState(PerunSession session, Facility facility) throws PrivilegeException, FacilityNotExistsException, InternalErrorException {

		// get all tasks
		List<Task> tasks = taskDao.listAllTasksForFacility(facility.getId());
		// define state
		FacilityState state = new FacilityState();
		state.setFacility(facility);
		// if no tasks we can't determine facility state
		if (tasks == null || tasks.isEmpty()) {
			state.setState(FacilityPropagationState.NOT_DETERMINED);
			return state;
		} else {
			state.setState(FacilityPropagationState.OK); // OK if no change
		}

		// fill all available destinations
		List<RichDestination> destinations = perun.getServicesManager().getAllRichDestinations(session, facility);
		for (RichDestination rd : destinations){
			state.getResults().put(rd.getDestination(), FacilityPropagationState.NOT_DETERMINED);
		}

		// magic with tasks :-)
		for (Task task : tasks) {
			// save previous facility state
			FacilityPropagationState facState = state.getState();
			// PROCESSING and not ERROR before
			if (Arrays.asList(TaskStatus.GENERATED, TaskStatus.GENERATING, TaskStatus.PLANNED, TaskStatus.SENDING).contains(task.getStatus()) && (facState!=FacilityPropagationState.ERROR)) {
				state.setState(FacilityPropagationState.PROCESSING);
			}
			// ERROR - set ERROR
			else if (Arrays.asList(TaskStatus.ERROR, TaskStatus.GENERROR, TaskStatus.SENDERROR).contains(task.getStatus())) {
				state.setState(FacilityPropagationState.ERROR);
			}

			// get destination status
			if (task.getService() != null) {
				List<TaskResult> results = taskResultDao.getTaskResultsByTask(task.getId());

				Map<Service, Map<Destination, TaskResult>> latestResults = new HashMap<>();
				for (TaskResult res : results) {

					if (latestResults.get(res.getService()) == null) {
						// put in map since result for service exists
						Map<Destination, TaskResult> value = new HashMap<>();
						value.put(res.getDestination(), res);
						latestResults.put(res.getService(), value);
					} else if (latestResults.get(res.getService()) != null && latestResults.get(res.getService()).get(res.getDestination()) == null) {
						// put in inner map, since destination for service not yet exists
						latestResults.get(res.getService()).put(res.getDestination(), res);
					} else {
						// update in inner map since this is later task result
						if (latestResults.get(res.getService()).get(res.getDestination()).getId() < res.getId()) {
							// put in map
							latestResults.get(res.getService()).put(res.getDestination(), res);
						}
					}
				}

				for (Map<Destination, TaskResult> res : latestResults.values()) {
					for (TaskResult result : res.values()) {
						// iterate over all latest tasks results
						String destination = result.getDestination().getDestination();
						FacilityPropagationState propState = state.getResults().get(destination);
						// if any error => state is error
						if (TaskResult.TaskResultStatus.ERROR.equals(result.getStatus())) {
							state.getResults().put(destination, FacilityPropagationState.ERROR);
							continue;
						}
						// if result ok and previous was not bad
						if (TaskResult.TaskResultStatus.DONE.equals(result.getStatus())) {
							if (FacilityPropagationState.NOT_DETERMINED.equals(propState)) {
								state.getResults().put(destination, FacilityPropagationState.OK);
							}
						}
					}

				}
			}

		}
		return state;

	}

	@Override
	public List<FacilityState> getAllFacilitiesStates(PerunSession session) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
		List<FacilityState> list = new ArrayList<>();

		// return facilities where user is admin or all if perun admin
		List<Facility> facs = perun.getFacilitiesManager().getFacilities(session);
		Collections.sort(facs);
		for (Facility facility : facs) {
			list.add(getFacilityState(session, facility));
		}
		return list;
	}

	@Override
	public List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, FacilityNotExistsException {

		List<FacilityState> list = new ArrayList<>();
		List<RichResource> facs = perun.getResourcesManager().getRichResources(session, vo);

		Set<Facility> facilities = new HashSet<>();
		for (RichResource res : facs) {
			facilities.add(res.getFacility());
		}
		for (Facility f : facilities) {
			list.add(getFacilityState(session, f));
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public TaskResult getTaskResultById(int taskResultId) {
		return taskResultDao.getTaskResultById(taskResultId);
	}

	public void setTaskDao(TaskDao taskDao) {
		this.taskDao = taskDao;
	}

	public TaskDao getTaskDao() {
		return taskDao;
	}

	public void setTaskResultDao(TaskResultDao taskResultDao) {
		this.taskResultDao = taskResultDao;
	}

	public TaskResultDao getTaskResultDao() {
		return taskResultDao;
	}

	public GeneralServiceManager getGeneralServiceManager() {
		return generalServiceManager;
	}

	public void setGeneralServiceManager(GeneralServiceManager generalServiceManager) {
		this.generalServiceManager = generalServiceManager;
	}

	public List<TaskResult> getTaskResultsForDestinations(PerunSession session, List<String> destinationsNames) throws InternalErrorException {

		//FIXME check privileges, probably only some monitoring system can request these data
		return getTaskResultDao().getTaskResultsForDestinations(destinationsNames);
	}

	@Override
	public List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws PrivilegeException, VoNotExistsException, InternalErrorException {

		List<Resource> resources = perun.getResourcesManager().getResources(session, vo);
		List<ResourceState> resourceStateList = new ArrayList<>();

		for (Resource resource : resources) {
			List<Task> taskList = taskManager.listAllTasksForFacility(resource.getFacilityId());

			// create new resourceState
			ResourceState resourceState = new ResourceState();
			resourceState.setResource(resource);
			resourceState.setTaskList(taskList);

			// add new created resourceState to List
			resourceStateList.add(resourceState);
		}

		return resourceStateList;
	}

	@Override
	public List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) throws InternalErrorException, PrivilegeException{

		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException("getFacilityServicesState");
		}

		Map<Service, ServiceState> serviceStates = new HashMap<>();

		// fill states for all services which are currently on facility
		for (Service service : perun.getServicesManagerBl().getAssignedServices(sess, facility)) {

			serviceStates.put(service, new ServiceState(service, facility));
			serviceStates.get(service).setBlockedOnFacility(getGeneralServiceManager().isServiceBlockedOnFacility(service, facility));

			// service has destination on facility
			serviceStates.get(service).setHasDestinations(!perun.getServicesManagerBl().getDestinations(sess, service, facility).isEmpty());

		}

		// fill states for all tasks on facility

		List<Task> tasks = taskDao.listAllTasksForFacility(facility.getId());

		for (Task task : tasks) {

			Service taskService = task.getService();

			ServiceState serviceState = serviceStates.get(taskService);
			if (serviceState == null) {
				serviceState = new ServiceState(taskService, facility);
				serviceStates.put(taskService, serviceState);
				// fill destinations if service was not assigned
				serviceStates.get(taskService).setHasDestinations(!perun.getServicesManagerBl().getDestinations(sess, taskService, facility).isEmpty());
			}

			// fill service state
			serviceState.setTask(task);
			serviceStates.get(taskService).setBlockedOnFacility(getGeneralServiceManager().isServiceBlockedOnFacility(task.getService(), facility));

		}

		return new ArrayList<>(serviceStates.values());

	}

	@Override
	public void deleteTask(PerunSession sess, Task task) throws InternalErrorException, PrivilegeException {

		Facility facility = task.getFacility();
		Service service = task.getService();

		if (!AuthzResolver.isAuthorized(sess, Role.FACILITYADMIN, facility)) {
			throw new PrivilegeException("deleteTask");
		}

		// clear all task results
		taskResultDao.clearByTask(task.getId());

		// remove task itself
		taskDao.removeTask(service, facility);

	}

}
