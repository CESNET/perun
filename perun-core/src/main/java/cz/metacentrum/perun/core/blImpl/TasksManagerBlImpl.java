package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.ResourceState;
import cz.metacentrum.perun.controller.model.ServiceState;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.RichResource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.ServicesManagerBl;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.core.implApi.TasksManagerImplApi;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.TaskResult;
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
 * TasksManagerBlImpl
 */
public class TasksManagerBlImpl implements TasksManagerBl {

	@Autowired
	private ServicesManagerBl servicesManagerBl;
	@Autowired
	protected PerunBl perun;
	@Autowired
	protected TasksManagerImplApi tasksManagerImpl;

	private static boolean suspendedTasksPropagation = false; //are tasks stopped from propagating to engine

	// -------------- constructors
	
	public TasksManagerBlImpl(TasksManagerImplApi tasksManagerImpl) {
		this.tasksManagerImpl = tasksManagerImpl;
	}

	// -------------- getters and setters
	
	public PerunBl getPerunBl() {
		return perun;
	}

	public void setPerunBl(PerunBlImpl perunBl) {
		this.perun = perunBl;
	}

	public ServicesManagerBl getServicesManagerBl() {
		return servicesManagerBl;
	}

	public void setServicesManagerBl(ServicesManagerBl servicesManagerBl) {
		this.servicesManagerBl = servicesManagerBl;
	}

	// -------------- methods

	@Override
	public int countTasks() {
		return getTasksManagerImpl().countTasks();
	}

	@Override
	public int deleteAllTaskResults(PerunSession sess) {
		return getTasksManagerImpl().deleteAllTaskResults();
	}

	@Override
	public int deleteOldTaskResults(PerunSession sess, int numDays) {
		return getTasksManagerImpl().deleteOldTaskResults(numDays);
	}

	@Override
	public void deleteTask(PerunSession sess, Task task) {

		Facility facility = task.getFacility();
		Service service = task.getService();

		// clear all task results
		getTasksManagerImpl().deleteTaskResults(task.getId());

		// remove task itself
		getTasksManagerImpl().removeTask(service, facility);

	}

	@Override
	public void deleteTaskResultById(PerunSession sess, int taskResultId) {
		getTasksManagerImpl().deleteTaskResultById(taskResultId);
	}

	@Override
	public int deleteTaskResults(PerunSession sess, int taskId) {
		return getTasksManagerImpl().deleteTaskResults(taskId);
	}

	@Override
	public int deleteTaskResults(PerunSession sess, int taskId, int destinationId) {
		return getTasksManagerImpl().deleteTaskResults(taskId, destinationId);
	}

