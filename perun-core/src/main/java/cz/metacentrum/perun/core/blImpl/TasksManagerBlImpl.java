package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.core.impl.FacilitiesManagerImpl;
import cz.metacentrum.perun.core.impl.ServicesManagerImpl;
import cz.metacentrum.perun.core.impl.TasksManagerImpl;
import cz.metacentrum.perun.core.implApi.TasksManagerImplApi;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TasksManagerBlImpl
 */
public class TasksManagerBlImpl implements TasksManagerBl {
	@Autowired
	private ServicesManagerBl servicesManagerBl;
	@Autowired
	protected PerunBl perun;
	@Autowired
	protected TasksManagerImplApi tasksManagerImpl;

	public TasksManagerBlImpl(TasksManagerImpl tasksManagerImpl) {
		this.tasksManagerImpl = tasksManagerImpl;
	}

	@Override
	public int insertNewTaskResult(TaskResult taskResult, int engineID) throws InternalErrorException {
		return tasksManagerImpl.insertNewTaskResult(taskResult, engineID);
	}

	@Override
	public List<TaskResult> getTaskResults(int engineID) {
		return getTaskResults(engineID);
	}

	@Override
	public TaskResult getTaskResultById(int taskResultId, int engineID) {
		return tasksManagerImpl.getTaskResultById(taskResultId, engineID);
	}

	@Override
	public int clearByTask(int taskId, int engineID) {
		return tasksManagerImpl.clearByTask(taskId, engineID);
	}

	@Override
	public int clearByTask(int taskId) {
		return tasksManagerImpl.clearByTask(taskId);
	}

	@Override
	public int clearAll(int engineID) {
		return tasksManagerImpl.clearAll(engineID);
	}

	@Override
	public int clearAll() {
		return tasksManagerImpl.clearAll();
	}

	@Override
	public int clearOld(int engineID, int numDays) {
		return tasksManagerImpl.clearOld(engineID, numDays);
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId, int engineID) {
		return tasksManagerImpl.getTaskResultsByTask(taskId, engineID);
	}

