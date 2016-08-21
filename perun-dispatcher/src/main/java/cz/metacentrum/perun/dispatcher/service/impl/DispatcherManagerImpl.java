package cz.metacentrum.perun.dispatcher.service.impl;

import cz.metacentrum.perun.dispatcher.exceptions.PerunHornetQServerException;
import cz.metacentrum.perun.dispatcher.hornetq.PerunHornetQServer;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducerPool;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProcessor;
import cz.metacentrum.perun.dispatcher.processing.AuditerListener;
import cz.metacentrum.perun.dispatcher.processing.EventProcessor;
import cz.metacentrum.perun.dispatcher.processing.SmartMatcher;
import cz.metacentrum.perun.dispatcher.scheduling.PropagationMaintainer;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler;
import cz.metacentrum.perun.dispatcher.service.DispatcherManager;
import cz.metacentrum.perun.taskslib.service.ResultManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.util.Properties;

/**
 * Implementation of DispatcherManager.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author David Šarman
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class DispatcherManagerImpl implements DispatcherManager {

	private final static Logger log = LoggerFactory.getLogger(DispatcherManagerImpl.class);

	private PerunHornetQServer perunHornetQServer;
	private EngineMessageProcessor engineMessageProcessor;
	private EventProcessor eventProcessor;
	private SmartMatcher smartMatcher;
	private SchedulingPool schedulingPool;
	private EngineMessageProducerPool engineMessageProducerPool;
	private ResultManager resultManager;
	private TaskScheduler taskScheduler;
	private TaskExecutor taskExecutor;
	private AuditerListener auditerListener;
	private Properties dispatcherProperties;
	private PropagationMaintainer propagationMaintainer;

	// allow cleaning of old TaskResults
	private boolean cleanTaskResultsJobEnabled = true;


	// ----- setters -------------------------------------


	public PerunHornetQServer getPerunHornetQServer() {
		return perunHornetQServer;
	}

	@Autowired
	public void setPerunHornetQServer(PerunHornetQServer perunHornetQServer) {
		this.perunHornetQServer = perunHornetQServer;
	}

	public EngineMessageProcessor getEngineMessageProcessor() {
		return engineMessageProcessor;
	}

	@Autowired
	public void setEngineMessageProcessor(EngineMessageProcessor engineMessageProcessor) {
		this.engineMessageProcessor = engineMessageProcessor;
	}

	public EventProcessor getEventProcessor() {
		return eventProcessor;
	}

	@Autowired
	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	public SmartMatcher getSmartMatcher() {
		return smartMatcher;
	}

	@Autowired
	public void setSmartMatcher(SmartMatcher smartMatcher) {
		this.smartMatcher = smartMatcher;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	@Autowired
	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	public EngineMessageProducerPool getEngineMessageProducerPool() {
		return engineMessageProducerPool;
	}

	@Autowired
	public void setEngineMessageProducerPool(EngineMessageProducerPool engineMessageProducerPool) {
		this.engineMessageProducerPool = engineMessageProducerPool;
	}

	public ResultManager getResultManager() {
		return resultManager;
	}

	@Autowired
	public void setResultManager(ResultManager resultManager) {
		this.resultManager = resultManager;
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	@Autowired
	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	@Autowired
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public AuditerListener getAuditerListener() {
		return auditerListener;
	}

	@Autowired
	public void setAuditerListener(AuditerListener auditerListener) {
		this.auditerListener = auditerListener;
	}

	public Properties getDispatcherProperties() {
		return dispatcherProperties;
	}

	@Resource(name="dispatcherPropertiesBean")
	public void setDispatcherProperties(Properties dispatcherProperties) {
		this.dispatcherProperties = dispatcherProperties;
	}

	public PropagationMaintainer getPropagationMaintainer() {
		return propagationMaintainer;
	}

	@Autowired
	public void setPropagationMaintainer(PropagationMaintainer propagationMaintainer) {
		this.propagationMaintainer = propagationMaintainer;
	}

	public boolean isCleanTaskResultsJobEnabled() {
		return cleanTaskResultsJobEnabled;
	}

	public void setCleanTaskResultsJobEnabled(boolean enabled) {
		this.cleanTaskResultsJobEnabled = enabled;
	}


	// ----- methods -------------------------------------


	@Override
	public void startPerunHornetQServer() {
		perunHornetQServer.startServer();
	}

	@Override
	public void stopPerunHornetQServer() {
		perunHornetQServer.stopServer();
	}

	@Override
	public void prefetchRulesAndDispatcherQueues() throws PerunHornetQServerException {
		smartMatcher.loadAllRulesFromDB();
		engineMessageProcessor.createDispatcherQueuesForClients(smartMatcher.getClientsWeHaveRulesFor());
	}

	@Override
	public void startProcessingSystemMessages() {
		engineMessageProcessor.startProcessingSystemMessages();
	}

	@Override
	public void stopProcessingSystemMessages() {
		engineMessageProcessor.stopProcessingSystemMessages();
	}

	@Override
	public void startAuditerListener() {
		try {
			taskExecutor.execute(auditerListener);
		} catch (Exception ex) {
			log.error("Unable to start AuditerListener thread.");
		}
	}

	@Override
	public void stopAuditerListener() {
		auditerListener.stop();
	}

	@Override
	public void startProcessingEvents() {
		try {
			taskExecutor.execute(eventProcessor);
		} catch (Exception ex) {
			log.error("Unable to start EventProcessor thread.");
		}
	}

	@Override
	public void stopProcessingEvents() {
		eventProcessor.stop();
	}

	@Override
	public void loadSchedulingPool() {
		schedulingPool.reloadTasks();
	}

	@Override
	public void startTasksScheduling() {
		try {
			taskExecutor.execute(taskScheduler);
		} catch (Exception ex) {
			log.error("Unable to start TaskScheduler thread.");
		}
	}

	@Override
	public void stopTaskScheduling() {
		taskScheduler.stop();
	}

	@Override
	public void startPropagationMaintaining() {
		try {
			taskExecutor.execute(propagationMaintainer);
		} catch (Exception ex) {
			log.error("Unable to start PropagationMaintainer thread.");
		}
	}

	@Override
	public void stopPropagationMaintaining() {
		propagationMaintainer.stop();
	}

	@Override
	public void cleanOldTaskResults() {
		if (cleanTaskResultsJobEnabled) {
			for (EngineMessageProducer queue : engineMessageProducerPool.getPool()) {
				try {
					int numRows = resultManager.clearOld(queue.getClientID(), 3);
					log.debug("Cleaned {} old task results for engine {}", numRows, queue.getClientID());
				} catch (Throwable e) {
					log.error("Error cleaning old task results for engine {} : {}", queue.getClientID(), e);
				}
			}
		} else {
			log.debug("Cleaning of old task results is disabled.");
		}
	}

	/**
	 * Main initialization method. Loads all data and starts all scheduling a processing threads.
	 */
	public final void init() {

		String dispatcherEnabled = dispatcherProperties.getProperty("dispatcher.enabled");

		// skip start of HornetQ and other dispatcher jobs if dispatcher is disabled
		if(dispatcherEnabled != null && !Boolean.parseBoolean(dispatcherEnabled)) {
			cleanTaskResultsJobEnabled = false;
			log.debug("Perun-Dispatcher startup disabled by configuration.");
			return;
		}

		// dispatcher is enabled

		try {

			// Start HornetQ server
			startPerunHornetQServer();
			// Start System Queue Processor
			startProcessingSystemMessages();
			// Prefetch rules for all the Engines in the Perun DB and create
			prefetchRulesAndDispatcherQueues();
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

			log.info("Perun-Dispatcher has started.");

		} catch (Exception e) {
			log.error("Unable to start Perun-Dispatcher: {}.", e);
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

}