	@Override
	public List<FacilityState> getAllFacilitiesStates(PerunSession session) throws FacilityNotExistsException {
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
	public List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws VoNotExistsException, FacilityNotExistsException {
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
	public List<ServiceState> getFacilityServicesState(PerunSession sess, Facility facility) {

		Map<Service, ServiceState> serviceStates = new HashMap<>();

		// fill states for all services which are currently on facility
		for (Service service : perun.getServicesManagerBl().getAssignedServices(sess, facility)) {

			serviceStates.put(service, new ServiceState(service, facility));
			serviceStates.get(service).setBlockedOnFacility(getServicesManagerBl().isServiceBlockedOnFacility(service, facility));

			// service has destination on facility
			serviceStates.get(service).setHasDestinations(!perun.getServicesManagerBl().getDestinations(sess, service, facility).isEmpty());

		}

		// fill states for all tasks on facility

		List<Task> tasks = getTasksManagerImpl().listAllTasksForFacility(facility.getId());

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
	public FacilityState getFacilityState(PerunSession session, Facility facility) throws FacilityNotExistsException {
		perun.getFacilitiesManagerBl().checkFacilityExists(session, facility);

		// get all tasks
		List<Task> tasks = getTasksManagerImpl().listAllTasksForFacility(facility.getId());
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
				List<TaskResult> results = getTasksManagerImpl().getTaskResultsByTask(task.getId());

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
	public List<ResourceState> getResourcesState(PerunSession session, Vo vo) throws VoNotExistsException {
		perun.getVosManagerBl().checkVoExists(session, vo);

		List<Resource> resources = perun.getResourcesManagerBl().getResources(session, vo);
		List<ResourceState> resourceStateList = new ArrayList<>();

		for (Resource resource : resources) {
			List<Task> taskList = listAllTasksForFacility(session, resource.getFacilityId());

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
	public Task getTask(PerunSession perunSession, Service service, Facility facility) {
		return getTasksManagerImpl().getTask(service, facility);
	}

	@Override
	public Task getTaskById(PerunSession perunSession, int id) {
		return getTasksManagerImpl().getTaskById(id);
	}

	@Override
	public TaskResult getTaskResultById(PerunSession sess, int taskResultId) {
		return getTasksManagerImpl().getTaskResultById(taskResultId);
	}

	@Override
	public List<TaskResult> getTaskResults(PerunSession sess) {
		return getTasksManagerImpl().getTaskResults();
	}

	@Override
	public List<TaskResult> getTaskResultsByTask(PerunSession sess, int taskId) {
		return getTasksManagerImpl().getTaskResultsByTask(taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsByTaskAndDestination(PerunSession sess, int taskId, int destinationId) {
		return getTasksManagerImpl().getTaskResultsByTaskAndDestination(taskId, destinationId);
	}

	@Override
	public List<TaskResult> getTaskResultsByTaskOnlyNewest(PerunSession sess, int taskId) {
		return getTasksManagerImpl().getTaskResultsByTaskOnlyNewest(taskId);
	}

	@Override
	public List<TaskResult> getTaskResultsByDestinations(PerunSession session, List<String> destinationsNames) {
		return getTasksManagerImpl().getTaskResultsByDestinations(destinationsNames);
	}

	public TasksManagerImplApi getTasksManagerImpl() {
		return tasksManagerImpl;
	}

	@Override
	public int insertNewTaskResult(PerunSession sess, TaskResult taskResult) {
		return getTasksManagerImpl().insertNewTaskResult(taskResult);
	}

	@Override
	public int insertTask(PerunSession sess, Task task) {
		return getTasksManagerImpl().insertTask(task);
	}

	@Override
	public boolean isThereSuchTask(PerunSession sess, Service service, Facility facility) {
		return getTasksManagerImpl().isThereSuchTask(service, facility);
	}

	@Override
	public List<Task> listAllTasks(PerunSession perunSession) {
		return getTasksManagerImpl().listAllTasks();
	}

	@Override
	public List<Task> listAllTasksForFacility(PerunSession session, int facilityId) {
		return getTasksManagerImpl().listAllTasksForFacility(facilityId);
	}

	@Override
	public List<Task> listAllTasksForService(PerunSession sess, int serviceId) {
		return getTasksManagerImpl().listAllTasksForService(serviceId);
	}

	@Override
	public List<Task> listAllTasksInState(PerunSession perunSession, Task.TaskStatus state) {
		return getTasksManagerImpl().listAllTasksInState(state);
	}

	@Override
	public List<Task> listAllTasksNotInState(PerunSession sess, Task.TaskStatus state) {
		return getTasksManagerImpl().listAllTasksNotInState(state);
	}

	@Override
	public void removeAllTasksForService(PerunSession sess, Service service) {
		for(Task task : listAllTasksForService(sess, service.getId())) {
			getTasksManagerImpl().deleteTaskResults(task.getId());
			removeTask(sess, task.getId());
		}
	}

	@Override
	public void removeTask(PerunSession sess, int id) {
		getTasksManagerImpl().removeTask(id);
	}


	@Override
	public void removeTask(PerunSession sess, Service service, Facility facility) {
		getTasksManagerImpl().removeTask(service, facility);
	}

	@Override
	public void updateTask(PerunSession sess, Task task) {
		getTasksManagerImpl().updateTask(task);
	}

	@Override
	public void suspendTasksPropagation(PerunSession perunSession, boolean suspend) {
		synchronized(TasksManagerBlImpl.class) {
			suspendedTasksPropagation = suspend;
		}
	}

	@Override
	public boolean isSuspendedTasksPropagation() {
		synchronized(TasksManagerBlImpl.class) {
			return suspendedTasksPropagation;
		}
	}

}
