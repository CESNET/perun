package cz.metacentrum.perun.engine.scheduling.impl;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.engine.scheduling.ExecutorEngineWorker;
import cz.metacentrum.perun.engine.scheduling.TaskExecutorEngine;
import cz.metacentrum.perun.engine.service.EngineManager;
import cz.metacentrum.perun.rpclib.Rpc;
import cz.metacentrum.perun.taskslib.model.ExecService;
import cz.metacentrum.perun.taskslib.model.ExecService.ExecServiceType;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.service.TaskManager;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
@org.springframework.stereotype.Service(value = "taskExecutorEngine")
@Transactional
public class TaskExecutorEngineImpl implements TaskExecutorEngine {
    private final static Logger log = LoggerFactory.getLogger(TaskExecutorEngineImpl.class);

    @Autowired
    private TaskManager taskManager;
    @Autowired
    private TaskExecutor taskExecutorGenWorkers;
    @Autowired
    private TaskExecutor taskExecutorSendWorkers;
    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private EngineManager engineManager;
    @Autowired
    private Properties propertiesBean;

    @Override
    public void beginExecuting() {
        int executorWorkersCreated = 0;
        log.debug("Begin execution process...");
        List<Task> tasks = null;
        Date olderThen = null;
        Date youngerThen = null;
        // TODO: Just playing with the dates, for now, we are gonna list Tasks in an interval (-1 year, +1 hour) from NOW.
        // TODO: method: listTasksScheduledBefore(youngerThen)
        long hour = 3600000;
        long year = hour * 8760;
        olderThen = new Date(System.currentTimeMillis() - year);
        youngerThen = new Date(System.currentTimeMillis() + hour);
        tasks = taskManager.listTasksScheduledBetweenDates(olderThen, youngerThen, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
        if (tasks != null) {
            log.info("There are " + tasks.size() + " tasks between dates [" + olderThen + "] and [" + youngerThen + "]");
            for (Task task : tasks) {
                // We are about to execute only these tasks that are in the PLANNED state.
                if (task.getStatus().equals(TaskStatus.PLANNED)) {
                    ExecService execService = task.getExecService();

                    // set task to PROCESSING state
                    task.setStatus(TaskStatus.PROCESSING);
                    task.setStartTime(new Date(System.currentTimeMillis()));
                    taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
                    // TODO switch the state when following core failed somewhere
                    // (for example when break is called)

                    if (execService.getExecServiceType().equals(ExecServiceType.GENERATE)) {
                        log.debug("I'm gonna create worker for ExecService GENERATE, ID:" + execService.getId());
                        ExecutorEngineWorker executorEngineWorker = createExecutorEngineWorker();
                        executorWorkersCreated++;
                        executorEngineWorker.setTask(task);
                        executorEngineWorker.setExecService(execService);
                        //A dedicated executor for GENERATE
                        taskExecutorGenWorkers.execute(executorEngineWorker);
                    } else if (execService.getExecServiceType().equals(ExecServiceType.SEND)) {
                        log.debug(   "I'm gonna create worker for ExecService SEND, ID:" + execService.getId());
                        Facility facility = null;
                        List<Destination> destinations = null;
                        try {
                            facility = task.getFacility();
                            destinations = Rpc.ServicesManager.getDestinations(engineManager.getRpcCaller(), execService.getService(), facility);

                            for (Destination destination : destinations) {
                                ExecutorEngineWorker executorEngineWorker = createExecutorEngineWorker();
                                executorWorkersCreated++;
                                executorEngineWorker.setTask(task);
                                executorEngineWorker.setFacility(facility);
                                executorEngineWorker.setExecService(execService);
                                executorEngineWorker.setDestination(destination);
                                //A dedicated executor for SEND
                                taskExecutorSendWorkers.execute(executorEngineWorker);
                            }

                        } catch (FacilityNotExistsException e) {
                            log.error("Skipping this one due to:" + e.toString(), e);
                            task.setStatus(TaskStatus.ERROR);
                            taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
                            continue;
                        } catch (InternalErrorException e) {
                            log.error("Skipping this one due to:" + e.toString(), e);
                            task.setStatus(TaskStatus.ERROR);
                            task.setEndTime(new Date(System.currentTimeMillis()));
                            taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
                            continue;
                        } catch (PrivilegeException e) {
                            log.error("Skipping this one due to:" + e.toString(), e);
                            task.setStatus(TaskStatus.ERROR);
                            task.setEndTime(new Date(System.currentTimeMillis()));
                            taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
                            continue;
                        } catch (ServiceNotExistsException e) {
                            log.error("Skipping this one due to:" + e.toString(), e);
                            task.setStatus(TaskStatus.ERROR);
                            task.setEndTime(new Date(System.currentTimeMillis()));
                            taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
                            continue;
                        } catch (Exception e) {
                            log.error("Skipping this one due to:" + e.toString(), e);
                            task.setStatus(TaskStatus.ERROR);
                            task.setEndTime(new Date(System.currentTimeMillis()));
                            taskManager.updateTask(task, Integer.parseInt(propertiesBean.getProperty("engine.unique.id")));
                            continue;
                        }
                    } else {
                        throw new IllegalArgumentException("ExecService type has to be either SEND or GENERATE.");
                    }
                }
            }
        } else {
            log.debug("There are no tasks between dates [" + olderThen + "] and [" + youngerThen + "]");
        }
        log.info("Executing process ended. Created workers:" + executorWorkersCreated);
    }

    protected ExecutorEngineWorker createExecutorEngineWorker() {
        return (ExecutorEngineWorker) this.beanFactory.getBean("executorEngineWorker");
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public EngineManager getEngineManager() {
        return engineManager;
    }

    public void setEngineManager(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    public Properties getPropertiesBean() {
        return propertiesBean;
    }

    public void setPropertiesBean(Properties propertiesBean) {
        this.propertiesBean = propertiesBean;
    }

    public TaskExecutor getTaskExecutorGenWorkers() {
        return taskExecutorGenWorkers;
    }

    public void setTaskExecutorGenWorkers(TaskExecutor taskExecutorGenWorkers) {
        this.taskExecutorGenWorkers = taskExecutorGenWorkers;
    }

    public TaskExecutor getTaskExecutorSendWorkers() {
        return taskExecutorSendWorkers;
    }

    public void setTaskExecutorSendWorkers(TaskExecutor taskExecutorSendWorkers) {
        this.taskExecutorSendWorkers = taskExecutorSendWorkers;
    }
}
