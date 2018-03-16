package cz.metacentrum.perun.dispatcher.unit;

import cz.metacentrum.perun.auditparser.AuditParser;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.dispatcher.scheduling.impl.TaskScheduled;
import cz.metacentrum.perun.dispatcher.scheduling.impl.SchedulingPoolImpl;
import cz.metacentrum.perun.dispatcher.scheduling.TaskScheduler;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;
import cz.metacentrum.perun.taskslib.model.TaskSchedule;
import cz.metacentrum.perun.taskslib.service.TaskManager;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class TaskSchedulerTest extends AbstractDispatcherTest {

	private final static Logger log = LoggerFactory.getLogger(TaskSchedulerTest.class);

	Destination destination1 = new Destination(1, "par_dest1", "host", "PARALLEL");
	SimpleTaskSchedulerSpy simpleSpy = new SimpleTaskSchedulerSpy(2);
	SimpleTaskSchedulerSpy recurrenceSpy = new SimpleTaskSchedulerSpy(0);
	FutureTask simpleFutureTask = new FutureTask(simpleSpy, null);
	FutureTask recurrenceFutureTask = new FutureTask(recurrenceSpy, null);
	@Autowired
	SchedulingPool schedulingPool;
	@Resource(name="dispatcherPropertiesBean")
	Properties dispatcherProperties;
	@Autowired
	DelayQueue<TaskSchedule> waitingTasksQueue;
	@Autowired
	DelayQueue<TaskSchedule>waitingForcedTasksQueue; 
	@Autowired
	TaskManager taskManager;
	
	@IfProfileValue(name = "perun.test.groups", values = ("xxx"))
	@Test
	public void sendToEngineTest() {
		System.out.println("TaskScheduler.sendToEngine()");

		StringBuilder destinations_s = new StringBuilder("");
		destinations_s.append(destination1.serializeToString());
		destinations_s.append("");

		log.debug("Destination list to parse: " + destinations_s.toString());
		List<PerunBean> listOfBeans;
		List<Destination> destinationList = new ArrayList<Destination>();
		try {
			listOfBeans = AuditParser.parseLog(destination1.serializeToString());
			log.debug("Found list of destination beans: " + listOfBeans);
			for (PerunBean bean : listOfBeans) {
				destinationList.add((Destination) bean);
			}
		} catch (InternalErrorException e) {
			log.error("Could not resolve destination from destination list");
		}
	}

	@Before
	public void setupTests() {
		simpleSpy.setWaitingTasksQueue(waitingTasksQueue);
		simpleSpy.setWaitingForcedTasksQueue(waitingForcedTasksQueue);
		simpleSpy.setTaskManager(taskManager);
		recurrenceSpy.setWaitingTasksQueue(waitingTasksQueue);
		recurrenceSpy.setWaitingForcedTasksQueue(waitingForcedTasksQueue);
		recurrenceSpy.setTaskManager(taskManager);
	}
	
	@Test
	public void simpleRunTest() throws InterruptedException, ExecutionException, TimeoutException {
		//SchedulingPool schedulingPool = new SchedulingPoolImpl();
		simpleSpy.setTask(simpleFutureTask);
		simpleSpy.setSchedulingPool(schedulingPool);
		Long timeLimit = 100L;
		Task[] tasks = simpleSetup(timeLimit, schedulingPool);
		Task testTask1 = tasks[0], testTask2 = tasks[1];
		schedulingPool.scheduleTask(testTask1, 4);
		schedulingPool.scheduleTask(testTask1, 4);
		Thread.sleep(timeLimit / 100);
		schedulingPool.scheduleTask(testTask1, 4);
		Thread.sleep((timeLimit / 100) * 8);
		schedulingPool.scheduleTask(testTask2, 4);

		simpleFutureTask.run();
		assertFalse(simpleSpy.testFailed);
	}

	@Test
	public void sourceUpdatedRunTest() throws InterruptedException, ExecutionException, TimeoutException {
		//SchedulingPool schedulingPool = new SchedulingPoolImpl();
		simpleSpy.setTask(simpleFutureTask);
		simpleSpy.setSchedulingPool(schedulingPool);
		Long timeLimit = 100L;
		Task[] tasks = simpleSetup(timeLimit, schedulingPool);
		Task testTask1 = tasks[0], testTask2 = tasks[1];
		testTask2.setSourceUpdated(true);
		schedulingPool.scheduleTask(testTask2, 4);
		schedulingPool.scheduleTask(testTask1, 4);
		simpleFutureTask.run();
		assertFalse(simpleSpy.testFailed);
	}

	@Test
	public void sourceUpdatedRecurrenceRunTest() throws InterruptedException, ExecutionException, TimeoutException {
		//SchedulingPool schedulingPool = new SchedulingPoolRecurrenceSpy();
		simpleSpy.setTask(recurrenceFutureTask);
		recurrenceSpy.setSchedulingPool(schedulingPool);
		Long timeLimit = 100L;
		Task[] tasks = simpleSetup(timeLimit, schedulingPool);
		Task testTask1 = tasks[0];
		schedulingPool.scheduleTask(testTask1, 2);

		recurrenceFutureTask.run();
		assertFalse(recurrenceSpy.testFailed);
	}

	private Task[] simpleSetup(Long timeLimit, SchedulingPool schedulingPool) {
		Properties properties = ((SchedulingPoolImpl)schedulingPool).getDispatcherProperties();
		if(properties == null) {
			properties = dispatcherProperties;
		}
		properties.setProperty("dispatcher.task.delay.time", timeLimit.toString());
		properties.setProperty("dispatcher.task.delay.count", "1");
		((SchedulingPoolImpl)schedulingPool).setDispatcherProperties(properties);
		Task testTask1 = new Task();
		testTask1.setService(service1);
		testTask1.setFacility(facility1);
		testTask1.setId(0);
		testTask1.setStatus(TaskStatus.WAITING);
		Task testTask2 = new Task();
		testTask2.setId(2);
		testTask2.setService(service2);
		testTask2.setFacility(facility1);
		testTask2.setStatus(TaskStatus.WAITING);
		
		return new Task[]{testTask1, testTask2};
	}

	private abstract class AbstractTaskSchedulerSpy extends TaskScheduler {
		@Override
		protected void initPerunSession() throws InternalErrorException {
		}
	}

	/* WHY????
	private class SchedulingPoolRecurrenceSpy extends SchedulingPoolImpl {
		@Override
		public void scheduleTask(Task task, int delayCount) {
			super.scheduleTask(task, delayCount);
		}
	}
	*/

	private class SimpleTaskSchedulerSpy extends AbstractTaskSchedulerSpy {
		int scheduledCounter = 0;
		int scheduleLimit;
		boolean testFailed = true;
		FutureTask task;

		public SimpleTaskSchedulerSpy(int scheduleLimit) {
			this.scheduleLimit = scheduleLimit;
		}

		public void setTask(FutureTask task) {
			this.task = task;
		}

		@Override
		protected TaskScheduled sendToEngine(Task task) {
			if (task.isSourceUpdated() && !task.isPropagationForced()) {
				testFailed = true;
				this.task.cancel(true);
				return TaskScheduled.ERROR;
			}
			scheduledCounter += 1;
			if (scheduledCounter >= scheduleLimit) {
				testFailed = false;
				this.task.cancel(true);
				stop();
			}
			return TaskScheduled.SUCCESS;
		}
	}

}
