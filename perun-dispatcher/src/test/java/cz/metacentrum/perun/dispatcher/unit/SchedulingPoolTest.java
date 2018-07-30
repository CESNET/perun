package cz.metacentrum.perun.dispatcher.unit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.dispatcher.AbstractDispatcherTest;
import cz.metacentrum.perun.taskslib.exceptions.TaskStoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.util.Assert;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.dispatcher.jms.EngineMessageProducer;
import cz.metacentrum.perun.dispatcher.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

/**
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class SchedulingPoolTest extends AbstractDispatcherTest {

	private final static Logger log = LoggerFactory.getLogger(SchedulingPoolTest.class);

	@Autowired
	private SchedulingPool schedulingPool;
	private EngineMessageProducer engineMessageProducer;
	private List<Destination> destinations = new ArrayList<Destination>() {{
		add(new Destination(1, "par_dest1", "host", "PARALLEL"));
		add(new Destination(2, "par_dest2", "host", "PARALLEL"));
		add(new Destination(3, "one_dest1", "host", "ONE"));
		add(new Destination(4, "one_dest2", "host", "ONE"));
	}};

	private Task task1;
	private Task task2;

	@Before
	public void setup() throws InternalErrorException, TaskStoreException {
		task1 = new Task();
		task1.setId(1);
		task1.setService(service1);
		task1.setFacility(facility1);
		task1.setDestinations(destinations);

		task1.setStatus(TaskStatus.WAITING);
		task1.setSchedule(new Date(System.currentTimeMillis()));
		engineMessageProducer = new EngineMessageProducer(1, "test-queue");
		schedulingPool.addToPool(task1, engineMessageProducer);
	}

	@After
	public void cleanup() {
		schedulingPool.clear();
	}

	@IfProfileValue(name = "perun.test.groups", values = ("unit-tests"))
	@Test
	public void addToPoolTest() throws InternalErrorException, TaskStoreException {
		System.out.println("SchedulingPool.addTask()");

		Assert.isTrue(schedulingPool.getSize() == 1, "original size is 1");
		schedulingPool.addToPool(task1, engineMessageProducer);
		Assert.isTrue(schedulingPool.getSize() == 1, "new size is 1"); // pool already contains this task
		task2 = new Task();
		task2.setId(2);
		task2.setService(service2);
		task2.setFacility(facility1);
		task2.setDestinations(destinations);
		task2.setSchedule(new Date(System.currentTimeMillis()));
		schedulingPool.addToPool(task2, engineMessageProducer);
		Assert.isTrue(schedulingPool.getSize() == 2, "new size is 2");
	}

	@IfProfileValue(name = "perun.test.groups", values = ("unit-tests"))
	@Test
	public void getFromPoolTest() {
		System.out.println("SchedulingPool.getFromPool()");
		List<Task> tasks = schedulingPool.getTasksWithStatus(TaskStatus.DONE);
		Assert.isTrue(tasks.isEmpty(), "done list is empty");
		tasks = schedulingPool.getTasksWithStatus(TaskStatus.WAITING);
		log.debug("new size: " + tasks.size());
		Assert.isTrue(tasks.size() == 1, "new list has size 1");
		Assert.isTrue(task1 == tasks.get(0), "task equals");
	}

/*	@IfProfileValue(name = "perun.test.groups", values = ("unit-tests"))
	@Test
	public void setTaskStatusTest() {
		System.out.println("SchedulingPool.setTaskStatusTest()");
		schedulingPool.setTaskStatus(task1, TaskStatus.PROCESSING);
		List<Task> tasks = schedulingPool.getWaitingTasks();
		Assert.isTrue(tasks.isEmpty());
		tasks = schedulingPool.getProcessingTasks();
		Assert.isTrue(tasks.size() == 1);
		Assert.isTrue(task1 == tasks.get(0));
	}

	@IfProfileValue(name = "perun.test.groups", values = ("unit-tests"))
	@Test
	public void getTaskByIdTest() {
		System.out.println("SchedulingPool.getTaskById()");
		Task task = schedulingPool.getTaskById(task1.getId());
		Assert.isTrue(task == task1);
	}*/
}
