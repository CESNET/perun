package cz.metacentrum.perun.dispatcher.service.impl;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.bl.TasksManagerBl;
import cz.metacentrum.perun.dispatcher.hornetq.PerunHornetQServer;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProcessor;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducerFactory;
import cz.metacentrum.perun.dispatcher.processing.AuditerListener;
import cz.metacentrum.perun.dispatcher.processing.EventProcessor;
import cz.metacentrum.perun.dispatcher.scheduling.PropagationMaintainer;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler;
import cz.metacentrum.perun.dispatcher.service.DispatcherManager;
import java.util.Properties;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

/**
 * Implementation of DispatcherManager.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class DispatcherManagerImpl implements DispatcherManager {

  private static final Logger LOG = LoggerFactory.getLogger(DispatcherManagerImpl.class);

  private PerunHornetQServer perunHornetQServer;
  private EngineMessageProcessor engineMessageProcessor;
  private EventProcessor eventProcessor;
  private SchedulingPool schedulingPool;
  private EngineMessageProducerFactory engineMessageProducerPool;
  private TasksManagerBl tasksManagerBl;
  private TaskScheduler taskScheduler;
  private TaskExecutor taskExecutor;
  private AuditerListener auditerListener;
  private Properties dispatcherProperties;
  private PropagationMaintainer propagationMaintainer;

  @Autowired
  private Perun perun;

  // allow cleaning of old TaskResults
  private boolean cleanTaskResultsJobEnabled = true;


  // ----- setters -------------------------------------

  @Override
  public void cleanOldTaskResults() {
    if (cleanTaskResultsJobEnabled) {
      try {
        PerunSession sess = perun.getPerunSession(
            new PerunPrincipal(dispatcherProperties.getProperty("perun.principal.name"),
                dispatcherProperties.getProperty("perun.principal.extSourceName"),
                dispatcherProperties.getProperty("perun.principal.extSourceType")), new PerunClient());
        int numRows = tasksManagerBl.deleteOldTaskResults(sess, 3);
        LOG.debug("Cleaned {} old task results for engine", numRows);
      } catch (Throwable e) {
        LOG.error("Error cleaning old task results for engine: {}", e);
      }
    } else {
      LOG.debug("Cleaning of old task results is disabled.");
    }
  }

  /**
   * Stop all processing when application is shut down.
   */
  @PreDestroy
  public void destroy() {
    // stop current scheduler
    cleanTaskResultsJobEnabled = false;
    // stop currently running jobs
    stopAuditerListener();
    stopProcessingEvents();
    stopTaskScheduling();
    stopPropagationMaintaining();
    stopProcessingSystemMessages();
    stopPerunHornetQServer();
  }

  public AuditerListener getAuditerListener() {
    return auditerListener;
  }

  public Properties getDispatcherProperties() {
    return dispatcherProperties;
  }

  public EngineMessageProcessor getEngineMessageProcessor() {
    return engineMessageProcessor;
  }

  public EngineMessageProducerFactory getEngineMessageProducerPool() {
    return engineMessageProducerPool;
  }

  public EventProcessor getEventProcessor() {
    return eventProcessor;
  }

  public PerunHornetQServer getPerunHornetQServer() {
    return perunHornetQServer;
  }

  public PropagationMaintainer getPropagationMaintainer() {
    return propagationMaintainer;
  }

  public SchedulingPool getSchedulingPool() {
    return schedulingPool;
  }

  public TaskExecutor getTaskExecutor() {
    return taskExecutor;
  }

  public TaskScheduler getTaskScheduler() {
    return taskScheduler;
  }

  public TasksManagerBl getTasksManagerBl() {
    return tasksManagerBl;
  }

  /**
   * Main initialization method. Loads all data and starts all scheduling a processing threads.
   */
  public final void init() {

    String dispatcherEnabled = dispatcherProperties.getProperty("dispatcher.enabled");

    // skip start of HornetQ and other dispatcher jobs if dispatcher is disabled
    if (dispatcherEnabled != null && !Boolean.parseBoolean(dispatcherEnabled)) {
      cleanTaskResultsJobEnabled = false;
      LOG.info("Perun-Dispatcher startup disabled by configuration.");
      return;
    }

    // dispatcher is enabled

    try {

      // Start HornetQ server
      startPerunHornetQServer();
      // Start System Queue Processor
      startProcessingSystemMessages();
      // Reload tasks from database
      loadSchedulingPool();
      // Start listening to Audit messages
      startAuditerListener();
      // Start Event Processor
      startProcessingEvents();
      // Start Task scheduling
      startTasksScheduling();
      // Start rescheduling of done/error or stuck Tasks
      startPropagationMaintaining();

      LOG.info("Perun-Dispatcher has started.");

    } catch (Exception e) {
      LOG.error("Unable to start Perun-Dispatcher: {}.", e);
    }

  }

  public boolean isCleanTaskResultsJobEnabled() {
    return cleanTaskResultsJobEnabled;
  }

  @Override
  public void loadSchedulingPool() {
    schedulingPool.reloadTasks();
  }

  @Autowired
  public void setAuditerListener(AuditerListener auditerListener) {
    this.auditerListener = auditerListener;
  }

  public void setCleanTaskResultsJobEnabled(boolean enabled) {
    this.cleanTaskResultsJobEnabled = enabled;
  }

  @Resource(name = "dispatcherPropertiesBean")
  public void setDispatcherProperties(Properties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
  }

  @Autowired
  public void setEngineMessageProcessor(EngineMessageProcessor engineMessageProcessor) {
    this.engineMessageProcessor = engineMessageProcessor;
  }

  @Autowired
  public void setEngineMessageProducerPool(EngineMessageProducerFactory engineMessageProducerPool) {
    this.engineMessageProducerPool = engineMessageProducerPool;
  }

  @Autowired
  public void setEventProcessor(EventProcessor eventProcessor) {
    this.eventProcessor = eventProcessor;
  }

  @Autowired
  public void setPerunHornetQServer(PerunHornetQServer perunHornetQServer) {
    this.perunHornetQServer = perunHornetQServer;
  }

  @Autowired
  public void setPropagationMaintainer(PropagationMaintainer propagationMaintainer) {
    this.propagationMaintainer = propagationMaintainer;
  }


  // ----- methods -------------------------------------

  @Autowired
  public void setSchedulingPool(SchedulingPool schedulingPool) {
    this.schedulingPool = schedulingPool;
  }

  @Autowired
  public void setTaskExecutor(TaskExecutor taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  @Autowired
  public void setTaskScheduler(TaskScheduler taskScheduler) {
    this.taskScheduler = taskScheduler;
  }

  @Autowired
  public void setTasksManagerBl(TasksManagerBl tasksManagerBl) {
    this.tasksManagerBl = tasksManagerBl;
  }

  @Override
  public void startAuditerListener() {
    try {
      taskExecutor.execute(auditerListener);
    } catch (Exception ex) {
      LOG.error("Unable to start AuditerListener thread.");
    }
  }

  @Override
  public void startPerunHornetQServer() {
    perunHornetQServer.startServer();
  }

  @Override
  public void startProcessingEvents() {
    try {
      taskExecutor.execute(eventProcessor);
    } catch (Exception ex) {
      LOG.error("Unable to start EventProcessor thread.");
    }
  }

  @Override
  public void startProcessingSystemMessages() {
    engineMessageProcessor.startProcessingSystemMessages();
  }

  @Override
  public void startPropagationMaintaining() {
    try {
      taskExecutor.execute(propagationMaintainer);
    } catch (Exception ex) {
      LOG.error("Unable to start PropagationMaintainer thread.");
    }
  }

  @Override
  public void startTasksScheduling() {
    try {
      taskExecutor.execute(taskScheduler);
    } catch (Exception ex) {
      LOG.error("Unable to start TaskScheduler thread.");
    }
  }

  @Override
  public void stopAuditerListener() {
    auditerListener.stop();
  }

  @Override
  public void stopPerunHornetQServer() {
    perunHornetQServer.stopServer();
  }

  @Override
  public void stopProcessingEvents() {
    eventProcessor.stop();
  }

  @Override
  public void stopProcessingSystemMessages() {
    engineMessageProcessor.stopProcessingSystemMessages();
  }

  @Override
  public void stopPropagationMaintaining() {
    propagationMaintainer.stop();
  }

  @Override
  public void stopTaskScheduling() {
    taskScheduler.stop();
  }

}
