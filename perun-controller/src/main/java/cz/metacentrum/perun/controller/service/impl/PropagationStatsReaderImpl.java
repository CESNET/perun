package cz.metacentrum.perun.controller.service.impl;

import java.util.*;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.controller.model.FacilityState;
import cz.metacentrum.perun.controller.model.FacilityState.FacilityPropagationState;
import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.controller.service.PropagationStatsReader;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.taskslib.dao.TaskDao;
import cz.metacentrum.perun.taskslib.dao.TaskResultDao;
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

  @Override
  public Task getTask(PerunSession perunSession, ExecService execService, Facility facility) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.getTask(execService, facility);
  }

  @Override
  public Task getTaskById(PerunSession perunSession, int id) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.getTaskById(id);
  }

  @Override
  public List<Task> listAllTasks(PerunSession perunSession) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.listAllTasks();
  }

  @Override
  public List<Task> listAllTasksForFacility(PerunSession session, int facilityId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.listAllTasksForFacility(facilityId);
  }

  @Override
  public List<Task> listAllTasksInState(PerunSession perunSession,TaskStatus state) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.listAllTasksInState(state);
  }

  @Override
  public List<Task> listTasksScheduledBetweenDates(PerunSession perunSession, Date olderThen, Date youngerThen) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.listTasksScheduledBetweenDates(olderThen, youngerThen);
  }

  @Override
  public List<Task> listTasksStartedBetweenDates(PerunSession perunSession,Date olderThen, Date youngerThen) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.listTasksStartedBetweenDates(olderThen, youngerThen);
  }

  @Override
  public List<Task> listTasksEndedBetweenDates(PerunSession perunSession,Date olderThen, Date youngerThen) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.listTasksEndedBetweenDates(olderThen, youngerThen);
  }

  @Override
  public boolean isThereSuchTask(ExecService execService, Facility facility) {
    return taskDao.isThereSuchTask(execService, facility);
  }

  @Override
  public int countTasks() {
    return taskDao.countTasks();
  }

  @Override
  public Task getTask(PerunSession perunSession,int execServiceId, int facilityId) throws ServiceNotExistsException, InternalErrorException, PrivilegeException {
    return taskDao.getTask(execServiceId, facilityId);
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
  public List<TaskResult> getTaskResultsForGUIByTask(PerunSession session, int taskId) throws DestinationNotExistsException, PrivilegeException, InternalErrorException {
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
        if (tasks.isEmpty() || tasks == null) {
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
            if (TaskStatus.PROCESSING.equals(task.getStatus()) && (facState!=FacilityPropagationState.ERROR)) {
                state.setState(FacilityPropagationState.PROCESSING);
            }
            // ERROR - set ERROR
            else if (TaskStatus.ERROR.equals(task.getStatus())) {
                state.setState(FacilityPropagationState.ERROR);
            }

            // get destination status
            if (task.getExecService().getExecServiceType().equals(ExecService.ExecServiceType.SEND)) {
                List<TaskResult> results = taskResultDao.getTaskResultsByTask(task.getId());
                for (TaskResult res : results) {

                    String destination = res.getDestination().getDestination();
                    FacilityPropagationState propState = state.getResults().get(destination);
                    // if any error => state is error
                    if (TaskResult.TaskResultStatus.ERROR.equals(res.getStatus())){
                        state.getResults().put(destination, FacilityPropagationState.ERROR);
                        continue;
                    }
                    // if result ok and previous was not bad
                    if (TaskResult.TaskResultStatus.DONE.equals(res.getStatus())) {
                        if (FacilityPropagationState.NOT_DETERMINED.equals(propState)) {
                            state.getResults().put(destination, FacilityPropagationState.OK);
                        }
                    }

                }
            }

        }
        return state;

    }

    @Override
    public List<FacilityState> getAllFacilitiesStates(PerunSession session) throws InternalErrorException, PrivilegeException, FacilityNotExistsException, UserNotExistsException {
        List<FacilityState> list = new ArrayList<FacilityState>();
        List<Facility> facs = new ArrayList<Facility>();

        // return facilities where user is admin or all if perun admin
        facs = perun.getFacilitiesManager().getFacilities(session);

        for (Facility facility : facs) {
            list.add(getFacilityState(session, facility));
        }
        return list;
    };

    @Override
    public List<FacilityState> getAllFacilitiesStatesForVo(PerunSession session, Vo vo) throws InternalErrorException, PrivilegeException, VoNotExistsException, FacilityNotExistsException, UserNotExistsException {

        List<FacilityState> list = new ArrayList<FacilityState>();
        List<RichResource> facs = new ArrayList<RichResource>();
        facs = perun.getResourcesManager().getRichResources(session, vo);

        Set<Facility> facilities = new HashSet<Facility>();
        for (RichResource res : facs) {
            facilities.add(res.getFacility());
        }
        for (Facility f : facilities) {
            list.add(getFacilityState(session, f));
        }
        return list;
    };

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

  public List<TaskResult> getTaskResultsForDestinations(PerunSession session, List<String> destinationsNames) throws InternalErrorException, PrivilegeException {
    
    //FIXME check privileges, propably only some monitoring system can request these data
    return getTaskResultDao().getTaskResultsForDestinations(destinationsNames);
  }
}
