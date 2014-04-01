package cz.metacentrum.perun.engine.integration;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.engine.BaseTest;
import cz.metacentrum.perun.engine.processing.EventProcessor;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.engine.scheduling.TaskScheduler;
import cz.metacentrum.perun.taskslib.service.TaskManager;

/**
 * @author Michal Karm Babacek
 *
 *         Unfortunately, this test can not be transactional due to multi-threaded environment, hence it can not be rolled back. We have to clean up after ourselves...
 */
//@Transactional(propagation = Propagation.NEVER)
public class TaskSchedulerIntegrationTest extends BaseTest {

	private final static Logger log = LoggerFactory.getLogger(TaskSchedulerIntegrationTest.class);
	// Time out for threads to complete (milliseconds)
	private final static int TIME_OUT = 20000;

	@Autowired
	private TaskScheduler taskScheduler;
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private TaskExecutor taskExecutor;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private Properties propertiesBean;
	@Autowired
	private EventProcessor eventProcessor;
	private Set<Integer> tasks = null;

	@PostConstruct
	public void setUp() {
		log.debug("Gonna do @PostConstruct SETUP...");
		initJdbcTemplate();
	}

	@Before
	public void init() throws Exception {
		intiIt();
	}

	@Before
	public void rememberState() {
		tasks = new HashSet<Integer>(getJdbcTemplate().queryForList("select id from tasks", Integer.class));
	}

	@After
	public void returnToRememberedState() {
		Set<Integer> currentTasks = new HashSet<Integer>(getJdbcTemplate().queryForList("select id from tasks", Integer.class));
		// Difference
		currentTasks.removeAll(tasks);
		// Log
		log.debug("We are gonna delete TasksResults for Tasks:" + currentTasks.toString());
		for (Integer id : currentTasks) {
			getJdbcTemplate().update("delete from tasks_results where tasks_results.task_id = ?", id);
		}
		log.debug("We are gonna delete Tasks:" + currentTasks.toString());
		for (Integer id : currentTasks) {
			getJdbcTemplate().update("delete from tasks where tasks.id = ?", id);
		}
	}

	@Test
	public void testSchedulingRealMessage() throws InterruptedException {
		String message = "event|1|[Tue Aug 30 12:29:23 CEST 2011][clockworkorange][Member:[id='36712'] added to Group:[id='16326', name='falcon', description='null'].]";
		EventProcessorWorker eventProcessorWorker = new EventProcessorWorker(message);
		taskExecutor.execute(eventProcessorWorker);
		boolean itWentOk = false;
		long started = System.currentTimeMillis();
		while (System.currentTimeMillis() - started < TIME_OUT) {
			if (taskScheduler.getPoolSize() >= 1) {
				itWentOk = true;
				log.debug("     #hashAH23 OK, we have " + taskScheduler.getPoolSize() + " ExecService-Facility pairs in the pool.");
				break;
			} else {
				log.debug("     #hashAH23 There are only " + taskScheduler.getPoolSize() + " ExecService-Facility pairs in the pool.");
			}
			Thread.sleep(500);
		}
		if (!itWentOk) {
			fail("#hashAH23 Waiting for SchedilingPool has timed out.");
		}

		TaskSchedulerWorker taskSchedulerWorker = new TaskSchedulerWorker();
		taskExecutor.execute(taskSchedulerWorker);
		started = System.currentTimeMillis();
		itWentOk = false;
		while (System.currentTimeMillis() - started < TIME_OUT) {
			itWentOk = true;
			/*
				 No no no, PasswdSend can not be ready by this time, because we have PasswdGenerate in PLANNED.
				 if (taskManager.getTask(getExecServicePasswdSend(), getFacility1195(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))) == null) {
				 itWentOk = false;
				 log.debug("###TASK for ExecServicePasswdSend ("+getExecServicePasswdSend().getId()+") and Facility1195 ("+getFacility1195().getId()+") has not been found.");
				 }*/
			if (taskManager.getTask(getExecServicePasswdGenerate(), getFacility1195(), Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))) == null) {
				itWentOk = false;
				log.debug("###TASK for ExecServicePasswdGenerate ("+getExecServicePasswdGenerate().getId()+") and Facility1195 ("+getFacility1195().getId()+") has not been found.");
			}
			Thread.sleep(500);
			if (itWentOk) {
				break;
			}
			log.debug("###TASK TASKS were:" + taskManager.listAllTasks(Integer.parseInt(propertiesBean.getProperty("engine.unique.id"))).toString());
		}
		if (!itWentOk) {
			fail("#hashAH23 Waiting for TaskScheduler has timed out.");
		}
	}

	private class EventProcessorWorker implements Runnable {
		private String message;

		public EventProcessorWorker(String message) {
			super();
			this.message = message;
		}

		@Override
		public void run() {
			eventProcessor.receiveEvent(message);
		}

	}

	private class TaskSchedulerWorker implements Runnable {
		@Override
		public void run() {
			try {
				taskScheduler.processPool();
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public SchedulingPool getSchedulingPool() {
		return schedulingPool;
	}

	public void setSchedulingPool(SchedulingPool schedulingPool) {
		this.schedulingPool = schedulingPool;
	}

	public EventProcessor getEventProcessor() {
		return eventProcessor;
	}

	public void setEventProcessor(EventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	public Properties getPropertiesBean() {
		return propertiesBean;
	}

	public void setPropertiesBean(Properties propertiesBean) {
		this.propertiesBean = propertiesBean;
	}

}