	@Override
	public List<TaskResult> getTaskResultsByTaskOnlyNewest(int taskId) {
		return getTaskResultsByTaskOnlyNewest(taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsByTaskAndDestination(int taskId, int destinationId) {
		return tasksManagerImpl.getTaskResultsByTaskAndDestination(taskId, destinationId);
	}

	public List<TaskResult> getTaskResultsForDestinations(List<String> destinationsNames) throws InternalErrorException {
		return tasksManagerImpl.getTaskResultsForDestinations(destinationsNames);
	}

	@Override
	public int scheduleNewTask(Task task, int engineID) {
		return tasksManagerImpl.scheduleNewTask(task, engineID);
	}

	@Override
	public int insertTask(Task task, int engineID) {
		return tasksManagerImpl.insertTask(task, engineID);
	}

	@Override
	public Task getTask(Service service, Facility facility) {
		return getTask(service.getId(), facility.getId());
	}

	@Override
	public Task getTask(int serviceId, int facilityId) {
		return tasksManagerImpl.getTask(serviceId, facilityId);
	}

	@Override
	public Task getTask(Service service, Facility facility, int engineID) {
		return tasksManagerImpl.getTask(service.getId(), facility.getId(), engineID);
	}

	@Override
	public Task getTask(int serviceId, int facilityId, int engineID) {
		return tasksManagerImpl.getTask(serviceId, facilityId, engineID);
	}

	@Override
	public List<Task> listAllTasksForFacility(int facilityId) {
		return tasksManagerImpl.listAllTasksForFacility(facilityId);
	}

	@Override
	public Task getTaskById(int id) {
		return tasksManagerImpl.getTaskById(id);
	}

	@Override
	public Task getTaskById(int id, int engineID) {
		return tasksManagerImpl.getTaskById(id, engineID);
	}

	@Override
	public List<Task> listAllTasks() {
		return tasksManagerImpl.listAllTasks();
	}

	@Override
	public List<Task> listAllTasks(int engineID) {
		return tasksManagerImpl.listAllTasks(engineID);
	}

	@Override
	public List<Pair<Task, Integer>> listAllTasksAndClients() {
		return tasksManagerImpl.listAllTasksAndClients();
	}

	@Override
	public List<Task> listAllTasksInState(Task.TaskStatus state) {
		return tasksManagerImpl.listAllTasksInState(state);
	}

	@Override
	public List<Task> listAllTasksInState(Task.TaskStatus state, int engineID) {
		return tasksManagerImpl.listAllTasksInState(state, engineID);
	}

	@Override
	public List<Task> listAllTasksNotInState(Task.TaskStatus state, int engineID) {
		return tasksManagerImpl.listAllTasksNotInState(state, engineID);
	}

	@Override
	public void updateTask(Task task, int engineID) {
		tasksManagerImpl.updateTask(task, engineID);
	}

	@Override
	public void updateTask(Task task) {
		tasksManagerImpl.updateTask(task);
	}

	@Override
	public void updateTaskEngine(Task task, int engineID) throws InternalErrorException {
		tasksManagerImpl.updateTaskEngine(task, engineID);
	}

	@Override
	public boolean isThereSuchTask(Service service, Facility facility, int engineID) {
		return tasksManagerImpl.isThereSuchTask(service, facility, engineID);
	}

	@Override
	public void removeTask(Service service, Facility facility, int engineID) {
		tasksManagerImpl.removeTask(service, facility, engineID);
	}

	@Override
	public void removeTask(Service service, Facility facility) {
		tasksManagerImpl.removeTask(service, facility);
	}

	@Override
	public void removeTask(int id, int engineID) {
		tasksManagerImpl.removeTask(id, engineID);
	}

	@Override
	public void removeTask(int id) {
		tasksManagerImpl.removeTask(id);
	}

	@Override
	public int countTasks(int engineID) {
		return tasksManagerImpl.countTasks(engineID);
	}

	@Override
	public Task getTask(PerunSession perunSession, Service service, Facility facility) {
		return tasksManagerImpl.getTask(service, facility);
	}

	@Override
	public Task getTaskById(PerunSession perunSession, int id) {
		return tasksManagerImpl.getTaskById(id);
	}

	@Override
	public List<Task> listAllTasks(PerunSession perunSession) {
		return tasksManagerImpl.listAllTasks();
	}

	@Override
	public List<Task> listAllTasksForFacility(PerunSession session, int facilityId) {
		return tasksManagerImpl.listAllTasksForFacility(facilityId);
	}

	@Override
	public List<Task> listAllTasksInState(PerunSession perunSession, Task.TaskStatus state) {
		return tasksManagerImpl.listAllTasksInState(state);
	}

	@Override
	public boolean isThereSuchTask(Service service, Facility facility) {
		return tasksManagerImpl.isThereSuchTask(service, facility);
	}

	@Override
	public int countTasks() {
		return tasksManagerImpl.countTasks();
	}

	@Override
	public Task getTask(PerunSession perunSession,int serviceId, int facilityId) {
		return tasksManagerImpl.getTask(serviceId, facilityId);
	}

	@Override
	public List<TaskResult> getTaskResults() {
		return tasksManagerImpl.getTaskResults();
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(int taskId) {
		return tasksManagerImpl.getTaskResultsByTask(taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTaskOnlyNewest(PerunSession session, int taskId) {
		return tasksManagerImpl.getTaskResultsByTaskOnlyNewest(taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTaskAndDestination(PerunSession session, int taskId, int destinationId) {
		return tasksManagerImpl.getTaskResultsByTaskAndDestination(taskId, destinationId);
	}

	@Override
	public List<TaskResult> getTaskResultsForGUIByTask(PerunSession session, int taskId) {
		return tasksManagerImpl.getTaskResultsByTask(taskId);
	}

	@Override
	public FacilityState getFacilityState(PerunSession session, Facility facility) throws FacilityNotExistsException, InternalErrorException {
		perun.getFacilitiesManagerBl().checkFacilityExists(session, facility);

		// get all tasks
		List<Task> tasks = tasksManagerImpl.listAllTasksForFacility(facility.getId());
		// define state
		FacilityState state = new FacilityState();
		state.setFacility(facility);
		// if no tasks we can't determine facility state
		if (tasks == null || tasks.isEmpty()) {
			state.setState(FacilityState.FacilityPropagationState.NOT_DETERMINED);
			return state;
		} else {
			state.setState(FacilityState.FacilityPropagationState.OK); // OK if no change
		}

		// fill all available destinations
		List<RichDestination> destinations = perun.getServicesManagerBl().getAllRichDestinations(session, facility);
		for (RichDestination rd : destinations){
			state.getResults().put(rd.getDestination(), FacilityState.FacilityPropagationState.NOT_DETERMINED);
		}

		// magic with tasks :-)
		for (Task task : tasks) {
			// save previous facility state
			FacilityState.FacilityPropagationState facState = state.getState();
			// PROCESSING and not ERROR before
			if (Arrays.asList(Task.TaskStatus.GENERATED, Task.TaskStatus.GENERATING, Task.TaskStatus.PLANNED, Task.TaskStatus.SENDING).contains(task.getStatus()) && (facState!= FacilityState.FacilityPropagationState.ERROR)) {
				state.setState(FacilityState.FacilityPropagationState.PROCESSING);
			}
			// ERROR - set ERROR
			else if (Arrays.asList(Task.TaskStatus.ERROR, Task.TaskStatus.GENERROR, Task.TaskStatus.SENDERROR).contains(task.getStatus())) {
				state.setState(FacilityState.FacilityPropagationState.ERROR);
			}

			// get destination status
			if (task.getService() != null) {
				List<TaskResult> results = tasksManagerImpl.getTaskResultsByTask(task.getId());

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
						FacilityState.FacilityPropagationState propState = state.getResults().get(destination);
						// if any error => state is error
						if (TaskResult.TaskResultStatus.ERROR.equals(result.getStatus())) {
							state.getResults().put(destination, FacilityState.FacilityPropagationState.ERROR);
							continue;
						}
						// if result ok and previous was not bad
						if (TaskResult.TaskResultStatus.DONE.equals(result.getStatus())) {
							if (FacilityState.FacilityPropagationState.NOT_DETERMINED.equals(propState)) {
								state.getResults().put(destination, FacilityState.FacilityPropagationState.OK);
							}
						}
					}

				}
			}

		}
		return state;

	}

	@Override
	public List<FacilityState> getAllFacilitiesStates(PerunSession session) throws InternalErrorException, FacilityNotExistsException {
		List<FacilityState> list = new ArrayList<>();

		// return facilities where user is admin or all if perun admin
		List<Facility> facs = perun.getFacilitiesManagerBl().getFacilities(session);
		Collections.sort(facs);
		for (Facility facility : facs) {
			list.add(getFacilityState(session, facility));
		}
		return list;
	}

	@Override
	public List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws InternalErrorException, VoNotExistsException, FacilityNotExistsException {
		perun.getVosManagerBl().checkVoExists(session, vo);

		List<FacilityState> list = new ArrayList<>();
		List<RichResource> facs = perun.getResourcesManagerBl().getRichResources(session, vo);

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
		return tasksManagerImpl.getTaskResultById(taskResultId);
	}

	public List<TaskResult> getTaskResultsForDestinations(PerunSession session, List<String> destinationsNames) throws InternalErrorException {
		return tasksManagerImpl.getTaskResultsForDestinations(destinationsNames);
	}

	@Override
	public List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws VoNotExistsException, InternalErrorException {
		perun.getVosManagerBl().checkVoExists(session, vo);

		List<Resource> resources = perun.getResourcesManagerBl().getResources(session, vo);
		List<ResourceState> resourceStateList = new ArrayList<>();

		for (Resource resource : resources) {
			List<Task> taskList = listAllTasksForFacility(resource.getFacilityId());

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
	public List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) throws InternalErrorException {

		Map<Service, ServiceState> serviceStates = new HashMap<>();

		// fill states for all services which are currently on facility
		for (Service service : perun.getServicesManagerBl().getAssignedServices(sess, facility)) {

			serviceStates.put(service, new ServiceState(service, facility));
			serviceStates.get(service).setBlockedOnFacility(getServicesManagerBl().isServiceBlockedOnFacility(service, facility));

			// service has destination on facility
			serviceStates.get(service).setHasDestinations(!perun.getServicesManagerBl().getDestinations(sess, service, facility).isEmpty());

		}

		// fill states for all tasks on facility

		List<Task> tasks = tasksManagerImpl.listAllTasksForFacility(facility.getId());

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
			serviceStates.get(taskService).setBlockedOnFacility(getServicesManagerBl().isServiceBlockedOnFacility(task.getService(), facility));

		}

		return new ArrayList<>(serviceStates.values());

	}

	@Override
	public void deleteTask(PerunSession sess, Task task) throws InternalErrorException {

		Facility facility = task.getFacility();
		Service service = task.getService();

		// clear all task results
		tasksManagerImpl.clearByTask(task.getId());

		// remove task itself
		tasksManagerImpl.removeTask(service, facility);

	}

	public ServicesManagerBl getServicesManagerBl() {
		return servicesManagerBl;
	}

	public void setServicesManagerBl(ServicesManagerBl servicesManagerBl) {
		this.servicesManagerBl = servicesManagerBl;
	}

	public void setPerunBl(PerunBlImpl perunBl) {
		this.perun = perunBl;
	}
}
